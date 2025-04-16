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

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DateTimeUtils
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun InAppMessage(
    message: OrderUseCaseData.Message,
    isFirstMessage: Boolean,
    isLastMessage: Boolean,
    dateFormatter: DateTimeFormatter = DateTimeUtils.dateFormatter,
    timeFormatter: DateTimeFormatter = DateTimeUtils.timeFormatter
) {
    val localDateTime = remember(message) {
        message.sentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
    }

    // State for dynamically tracking the circle position
    val circleYPositionState = remember { mutableFloatStateOf(0f) }

    val date = remember(localDateTime) {
        dateFormatter.format(localDateTime)
    }

    val time = remember(localDateTime) {
        timeFormatter.format(localDateTime)
    }

    Row(
        Modifier
            .drawConnectedLine(
                drawFilledTop = !isFirstMessage,
                drawFilledBottom = !isLastMessage,
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
            Text(
                stringResource(R.string.orders_timestamp, date, time),
                style = AppTheme.typography.subtitle2,
                modifier = Modifier.calculateVerticalCenter(
                    onCenterCalculated = { circleYPositionState.floatValue = it }
                )
            )
            SpacerTiny()
            if (message.additionalInfo.isNotEmpty()) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(AppTheme.colors.primary100)
                        .padding(horizontal = PaddingDefaults.Small, vertical = SizeDefaults.threeSeventyFifth),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message.additionalInfo,
                        style = AppTheme.typography.caption2
                    )
                }
            }
            message.content?.let {
                SpacerTiny()
                MarkdownText(
                    markdown = it,
                    style = AppTheme.typography.body2l,
                    linkColor = AppTheme.colors.primary700
                )
            }
        }
    }
}

@Preview
@Composable
fun InAppMessagePreview() {
    PreviewAppTheme {
        InAppMessage(
            message = OrderUseCaseData.Message(
                communicationId = "1",
                sentOn = Instant.parse("2022-01-01T00:00:00Z"),
                content = """
                    Lorem ipsum dolor sit amet, consetetur sadipscing elitr, 
                    sed diam nonumy eirmod tempor invidunt ut labore et dolore 
                    magna aliquyam erat, sed diam voluptua. At vero eos et accusam 
                    et justo duo dolores et ea rebum. Stet clita kasd gubergren, 
                    no sea takimata sanctus est Lorem ipsum dolor sit amet. 
                    Lorem ipsum dolor sit amet, consetetur sadipscing elitr, 
                    sed diam nonumy eirmod tempor invidunt ut labore et dolore 
                    magna aliquyam erat, sed diam voluptua. At vero eos et 
                    accusam et justo duo dolores et ea rebum. Stet clita kasd 
                    gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.
                """.trimIndent(),
                additionalInfo = "",
                pickUpCodeDMC = "",
                pickUpCodeHR = "",
                link = "",
                consumed = false,
                prescriptions = emptyList(),
                taskIds = emptyList(),
                isTaskIdCountMatching = false
            ),
            isFirstMessage = true,
            isLastMessage = true
        )
    }
}
