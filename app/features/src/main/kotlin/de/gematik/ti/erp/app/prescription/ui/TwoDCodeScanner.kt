/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.prescription.ui

import android.content.Context
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.MlKit
import com.google.mlkit.common.sdkinternal.MlKitContext
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import de.gematik.ti.erp.app.Requirement
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

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
class TwoDCodeScanner(

    private val context: Context
) : ImageAnalysis.Analyzer {
    data class Batch(
        val matrixCodes: List<Barcode>,
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

    @Requirement(
        "O.Data_8#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "This controller uses the camera as an input device. Frames are processed but never " +
            "stored, metadata is never created here."
    )
    private val scanner: BarcodeScanner by lazy {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_DATA_MATRIX)
                .build()
        )
    }

    private val countLock = Any()
    private var averageTime = DEFAULT_SCAN_TIME

    private fun onInitMlKit() {
        try {
            // will throw if not initialized
            MlKitContext.getInstance()
        } catch (_: Exception) {
            MlKit.initialize(context)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        onInitMlKit()

        imageProxy.image?.also {
            val image = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)

            try {
                val t0 = System.currentTimeMillis()
                scanner.process(image)
                    .addOnSuccessListener { matrixCodes ->
                        synchronized(countLock) {
                            averageTime =
                                ((System.currentTimeMillis() - t0) + averageTime) / 2
                        }

                        if (matrixCodes.isNotEmpty()) {
                            batch.tryEmit(
                                Batch(
                                    matrixCodes = matrixCodes,
                                    cameraSize = Size(image.width, image.height),
                                    cameraRotation = image.rotationDegrees
                                )
                            )
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            } catch (e: Exception) {
                Napier.d("2D code processing error", e)
            }
        }
    }
}
