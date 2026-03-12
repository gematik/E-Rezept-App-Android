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

package de.gematik.ti.erp.app.authentication.mapper

import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.authentication.model.DialogParameter
import de.gematik.ti.erp.app.core.R

fun HttpErrorState.toDialogMapper(): DialogParameter? {
    val cancelText = R.string.consent_error_dialog_cancel
    val retryText = R.string.consent_error_dialog_retry
    return when (this) {
        HttpErrorState.RequestTimeout ->
            DialogParameter(
                title = R.string.consent_error_server_timeout_header,
                message = R.string.consent_error_server_timeout_info,
                cancelText = cancelText,
                actionText = retryText
            )

        HttpErrorState.ServerError ->
            DialogParameter(
                title = R.string.consent_error_internal_error_header,
                message = R.string.consent_error_internal_error_info,
                cancelText = cancelText,
                actionText = retryText
            )

        HttpErrorState.TooManyRequest ->
            DialogParameter(
                title = R.string.consent_error_too_many_requests_header,
                message = R.string.consent_error_too_many_requests_info,
                cancelText = cancelText,
                actionText = retryText
            )

        HttpErrorState.BadRequest ->
            DialogParameter(
                title = R.string.consent_error_bad_request_header,
                message = R.string.consent_error_bad_request_info,
                cancelText = cancelText
            )

        HttpErrorState.Forbidden ->
            DialogParameter(
                title = R.string.consent_error_forbidden_header,
                message = R.string.consent_error_forbidden_info,
                cancelText = cancelText
            )

        HttpErrorState.Unauthorized ->
            DialogParameter(
                title = R.string.consent_error_unauthorized_header,
                message = R.string.consent_error_unauthorized_info,
                cancelText = cancelText,
                actionText = R.string.consent_error_connect
            )
        else -> null
    }
}
