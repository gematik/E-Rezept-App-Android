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

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import dev.jeziellago.compose.markdowntext.MarkdownText

// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41311-36198&m=dev

@Composable
internal fun CommResV3InternalMessageCard(
    content: String,
    time: String,
    modifier: Modifier = Modifier,
    tag: String? = null,
    title: String? = null,
    showAppIcon: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            width = SizeDefaults.sixteenth,
            color = AppTheme.colors.neutral200
        ),
        shape = RoundedCornerShape(SizeDefaults.double),
        backgroundColor = AppTheme.colors.neutral000,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            when {
                showAppIcon -> {
                    Box(
                        modifier = Modifier
                            .size(SizeDefaults.fourfold)
                            .clip(CircleShape)
                            .background(AppTheme.colors.primary700),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_logo_outlined),
                            contentDescription = null,
                            tint = AppTheme.colors.neutral000,
                            modifier = Modifier.size(SizeDefaults.triple)
                        )
                    }
                }

                !tag.isNullOrBlank() -> {
                    Text(
                        text = tag,
                        style = AppTheme.typography.caption1,
                        color = AppTheme.colors.neutral900,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(SizeDefaults.double))
                            .background(AppTheme.colors.primary100)
                            .padding(
                                horizontal = PaddingDefaults.Small,
                                vertical = PaddingDefaults.Tiny
                            )
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
            ) {
                title?.let {
                    Text(
                        text = it,
                        style = AppTheme.typography.subtitle1,
                        color = AppTheme.colors.neutral900
                    )
                }

                MarkdownText(
                    markdown = content,
                    style = if (title == null) {
                        AppTheme.typography.body2.copy(color = AppTheme.colors.neutral900)
                    } else {
                        AppTheme.typography.caption1.copy(color = AppTheme.colors.neutral700)
                    },
                    linkColor = AppTheme.colors.primary700
                )

                Text(
                    text = time,
                    style = AppTheme.typography.caption1,
                    fontStyle = FontStyle.Italic,
                    color = AppTheme.colors.neutral700
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun CommResV3InternalMessageCardPreview() {
    PreviewTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large),
            modifier = Modifier
                .background(AppTheme.colors.neutral100)
                .padding(PaddingDefaults.Medium)
        ) {
            CommResV3InternalMessageCard(
                title = "\uD83C\uDF89 Herzlich Willkommen!",
                content = "Herzlich Willkommen in der E-Rezept App! Mit dieser App können Sie digital E-Rezepte empfangen und an eine " +
                    "Apotheke Ihrer Wahl senden.",
                time = "15.05.23, 17:48",
                showAppIcon = true
            )
            CommResV3InternalMessageCard(
                tag = "Neuerungen in der App Version 1.39.0",
                content = "**✅ T-Rezepte verfügbar**\nT-Rezepte können jetzt empfangen und korrekt angezeigt werden.",
                time = "15.08.26, 08:00"
            )
        }
    }
}
