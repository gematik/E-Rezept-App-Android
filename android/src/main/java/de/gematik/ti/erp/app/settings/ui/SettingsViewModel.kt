/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.settings.ui

import android.content.SharedPreferences
import androidx.compose.runtime.Immutable
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.SCREENSHOTS_ALLOWED
import androidx.lifecycle.ViewModel
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesWithPairedDevicesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.protocol.model.AuditEventData
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import de.gematik.ti.erp.app.analytics.Analytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

object SettingsScreen {
    @Immutable
    data class State(
        val analyticsAllowed: Boolean,
        val authenticationMode: SettingsData.AuthenticationMode,
        val zoomEnabled: Boolean,
        val screenshotsAllowed: Boolean,
        val profiles: List<ProfilesUseCaseData.Profile>
    ) {
        fun activeProfile() = profiles.find { it.active }!!
        fun profileById(profileId: String) = profiles.find { it.id == profileId }
        fun containsProfileWithName(name: String) = profiles.any {
            it.name.equals(name.trim(), true)
        }
    }

    val defaultState = State(
        analyticsAllowed = false,
        authenticationMode = SettingsData.AuthenticationMode.Unspecified,
        zoomEnabled = false,
        // `gemSpec_eRp_FdV A_20203` default settings does not allow screenshots
        screenshotsAllowed = false,
        profiles = listOf()
    )
}

class SettingsViewModel(
    private val settingsUseCase: SettingsUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val profilesWithPairedDevicesUseCase: ProfilesWithPairedDevicesUseCase,
    private val analytics: Analytics,
    private val appPrefs: SharedPreferences,
    private val dispatchers: DispatchProvider
) : ViewModel() {

    private var screenshotsAllowed =
        MutableStateFlow(appPrefs.getBoolean(SCREENSHOTS_ALLOWED, false))

    fun screenState() = combine(
        analytics.trackingAllowed,
        settingsUseCase.general,
        settingsUseCase.authenticationMode,
        screenshotsAllowed,
        profilesUseCase.profiles
    ) { analyticsAllowed, settings, authenticationMode, screenshotsAllowed, profiles ->
        SettingsScreen.State(
            zoomEnabled = settings.zoomEnabled,
            analyticsAllowed = analyticsAllowed,
            authenticationMode = authenticationMode,
            screenshotsAllowed = screenshotsAllowed,
            profiles = profiles
        )
    }.flowOn(dispatchers.Default)

    fun pairedDevices(profileId: ProfileIdentifier) =
        profilesWithPairedDevicesUseCase.pairedDevices(profileId)

    // tag::DeletePairedDevicesViewModel[]
    suspend fun deletePairedDevice(profileId: ProfileIdentifier, device: ProfilesUseCaseData.PairedDevice) =
        profilesWithPairedDevicesUseCase.deletePairedDevices(profileId, device)

    // end::DeletePairedDevicesViewModel[]
    fun decryptedAccessToken(profile: ProfilesUseCaseData.Profile) =
        profilesUseCase.decryptedAccessToken(profile.id)

    fun onSelectDeviceSecurityAuthenticationMode() =
        viewModelScope.launch(Dispatchers.IO) {
            settingsUseCase.saveAuthenticationMode(
                SettingsData.AuthenticationMode.DeviceSecurity
            )
        }

    fun onSelectPasswordAsAuthenticationMode(password: String) =
        viewModelScope.launch(Dispatchers.IO) {
            settingsUseCase.saveAuthenticationMode(SettingsData.AuthenticationMode.Password(password = password))
        }

    fun onSwitchAllowScreenshots(allowScreenshots: Boolean) {
        appPrefs.edit {
            putBoolean(SCREENSHOTS_ALLOWED, allowScreenshots)
        }
        screenshotsAllowed.value = allowScreenshots
    }

    fun onEnableZoom() {
        viewModelScope.launch {
            settingsUseCase.saveZoomPreference(true)
        }
    }

    fun onDisableZoom() {
        viewModelScope.launch {
            settingsUseCase.saveZoomPreference(false)
        }
    }

    fun onTrackingAllowed() {
        analytics.allowTracking()
    }

    fun onTrackingDisallowed() {
        analytics.disallowTracking()
    }

    fun logout(profile: ProfilesUseCaseData.Profile) {
        viewModelScope.launch {
            profilesUseCase.logout(profile)
        }
    }

    fun addProfile(profileName: String) {
        viewModelScope.launch {
            profilesUseCase.addProfile(profileName, activate = true)
        }
    }

    fun removeProfile(profile: ProfilesUseCaseData.Profile, newProfileName: String?) {
        viewModelScope.launch {
            if (newProfileName != null) {
                profilesUseCase.removeAndSaveProfile(profile, newProfileName)
            } else {
                profilesUseCase.removeProfile(profile)
            }
        }
    }

    fun switchProfile(profile: ProfilesUseCaseData.Profile) {
        viewModelScope.launch {
            profilesUseCase.switchActiveProfile(profile)
        }
    }

    fun loadAuditEventsForProfile(profileId: ProfileIdentifier): Flow<PagingData<AuditEventData.AuditEvent>> =
        profilesUseCase.auditEvents(profileId)

    suspend fun onboardingSucceeded(
        authenticationMode: SettingsData.AuthenticationMode,
        defaultProfileName: String,
        allowTracking: Boolean
    ) {
        settingsUseCase.onboardingSucceeded(
            authenticationMode = authenticationMode,
            defaultProfileName = defaultProfileName
        )
        if (allowTracking) {
            onTrackingAllowed()
        } else {
            onTrackingDisallowed()
        }
    }
}
