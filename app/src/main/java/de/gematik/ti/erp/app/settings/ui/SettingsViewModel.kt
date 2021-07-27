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
import de.gematik.ti.erp.app.SCREENSHOTS_ALLOWED
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.di.ApplicationPreferences
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import de.gematik.ti.erp.app.tracking.Tracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsNavigationScreens(
    val route: String
) {
    object Settings : SettingsNavigationScreens("Settings")
    object Terms : SettingsNavigationScreens("Terms")
    object Imprint : SettingsNavigationScreens("Imprint")
    object DataProtection : SettingsNavigationScreens("DataProtection")
    object OpenSourceLicences : SettingsNavigationScreens("OpenSourceLicences")
    object AllowAnalytics : SettingsNavigationScreens("AcceptAnalytics")
    object FeedbackForm : SettingsNavigationScreens("FeedbackForm")
}

object SettingsScreen {
    enum class AuthenticationMode {
        EHealthCard,
        Biometrics,
        DeviceCredentials,
        Password,
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
        val screenShotsAllowed: Boolean,
        val healthCardUsers: List<HealthCardUser>
    )
}

private val defaultState = SettingsScreen.State(
    demoModeActive = false,
    analyticsAllowed = false,
    authenticationMode = SettingsScreen.AuthenticationMode.Unspecified,
    // `gemSpec_eRp_FdV A_20203` default settings are not allow screenshots
    screenShotsAllowed = false,
    healthCardUsers = listOf()
)

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

    private var screenshotsAllowed = MutableStateFlow(appPrefs.getBoolean(SCREENSHOTS_ALLOWED, false))

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
                        SettingsAuthenticationMethod.Biometrics -> SettingsScreen.AuthenticationMode.Biometrics
                        SettingsAuthenticationMethod.DeviceCredentials -> SettingsScreen.AuthenticationMode.DeviceCredentials
                        SettingsAuthenticationMethod.None -> SettingsScreen.AuthenticationMode.None
                        else -> SettingsScreen.AuthenticationMode.Unspecified
                    },
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

    fun onSelectAuthenticationMode(which: SettingsScreen.AuthenticationMode) =
        viewModelScope.launch(Dispatchers.IO) {
            settingsUseCase.settings.collect { settings ->
                if (which != SettingsScreen.AuthenticationMode.Unspecified) {
                    settingsUseCase.saveSettings(
                        settings.copy(
                            authenticationMethod = when (which) {
                                SettingsScreen.AuthenticationMode.Biometrics -> SettingsAuthenticationMethod.Biometrics
                                SettingsScreen.AuthenticationMode.DeviceCredentials -> SettingsAuthenticationMethod.DeviceCredentials
                                else -> SettingsAuthenticationMethod.None
                            }
                        )
                    )
                }
                cancel()
            }
        }

    fun onSwitchAllowScreenshots(allowScreenshots: Boolean) {
        appPrefs.edit {
            putBoolean(SCREENSHOTS_ALLOWED, allowScreenshots)
        }
        screenshotsAllowed.value = allowScreenshots
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
}
