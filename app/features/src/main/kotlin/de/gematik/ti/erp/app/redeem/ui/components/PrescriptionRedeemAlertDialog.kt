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

package de.gematik.ti.erp.app.redeem.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.redeem.model.RedeemDialogParameters
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
fun PrescriptionRedeemAlertDialog(
    event: ComposableEvent<RedeemDialogParameters>,
    dialog: DialogScaffold,
    onDismiss: () -> Unit
) {
    event.listen { dialogParams ->
        dialog.show {
            PrescriptionRedeemAlertDialog(
                title = stringResource(dialogParams.title),
                description = stringResource(dialogParams.description),
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
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    AcceptDialog(
        header = title,
        info = description,
        onClickAccept = {
            onDismiss()
        },
        acceptText = stringResource(R.string.pharmacy_search_apovz_call_failed_accept)
    )
}
