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
 */

package de.gematik.ti.erp.app.cardwall.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.Camera
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.mlkitscanner.model.CardScannerData.ScanRegionParams
import de.gematik.ti.erp.app.mlkitscanner.usecase.CalculateScanRegionUseCase
import de.gematik.ti.erp.app.mlkitscanner.usecase.CameraSetupParams
import de.gematik.ti.erp.app.mlkitscanner.usecase.CardScannerUseCase
import de.gematik.ti.erp.app.mlkitscanner.usecase.SetupCameraUseCase
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

enum class ReferenceStrategy {
    DEBUG_ONLY,
    RELEASE_ONLY,
    TOGGLE_BETWEEN_DEBUG_AND_RELEASE
}

@Stable
class CardWallScannerController(
    private val cardScannerUseCase: CardScannerUseCase,
    private val setupCameraUseCase: SetupCameraUseCase,
    private val calculateScanRegionUseCase: CalculateScanRegionUseCase
) : Controller() {

    private val _detectedCan = MutableStateFlow<String?>(null)
    private val _isScanning = MutableStateFlow(true)
    private val _camera = MutableStateFlow<Camera?>(null)
    private val _hasPermission = MutableStateFlow(false)

    private val referenceStrategy = if (BuildConfigExtension.isInternalDebug) {
        ReferenceStrategy.TOGGLE_BETWEEN_DEBUG_AND_RELEASE
    } else {
        ReferenceStrategy.RELEASE_ONLY
    }

    private var toggleFlag = true

    val detectedCan: StateFlow<String?> = _detectedCan.asStateFlow()
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    val camera: StateFlow<Camera?> = _camera.asStateFlow()
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    init {
        reset()
    }

    override fun onCleared() {
        super.onCleared()
        reset()
    }

    fun reset() {
        controllerScope.launch {
            _detectedCan.value = null
            _isScanning.value = true
            _camera.value = null
            toggleFlag = true
        }
    }

    fun checkCameraPermission(context: Context) {
        val hasPermission = context.checkSelfPermission(Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        _hasPermission.value = hasPermission
    }

    fun setupCamera(params: CameraSetupParams) {
        controllerScope.launch {
            try {
                val camera = setupCameraUseCase(params)
                _camera.value = camera
            } catch (_: Exception) {
                _camera.value = null
            }
        }
    }

    fun toggleFlashlight(enabled: Boolean) {
        controllerScope.launch {
            _camera.value
                ?.takeIf { it.cameraInfo.hasFlashUnit() }
                ?.cameraControl
                ?.enableTorch(enabled)
        }
    }

    fun processImageFrame(
        imageProxy: ImageProxy,
        previewView: PreviewView,
        density: Density,
        screenWidth: Int,
        screenHeight: Int,
        debugHealthCardReference: String,
        releaseHealthCardReference: String
    ) {
        if (!_isScanning.value) {
            imageProxy.close()
            return
        }

        controllerScope.launch {
            val scanRegion = calculateScanRegionUseCase.calculateWithScreenCoordinates(
                ScanRegionParams(
                    imageProxy = imageProxy,
                    previewView = previewView,
                    density = density
                ),
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            val currentReference = when (referenceStrategy) {
                ReferenceStrategy.DEBUG_ONLY -> debugHealthCardReference
                ReferenceStrategy.RELEASE_ONLY -> releaseHealthCardReference
                ReferenceStrategy.TOGGLE_BETWEEN_DEBUG_AND_RELEASE -> {
                    val ref = if (toggleFlag) debugHealthCardReference else releaseHealthCardReference
                    toggleFlag = !toggleFlag
                    ref
                }
            }

            cardScannerUseCase(imageProxy, scanRegion, currentReference)
                .onSuccess { detectedCan ->
                    detectedCan?.let { can ->
                        _detectedCan.value = can
                        _isScanning.value = false
                        Napier.d("CAN detected using ${if (!toggleFlag) "debug" else "release"} reference")
                    }
                }
                .onFailure {
                    Napier.e("Error processing image frame with ${if (!toggleFlag) "debug" else "release"} reference", it)
                }
        }
    }
}

@Composable
fun rememberScannerController(): CardWallScannerController {
    val cardScannerUseCase by rememberInstance<CardScannerUseCase>()
    val setupCameraUseCase by rememberInstance<SetupCameraUseCase>()
    val calculateScanRegionUseCase by rememberInstance<CalculateScanRegionUseCase>()
    return remember {
        CardWallScannerController(
            cardScannerUseCase = cardScannerUseCase,
            setupCameraUseCase = setupCameraUseCase,
            calculateScanRegionUseCase = calculateScanRegionUseCase
        )
    }
}
