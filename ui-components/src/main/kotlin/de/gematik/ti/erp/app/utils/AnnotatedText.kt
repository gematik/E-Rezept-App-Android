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

@file:Suppress("TooManyFunctions", "MagicNumber")

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import de.gematik.ti.erp.app.theme.AppTheme

@Composable
fun annotatedLinkUnderlined(fullText: String, clickableText: String, tag: String, textColor: Color? = null): AnnotatedString {
    val startIndex = fullText.indexOf(clickableText)
    val endIndex = startIndex + clickableText.length

    return buildAnnotatedString {
        append(fullText)
        addStyle(
            style = SpanStyle(color = textColor ?: AppTheme.colors.primary700, textDecoration = TextDecoration.Underline),
            start = startIndex,
            end = endIndex
        )
        addStringAnnotation(
            tag = tag,
            start = startIndex,
            end = endIndex,
            annotation = clickableText
        )
    }
}
