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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.em
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.messages.mappers.ReplyMessageType
import de.gematik.ti.erp.app.messages.ui.model.ReplyMessageUiModel
import de.gematik.ti.erp.app.messages.ui.preview.MessagePreviewMocks.MOCK_PRESCRIPTION_01
import de.gematik.ti.erp.app.messages.ui.preview.MessagePreviewMocks.MOCK_PRESCRIPTION_02
import de.gematik.ti.erp.app.messages.ui.preview.MessagePreviewMocks.MOCK_PRESCRIPTION_03
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.DynamicText

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReplyMessage(
    item: ReplyMessageUiModel,
    isTranslationsAllowed: Boolean,
    showTranslationFeature: Boolean,
    isTranslationInProgress: Boolean,
    onClickTranslation: (String) -> Unit,
    onClick: () -> Unit
) {
    MessageTimeline(
        drawFilledTop = !item.isFirstMessage,
        drawFilledBottom = !item.isLastMessage,
        isClickable = item.isEnabled,
        onClick = onClick,
        timestamp = {
            Text(
                text = item.sentOn,
                style = AppTheme.typography.subtitle2
            )
        },
        content = {
            FlowRow(
                modifier = Modifier.padding(
                    top = PaddingDefaults.Small,
                    bottom = PaddingDefaults.Tiny
                )
            ) {
                if (item.showInfoChip) {
                    InfoChip(stringResource(R.string.all_prescriptions_of_order))
                } else {
                    item.prescriptionsLinked.forEach { pres ->
                        pres?.name?.let { InfoChip(it) }
                    }
                }
            }

            Text(
                text = item.description.second,
                style = AppTheme.typography.body2
            )

            item.title?.let {
                SpacerTiny()
                AnnotatedInfoText(info = it)
            }

            if (item.description.first == ReplyMessageType.Text &&
                showTranslationFeature
            ) {
                TranslateChip(
                    isTranslationsAllowed,
                    isTranslationInProgress
                ) {
                    onClickTranslation(item.description.second)
                }
            }
        }
    )
}

@Composable
private fun AnnotatedInfoText(info: String) {
    val annotatedText = buildAnnotatedString {
        append(info)
        append(" ")
        appendInlineContent("button", "button")
    }

    val inlineContent = mapOf(
        "button" to InlineTextContent(
            Placeholder(
                width = 0.em,
                height = 0.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = AppTheme.colors.primary700
            )
        }
    )

    DynamicText(
        text = annotatedText,
        style = AppTheme.typography.body2,
        color = AppTheme.colors.primary700,
        inlineContent = inlineContent
    )
}

@LightDarkPreview
@Composable
fun ReplyMessagePreview() {
    PreviewTheme {
        Column(modifier = Modifier.padding(SizeDefaults.double)) {
            val previewModel = ReplyMessageUiModel(
                title = "Mehr Details",
                description = ReplyMessageType.Text to "Ihre Apotheke hat neue Informationen gesendet.",
                sentOn = "23 Feb 2025 • 14:35",
                showInfoChip = false,
                isEnabled = true,
                isFirstMessage = true,
                isLastMessage = false,
                prescriptionsLinked = listOf(
                    MOCK_PRESCRIPTION_01,
                    MOCK_PRESCRIPTION_02
                )
            )

            val previewModel2 = ReplyMessageUiModel(
                title = "Mehr Details",
                description = ReplyMessageType.Text to "Ihre Apotheke hat neue Informationen gesendet.",
                sentOn = "21 Feb 2025 • 14:35",
                showInfoChip = false,
                isEnabled = true,
                isFirstMessage = false,
                isLastMessage = false,
                prescriptionsLinked = listOf(
                    MOCK_PRESCRIPTION_03
                )
            )

            ReplyMessage(
                item = previewModel,
                isTranslationsAllowed = true,
                showTranslationFeature = true,
                isTranslationInProgress = false,
                onClickTranslation = {},
                onClick = {}
            )

            ReplyMessage(
                item = previewModel2,
                isTranslationsAllowed = false,
                showTranslationFeature = false,
                isTranslationInProgress = false,
                onClickTranslation = {},
                onClick = {}
            )
        }
    }
}
