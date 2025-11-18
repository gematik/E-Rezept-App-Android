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

/*
 * Copyright 2025, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 */

package de.gematik.ti.erp.app.mlkitscanner.usecase

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text.TextBlock
import com.google.mlkit.vision.text.TextRecognizer
import de.gematik.ti.erp.app.mlkitscanner.model.CardScannerData.ScanRegion
import de.gematik.ti.erp.app.mlkitscanner.usecase.CardScannerUseCase.Companion.DetectedText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.math.sqrt

class CardScannerUseCase(
    private val textRecognizer: TextRecognizer,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val CAN_DIGIT_COUNT = 6

        data class DetectedText(
            val text: String,
            val boundingBox: Rect
        )
    }

    private val canPattern = """\b\d{$CAN_DIGIT_COUNT}\b""".toRegex()

    suspend operator fun invoke(
        imageProxy: ImageProxy,
        scanRegion: ScanRegion? = null,
        healthCardReference: String
    ): Result<String?> = withContext(dispatcher) {
        runCatching {
            val detectedTexts = recognizeAllTextFromImage(imageProxy, scanRegion)
            findCanNearHealthCard(detectedTexts, healthCardReference)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private suspend fun recognizeAllTextFromImage(
        imageProxy: ImageProxy,
        scanRegion: ScanRegion?
    ): List<DetectedText> = suspendCancellableCoroutine { continuation ->

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            continuation.cancel(Exception("No image available"))
            return@suspendCancellableCoroutine
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val detectedTexts = visionText.textBlocks.extractTextElements { boundingBox ->
                    scanRegion?.toRect()?.contains(boundingBox) ?: true
                }
                continuation.resume(detectedTexts)
            }
            .addOnFailureListener(continuation::cancel)
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun findCanNearHealthCard(
        detectedTexts: List<DetectedText>,
        healthCardReference: String
    ): String? {
        val healthCardText = detectedTexts.find { detectedText ->
            detectedText.text.lowercase().contains(healthCardReference)
        } ?: return null

        val possibleCanNumbers = detectedTexts.mapNotNull { detectedText ->
            canPattern.find(detectedText.text)?.let { match ->
                detectedText.copy(text = match.value)
            }
        }

        return if (possibleCanNumbers.isNotEmpty()) {
            findClosestCanToHealthCard(possibleCanNumbers, healthCardText)
        } else {
            null
        }
    }

    private fun findClosestCanToHealthCard(
        possibleCanNumbers: List<DetectedText>,
        healthCardText: DetectedText
    ): String? {
        val healthCardCenter = healthCardText.boundingBox.getCenterPoint()

        return possibleCanNumbers.minByOrNull { possibleCan ->
            val canCenter = possibleCan.boundingBox.getCenterPoint()
            calculateDistance(healthCardCenter, canCenter)
        }?.text ?: possibleCanNumbers.firstOrNull()?.text
    }

    private fun calculateDistance(point1: Pair<Float, Float>, point2: Pair<Float, Float>): Float {
        val deltaX = point1.first - point2.first
        val deltaY = point1.second - point2.second
        return sqrt(deltaX * deltaX + deltaY * deltaY)
    }
}

private fun List<TextBlock>.extractTextElements(
    filter: (Rect) -> Boolean = { true }
): List<DetectedText> {
    return this
        .flatMap { it.lines }
        .flatMap { it.elements }
        .mapNotNull { element ->
            element.boundingBox?.let { boundingBox ->
                if (filter(boundingBox)) {
                    DetectedText(element.text, boundingBox)
                } else {
                    null
                }
            }
        }
}

private fun Rect.getCenterPoint(): Pair<Float, Float> {
    return Pair(
        centerX().toFloat(),
        centerY().toFloat()
    )
}
