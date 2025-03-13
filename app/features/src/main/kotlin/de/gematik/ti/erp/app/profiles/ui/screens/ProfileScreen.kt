/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.profiles.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes.CardWallIntroScreen
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.profiles.model.ProfileCombinedData
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileScreenController
import de.gematik.ti.erp.app.profiles.ui.components.DeleteProfileDialog
import de.gematik.ti.erp.app.profiles.ui.components.ProfileAvatarSection
import de.gematik.ti.erp.app.profiles.ui.components.ProfileEditPairedDeviceSection
import de.gematik.ti.erp.app.profiles.ui.components.ProfileInsuranceInformationSection
import de.gematik.ti.erp.app.profiles.ui.components.ProfileInvoiceInformationSection
import de.gematik.ti.erp.app.profiles.ui.components.ProfileNameSection
import de.gematik.ti.erp.app.profiles.ui.components.ProfileSecuritySection
import de.gematik.ti.erp.app.profiles.ui.preview.ProfileStatePreviewParameterProvider
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.uistate.UiState

class ProfileScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
        val keyboardController = LocalSoftwareKeyboardController.current
        val dialog = LocalDialog.current

        val profileId =
            remember { navBackStackEntry.arguments?.getString(ProfileRoutes.PROFILE_NAV_PROFILE_ID) } ?: return
        val profileScreenController = rememberProfileScreenController(profileId)
        val combinedProfileState by profileScreenController.combinedProfile.collectAsStateWithLifecycle()

        val activity = LocalActivity.current as? BaseActivity
        val isDemoMode = remember { activity?.isDemoMode?.value ?: false }
        val listState = rememberLazyListState()
        val scaffoldState = rememberScaffoldState()
        val defaultProfileName = stringResource(id = R.string.onboarding_default_profile_name)
        @Requirement(
            "A_19229-01#3",
            sourceSpecification = "gemSpec_eRp_FdV",
            rationale = "Deletion button is tapped -> delete confirmation dialog shows."
        )
        DeleteProfileDialog(
            event = profileScreenController.deleteDialogEvent,
            dialogScaffold = dialog,
            onClickAction = {
                profileScreenController.deleteProfile(profileId, defaultProfileName)
                navController.popBackStack()
            }
        )

        with(profileScreenController) {
            showCardWallEvent.listen { profileId ->
                navController.navigate(CardWallIntroScreen.path(profileId))
            }
            showCardWallWithFilledCanEvent.listen { cardWallData ->
                navController.navigate(
                    CardWallRoutes.CardWallPinScreen.path(
                        profileIdentifier = cardWallData.profileId,
                        can = cardWallData.can
                    )
                )
            }
            showGidEvent.listen { gidData ->
                navController.navigate(
                    CardWallIntroScreen.pathWithGid(gidData)
                )
            }
        }

        ProfileScreenScaffold(
            profileState = combinedProfileState,
            scaffoldState = scaffoldState,
            listState = listState,
            isDemoMode = isDemoMode,
            color = color,
            keyboardController = keyboardController,
            onClickLogIn = {
                combinedProfileState.data?.selectedProfile?.let { profile ->
                    profileScreenController.switchActiveProfile(profile.id)
                    profileScreenController.chooseAuthenticationMethod(profile.id)
                }
            },
            onClickLogout = {
                combinedProfileState.data?.selectedProfile?.let { profile ->
                    profileScreenController.logout(profile.id)
                    profileScreenController.refreshCombinedProfile()
                }
            },
            onClickDelete = {
                profileScreenController.triggerDeleteProfileDialog()
            },
            onUpdateProfileName = { name ->
                profileScreenController.updateProfileName(name)
            },
            onClickEditAvatar = {
                combinedProfileState.data?.selectedProfile?.let { profile ->
                    navController.navigate(ProfileRoutes.ProfileEditPictureScreen.path(profileId = profile.id))
                }
            },
            onClickInvoices = {
                combinedProfileState.data?.selectedProfile?.let { profile ->
                    navController.navigate(PkvRoutes.InvoiceListScreen.path(profileId = profile.id))
                }
            },
            onShowPairedDevices = {
                combinedProfileState.data?.selectedProfile?.let { profile ->
                    navController.navigate(ProfileRoutes.ProfilePairedDevicesScreen.path(profileId = profile.id))
                }
            },
            onClickAuditEvents = {
                combinedProfileState.data?.selectedProfile?.let { profile ->
                    navController.navigate(ProfileRoutes.ProfileAuditEventsScreen.path(profileId = profile.id))
                }
            }
        ) { navController.popBackStack() }
    }
}

