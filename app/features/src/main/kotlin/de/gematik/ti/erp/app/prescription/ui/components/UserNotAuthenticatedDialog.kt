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

package de.gematik.ti.erp.app.prescription.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
fun UserNotAuthenticatedDialog(
    event: ComposableEvent<Unit>,
    dialogScaffold: DialogScaffold,
    onShowCardWall: () -> Unit
) {
    event.listen {
        dialogScaffold.show {
            UserNotAuthenticatedDialog(
                onShowCardWall = {
                    onShowCardWall()
                    it.dismiss()
                },
                onCancel = { it.dismiss() }
            )
        }
    }
}

@Composable
private fun UserNotAuthenticatedDialog(
    onShowCardWall: () -> Unit,
    onCancel: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.user_not_authenticated_dialog_header),
        bodyText = stringResource(R.string.user_not_authenticated_dialog_info),
        confirmText = stringResource(R.string.user_not_authenticated_dialog_connect),
        dismissText = stringResource(R.string.user_not_authenticated_dialog_cancel),
        onDismissRequest = onCancel,
        onConfirmRequest = onShowCardWall
    )
}

@LightDarkPreview
@Composable
fun UserNotAuthenticatedDialogPreview() {
    PreviewAppTheme {
        UserNotAuthenticatedDialog(
            onShowCardWall = {},
            onCancel = {}
        )
    }
}
