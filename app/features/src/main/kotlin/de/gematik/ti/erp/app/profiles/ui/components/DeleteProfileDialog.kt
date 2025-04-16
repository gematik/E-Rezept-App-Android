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

package de.gematik.ti.erp.app.profiles.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
internal fun DeleteProfileDialog(
    event: ComposableEvent<Unit>,
    dialogScaffold: DialogScaffold,
    onClickAction: () -> Unit
) {
    event.listen {
        dialogScaffold.show { dialog ->
            ErezeptAlertDialog(
                title = stringResource(id = R.string.remove_profile_header),
                bodyText = stringResource(R.string.remove_profile_detail_message),
                confirmText = stringResource(R.string.remove_profile_yes),
                dismissText = stringResource(R.string.remove_profile_no),
                onConfirmRequest = {
                    onClickAction()
                    dialog.dismiss()
                },
                onDismissRequest = { dialog.dismiss() }
            )
        }
    }
}
