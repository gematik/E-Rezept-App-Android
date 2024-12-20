/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.gid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun DomainsNotVerifiedDialog(
    onClickSettingsOpen: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.domain_verifier_dialog_title),
        bodyText = stringResource(R.string.domain_verifier_dialog_body),
        confirmText = stringResource(R.string.domain_verifier_dialog_button),
        dismissText = stringResource(R.string.cancel),
        titleAlignment = ErezeptText.TextAlignment.Center,
        buttonsArrangement = Arrangement.Start,
        onDismissRequest = onDismissRequest,
        onConfirmRequest = onClickSettingsOpen
    )
}

@LightDarkPreview
@Composable
fun DomainsNotVerifiedDialogPreview() {
    PreviewAppTheme {
        DomainsNotVerifiedDialog(
            onClickSettingsOpen = {},
            onDismissRequest = {}
        )
    }
}
