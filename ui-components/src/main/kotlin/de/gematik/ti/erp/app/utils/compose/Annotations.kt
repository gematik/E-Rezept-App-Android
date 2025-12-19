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

import android.content.res.Resources
import android.util.Patterns
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.theme.AppTheme

@Suppress("SpreadOperator")
@Composable
fun annotatedStringResource(@StringRes id: Int, vararg args: Any): AnnotatedString =
    annotatedStringResource(id, *(args.map { AnnotatedString(it.toString()) }.toTypedArray()))

@Composable
fun annotatedStringResource(@StringRes id: Int, vararg args: AnnotatedString): AnnotatedString =
    buildAnnotatedString {
        val res = stringResource(id)
        appendSubStrings(args, res)
    }

@Composable
fun annotatedPluralsResource(
    @PluralsRes id: Int,
    quantity: Int,
    vararg args: AnnotatedString
): AnnotatedString =
    buildAnnotatedString {
        val res = resources().getQuantityString(id, quantity)

        appendSubStrings(args, res)
    }

fun String.toAnnotatedString() =
    buildAnnotatedString { append(this@toAnnotatedString) }

@Composable
fun annotatedLinkString(uri: String, text: String, tag: String = "URL"): AnnotatedString =
    buildAnnotatedString {
        pushStringAnnotation(tag, uri)
        pushStyle(AppTheme.typography.subtitle2.toSpanStyle())
        pushStyle(SpanStyle(color = AppTheme.colors.primary700))
        append(text)
        pop()
        pop()
        pop()
    }

@Composable
fun annotatedLinkStringLight(uri: String, text: String, tag: String = "URL"): AnnotatedString =
    buildAnnotatedString {
        pushStringAnnotation(tag, uri)
        pushStyle(SpanStyle(color = AppTheme.colors.primary700))
        append(text)
        pop()
        pop()
    }

@Composable
fun annotatedStringBold(text: String) =
    buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text)
        }
    }

@Composable
fun createPhoneNumberAnnotations(
    text: String,
    textColor: Color = LocalContentColor.current,
    phoneNumberColor: Color = AppTheme.colors.primary600,
    tag: String = TestTag.Orders.Messages.PhoneNumber
): AnnotatedString = remember(text, textColor, phoneNumberColor) {
    buildAnnotatedString {
        val matcher = Patterns.PHONE.matcher(text)
        var lastIndex = 0

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                withStyle(SpanStyle(color = textColor)) {
                    append(text.substring(lastIndex, matcher.start()))
                }
            }

            val phoneNumber = matcher.group()
            pushStringAnnotation(tag, phoneNumber)
            withStyle(
                style = SpanStyle(
                    color = phoneNumberColor,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(phoneNumber)
            }
            pop()

            lastIndex = matcher.end()
        }

        if (lastIndex < text.length) {
            withStyle(SpanStyle(color = textColor)) {
                append(text.substring(lastIndex))
            }
        }
    }
}

private fun AnnotatedString.Builder.appendSubStrings(
    args: Array<out AnnotatedString>,
    res: String
) {
    val argIt = args.iterator()
    var i = 0
    while (i <= res.length) {
        val j = res.indexOf("%s", i)
        if (j != -1) {
            append(res.substring(i, j))
            if (argIt.hasNext()) {
                append(argIt.next())
            }
            i = j + 2
        } else {
            append(res.substring(i, res.length))
            break
        }
    }
}

@Composable
private fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}
