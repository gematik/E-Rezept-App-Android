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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.SCREENSHOTS_ALLOWED
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.di.ApplicationPreferences
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import de.gematik.ti.erp.app.tracking.Tracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsNavigationScreens {
    object Settings : Route("Settings")
    object Terms : Route("Terms")
    object Imprint : Route("Imprint")
    object DataProtection : Route("DataProtection")
    object OpenSourceLicences : Route("OpenSourceLicences")
    object AllowAnalytics : Route("AcceptAnalytics")
    object FeedbackForm : Route("FeedbackForm")
    object Password : Route("Password")
    object Debug : Route("Debug")
    object Token : Route("Token")
}

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
    data class HealthCardUser(val name: String?)

    @Immutable
    data class State(
        val demoModeActive: Boolean,
        val analyticsAllowed: Boolean,
        val authenticationMode: AuthenticationMode,
        val zoomEnabled: Boolean,
        val screenShotsAllowed: Boolean,
        val healthCardUsers: List<HealthCardUser>
    )
}

private val defaultState = SettingsScreen.State(
    demoModeActive = false,
    analyticsAllowed = false,
    authenticationMode = SettingsScreen.AuthenticationMode.Unspecified,
    zoomEnabled = false,
    // `gemSpec_eRp_FdV A_20203` default settings are not allow screenshots
    screenShotsAllowed = false,
    healthCardUsers = listOf()
)

const val NEW_USER = "newUser"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
    private val demoUseCase: DemoUseCase,
    private val tracker: Tracker,
    @ApplicationPreferences
    private val appPrefs: SharedPreferences
) : BaseViewModel() {
    var screenState by mutableStateOf(defaultState)
        private set

    var isNewUser by settingsUseCase::isNewUser

    private var screenshotsAllowed =
        MutableStateFlow(appPrefs.getBoolean(SCREENSHOTS_ALLOWED, false))

    init {
        viewModelScope.launch {
            combine(
                demoUseCase.demoModeActive,
                tracker.trackingAllowed,
                settingsUseCase.settings,
                screenshotsAllowed,
                settingsUseCase.healthCardUser()
            ) { demoActive, analyticsAllowed, settings, screenshotsAllowed, healthCardUser ->
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
                    healthCardUsers = healthCardUser.map {
                        SettingsScreen.HealthCardUser(it.name)
                    }
                )
            }.collect {
                screenState = it
            }
        }
    }

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

    fun logout() {
        viewModelScope.launch {
            settingsUseCase.logout()
        }
    }

    suspend fun getToken() = settingsUseCase.getToken()
}
