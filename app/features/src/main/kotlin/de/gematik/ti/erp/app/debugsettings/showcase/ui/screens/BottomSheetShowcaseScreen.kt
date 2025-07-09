/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.debugsettings.showcase.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Announcement
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.automirrored.outlined.Feed
import androidx.compose.material.icons.automirrored.outlined.Input
import androidx.compose.material.icons.automirrored.outlined.Segment
import androidx.compose.material.icons.automirrored.rounded.More
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.debugsettings.showcase.presentation.rememberBottomSheetShowcaseScreenController
import de.gematik.ti.erp.app.digas.navigation.DigasRoutes
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pharmacy.ui.preview.PharmacyPreviewData
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LabelButton
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import kotlinx.coroutines.launch

class BottomSheetShowcaseScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val snackbar = LocalSnackbarScaffold.current

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        val controller = rememberBottomSheetShowcaseScreenController()
        val profileData = controller.activeProfile

        BackHandler {
            navController.popBackStack()
        }
        AnimatedElevationScaffold(
            topBarTitle = "Bottom sheet screes",
            listState = listState,
            onBack = { navController.navigateUp() }
        ) {
            BottomSheetShowcaseScreenContent(
                paddingValues = it,
                onClickWelcomeDrawer = {
                    navController.navigate(PrescriptionRoutes.WelcomeDrawerBottomSheetScreen.path())
                },
                onClickGrantConsent = {
                    navController.navigate(PrescriptionRoutes.GrantConsentBottomSheetScreen.path())
                },
                onClickPharmacyDetailFromMessage = {
                    navController.navigate(
                        PharmacyRoutes.PharmacyDetailsFromMessageScreen.path(
                            pharmacy = PharmacyPreviewData.ALL_PRESENT_DATA,
                            taskId = ""
                        )
                    )
                },
                onClickPharmacyDetailFromDetail = {
                    navController.navigate(
                        PharmacyRoutes.PharmacyDetailsFromPharmacyScreen.path(
                            pharmacy = PharmacyPreviewData.ALL_PRESENT_DATA,
                            taskId = ""
                        )
                    )
                },
                onClickPharmacyFilter = {
                    navController.navigate(
                        PharmacyRoutes.PharmacyFilterSheetScreen.path(
                            showNearbyFilter = true,
                            navigateWithSearchButton = true
                        )
                    )
                },
                onClickChangeProfilePicture = {
                    profileData.value.data?.let { activeProfile ->
                        navController.navigate(
                            ProfileRoutes.ProfileEditPictureBottomSheetScreen.path(profileId = activeProfile.id)
                        )
                    } ?: run {
                        scope.launch {
                            snackbar.showSnackbar("No active profile found")
                        }
                    }
                },
                onClickChangeProfileName = {
                    profileData.value.data?.let { activeProfile ->
                        navController.navigate(
                            ProfileRoutes.ProfileEditNameBottomSheetScreen.path(profileId = activeProfile.id)
                        )
                    } ?: run {
                        scope.launch {
                            snackbar.showSnackbar("No active profile found")
                        }
                    }
                },
                onClickDigaFeedback = {
                    navController.navigate(DigasRoutes.DigaFeedbackPromptScreen.path())
                }
            )
        }
    }
}

@Composable
private fun BottomSheetShowcaseScreenContent(
    paddingValues: PaddingValues,
    onClickWelcomeDrawer: () -> Unit,
    onClickGrantConsent: () -> Unit,
    onClickPharmacyDetailFromMessage: () -> Unit,
    onClickPharmacyDetailFromDetail: () -> Unit,
    onClickPharmacyFilter: () -> Unit,
    onClickChangeProfilePicture: () -> Unit,
    onClickChangeProfileName: () -> Unit,
    onClickDigaFeedback: () -> Unit
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        item {
            SpacerLarge()
        }
        item {
            LabelButton(
                Icons.AutoMirrored.Outlined.Announcement,
                "Welcome drawer"
            ) {
                onClickWelcomeDrawer()
            }
        }
        item {
            LabelButton(
                Icons.AutoMirrored.Outlined.ContactSupport,
                "Grant consent"
            ) {
                onClickGrantConsent()
            }
        }
        item {
            LabelButton(
                Icons.AutoMirrored.Rounded.More,
                "Pharmacy Detail from Message "
            ) {
                onClickPharmacyDetailFromMessage()
            }
        }
        item {
            LabelButton(
                Icons.AutoMirrored.Rounded.More,
                "Pharmacy Detail from Detail"
            ) {
                onClickPharmacyDetailFromDetail()
            }
        }
        item {
            LabelButton(
                Icons.AutoMirrored.Outlined.Segment,
                "Pharmacy Filter"
            ) {
                onClickPharmacyFilter()
            }
        }
        item {
            LabelButton(
                Icons.AutoMirrored.Outlined.Input,
                "Profile change picture"
            ) {
                onClickChangeProfilePicture()
            }
        }
        item {
            LabelButton(
                Icons.AutoMirrored.Outlined.Assignment,
                "Profile edit name"
            ) {
                onClickChangeProfileName()
            }
        }
        item {
            LabelButton(
                Icons.AutoMirrored.Outlined.Feed,
                "Diga Feedback"
            ) {
                onClickDigaFeedback()
            }
        }
        item {
            SpacerLarge()
        }
    }
}

@LightDarkPreview
@Composable
fun BottomSheetShowcaseScreenContentPreview() {
    PreviewAppTheme {
        BottomSheetShowcaseScreenContent(
            paddingValues = PaddingValues(),
            onClickWelcomeDrawer = {},
            onClickGrantConsent = {},
            onClickPharmacyDetailFromMessage = {},
            onClickPharmacyFilter = {},
            onClickChangeProfilePicture = {},
            onClickChangeProfileName = {},
            onClickPharmacyDetailFromDetail = {},
            onClickDigaFeedback = {}
        )
    }
}
