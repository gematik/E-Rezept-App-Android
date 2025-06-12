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

package de.gematik.ti.erp.app.digas.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.ui.preview.DigaStatusPreviewParameterProvider
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme

@Composable
fun ColumnScope.ProcessSection(
    current: DigaStatus,
    code: String,
    declineNote: String?,
    onClickCopy: () -> Unit,
    onRegisterFeedback: () -> Unit
) {
    Text(
        modifier = Modifier.semanticsHeading(),
        text = stringResource(R.string.diga_overview_how_it_works),
        style = AppTheme.typography.subtitle1
    )
    RequestRowItem(current)
    InsuranceRowItem(
        currentProcess = current,
        code = code,
        declineNote = declineNote,
        onClick = onClickCopy,
        onRegisterFeedback = onRegisterFeedback
    )
    DownloadRowItem(current)
    ActivateRowItem(current)
}

@LightDarkPreview
@Composable
private fun ProcessSectionPreview(
    @PreviewParameter(DigaStatusPreviewParameterProvider::class) data: DigaStatus
) {
    PreviewTheme {
        Column {
            ProcessSection(
                current = data,
                code = "1234567890",
                declineNote = null,
                onClickCopy = {},
                onRegisterFeedback = {}
            )
        }
    }
}
