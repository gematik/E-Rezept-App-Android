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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.mainscreen.model.ProfileIconState
import de.gematik.ti.erp.app.mainscreen.model.ProfileIconState.IsError
import de.gematik.ti.erp.app.mainscreen.model.ProfileIconState.IsOffline
import de.gematik.ti.erp.app.mainscreen.model.ProfileIconState.IsOnline
import de.gematik.ti.erp.app.mainscreen.ui.ProfileChip
import de.gematik.ti.erp.app.prescription.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.model.PrescriptionScreenData.AvatarDimensions
import de.gematik.ti.erp.app.prescription.model.PrescriptionScreenData.AvatarDimensions.Default
import de.gematik.ti.erp.app.prescription.model.PrescriptionScreenData.AvatarDimensions.Small
import de.gematik.ti.erp.app.prescription.ui.preview.AvatarPreview
import de.gematik.ti.erp.app.prescription.ui.preview.AvatarPreviewParameterProvider
import de.gematik.ti.erp.app.profiles.ui.components.ChooseAvatar
import de.gematik.ti.erp.app.profiles.ui.components.color
import de.gematik.ti.erp.app.profiles.ui.components.profileColor
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.OutlinedIconButton
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

@Composable
fun ProfileConnectionSection(
    activeProfile: UiState<ProfilesUseCaseData.Profile>,
    profileIconState: ProfileIconState,
    isRegistered: Boolean,
    onClickAvatar: () -> Unit,
    onClickLogin: () -> Unit,
    onClickRefresh: () -> Unit
) {
    UiStateMachine(activeProfile) { profile ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainScreenAvatar(
                modifier = Modifier
                    .weight(0.6f)
                    .align(Alignment.CenterVertically),
                activeProfile = profile,
                profileIconState = profileIconState,
                isRegistered = isRegistered,
                avatarDimension = Small(),
                onClickAvatar = onClickAvatar
            )
            if (profileIconState !is IsError) {
                ConnectionHelper(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxWidth(), // Ensures proper alignment vertically
                    contentAlignment = Alignment.CenterEnd, // Aligns content to the end
                    isProfileWithValidSsoTokenScope = profile.isSSOTokenValid(),
                    onClickLogin = onClickLogin,
                    onClickRefresh = onClickRefresh
                )
            }
        }
    }
}

@Composable
fun MainScreenAvatar(
    modifier: Modifier = Modifier,
    activeProfile: ProfilesUseCaseData.Profile,
    profileIconState: ProfileIconState,
    isRegistered: Boolean,
    avatarDimension: AvatarDimensions = Default(),
    onClickAvatar: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (avatarDimension) {
            is Small -> {
                AvatarScreen(
                    profile = activeProfile,
                    avatarDimension = avatarDimension.dimension,
                    profileIconState = profileIconState,
                    onClickAvatar = onClickAvatar
                )
                if (isRegistered) {
                    SpacerMedium()
                    var fontColor = AppTheme.colors.neutral600
                    var statusText = stringResource(R.string.not_logged_in)
                    when {
                        profileIconState is IsError -> {
                            fontColor = AppTheme.colors.yellow900
                            statusText = stringResource(R.string.no_login_state_no_internet)
                        }

                        profileIconState is IsOffline -> {
                            fontColor = AppTheme.colors.neutral600
                            statusText = stringResource(R.string.not_logged_in)
                        }

                        profileIconState is IsOnline -> {
                            fontColor = AppTheme.colors.green800
                            statusText = stringResource(R.string.logged_in)
                        }
                    }
                    Text(
                        modifier = Modifier
                            .padding(
                                end = PaddingDefaults.Medium
                            )
                            .clearAndSetSemantics {},
                        text = statusText,
                        color = fontColor,
                        style = AppTheme.typography.subtitle2
                    )
                }
            }

            is Default -> AvatarScreen(
                profile = activeProfile,
                avatarDimension = avatarDimension.dimension,
                profileIconState = profileIconState,
                onClickAvatar = onClickAvatar
            )
        }
    }
}

