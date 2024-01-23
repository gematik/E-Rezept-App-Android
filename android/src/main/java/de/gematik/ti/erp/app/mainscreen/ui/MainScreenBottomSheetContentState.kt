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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.ui.AvatarPicker
import de.gematik.ti.erp.app.profiles.ui.ColorPicker
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.profiles.ui.ProfileImage
import de.gematik.ti.erp.app.profiles.ui.ProfilesController
import de.gematik.ti.erp.app.profiles.ui.rememberProfilesController
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.ui.rememberSettingsController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.sanitizeProfileName
import kotlinx.coroutines.launch

@Stable
sealed class MainScreenBottomSheetContentState {
    @Stable
    class EditProfilePicture(
        val popUp: MainScreenBottomPopUpNames.EditProfilePicture = MainScreenBottomPopUpNames.EditProfilePicture
    ) : MainScreenBottomSheetContentState()

    @Stable
    class EditProfileName(
        val popUp: MainScreenBottomPopUpNames.EditProfileName = MainScreenBottomPopUpNames.EditProfileName
    ) : MainScreenBottomSheetContentState()

    @Stable
    class AddProfile(
        val popUp: MainScreenBottomPopUpNames.AddProfile = MainScreenBottomPopUpNames.AddProfile
    ) : MainScreenBottomSheetContentState()

    @Stable
    class Welcome(
        val popUp: MainScreenBottomPopUpNames.Welcome = MainScreenBottomPopUpNames.Welcome
    ) : MainScreenBottomSheetContentState()
}

