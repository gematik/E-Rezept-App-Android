/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.ErezeptText.ErezeptTextAlignment.Center
import de.gematik.ti.erp.app.utils.compose.ErezeptText.ErezeptTextAlignment.Default

internal object ErezeptText {

    enum class ErezeptTextAlignment {
        Center, Default
    }

    @Composable
    internal fun Title(
        text: String,
        modifier: Modifier = Modifier,
        textAlignment: ErezeptTextAlignment = Default
    ) {
        when (textAlignment) {
            Center -> CenteredTitle(text, modifier)
            Default -> UnCenteredTitle(text, modifier)
        }
    }

    @Composable
    private fun UnCenteredTitle(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            modifier = modifier,
            text = text,
            style = AppTheme.typography.h6
        )
    }

    @Composable
    private fun CenteredTitle(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Title(text, modifier)
        }
    }

    @Composable
    internal fun Subtitle(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            modifier = modifier,
            text = text,
            style = AppTheme.typography.subtitle2
        )
    }

    @Composable
    internal fun Body(
        text: String,
        modifier: Modifier = Modifier,
        maxLines: Int = Int.MAX_VALUE
    ) {
        Text(
            modifier = modifier,
            text = text,
            maxLines = maxLines,
            style = AppTheme.typography.body2
        )
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
internal fun SubtitlePreview() {
    PreviewAppTheme {
        ErezeptText.Subtitle(text = "Subtitle text")
    }
}

@LightDarkPreview
@Composable
internal fun BodyPreview() {
    PreviewAppTheme {
        ErezeptText.Body(text = "Body text")
    }
}