@Composable
fun AvatarScreen(
    profile: ProfilesUseCaseData.Profile,
    profileIconState: ProfileIconState,
    avatarDimension: PrescriptionScreenData.AvatarDimension,
    onClickAvatar: () -> Unit
) {
    val selectedColor = profileColor(profileColorNames = profile.color)
    val description = stringResource(id = R.string.edit_profile_picture)
    val noInternet = stringResource(R.string.no_login_state_no_internet)
    val notLoggedIn = stringResource(R.string.not_logged_in)
    val loggedIn = stringResource(R.string.logged_in)

    Box(
        modifier = Modifier.padding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(avatarDimension.avatarSize),
            shape = CircleShape,
            color = selectedColor.backGroundColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onClickAvatar
                    )
                    .semantics {
                        role = Role.Button
                        contentDescription = description
                    },
                contentAlignment = Alignment.Center
            ) {
                ChooseAvatar(
                    modifier = Modifier.size(avatarDimension.chooseSize),
                    emptyIcon = Icons.Rounded.AddAPhoto,
                    image = profile.image,
                    profileColor = profile.color.color(),
                    avatar = profile.avatar
                )
            }
        }
        if (profile.lastAuthenticated != null) {
            Box(
                modifier = Modifier
                    .size(avatarDimension.statusSize)
                    .align(Alignment.BottomEnd)
                    .offset(avatarDimension.statusOffset.x, avatarDimension.statusOffset.y)
                    .clip(CircleShape)
                    .aspectRatio(1f)
                    .background(profileIconState.avatar().background)
                    .border(
                        avatarDimension.statusBorder,
                        AppTheme.colors.neutral000,
                        CircleShape
                    ).semantics {
                        contentDescription = when (profileIconState) {
                            IsError -> noInternet
                            IsOffline -> notLoggedIn
                            IsOnline -> loggedIn
                            ProfileIconState.IsRefreshing -> ""
                        }
                    }
            ) {
                Icon(
                    modifier = Modifier
                        .size(avatarDimension.iconSize)
                        .align(Alignment.Center),
                    contentDescription = null,
                    imageVector = profileIconState.avatar().imageVector,
                    tint = profileIconState.avatar().tint
                )
            }
        }
    }
}

@Requirement(
    "A_24857#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Refreshing the prescription list happens only if the user is authenticated. " +
        "If the user is not authenticated, the user is prompted to authenticate."
)
@Composable
fun ConnectionHelper(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment,
    isProfileWithValidSsoTokenScope: Boolean,
    onClickLogin: () -> Unit,
    onClickRefresh: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment
    ) {
        if (isProfileWithValidSsoTokenScope) {
            OutlinedIconButton(
                onClick = onClickRefresh,
                imageVector = Icons.Default.Replay,
                contentDescription = stringResource(R.string.refresh)
            )
        } else {
            TertiaryButton(onClickLogin) {
                Text(stringResource(R.string.mainscreen_login))
            }
        }
    }
}

@LightDarkPreview
@Composable
fun ProfileStatePreviews(
    @PreviewParameter(AvatarPreviewParameterProvider::class) state: AvatarPreview
) {
    PreviewAppTheme {
        Column(
            modifier = Modifier.padding(PaddingDefaults.Medium)
        ) {
            SpacerMedium()
            Row(
                horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.XXLarge)
            ) {
                Column {
                    Text(
                        text = "ProfileChip",
                        style = AppTheme.typography.subtitle2,
                        color = AppTheme.colors.neutral600
                    )
                    SpacerTiny()
                    ProfileChip(
                        profile = state.activeProfile,
                        profileIconState = state.profileIconState,
                        selected = true,
                        onClickChip = {},
                        onClickChangeProfileName = {}
                    )
                }
                Column {
                    Text(
                        text = "Default size",
                        style = AppTheme.typography.subtitle2,
                        color = AppTheme.colors.neutral600
                    )
                    SpacerTiny()
                    MainScreenAvatar(
                        activeProfile = state.activeProfile,
                        profileIconState = state.profileIconState,
                        isRegistered = state.isRegistered,
                        avatarDimension = Default(),
                        onClickAvatar = {}
                    )
                }
            }
            SpacerMedium()
            Box(
                modifier = Modifier
                    .border(width = SizeDefaults.quarter, color = AppTheme.colors.neutral100) // Add a border
                    .padding(PaddingDefaults.Tiny)
            ) {
                Column {
                    Text(
                        text = "Small size",
                        style = AppTheme.typography.subtitle2,
                        color = AppTheme.colors.neutral600
                    )
                    SpacerTiny()
                    ProfileConnectionSection(
                        activeProfile = UiState.Data(state.activeProfile),
                        profileIconState = state.profileIconState,
                        isRegistered = state.isRegistered,
                        onClickAvatar = {},
                        onClickLogin = {},
                        onClickRefresh = {}
                    )
                }
            }
        }
    }
}
