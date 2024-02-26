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

package de.gematik.ti.erp.app.gid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme

@Composable
fun DomainsNotVerifiedDialog(
    onClickSettingsOpen: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ErezeptAlertDialog(
        modifier = Modifier.width(600.dp),
        title = stringResource(R.string.domain_verifier_dialog_title),
        body = stringResource(R.string.domain_verifier_dialog_body),
        confirmText = stringResource(R.string.domain_verifier_dialog_button),
        dismissText = stringResource(R.string.cancel),
        titleAlignment = ErezeptText.ErezeptTextAlignment.Center,
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
