/*
 * Copyright (c) 2021 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.ui

import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import android.util.Size
import androidx.core.graphics.minus
import com.google.mlkit.vision.barcode.Barcode
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

data class Metrics(
    val camImageSize: Size = Size(0, 0),
    val camRotation: Int = 0,
    val screenSize: Size = Size(0, 0)
) {
    val aidRect = Rect(
        0,
        (camImageSize.height * 0.15).toInt(),
        camImageSize.width,
        camImageSize.height - (camImageSize.height * 0.10).toInt()
    )
    val glueDistance =
        (camImageSize.height * 0.10).toInt()

    // from https://github.com/googlesamples/mlkit/blob/1e79fb6ba939b5dc8ce620a791cd9d5556f34e6f/android/vision-quickstart/app/src/main/java/com/google/mlkit/vision/demo/GraphicOverlay.java#L197
    // licenced under Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
    val transformationMatrix = Matrix().apply {
        if (camImageSize.width <= 0 || camImageSize.height <= 0) {
            return@apply
        }

        val viewAspectRatio: Float = screenSize.width.toFloat() / screenSize.height.toFloat()
        val imageAspectRatio: Float = camImageSize.width.toFloat() / camImageSize.height

        var postScaleWidthOffset = 0f
        var postScaleHeightOffset = 0f

        val scaleFactor: Float
        if (viewAspectRatio > imageAspectRatio) {
            // The image needs to be vertically cropped to be displayed in this view.
            scaleFactor = screenSize.width.toFloat() / camImageSize.width
            postScaleHeightOffset =
                (screenSize.width.toFloat() / imageAspectRatio - screenSize.height.toFloat()) / 2
        } else {
            // The image needs to be horizontally cropped to be displayed in this view.
            scaleFactor = screenSize.height.toFloat() / camImageSize.height
            postScaleWidthOffset =
                (screenSize.height.toFloat() * imageAspectRatio - screenSize.width) / 2
        }

        this.setScale(scaleFactor, scaleFactor)
        this.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset)
    }
}

class TwoDCodeProcessor @Inject constructor() {
    private fun Rect.center() = Point(this.centerX(), this.centerY())
    private fun Size.center() = Point(this.width / 2, this.height / 2)

    private var metrics = Metrics()
    private val pointArray = FloatArray(8)

    fun onLayoutChange(screen: Size) {
        metrics = metrics.copy(screenSize = screen)
    }

    private data class GluedCode(val center: Point, val id: Int)

    // The "glued" code matches the first focused code;
    // after a movement over a certain threshold, this code is no more glued.
    private var gluedCode = GluedCode(Point(), 0)

    private var codeHist = listOf<Pair<Barcode, Long>>()

    fun process(batch: TwoDCodeScanner.Batch): Pair<String, FloatArray>? {
        if (batch.cameraRotation != metrics.camRotation || batch.cameraSize != metrics.camImageSize) {
            val cs = batch.cameraSize
            metrics = metrics.copy(
                camImageSize = if (batch.cameraRotation == 90 || batch.cameraRotation == 270) {
                    Size(cs.height, cs.width)
                } else {
                    cs
                }
            )
        }

        val currTime = System.currentTimeMillis()
        val allMatrixCodes = codeHist.filterNot { h ->
            batch.matrixCodes.any {
                h.first.rawValue == it.rawValue
            }
        }.filter {
            currTime - it.second < batch.averageScanTime * AVG_TIME_FACTOR
        } + batch.matrixCodes.map { Pair(it, currTime) }

        codeHist = allMatrixCodes

        // process only codes within aidRect
        val matrixCodes = allMatrixCodes
            .map { it.first }
//            .filter {
//                metrics.aidRect.contains(it.boundingBox!!)
//            }
            .filter {
                it.boundingBox!!.width()
                    .toFloat() > metrics.camImageSize.width * MIN_DETECTION_FACTOR
            }

        if (matrixCodes.isEmpty()) {
            return null
        }

        // check if clued code exists
        val gluedCodeMatch = matrixCodes.find {
            it.rawValue.hashCode() == gluedCode.id
        }

        // moved over threshold?
        val minDistCode = when {
            gluedCodeMatch != null && movedNotOverThreshold(
                gluedCodeMatch,
                gluedCode,
                metrics.glueDistance
            ) -> {
                gluedCodeMatch
            }
            else -> {
                Timber.d("Moved!!")

                // moved; find code nearest to center
                matrixCodes
                    .map { code ->
                        Pair(
                            code,
                            code.boundingBox!!.let {
                                squaredDistance(
                                    it.center(),
                                    // metrics.aidRect.center(),
                                    metrics.camImageSize.center(),
                                    min(it.width(), it.height()) / 2
                                )
                            }
                        )
                    }
                    .minByOrNull { it.second }!!
                    .first
                    .also {
                        gluedCode = GluedCode(it.boundingBox!!.center(), it.rawValue.hashCode())
                    }
            }
        }

        minDistCode.cornerPoints?.forEachIndexed { index, point ->
            pointArray[index * 2] = point.x.toFloat()
            pointArray[index * 2 + 1] = point.y.toFloat()
        }

        metrics.transformationMatrix.mapPoints(pointArray)

        return Pair(
            minDistCode.rawValue!!,
            pointArray
        )
    }

    private fun squaredDistance(p1: Point, p2: Point, radius: Int): Int =
        squaredDistance(p1, p2) - radius * radius

    private fun squaredDistance(p1: Point, p2: Point): Int {
        val dX = p1.x - p2.x
        val dY = p1.y - p2.y
        val dX2 = dX * dX
        val dY2 = dY * dY

        return dX2 + dY2
    }

    private fun movedNotOverThreshold(
        currentCode: Barcode,
        otherBarcode: GluedCode,
        threshold: Int
    ): Boolean =
        (currentCode.boundingBox!!.center() - otherBarcode.center).let {
            max(it.x.absoluteValue, it.y.absoluteValue) < threshold
        }

    companion object {
        private const val AVG_TIME_FACTOR = 2
        private const val MIN_DETECTION_FACTOR = 1 / 5f
    }
}
