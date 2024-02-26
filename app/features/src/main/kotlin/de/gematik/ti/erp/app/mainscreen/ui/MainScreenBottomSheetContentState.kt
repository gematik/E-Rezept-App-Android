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
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.navigation.MainScreenBottomPopUpNames
import de.gematik.ti.erp.app.mainscreen.presentation.MainScreenController
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.ProfileController
import de.gematik.ti.erp.app.profiles.ui.AvatarPicker
import de.gematik.ti.erp.app.profiles.ui.ColorPicker
import de.gematik.ti.erp.app.profiles.ui.ProfileImage
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge

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

    @Stable
    class GrantConsent(
        val popUp: MainScreenBottomPopUpNames.GrantConsent = MainScreenBottomPopUpNames.GrantConsent
    ) : MainScreenBottomSheetContentState()
}

@Composable
fun MainScreenBottomSheetContentState(
    mainNavController: NavController,
    mainScreenController: MainScreenController,
    profileController: ProfileController,
    infoContentState: MainScreenBottomSheetContentState?,
    profileToRename: ProfilesUseCaseData.Profile,
    onGrantConsent: () -> Unit,
    onCancel: () -> Unit
) {
    val profile by profileController.getActiveProfileState()

    val title = when (infoContentState) {
        is MainScreenBottomSheetContentState.EditProfilePicture ->
            stringResource(R.string.mainscreen_bottom_sheet_edit_profile_image)

        is MainScreenBottomSheetContentState.EditProfileName ->
            stringResource(R.string.bottom_sheet_edit_profile_name_title)

        is MainScreenBottomSheetContentState.AddProfile ->
            stringResource(R.string.bottom_sheet_edit_profile_name_title)

        else -> null
    }

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
                                profile = profile,
                                clearPersonalizedImage = {
                                    profileController.clearPersonalizedImage(profile.id)
                                },
                                onPickPersonalizedImage = {
                                    mainNavController.navigate(
                                        ProfileRoutes.ProfileImageCropperScreen.path(
                                            profileId = profile.id
                                        )
                                    )
                                },
                                onSelectAvatar = { avatar ->

                                    profileController.saveAvatarFigure(profile.id, avatar)
                                },
                                onSelectProfileColor = { color ->

                                    profileController.updateProfileColor(profile, color)
                                }
                            )

                        is MainScreenBottomSheetContentState.EditProfileName ->
                            ProfileSheetContent(
                                profileController = profileController,
                                addProfile = false,
                                profileToEdit = profileToRename,
                                onCancel = onCancel
                            )

                        is MainScreenBottomSheetContentState.AddProfile ->
                            ProfileSheetContent(
                                profileController = profileController,
                                addProfile = true,
                                profileToEdit = null,
                                onCancel = onCancel
                            )

                        is MainScreenBottomSheetContentState.Welcome ->
                            ConnectBottomSheetContent(
                                onClickConnect = {
                                    mainScreenController.welcomeDrawerShown()
                                    mainNavController.navigate(
                                        CardWallRoutes.CardWallIntroScreen.path(profile.id)
                                    )
                                },
                                onCancel = {
                                    mainScreenController.welcomeDrawerShown()
                                    onCancel()
                                }
                            )

                        is MainScreenBottomSheetContentState.GrantConsent ->
                            GrantConsentBottomSheetContent(
                                onClickGrantConsent = {
                                    onGrantConsent()
                                },
                                onCancel = {
                                    onCancel()
                                }
                            )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProfileAvatar(
    profile: ProfilesUseCaseData.Profile,
    clearPersonalizedImage: () -> Unit,
    onPickPersonalizedImage: () -> Unit,
    onSelectAvatar: (ProfilesData.Avatar) -> Unit,
    onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    ProfileColorAndImagePickerContent(
        profile = profile,
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
    onSelectAvatar: (ProfilesData.Avatar) -> Unit,
    onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    var editableProfile by remember { mutableStateOf(profile) }
    Column(modifier = Modifier.fillMaxSize()) {
        SpacerMedium()
        ProfileImage(editableProfile) {
            editableProfile = editableProfile.copy(
                avatar = ProfilesData.Avatar.PersonalizedImage,
                image = null
            )
            clearPersonalizedImage()
        }

        SpacerXXLarge()
        AvatarPicker(
            profile = editableProfile,
            currentAvatar = editableProfile.avatar,
            onPickPersonalizedImage = onPickPersonalizedImage,
            onSelectAvatar = {
                editableProfile = editableProfile.copy(avatar = it)
                onSelectAvatar(it)
            }
        )

        if (editableProfile.avatar != ProfilesData.Avatar.PersonalizedImage) {
            SpacerXXLarge()
            SpacerMedium()
            Text(
                stringResource(R.string.edit_profile_background_color),
                style = AppTheme.typography.h6
            )
            SpacerLarge()

            ColorPicker(
                profileColorName = editableProfile.color,
                onSelectProfileColor = {
                    editableProfile = editableProfile.copy(color = it)
                    onSelectProfileColor(it)
                }
            )
            SpacerLarge()
        }
    }
}

@Composable
private fun ConnectBottomSheetContent(onClickConnect: () -> Unit, onCancel: () -> Unit) {
    ConnectBottomSheet(
        header = stringResource(R.string.mainscreen_welcome_drawer_header),
        info = stringResource(R.string.mainscreen_welcome_drawer_info),
        image = painterResource(R.drawable.man_phone_blue_circle),
        connectButtonText = stringResource(R.string.mainscreen_connect_bottomsheet_connect),
        cancelButtonText = stringResource(R.string.mainscreen_connect_bottomsheet_connect_later),
        onClickConnect = onClickConnect,
        onCancel = onCancel
    )
}

@Composable
private fun GrantConsentBottomSheetContent(onClickGrantConsent: () -> Unit, onCancel: () -> Unit) {
    ConnectBottomSheet(
        header = stringResource(R.string.give_consent_bottom_sheet_header),
        info = stringResource(R.string.give_consent_bottom_sheet_info),
        image = painterResource(R.drawable.pharmacist_circle_blue),
        connectButtonText = stringResource(R.string.give_consent_bottom_sheet_activate),
        cancelButtonText = stringResource(R.string.give_consent_bottom_sheet_activate_later),
        onClickConnect = onClickGrantConsent,
        onCancel = onCancel
    )
}

@Composable
private fun ConnectBottomSheet(
    header: String,
    info: String,
    image: Painter,
    connectButtonText: String,
    cancelButtonText: String,
    onClickConnect: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpacerSmall()
        Image(
            image,
            null
        )
        Text(
            header,
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        SpacerSmall()
        Text(
            info,
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
        SpacerLarge()
        PrimaryButton(
            modifier = Modifier
                .testTag(TestTag.Main.MainScreenBottomSheet.GetConsentButton),
            onClick = onClickConnect,
            contentPadding = PaddingValues(
                vertical = 13.dp,
                horizontal = 48.dp
            )
        ) {
            Text(
                connectButtonText
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
                cancelButtonText
            )
        }
    }
}
