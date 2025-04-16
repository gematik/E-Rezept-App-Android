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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.ErezeptText.TextAlignment.Center
import de.gematik.ti.erp.app.utils.compose.ErezeptText.TextAlignment.Default
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

object ErezeptText {

    enum class TextAlignment {
        Center, Default
    }

    sealed interface HeaderStyle {
        data object H1 : HeaderStyle
        data object H2 : HeaderStyle
        data object H3 : HeaderStyle
        data object H4 : HeaderStyle
        data object H5 : HeaderStyle
        data object H6 : HeaderStyle

        @Composable
        fun textStyle(): TextStyle = when (this) {
            is H1 -> AppTheme.typography.h1
            is H2 -> AppTheme.typography.h2
            is H3 -> AppTheme.typography.h3
            is H4 -> AppTheme.typography.h4
            is H5 -> AppTheme.typography.h5
            is H6 -> AppTheme.typography.h6
        }
    }

    enum class BodyStyle {
        Body1, Body2, Body1l, Body2l;

        companion object {
            @Composable
            fun select(style: BodyStyle): TextStyle = when (style) {
                Body1 -> AppTheme.typography.body1
                Body2 -> AppTheme.typography.body2
                Body1l -> AppTheme.typography.body1l
                Body2l -> AppTheme.typography.body2l
            }
        }
    }

    @Composable
    internal fun Title(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified,
        style: HeaderStyle = HeaderStyle.H6,
        textAlignment: TextAlignment = Default
    ) {
        when (textAlignment) {
            Center -> CenteredTitle(text, modifier, color, style.textStyle())
            Default -> UnCenteredTitle(text, modifier, color, style.textStyle())
        }
    }

    @Composable
    private fun UnCenteredTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified,
        style: TextStyle
    ) {
        Text(
            modifier = modifier,
            text = text,
            color = color,
            style = style
        )
    }

    @Composable
    private fun CenteredTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified,
        style: TextStyle
    ) {
        Center {
            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = style
            )
        }
    }

    @Composable
    internal fun SubtitleOne(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified
    ) {
        Text(
            modifier = modifier,
            text = text,
            color = color,
            style = AppTheme.typography.subtitle1
        )
    }

    @Composable
    internal fun SubtitleTwo(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified
    ) {
        Text(
            modifier = modifier,
            text = text,
            color = color,
            style = AppTheme.typography.subtitle2
        )
    }

    @Composable
    internal fun Body(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Ellipsis,
        textAlignment: TextAlignment = Default,
        style: BodyStyle = BodyStyle.Body2
    ) {
        when (textAlignment) {
            Center -> CenteredBody(text, modifier, color, maxLines, overflow, style)
            Default -> UnCenteredBody(text, modifier, color, maxLines, overflow, style)
        }
    }

    @Composable
    private fun UnCenteredBody(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Ellipsis,
        style: BodyStyle = BodyStyle.Body2
    ) {
        Text(
            modifier = modifier,
            text = text,
            color = color,
            maxLines = maxLines,
            style = BodyStyle.select(style),
            overflow = overflow
        )
    }

    @Composable
    private fun CenteredBody(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Ellipsis,
        style: BodyStyle = BodyStyle.Body2
    ) {
        Center {
            Text(
                modifier = modifier,
                text = text,
                color = color,
                maxLines = maxLines,
                style = BodyStyle.select(style),
                overflow = overflow,
                textAlign = TextAlign.Center
            )
        }
    }
}

@LightDarkPreview
@Composable
internal fun TitlePreview() {
    PreviewAppTheme {
        Column {
            ErezeptText.Title(
                textAlignment = Center,
                text = "Center Title text"
            )
            ErezeptText.Title(
                textAlignment = Default,
                text = "Default Title text"
            )
            ErezeptText.SubtitleTwo(text = "Subtitle text")
            ErezeptText.Body(text = "Body text")
        }
    }
}

@LightDarkPreview
@Composable
internal fun TitleCenterPreview() {
    PreviewAppTheme {
        ErezeptText.Title(
            textAlignment = Center,
            text = "Title text"
        )
    }
}

@LightDarkPreview
@Composable
internal fun TitleDefaultPreview() {
    PreviewAppTheme {
        ErezeptText.Title(
            textAlignment = Default,
            text = "Title text"
        )
    }
}

@LightDarkPreview
@Composable
internal fun SubtitleOnePreview() {
    PreviewAppTheme {
        ErezeptText.SubtitleOne(text = "Subtitle One text")
    }
}

@LightDarkPreview
@Composable
internal fun SubtitleTwoPreview() {
    PreviewAppTheme {
        ErezeptText.SubtitleTwo(text = "Subtitle Two text")
    }
}

@LightDarkPreview
@Composable
internal fun BodyPreview() {
    PreviewAppTheme {
        ErezeptText.Body(text = "Body text")
    }
}
