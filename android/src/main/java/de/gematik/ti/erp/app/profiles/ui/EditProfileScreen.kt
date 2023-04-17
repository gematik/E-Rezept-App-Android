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

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.EuroSymbol
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.TestTag.Profile.OpenTokensScreenButton
import de.gematik.ti.erp.app.TestTag.Profile.ProfileScreen
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.mainscreen.ui.rememberMainScreenController
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.ui.ProfileNameDialog
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.DynamicText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import de.gematik.ti.erp.app.utils.sanitizeProfileName
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@Composable
fun EditProfileScreen(
    profilesState: ProfilesStateData.ProfilesState,
    profile: ProfilesUseCaseData.Profile,
    profilesController: ProfilesController,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    onBack: () -> Unit,
    mainNavController: NavController
) {
    val navController = rememberNavController()
    val mainScreenController = rememberMainScreenController()

    EditProfileNavGraph(
        profilesState = profilesState,
        navController = navController,
        onBack = onBack,
        selectedProfile = profile,
        mainScreenController = mainScreenController,
        profilesController = profilesController,
        onRemoveProfile = onRemoveProfile,
        mainNavController = mainNavController
    )
}

@Composable
fun EditProfileScreen(
    profileId: String,
    profilesController: ProfilesController,
    onBack: () -> Unit,
    mainNavController: NavController
) {
    val profilesState by profilesController.profilesState
    val scope = rememberCoroutineScope()

    profilesState.profileById(profileId)?.let { profile ->
        val selectedProfile = remember(profile) {
            profile
        }
        EditProfileScreen(
            profilesState = profilesState,
            onBack = onBack,
            profile = selectedProfile,
            profilesController = profilesController,
            onRemoveProfile = {
                scope.launch {
                    profilesController.removeProfile(profile, it)
                    onBack()
                }
            },
            mainNavController = mainNavController
        )
    }
}

@Suppress("LongParameterList")
@Composable
fun EditProfileScreenContent(
    onBack: () -> Unit,
    selectedProfile: ProfilesUseCaseData.Profile,
    profilesState: ProfilesStateData.ProfilesState,
    profilesController: ProfilesController,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    onClickEditAvatar: () -> Unit,
    onClickToken: () -> Unit,
    onClickLogIn: () -> Unit,
    onClickLogout: () -> Unit,
    onClickAuditEvents: () -> Unit,
    onClickPairedDevices: () -> Unit,
    onClickInvoices: () -> Unit
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    var showAddDefaultProfileDialog by remember { mutableStateOf(false) }
    var deleteProfileDialogVisible by remember { mutableStateOf(false) }

    if (deleteProfileDialogVisible) {
        DeleteProfileDialog(
            onCancel = { deleteProfileDialogVisible = false },
            onClickAction = {
                if (profilesState.profiles.size == 1) {
                    showAddDefaultProfileDialog = true
                } else {
                    onRemoveProfile(null)
                }
                deleteProfileDialogVisible = false
            }
        )
    }

    AnimatedElevationScaffold(
        modifier = Modifier
            .imePadding()
            .visualTestTag(ProfileScreen),
        topBarTitle = stringResource(R.string.edit_profile_title),
        navigationMode = NavigationBarMode.Back,
        scaffoldState = scaffoldState,
        listState = listState,
        actions = {
            ThreeDotMenu(
                selectedProfile = selectedProfile,
                onClickLogIn = onClickLogIn,
                onClickLogout = onClickLogout,
                onClickDelete = { deleteProfileDialogVisible = true }
            )
        },
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.testTag(TestTag.Profile.ProfileScreenContent),
            state = listState,
            contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
        ) {
            item {
                ProfileNameSection(
                    profile = selectedProfile,
                    profilesState = profilesState,
                    onChangeProfileName = {
                        scope.launch {
                            profilesController.updateProfileName(selectedProfile.id, it)
                        }
                    }
                )
            }
            item {
                SpacerLarge()
                ProfileAvatarSection(
                    profile = selectedProfile,
                    onClickEditAvatar = onClickEditAvatar
                )
            }
            item {
                ProfileInsuranceInformation(
                    selectedProfile.lastAuthenticated,
                    selectedProfile.ssoTokenScope,
                    selectedProfile.insuranceInformation,
                    onClickLogIn
                )
            }

            if (selectedProfile.insuranceInformation.insuranceType == ProfilesUseCaseData.InsuranceType.PKV) {
                item {
                    ProfileInvoiceInformation { onClickInvoices() }
                }
            }

            if (selectedProfile.ssoTokenScope != null) {
                item {
                    ProfileEditPairedDeviceSection(onShowPairedDevices = onClickPairedDevices)
                }
            }
            item { SecuritySection(onClickToken, onClickAuditEvents) }
        }

        if (showAddDefaultProfileDialog) {
            ProfileNameDialog(
                profilesController = profilesController,
                wantRemoveLastProfile = true,
                onEdit = { showAddDefaultProfileDialog = false; onRemoveProfile(it) },
                onDismissRequest = { showAddDefaultProfileDialog = false }
            )
        }
    }
}

