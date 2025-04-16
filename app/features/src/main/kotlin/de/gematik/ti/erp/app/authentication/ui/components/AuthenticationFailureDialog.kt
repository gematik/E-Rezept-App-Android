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

package de.gematik.ti.erp.app.authentication.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.authentication.mapper.toDialogMapper
import de.gematik.ti.erp.app.authentication.model.AuthenticationDialogParameter
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import io.github.aakira.napier.Napier

@Composable
fun AuthenticationFailureDialog(
    event: ComposableEvent<AuthenticationResult.Error>,
    dialogScaffold: DialogScaffold
) {
    event.listen { error ->
        error.toDialogMapper()?.let { errorParams ->
            dialogScaffold.show {
                AuthenticationFailureDialog(
                    error = errorParams
                ) {
                    it.dismiss()
                }
            }
        } ?: run {
            Napier.i { "No dialog parameters found for error: $error" }
        }
    }
}

@Composable
private fun AuthenticationFailureDialog(
    error: AuthenticationDialogParameter,
    onClickAction: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(error.title),
        body = stringResource(error.message),
        onDismissRequest = onClickAction
    )
}
