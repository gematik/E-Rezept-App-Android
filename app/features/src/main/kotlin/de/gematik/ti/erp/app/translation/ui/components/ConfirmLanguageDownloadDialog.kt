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

package de.gematik.ti.erp.app.translation.ui.components

import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
internal fun ConfirmLanguageDownloadDialog(
    event: ComposableEvent<String>,
    onConfirmRequest: (String) -> Unit,
    onDismissRequest: () -> Unit,
    dialogScaffold: DialogScaffold
) {
    event.listen {
        dialogScaffold.show { dialog ->
            ConfirmLanguageDownloadDialog(
                onConfirmRequest = {
                    onConfirmRequest(it)
                    dialog.dismiss()
                },
                onDismissRequest = {
                    onDismissRequest()
                    dialog.dismiss()
                }
            )
        }
    }
}

@Composable
internal fun ConfirmLanguageDownloadDialog(
    onConfirmRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ErezeptAlertDialog(
        title = "Download Language Model",
        bodyText = "Are you sure you want to download this language model?",
        confirmText = "Download",
        dismissText = "Cancel",
        onConfirmRequest = onConfirmRequest,
        onDismissRequest = onDismissRequest
    )
}