@Composable
fun ProfileInvoiceInformation(onClick: () -> Unit) {
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
            modifier = Modifier
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
fun ThreeDotMenu(
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
            modifier = Modifier.testTag(
                if (selectedProfile.ssoTokenScope != null) {
                    TestTag.Profile.LogoutButton
                } else {
                    TestTag.Profile.LoginButton
                }
            ),
            onClick = if (selectedProfile.ssoTokenScope != null) {
                onClickLogout
            } else {
                onClickLogIn
            }
        ) {
            Text(
                text = if (selectedProfile.ssoTokenScope != null) {
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
fun DeleteProfileDialog(onCancel: () -> Unit, onClickAction: () -> Unit) {
    CommonAlertDialog(
        header = stringResource(id = R.string.remove_profile_header),
        info = stringResource(R.string.remove_profile_detail_message),
        actionText = stringResource(R.string.remove_profile_yes),
        cancelText = stringResource(R.string.remove_profile_no),
        onCancel = onCancel,
        onClickAction = onClickAction
    )
}

@Composable
fun SecuritySection(
    onClickToken: () -> Unit,
    onClickAuditEvents: () -> Unit
) {
    SettingsMenuHeadline(stringResource(R.string.settings_appprotection_headline))
    SecurityTokenSubSection(onClickToken)
    SecurityAuditEventsSubSection(onClickAuditEvents)
}

@Composable
fun SecurityAuditEventsSubSection(onClickAuditEvents: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
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
fun SecurityTokenSubSection(onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onClick() }
            )
            .visualTestTag(OpenTokensScreenButton)
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Icon(Icons.Outlined.VpnKey, null, tint = AppTheme.colors.primary600)
        Column {
            Text(
                stringResource(
                    R.string.settings_show_token
                ),
                style = AppTheme.typography.body1
            )
            Text(
                stringResource(
                    R.string.settings_show_token_info
                ),
                style = AppTheme.typography.body2l
            )
        }
    }
}

@Composable
fun SettingsMenuHeadline(text: String) {
    Text(
        text = text,
        style = AppTheme.typography.h6,
        modifier = Modifier.padding(PaddingDefaults.Medium)
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileNameSection(
    profile: ProfilesUseCaseData.Profile,
    profilesState: ProfilesStateData.ProfilesState,
    onChangeProfileName: (String) -> Unit
) {
    var profileName by remember(profile.name) { mutableStateOf(profile.name) }
    var profileNameValid by remember { mutableStateOf(true) }
    var textFieldEnabled by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current

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
                val txt = buildAnnotatedString {
                    append(profileName)
                    append(" ")
                    appendInlineContent("edit", "edit")
                }
                val c = mapOf(
                    "edit" to InlineTextContent(
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
                    inlineContent = c,
                    modifier = Modifier
                        .clickable {
                            textFieldEnabled = true
                        }
                        .testTag(TestTag.Profile.EditProfileNameButton)
                )
            } else {
                ProfileEditBasicTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag(TestTag.Profile.NewProfileNameField),
                    enabled = textFieldEnabled,
                    initialProfileName = profile.name,
                    onChangeProfileName = { name: String, isValid: Boolean ->
                        profileName = name
                        profileNameValid = isValid
                    },
                    profilesState = profilesState,
                    onDone = {
                        if (profileNameValid) {
                            onChangeProfileName(profileName)
                            textFieldEnabled = false
                            scope.launch { keyboardController?.hide() }
                        }
                    }
                )
            }
        }

        if (!profileNameValid) {
            SpacerTiny()
            val errorText = if (profileName.isBlank()) {
                stringResource(R.string.edit_profile_empty_profile_name)
            } else {
                stringResource(R.string.edit_profile_duplicated_profile_name)
            }

            Text(
                text = errorText,
                color = AppTheme.colors.red600,
                style = AppTheme.typography.caption1,
                modifier = Modifier.padding(start = PaddingDefaults.Medium)
            )
        }
    }
}

@Composable
fun ProfileEditBasicTextField(
    modifier: Modifier,
    enabled: Boolean,
    textStyle: TextStyle = AppTheme.typography.h5,
    initialProfileName: String,
    onChangeProfileName: (String, Boolean) -> Unit,
    profilesState: ProfilesStateData.ProfilesState,
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

    val color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    val mergedTextStyle = textStyle.merge(TextStyle(color = color))

    BasicTextField(
        value = profileNameState,
        onValueChange = {
            val name = sanitizeProfileName(it.text.trimStart())
            profileNameState = TextFieldValue(
                text = name,
                selection = it.selection,
                composition = it.composition
            )

            onChangeProfileName(
                name,
                name.trim().equals(initialProfileName, true) ||
                    !profilesState.containsProfileWithName(name) && name.isNotEmpty()
            )
        },
        enabled = enabled,
        singleLine = !enabled,
        textStyle = mergedTextStyle,
        modifier = modifier,
        cursorBrush = SolidColor(color),
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}

@Composable
fun ProfileAvatarSection(
    profile: ProfilesUseCaseData.Profile,
    onClickEditAvatar: () -> Unit
) {
    val currentSelectedColors = profileColor(profileColorNames = profile.color)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium)
    ) {
        SpacerTiny()
        Surface(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.CenterHorizontally),
            shape = CircleShape,
            color = currentSelectedColors.backGroundColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClickEditAvatar),
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    emptyIcon = Icons.Rounded.AddAPhoto,
                    iconModifier = Modifier.size(24.dp),
                    profile = profile,
                    figure = profile.avatarFigure
                )
            }
        }
        SpacerSmall()
        TextButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .testTag(TestTag.Profile.EditProfileImageButton),
            onClick = onClickEditAvatar
        ) {
            Text(text = stringResource(R.string.edit_profile_avatar), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ChooseAvatar(
    iconModifier: Modifier = Modifier,
    useSmallImages: Boolean? = false,
    profile: ProfilesUseCaseData.Profile,
    emptyIcon: ImageVector,
    showPersonalizedImage: Boolean = true,
    figure: ProfilesData.AvatarFigure
) {
    val imageRessource = extractImageResource(useSmallImages, figure)

    when (figure) {
        ProfilesData.AvatarFigure.PersonalizedImage -> {
            if (showPersonalizedImage) {
                if (profile.personalizedImage != null) {
                    BitmapImage(profile)
                } else {
                    Icon(
                        emptyIcon,
                        modifier = iconModifier,
                        contentDescription = null,
                        tint = AppTheme.colors.neutral600
                    )
                }
            }
        }

        else -> {
            if (imageRessource == 0) {
                Icon(
                    emptyIcon,
                    modifier = iconModifier,
                    contentDescription = null,
                    tint = AppTheme.colors.neutral600
                )
            } else {
                Image(
                    painterResource(id = imageRessource),
                    null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun extractImageResource(
    useSmallImages: Boolean? = false,
    figure: ProfilesData.AvatarFigure
) = if (useSmallImages == true) {
    when (figure) {
        ProfilesData.AvatarFigure.FemaleDoctor -> R.drawable.femal_doctor_small_portrait
        ProfilesData.AvatarFigure.WomanWithHeadScarf -> R.drawable.woman_with_head_scarf_small_portrait
        ProfilesData.AvatarFigure.Grandfather -> R.drawable.grand_father_small_portrait
        ProfilesData.AvatarFigure.BoyWithHealthCard -> R.drawable.boy_with_health_card_small_portrait
        ProfilesData.AvatarFigure.OldManOfColor -> R.drawable.old_man_of_color_small_portrait
        ProfilesData.AvatarFigure.WomanWithPhone -> R.drawable.woman_with_phone_small_portrait
        ProfilesData.AvatarFigure.Grandmother -> R.drawable.grand_mother_small_portrait
        ProfilesData.AvatarFigure.ManWithPhone -> R.drawable.man_with_phone_small_portrait
        ProfilesData.AvatarFigure.WheelchairUser -> R.drawable.wheel_chair_user_small_portrait
        ProfilesData.AvatarFigure.Baby -> R.drawable.baby_small_portrait
        ProfilesData.AvatarFigure.MaleDoctorWithPhone -> R.drawable.doctor_with_phone_small_portrait
        ProfilesData.AvatarFigure.FemaleDoctorWithPhone -> R.drawable.femal_doctor_with_phone_small_portrait
        ProfilesData.AvatarFigure.FemaleDeveloper -> R.drawable.femal_developer_small_portrait
        else -> 0
    }
} else {
    when (figure) {
        ProfilesData.AvatarFigure.FemaleDoctor -> R.drawable.femal_doctor_portrait
        ProfilesData.AvatarFigure.WomanWithHeadScarf -> R.drawable.woman_with_head_scarf_portrait
        ProfilesData.AvatarFigure.Grandfather -> R.drawable.grand_father_portrait
        ProfilesData.AvatarFigure.BoyWithHealthCard -> R.drawable.boy_with_health_card_portrait
        ProfilesData.AvatarFigure.OldManOfColor -> R.drawable.old_man_of_color_portrait
        ProfilesData.AvatarFigure.WomanWithPhone -> R.drawable.woman_with_phone_portrait
        ProfilesData.AvatarFigure.Grandmother -> R.drawable.grand_mother_portrait
        ProfilesData.AvatarFigure.ManWithPhone -> R.drawable.man_with_phone_portrait
        ProfilesData.AvatarFigure.WheelchairUser -> R.drawable.wheel_chair_user_portrait
        ProfilesData.AvatarFigure.Baby -> R.drawable.baby_portrait
        ProfilesData.AvatarFigure.MaleDoctorWithPhone -> R.drawable.doctor_with_phone_portrait
        ProfilesData.AvatarFigure.FemaleDoctorWithPhone -> R.drawable.femal_doctor_with_phone_portrait
        ProfilesData.AvatarFigure.FemaleDeveloper -> R.drawable.femal_developer_portrait
        else -> 0
    }
}

@Composable
fun BitmapImage(profile: ProfilesUseCaseData.Profile) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, profile) {
        value = profile.personalizedImage?.let {
            BitmapFactory.decodeByteArray(profile.personalizedImage, 0, it.size).asImageBitmap()
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ProfileInsuranceInformation(
    lastAuthenticated: Instant?,
    ssoTokenScope: IdpData.SingleSignOnTokenScope?,
    insuranceInformation: ProfilesUseCaseData.ProfileInsuranceInformation,
    onClickLogIn: () -> Unit
) {
    SpacerLarge()
    val cardAccessNumber = if (ssoTokenScope is IdpData.TokenWithHealthCardScope) {
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
                    is IdpData.DefaultToken -> stringResource(
                        R.string.profile_insurance_information_connected_health_card
                    )

                    is IdpData.ExternalAuthenticationToken -> ssoTokenScope.authenticatorName
                    is IdpData.AlternateAuthenticationToken,
                    is IdpData.AlternateAuthenticationWithoutToken -> stringResource(
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

@Composable
fun createProfileColor(colors: ProfilesData.ProfileColorNames): ProfileColor {
    return profileColor(profileColorNames = colors)
}

@Composable
fun ColorSelector(
    modifier: Modifier,
    profileColorName: ProfilesData.ProfileColorNames,
    selected: Boolean,
    onSelectColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    val colors = createProfileColor(profileColorName)
    val contentDescription = annotatedStringResource(
        R.string.edit_profile_color_selected,
        profileColorName.name
    ).toString()

    Surface(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = { onSelectColor(profileColorName) }),
        color = colors.backGroundColor
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    Icons.Outlined.Done,
                    contentDescription,
                    tint = colors.textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Shows the given content if != null labeled with a description as described in design guide for ProfileScreen.
 */
@Composable
fun LabeledText(description: String, content: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(PaddingDefaults.Medium)) {
        Text(content, style = AppTheme.typography.body1)
        Text(description, style = AppTheme.typography.body2l)
    }
}

@Composable
fun ClickableLabeledTextWithIcon(
    description: String,
    content: String,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
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
