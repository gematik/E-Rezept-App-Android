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

package de.gematik.ti.erp.app.pkv.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.api.ErpServiceState
import de.gematik.ti.erp.app.consent.model.ConsentContext
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

// TODO: Needs to follow SOLID principles
@Suppress("LongParameterList", "CyclomaticComplexMethod")
@Composable
fun HandleConsentState(
    consentState: ConsentState,
    onDeleteLocalInvoices: () -> Unit = {},
    dialog: DialogScaffold,
    onRetry: (ConsentContext) -> Unit,
    onShowCardWall: () -> Unit,
    onConsentGranted: () -> Unit = {},
    onConsentRevoked: () -> Unit = {},
    onConsentNotGranted: () -> Unit = {}
) {
    val alertData = consentErrorData(consentState)

    when (consentState) {
        is ConsentState.ConsentErrorState.NoInternet ->
            ConsentErrorDialog(dialog, alertData) {
                onRetry(consentState.context)
            }
        ConsentState.ConsentErrorState.BadRequest -> ConsentErrorDialog(dialog, alertData) {}
        is ConsentState.ConsentErrorState.ServerTimeout ->
            ConsentErrorDialog(dialog, alertData) {
                onRetry(consentState.context)
            }
        is ConsentState.ConsentErrorState.InternalError ->
            ConsentErrorDialog(dialog, alertData) {
                onRetry(consentState.context)
            }
        is ConsentState.ConsentErrorState.TooManyRequests ->
            ConsentErrorDialog(dialog, alertData) {
                onRetry(consentState.context)
            }
        ConsentState.ConsentErrorState.Forbidden -> ConsentErrorDialog(dialog, alertData) {}
        ConsentState.ConsentErrorState.Unauthorized ->
            ConsentErrorDialog(
                dialog,
                alertData,
                onShowCardWall
            )
        ConsentState.ConsentErrorState.AlreadyGranted -> { /* do nothing */ }
        ConsentState.ConsentErrorState.ChargeConsentAlreadyRevoked -> { /* do nothing */ }
        ConsentState.ConsentErrorState.Unknown -> { /* do nothing */ }
        is ConsentState.ValidState.Granted -> {
            if (consentState.context == ConsentContext.GrantConsent) {
                onConsentGranted()
            }
        }
        ConsentState.ValidState.Revoked -> {
            onDeleteLocalInvoices()
            onConsentRevoked()
        }
        ConsentState.ValidState.NotGranted -> { onConsentNotGranted() }
        ConsentState.ValidState.Loading -> { /* do nothing */ }
        ConsentState.ValidState.UnknownConsent -> { /* do nothing */ }
    }
}

data class ConsentAlertData(
    val header: String,
    val info: String,
    val cancelText: String,
    val actionText: String? = null
)

@Composable
fun consentErrorData(errorState: ErpServiceState): ConsentAlertData {
    val cancelText = stringResource(R.string.consent_error_dialog_cancel)
    val retryText = stringResource(R.string.consent_error_dialog_retry)
    return when (errorState) {
        ConsentState.ConsentErrorState.AlreadyGranted ->
            ConsentAlertData(
                header = stringResource(R.string.consent_error_already_granted_header),
                info = stringResource(R.string.consent_error_already_granted_info),
                cancelText = cancelText,
                actionText = stringResource(R.string.consent_action_to_invoices)
            )
        is ConsentState.ConsentErrorState.NoInternet ->
            ConsentAlertData(
                header = stringResource(R.string.consent_error_no_internet_header),
                info = stringResource(R.string.consent_error_no_internet_info),
                cancelText = cancelText,
                actionText = retryText
            )
        ConsentState.ConsentErrorState.BadRequest ->
            ConsentAlertData(
                header = stringResource(R.string.consent_error_bad_request_header),
                info = stringResource(R.string.consent_error_bad_request_info),
                cancelText = cancelText
            )
        is ConsentState.ConsentErrorState.ServerTimeout ->
            ConsentAlertData(
                header = stringResource(R.string.consent_error_server_timeout_header),
                info = stringResource(R.string.consent_error_server_timeout_info),
                cancelText = cancelText,
                actionText = retryText
            )
        is ConsentState.ConsentErrorState.InternalError ->
            ConsentAlertData(
                header = stringResource(R.string.consent_error_internal_error_header),
                info = stringResource(R.string.consent_error_internal_error_info),
                cancelText = cancelText,
                actionText = retryText
            )
        is ConsentState.ConsentErrorState.TooManyRequests ->
            ConsentAlertData(
                header = stringResource(R.string.consent_error_too_many_requests_header),
                info = stringResource(R.string.consent_error_too_many_requests_info),
                cancelText = cancelText,
                actionText = retryText
            )
        ConsentState.ConsentErrorState.Forbidden ->
            ConsentAlertData(
                header = stringResource(R.string.consent_error_forbidden_header),
                info = stringResource(R.string.consent_error_forbidden_info),
                cancelText = cancelText
            )
        ConsentState.ConsentErrorState.Unauthorized ->
            ConsentAlertData(
                header = stringResource(R.string.consent_error_unauthorized_header),
                info = stringResource(R.string.consent_error_unauthorized_info),
                cancelText = cancelText,
                actionText = stringResource(R.string.consent_error_connect)
            )
        else -> {
            ConsentAlertData(
                header = "",
                info = "",
                cancelText = cancelText
            )
        }
    }
}

@Composable
fun ConsentErrorDialog(
    dialogScaffold: DialogScaffold,
    alertData: ConsentAlertData,
    onAction: () -> Unit
) = if (alertData.actionText != null) {
    dialogScaffold.show { dialog ->
        ErezeptAlertDialog(
            title = alertData.header,
            bodyText = alertData.info,
            confirmText = alertData.actionText,
            dismissText = alertData.cancelText,
            onDismissRequest = {
                dialog.dismiss()
            },
            onConfirmRequest = {
                onAction()
                dialog.dismiss()
            }
        )
    }
} else {
    dialogScaffold.show { dialog ->
        ErezeptAlertDialog(
            title = alertData.header,
            body = alertData.info,
            okText = alertData.cancelText,
            onDismissRequest = dialog::dismiss
        )
    }
}
