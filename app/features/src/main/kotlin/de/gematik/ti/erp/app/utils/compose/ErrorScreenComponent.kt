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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun ErrorScreenComponent(
    modifier: Modifier = Modifier,
    noMaxSize: Boolean = false,
    title: String = stringResource(R.string.generic_error_title),
    body: String = stringResource(R.string.generic_error_info),
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
                text = title,
                style = AppTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )
            Text(
                text = body,
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center
            )
            // show retry button if onClickRetry is not null
            onClickRetry?.let {
                TextButton(
                    onClick = onClickRetry
                ) {
                    Icon(Icons.Rounded.Refresh, null)
                    SpacerSmall()
                    Text(stringResource(R.string.cdw_fasttrack_try_again))
                }
            } ?: run {
                SpacerMedium()
            }
        }
    }

@Composable
@LightDarkPreview
fun ErrorScreenComponentPreview() {
    PreviewAppTheme {
        ErrorScreenComponent(
            onClickRetry = {}
        )
    }
}

@Composable
@LightDarkPreview
fun ErrorScreenComponentPreviewNoRetry() {
    PreviewAppTheme {
        ErrorScreenComponent()
    }
}
