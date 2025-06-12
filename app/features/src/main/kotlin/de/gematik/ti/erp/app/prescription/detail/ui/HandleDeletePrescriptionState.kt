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

package de.gematik.ti.erp.app.prescription.detail.ui

import android.app.Dialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.api.HTTP_BAD_REQUEST
import de.gematik.ti.erp.app.api.HTTP_METHOD_NOT_ALLOWED
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.prescription.usecase.DeletePrescriptionUseCase
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Suppress("FunctionNaming", "LongParameterList", "LongMethod")
@Composable
fun HandleDeletePrescriptionState(
    state: DeletePrescriptionUseCase.DeletePrescriptionState,
    dialog: DialogScaffold,
    onConfirmDialogRequest: (
        sendFeedBackMessage: Pair<Boolean, String>,
        deletePrescriptionLocally: Boolean
    ) -> Unit,
    onShowCardWall: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onBack: () -> Unit
) {
    var sendFeedBackMessageSelected by remember { mutableStateOf(true) }
    var deletePrescriptionLocallySelected by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        when (state) {
            is DeletePrescriptionUseCase.DeletePrescriptionState.ValidState.Deleted -> {
                onBack()
            }

            is DeletePrescriptionUseCase.DeletePrescriptionState.ErrorState.BadRequest -> {
                dialog.show { dialog ->
                    BadRequestErrorDialog(
                        sendFeedBackMessageSelected = sendFeedBackMessageSelected,
                        deletePrescriptionLocallySelected = deletePrescriptionLocallySelected,
                        dialog = dialog,
                        errorCode = HTTP_BAD_REQUEST,
                        onConfirmDialogRequest = onConfirmDialogRequest,
                        onClickSendFeedBackMessage = { selected ->
                            sendFeedBackMessageSelected = selected
                        }
                    ) { selected ->
                        deletePrescriptionLocallySelected = selected
                    }
                }
            }

            DeletePrescriptionUseCase.DeletePrescriptionState.ErrorState.InternalError -> {
                dialog.show { dialog ->
                    ErezeptAlertDialog(
                        title = stringResource(R.string.delete_prescription_error_internal_title),
                        body = stringResource(R.string.delete_prescription_error_internal_info),
                        onDismissRequest = {
                            onDismiss()
                            dialog.dismiss()
                        }
                    )
                }
            }
            DeletePrescriptionUseCase.DeletePrescriptionState.ErrorState.MethodNotAllowed -> {
                dialog.show { dialog ->
                    BadRequestErrorDialog(
                        sendFeedBackMessageSelected = sendFeedBackMessageSelected,
                        deletePrescriptionLocallySelected = deletePrescriptionLocallySelected,
                        dialog = dialog,
                        errorCode = HTTP_METHOD_NOT_ALLOWED,
                        onConfirmDialogRequest = onConfirmDialogRequest,
                        onClickSendFeedBackMessage = { selected ->
                            sendFeedBackMessageSelected = selected
                        }
                    ) { selected ->
                        deletePrescriptionLocallySelected = selected
                    }
                }
            }
            DeletePrescriptionUseCase.DeletePrescriptionState.ErrorState.NoInternet -> {
                dialog.show { dialog ->
                    ErezeptAlertDialog(
                        title = stringResource(R.string.delete_prescription_error_no_internet_title),
                        bodyText = stringResource(R.string.delete_prescription_error_no_internet_info),
                        confirmText = stringResource(R.string.delete_prescription_error_no_internet_confirm),
                        dismissText = stringResource(R.string.delete_prescription_error_no_internet_dissmiss),
                        onDismissRequest = {
                            onDismiss()
                            dialog.dismiss()
                        },
                        onConfirmRequest = {
                            onRetry()
                            dialog.dismiss()
                        }
                    )
                }
            }
            DeletePrescriptionUseCase.DeletePrescriptionState.ErrorState.ErpWorkflowBlocked -> {
                dialog.show { dialog ->
                    ErezeptAlertDialog(
                        title = stringResource(R.string.delete_prescription_error_blocked_title),
                        body = stringResource(R.string.delete_prescription_error_blocked_info),
                        onDismissRequest = {
                            onDismiss()
                            dialog.dismiss()
                        }
                    )
                }
            }
            DeletePrescriptionUseCase.DeletePrescriptionState.ErrorState.TooManyRequests -> {
                dialog.show { dialog ->
                    ErezeptAlertDialog(
                        title = stringResource(R.string.delete_prescription_error_too_many_requests_title),
                        body = stringResource(R.string.delete_prescription_error_too_many_requests_info),
                        onDismissRequest = {
                            onDismiss()
                            dialog.dismiss()
                        }
                    )
                }
            }
            DeletePrescriptionUseCase.DeletePrescriptionState.ErrorState.Unauthorized -> {
                dialog.show { dialog ->
                    ErezeptAlertDialog(
                        title = stringResource(R.string.delete_prescription_error_unauthorized_title),
                        bodyText = stringResource(R.string.delete_prescription_error_unauthorized_info),
                        confirmText = stringResource(R.string.delete_prescription_error_unauthorized_confirm),
                        dismissText = stringResource(R.string.delete_prescription_error_unauthorized_dissmiss),
                        onDismissRequest = {
                            onDismiss()
                            dialog.dismiss()
                        },
                        onConfirmRequest = {
                            onShowCardWall()
                            dialog.dismiss()
                        }
                    )
                }
            }
            else -> {}
        }
    }
}

