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

package de.gematik.ti.erp.app.digas.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.ui.model.RedeemDialogEvent
import de.gematik.ti.erp.app.digas.ui.model.RedeemDialogEvent.Companion.toDialogEvent
import de.gematik.ti.erp.app.digas.ui.model.RedeemEvent
import de.gematik.ti.erp.app.error.code.DigaErrorCode
import de.gematik.ti.erp.app.error.diga.DigaErrorCodeMapper
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
internal fun DigaRedeemDialog(
    dialogScaffold: DialogScaffold,
    event: ComposableEvent<RedeemEvent>,
    onTryAgainRequest: () -> Unit
) {
    // Show the dialog when the event is trigger
    event.listen { redeemEvent ->
        dialogScaffold.show { dialog ->
            when (val dialogEvent = redeemEvent.toDialogEvent()) {
                RedeemDialogEvent.DirectoryError -> {
                    DirectoryErrorOnDigaRedeemDialog {
                        dialog.dismiss()
                    }
                }

                is RedeemDialogEvent.GenericError -> {
                    GenericErrorOnDigaRedeemDialog(
                        errorCode = dialogEvent.errorCode,
                        onConfirmRequest = {
                            onTryAgainRequest()
                            dialog.dismiss()
                        },
                        onDismissRequest = {
                            dialog.dismiss()
                        }
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun DirectoryErrorOnDigaRedeemDialog(
    onDismissRequest: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.diga_directory_missing_dialog_title),
        body = stringResource(R.string.diga_directory_missing_dialog_description),
        onDismissRequest = onDismissRequest
    )
}

@Composable
private fun GenericErrorOnDigaRedeemDialog(
    errorCode: String,
    onConfirmRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val bodyText = stringResource(R.string.diga_error_dialog_description)
    ErezeptAlertDialog(
        title = stringResource(R.string.diga_error_dialog_title),
        bodyText = """
           $bodyText 
           (ErrorCode: $errorCode)               
        """.trimIndent(),
        confirmText = stringResource(R.string.cdw_auth_retry),
        dismissText = stringResource(R.string.cancel),
        buttonsArrangement = Arrangement.spacedBy(SizeDefaults.one),
        onConfirmRequest = onConfirmRequest,
        onDismissRequest = onDismissRequest
    )
}

@LightDarkPreview
@Composable
internal fun GenericErrorOnDigaRedeemDialogPreview() {
    PreviewTheme {
        GenericErrorOnDigaRedeemDialog(
            onDismissRequest = {},
            onConfirmRequest = {},
            errorCode = DigaErrorCodeMapper.buildErrorCode(DigaErrorCode.ALREADY_REDEEMED_ERROR)
        )
    }
}

@LightDarkPreview
@Composable
internal fun DirectoryErrorOnDigaRedeemDialogPreview() {
    PreviewTheme {
        DirectoryErrorOnDigaRedeemDialog {}
    }
}
