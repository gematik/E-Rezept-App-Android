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

package de.gematik.ti.erp.app.settings.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
fun OrganDonationRegisterDialog(
    event: ComposableEvent<Unit>,
    dialog: DialogScaffold,
    createIntent: () -> Unit
) {
    event.listen {
        dialog.show {
            OrganDonationRegisterDialog_(
                onDismissRequest = {
                    it.dismiss()
                },
                onConfirmRequest = {
                    createIntent()
                    it.dismiss()
                }
            )
        }
    }
}

@Composable
private fun OrganDonationRegisterDialog_(
    onConfirmRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.organ_donation_dialog_title),
        bodyText = stringResource(R.string.organ_donation_dialog_info),
        confirmText = stringResource(R.string.organ_donation_dialog_confirm),
        dismissText = stringResource(R.string.organ_donation_dialog_dismiss),
        onConfirmRequest = onConfirmRequest,
        onDismissRequest = onDismissRequest
    )
}

@LightDarkPreview
@Composable
fun OrganDonationRegisterDialogPreview() {
    PreviewAppTheme {
        OrganDonationRegisterDialog_(
            onConfirmRequest = {},
            onDismissRequest = {}
        )
    }
}
