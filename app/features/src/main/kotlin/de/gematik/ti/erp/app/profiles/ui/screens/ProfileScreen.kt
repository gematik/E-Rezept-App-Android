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

package de.gematik.ti.erp.app.profiles.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.authentication.observer.ChooseAuthenticationNavigationEventsListener
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.profiles.model.ProfileCombinedData
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileScreenController
import de.gematik.ti.erp.app.profiles.ui.components.DeleteProfileDialog
import de.gematik.ti.erp.app.profiles.ui.components.ProfileAvatarSection
import de.gematik.ti.erp.app.profiles.ui.components.ProfileEditPairedDeviceSection
import de.gematik.ti.erp.app.profiles.ui.components.ProfileEuConsentSection
import de.gematik.ti.erp.app.profiles.ui.components.ProfileInsuranceInformationSection
import de.gematik.ti.erp.app.profiles.ui.components.ProfileInvoiceInformationSection
import de.gematik.ti.erp.app.profiles.ui.components.ProfileNameSection
import de.gematik.ti.erp.app.profiles.ui.components.ProfileSecuritySection
import de.gematik.ti.erp.app.profiles.ui.preview.ProfileStatePreviewParameterProvider
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkLongPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonLarge
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
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
        val hasEuRedeemablePrescriptions by profileScreenController.hasEuRedeemablePrescriptions.collectAsStateWithLifecycle()
        val euConsentStatus by profileScreenController.euConsentStatus.collectAsStateWithLifecycle()
        val euRedeemFeatureFlag by profileScreenController.euRedeemFeatureFlag.collectAsStateWithLifecycle()
        val activity = LocalActivity.current as? BaseActivity
        val isDemoMode = remember { activity?.isDemoMode?.value ?: false }
        val listState = rememberLazyListState()
        val scaffoldState = rememberScaffoldState()
        val defaultProfileName = stringResource(id = R.string.onboarding_default_profile_name)

        val context = LocalContext.current
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val kvnrString = stringResource(R.string.insurance_information_insurance_identifier)
        var triggerIsKVNRCopiedCheck by remember { mutableStateOf(true) }
        val isKVNRCopied by remember(triggerIsKVNRCopiedCheck) {
            mutableStateOf(clipboard.primaryClipDescription?.label == kvnrString)
        }

        LaunchedEffect(profileId) {
            profileScreenController.fetchEuConsentStatus(profileId)
        }

        val onBack by rememberUpdatedState { navController.popBackStack() }
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
                onBack()
            }
        )
        ChooseAuthenticationNavigationEventsListener(profileScreenController, navController, dialogScaffold = LocalDialog.current)
        BackHandler { onBack() }
        ProfileScreenScaffold(
            profileState = combinedProfileState,
            scaffoldState = scaffoldState,
            listState = listState,
            hasEuRedeemablePrescriptions = hasEuRedeemablePrescriptions,
            euConsentStatus = euConsentStatus,
            euRedeemFeatureFlag = euRedeemFeatureFlag,
            isDemoMode = isDemoMode,
            isKVNRCopied = isKVNRCopied,
            color = color,
            keyboardController = keyboardController,
            onClickLogIn = {
                combinedProfileState.data?.selectedProfile?.let { profile ->
                    profileScreenController.switchActiveProfile(profile.id)
                    profileScreenController.chooseAuthenticationMethod(profile)
                }
            },
            onClickLogout = {
                combinedProfileState.data?.selectedProfile?.let { profile ->
                    profileScreenController.logout(profile.id)
                }
            },
            onClickDelete = {
                profileScreenController.triggerDeleteProfileDialog()
            },
            onClickCopy = {
                clipboard.setPrimaryClip(it)
                triggerIsKVNRCopiedCheck = !triggerIsKVNRCopiedCheck
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
            onClickEuConsent = {
                combinedProfileState.data?.selectedProfile?.let { profile ->
                    if (profileScreenController.onEuConsentClick(profile)) {
                        navController.navigate(ProfileRoutes.ProfileEuConsentScreen.path())
                    }
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
            },
            onClickChangeInsuranceType = {
                navController.navigate(
                    ProfileRoutes.ProfileChangeInsuranceTypeBottomSheetScreen.path(profileId = profileId)
                )
            }
        ) { onBack() }
    }
}

