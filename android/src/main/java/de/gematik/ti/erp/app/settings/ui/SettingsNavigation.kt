/*
 * Copyright (c) 2023 gematik GmbH
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.createToastShort

object SettingsNavigationScreens {
    object Settings : Route("Settings")

    object AccessibilitySettings : Route("AccessibilitySettings")
    object ProductImprovementSettings : Route("ProductImprovementSettings")
    object DeviceSecuritySettings : Route("DeviceSecuritySettings")
}

@Suppress("LongMethod")
@Composable
fun SettingsNavGraph(
    settingsNavController: NavHostController,
    navigationMode: NavigationMode,
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        settingsNavController,
        startDestination = SettingsNavigationScreens.Settings.path()
    ) {
        composable(SettingsNavigationScreens.Settings.route) {
            NavigationAnimation(mode = navigationMode) {
                SettingsScreenWithScaffold(
                    mainNavController = mainNavController,
                    navController = settingsNavController,
                    settingsViewModel = settingsViewModel
                )
            }
        }
        composable(SettingsNavigationScreens.AccessibilitySettings.route) {
            AccessibilitySettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack = { settingsNavController.popBackStack() }
            )
        }
        composable(SettingsNavigationScreens.ProductImprovementSettings.route) {
            val context = LocalContext.current
            val disAllowAnalyticsToast = stringResource(R.string.settings_tracking_disallow_info)

            ProductImprovementSettingsScreen(
                settingsViewModel = settingsViewModel,
                onAllowAnalytics = {
                    if (!it) {
                        settingsViewModel.onTrackingDisallowed()
                        createToastShort(context, disAllowAnalyticsToast)
                    } else {
                        mainNavController.navigate(MainNavigationScreens.AllowAnalytics.path())
                    }
                },
                onBack = { settingsNavController.popBackStack() }
            )
        }

        composable(SettingsNavigationScreens.DeviceSecuritySettings.route) {
            DeviceSecuritySettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack = { settingsNavController.popBackStack() }
            ) {
                when (it) {
                    is SettingsData.AuthenticationMode.Password ->
                        mainNavController.navigate(MainNavigationScreens.Password.path())
                    else -> settingsViewModel.onSelectDeviceSecurityAuthenticationMode()
                }
            }
        }
    }
}
