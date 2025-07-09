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

package de.gematik.ti.erp.app.profiles.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.profiles.usecase.model.PairedDevice
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

private data class DeleteDeviceDialogParams(
    val title: String,
    val info: AnnotatedString
)

@Composable
fun DeleteDeviceDialog(
    event: ComposableEvent<PairedDevice>,
    dialogScaffold: DialogScaffold,
    onClickAction: (PairedDevice) -> Unit
) {
    event.listen { device ->
        dialogScaffold.show {
            val dialogParams = if (device.isCurrentDevice) {
                DeleteDeviceDialogParams(
                    title = stringResource(R.string.paired_devices_delete_this_title),
                    info = annotatedStringResource(
                        R.string.paired_devices_delete_this_description,
                        annotatedStringBold(device.name)
                    )
                )
            } else {
                DeleteDeviceDialogParams(
                    title = stringResource(R.string.paired_devices_delete_title),
                    info = annotatedStringResource(
                        R.string.paired_devices_delete_description,
                        annotatedStringBold(device.name)
                    )
                )
            }
            DeleteDeviceDialog(
                dialogParams = dialogParams,
                onClickDismiss = { it.dismiss() },
                onClickAction = {
                    onClickAction(device)
                    it.dismiss()
                }
            )
        }
    }
}

@Composable
private fun DeleteDeviceDialog(
    dialogParams: DeleteDeviceDialogParams,
    onClickDismiss: () -> Unit,
    onClickAction: () -> Unit
) {
    ErezeptAlertDialog(
        title = dialogParams.title,
        bodyText = dialogParams.info.text,
        confirmText = stringResource(R.string.paired_devices_delete_remove),
        dismissText = stringResource(R.string.paired_devices_delete_cancel),
        onDismissRequest = onClickDismiss,
        onConfirmRequest = onClickAction
    )
}

@Composable
@LightDarkPreview
fun DeleteDeviceDialogPreview() {
    PreviewAppTheme {
        DeleteDeviceDialog(
            dialogParams = DeleteDeviceDialogParams(
                title = stringResource(R.string.paired_devices_delete_this_title),
                info = annotatedStringResource(
                    R.string.paired_devices_delete_description,
                    annotatedStringBold("Pixel 10")
                )
            ),
            onClickDismiss = {},
            onClickAction = {}
        )
    }
}