@Suppress("LongParameterList")
@Composable
internal fun ProfileScreenScaffold(
    profileState: UiState<ProfileCombinedData>,
    scaffoldState: ScaffoldState,
    listState: LazyListState,
    hasEuRedeemablePrescriptions: Boolean,
    euConsentStatus: Boolean?,
    euRedeemFeatureFlag: Boolean,
    isDemoMode: Boolean,
    color: Color,
    isKVNRCopied: Boolean,
    keyboardController: SoftwareKeyboardController?,
    onClickLogIn: () -> Unit,
    onClickLogout: () -> Unit,
    onClickDelete: () -> Unit,
    onUpdateProfileName: (String) -> Unit,
    onClickEditAvatar: () -> Unit,
    onClickCopy: (ClipData) -> Unit,
    onClickChangeInsuranceType: () -> Unit,
    onClickInvoices: () -> Unit,
    onClickEuConsent: () -> Unit,
    onShowPairedDevices: () -> Unit,
    onClickAuditEvents: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.imePadding(),
        topBarTitle = stringResource(R.string.edit_profile_title),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
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
                FullScreenLoadingIndicator()
            },
            onContent = { profileCombinedData ->
                profileCombinedData.selectedProfile?.let { selectedProfile ->
                    ProfileScreenContent(
                        listState = listState,
                        profileState = profileState,
                        selectedProfile = selectedProfile,
                        hasEuRedeemablePrescriptions = hasEuRedeemablePrescriptions,
                        euConsentStatus = euConsentStatus,
                        euRedeemFeatureFlag = euRedeemFeatureFlag,
                        isDemoMode = isDemoMode,
                        isKVNRCopied = isKVNRCopied,
                        keyboardController = keyboardController,
                        color = color,
                        onUpdateProfileName = onUpdateProfileName,
                        onClickEditAvatar = onClickEditAvatar,
                        onClickLogIn = onClickLogIn,
                        onClickLogOut = onClickLogout,
                        onClickDeleteProfile = onClickDelete,
                        onClickCopy = onClickCopy,
                        onClickChangeInsuranceType = onClickChangeInsuranceType,
                        onClickInvoices = onClickInvoices,
                        onClickEuConsent = onClickEuConsent,
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
    isKVNRCopied: Boolean,
    hasEuRedeemablePrescriptions: Boolean,
    euConsentStatus: Boolean?,
    euRedeemFeatureFlag: Boolean,
    isDemoMode: Boolean,
    color: Color,
    keyboardController: SoftwareKeyboardController?,
    onUpdateProfileName: (String) -> Unit,
    onClickEditAvatar: () -> Unit,
    onClickCopy: (ClipData) -> Unit,
    onClickLogIn: () -> Unit,
    onClickLogOut: () -> Unit,
    onClickDeleteProfile: () -> Unit,
    onClickChangeInsuranceType: () -> Unit,
    onClickInvoices: () -> Unit,
    onClickEuConsent: () -> Unit,
    onShowPairedDevices: () -> Unit,
    onClickAuditEvents: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.testTag(TestTag.Profile.ProfileScreenContent),
        state = listState,
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.XXLarge)
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
            Divider(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                color = AppTheme.colors.neutral300
            )
        }
        item {
            ProfileInsuranceInformationSection(
                selectedProfile,
                isKVNRCopied = isKVNRCopied,
                onClickCopy = onClickCopy,
                onClickLogIn = onClickLogIn,
                onClickLogOut = onClickLogOut,
                onClickChangeInsuranceType = onClickChangeInsuranceType
            )
        }
        item {
            Divider(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                color = AppTheme.colors.neutral300
            )
        }
        if (selectedProfile.hasBundFeatures()) {
            item {
                ProfileInvoiceInformationSection {
                    onClickInvoices()
                }
            }
            item {
                Divider(
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                    color = AppTheme.colors.neutral300
                )
            }
        }
        if (euRedeemFeatureFlag && hasEuRedeemablePrescriptions) {
            item {
                ProfileEuConsentSection(
                    euConsentStatus = euConsentStatus
                ) {
                    onClickEuConsent()
                }
            }
            item {
                Divider(
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                    color = AppTheme.colors.neutral300
                )
            }
        }
        if (!isDemoMode) {
            item {
                ProfileEditPairedDeviceSection {
                    onShowPairedDevices()
                }
            }
            item {
                Divider(
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                    color = AppTheme.colors.neutral300
                )
            }
        }

        item {
            ProfileSecuritySection {
                onClickAuditEvents()
            }
        }
        item {
            PrimaryButtonLarge(
                modifier = Modifier
                    .padding(horizontal = PaddingDefaults.Medium)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppTheme.colors.red700,
                    contentColor = AppTheme.colors.neutral000
                ),
                onClick = { onClickDeleteProfile() }
            ) {
                Text(text = stringResource(id = R.string.remove_profile))
            }
            SpacerXXLarge()
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
    val description = stringResource(R.string.profile_show_options)
    val isSsoTokenValid by remember(selectedProfile) { mutableStateOf(selectedProfile.isSSOTokenValid()) }
    IconButton(
        onClick = { expanded = true },
        modifier = Modifier
            .testTag(TestTag.Profile.ThreeDotMenuButton)
            .semantics { contentDescription = description }
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
            Modifier
                .testTag(
                    if (isSsoTokenValid) {
                        TestTag.Profile.LogoutButton
                    } else {
                        TestTag.Profile.LoginButton
                    }
                )
                .semantics { role = Role.Button },
            onClick =
            if (isSsoTokenValid) {
                onClickLogout
            } else {
                onClickLogIn
            }
        ) {
            Text(
                text =
                if (isSsoTokenValid) {
                    stringResource(R.string.insurance_information_logout)
                } else {
                    stringResource(R.string.insurance_information_login)
                }
            )
        }

        DropdownMenuItem(
            modifier = Modifier
                .testTag(TestTag.Profile.DeleteProfileButton)
                .semantics { role = Role.Button },
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

@LightDarkLongPreview
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
            hasEuRedeemablePrescriptions = true,
            euConsentStatus = true,
            euRedeemFeatureFlag = true,
            isDemoMode = false,
            color = color,
            keyboardController = null,
            isKVNRCopied = false,
            onUpdateProfileName = {},
            onClickEditAvatar = {},
            onClickLogIn = {},
            onClickInvoices = {},
            onClickEuConsent = {},
            onShowPairedDevices = {},
            onClickChangeInsuranceType = {},
            onClickAuditEvents = {},
            onBack = {},
            onClickCopy = {},
            onClickDelete = {},
            onClickLogout = {},
            scaffoldState = scaffoldState
        )
    }
}
