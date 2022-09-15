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

package de.gematik.ti.erp.app.profiles.ui

import AuditEventsScreen
import TokenScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode

object ProfileDestinations {
    object Profile : Route("profile")
    object Token : Route("token")
    object AuditEvents : Route("auditEvents")
}

@Composable
fun EditProfileNavGraph(
    state: SettingsScreen.State,
    navController: NavHostController,
    onBack: () -> Unit,
    profile: ProfilesUseCaseData.Profile,
    settingsViewModel: SettingsViewModel,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    mainNavController: NavController,
) {

    NavHost(navController = navController, startDestination = ProfileDestinations.Profile.route) {
        composable(ProfileDestinations.Profile.route) {
            EditProfileScreenContent(
                onClickToken = { navController.navigate(ProfileDestinations.Token.path()) },
                onClickAuditEvents = { navController.navigate(ProfileDestinations.AuditEvents.path()) },
                ssoTokenValid = profile.ssoTokenValid(),
                onClickLogIn = {
                    settingsViewModel.switchProfile(profile)
                    mainNavController.navigate(
                        MainNavigationScreens.CardWall.path(settingsViewModel.isCanAvailable(profile))
                    )
                },
                onBack = onBack,
                state = state,
                settingsViewModel = settingsViewModel,
                selectedProfile = profile,
                onRemoveProfile = onRemoveProfile
            )
        }
        composable(ProfileDestinations.Token.route) {
            NavigationAnimation(mode = NavigationMode.Closed) {
                TokenScreen(
                    onBack = { navController.popBackStack() },
                    ssoToken = profile.ssoToken?.tokenOrNull(),
                    accessToken = profile.accessToken,
                )
            }
        }
        composable(
            ProfileDestinations.AuditEvents.route,
        ) {
            NavigationAnimation(mode = NavigationMode.Closed) {
                AuditEventsScreen(
                    profile.name,
                    settingsViewModel,
                    profile.lastAuthenticated,
                    profile.ssoTokenValid(),
                ) { navController.popBackStack() }
            }
        }
    }
}
