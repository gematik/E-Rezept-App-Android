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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileController
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileEditPictureController
import de.gematik.ti.erp.app.profiles.ui.components.ChooseAvatar
import de.gematik.ti.erp.app.profiles.ui.components.ProfileBackgroundColorComponent
import de.gematik.ti.erp.app.profiles.ui.components.ProfileColor
import de.gematik.ti.erp.app.profiles.ui.components.ProfileImageSelectorDialog
import de.gematik.ti.erp.app.profiles.ui.components.color
import de.gematik.ti.erp.app.profiles.ui.components.profileColor
import de.gematik.ti.erp.app.profiles.ui.components.toDescription
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.Center
import de.gematik.ti.erp.app.utils.compose.CenterColumn
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.circularBorder

// TODO: this is duplicated from EditProfilePicture.kt, needs to be combined into one view
class ProfileEditPictureScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val dialog = LocalDialog.current
        val profileId =
            remember { requireNotNull(navBackStackEntry.arguments?.getString(ProfileRoutes.PROFILE_NAV_PROFILE_ID)) }
        val imageTypeDialogEvent by lazy { ComposableEvent<Unit>() }
        val profilesController = rememberProfileController()
        val profileEditPictureController = rememberProfileEditPictureController(profileId)
        val profileData by profileEditPictureController.profile.collectAsStateWithLifecycle()

        ProfileImageSelectorDialog(
            composableEvent = imageTypeDialogEvent,
            dialogScaffold = dialog,
            onPickEmojiImage = {
                navController.navigate(
                    ProfileRoutes.ProfileImageEmojiScreen.path(profileId)
                )
            },
            onPickPersonalizedImage = {
                navController.navigate(
                    ProfileRoutes.ProfileImageCropperScreen.path(profileId)
                )
            },
            onPickCamera = {
                navController.navigate(
                    ProfileRoutes.ProfileImageCameraScreen.path(profileId)
                )
            }
        )

        UiStateMachine(
            state = profileData,
            onLoading = {
                Center {
                    CircularProgressIndicator()
                }
            },
            onError = {
                Center {
                    Text("Error loading profile data")
                }
            },
            onContent = { selectedProfile ->
                val listState = rememberLazyListState()
                var editableProfile by remember(selectedProfile.image) { mutableStateOf(selectedProfile) }
                Scaffold(
                    modifier = Modifier.imePadding(),
                    topBar = {
                        NavigationTopAppBar(
                            navigationMode = NavigationBarMode.Back,
                            title = stringResource(R.string.edit_profile_picture),
                            backLabel = stringResource(R.string.back),
                            closeLabel = stringResource(R.string.cancel),
                            onBack = { navController.popBackStack() },
                            actions = {}
                        )
                    }
                ) { paddingValues ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(PaddingDefaults.Medium)
                    ) {
                        item {
                            SpacerMedium()
                            ProfileImage(editableProfile) {
                                editableProfile = editableProfile.copy(
                                    avatar = ProfilesData.Avatar.PersonalizedImage,
                                    image = null
                                )
                                profilesController.clearPersonalizedImage(selectedProfile.id)
                            }
                        }
                        item {
                            SpacerXXLarge()
                            AvatarPicker(
                                profile = editableProfile,
                                currentAvatar = editableProfile.avatar,
                                onPickPersonalizedImage = {
                                    imageTypeDialogEvent.trigger()
                                },
                                onSelectAvatar = {
                                    editableProfile = editableProfile.copy(avatar = it)
                                    profilesController.saveAvatarFigure(selectedProfile.id, it)
                                }
                            )
                        }
                        item {
                            CenterColumn {
                                ProfileBackgroundColorComponent(
                                    color = editableProfile.color
                                ) {
                                    editableProfile = editableProfile.copy(color = it)
                                    profilesController.updateProfileColor(selectedProfile, it)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun AvatarPicker(
    profile: ProfilesUseCaseData.Profile,
    currentAvatar: ProfilesData.Avatar?,
    onPickPersonalizedImage: () -> Unit,
    onSelectAvatar: (ProfilesData.Avatar) -> Unit
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        ProfilesData.Avatar.entries.forEachIndexed { index, figure ->
            item {
                AvatarSelector(
                    modifier = when (index) {
                        ProfilesData.Avatar.lastIndex -> Modifier.padding(end = PaddingDefaults.Small)
                        ProfilesData.Avatar.firstIndex -> Modifier.padding(start = PaddingDefaults.Small)
                        else -> Modifier
                    },
                    figure = figure,
                    profile = profile.copy(image = null),
                    selected = figure == currentAvatar,
                    onPickPersonalizedImage = onPickPersonalizedImage,
                    onSelectAvatar = onSelectAvatar
                )
            }
        }
    }
}

@Composable
private fun AvatarSelector(
    modifier: Modifier = Modifier,
    profile: ProfilesUseCaseData.Profile,
    figure: ProfilesData.Avatar,
    selected: Boolean,
    onPickPersonalizedImage: () -> Unit,
    onSelectAvatar: (ProfilesData.Avatar) -> Unit
) {
    val avatarDescription = figure.toDescription()
    val selectedDescription = stringResource(R.string.active_description)
    val notSelectedDescription = stringResource(R.string.inactive_description)
    val onClickDescription = stringResource(R.string.choose_profile_picture)
    Surface(
        modifier = modifier
            .size(SizeDefaults.tenfold)
            .semantics {
                role = Role.Button
                stateDescription = avatarDescription
                contentDescription = if (selected) {
                    selectedDescription
                } else {
                    notSelectedDescription
                }
            }
            .clickable(
                onClickLabel = onClickDescription,
                onClick = {
                    if (figure == ProfilesData.Avatar.PersonalizedImage) {
                        onPickPersonalizedImage()
                        onSelectAvatar(figure)
                    }
                    onSelectAvatar(figure)
                }
            ),
        shape = CircleShape,
        border = if (selected) {
            BorderStroke(SizeDefaults.fivefoldHalf, color = AppTheme.colors.primary700)
        } else if (figure != ProfilesData.Avatar.PersonalizedImage) {
            BorderStroke(SizeDefaults.eighth, color = AppTheme.colors.neutral300)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .testTag(TestTag.Profile.EditProfileIcon.AvatarSelectorRow)
                .background(
                    color = when (figure) {
                        ProfilesData.Avatar.PersonalizedImage -> AppTheme.colors.neutral100
                        else -> AppTheme.colors.neutral025
                    }
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChooseAvatar(
                useSmallImages = true,
                emptyIcon = Icons.Rounded.AddAPhoto,
                modifier = Modifier
                    .size(SizeDefaults.triple),
                image = profile.image,
                profileColor = profile.color.color(),
                avatar = figure
            )
        }
    }
}

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    profileColorName: ProfilesData.ProfileColorNames,
    onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    val currentSelectedColors = profileColor(profileColorNames = profileColorName)

    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            ProfilesData.ProfileColorNames.entries.forEach {
                val currentValueColors = profileColor(profileColorNames = it)
                ColorSelector(
                    modifier = Modifier.testTag(
                        when (it) {
                            ProfilesData.ProfileColorNames.SPRING_GRAY ->
                                TestTag.Profile.EditProfileIcon.ColorSelectorSpringGrayButton

                            ProfilesData.ProfileColorNames.SUN_DEW ->
                                TestTag.Profile.EditProfileIcon.ColorSelectorSunDewButton

                            ProfilesData.ProfileColorNames.PINK ->
                                TestTag.Profile.EditProfileIcon.ColorSelectorPinkButton

                            ProfilesData.ProfileColorNames.TREE ->
                                TestTag.Profile.EditProfileIcon.ColorSelectorTreeButton

                            ProfilesData.ProfileColorNames.BLUE_MOON ->
                                TestTag.Profile.EditProfileIcon.ColorSelectorBlueMoonButton
                        }
                    ),
                    profileColorName = it,
                    selected = currentValueColors == currentSelectedColors,
                    onSelectColor = onSelectProfileColor
                )
            }
        }
        SpacerMedium()
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                text = currentSelectedColors.colorName,
                color = AppTheme.colors.neutral600,
                style = AppTheme.typography.body2l
            )
        }
    }
}

@Composable
private fun createProfileColor(colors: ProfilesData.ProfileColorNames): ProfileColor {
    return profileColor(profileColorNames = colors)
}

@Composable
private fun ColorSelector(
    modifier: Modifier,
    profileColorName: ProfilesData.ProfileColorNames,
    selected: Boolean,
    onSelectColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    val colors = createProfileColor(profileColorName)
    val activeProfileDescription = stringResource(R.string.active_description)
    val inactiveProfileDescription = stringResource(R.string.inactive_description)
    Surface(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(
                role = Role.Button,
                onClick = { onSelectColor(profileColorName) }
            )
            .semantics {
                stateDescription = colors.colorName
                contentDescription = if (selected) {
                    activeProfileDescription
                } else {
                    inactiveProfileDescription
                }
            },
        color = colors.backgroundColor
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    Icons.Outlined.Done,
                    null,
                    tint = colors.textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileImage(
    selectedProfile: ProfilesUseCaseData.Profile,
    onClickDeleteAvatar: () -> Unit
) {
    val selectedColor = profileColor(profileColorNames = selectedProfile.color)
    val deleteDescription = stringResource(R.string.delete_profile_picture)
    val contentDescription = stringResource(R.string.profile_picture)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium)
            .semantics() {
                stateDescription = contentDescription
            }
    ) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Box(
                modifier = Modifier
                    .size(SizeDefaults.twentyfold)
                    .clip(CircleShape)
                    .aspectRatio(1f)
                    .circularBorder(selectedColor.borderColor)
                    .background(selectedColor.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    modifier = Modifier.size(SizeDefaults.fourfoldAndHalf),
                    image = selectedProfile.image,
                    profileColor = selectedProfile.color.color(),
                    emptyIcon = Icons.Rounded.PersonOutline,
                    avatar = selectedProfile.avatar
                )
            }
            if (!(selectedProfile.hasNoImageSelected())) {
                @Suppress("MagicNumber")
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.TopEnd)
                        .offset((-8).dp, 4.dp)
                        .clip(CircleShape)
                        .aspectRatio(1f)
                        .background(AppTheme.colors.neutral050)
                        .border(1.dp, color = AppTheme.colors.neutral000, shape = RoundedCornerShape(16.dp))
                ) {
                    IconButton(
                        onClick = {
                            onClickDeleteAvatar()
                        }
                    ) {
                        Icon(
                            modifier = Modifier.testTag(
                                TestTag.Profile.EditProfileIcon.DeleteAvatarButton
                            ),
                            imageVector = Icons.Rounded.Delete,
                            tint = AppTheme.colors.neutral600,
                            contentDescription = deleteDescription
                        )
                    }
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
fun AvatarSelectorPreview() {
    val profile = mockProfile()
    PreviewAppTheme {
        AvatarSelector(
            profile = profile,
            figure = profile.avatar,
            selected = false,
            onPickPersonalizedImage = {},
            onSelectAvatar = {}
        )
    }
}

@LightDarkPreview
@Composable
fun AvatarPickerPreview() {
    val profile = mockProfile()
    val currentAvatar = ProfilesData.Avatar.PersonalizedImage
    val onPickPersonalizedImage: () -> Unit = {}
    val onSelectAvatar: (ProfilesData.Avatar) -> Unit = {}
    PreviewAppTheme {
        AvatarPicker(
            profile = profile,
            currentAvatar = currentAvatar,
            onPickPersonalizedImage = onPickPersonalizedImage,
            onSelectAvatar = onSelectAvatar
        )
    }
}

@LightDarkPreview
@Composable
fun ColorPickerPreview() {
    val profileColorName = ProfilesData.ProfileColorNames.SPRING_GRAY
    val onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit = {}
    PreviewAppTheme {
        ColorPicker(
            modifier = Modifier.fillMaxWidth(),
            profileColorName = profileColorName,
            onSelectProfileColor = onSelectProfileColor
        )
    }
}

@LightDarkPreview
@Composable
fun ProfileImagePreview() {
    val profile = mockProfile()
    val onClickDeleteAvatar: () -> Unit = {}
    PreviewAppTheme {
        ProfileImage(
            selectedProfile = profile,
            onClickDeleteAvatar = onClickDeleteAvatar
        )
    }
}

@Composable
fun mockProfile(): ProfilesUseCaseData.Profile {
    return ProfilesUseCaseData.Profile(
        id = "",
        name = "",
        avatar = ProfilesData.Avatar.BoyWithHealthCard,
        color = ProfilesData.ProfileColorNames.PINK,
        isActive = false,
        insurance = de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation(),
        lastAuthenticated = null,
        ssoTokenScope = null,
        image = null
    )
}