@Suppress("FunctionNaming", "LongParameterList")
@Composable
private fun BadRequestErrorDialog(
    sendFeedBackMessageSelected: Boolean,
    deletePrescriptionLocallySelected: Boolean,
    dialog: Dialog,
    errorCode: Int,
    onConfirmDialogRequest: (
        sendFeedBackMessage: Pair<Boolean, String>,
        deletePrescriptionLocally: Boolean
    ) -> Unit,
    onClickSendFeedBackMessage: (Boolean) -> Unit,
    onClickDeletePrescriptionLocally: (Boolean) -> Unit
) {
    val errorMessage = stringResource(R.string.delete_prescription_feedback_mail_body, errorCode)

    ErezeptAlertDialog(
        title = stringResource(R.string.delete_error_dialog_bad_request_title),
        body = {
            BadRequestErrorBody(
                errorNumber = errorCode,
                sendFeedBackMessageSelected,
                deletePrescriptionLocallySelected,
                onClickSendFeedBackMessage = { selected ->
                    onClickSendFeedBackMessage(selected)
                },
                onClickDeletePrescriptionLocally = { selected ->
                    onClickDeletePrescriptionLocally(selected)
                }
            )
        },
        okText = stringResource(R.string.delete_error_dialog_forward),
        onDismissRequest = { dialog.dismiss() },
        onConfirmRequest = {
            onConfirmDialogRequest(
                Pair(sendFeedBackMessageSelected, errorMessage),
                deletePrescriptionLocallySelected
            )
            dialog.dismiss()
        }
    )
}

@Suppress("FunctionNaming")
@Composable
private fun BadRequestErrorBody(
    errorNumber: Int,
    sendFeedBackMessageSelected: Boolean,
    deletePrescriptionLocallySelected: Boolean,
    onClickSendFeedBackMessage: (Boolean) -> Unit,
    onClickDeletePrescriptionLocally: (Boolean) -> Unit
) {
    Column {
        Text(
            annotatedStringResource(R.string.delete_prescription_bad_request_info, errorNumber).toString()
        )
        SpacerMedium()

        LabeledCheckBox(
            checked = sendFeedBackMessageSelected,
            labelText = stringResource(R.string.delete_prescription_send_support_message),
            onCheckedChanged = {
                onClickSendFeedBackMessage(it)
            }
        )
        LabeledCheckBox(
            checked = deletePrescriptionLocallySelected,
            labelText = stringResource(R.string.delete_prescription_locally),
            onCheckedChanged = {
                onClickDeletePrescriptionLocally(it)
            }
        )
    }
}

@Suppress("FunctionNaming")
@Composable
private fun LabeledCheckBox(
    checked: Boolean,
    labelText: String,
    onCheckedChanged: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
        Modifier
            .fillMaxWidth()
            .clickable
            { onCheckedChanged(!checked) }
    ) {
        Checkbox(
            modifier = Modifier.padding(start = PaddingDefaults.ShortMedium),
            checked = checked,
            onCheckedChange = { onCheckedChanged(it) }
        )
        Text(labelText)
    }
}
