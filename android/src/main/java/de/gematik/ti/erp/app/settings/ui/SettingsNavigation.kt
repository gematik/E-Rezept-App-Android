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

import TokenScreen
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
import de.gematik.ti.erp.app.debug.ui.DebugScreenWrapper
import de.gematik.ti.erp.app.orderhealthcard.ui.HealthCardContactOrderScreen
import de.gematik.ti.erp.app.profiles.ui.EditProfileScreen
import de.gematik.ti.erp.app.profiles.ui.ProfileDestinations
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.webview.URI_DATA_TERMS
import de.gematik.ti.erp.app.webview.URI_LICENCES
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
    object FeedbackForm : Route("FeedbackForm")
    object Password : Route("Password")
    object Debug : Route("Debug")
    object Token : Route("Token")
    object OrderHealthCard : Route("OrderHealthCard")
    object EditProfile :
        Route("EditProfile", navArgument("profileId") { type = NavType.IntType }) {
        fun path(profileId: Int) = path("profileId" to profileId)
    }
}

enum class SettingsScrollTo {
    None,
    Authentication,
    DemoMode,
    Profiles
}

@Composable
fun SettingsNavGraph(
    settingsNavController: NavHostController,
    navigationMode: NavigationMode,
    scrollTo: SettingsScrollTo,
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel
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
            NavigationAnimation(mode = navigationMode) {
                DebugScreenWrapper(settingsNavController)
            }
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
                WebViewScreen(
                    title = stringResource(R.string.settings_legal_licences),
                    onBack = { settingsNavController.popBackStack() },
                    url = URI_LICENCES
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
                AllowAnalyticsScreen {
                    if (it) {
                        settingsViewModel.onTrackingAllowed()
                    } else {
                        settingsViewModel.onTrackingDisallowed()
                    }
                    settingsNavController.popBackStack()
                }
            }
        }
        composable(SettingsNavigationScreens.FeedbackForm.route) {
            NavigationAnimation(mode = navigationMode) {
                FeedbackForm(
                    settingsNavController
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
        composable(ProfileDestinations.Token.route) {
            val activeProfile = state.activeProfile()
            NavigationAnimation(mode = NavigationMode.Closed) {
                TokenScreen(
                    onBack = { settingsNavController.popBackStack() },
                    ssoToken = activeProfile.ssoToken?.tokenOrNull(),
                    accessToken = activeProfile.accessToken,
                )
            }
        }
        composable(
            SettingsNavigationScreens.EditProfile.route,
            SettingsNavigationScreens.EditProfile.arguments,
        ) {
            val profileId =
                remember { settingsNavController.currentBackStackEntry!!.arguments!!.getInt("profileId") }

            state.profileById(profileId)?.let { profile ->
                EditProfileScreen(
                    state,
                    profile,
                    settingsViewModel,
                    onRemoveProfile = {
                        settingsViewModel.removeProfile(profile, it)
                        settingsNavController.popBackStack()
                    },
                    onBack = { settingsNavController.popBackStack() },
                    mainNavController = mainNavController
                )
            }
        }
    }
}
