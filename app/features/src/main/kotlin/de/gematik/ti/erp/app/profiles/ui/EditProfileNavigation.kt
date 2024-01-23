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

package de.gematik.ti.erp.app.profiles.ui

import AuditEventsScreen
import TokenScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.analytics.trackNavigationChangesAsync
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.navigation.Routes
import de.gematik.ti.erp.app.pkv.ui.InvoiceDetailsScreen
import de.gematik.ti.erp.app.pkv.ui.InvoiceInformationScreen
import de.gematik.ti.erp.app.pkv.ui.InvoiceLocalCorrectionScreen
import de.gematik.ti.erp.app.pkv.ui.InvoicesScreen
import de.gematik.ti.erp.app.pkv.ui.ShareInformationScreen
import de.gematik.ti.erp.app.pkv.ui.rememberInvoicesController
import de.gematik.ti.erp.app.profiles.presentation.ProfilesController
import de.gematik.ti.erp.app.profiles.presentation.rememberSelectedProfileController
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.rememberAuditEventsController
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import kotlinx.coroutines.launch

object ProfileDestinations {
    object Profile : Routes("profile")
    object Token : Routes("profile_token")
    object AuditEvents : Routes("profile_auditEvents")
    object PairedDevices : Routes("profile_registeredDevices")
    object ProfileImagePicker : Routes("profile_editPicture")
    object ProfileImageCropper : Routes("profile_editPicture_imageCropper")
    object Invoices : Routes("chargeItem_list")

    object InvoiceInformation :
        Routes(
            "chargeItem_details",
            navArgument("taskId") { type = NavType.StringType }
        ) {
        fun path(taskId: String) = path("taskId" to taskId)
    }

    object InvoiceDetails :
        Routes(
            "chargeItem_details_expanded",
            navArgument("taskId") { type = NavType.StringType }
        ) {
        fun path(taskId: String) = path("taskId" to taskId)
    }

    object InvoiceLocalCorrection :
        Routes(
            "chargeItem_correct_locally",
            navArgument("taskId") { type = NavType.StringType }
        ) {
        fun path(taskId: String) = path("taskId" to taskId)
    }

    object ShareInformation :
        Routes(
            "chargeItem_share",
            navArgument("taskId") { type = NavType.StringType }
        ) {
        fun path(taskId: String) = path("taskId" to taskId)
    }
}

@Suppress("LongMethod")
@Composable
fun EditProfileNavGraph(
    navController: NavHostController,
    onBack: () -> Unit,
    selectedProfile: ProfilesUseCaseData.Profile,
    profilesController: ProfilesController,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    mainNavController: NavController
) {
    var previousNavEntry by remember { mutableStateOf("profile") }
    trackNavigationChangesAsync(navController, previousNavEntry, onNavEntryChange = { previousNavEntry = it })
    val scope = rememberCoroutineScope()
    val invoicesController = rememberInvoicesController(profileId = selectedProfile.id)
    val auditEventsController = rememberAuditEventsController()
    NavHost(navController = navController, startDestination = ProfileDestinations.Profile.route) {
        composable(ProfileDestinations.Profile.route) {
            val selectedProfileController = rememberSelectedProfileController(selectedProfile.id)

            val profiles by profilesController.getProfilesState()
            val profile by selectedProfileController.selectedProfileState

            EditProfileScreenContent(
                selectedProfile = profile,
                profiles = profiles,
                profilesController = profilesController,
                onClickToken = { navController.navigate(ProfileDestinations.Token.path()) },
                onClickAuditEvents = { navController.navigate(ProfileDestinations.AuditEvents.path()) },
                onClickLogIn = {
                    profilesController.switchActiveProfile(selectedProfile.id)
                    mainNavController.navigate(
                        MainNavigationScreens.CardWall.path(selectedProfile.id)
                    )
                },
                onClickLogout = { profilesController.logout(selectedProfile) },
                onClickInvoices = { navController.navigate(ProfileDestinations.Invoices.path()) },
                onRemoveProfile = onRemoveProfile,
                onClickEditAvatar = { navController.navigate(ProfileDestinations.ProfileImagePicker.path()) },
                onClickPairedDevices = {
                    navController.navigate(ProfileDestinations.PairedDevices.path())
                },
                onBack = onBack
            )
        }

        composable(ProfileDestinations.ProfileImagePicker.route) {
            ProfileColorAndImagePicker(
                selectedProfile = selectedProfile,
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
            val accessToken by profilesController.decryptedAccessToken(selectedProfile)

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
                    onShowCardWall = {
                        profilesController.switchActiveProfile(selectedProfile.id)
                        mainNavController.navigate(
                            MainNavigationScreens.CardWall.path(selectedProfile.id)
                        )
                    },
                    auditEventsController = auditEventsController,
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
                    invoicesController = invoicesController,
                    selectedProfile = selectedProfile,
                    onBack = { navController.popBackStack() },
                    onClickInvoice = { taskId ->
                        navController.navigate(
                            ProfileDestinations.InvoiceInformation.path(taskId)
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
                    onClickCorrectInvoiceLocally = {
                        navController.navigate(
                            ProfileDestinations.InvoiceLocalCorrection.path(it)
                        )
                    },
                    onClickSubmit = {
                        navController.navigate(
                            ProfileDestinations.ShareInformation.path(it)
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
            ProfileDestinations.InvoiceLocalCorrection.route,
            ProfileDestinations.InvoiceLocalCorrection.arguments
        ) {
            val taskId = remember { requireNotNull(it.arguments?.getString("taskId")) }

            NavigationAnimation(mode = NavigationMode.Closed) {
                InvoiceLocalCorrectionScreen(
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
