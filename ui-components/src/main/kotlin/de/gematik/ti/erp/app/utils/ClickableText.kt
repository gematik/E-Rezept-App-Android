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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import de.gematik.ti.erp.app.theme.AppTheme

/**
 * A clickable text which allows you to give in the text string and the link-text
 * and it automatically builds the clickable listener on the text
 */
@Composable
fun ClickableText(
    modifier: Modifier = Modifier,
    clickText: ClickText,
    textStyle: TextStyle = TextStyle(color = AppTheme.colors.neutral600),
    linkTextStyle: SpanStyle = SpanStyle(color = AppTheme.colors.primary700, textDecoration = TextDecoration.Underline),
    text: String
) {
    val annotatedString = remember(clickText, text) {
        buildAnnotatedString {
            val clickableText = clickText.text
            val startIndex = text.indexOf(clickableText, ignoreCase = true)
            val endIndex = startIndex + clickableText.length

            if (startIndex != -1) {
                if (startIndex > 0) {
                    withStyle(textStyle.toSpanStyle()) {
                        append(text.substring(0, startIndex))
                    }
                }

                pushStringAnnotation(tag = "clickable_link", annotation = "clickable_link")
                withStyle(linkTextStyle) {
                    append(clickableText)
                }
                pop()

                if (endIndex < text.length) {
                    withStyle(textStyle.toSpanStyle()) {
                        append(text.substring(endIndex))
                    }
                }
            } else {
                withStyle(textStyle.toSpanStyle()) {
                    append(text)
                }
            }
        }
    }

    ClickableText(
        text = annotatedString,
        style = textStyle,
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(
                tag = "clickable_link",
                start = offset,
                end = offset
            ).firstOrNull()?.let {
                clickText.onClick()
            }
        }
    )
}

data class ClickText(
    val text: String,
    val onClick: () -> Unit
)
