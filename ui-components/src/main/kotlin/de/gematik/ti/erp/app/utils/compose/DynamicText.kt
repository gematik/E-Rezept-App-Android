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

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import de.gematik.ti.erp.app.theme.AppTheme

/**
 * Measures all inlined composables upfront and applies the size to the actual placeable of the [Text].
 */
@Composable
fun DynamicText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    inlineContent: Map<String, InlineTextContent>,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    CompositionLocalProvider(LocalTextStyle provides AppTheme.typography.subtitle1) {
        SubcomposeLayout(modifier = Modifier.wrapContentSize()) { constraints ->
            val contentPlaceables = inlineContent.mapValues { (key, content) ->
                val maxSize = subcompose(key, content = { content.children(key) }).map {
                    it.measure(constraints)
                }.fold(IntSize.Zero) { acc, p ->
                    IntSize(
                        width = maxOf(acc.width, p.width),
                        height = maxOf(acc.height, p.height)
                    )
                }

                Pair(content, maxSize)
            }

            val main = subcompose(1) {
                Text(
                    text = text,
                    modifier = modifier,
                    color = color,
                    fontSize = fontSize,
                    fontStyle = fontStyle,
                    fontWeight = fontWeight,
                    fontFamily = fontFamily,
                    letterSpacing = letterSpacing,
                    textDecoration = textDecoration,
                    textAlign = textAlign,
                    lineHeight = lineHeight,
                    overflow = overflow,
                    softWrap = softWrap,
                    maxLines = maxLines,
                    inlineContent = with(LocalDensity.current) {
                        contentPlaceables.mapValues { (_, value) ->
                            val (content, maxSize) = value

                            InlineTextContent(
                                placeholder = content.placeholder.copy(
                                    width = maxSize.width.toSp(),
                                    height = maxSize.height.toSp()
                                ),
                                children = content.children
                            )
                        }
                    },
                    onTextLayout = onTextLayout,
                    style = style
                )
            }.first().measure(constraints)

            layout(main.measuredWidth, main.measuredHeight) {
                main.placeRelative(0, 0)
            }
        }
    }
}
