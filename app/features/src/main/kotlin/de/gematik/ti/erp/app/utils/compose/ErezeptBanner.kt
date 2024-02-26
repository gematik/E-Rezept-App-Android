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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.letNotNullOnCondition

private const val MIN_WEIGHT = 0.1f
private const val MAX_WEIGHT = 0.9f
private const val INFO_WEIGHT = 0.15f

sealed class BannerIcon(
    open val vector: ImageVector?,
    open val color: Color?
) {
    data object NoIcon : BannerIcon(null, null)
    data object Gears : BannerIcon(Icons.Default.Settings, Color.White)
    data object Info : BannerIcon(Icons.Default.Info, Color.White)
    data object Warning : BannerIcon(Icons.Default.Warning, Color.Black)
    data class Custom(override val vector: ImageVector, override val color: Color) : BannerIcon(vector, color)
}

data class BannerClickableIcon(
    val icon: BannerIcon,
    val onClick: (() -> Unit)?
)

data class BannerClickableTextIcon(
    val text: String?,
    val icon: BannerIcon,
    val onClick: (() -> Unit)?
)

@Composable
fun ErezeptBanner(
    title: String,
    text: String,
    contentColor: Color = Color.White,
    containerColor: Color = AppTheme.colors.primary300,
    startIcon: BannerClickableIcon = BannerClickableIcon(BannerIcon.NoIcon, null),
    gearsIcon: BannerClickableIcon = BannerClickableIcon(BannerIcon.NoIcon, null),
    bottomIcon: BannerClickableTextIcon = BannerClickableTextIcon(null, BannerIcon.NoIcon, null),
    onClickClose: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SizeDefaults.double))
            .border(
                border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.primary500),
                shape = RoundedCornerShape(SizeDefaults.double)
            ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(SizeDefaults.half)
    ) {
        val isNoStartIcon = startIcon.icon == BannerIcon.NoIcon
        val isNoGearsIcon = gearsIcon.icon == BannerIcon.NoIcon
        val isNoBottomIcon = bottomIcon.icon == BannerIcon.NoIcon
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PaddingDefaults.Small)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = PaddingDefaults.Small),
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                ErezeptText.SubtitleOne(
                    modifier = Modifier
                        .padding(horizontal = PaddingDefaults.MediumPlus)
                        .weight(MAX_WEIGHT),
                    color = AppTheme.colors.primary600,
                    text = title
                )
                onClickClose?.let {
                    IconButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(MIN_WEIGHT)
                            .padding(
                                end = PaddingDefaults.ShortMedium
                            )
                            .size(SizeDefaults.triple),
                        onClick = it
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .let {
                        when {
                            isNoStartIcon -> it.padding(horizontal = PaddingDefaults.Medium)
                            else -> it.padding(end = PaddingDefaults.Medium)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                letNotNullOnCondition(
                    first = startIcon.icon.vector,
                    second = startIcon.icon.color,
                    condition = { !isNoStartIcon && startIcon.onClick != null }
                ) { vector, color ->
                    IconButton(
                        modifier = Modifier.weight(INFO_WEIGHT),
                        onClick = { startIcon.onClick?.invoke() }
                    ) {
                        Icon(
                            imageVector = vector,
                            tint = color,
                            contentDescription = "Info"
                        )
                    }
                }
                Text(
                    modifier = Modifier.weight(MAX_WEIGHT),
                    style = AppTheme.typography.body1,
                    text = text
                )
                letNotNullOnCondition(
                    first = gearsIcon.icon.vector,
                    second = gearsIcon.icon.color,
                    condition = { !isNoGearsIcon && gearsIcon.onClick != null }
                ) { vector, color ->
                    IconButton(
                        modifier = Modifier
                            .weight(MIN_WEIGHT)
                            .size(SizeDefaults.triple),
                        onClick = { gearsIcon.onClick?.invoke() }
                    ) {
                        Icon(
                            imageVector = vector,
                            tint = color,
                            contentDescription = "Close"
                        )
                    }
                }
            }
            letNotNullOnCondition(
                first = bottomIcon.text,
                second = bottomIcon.icon.vector,
                third = bottomIcon.icon.color,
                condition = { !isNoBottomIcon && bottomIcon.onClick != null }
            ) { text, vector, color ->
                TextButton(
                    modifier = Modifier
                        .testTag(TestTag.AlertDialog.ConfirmButton)
                        .padding(horizontal = PaddingDefaults.Small),
                    onClick = { bottomIcon.onClick?.invoke() }
                ) {
                    ErezeptText.Body(text = text, color = color)
                    SpacerTiny()
                    Icon(
                        imageVector = vector,
                        tint = color,
                        contentDescription = "ArrowForward"
                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
fun AllIconsBannerPreview() {
    PreviewAppTheme {
        ErezeptBanner(
            title = "Lorem ipsum dolor sit amet",
            text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt " +
                "ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et",
            startIcon = BannerClickableIcon(BannerIcon.Info) {},
            gearsIcon = BannerClickableIcon(BannerIcon.Gears) {},
            bottomIcon = BannerClickableTextIcon(
                text = "Lorem ipsum dolor sit amet",
                icon = BannerIcon.Custom(Icons.Default.ArrowForward, AppTheme.colors.neutral999)
            ) {},
            onClickClose = {}
        )
    }
}

@LightDarkPreview
@Composable
fun OnlyStartIconBannerPreview() {
    PreviewAppTheme {
        ErezeptBanner(
            title = "Lorem ipsum dolor sit amet",
            startIcon = BannerClickableIcon(BannerIcon.Warning) {},
            contentColor = AppTheme.colors.neutral000,
            containerColor = AppTheme.colors.yellow600,
            text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt " +
                "ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et"
        )
    }
}

@LightDarkPreview
@Composable
fun OnlyGearsIconBannerPreview() {
    PreviewAppTheme {
        ErezeptBanner(
            title = "Lorem ipsum dolor sit amet",
            contentColor = AppTheme.colors.neutral999,
            containerColor = AppTheme.colors.primary200,
            text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt " +
                "ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et",
            gearsIcon = BannerClickableIcon(BannerIcon.Gears) {}
        )
    }
}

@LightDarkPreview
@Composable
fun BottomTextBannerPreview() {
    PreviewAppTheme {
        ErezeptBanner(
            title = "404 Seite?",
            contentColor = AppTheme.colors.neutral999,
            containerColor = AppTheme.colors.primary100,
            text = "Bitte aktivieren Sie in Ihren Einstellungen das Öffnen von Links, um fortzufahren",
            bottomIcon = BannerClickableTextIcon(
                text = "Lorem ipsum dolor sit amet",
                icon = BannerIcon.Custom(Icons.Default.ArrowForward, AppTheme.colors.primary600)
            ) {},
            onClickClose = {}
        )
    }
}
