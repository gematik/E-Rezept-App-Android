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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.LegalNoticeWithScaffold
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.cardunlock.ui.UnlockEgKScreen
import de.gematik.ti.erp.app.debug.ui.DebugScreenWrapper
import de.gematik.ti.erp.app.license.ui.LicenseScreen
import de.gematik.ti.erp.app.orderhealthcard.ui.HealthCardContactOrderScreen
import de.gematik.ti.erp.app.profiles.ui.EditProfileScreen
import de.gematik.ti.erp.app.profiles.ui.ProfileSettingsViewModel
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.webview.URI_DATA_TERMS
import de.gematik.ti.erp.app.webview.URI_TERMS_OF_USE
import de.gematik.ti.erp.app.webview.WebViewScreen

object SettingsNavigationScreens {
    object Settings : Route("Settings")
    object Terms : Route("Terms")
    object Imprint : Route("Imprint")
    object DataProtection : Route("DataProtection")
    object OpenSourceLicences : Route("OpenSourceLicences")
    object AdditionalLicences : Route("AdditionalLicences")
    object AllowAnalytics : Route("AcceptAnalytics")
    object Password : Route("Password")
    object Debug : Route("Debug")
    object OrderHealthCard : Route("OrderHealthCard")
    object EditProfile :
        Route("EditProfile", navArgument("profileId") { type = NavType.StringType }) {
        fun path(profileId: String) = path("profileId" to profileId)
    }
    object UnlockEgk : Route("UnlockEgk", navArgument("changeSecret") { type = NavType.BoolType }) {
        fun path(changeSecret: Boolean) = path("changeSecret" to changeSecret)
    }
}

enum class SettingsScrollTo {
    None,
    Authentication,
    Profiles,
    HealthCard
}

@Suppress("LongMethod")
@Composable
fun SettingsNavGraph(
    settingsNavController: NavHostController,
    navigationMode: NavigationMode,
    scrollTo: SettingsScrollTo,
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel,
    profileSettingsViewModel: ProfileSettingsViewModel
) {
    val state by produceState(SettingsScreen.defaultState) {
        settingsViewModel.screenState().collect {
            value = it
        }
    }
    NavHost(
        settingsNavController,
        startDestination = SettingsNavigationScreens.Settings.path()
    ) {
        composable(SettingsNavigationScreens.Settings.route) {
            NavigationAnimation(mode = navigationMode) {
                SettingsScreenWithScaffold(
                    scrollTo,
                    mainNavController = mainNavController,
                    navController = settingsNavController,
                    settingsViewModel = settingsViewModel
                )
            }
        }
        composable(SettingsNavigationScreens.Debug.route) {
            DebugScreenWrapper(settingsNavController)
        }
        composable(SettingsNavigationScreens.Terms.route) {
            NavigationAnimation(mode = navigationMode) {
                WebViewScreen(
                    title = stringResource(R.string.onb_terms_of_use),
                    onBack = { settingsNavController.popBackStack() },
                    url = URI_TERMS_OF_USE
                )
            }
        }
        composable(SettingsNavigationScreens.Imprint.route) {
            NavigationAnimation(mode = navigationMode) {
                LegalNoticeWithScaffold(
                    settingsNavController
                )
            }
        }
        composable(SettingsNavigationScreens.DataProtection.route) {
            NavigationAnimation(mode = navigationMode) {
                WebViewScreen(
                    title = stringResource(R.string.onb_data_consent),
                    onBack = { settingsNavController.popBackStack() },
                    url = URI_DATA_TERMS
                )
            }
        }
        composable(SettingsNavigationScreens.OpenSourceLicences.route) {
            NavigationAnimation(mode = navigationMode) {
                LicenseScreen(
                    onBack = { settingsNavController.popBackStack() }
                )
            }
        }
        composable(SettingsNavigationScreens.AdditionalLicences.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacyLicenseScreen {
                    settingsNavController.popBackStack()
                }
            }
        }
        composable(SettingsNavigationScreens.AllowAnalytics.route) {
            NavigationAnimation(mode = navigationMode) {
                AllowAnalyticsScreen(
                    onBack = { settingsNavController.popBackStack() },
                    onAllowAnalytics = {
                        if (it) {
                            settingsViewModel.onTrackingAllowed()
                        } else {
                            settingsViewModel.onTrackingDisallowed()
                        }
                    }
                )
            }
        }
        composable(SettingsNavigationScreens.Password.route) {
            NavigationAnimation(mode = navigationMode) {
                SecureAppWithPassword(
                    settingsNavController,
                    settingsViewModel
                )
            }
        }
        composable(SettingsNavigationScreens.OrderHealthCard.route) {
            HealthCardContactOrderScreen(onBack = { settingsNavController.popBackStack() })
        }
        composable(
            SettingsNavigationScreens.EditProfile.route,
            SettingsNavigationScreens.EditProfile.arguments
        ) {
            val profileId = remember { it.arguments!!.getString("profileId")!! }

            state.profileById(profileId)?.let { profile ->
                EditProfileScreen(
                    state,
                    profile,
                    settingsViewModel,
                    profileSettingsViewModel,
                    onRemoveProfile = {
                        settingsViewModel.removeProfile(profile, it)
                        settingsNavController.popBackStack()
                    },
                    onBack = { settingsNavController.popBackStack() },
                    mainNavController = mainNavController
                )
            }
        }
        composable(
            SettingsNavigationScreens.UnlockEgk.route,
            SettingsNavigationScreens.UnlockEgk.arguments
        ) {
            val changeSecret = remember {
                it.arguments!!.getBoolean("changeSecret")
            }

            NavigationAnimation(mode = navigationMode) {
                UnlockEgKScreen(
                    changeSecret = changeSecret,
                    navController = settingsNavController,
                    onClickLearnMore = {
                        settingsNavController.navigate(
                            SettingsNavigationScreens.OrderHealthCard.path()
                        )
                    }
                )
            }
        }
    }
}
