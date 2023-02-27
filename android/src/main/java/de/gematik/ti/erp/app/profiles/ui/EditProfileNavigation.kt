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

package de.gematik.ti.erp.app.profiles.ui

import AuditEventsScreen
import TokenScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenController
import de.gematik.ti.erp.app.pkv.ui.InvoiceInformationScreen
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.ui.SettingsController
import de.gematik.ti.erp.app.settings.ui.SettingStatesData
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import kotlinx.coroutines.launch

object ProfileDestinations {
    object Profile : Route("profile")
    object Token : Route("token")
    object AuditEvents : Route("auditEvents")
    object PairedDevices : Route("pairedDevices")
    object ProfileImagePicker : Route("profileImagePicker")
    object ProfileImageCropper : Route("imageCropper")
    object InvoiceInformation : Route("invoiceInformation")
}

@Composable
fun EditProfileNavGraph(
    profilesState: SettingStatesData.ProfilesState,
    navController: NavHostController,
    onBack: () -> Unit,
    selectedProfile: ProfilesUseCaseData.Profile,
    settingsController: SettingsController,
    mainScreenController: MainScreenController,
    profileSettingsViewModel: ProfileSettingsViewModel,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    mainNavController: NavController
) {
    val scope = rememberCoroutineScope()
    NavHost(navController = navController, startDestination = ProfileDestinations.Profile.route) {
        composable(ProfileDestinations.Profile.route) {
            EditProfileScreenContent(
                onClickToken = { navController.navigate(ProfileDestinations.Token.path()) },
                onClickAuditEvents = { navController.navigate(ProfileDestinations.AuditEvents.path()) },
                onClickLogIn = {
                    scope.launch {
                        settingsController.switchProfile(selectedProfile)
                    }
                    mainNavController.navigate(
                        MainNavigationScreens.CardWall.path(selectedProfile.id)
                    )
                },
                onClickLogout = {
                    scope.launch {
                        settingsController.logout(selectedProfile)
                    }
                },
                onBack = onBack,
                profilesState = profilesState,
                settingsController = settingsController,
                profileSettingsViewModel = profileSettingsViewModel,
                selectedProfile = selectedProfile,
                onRemoveProfile = onRemoveProfile,
                onClickEditAvatar = { navController.navigate(ProfileDestinations.ProfileImagePicker.path()) },
                onClickPairedDevices = {
                    navController.navigate(ProfileDestinations.PairedDevices.path())
                },
                onClickInvoiceInformation = { navController.navigate(ProfileDestinations.InvoiceInformation.path()) }
            )
        }

        composable(ProfileDestinations.ProfileImagePicker.route) {
            ProfileColorAndImagePicker(
                selectedProfile,
                clearPersonalizedImage = {
                    profileSettingsViewModel.clearPersonalizedImage(selectedProfile.id)
                },
                onBack = { navController.popBackStack() },
                onPickPersonalizedImage = {
                    navController.navigate(ProfileDestinations.ProfileImageCropper.path())
                },
                onSelectAvatar = { avatar ->
                    profileSettingsViewModel.saveAvatarFigure(selectedProfile.id, avatar)
                },
                onSelectProfileColor = { color ->
                    profileSettingsViewModel.updateProfileColor(selectedProfile, color)
                }
            )
        }

        composable(
            ProfileDestinations.ProfileImageCropper.route
        ) {
            ProfileImageCropper(
                onSaveCroppedImage = {
                    profileSettingsViewModel.savePersonalizedProfileImage(selectedProfile.id, it)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(ProfileDestinations.Token.route) {
            val accessToken by settingsController.decryptedAccessToken(selectedProfile).collectAsState(null)

            NavigationAnimation(mode = NavigationMode.Closed) {
                TokenScreen(
                    onBack = { navController.popBackStack() },
                    ssoToken = selectedProfile.ssoTokenScope?.token?.token,
                    accessToken = accessToken
                )
            }
        }
        composable(ProfileDestinations.AuditEvents.route) {
            NavigationAnimation(mode = NavigationMode.Closed) {
                AuditEventsScreen(
                    profileId = selectedProfile.id,
                    settingsController = settingsController,
                    lastAuthenticated = selectedProfile.lastAuthenticated,
                    tokenValid = selectedProfile.ssoTokenValid()
                ) { navController.popBackStack() }
            }
        }
        composable(ProfileDestinations.PairedDevices.route) {
            NavigationAnimation(mode = NavigationMode.Closed) {
                PairedDevicesScreen(
                    selectedProfile = selectedProfile,
                    settingsController = settingsController,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(ProfileDestinations.InvoiceInformation.route) {
            NavigationAnimation(mode = NavigationMode.Closed) {
                InvoiceInformationScreen(
                    mainScreenController = mainScreenController,
                    selectedProfile = selectedProfile,
                    onBack = { navController.popBackStack() }
                ) {
                    mainNavController.navigate(
                        MainNavigationScreens.CardWall.path(selectedProfile.id)
                    )
                }
            }
        }
    }
}
