/*
 * Copyright (c) 2024 gematik GmbH
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

import AccessibilitySettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.shortToast
import kotlinx.coroutines.launch

object SettingsNavigationScreens {
    object Settings : Route("settings")
    object AccessibilitySettings : Route("settings_accessibility")
    object ProductImprovementSettings : Route("settings_productImprovements")
    object DeviceSecuritySettings : Route("settings_authenticationMethods")
}

@Suppress("LongMethod")
@Composable
fun SettingsNavGraph(
    settingsNavController: NavHostController,
    navigationMode: NavigationMode,
    mainNavController: NavController,
    settingsController: SettingsController
) {
    val scope = rememberCoroutineScope()
    NavHost(
        settingsNavController,
        startDestination = SettingsNavigationScreens.Settings.path()
    ) {
        composable(SettingsNavigationScreens.Settings.route) {
            NavigationAnimation(mode = navigationMode) {
                SettingsScreenWithScaffold(
                    mainNavController = mainNavController,
                    navController = settingsNavController
                )
            }
        }
        composable(SettingsNavigationScreens.AccessibilitySettings.route) {
            AccessibilitySettingsScreen(
                onBack = { settingsNavController.popBackStack() }
            )
        }
        composable(SettingsNavigationScreens.ProductImprovementSettings.route) {
            val context = LocalContext.current
            val disAllowAnalyticsToast = stringResource(R.string.settings_tracking_disallow_info)

            ProductImprovementSettingsScreen(
                settingsController = settingsController,
                onAllowAnalytics = {
                    @Requirement(
                        "O.Purp_5#2",
                        sourceSpecification = "BSI-eRp-ePA",
                        rationale = "The agreement to the use of the analytics framework could be revoked. " +
                            "But other agreements cannot be revoked, since the app could not operate properly."
                    )
                    @Requirement(
                        "A_19982",
                        sourceSpecification = "gemSpec_eRp_FdV",
                        rationale = "The agreement to the use of the analytics framework could be revoked. " +
                            "But other agreements cannot be revoked, since the app could not operate properly."
                    )
                    if (!it) {
                        settingsController.onTrackingDisallowed()
                        context.shortToast(disAllowAnalyticsToast)
                    } else {
                        mainNavController.navigate(MainNavigationScreens.AllowAnalytics.path())
                    }
                },
                onBack = { settingsNavController.popBackStack() }
            )
        }

        composable(SettingsNavigationScreens.DeviceSecuritySettings.route) {
            DeviceSecuritySettingsScreen(
                onBack = { settingsNavController.popBackStack() }
            ) {
                when (it) {
                    is SettingsData.AuthenticationMode.Password ->
                        mainNavController.navigate(MainNavigationScreens.Password.path())
                    else ->
                        scope.launch {
                            settingsController.onSelectDeviceSecurityAuthenticationMode()
                        }
                }
            }
        }
    }
}
