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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.demomode.DemoModeIntent
import de.gematik.ti.erp.app.demomode.startAppWithDemoMode
import de.gematik.ti.erp.app.demomode.startAppWithNormalMode
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.navigation.Routes
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import kotlinx.coroutines.launch

object SettingsNavigationScreens {
    object Settings : Routes("settings")
    object AccessibilitySettings : Routes("settings_accessibility")
    object ProductImprovementSettings : Routes("settings_productImprovements")
    object DeviceSecuritySettings : Routes("settings_authenticationMethods")
}

@Suppress("LongMethod")
@Composable
fun SettingsNavGraph(
    settingsNavController: NavHostController,
    navigationMode: NavigationMode,
    mainNavController: NavController,
    settingsController: SettingsController,
    buildConfig: BuildConfigInformation
) {
    val scope = rememberCoroutineScope()
    NavHost(
        settingsNavController,
        startDestination = SettingsNavigationScreens.Settings.path()
    ) {
        composable(SettingsNavigationScreens.Settings.route) {
            val activity = LocalActivity.current
            NavigationAnimation(mode = navigationMode) {
                SettingsScreenWithScaffold(
                    mainNavController = mainNavController,
                    navController = settingsNavController,
                    buildConfig = buildConfig,
                    onClickDemoModeEnd = {
                        DemoModeIntent.startAppWithNormalMode<MainActivity>(activity)
                    },
                    onClickDemoMode = {
                        DemoModeIntent.startAppWithDemoMode<MainActivity>(activity)
                    }
                )
            }
        }
        composable(SettingsNavigationScreens.AccessibilitySettings.route) {
            AccessibilitySettingsScreen(
                onBack = { settingsNavController.popBackStack() }
            )
        }
        composable(SettingsNavigationScreens.ProductImprovementSettings.route) {
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
                    mainNavController.navigate(MainNavigationScreens.AllowAnalytics.path())
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
