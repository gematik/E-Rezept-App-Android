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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import de.gematik.ti.erp.app.messages.ui.model.InAppMessageUiModel
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
internal fun InAppMessage(item: InAppMessageUiModel) {
    MessageTimeline(
        drawFilledTop = !item.isFirstMessage,
        drawFilledBottom = !item.isLastMessage,
        isClickable = false,
        timestamp = {
            Text(
                text = item.sentOn,
                style = AppTheme.typography.subtitle2
            )
        },

        content = {
            if (item.chipText?.isNotEmpty() == true) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(AppTheme.colors.primary100)
                        .padding(
                            horizontal = PaddingDefaults.Small,
                            vertical = SizeDefaults.threeSeventyFifth
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.chipText,
                        style = AppTheme.typography.caption2
                    )
                }
            }

            item.content?.let { markdown ->
                SpacerTiny()
                MarkdownText(
                    markdown = markdown,
                    style = AppTheme.typography.body2.copy(
                        color = AppTheme.colors.neutral900
                    ),
                    linkColor = AppTheme.colors.primary700
                )
            }
        }
    )
}

@LightDarkPreview
@Composable
fun InAppMessagePreview() {
    PreviewTheme {
        val item1 = InAppMessageUiModel(
            isFirstMessage = true,
            isLastMessage = false,
            chipText = "1.0.0",
            content = "Dies ist eine interne Mitteilung für Ihr E-Rezept.",
            sentOn = "12 Mär 2025 • 09:21"
        )

        val item2 = InAppMessageUiModel(
            isFirstMessage = false,
            isLastMessage = false,
            chipText = "1.0.1",
            content = "Hier stehen weitere Details zu Ihrer Rezeptbearbeitung.",
            sentOn = "13 Mär 2025 • 10:05"
        )

        val item3 = InAppMessageUiModel(
            isFirstMessage = false,
            isLastMessage = true,
            chipText = "1.0.2",
            content = "Hier stehen weitere Details zu Ihrer Rezeptbearbeitung.",
            sentOn = "14 Mär 2025 • 10:05"
        )

        Column(Modifier.padding(SizeDefaults.double)) {
            InAppMessage(item1)
            InAppMessage(item2)
            InAppMessage(item3)
        }
    }
}
