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

package de.gematik.ti.erp.app.pushnotifications.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.api.NoInternetException
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.NetworkStatusTracker.Companion.isNetworkAvailable
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.DeletePusherUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.GetPushersUseCase
import de.gematik.ti.erp.app.pushnotifications.mapper.toUiModel
import de.gematik.ti.erp.app.pushnotifications.provider.FcmTokenProvider
import de.gematik.ti.erp.app.pushnotifications.ui.model.RegisteredDeviceUiModel
import de.gematik.ti.erp.app.pushnotifications.ui.model.RegisteredPushDevicesErrorState.CannotLoadRegisteredPushDevicesError
import de.gematik.ti.erp.app.pushnotifications.ui.model.RegisteredPushDevicesErrorState.NoInternetErrorPush
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class RegisteredDevicesController(
    private val profileId: ProfileIdentifier,
    private val getPushersUseCase: GetPushersUseCase,
    private val deletePusherUseCase: DeletePusherUseCase,
    private val fcmTokenProvider: FcmTokenProvider,
    private val networkStatusTracker: NetworkStatusTracker
) : Controller() {

    private val _registeredDevices = MutableStateFlow<UiState<List<RegisteredDeviceUiModel>>>(UiState.Loading())
    val registeredDevices: StateFlow<UiState<List<RegisteredDeviceUiModel>>> = _registeredDevices.asStateFlow()

    private val _deletingPushKeys = MutableStateFlow<Set<String>>(emptySet())
    val deletingPushKeys: StateFlow<Set<String>> = _deletingPushKeys.asStateFlow()

    private val _devicePendingDeletion = MutableStateFlow<RegisteredDeviceUiModel?>(null)
    val devicePendingDeletion: StateFlow<RegisteredDeviceUiModel?> = _devicePendingDeletion.asStateFlow()

    private val _showDeletionError = MutableStateFlow(false)
    val showDeletionError: StateFlow<Boolean> = _showDeletionError.asStateFlow()

    val deviceDeletedEvent = ComposableEvent<Unit>()

    init {
        refreshRegisteredDevices()
    }

    fun refreshRegisteredDevices() {
        controllerScope.launch {
            _registeredDevices.value = UiState.Loading()
            val currentPushKey = runCatching { fcmTokenProvider.getToken() }.getOrNull()
            getPushersUseCase(profileId)
                .onSuccess { pushers ->
                    _registeredDevices.value = UiState.Data(pushers.map { it.toUiModel(currentPushKey) })
                }
                .onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable
                    Napier.e(throwable) { "Error loading registered push devices" }
                    _registeredDevices.value = UiState.Error(throwable.toErrorState())
                }
        }
    }

    fun requestDeleteDevice(device: RegisteredDeviceUiModel) {
        _devicePendingDeletion.value = device
    }

    fun dismissDeleteDeviceDialog() {
        _devicePendingDeletion.value = null
    }

    fun dismissDeletionError() {
        _showDeletionError.value = false
    }

    fun deleteDevice(device: RegisteredDeviceUiModel) {
        if (device.pushKey in _deletingPushKeys.value) return
        _devicePendingDeletion.value = null
        controllerScope.launch {
            _deletingPushKeys.update { it + device.pushKey }
            deletePusherUseCase(
                profileId = profileId,
                pushKey = device.pushKey,
                appId = device.appId
            )
                .onSuccess {
                    deviceDeletedEvent.trigger(Unit)
                    refreshRegisteredDevices()
                }
                .onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable
                    Napier.e(throwable) { "Error deleting registered push device" }
                    _showDeletionError.value = true
                }
            _deletingPushKeys.update { it - device.pushKey }
        }
    }

    private fun Throwable.toErrorState(): Throwable =
        if (this is NoInternetException || !networkStatusTracker.isNetworkAvailable()) {
            NoInternetErrorPush
        } else {
            CannotLoadRegisteredPushDevicesError
        }
}

@Composable
fun rememberRegisteredDevicesController(
    profileId: ProfileIdentifier
): RegisteredDevicesController {
    val getPushersUseCase by rememberInstance<GetPushersUseCase>()
    val deletePusherUseCase by rememberInstance<DeletePusherUseCase>()
    val fcmTokenProvider by rememberInstance<FcmTokenProvider>()
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()

    return remember(profileId) {
        RegisteredDevicesController(
            profileId = profileId,
            getPushersUseCase = getPushersUseCase,
            deletePusherUseCase = deletePusherUseCase,
            fcmTokenProvider = fcmTokenProvider,
            networkStatusTracker = networkStatusTracker
        )
    }
}
