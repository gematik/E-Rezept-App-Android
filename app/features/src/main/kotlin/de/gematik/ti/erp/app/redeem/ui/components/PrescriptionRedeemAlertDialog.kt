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

package de.gematik.ti.erp.app.redeem.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.redeem.model.RedeemPrescriptionDialogMessageState
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemPrescriptionDialogParameter
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
fun PrescriptionRedeemAlertDialog(
    event: ComposableEvent<RedeemPrescriptionDialogMessageState>,
    dialog: DialogScaffold,
    onDismiss: () -> Unit
) {
    event.listen { dialogParams ->
        dialog.show {
            PrescriptionRedeemAlertDialog(
                state = dialogParams,
                onDismiss = {
                    it.dismiss()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun PrescriptionRedeemAlertDialog(
    state: RedeemPrescriptionDialogMessageState,
    onDismiss: () -> Unit
) {
    AcceptDialog(
        header = stringResource(state.title),
        info = stringResource(state.description),
        onClickAccept = {
            onDismiss()
        },
        acceptText = stringResource(R.string.pharmacy_search_apovz_call_failed_accept)
    )
}

@LightDarkPreview
@Composable
internal fun PrescriptionRedeemAlertDialogPreview(
    @PreviewParameter(RedeemPrescriptionDialogParameter::class) state: RedeemPrescriptionDialogMessageState
) {
    PreviewTheme {
        PrescriptionRedeemAlertDialog(
            state = state,
            onDismiss = {}
        )
    }
}
