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

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.outlined.Image
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
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.ui.graphics.SolidColor
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.TestTag.Profile.OpenTokensScreenButton
import de.gematik.ti.erp.app.TestTag.Profile.ProfileScreen
import de.gematik.ti.erp.app.cardwall.ui.PrimaryButtonSmall
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.ui.AddProfileDialog
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.DynamicText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import de.gematik.ti.erp.app.utils.firstCharOfForeNameSurName
import de.gematik.ti.erp.app.utils.sanitizeProfileName
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun EditProfileScreen(
    state: SettingsScreen.State,
    profile: ProfilesUseCaseData.Profile,
    settingsViewModel: SettingsViewModel,
    profileSettingsViewModel: ProfileSettingsViewModel,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    onBack: () -> Unit,
    mainNavController: NavController
) {
    val navController = rememberNavController()

    EditProfileNavGraph(
        state = state,
        navController = navController,
        onBack = onBack,
        selectedProfile = profile,
        settingsViewModel = settingsViewModel,
        profileSettingsViewModel = profileSettingsViewModel,
        onRemoveProfile = onRemoveProfile,
        mainNavController = mainNavController
    )
}

@Composable
fun EditProfileScreen(
    profileId: String,
    settingsViewModel: SettingsViewModel,
    profileSettingsViewModel: ProfileSettingsViewModel,
    onBack: () -> Unit,
    mainNavController: NavController
) {
    val state by produceState(initialValue = SettingsScreen.defaultState) {
        settingsViewModel.screenState().collect {
            value = it
        }
    }

    state.profileById(profileId)?.let { profile ->
        val selectedProfile = remember(profile) {
            profile
        }
        EditProfileScreen(
            state = state,
            onBack = onBack,
            profile = selectedProfile,
            settingsViewModel = settingsViewModel,
            profileSettingsViewModel = profileSettingsViewModel,
            onRemoveProfile = {
                settingsViewModel.removeProfile(profile, it)
                onBack()
            },
            mainNavController = mainNavController
        )
    }
}

