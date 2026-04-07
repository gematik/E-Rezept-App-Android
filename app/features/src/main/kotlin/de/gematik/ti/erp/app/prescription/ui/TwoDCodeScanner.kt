/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.prescription.ui

import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.Rect
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import de.gematik.ti.erp.app.Requirement
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.math.roundToInt

private const val DEFAULT_SCAN_TIME = 250L

@Requirement(
    "O.Source_1#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "analyse the camera input"
)
@Requirement(
    "O.Data_9#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The device camera is also used for scanning data matrix codes."
)
class TwoDCodeScanner : ImageAnalysis.Analyzer {
    data class ScannedCode(
        val rawBytes: ByteArray?,
        val text: String,
        val format: BarcodeFormat,
        val cornerPoints: Array<Point>,
        val boundingBox: Rect
    )

    data class Batch(
        val matrixCodes: List<ScannedCode>,
        val cameraSize: Size = Size(0, 0),
        val cameraRotation: Int = 0,
        val averageScanTime: Long = DEFAULT_SCAN_TIME
    )

    var batch: MutableSharedFlow<Batch> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 3,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
        private set

    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    private val reader by lazy {
        MultiFormatReader().apply {
            setHints(
                mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                        BarcodeFormat.QR_CODE,
                        BarcodeFormat.DATA_MATRIX
                    ),
                    DecodeHintType.TRY_HARDER to true
                )
            )
        }
    }

    private val countLock = Any()
    private var averageTime = DEFAULT_SCAN_TIME

    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.format !in supportedImageFormats) {
            imageProxy.close()
            return
        }

        val t0 = System.currentTimeMillis()
        val (detectedCodes, frameSize) = try {
            decode(imageProxy)
        } catch (e: Exception) {
            Napier.e(tag = "2DScanner", message = "2D code processing error", throwable = e)
            emptyList<ScannedCode>() to Size(0, 0)
        } finally {
            synchronized(countLock) {
                averageTime = ((System.currentTimeMillis() - t0) + averageTime) / 2
            }
            imageProxy.close()
        }

        if (detectedCodes.isNotEmpty()) {
            val detectedText = detectedCodes.first().text
            Napier.d(tag = "2DScanner") {
                "Detected: format=${detectedCodes.first().format} text='$detectedText'"
            }
            batch.tryEmit(
                Batch(
                    matrixCodes = detectedCodes,
                    cameraSize = frameSize,
                    cameraRotation = imageProxy.imageInfo.rotationDegrees,
                    averageScanTime = averageTime
                )
            )
        }
    }

    private fun decode(imageProxy: ImageProxy): Pair<List<ScannedCode>, Size> {
        val yPlane = imageProxy.planes.firstOrNull() ?: return emptyList<ScannedCode>() to Size(0, 0)
        val dataWidth = yPlane.rowStride
        val dataHeight = imageProxy.height
        val cropWidth = imageProxy.width
        val cropHeight = imageProxy.height

        val buffer = yPlane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val source = PlanarYUVLuminanceSource(
            bytes,
            dataWidth,
            dataHeight,
            0,
            0,
            cropWidth,
            cropHeight,
            false
        )

        val primarySize = Size(source.width, source.height)

        fun decodeOnce(ls: com.google.zxing.LuminanceSource, size: Size): List<ScannedCode> {
            return try {
                val binaryBitmap = BinaryBitmap(HybridBinarizer(ls))
                val result = reader.decodeWithState(binaryBitmap)
                result?.toScannedCode(size.width, size.height)?.let { listOf(it) } ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            } finally {
                reader.reset()
            }
        }

        decodeOnce(source, primarySize).takeIf { it.isNotEmpty() }?.let { return it to primarySize }

        if (source.isRotateSupported) {
            val rotated = source.rotateCounterClockwise()
            val rotatedSize = Size(rotated.width, rotated.height)
            decodeOnce(rotated, rotatedSize).takeIf { it.isNotEmpty() }?.let { return it to rotatedSize }
        }

        return emptyList<ScannedCode>() to primarySize
    }

    private fun Result.toScannedCode(imageWidth: Int, imageHeight: Int): ScannedCode? {
        val points = resultPoints?.takeIf { it.isNotEmpty() }?.map {
            Point(it.x.roundToInt(), it.y.roundToInt())
        } ?: return null

        val minX = points.minOf { it.x }.coerceIn(0, imageWidth - 1)
        val maxX = points.maxOf { it.x }.coerceIn(minX + 1, imageWidth)
        val minY = points.minOf { it.y }.coerceIn(0, imageHeight - 1)
        val maxY = points.maxOf { it.y }.coerceIn(minY + 1, imageHeight)

        val box = Rect(minX, minY, maxX, maxY)

        return ScannedCode(
            rawBytes = rawBytes,
            text = text.orEmpty(),
            format = barcodeFormat,
            cornerPoints = points.toTypedArray(),
            boundingBox = box
        )
    }
}