@Suppress("LongParameterList")
@Composable
internal fun ProfileScreenScaffold(
    profileState: UiState<ProfileCombinedData>,
    scaffoldState: ScaffoldState,
    listState: LazyListState,
    isDemoMode: Boolean,
    color: Color,
    keyboardController: SoftwareKeyboardController?,
    onClickLogIn: () -> Unit,
    onClickLogout: () -> Unit,
    onClickDelete: () -> Unit,
    onUpdateProfileName: (String) -> Unit,
    onClickEditAvatar: () -> Unit,
    onClickInvoices: () -> Unit,
    onShowPairedDevices: () -> Unit,
    onClickAuditEvents: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        modifier =
        Modifier.imePadding(),
        topBarTitle = stringResource(R.string.edit_profile_title),
        navigationMode = NavigationBarMode.Back,
        scaffoldState = scaffoldState,
        listState = listState,
        actions = {
            profileState.data?.selectedProfile?.let {
                ThreeDotMenu(
                    selectedProfile = it,
                    onClickLogIn = onClickLogIn,
                    onClickLogout = onClickLogout,
                    onClickDelete = onClickDelete
                )
            }
        },
        onBack = onBack
    ) {
        UiStateMachine(
            state = profileState,
            onError = {
                ErrorScreenComponent()
            },
            onEmpty = {
                ErrorScreenComponent()
            },
            onLoading = {
                Center {
                    CircularProgressIndicator()
                }
            },
            onContent = { profileCombinedData ->
                profileCombinedData.selectedProfile?.let { selectedProfile ->
                    ProfileScreenContent(
                        listState = listState,
                        profileState = profileState,
                        selectedProfile = selectedProfile,
                        isDemoMode = isDemoMode,
                        keyboardController = keyboardController,
                        color = color,
                        onUpdateProfileName = onUpdateProfileName,
                        onClickEditAvatar = onClickEditAvatar,
                        onClickLogIn = onClickLogIn,
                        onClickInvoices = onClickInvoices,
                        onShowPairedDevices = onShowPairedDevices,
                        onClickAuditEvents = onClickAuditEvents
                    )
                }
            }
        )
    }
}

@Suppress("LongParameterList")
@Composable
internal fun ProfileScreenContent(
    listState: LazyListState,
    profileState: UiState<ProfileCombinedData>,
    selectedProfile: ProfilesUseCaseData.Profile,
    isDemoMode: Boolean,
    color: Color,
    keyboardController: SoftwareKeyboardController?,
    onUpdateProfileName: (String) -> Unit,
    onClickEditAvatar: () -> Unit,
    onClickLogIn: () -> Unit,
    onClickInvoices: () -> Unit,
    onShowPairedDevices: () -> Unit,
    onClickAuditEvents: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.testTag(TestTag.Profile.ProfileScreenContent),
        state = listState,
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            ProfileNameSection(profileState, color, keyboardController) { name ->
                onUpdateProfileName(name)
            }
        }
        item {
            ProfileAvatarSection(profile = selectedProfile) {
                onClickEditAvatar()
            }
        }
        item {
            ProfileInsuranceInformationSection(
                selectedProfile.lastAuthenticated,
                selectedProfile.ssoTokenScope,
                selectedProfile.insurance
            ) {
                onClickLogIn()
            }
        }

        if (selectedProfile.insurance.insuranceType == ProfilesUseCaseData.InsuranceType.PKV) {
            item {
                ProfileInvoiceInformationSection {
                    onClickInvoices()
                }
            }
        }

        if (!isDemoMode) {
            item {
                ProfileEditPairedDeviceSection {
                    onShowPairedDevices()
                }
            }
        }

        item {
            ProfileSecuritySection {
                onClickAuditEvents()
            }
        }
    }
}

@Composable
internal fun ThreeDotMenu(
    selectedProfile: ProfilesUseCaseData.Profile,
    onClickLogIn: () -> Unit,
    onClickLogout: () -> Unit,
    onClickDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = true },
        modifier = Modifier.testTag(TestTag.Profile.ThreeDotMenuButton)
    ) {
        Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral600)
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        offset = DpOffset(24.dp, 0.dp)
    ) {
        DropdownMenuItem(
            modifier =
            Modifier.testTag(
                if (selectedProfile.ssoTokenScope != null) {
                    TestTag.Profile.LogoutButton
                } else {
                    TestTag.Profile.LoginButton
                }
            ),
            onClick =
            if (selectedProfile.ssoTokenScope != null) {
                onClickLogout
            } else {
                onClickLogIn
            }
        ) {
            Text(
                text =
                if (selectedProfile.ssoTokenScope != null) {
                    stringResource(R.string.insurance_information_logout)
                } else {
                    stringResource(R.string.insurance_information_login)
                }
            )
        }

        DropdownMenuItem(
            modifier = Modifier.testTag(TestTag.Profile.DeleteProfileButton),
            onClick = {
                expanded = false
                onClickDelete()
            }
        ) {
            Text(
                text = stringResource(R.string.remove_profile),
                color = AppTheme.colors.red600
            )
        }
    }
}

@LightDarkPreview
@Composable
fun ProfileScreenPreview(
    @PreviewParameter(ProfileStatePreviewParameterProvider::class) profileState: UiState<ProfileCombinedData>
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    PreviewAppTheme {
        val color = AppTheme.colors.primary700
        ProfileScreenScaffold(
            listState = listState,
            profileState = profileState,
            isDemoMode = false,
            color = color,
            keyboardController = null,
            onUpdateProfileName = {},
            onClickEditAvatar = {},
            onClickLogIn = {},
            onClickInvoices = {},
            onShowPairedDevices = {},
            onClickAuditEvents = {},
            onBack = {},
            onClickDelete = {},
            onClickLogout = {},
            scaffoldState = scaffoldState
        )
    }
}
