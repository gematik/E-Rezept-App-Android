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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.EuroSymbol
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.profiles.model.ProfileCombinedData
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileScreenController
import de.gematik.ti.erp.app.profiles.ui.components.ChooseAvatar
import de.gematik.ti.erp.app.profiles.ui.components.color
import de.gematik.ti.erp.app.profiles.ui.components.profileColor
import de.gematik.ti.erp.app.profiles.ui.preview.ProfileStatePreviewParameterProvider
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile.Companion.containsProfileWithName
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.DynamicText
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.ErrorText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.circularBorder
import de.gematik.ti.erp.app.utils.extensions.sanitizeProfileName
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class ProfileScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
        val keyboardController = LocalSoftwareKeyboardController.current
        val dialog = LocalDialog.current

        val deleteDialogEvent = ComposableEvent<Unit>()

        val profileId =
            remember { navBackStackEntry.arguments?.getString(ProfileRoutes.PROFILE_NAV_PROFILE_ID) } ?: return
        val profileScreenController = rememberProfileScreenController(profileId)
        val combinedProfileState by profileScreenController.combinedProfile.collectAsStateWithLifecycle()

        @Requirement(
            "A_19229-01#3",
            sourceSpecification = "gemSpec_eRp_FdV",
            rationale = "Deletion button is tapped -> delete confirmation dialog shows."
        )
        val defaultProfileName = stringResource(id = R.string.onboarding_default_profile_name)
        DeleteProfileDialog(
            event = deleteDialogEvent,
            dialogScaffold = dialog,
            onClickAction = {
                profileScreenController.deleteProfile(profileId, defaultProfileName)
                navController.popBackStack()
            }
        )

        ProfileScreenScaffold(
            profileState = combinedProfileState,
            color = color,
            keyboardController = keyboardController,
            onClickLogIn = {
                combinedProfileState.data?.selectedProfile?.let { profile ->
                    profileScreenController.switchActiveProfile(profile.id)
                    navController.navigate(CardWallRoutes.CardWallIntroScreen.path(profile.id))
                }
            },
            onClickLogout = {
                combinedProfileState.data?.selectedProfile?.let { profile ->
                    profileScreenController.logout(profile.id)
                    profileScreenController.refreshCombinedProfile()
                }
            },
            onClickDelete = {
                deleteDialogEvent.trigger(Unit)
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
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()

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
        ProfileScreenContent(
            listState = listState,
            profileState = profileState,
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

@Suppress("LongParameterList")
@Composable
internal fun ProfileScreenContent(
    listState: LazyListState,
    profileState: UiState<ProfileCombinedData>,
    color: Color,
    keyboardController: SoftwareKeyboardController?,
    onUpdateProfileName: (String) -> Unit,
    onClickEditAvatar: () -> Unit,
    onClickLogIn: () -> Unit,
    onClickInvoices: () -> Unit,
    onShowPairedDevices: () -> Unit,
    onClickAuditEvents: () -> Unit
) {
    val activity = LocalActivity.current as? BaseActivity
    val isDemoMode = remember { activity?.isDemoMode?.value ?: false }
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
                        SpacerLarge()
                        ProfileAvatarSection(profile = selectedProfile) {
                            onClickEditAvatar()
                        }
                    }
                    item {
                        ProfileInsuranceInformation(
                            selectedProfile.lastAuthenticated,
                            selectedProfile.ssoTokenScope,
                            selectedProfile.insurance
                        ) {
                            onClickLogIn()
                        }
                    }

                    if (selectedProfile.insurance.insuranceType == ProfilesUseCaseData.InsuranceType.PKV) {
                        item {
                            ProfileInvoiceInformation {
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
                        SecuritySection {
                            onClickAuditEvents()
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ProfileEditPairedDeviceSection(onShowPairedDevices: () -> Unit) {
    Text(
        text = stringResource(R.string.settings_paired_devices_title),
        style = AppTheme.typography.h6,
        modifier = Modifier.padding(PaddingDefaults.Medium)
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
        modifier =
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onShowPairedDevices
            )
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Icon(Icons.Rounded.Devices, null, tint = AppTheme.colors.primary600)
        Text(stringResource(R.string.settings_login_connected_devices), style = AppTheme.typography.body1)
    }
    SpacerLarge()
    Divider(modifier = Modifier.padding(horizontal = PaddingDefaults.Medium))
    SpacerLarge()
}

@Composable
private fun ProfileInvoiceInformation(onClick: () -> Unit) {
    Column {
        Text(
            stringResource(
                id = R.string.profile_invoiceInformation_header
            ),
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
            style = AppTheme.typography.h6
        )
        SpacerSmall()
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
            modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                )
                .padding(PaddingDefaults.Medium)
                .semantics(mergeDescendants = true) {}
        ) {
            Icon(Icons.Rounded.EuroSymbol, null, tint = AppTheme.colors.primary600)
            Text(
                stringResource(
                    R.string.profile_show_invoices
                ),
                style = AppTheme.typography.body1
            )
        }
        SpacerLarge()
        Divider()
        SpacerLarge()
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

@Composable
internal fun DeleteProfileDialog(
    event: ComposableEvent<Unit>,
    dialogScaffold: DialogScaffold,
    onClickAction: () -> Unit
) {
    event.listen {
        dialogScaffold.show { dialog ->
            ErezeptAlertDialog(
                title = stringResource(id = R.string.remove_profile_header),
                bodyText = stringResource(R.string.remove_profile_detail_message),
                confirmText = stringResource(R.string.remove_profile_yes),
                dismissText = stringResource(R.string.remove_profile_no),
                onConfirmRequest = {
                    onClickAction()
                    dialog.dismiss()
                },
                onDismissRequest = { dialog.dismiss() }
            )
        }
    }
}

@Requirement(
    "O.Auth_6#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Button to display audit events for profile."
)
@Composable
private fun SecuritySection(onClickAuditEvents: () -> Unit) {
    Text(
        text = stringResource(R.string.settings_app_security_header),
        style = AppTheme.typography.h6,
        modifier = Modifier.padding(PaddingDefaults.Medium)
    )
    SecurityAuditEventsSubSection(onClickAuditEvents)
}

@Requirement(
    "A_19177#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Button to display audit events for profile."
)
@Composable
private fun SecurityAuditEventsSubSection(onClickAuditEvents: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
        modifier =
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    onClickAuditEvents()
                }
            )
            .testTag(TestTag.Profile.OpenAuditEventsScreenButton)
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Icon(Icons.Outlined.CloudQueue, null, tint = AppTheme.colors.primary600)
        Column {
            Text(
                stringResource(
                    R.string.settings_show_audit_events
                ),
                style = AppTheme.typography.body1
            )
            Text(
                stringResource(
                    R.string.settings_show_audit_events_info
                ),
                style = AppTheme.typography.body2l
            )
        }
    }
}

@Composable
private fun ProfileNameSection(
    profileState: UiState<ProfileCombinedData>,
    color: Color,
    keyboardController: SoftwareKeyboardController?,
    onUpdateProfileName: (String) -> Unit
) {
    val initialProfileName = profileState.data?.selectedProfile?.name ?: ""
    var profileName by remember(profileState) { mutableStateOf(initialProfileName) }
    var profileNameValid by remember { mutableStateOf(true) }
    var textFieldEnabled by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(textFieldEnabled) {
        if (textFieldEnabled) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Column {
        Row(
            modifier = Modifier.padding(PaddingDefaults.Medium)
        ) {
            if (!textFieldEnabled) {
                val txt =
                    buildAnnotatedString {
                        append(profileName)
                        append(" ")
                        appendInlineContent("edit", "edit")
                    }
                val inlineContent =
                    mapOf(
                        "edit" to
                            InlineTextContent(
                                Placeholder(
                                    width = 0.em,
                                    height = 0.em,
                                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                                )
                            ) {
                                Icon(Icons.Outlined.Edit, null, tint = AppTheme.colors.neutral400)
                            }
                    )
                DynamicText(
                    txt,
                    style = AppTheme.typography.h5,
                    inlineContent = inlineContent,
                    modifier =
                    Modifier
                        .clickable {
                            textFieldEnabled = true
                        }
                        .testTag(TestTag.Profile.EditProfileNameButton)
                )
            } else {
                ProfileEditBasicTextField(
                    modifier =
                    Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag(TestTag.Profile.NewProfileNameField),
                    enabled = textFieldEnabled,
                    initialProfileName = initialProfileName,
                    onChangeProfileName = { name: String, isValid: Boolean ->
                        profileName = name
                        profileNameValid = isValid
                    },
                    color = color,
                    profiles = profileState.data?.profiles ?: emptyList(),
                    onDone = {
                        if (profileNameValid) {
                            onUpdateProfileName(profileName)
                            textFieldEnabled = false
                            scope.launch { keyboardController?.hide() }
                        }
                    }
                )
            }
        }

        if (!profileNameValid) {
            SpacerTiny()
            val errorText =
                if (profileName.isBlank()) {
                    stringResource(R.string.edit_profile_empty_profile_name)
                } else {
                    stringResource(R.string.edit_profile_duplicated_profile_name)
                }

            ErrorText(
                text = errorText,
                modifier = Modifier.padding(start = PaddingDefaults.Medium)
            )
        }
    }
}

@Composable
private fun ProfileEditBasicTextField(
    modifier: Modifier,
    enabled: Boolean,
    textStyle: TextStyle = AppTheme.typography.h5,
    color: Color,
    initialProfileName: String,
    onChangeProfileName: (String, Boolean) -> Unit,
    profiles: List<ProfilesUseCaseData.Profile>,
    onDone: () -> Unit
) {
    var profileNameState by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialProfileName,
                selection = TextRange(initialProfileName.length)
            )
        )
    }

    val mergedTextStyle = textStyle.merge(TextStyle(color = color))

    BasicTextField(
        value = profileNameState,
        onValueChange = {
            val name = it.text.trimStart().sanitizeProfileName()
            profileNameState =
                TextFieldValue(
                    text = name,
                    selection = it.selection,
                    composition = it.composition
                )

            onChangeProfileName(
                name,
                name.trim().equals(initialProfileName, true) ||
                    !profiles.containsProfileWithName(name) &&
                    name.isNotEmpty()
            )
        },
        enabled = enabled,
        singleLine = !enabled,
        textStyle = mergedTextStyle,
        modifier = modifier,
        cursorBrush = SolidColor(color),
        keyboardOptions =
        KeyboardOptions(
            autoCorrect = false,
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}

@Composable
private fun ProfileAvatarSection(
    profile: ProfilesUseCaseData.Profile,
    onClickEditAvatar: () -> Unit
) {
    val selectedColor = profileColor(profileColorNames = profile.color)

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium)
    ) {
        SpacerTiny()
        Surface(
            modifier =
            Modifier
                .size(140.dp)
                .align(Alignment.CenterHorizontally),
            shape = CircleShape,
            color = selectedColor.backGroundColor
        ) {
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .circularBorder(selectedColor.borderColor)
                    .clickable(onClick = onClickEditAvatar),
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    emptyIcon = Icons.Rounded.AddAPhoto,
                    modifier = Modifier.size(SizeDefaults.triple),
                    image = profile.image,
                    profileColor = profile.color.color(),
                    avatar = profile.avatar
                )
            }
        }
        SpacerSmall()
        TextButton(
            modifier =
            Modifier
                .align(Alignment.CenterHorizontally)
                .testTag(TestTag.Profile.EditProfileImageButton),
            onClick = onClickEditAvatar
        ) {
            Text(text = stringResource(R.string.edit_profile_avatar), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ProfileInsuranceInformation(
    lastAuthenticated: Instant?,
    ssoTokenScope: IdpData.SingleSignOnTokenScope?,
    insuranceInformation: ProfileInsuranceInformation,
    onClickLogIn: () -> Unit
) {
    SpacerLarge()
    val cardAccessNumber =
        if (ssoTokenScope is IdpData.TokenWithHealthCardScope) {
            ssoTokenScope.cardAccessNumber
        } else {
            null
        }

    Column {
        Text(
            stringResource(
                id = R.string.insurance_information_header
            ),
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
            style = AppTheme.typography.h6
        )
        SpacerSmall()

        if (lastAuthenticated != null) {
            LabeledText(
                stringResource(R.string.insurance_information_insurant_name),
                insuranceInformation.insurantName
            )
            LabeledText(
                stringResource(R.string.insurance_information_insurance_name),
                insuranceInformation.insuranceName
            )
            cardAccessNumber?.let {
                LabeledText(stringResource(R.string.insurance_information_insurant_can), it)
            }
            LabeledText(
                stringResource(R.string.insurance_information_insurance_identifier),
                insuranceInformation.insuranceIdentifier,
                Modifier.testTag(TestTag.Profile.InsuranceId)
            )
        }

        if (ssoTokenScope != null) {
            LabeledText(
                stringResource(R.string.profile_insurance_information_connected_label),
                when (ssoTokenScope) {
                    is IdpData.DefaultToken ->
                        stringResource(
                            R.string.profile_insurance_information_connected_health_card
                        )

                    is IdpData.ExternalAuthenticationToken -> ssoTokenScope.authenticatorName
                    is IdpData.AlternateAuthenticationToken,
                    is IdpData.AlternateAuthenticationWithoutToken
                    ->
                        stringResource(
                            R.string.profile_insurance_information_connected_biometrics
                        )
                }
            )
        } else {
            ClickableLabeledTextWithIcon(
                description = stringResource(R.string.profile_insurance_information_connected_label),
                content = stringResource(R.string.profile_insurance_information_not_connected),
                icon = Icons.Rounded.Refresh
            ) {
                onClickLogIn()
            }
        }
        SpacerLarge()
        Divider()
        SpacerLarge()
    }
}

/**
 * Shows the given content if != null labeled with a description as described in design guide for ProfileScreen.
 */
@Composable
private fun LabeledText(
    description: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(PaddingDefaults.Medium)) {
        Text(content, style = AppTheme.typography.body1)
        Text(description, style = AppTheme.typography.body2l)
    }
}

@Composable
private fun ClickableLabeledTextWithIcon(
    description: String,
    content: String,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(PaddingDefaults.Medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(content, style = AppTheme.typography.body1)
            Text(description, style = AppTheme.typography.body2l)
        }
        Icon(icon, contentDescription = null, tint = AppTheme.colors.primary600)
    }
}

@LightDarkPreview
@Composable
fun ProfileScreenPreview(
    @PreviewParameter(ProfileStatePreviewParameterProvider::class) profileState: UiState<ProfileCombinedData>
) {
    val listState = rememberLazyListState()
    PreviewAppTheme {
        val color = AppTheme.colors.primary600
        ProfileScreenContent(
            listState = listState,
            profileState = profileState,
            color = color,
            keyboardController = null,
            onUpdateProfileName = {},
            onClickEditAvatar = {},
            onClickLogIn = {},
            onClickInvoices = {},
            onShowPairedDevices = {},
            onClickAuditEvents = {}
        )
    }
}
