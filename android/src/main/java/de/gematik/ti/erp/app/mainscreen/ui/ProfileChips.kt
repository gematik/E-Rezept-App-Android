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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.prescription.ui.rememberRefreshPrescriptionsController
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import kotlinx.coroutines.delay

private const val OneThird = 1 / 3f
private const val StateVisibilityTime = 4000L
private const val IconTweenDuration = 2000

@Composable
fun AddProfileChip(
    onClickAddProfile: () -> Unit,
    tooltipBounds: MutableState<Map<Int, Rect>>,
    toolTipBoundsRequired: Boolean
) {
    val shape = RoundedCornerShape(8.dp)

    Surface(
        modifier = Modifier
            .clip(shape)
            .clickable {
                onClickAddProfile()
            }
            .height(IntrinsicSize.Max)
            .onGloballyPositioned { coordinates ->
                if (toolTipBoundsRequired) {
                    tooltipBounds.value += Pair(2, coordinates.boundsInRoot())
                }
            }
            .testTag(TestTag.Main.AddProfileButton),
        shape = shape,
        border = BorderStroke(1.dp, AppTheme.colors.neutral300)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = PaddingDefaults.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = AppTheme.colors.primary600
            )
        }
        // empty text to achieve same height as profile chips
        Text(
            text = "",
            style = AppTheme.typography.subtitle2,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = PaddingDefaults.ShortMedium)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileChip(
    profile: ProfilesUseCaseData.Profile,
    selected: Boolean,
    mainScreenController: MainScreenController,
    onClickChip: (ProfileIdentifier) -> Unit,
    onClickChangeProfileName: (profile: ProfilesUseCaseData.Profile) -> Unit,
    tooltipBounds: MutableState<Map<Int, Rect>>,
    toolTipBoundsRequired: Boolean
) {
    val refreshPrescriptionsController = rememberRefreshPrescriptionsController(mainScreenController)

    val isRefreshing by refreshPrescriptionsController.isRefreshing
    var refreshEvent by remember { mutableStateOf<PrescriptionServiceState?>(null) }

    LaunchedEffect(Unit) {
        mainScreenController.onRefreshEvent.collect {
            refreshEvent = it
        }
    }

    var iconVisible by remember { mutableStateOf(false) }
    val ssoTokenScope = profile.ssoTokenScope

    LaunchedEffect(Unit) {
        ssoTokenScope?.token?.let {
            if (!it.isValid()) {
                iconVisible = true
                delay(StateVisibilityTime)
                iconVisible = false
            }
        }
    }

    LaunchedEffect(key1 = refreshEvent, key2 = isRefreshing) {
        iconVisible = true
        delay(StateVisibilityTime)
        iconVisible = false
    }

    val icon = when {
        ssoTokenScope?.token?.isValid() == true -> Icons.Rounded.CloudDone
        else -> Icons.Outlined.CloudOff
    }

    val color = if (refreshEvent is PrescriptionServiceErrorState) {
        AppTheme.colors.neutral600
    } else {
        ssoStatusColor(profile, ssoTokenScope) ?: AppTheme.colors.neutral400
    }

    val configuration = LocalConfiguration.current
    val maxChipWidth = (configuration.screenWidthDp.dp) * OneThird

    val shape = RoundedCornerShape(8.dp)

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
                onClick = { onClickChip(profile.id) },
                onLongClick = { onClickChangeProfileName(profile) },
                role = Role.Button
            )
            .widthIn(max = maxChipWidth)
            .width(IntrinsicSize.Max)
            .semantics {
                contentDescription = description
            }
            .onGloballyPositioned { coordinates ->
                if (profile.active && toolTipBoundsRequired) {
                    tooltipBounds.value += Pair(1, coordinates.boundsInRoot())
                }
            },
        shape = shape,
        border = BorderStroke(1.dp, borderColor),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = PaddingDefaults.ShortMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                AnimatedVisibility(
                    visible = iconVisible,
                    enter = fadeIn(animationSpec = tween(IconTweenDuration)),
                    exit = fadeOut(animationSpec = tween(IconTweenDuration))
                ) {
                    Icon(
                        imageVector = icon,
                        modifier = Modifier
                            .size(16.dp),
                        contentDescription = null,
                        tint = color
                    )
                }
            }
        }
    }
}

@Composable
private fun ssoStatusColor(profile: ProfilesUseCaseData.Profile, ssoTokenScope: IdpData.SingleSignOnTokenScope?) =
    when {
        ssoTokenScope?.token?.isValid() == true -> AppTheme.colors.green400
        profile.lastAuthenticated != null -> AppTheme.colors.neutral400
        else -> null
    }
