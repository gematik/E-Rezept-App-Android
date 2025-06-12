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

package de.gematik.ti.erp.app.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.letNotNull

@Composable
fun ErrorScreenComponent(
    modifier: Modifier = Modifier,
    noMaxSize: Boolean = false,
    titleText: String,
    bodyText: String,
    tryAgainText: String? = null,
    onClickRetry: (() -> Unit)? = null
) =
    Box(
        modifier = modifier
            .then(
                if (noMaxSize) Modifier else Modifier.fillMaxSize()
            )
            .padding(PaddingDefaults.Medium),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            Text(
                text = titleText,
                style = AppTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )
            Text(
                text = bodyText,
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center
            )
            // show retry button if onClickRetry is not null
            letNotNull(
                onClickRetry,
                tryAgainText
            ) { onClick, text ->
                TextButton(
                    onClick = onClick
                ) {
                    Icon(Icons.Rounded.Refresh, null)
                    SpacerSmall()
                    Text(text)
                }
            } ?: run {
                SpacerMedium()
            }
        }
    }

@Composable
@LightDarkPreview
fun ErrorScreenComponentPreview() {
    PreviewTheme {
        ErrorScreenComponent(
            titleText = "title to show something",
            bodyText = "body stating that the title is not enough",
            tryAgainText = "retry",
            onClickRetry = {}
        )
    }
}

@Composable
@LightDarkPreview
fun ErrorScreenComponentPreviewNoRetry() {
    PreviewTheme {
        ErrorScreenComponent(
            titleText = "title to show something",
            bodyText = "body stating that the title is not enough"
        )
    }
}
