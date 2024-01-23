/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.pkv.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.google.android.material.snackbar.Snackbar
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pkv.presentation.ConsentController
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.extensions.SnackbarScaffold

@Composable
fun HandleConsentState(
    consentState: PrescriptionServiceState,
    snackbar: SnackbarScaffold,
    onClickSnackbarAction: () -> Unit
) {
    val consentGrantedInfo = stringResource(R.string.consent_granted_info)
    val actionId = R.string.consent_action_to_invoices

    when (consentState) {
        ConsentController.ConsentState.Granted -> {
            snackbar.show(
                consentGrantedInfo,
                length = Snackbar.LENGTH_LONG,
                actionTextId = actionId,
                onClickAction = onClickSnackbarAction
            )
        }

        else -> {
            // Don't show anything
        }
    }
}

@Composable
fun HandleConsentErrorState(
    consentErrorState: PrescriptionServiceState,
    onShowCardWall: () -> Unit,
    onClickToInvoices: () -> Unit,
    onRetry: () -> Unit
) {
    var showAlertDialog by remember { mutableStateOf(true) }
    val alertMessage = consentErrorMessage(consentErrorState)
    val onCancel = { showAlertDialog = false }

    alertMessage?.let { message ->
        if (showAlertDialog) {
            when (consentErrorState) {
                ConsentController.ConsentErrorState.AlreadyGranted -> ConsentErrorDialog(
                    message,
                    onClickToInvoices,
                    onCancel
                )
                ConsentController.ConsentErrorState.NoInternet -> ConsentErrorDialog(message, onRetry, onCancel)
                ConsentController.ConsentErrorState.BadRequest -> ConsentErrorDialog(message, {}, onCancel)
                ConsentController.ConsentErrorState.ServerTimeout -> ConsentErrorDialog(message, onRetry, onCancel)
                ConsentController.ConsentErrorState.InternalError -> ConsentErrorDialog(message, onRetry, onCancel)
                ConsentController.ConsentErrorState.TooManyRequests -> ConsentErrorDialog(message, onRetry, onCancel)
                ConsentController.ConsentErrorState.Forbidden -> ConsentErrorDialog(message, {}, onCancel)
                ConsentController.ConsentErrorState.Unauthorized -> ConsentErrorDialog(
                    message,
                    onShowCardWall,
                    onCancel
                )
                else -> {
                    // Don't show anything
                }
            }
        }
    }
}

data class ConsentAlertData(
    val header: String,
    val info: String,
    val cancelText: String,
    val actionText: String? = null
)

@Composable
fun consentErrorMessage(
    errorState: PrescriptionServiceState
): ConsentAlertData? {
    val cancelText = stringResource(R.string.consent_error_dialog_cancel)
    val retryText = stringResource(R.string.consent_error_dialog_retry)
    return when (errorState) {
        ConsentController.ConsentErrorState.AlreadyGranted -> ConsentAlertData(
            header = stringResource(R.string.consent_error_already_granted_header),
            info = stringResource(R.string.consent_error_already_granted_info),
            cancelText = cancelText,
            actionText = stringResource(R.string.consent_action_to_invoices)
        )
        ConsentController.ConsentErrorState.NoInternet -> ConsentAlertData(
            header = stringResource(R.string.consent_error_no_internet_header),
            info = stringResource(R.string.consent_error_no_internet_info),
            cancelText = cancelText,
            actionText = retryText
        )
        ConsentController.ConsentErrorState.BadRequest -> ConsentAlertData(
            header = stringResource(R.string.consent_error_bad_request_header),
            info = stringResource(R.string.consent_error_bad_request_info),
            cancelText = cancelText
        )
        ConsentController.ConsentErrorState.ServerTimeout -> ConsentAlertData(
            header = stringResource(R.string.consent_error_server_timeout_header),
            info = stringResource(R.string.consent_error_server_timeout_info),
            cancelText = cancelText,
            actionText = retryText
        )
        ConsentController.ConsentErrorState.InternalError -> ConsentAlertData(
            header = stringResource(R.string.consent_error_internal_error_header),
            info = stringResource(R.string.consent_error_internal_error_info),
            cancelText = cancelText,
            actionText = retryText
        )
        ConsentController.ConsentErrorState.TooManyRequests -> ConsentAlertData(
            header = stringResource(R.string.consent_error_too_many_requests_header),
            info = stringResource(R.string.consent_error_too_many_requests_info),
            cancelText = cancelText,
            actionText = retryText
        )
        ConsentController.ConsentErrorState.Forbidden -> ConsentAlertData(
            header = stringResource(R.string.consent_error_forbidden_header),
            info = stringResource(R.string.consent_error_forbidden_info),
            cancelText = cancelText
        )
        ConsentController.ConsentErrorState.Unauthorized -> ConsentAlertData(
            header = stringResource(R.string.consent_error_unauthorized_header),
            info = stringResource(R.string.consent_error_unauthorized_info),
            cancelText = cancelText,
            actionText = stringResource(R.string.consent_error_connect)
        )
        else -> null
    }
}

@Composable
fun ConsentErrorDialog(alertData: ConsentAlertData, onAction: () -> Unit, onCancel: () -> Unit) =
    if (alertData.actionText != null) {
        CommonAlertDialog(
            header = alertData.header,
            info = alertData.info,
            actionText = alertData.actionText,
            cancelText = alertData.cancelText,
            onClickAction = onAction,
            onCancel = onCancel
        )
    } else {
        AcceptDialog(
            header = alertData.header,
            info = alertData.info,
            acceptText = alertData.cancelText,
            onClickAccept = onCancel
        )
    }
