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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenController
import de.gematik.ti.erp.app.pkv.ui.InvoiceDetailsScreen
import de.gematik.ti.erp.app.pkv.ui.InvoiceInformationScreen
import de.gematik.ti.erp.app.pkv.ui.InvoicesScreen
import de.gematik.ti.erp.app.pkv.ui.ShareInformationScreen
import de.gematik.ti.erp.app.pkv.ui.rememberInvoicesController
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
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
    object Invoices : Route("invoices")

    object InvoiceInformation :
        Route(
            "invoiceInformation",
            navArgument("taskId") { type = NavType.StringType }
        ) {
        fun path(taskId: String) = path("taskId" to taskId)
    }

    object InvoiceDetails :
        Route(
            "invoiceDetails",
            navArgument("taskId") { type = NavType.StringType }
        ) {
        fun path(taskId: String) = path("taskId" to taskId)
    }

    object ShareInformation :
        Route(
            "shareInformation",
            navArgument("taskId") { type = NavType.StringType }
        ) {
        fun path(taskId: String) = path("taskId" to taskId)
    }
}

@Suppress("LongMethod")
@Composable
fun EditProfileNavGraph(
    profilesState: ProfilesStateData.ProfilesState,
    navController: NavHostController,
    onBack: () -> Unit,
    selectedProfile: ProfilesUseCaseData.Profile,
    mainScreenController: MainScreenController,
    profilesController: ProfilesController,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    mainNavController: NavController
) {
    val scope = rememberCoroutineScope()
    val invoicesController = rememberInvoicesController(profileId = selectedProfile.id)
    NavHost(navController = navController, startDestination = ProfileDestinations.Profile.route) {
        composable(ProfileDestinations.Profile.route) {
            EditProfileScreenContent(
                onClickToken = { navController.navigate(ProfileDestinations.Token.path()) },
                onClickAuditEvents = { navController.navigate(ProfileDestinations.AuditEvents.path()) },
                onClickLogIn = {
                    scope.launch {
                        profilesController.switchActiveProfile(selectedProfile)
                    }
                    mainNavController.navigate(
                        MainNavigationScreens.CardWall.path(selectedProfile.id)
                    )
                },
                onClickLogout = {
                    scope.launch {
                        profilesController.logout(selectedProfile)
                    }
                },
                onBack = onBack,
                profilesState = profilesState,
                profilesController = profilesController,
                selectedProfile = selectedProfile,
                onRemoveProfile = onRemoveProfile,
                onClickEditAvatar = { navController.navigate(ProfileDestinations.ProfileImagePicker.path()) },
                onClickPairedDevices = {
                    navController.navigate(ProfileDestinations.PairedDevices.path())
                },
                onClickInvoices = { navController.navigate(ProfileDestinations.Invoices.path()) }
            )
        }

        composable(ProfileDestinations.ProfileImagePicker.route) {
            ProfileColorAndImagePicker(
                selectedProfile,
                clearPersonalizedImage = {
                    scope.launch {
                        profilesController.clearPersonalizedImage(selectedProfile.id)
                    }
                },
                onBack = { navController.popBackStack() },
                onPickPersonalizedImage = {
                    navController.navigate(ProfileDestinations.ProfileImageCropper.path())
                },
                onSelectAvatar = { avatar ->
                    scope.launch {
                        profilesController.saveAvatarFigure(selectedProfile.id, avatar)
                    }
                },
                onSelectProfileColor = { color ->
                    scope.launch {
                        profilesController.updateProfileColor(selectedProfile, color)
                    }
                }
            )
        }

        composable(
            ProfileDestinations.ProfileImageCropper.route
        ) {
            ProfileImageCropper(
                onSaveCroppedImage = {
                    scope.launch {
                        profilesController.savePersonalizedProfileImage(selectedProfile.id, it)
                    }
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(ProfileDestinations.Token.route) {
            val accessToken by profilesController.decryptedAccessToken(selectedProfile).collectAsState(null)

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
                    profilesController = profilesController,
                    lastAuthenticated = selectedProfile.lastAuthenticated,
                    tokenValid = selectedProfile.ssoTokenValid()
                ) { navController.popBackStack() }
            }
        }
        composable(ProfileDestinations.PairedDevices.route) {
            NavigationAnimation(mode = NavigationMode.Closed) {
                PairedDevicesScreen(
                    selectedProfile = selectedProfile,
                    profilesController = profilesController,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(ProfileDestinations.Invoices.route) {
            NavigationAnimation(mode = NavigationMode.Closed) {
                InvoicesScreen(
                    mainScreenController = mainScreenController,
                    invoicesController = invoicesController,
                    selectedProfile = selectedProfile,
                    onBack = { navController.popBackStack() },
                    onClickInvoice = { taskId ->
                        navController.navigate(
                            ProfileDestinations.InvoiceInformation.path(taskId)
                        )
                    },
                    onClickShare = { taskId ->
                        navController.navigate(
                            ProfileDestinations.ShareInformation.path(taskId)
                        )
                    },
                    onShowCardWall = {
                        mainNavController.navigate(
                            MainNavigationScreens.CardWall.path(selectedProfile.id)
                        )
                    }
                )
            }
        }

        composable(
            ProfileDestinations.InvoiceInformation.route,
            ProfileDestinations.InvoiceInformation.arguments
        ) {
            val taskId = remember { requireNotNull(it.arguments?.getString("taskId")) }

            NavigationAnimation(mode = NavigationMode.Closed) {
                InvoiceInformationScreen(
                    selectedProfile = selectedProfile,
                    taskId = taskId,
                    onClickShowMore = {
                        navController.navigate(
                            ProfileDestinations.InvoiceDetails.path(taskId)
                        )
                    },
                    onClickSubmit = {
                        navController.navigate(
                            ProfileDestinations.ShareInformation.path(taskId)
                        )
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            ProfileDestinations.InvoiceDetails.route,
            ProfileDestinations.InvoiceDetails.arguments
        ) {
            val taskId = remember { requireNotNull(it.arguments?.getString("taskId")) }

            NavigationAnimation(mode = NavigationMode.Closed) {
                InvoiceDetailsScreen(
                    invoicesController = invoicesController,
                    taskId = taskId,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            ProfileDestinations.ShareInformation.route,
            ProfileDestinations.ShareInformation.arguments
        ) {
            val taskId = remember { requireNotNull(it.arguments?.getString("taskId")) }

            NavigationAnimation(mode = NavigationMode.Closed) {
                ShareInformationScreen(
                    invoicesController = invoicesController,
                    taskId = taskId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
