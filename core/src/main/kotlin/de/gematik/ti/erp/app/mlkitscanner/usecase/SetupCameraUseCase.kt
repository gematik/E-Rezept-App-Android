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

package de.gematik.ti.erp.app.mlkitscanner.usecase

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutorService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class CameraSetupParams(
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    val previewView: PreviewView,
    val cameraExecutor: ExecutorService,
    val onImageAnalysis: (ImageProxy) -> Unit,
    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
)

class SetupCameraUseCase {
    suspend operator fun invoke(params: CameraSetupParams): Camera? {
        return suspendCancellableCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(params.context)

            cameraProviderFuture.addListener({
                try {
                    if (!continuation.isActive) return@addListener

                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = params.previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(params.cameraExecutor, params.onImageAnalysis)
                        }

                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        params.lifecycleOwner,
                        params.cameraSelector,
                        preview,
                        imageAnalysis
                    )

                    continuation.resume(camera)
                } catch (exception: Exception) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(exception)
                    }
                }
            }, ContextCompat.getMainExecutor(params.context))
        }
    }
}
