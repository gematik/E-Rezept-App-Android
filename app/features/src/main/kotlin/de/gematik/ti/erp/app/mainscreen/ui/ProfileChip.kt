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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

private const val OneThird = 1 / 3f
private const val IconTweenDuration = 2000

@Suppress("CyclomaticComplexMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileChip(
    profile: Profile,
    refreshState: Boolean,
    selected: Boolean,
    onClickChangeProfileName: (profile: Profile) -> Unit,
    onClickChip: (Profile) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val ssoTokenScope = profile.ssoTokenScope

    data class IconState(val isVisible: Boolean, val icon: ImageVector)

    val isRefreshing = remember(refreshState, ssoTokenScope?.token) { refreshState }

    // Determine the icon visibility and type based on refreshState and token validity
    val iconState by remember(refreshState, ssoTokenScope?.token) {
        derivedStateOf {
            when {
                isRefreshing -> IconState(true, Icons.Outlined.Autorenew)
                ssoTokenScope?.token?.isValid() == true -> IconState(true, Icons.Rounded.CloudDone)
                else -> IconState(true, Icons.Outlined.CloudOff)
            }
        }
    }

    val color = ssoStatusColor(isRefreshing, profile, ssoTokenScope) ?: AppTheme.colors.neutral400

    val configuration = LocalConfiguration.current
    val maxChipWidth = (configuration.screenWidthDp.dp) * OneThird

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

    val description = stringResource(R.string.mainscreen_profile_chip_content_description)

    Surface(
        modifier = Modifier
            .clip(shape)
            .combinedClickable(
                onClick = {
                    onClickChip(profile)
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClickChangeProfileName(profile)
                },
                role = Role.Button
            )
            .widthIn(max = maxChipWidth)
            .width(IntrinsicSize.Max)
            .semantics {
                contentDescription = description
            },
        shape = shape,
        border = BorderStroke(SizeDefaults.eighth, borderColor),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(vertical = SizeDefaults.one, horizontal = PaddingDefaults.ShortMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SizeDefaults.one)
        ) {
            Text(
                text = profile.name,
                style = AppTheme.typography.subtitle2,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (profile.lastAuthenticated != null && selected) {
                val animatedIconSize by animateDpAsState(
                    label = "iconSize",
                    targetValue = if (isRefreshing) SizeDefaults.doubleHalf else SizeDefaults.double,
                    animationSpec = tween(durationMillis = IconTweenDuration)
                )
                val animatedAlpha by animateFloatAsState(
                    label = "iconAlpha",
                    targetValue = if (iconState.isVisible) 1f else 0.7f,
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

                Icon(
                    modifier = Modifier
                        .size(animatedIconSize)
                        .then(
                            when {
                                isRefreshing -> Modifier.rotate(rotateAlpha)
                                else -> Modifier
                            }
                        )
                        .graphicsLayer {
                            alpha = if (isRefreshing) shimmerAlpha else animatedAlpha
                        },
                    imageVector = iconState.icon,
                    contentDescription = null,
                    tint = color
                )
            }
        }
    }
}

@Composable
private fun ssoStatusColor(isRefreshing: Boolean, profile: Profile, ssoTokenScope: IdpData.SingleSignOnTokenScope?) =
    when {
        isRefreshing -> AppTheme.colors.primary400
        ssoTokenScope?.token?.isValid() == true -> AppTheme.colors.green400
        profile.lastAuthenticated != null -> AppTheme.colors.neutral400
        else -> null
    }
