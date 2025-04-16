/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.model.ProfileIconState
import de.gematik.ti.erp.app.prescription.ui.preview.AvatarPreview
import de.gematik.ti.erp.app.prescription.ui.preview.AvatarPreviewParameterProvider
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import kotlinx.coroutines.delay

private const val OneThird = 1 / 3f
private const val IconTweenDuration = 2000
private const val IconDelayDuration = 2500L

/**
 * A composable that represents a clickable profile chip UI element with dynamic states,
 * styling, and behavior based on the provided profile and connectivity state.
 *
 * @param profile The [ProfilesUseCaseData.Profile] object containing user-related information.
 * @param profileIconState A variable that provides the state at which the profile icon is going to be
 * @param selected A boolean indicating if this chip is currently selected.
 * @param onClickChangeProfileName A lambda invoked when the chip is long-pressed, allowing the user to change the profile name.
 * @param onClickChip A lambda invoked when the chip is clicked, typically used for selecting the profile.
 */
@Suppress("CyclomaticComplexMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileChip(
    profile: ProfilesUseCaseData.Profile,
    profileIconState: ProfileIconState,
    selected: Boolean,
    onClickChangeProfileName: (profile: ProfilesUseCaseData.Profile) -> Unit,
    onClickChip: (ProfilesUseCaseData.Profile) -> Unit
) {
    var iconVisible by remember { mutableStateOf(true) }

    LaunchedEffect(profileIconState, profile.id) {
        iconVisible = true
        delay(IconDelayDuration) // Delay the icon animation to prevent flickering
        iconVisible = false
    }

    val haptic = LocalHapticFeedback.current

    // Configuration for device screen size, used to set the maximum chip width
    val configuration = LocalConfiguration.current
    val maxChipWidth = (configuration.screenWidthDp.dp) * OneThird

    // Define the chip's shape, background, text, and border colors based on its selected state
    val shape = RoundedCornerShape(SizeDefaults.one)
    val backgroundColor = if (selected) {
        AppTheme.colors.neutral100
    } else {
        AppTheme.colors.neutral025
    }
    val textColor = if (selected) {
        AppTheme.colors.neutral900
    } else {
        AppTheme.colors.neutral600
    }
    val borderColor = if (selected) {
        AppTheme.colors.neutral300
    } else {
        AppTheme.colors.neutral200
    }

    // Content description for accessibility
    val longPressDescription = stringResource(R.string.edit_profile_name)
    val clickDescription = stringResource(R.string.activate_profile)
    val activeProfileDescription = stringResource(R.string.active_description)
    val inactiveProfileDescription = stringResource(R.string.inactive_description)
    Surface(
        modifier = Modifier
            .clip(shape)
            .combinedClickable(
                role = Role.Button,
                onClick = { onClickChip(profile) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClickChangeProfileName(profile)
                },
                onClickLabel = clickDescription,
                onLongClickLabel = longPressDescription
            )
            .widthIn(max = maxChipWidth)
            .width(IntrinsicSize.Max),
        shape = shape,
        border = BorderStroke(SizeDefaults.eighth, borderColor),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = SizeDefaults.one, horizontal = PaddingDefaults.ShortMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Displays the profile name with ellipsis if it's too long
            Text(
                text = profile.name,
                style = AppTheme.typography.subtitle2,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics {
                        stateDescription = profile.name
                        contentDescription = if (profile.isActive) {
                            activeProfileDescription
                        } else {
                            inactiveProfileDescription
                        }
                    }
            )

            // Display the profile icon with animation if the chip is selected
            if (profile.lastAuthenticated != null && selected) {
                val animatedIconSize by animateDpAsState(
                    label = "iconSize",
                    targetValue = if (iconVisible) {
                        if (profileIconState is ProfileIconState.IsRefreshing) SizeDefaults.doubleHalf else SizeDefaults.double
                    } else SizeDefaults.zero,
                    animationSpec = tween(durationMillis = IconTweenDuration)
                )
                val animatedAlpha by animateFloatAsState(
                    label = "iconAlpha",
                    targetValue = if (iconVisible) 1f else 0f, // Gradually fade out
                    animationSpec = tween(durationMillis = IconTweenDuration)
                )
                val rotateAlpha by rememberInfiniteTransition(label = "rotate").animateFloat(
                    label = "shimmerAlpha",
                    initialValue = 0f,
                    targetValue = 359f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 1000,
                            easing = FastOutLinearInEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val shimmerAlpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
                    label = "shimmerAlpha",
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 1000,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                if (iconVisible) {
                    SpacerSmall()
                }
                Icon(
                    modifier = Modifier
                        .size(animatedIconSize)
                        .then(
                            when {
                                profileIconState is ProfileIconState.IsRefreshing -> Modifier.rotate(rotateAlpha)
                                else -> Modifier
                            }
                        )
                        .graphicsLayer {
                            alpha = if (profileIconState is ProfileIconState.IsRefreshing) shimmerAlpha else animatedAlpha
                        },
                    imageVector = profileIconState.chip().icon,
                    contentDescription = null,
                    tint = profileIconState.chip().color
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
fun ProfileChipSelectedPreview(
    @PreviewParameter(AvatarPreviewParameterProvider::class) state: AvatarPreview
) {
    PreviewAppTheme {
        ProfileChip(
            profile = state.activeProfile,
            profileIconState = state.profileIconState,
            selected = true,
            onClickChip = {},
            onClickChangeProfileName = {}
        )
    }
}

@LightDarkPreview
@Composable
fun ProfileChipUnSelectedPreview(
    @PreviewParameter(AvatarPreviewParameterProvider::class) state: AvatarPreview
) {
    PreviewAppTheme {
        ProfileChip(
            profile = state.activeProfile,
            profileIconState = state.profileIconState,
            selected = false,
            onClickChip = {},
            onClickChangeProfileName = {}
        )
    }
}
