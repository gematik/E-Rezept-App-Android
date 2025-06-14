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

package de.gematik.ti.erp.app.authentication.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.cardwall.mini.ui.HealthCardPromptAuthenticator
import de.gematik.ti.erp.app.cardwall.ui.components.pinRetriesLeft
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.toAnnotatedString

@Requirement(
    "A_20079#1",
    "A_20085#1",
    "A_20605#4",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Display error messages from endpoint."
)
@Requirement(
    "A_20605#3",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "The interceptor pattern is used to add the bearer token to the request."
)
@Composable
internal fun HealthCardErrorDialog(
    state: HealthCardPromptAuthenticator.State.ReadState.Error,
    onCancel: () -> Unit,
    onEnableNfc: () -> Unit
) {
    if (state == HealthCardPromptAuthenticator.State.ReadState.Error.NfcDisabled) {
        CommonAlertDialog(
            header = stringResource(R.string.cdw_enable_nfc_header),
            info = stringResource(R.string.cdw_enable_nfc_info),
            cancelText = stringResource(R.string.cancel),
            actionText = stringResource(R.string.cdw_enable_nfc_btn_text),
            onCancel = onCancel,
            onClickAction = onEnableNfc
        )
    } else {
        val retryText = when (state) {
            HealthCardPromptAuthenticator.State.ReadState.Error.RemoteCommunicationFailed -> Pair(
                stringResource(R.string.cdw_nfc_intro_step1_header_on_error).toAnnotatedString(),
                stringResource(R.string.cdw_idp_error_time_and_connection).toAnnotatedString()
            )

            HealthCardPromptAuthenticator.State.ReadState.Error.RemoteCommunicationInvalidCertificate -> Pair(
                stringResource(R.string.cdw_nfc_error_title_invalid_certificate).toAnnotatedString(),
                stringResource(R.string.cdw_nfc_error_body_invalid_certificate).toAnnotatedString()
            )

            HealthCardPromptAuthenticator.State.ReadState.Error.RemoteCommunicationInvalidOCSP -> Pair(
                stringResource(R.string.cdw_nfc_error_title_invalid_ocsp_response_of_health_card_certificate)
                    .toAnnotatedString(),
                stringResource(R.string.cdw_nfc_error_body_invalid_ocsp_response_of_health_card_certificate)
                    .toAnnotatedString()
            )

            HealthCardPromptAuthenticator.State.ReadState.Error.CardAccessNumberWrong -> Pair(
                stringResource(R.string.cdw_nfc_intro_step2_header_on_can_error_alert).toAnnotatedString(),
                stringResource(R.string.cdw_nfc_intro_step2_info_on_can_error).toAnnotatedString()
            )

            is HealthCardPromptAuthenticator.State.ReadState.Error.PersonalIdentificationWrong -> Pair(
                stringResource(R.string.cdw_nfc_intro_step2_header_on_pin_error_alert).toAnnotatedString(),
                pinRetriesLeft(state.retriesLeft)
            )

            HealthCardPromptAuthenticator.State.ReadState.Error.HealthCardBlocked -> Pair(
                stringResource(R.string.cdw_header_on_card_blocked).toAnnotatedString(),
                stringResource(R.string.cdw_info_on_card_blocked).toAnnotatedString()
            )

            else -> null
        }

        retryText?.let { (title, message) ->

            AcceptDialog(
                header = title,
                info = message,
                acceptText = stringResource(R.string.ok),
                onClickAccept = onCancel
            )
        }
    }
}
