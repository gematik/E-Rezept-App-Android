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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import de.gematik.ti.erp.app.messages.ui.model.InAppMessageUiModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
internal fun LocalMessage(
    item: InAppMessageUiModel
) {
    // State for dynamically tracking the circle position
    val circleYPositionState = remember { mutableFloatStateOf(0f) }

    Row(
        Modifier
            .drawConnectedLine(
                drawFilledTop = !item.isFirstMessage,
                drawFilledBottom = !item.isLastMessage,
                circleYPosition = { circleYPositionState.floatValue } // Use a lambda for dynamic value
            )
    ) {
        Spacer(Modifier.width(SizeDefaults.triple))
        Column(
            Modifier
                .weight(1f)
                .padding(PaddingDefaults.Medium)
        ) {
            SpacerMedium()
            Row(
                modifier = Modifier.fillMaxWidth()
                    .calculateVerticalCenter(
                        onCenterCalculated = { circleYPositionState.floatValue = it }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.sentOn,
                    style = AppTheme.typography.subtitle2
                )
            }
            SpacerTiny()
            if (item.chipText?.isNotEmpty() == true) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(AppTheme.colors.primary100)
                        .padding(horizontal = PaddingDefaults.Small, vertical = SizeDefaults.threeSeventyFifth),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.chipText,
                        style = AppTheme.typography.caption2
                    )
                }
            }
            item.content?.let {
                SpacerTiny()
                MarkdownText(
                    markdown = it,
                    style = AppTheme.typography.body2.copy(
                        color = AppTheme.colors.neutral900
                    ),
                    linkColor = AppTheme.colors.primary700
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
fun LocalMessagePreview() {
    PreviewAppTheme {
        LocalMessage(
            item = InAppMessageUiModel(
                isFirstMessage = true,
                isLastMessage = true,
                chipText = "Lorem ipsum",
                content = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna",
                sentOn = "23 Feb 2023"
            )
        )
    }
}
