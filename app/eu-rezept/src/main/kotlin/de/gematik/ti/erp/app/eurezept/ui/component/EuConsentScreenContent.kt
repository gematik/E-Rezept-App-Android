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

package de.gematik.ti.erp.app.eurezept.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
fun EuConsentScreenContent(
    paddingValues: PaddingValues,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
        contentPadding = PaddingValues(
            start = PaddingDefaults.Medium,
            end = PaddingDefaults.Medium,
            top = PaddingDefaults.Medium,
            bottom = paddingValues.calculateBottomPadding()
        )
    ) {
        item {
            Text(
                text = stringResource(R.string.eu_consent_title),
                style = AppTheme.typography.h5,
                modifier = Modifier.semanticsHeading()

            )
        }

        item {
            Text(
                text = stringResource(R.string.eu_consent_description),
                style = AppTheme.typography.body1
            )
        }

        item {
            Text(
                text = stringResource(R.string.eu_consent_withdrawal),
                style = AppTheme.typography.body1
            )
        }
    }
}

@LightDarkPreview
@Composable
fun EuConsentScreenContentPreview() {
    val listState = rememberLazyListState()
    val paddingValues = PaddingValues(bottom = PaddingDefaults.Medium)
    PreviewTheme {
        EuConsentScreenContent(
            paddingValues = paddingValues,
            listState = listState
        )
    }
}
