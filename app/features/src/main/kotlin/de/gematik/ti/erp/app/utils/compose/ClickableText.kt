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

package de.gematik.ti.erp.app.utils.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import de.gematik.ti.erp.app.theme.AppTheme

/**
 * A clickable text which allows you to give in the text string and the link-text
 * and it automatically builds the clickable listener on the text
 */
@Composable
fun ClickableText(
    modifier: Modifier = Modifier,
    @StringRes textWithPlaceholdersRes: Int,
    clickText: ClickText,
    textStyle: TextStyle,
    linkTextStyle: SpanStyle = SpanStyle(color = AppTheme.colors.primary700),
    text: String = stringResource(id = textWithPlaceholdersRes)
) {
    data class TextData(
        val text: String,
        val tag: String? = null,
        val onClick: (() -> Unit)? = null
    )

    val resources = LocalContext.current.resources
    val textData = remember(clickText, text, resources) {
        val spacingDelimiter = " "
        val regex = Regex(clickText.text.lowercase())
        val splits = text
            .split(spacingDelimiter)
            .joinToString(spacingDelimiter)
            .splitToSequence(regex)
            .toMutableList()
        // add the delimiter also to the list
        splits.add(1, clickText.text)
        mutableListOf<TextData>().apply {
            splits.forEach { part ->
                if (part == clickText.text) {
                    add(
                        TextData(
                            text = " ${clickText.text} ",
                            tag = "$clickText-link-text-tag",
                            onClick = { clickText.onClick() }
                        )
                    )
                } else {
                    add(TextData(text = part))
                }
            }
        }
    }
    val annotatedString = remember(textData) {
        buildAnnotatedString {
            textData.forEach { data ->
                if (data.tag != null) {
                    pushStringAnnotation(
                        tag = data.tag,
                        annotation = "link-text"
                    )
                    withStyle(style = linkTextStyle) {
                        append(data.text)
                    }
                    pop()
                } else {
                    withStyle(style = textStyle.toSpanStyle()) {
                        append(data.text)
                    }
                }
            }
        }
    }
    ClickableText(
        modifier = modifier,
        text = annotatedString,
        style = textStyle,
        onClick = { offset ->
            textData.forEach { annotatedStringData ->
                if (annotatedStringData.tag != null) {
                    annotatedString.getStringAnnotations(
                        tag = annotatedStringData.tag,
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        annotatedStringData.onClick?.invoke()
                    }
                }
            }
        }
    )
}

data class ClickText(
    val text: String,
    val onClick: () -> Unit
)
