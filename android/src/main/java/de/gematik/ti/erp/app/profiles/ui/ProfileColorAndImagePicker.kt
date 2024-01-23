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

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ProfileColorAndImagePicker(
    selectedProfile: ProfilesUseCaseData.Profile,
    clearPersonalizedImage: () -> Unit,
    onPickPersonalizedImage: () -> Unit,
    onBack: () -> Unit,
    onSelectAvatar: (ProfilesData.AvatarFigure) -> Unit,
    onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            NavigationTopAppBar(
                navigationMode = NavigationBarMode.Back,
                title = stringResource(R.string.edit_profile_picture),
                onBack = onBack,
                actions = {}
            )
        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(PaddingDefaults.Medium)
        ) {
            item {
                SpacerMedium()
                ProfileImage(selectedProfile) {
                    clearPersonalizedImage()
                }
            }
            item {
                SpacerXXLarge()
                AvatarPicker(
                    profile = selectedProfile,
                    currentAvatarFigure = selectedProfile.avatarFigure,
                    onPickPersonalizedImage = onPickPersonalizedImage,
                    onSelectAvatar = onSelectAvatar
                )
            }

            if (selectedProfile.avatarFigure != ProfilesData.AvatarFigure.PersonalizedImage) {
                item {
                    SpacerXXLarge()
                    SpacerMedium()
                    Text(
                        stringResource(R.string.edit_profile_background_color),
                        style = AppTheme.typography.h6
                    )
                    SpacerLarge()
                    ColorPicker(selectedProfile.color, onSelectProfileColor)
                }
            }
        }
    }
}

@Composable
fun AvatarPicker(
    profile: ProfilesUseCaseData.Profile,
    currentAvatarFigure: ProfilesData.AvatarFigure,
    onPickPersonalizedImage: () -> Unit,
    onSelectAvatar: (ProfilesData.AvatarFigure) -> Unit
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        ProfilesData.AvatarFigure.values().forEach { figure ->
            item {
                AvatarSelector(
                    figure = figure,
                    profile = profile,
                    selected = figure == currentAvatarFigure,
                    onPickPersonalizedImage = onPickPersonalizedImage,
                    onSelectAvatar = onSelectAvatar
                )
            }
        }
    }
}

@Composable
fun AvatarSelector(
    profile: ProfilesUseCaseData.Profile,
    figure: ProfilesData.AvatarFigure,
    selected: Boolean,
    onPickPersonalizedImage: () -> Unit,
    onSelectAvatar: (ProfilesData.AvatarFigure) -> Unit
) {
    Surface(
        modifier = Modifier
            .size(80.dp),
        shape = CircleShape,
        border = if (selected) {
            BorderStroke(5.dp, color = AppTheme.colors.primary600)
        } else if (figure != ProfilesData.AvatarFigure.PersonalizedImage) {
            BorderStroke(1.dp, color = AppTheme.colors.neutral300)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = when (figure) {
                        ProfilesData.AvatarFigure.PersonalizedImage -> {
                            AppTheme.colors.neutral100
                        }

                        else -> {
                            AppTheme.colors.neutral025
                        }
                    }
                )
                .clickable(onClick = {
                    if (figure == ProfilesData.AvatarFigure.PersonalizedImage) {
                        onPickPersonalizedImage()
                        onSelectAvatar(figure)
                    }
                    onSelectAvatar(figure)
                }),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChooseAvatar(
                useSmallImages = true,
                emptyIcon = Icons.Rounded.AddAPhoto,
                iconModifier = Modifier.size(24.dp),
                profile = profile,
                figure = figure
            )
        }
    }
}

@Composable
fun ColorPicker(
    profileColorName: ProfilesData.ProfileColorNames,
    onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    val currentSelectedColors = profileColor(profileColorNames = profileColorName)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            ProfilesData.ProfileColorNames.values().forEach {
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
                currentSelectedColors.colorName,
                style = AppTheme.typography.body2l
            )
        }
    }
}

@Composable
fun ProfileImage(selectedProfile: ProfilesUseCaseData.Profile, onClickDeleteAvatar: () -> Unit) {
    val colors = profileColor(profileColorNames = selectedProfile.color)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium)
            .semantics(true) {
                stateDescription = selectedProfile.color.name
            }
    ) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .aspectRatio(1f)
                    .background(colors.backGroundColor),
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    iconModifier = Modifier.size(36.dp),
                    profile = selectedProfile,
                    emptyIcon = Icons.Rounded.PersonOutline,
                    figure = selectedProfile.avatarFigure,
                    showPersonalizedImage = selectedProfile.personalizedImage != null
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
                    IconButton(onClick = onClickDeleteAvatar) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            tint = AppTheme.colors.neutral600,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}
