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

@file:Suppress("UnusedPrivateMember")

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
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
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
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
    data object Warning : BannerIcon(Icons.Default.WarningAmber, Color.Black)
    data class Custom(override val vector: ImageVector, override val color: Color) :
        BannerIcon(vector, color)
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
fun Banner(
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String? = null,
    contentColor: Color = Color.White,
    containerColor: Color = AppTheme.colors.primary300,
    borderColor: Color = AppTheme.colors.primary500,
    startIcon: BannerClickableIcon = BannerClickableIcon(BannerIcon.NoIcon, null),
    gearsIcon: BannerClickableIcon = BannerClickableIcon(BannerIcon.NoIcon, null),
    bottomIcon: BannerClickableTextIcon = BannerClickableTextIcon(null, BannerIcon.NoIcon, null),
    onClickClose: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SizeDefaults.double))
            .border(
                border = BorderStroke(SizeDefaults.eighth, borderColor),
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
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                title?.let {
                    SpacerSmall()
                    ErezeptText.SubtitleOne(
                        modifier = Modifier
                            .padding(horizontal = PaddingDefaults.MediumPlus)
                            .weight(MAX_WEIGHT),
                        color = contentColor,
                        text = title
                    )
                }
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
                            modifier = Modifier.align(Alignment.CenterVertically),
                            imageVector = vector,
                            tint = contentColor,
                            contentDescription = "Info"
                        )
                    }
                }

                text?.let {
                    Text(
                        modifier = Modifier.weight(MAX_WEIGHT),
                        style = AppTheme.typography.body1,
                        text = it
                    )
                }

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
                            modifier = Modifier.align(Alignment.CenterVertically),
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
                        .padding(horizontal = PaddingDefaults.Small),
                    onClick = { bottomIcon.onClick?.invoke() }
                ) {
                    ErezeptText.Body(text = text, color = color)
                    SpacerTiny()
                    Icon(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        imageVector = vector,
                        tint = color,
                        contentDescription = "ArrowForward"
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleBanner(
    text: String,
    modifier: Modifier = Modifier,
    contentColor: Color = AppTheme.colors.neutral800,
    containerColor: Color = AppTheme.colors.primary300
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .background(containerColor),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ErezeptText.Body(
            modifier = Modifier.padding(vertical = PaddingDefaults.Small),
            color = contentColor,
            text = text
        )
    }
}

@LightDarkPreview
@Composable
private fun BannerPreview() {
    PreviewTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            Banner(
                contentColor = AppTheme.colors.yellow900,
                containerColor = AppTheme.colors.yellow100,
                borderColor = AppTheme.colors.yellow600,
                text = "Here we are explaining you why",
                startIcon = BannerClickableIcon(icon = BannerIcon.Warning) {}
            )

            Banner(
                text = "Another way to use this banner, as a information segment",
                startIcon = BannerClickableIcon(icon = BannerIcon.Info) {},
                bottomIcon = BannerClickableTextIcon(text = "You do something here and you click this for that", icon = BannerIcon.Gears) {}
            )

            Banner(
                title = "The big banner with the warning text",
                contentColor = AppTheme.colors.red900,
                containerColor = AppTheme.colors.red100,
                borderColor = AppTheme.colors.red600
            )
        }
    }
}
