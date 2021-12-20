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

package de.gematik.ti.erp.app.settings.ui

import android.content.SharedPreferences
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.SCREENSHOTS_ALLOWED
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.db.entities.ProfileColors
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
            uiProfiles = if (demoActive) profilesUseCase.demoProfiles() else uiProfiles
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
            profilesUseCase.addProfile(profileName)
        }
    }

    fun removeProfile(name: String, newProfileName: String?) {
        viewModelScope.launch {
            profilesUseCase.removeProfile(name, newProfileName)
        }
    }

    fun removeProfile(id: Int, newProfileName: String?) {
        viewModelScope.launch {
            profilesUseCase.getProfileById(id).first()?.name?.let { name ->
                profilesUseCase.removeProfile(name, newProfileName)
            }
        }
    }

    fun updateProfileName(profile: ProfilesUseCaseData.Profile, newName: String) {
        viewModelScope.launch {
            profilesUseCase.updateProfile(profile, newName)
        }
    }

    fun updateProfileColor(profileName: String, color: ProfileColors) {
        viewModelScope.launch {
            profilesUseCase.updateProfileColor(profileName, color)
        }
    }

    fun switchProfile(name: String) {
        viewModelScope.launch {
            profilesUseCase.switchActiveProfile(name)
        }
    }

    fun profilesOn() =
        toggleManager.isFeatureEnabled(Features.MULTI_PROFILE.featureName)

    suspend fun profileSetupViaLaunchedEffect() {
        profilesUseCase.addDefaultProfile()
    }

    fun isCanAvailable(profile: ProfilesUseCaseData.Profile) =
        runBlocking {
            profilesUseCase.isCanAvailable(profile).first()
        }

    companion object {
        const val PROFILES_HINT_SHOWN = "PROFILES_HINT_SHOWN"
    }
}
