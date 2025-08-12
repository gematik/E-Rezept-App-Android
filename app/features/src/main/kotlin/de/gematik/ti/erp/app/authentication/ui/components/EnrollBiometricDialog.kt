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

package de.gematik.ti.erp.app.authentication.ui.components

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.authentication.presentation.enrollBiometricsIntent
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
fun EnrollBiometricDialog(
    event: ComposableEvent<Unit>,
    context: Context,
    dialog: DialogScaffold,
    title: String = stringResource(R.string.enroll_biometric_dialog_header),
    body: String = stringResource(R.string.enroll_biometric_dialog_info)
) {
    event.listen {
        dialog.show {
            EnrollBiometricDialog(
                context = context,
                title = title,
                body = body,
                onDismiss = { it.dismiss() }
            )
        }
    }
}

@Composable
private fun EnrollBiometricDialog(
    context: Context,
    title: String,
    body: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    ErezeptAlertDialog(
        title = title,
        titleIcon = Icons.Rounded.Fingerprint,
        bodyText = body,
        confirmText = stringResource(R.string.enroll_biometric_dialog_settings),
        dismissText = stringResource(R.string.enroll_biometric_dialog_cancel),
        onConfirmRequest = {
            context.startActivity(enrollBiometricsIntent())
            onDismiss()
        },
        onDismissRequest = onDismiss
    )
}