@Suppress("LongMethod")
@Composable
fun EditProfileScreenContent(
    onBack: () -> Unit,
    selectedProfile: ProfilesUseCaseData.Profile,
    state: SettingsScreen.State,
    profileSettingsViewModel: ProfileSettingsViewModel,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    onClickEditAvatar: () -> Unit,
    onClickToken: () -> Unit,
    onClickLogIn: () -> Unit,
    onClickLogout: () -> Unit,
    onClickAuditEvents: () -> Unit,
    onClickPairedDevices: () -> Unit
) {
    val listState = rememberLazyListState()
    var showAddDefaultProfileDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var deleteProfileDialogVisible by remember { mutableStateOf(false) }

    if (deleteProfileDialogVisible) {
        CommonAlertDialog(
            header = stringResource(id = R.string.remove_profile_header),
            info = stringResource(R.string.remove_profile_detail_message),
            actionText = stringResource(R.string.remove_profile_yes),
            cancelText = stringResource(R.string.remove_profile_no),
            onCancel = { deleteProfileDialogVisible = false },
            onClickAction = {
                if (state.profiles.size == 1) {
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
        listState = listState,
        actions = {
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
                        deleteProfileDialogVisible = true
                    }
                ) {
                    Text(
                        text = stringResource(R.string.remove_profile),
                        color = AppTheme.colors.red600
                    )
                }
            }
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
                    state = state,
                    onChangeProfileName = {
                        profileSettingsViewModel.updateProfileName(selectedProfile, it)
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

            if (selectedProfile.ssoTokenScope != null) {
                item {
                    ProfileEditPairedDeviceSection(onShowPairedDevices = onClickPairedDevices)
                }
            }
            item { SecuritySection(onClickToken, onClickAuditEvents) }
        }

        if (showAddDefaultProfileDialog) {
            AddProfileDialog(
                state = state,
                wantRemoveLastProfile = true,
                onEdit = { showAddDefaultProfileDialog = false; onRemoveProfile(it) },
                onDismissRequest = { showAddDefaultProfileDialog = false }
            )
        }
    }
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

@Composable
private fun ColumnScope.LoginButton(
    onClick: () -> Unit,
    buttonText: String
) {
    PrimaryButtonSmall(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .testTag(TestTag.Profile.LoginButton),
        onClick = onClick
    ) {
        Text(buttonText)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileNameSection(
    profile: ProfilesUseCaseData.Profile,
    state: SettingsScreen.State,
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
                    modifier = Modifier.clickable {
                        textFieldEnabled = true
                    }
                )
            } else {
                ProfileEditBasicTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    enabled = textFieldEnabled,
                    initialProfileName = profile.name,
                    onChangeProfileName = { name: String, isValid: Boolean ->
                        profileName = name
                        profileNameValid = isValid
                    },
                    state = state,
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
private fun ProfileEditBasicTextField(
    modifier: Modifier,
    enabled: Boolean,
    initialProfileName: String,
    onChangeProfileName: (String, Boolean) -> Unit,
    state: SettingsScreen.State,
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
    val mergedTextStyle = AppTheme.typography.h5.merge(TextStyle(color = color))

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
                name.trim() == initialProfileName || !state.containsProfileWithName(name) && name.isNotEmpty()
            )
        },
        enabled = enabled,
        singleLine = false,
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
    val initials = remember(profile.name) { firstCharOfForeNameSurName(profile.name) }

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
                    modifier = Modifier.fillMaxSize(),
                    profile = profile,
                    figure = profile.avatarFigure,
                    initials = initials,
                    currentSelectedColors = currentSelectedColors
                )
            }
        }
        SpacerSmall()
        TextButton(modifier = Modifier.align(Alignment.CenterHorizontally), onClick = onClickEditAvatar) {
            Text(text = stringResource(R.string.edit_profile_avatar), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ChooseAvatar(
    profile: ProfilesUseCaseData.Profile,
    modifier: Modifier = Modifier,
    showPersonalizedImage: Boolean = true,
    figure: ProfilesData.AvatarFigure,
    initials: String,
    currentSelectedColors: ProfileColor,
    textStyle: TextStyle = AppTheme.typography.h3
) {
    val imageRessource = when (figure) {
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

    when (figure) {
        ProfilesData.AvatarFigure.PersonalizedImage -> {
            if (profile.personalizedImage != null && showPersonalizedImage) {
                BitmapImage(profile)
            } else {
                Icon(Icons.Outlined.Image, null, modifier = Modifier.size(40.dp))
            }
        }
        ProfilesData.AvatarFigure.Initials -> {
            Text(
                text = initials,
                style = textStyle,
                color = if (showPersonalizedImage) {
                    currentSelectedColors.textColor
                } else {
                    AppTheme.colors.neutral600
                },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
        else -> {
            Image(
                painterResource(id = imageRessource),
                null,
                modifier
            )
        }
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

    Column(modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)) {
        Text(stringResource(id = R.string.insurance_information_header), style = AppTheme.typography.h6)
        SpacerMedium()

        if (lastAuthenticated != null) {
            Column {
                LabeledText(
                    stringResource(R.string.insurance_information_insurant_name),
                    insuranceInformation.insurantName
                )
                SpacerMedium()
                LabeledText(
                    stringResource(R.string.insurance_information_insurance_name),
                    insuranceInformation.insuranceName
                )
                SpacerMedium()
                cardAccessNumber?.let {
                    LabeledText(stringResource(R.string.insurance_information_insurant_can), it)
                    SpacerMedium()
                }

                LabeledText(
                    stringResource(R.string.insurance_information_insurance_identifier),
                    insuranceInformation.insuranceIdentifier,
                    Modifier.testTag(TestTag.Profile.InsuranceId)
                )
                SpacerMedium()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = AppTheme.colors.neutral300, shape = RoundedCornerShape(size = 16.dp))
                    .padding(PaddingDefaults.Medium)
            ) {
                Text(stringResource(R.string.insurance_information_login_description))
                SpacerMedium()
                LoginButton(onClickLogIn, stringResource(R.string.insurance_information_login))
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
        modifier = Modifier
            .size(40.dp),
        shape = CircleShape,
        color = colors.backGroundColor
    ) {
        Row(
            modifier = Modifier.clickable(onClick = { onSelectColor(profileColorName) }),
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
    Column(modifier) {
        Text(content, style = AppTheme.typography.body1)
        Text(description, style = AppTheme.typography.body2l)
    }
}
