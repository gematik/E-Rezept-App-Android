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
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationType
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.model.hasEnabledPushNotificationSetting
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.AcceptPushNotificationPermissionUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.DeclinePushNotificationPermissionUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.ProfilePushNotificationState
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.SyncProfilePushNotificationStateUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.UpdateProfilePushNotificationSettingUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

sealed interface ProfilePushNotificationsUiState {
    data object LoggedOut : ProfilePushNotificationsUiState
    data object RegistrationRequired : ProfilePushNotificationsUiState
    data object Disabled : ProfilePushNotificationsUiState
    data class Loading(
        val previousSettings: ProfilePushNotificationSettings? = null
    ) : ProfilePushNotificationsUiState
    data class Ready(
        val settings: ProfilePushNotificationSettings
    ) : ProfilePushNotificationsUiState
    data class Error(
        val previousSettings: ProfilePushNotificationSettings?,
        val isInitialSetup: Boolean
    ) : ProfilePushNotificationsUiState
}

class ProfilePushNotificationsController(
    profileId: ProfileIdentifier,
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    biometricAuthenticator: BiometricAuthenticator,
    networkStatusTracker: NetworkStatusTracker,
    private val acceptPushNotificationPermissionUseCase: AcceptPushNotificationPermissionUseCase,
    private val declinePushNotificationPermissionUseCase: DeclinePushNotificationPermissionUseCase,
    private val updateProfilePushNotificationSettingUseCase: UpdateProfilePushNotificationSettingUseCase,
    private val syncProfilePushNotificationStateUseCase: SyncProfilePushNotificationStateUseCase
) : ChooseAuthenticationController(
    profileId = profileId,
    getProfileByIdUseCase = getProfileByIdUseCase,
    getProfilesUseCase = getProfilesUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    biometricAuthenticator = biometricAuthenticator,
    networkStatusTracker = networkStatusTracker
) {

    private val selectedProfileId = profileId
    private val _uiState = MutableStateFlow<ProfilePushNotificationsUiState>(ProfilePushNotificationsUiState.LoggedOut)
    private var pendingSyncAction: PendingPushNotificationSyncAction? = null
    private var syncJob: Job? = null

    val uiState: StateFlow<ProfilePushNotificationsUiState> = _uiState.asStateFlow()
    val showEnableNotificationsEvent = ComposableEvent<Unit>()

    fun onAuthenticationStateChanged(isAuthenticated: Boolean) {
        if (!isAuthenticated) {
            syncJob?.cancel()
            pendingSyncAction = null
            _uiState.value = ProfilePushNotificationsUiState.LoggedOut
        } else if (_uiState.value is ProfilePushNotificationsUiState.LoggedOut) {
            startPushNotificationSync(PendingPushNotificationSyncAction.Refresh)
        }
    }

    fun onToggle(type: ProfilePushNotificationType, enabled: Boolean) {
        if (
            _uiState.value is ProfilePushNotificationsUiState.Ready ||
            _uiState.value is ProfilePushNotificationsUiState.Disabled
        ) {
            startPushNotificationSync(PendingPushNotificationSyncAction.Toggle(type, enabled))
        }
    }

    fun onPermissionPromptDecision(accepted: Boolean) {
        startPushNotificationSync(
            if (accepted) {
                PendingPushNotificationSyncAction.InitialSetup
            } else {
                PendingPushNotificationSyncAction.DeclineInitialSetup
            }
        )
    }

    fun retryPushNotificationSync() {
        pendingSyncAction?.let(::startPushNotificationSync)
    }

    fun dismissPushNotificationSyncError() {
        val action = pendingSyncAction
        val settings = (_uiState.value as? ProfilePushNotificationsUiState.Error)?.previousSettings
        pendingSyncAction = null
        _uiState.value = if (action is PendingPushNotificationSyncAction.Toggle && settings != null) {
            settings.toStableUiState()
        } else if (
            action is PendingPushNotificationSyncAction.InitialSetup ||
            action is PendingPushNotificationSyncAction.DeclineInitialSetup
        ) {
            ProfilePushNotificationsUiState.Disabled
        } else {
            ProfilePushNotificationsUiState.RegistrationRequired
        }
    }

    private fun startPushNotificationSync(action: PendingPushNotificationSyncAction) {
        if (syncJob?.isActive == true) return

        val previousSettings = when (val state = _uiState.value) {
            is ProfilePushNotificationsUiState.Ready -> state.settings
            is ProfilePushNotificationsUiState.Loading -> state.previousSettings
            is ProfilePushNotificationsUiState.Error -> state.previousSettings
            is ProfilePushNotificationsUiState.Disabled -> disabledSettings()
            else -> null
        }
        pendingSyncAction = action
        _uiState.value = ProfilePushNotificationsUiState.Loading(previousSettings)
        syncJob = controllerScope.launch {
            try {
                val profileState = resolveProfileState(action, previousSettings)
                pendingSyncAction = null
                updateUiState(profileState, action)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.value = ProfilePushNotificationsUiState.Error(
                    previousSettings = previousSettings,
                    isInitialSetup = action !is PendingPushNotificationSyncAction.Toggle
                )
            }
        }
    }

    private suspend fun resolveProfileState(
        action: PendingPushNotificationSyncAction,
        previousSettings: ProfilePushNotificationSettings?
    ): ProfilePushNotificationState = when (action) {
        PendingPushNotificationSyncAction.Refresh ->
            syncProfilePushNotificationStateUseCase(selectedProfileId).getOrThrow()
        PendingPushNotificationSyncAction.InitialSetup ->
            ProfilePushNotificationState.Registered(
                acceptPushNotificationPermissionUseCase(selectedProfileId).getOrThrow()
            )
        PendingPushNotificationSyncAction.DeclineInitialSetup -> {
            declinePushNotificationPermissionUseCase(selectedProfileId).getOrThrow()
            ProfilePushNotificationState.Disabled
        }
        is PendingPushNotificationSyncAction.Toggle ->
            ProfilePushNotificationState.Registered(
                updateProfilePushNotificationSettingUseCase(
                    profileId = selectedProfileId,
                    currentSettings = previousSettings ?: ProfilePushNotificationSettings(),
                    notificationType = action.type,
                    enabled = action.enabled
                ).getOrThrow()
            )
    }

    private fun updateUiState(
        profileState: ProfilePushNotificationState,
        action: PendingPushNotificationSyncAction
    ) {
        _uiState.value = when (profileState) {
            ProfilePushNotificationState.RegistrationRequired ->
                ProfilePushNotificationsUiState.RegistrationRequired
            ProfilePushNotificationState.Disabled ->
                ProfilePushNotificationsUiState.Disabled
            is ProfilePushNotificationState.Registered ->
                profileState.settings.toStableUiState()
        }
        if (
            profileState is ProfilePushNotificationState.Registered &&
            action is PendingPushNotificationSyncAction.InitialSetup
        ) {
            showEnableNotificationsEvent.trigger(Unit)
        }
    }
}