@Composable
fun MainScreenBottomSheetContentState(
    infoContentState: MainScreenBottomSheetContentState?,
    mainNavController: NavController,
    profileToRename: ProfilesUseCaseData.Profile,
    onCancel: () -> Unit
) {
    val profilesController = rememberProfilesController()
    val settingsController = rememberSettingsController()
    val profileHandler = LocalProfileHandler.current

    val title = when (infoContentState) {
        is MainScreenBottomSheetContentState.EditProfilePicture ->
            stringResource(R.string.mainscreen_bottom_sheet_edit_profile_image)
        is MainScreenBottomSheetContentState.EditProfileName ->
            stringResource(R.string.bottom_sheet_edit_profile_name_title)
        is MainScreenBottomSheetContentState.AddProfile ->
            stringResource(R.string.bottom_sheet_edit_profile_name_title)
        else -> null
    }

    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(top = PaddingDefaults.Small, bottom = PaddingDefaults.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        title?.let {
            SpacerMedium()
            Text(it, style = AppTheme.typography.subtitle1)
            SpacerMedium()
        }
        LazyColumn(
            Modifier.fillMaxWidth()
        ) {
            item {
                infoContentState?.let {
                    when (it) {
                        is MainScreenBottomSheetContentState.EditProfilePicture ->
                            EditProfileAvatar(
                                profile = profileHandler.activeProfile,
                                clearPersonalizedImage = {
                                    scope.launch {
                                        profilesController.clearPersonalizedImage(profileHandler.activeProfile.id)
                                    }
                                },
                                onPickPersonalizedImage = {
                                    mainNavController.navigate(
                                        MainNavigationScreens.ProfileImageCropper.path(
                                            profileId = profileHandler.activeProfile.id
                                        )
                                    )
                                },
                                onSelectAvatar = { avatar ->
                                    scope.launch {
                                        profilesController.saveAvatarFigure(profileHandler.activeProfile.id, avatar)
                                    }
                                },
                                onSelectProfileColor = { color ->
                                    scope.launch {
                                        profilesController.updateProfileColor(profileHandler.activeProfile, color)
                                    }
                                }
                            )

                        is MainScreenBottomSheetContentState.EditProfileName ->
                            ProfileSheetContent(
                                profilesController = profilesController,
                                addProfile = false,
                                profileToEdit = profileToRename,
                                onCancel = onCancel
                            )

                        is MainScreenBottomSheetContentState.AddProfile ->
                            ProfileSheetContent(
                                profilesController = profilesController,
                                addProfile = true,
                                profileToEdit = null,
                                onCancel = onCancel
                            )
                        is MainScreenBottomSheetContentState.Welcome ->
                            ConnectBottomSheetContent(
                                onClickConnect = {
                                    scope.launch {
                                        settingsController.welcomeDrawerShown()
                                    }
                                    mainNavController.navigate(
                                        MainNavigationScreens.CardWall.path(profileHandler.activeProfile.id)
                                    )
                                },
                                onCancel = {
                                    scope.launch {
                                        settingsController.welcomeDrawerShown()
                                    }
                                    onCancel()
                                }
                            )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileSheetContent(
    profilesController: ProfilesController,
    profileToEdit: ProfilesUseCaseData.Profile?,
    addProfile: Boolean = false,
    onCancel: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val profilesState by profilesController.profilesState
    var textValue by remember { mutableStateOf(profileToEdit?.name ?: "") }
    var duplicated by remember { mutableStateOf(false) }

    val onEdit = {
        if (!addProfile) {
            profileToEdit?.let {
                scope.launch { profilesController.updateProfileName(it.id, textValue) }
            }
        } else {
            scope.launch {
                profilesController.addProfile(textValue)
            }
        }
        onCancel()
        keyboardController?.hide()
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier.testTag(TestTag.Main.MainScreenBottomSheet.ProfileNameField),
            shape = RoundedCornerShape(8.dp),
            value = textValue,
            singleLine = true,
            onValueChange = {
                val isNotExistingText = textValue.trim() != profileToEdit?.name
                val isNotExistingName = profilesState.containsProfileWithName(textValue)
                val name = it.trimStart().sanitizeProfileName()
                textValue = name
                duplicated = isNotExistingText && isNotExistingName
            },
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions {
                if (!duplicated && textValue.isNotEmpty()) {
                    onEdit()
                }
            },
            placeholder = { Text(stringResource(R.string.profile_edit_name_place_holder)) },
            isError = duplicated
        )

        if (duplicated) {
            Text(
                stringResource(R.string.edit_profile_duplicated_profile_name),
                color = AppTheme.colors.red600,
                style = AppTheme.typography.caption1,
                modifier = Modifier.padding(start = PaddingDefaults.Medium)
            )
        }
        SpacerLarge()
        PrimaryButton(
            modifier = Modifier.testTag(TestTag.Main.MainScreenBottomSheet.SaveProfileNameButton),
            enabled = !duplicated && textValue.isNotEmpty(),
            onClick = {
                onEdit()
            }
        ) {
            Text(stringResource(R.string.profile_bottom_sheet_save))
        }
    }
}

@Composable
private fun EditProfileAvatar(
    profile: ProfilesUseCaseData.Profile,
    clearPersonalizedImage: () -> Unit,
    onPickPersonalizedImage: () -> Unit,
    onSelectAvatar: (ProfilesData.AvatarFigure) -> Unit,
    onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    ProfileColorAndImagePickerContent(
        profile,
        clearPersonalizedImage = clearPersonalizedImage,
        onPickPersonalizedImage = onPickPersonalizedImage,
        onSelectAvatar = onSelectAvatar,
        onSelectProfileColor = onSelectProfileColor
    )
}

@Composable
private fun ProfileColorAndImagePickerContent(
    profile: ProfilesUseCaseData.Profile,
    clearPersonalizedImage: () -> Unit,
    onPickPersonalizedImage: () -> Unit,
    onSelectAvatar: (ProfilesData.AvatarFigure) -> Unit,
    onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SpacerMedium()
        ProfileImage(profile) {
            clearPersonalizedImage()
        }

        SpacerXXLarge()
        AvatarPicker(
            profile = profile,
            currentAvatarFigure = profile.avatarFigure,
            onPickPersonalizedImage = onPickPersonalizedImage,
            onSelectAvatar = onSelectAvatar
        )

        if (profile.avatarFigure != ProfilesData.AvatarFigure.PersonalizedImage) {
            SpacerXXLarge()
            SpacerMedium()
            Text(
                stringResource(R.string.edit_profile_background_color),
                style = AppTheme.typography.h6
            )
            SpacerLarge()

            ColorPicker(profile.color, onSelectProfileColor)
            SpacerLarge()
        }
    }
}

@Composable
private fun ConnectBottomSheetContent(onClickConnect: () -> Unit, onCancel: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpacerSmall()
        Image(
            painterResource(R.drawable.man_phone_blue_circle),
            null
        )
        Text(
            stringResource(R.string.mainscreen_welcome_drawer_header),
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        SpacerSmall()
        Text(
            stringResource(R.string.mainscreen_welcome_drawer_info),
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
        SpacerLarge()
        PrimaryButton(
            modifier = Modifier
                .testTag(TestTag.Main.MainScreenBottomSheet.LoginButton),
            onClick = onClickConnect,
            contentPadding = PaddingValues(
                vertical = 13.dp,
                horizontal = 48.dp
            )
        ) {
            Text(
                stringResource(R.string.mainscreen_connect_bottomsheet_connect)
            )
        }
        SpacerMedium()
        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTag.Main.MainScreenBottomSheet.ConnectLaterButton),
            contentPadding = PaddingValues(
                vertical = 13.dp
            )
        ) {
            Text(
                stringResource(R.string.mainscreen_connect_bottomsheet_connect_later)
            )
        }
    }
}
