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

package de.gematik.ti.erp.app.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme

/**
 * A clickable text which allows you to give in the text string and the link-text
 * and it automatically builds the clickable listener on the text
 */
@Composable
fun ClickableText(
    modifier: Modifier = Modifier,
    leadingText: String? = null,
    trailingText: String? = null,
    linkText: String,
    onClick: () -> Unit,
    textStyle: TextStyle,
    linkTextStyle: SpanStyle = SpanStyle(
        color = AppTheme.colors.primary700,
        textDecoration = TextDecoration.Underline
    )
) {
    val annotatedString = remember(linkText) {
        buildAnnotatedString {
            leadingText?.let {
                append(it)
            }
            withLink(
                LinkAnnotation.Clickable(
                    tag = linkText,
                    styles = TextLinkStyles(
                        style = linkTextStyle
                    )
                ) {
                    onClick()
                }
            ) {
                append(linkText)
            }
            trailingText?.let {
                append(it)
            }
        }
    }

    Text(
        annotatedString,
        style = textStyle,
        modifier = modifier.fillMaxWidth()
    )
}

@LightDarkPreview
@Composable
private fun ClickableTextPreview() {
    PreviewTheme {
        ClickableText(
            leadingText = "Prefix: ",
            trailingText = " :Suffix",
            linkText = "Link",
            onClick = {},
            textStyle = AppTheme.typography.body2l
        )
    }
}
