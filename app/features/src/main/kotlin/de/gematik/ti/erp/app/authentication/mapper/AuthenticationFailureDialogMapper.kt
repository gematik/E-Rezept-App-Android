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

package de.gematik.ti.erp.app.authentication.mapper

import de.gematik.ti.erp.app.authentication.model.AuthenticationDialogParameter
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult
import de.gematik.ti.erp.app.app_core.R

fun (AuthenticationResult.Error).toDialogMapper(): AuthenticationDialogParameter? =
    when (this) {
        AuthenticationResult.IdpCommunicationError.AuthenticationNotSuccessful -> AuthenticationDialogParameter(
            title = R.string.cdw_mini_alt_auth_removed_title,
            message = R.string.cdw_mini_alt_auth_removed
        )
        AuthenticationResult.IdpCommunicationError.CommunicationFailure -> AuthenticationDialogParameter(
            title = R.string.cdw_nfc_intro_step1_header_on_error,
            message = R.string.cdw_idp_error_time_and_connection
        )
        AuthenticationResult.IdpCommunicationError.InvalidCertificate -> AuthenticationDialogParameter(
            title = R.string.cdw_nfc_error_title_invalid_certificate,
            message = R.string.cdw_nfc_error_body_invalid_certificate
        )
        AuthenticationResult.IdpCommunicationError.InvalidOCSP -> AuthenticationDialogParameter(
            title = R.string.cdw_nfc_error_title_invalid_ocsp_response_of_health_card_certificate,
            message = R.string.cdw_nfc_error_body_invalid_ocsp_response_of_health_card_certificate
        )
        // TODO: Add dialog params for SecureElementFailure & UserNotAuthenticated
        AuthenticationResult.IdpCommunicationError.SecureElementFailure -> null
        AuthenticationResult.IdpCommunicationError.UserNotAuthenticated -> null
        is AuthenticationResult.BiometricResult.BiometricError -> null
        is AuthenticationResult.IdpCommunicationError.IllegalStateError -> null
        is AuthenticationResult.UnknownError -> null
    }