private sealed interface PendingPushNotificationSyncAction {
    data object Refresh : PendingPushNotificationSyncAction
    data object InitialSetup : PendingPushNotificationSyncAction
    data object DeclineInitialSetup : PendingPushNotificationSyncAction
    data class Toggle(
        val type: ProfilePushNotificationType,
        val enabled: Boolean
    ) : PendingPushNotificationSyncAction
}

@Composable
fun rememberProfilePushNotificationsController(
    profileId: ProfileIdentifier
): ProfilePushNotificationsController {
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val acceptPushNotificationPermissionUseCase by rememberInstance<AcceptPushNotificationPermissionUseCase>()
    val declinePushNotificationPermissionUseCase by rememberInstance<DeclinePushNotificationPermissionUseCase>()
    val updateProfilePushNotificationSettingUseCase by rememberInstance<UpdateProfilePushNotificationSettingUseCase>()
    val syncProfilePushNotificationStateUseCase by rememberInstance<SyncProfilePushNotificationStateUseCase>()
    val biometricAuthenticator = LocalBiometricAuthenticator.current

    return remember(profileId) {
        ProfilePushNotificationsController(
            profileId = profileId,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            biometricAuthenticator = biometricAuthenticator,
            networkStatusTracker = networkStatusTracker,
            acceptPushNotificationPermissionUseCase = acceptPushNotificationPermissionUseCase,
            declinePushNotificationPermissionUseCase = declinePushNotificationPermissionUseCase,
            updateProfilePushNotificationSettingUseCase = updateProfilePushNotificationSettingUseCase,
            syncProfilePushNotificationStateUseCase = syncProfilePushNotificationStateUseCase
        )
    }
}

private fun ProfilePushNotificationSettings.toStableUiState(): ProfilePushNotificationsUiState =
    if (hasEnabledPushNotificationSetting()) {
        ProfilePushNotificationsUiState.Ready(this)
    } else {
        ProfilePushNotificationsUiState.Disabled
    }

private fun disabledSettings() = ProfilePushNotificationSettings(
    newPrescriptionEnabled = false,
    newMessageEnabled = false,
    statusChangeEnabled = false,
    newInvoiceEnabled = false,
    externalAccessEnabled = false
)
