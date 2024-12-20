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

package de.gematik.ti.erp.app.authentication.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.cardwall.mini.ui.BiometricPromptAuthenticator
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.toAnnotatedString
import kotlinx.coroutines.launch

@Composable
fun BiometricPrompt(
    authenticator: BiometricPromptAuthenticator
) {
    val scope = rememberCoroutineScope()
    authenticator.showError?.let { error ->
        val retryText = when (error) {
            is BiometricPromptAuthenticator.Error.RemoteCommunicationAltAuthNotSuccessful -> Pair(
                stringResource(R.string.cdw_mini_alt_auth_removed_title).toAnnotatedString(),
                stringResource(R.string.cdw_mini_alt_auth_removed).toAnnotatedString()
            )

            BiometricPromptAuthenticator.Error.RemoteCommunicationFailed -> Pair(
                stringResource(R.string.cdw_nfc_intro_step1_header_on_error).toAnnotatedString(),
                stringResource(R.string.cdw_idp_error_time_and_connection).toAnnotatedString()
            )

            BiometricPromptAuthenticator.Error.RemoteCommunicationInvalidCertificate -> Pair(
                stringResource(R.string.cdw_nfc_error_title_invalid_certificate).toAnnotatedString(),
                stringResource(R.string.cdw_nfc_error_body_invalid_certificate).toAnnotatedString()
            )

            BiometricPromptAuthenticator.Error.RemoteCommunicationInvalidOCSP -> Pair(
                stringResource(R.string.cdw_nfc_error_title_invalid_ocsp_response_of_health_card_certificate)
                    .toAnnotatedString(),
                stringResource(R.string.cdw_nfc_error_body_invalid_ocsp_response_of_health_card_certificate)
                    .toAnnotatedString()
            )
        }

        retryText.let { (title, message) ->
            AcceptDialog(
                header = title,
                info = message,
                acceptText = stringResource(R.string.ok),
                onClickAccept = {
                    scope.launch {
                        if (error is BiometricPromptAuthenticator.Error.RemoteCommunicationAltAuthNotSuccessful) {
                            authenticator.removeAuthentication(error.profileId)
                        }
                        authenticator.resetErrorState()
                    }
                }
            )
        }
    }
}
