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
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.SCREENSHOTS_ALLOWED
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.db.entities.ProfileColorNames
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.di.ApplicationPreferences
import de.gematik.ti.erp.app.featuretoggle.FeatureToggleManager
import de.gematik.ti.erp.app.featuretoggle.Features
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import de.gematik.ti.erp.app.tracking.Tracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import javax.inject.Inject

object SettingsScreen {
    enum class AuthenticationMode {
        EHealthCard,
        DeviceSecurity,

        @Deprecated("replaced by deviceSecurity")
        Biometrics,

        @Deprecated("replaced by deviceSecurity")
        DeviceCredentials,
        Password,

        @Deprecated("not available anymore")
        None,
        Unspecified
    }

    @Immutable
    data class State(
        val demoModeActive: Boolean,
        val analyticsAllowed: Boolean,
        val authenticationMode: AuthenticationMode,
        val zoomEnabled: Boolean,
        val screenShotsAllowed: Boolean,
        val uiProfiles: List<ProfilesUseCaseData.Profile>
    ) {
        fun activeProfile() = uiProfiles.find { it.active }!!
        fun profileById(profileId: Int) = uiProfiles.find { it.id == profileId }
        fun containsProfileWithName(name: String) = uiProfiles.any {
            it.name.equals(name.trim(), true)
        }
    }

    val defaultState = State(
        demoModeActive = false,
        analyticsAllowed = false,
        authenticationMode = AuthenticationMode.Unspecified,
        zoomEnabled = false,
        // `gemSpec_eRp_FdV A_20203` default settings does not allow screenshots
        screenShotsAllowed = false,
        uiProfiles = listOf()
    )
}

const val NEW_USER = "newUser"
const val UPDATED_DATA_TERMS_ACCEPTED = "UpdatedDataTermsAccepted"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val demoUseCase: DemoUseCase,
    private val tracker: Tracker,
    @ApplicationPreferences
    private val appPrefs: SharedPreferences,
    private val toggleManager: FeatureToggleManager,
    private val coroutineDispatchProvider: DispatchProvider
) : BaseViewModel() {

    var isNewUser by settingsUseCase::isNewUser

    private var screenshotsAllowed =
        MutableStateFlow(appPrefs.getBoolean(SCREENSHOTS_ALLOWED, false))

    fun screenState() = combine(
        demoUseCase.demoModeActive,
        tracker.trackingAllowed,
        settingsUseCase.settings,
        screenshotsAllowed,
        profilesUseCase.profiles,
    ) { demoActive, analyticsAllowed, settings, screenshotsAllowed, uiProfiles ->
        SettingsScreen.State(
            demoModeActive = demoActive,
            analyticsAllowed = analyticsAllowed,
            authenticationMode = when (settings.authenticationMethod) {
                SettingsAuthenticationMethod.DeviceSecurity -> SettingsScreen.AuthenticationMode.DeviceSecurity
                SettingsAuthenticationMethod.Password -> SettingsScreen.AuthenticationMode.Password
                else -> SettingsScreen.AuthenticationMode.Unspecified
            },
            zoomEnabled = settings.zoomEnabled,
            screenShotsAllowed = screenshotsAllowed,
            uiProfiles = uiProfiles
        )
    }.flowOn(coroutineDispatchProvider.default())

    fun onSelectDeviceSecurityAuthenticationMode() =
        viewModelScope.launch(Dispatchers.IO) {
            settingsUseCase.saveAuthenticationMethod(
                SettingsAuthenticationMethod.DeviceSecurity
            )
        }

    fun onSelectPasswordAsAuthenticationMode(password: String) =
        viewModelScope.launch(Dispatchers.IO) {
            settingsUseCase.savePasswordAsAuthenticationMethod(password)
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

    fun onActivateDemoMode() {
        demoUseCase.activateDemoMode()
    }

    fun onDeactivateDemoMode() {
        demoUseCase.deactivateDemoMode()
    }

    fun onTrackingAllowed() {
        tracker.allowTracking()
    }

    fun onTrackingDisallowed() {
        tracker.disallowTracking()
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

    fun overwriteDefaultProfile(profileName: String) {
        viewModelScope.launch {
            profilesUseCase.overwriteDefaultProfileName(profileName)
        }
    }

    fun removeProfile(profile: ProfilesUseCaseData.Profile, newProfileName: String?) {
        viewModelScope.launch {
            if (newProfileName != null) {
                profilesUseCase.removeProfile(profile, newProfileName)
            } else {
                profilesUseCase.removeProfile(profile)
            }
        }
    }

    fun updateProfileName(profile: ProfilesUseCaseData.Profile, newName: String) {
        viewModelScope.launch {
            profilesUseCase.updateProfileName(profile, newName)
        }
    }

    fun updateProfileColor(profile: ProfilesUseCaseData.Profile, color: ProfileColorNames) {
        viewModelScope.launch {
            profilesUseCase.updateProfileColor(profile, color)
        }
    }

    fun switchProfile(profile: ProfilesUseCaseData.Profile) {
        viewModelScope.launch {
            profilesUseCase.switchActiveProfile(profile)
        }
    }

    fun allowAddProfiles() = toggleManager.isFeatureEnabled(Features.ADD_PROFILE.featureName)

    fun isFeatureBioLoginEnabled() = toggleManager.isFeatureEnabled(Features.BIO_LOGIN.featureName)

    fun isCanAvailable(profile: ProfilesUseCaseData.Profile) =
        runBlocking {
            profilesUseCase.isCanAvailable(profile).first()
        }

    fun loadAuditEventsForProfile(profileName: String): Flow<PagingData<ProfilesUseCaseData.AuditEvent>> =
        profilesUseCase.loadAuditEventsForProfile(profileName)

    fun acceptUpdatedDataTerms(date: LocalDate) {
        viewModelScope.launch {
            settingsUseCase.updatedDataTermsAccepted(date)
        }
    }
}
