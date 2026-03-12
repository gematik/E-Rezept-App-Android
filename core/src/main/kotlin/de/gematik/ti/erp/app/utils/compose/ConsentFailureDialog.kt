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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import de.gematik.ti.erp.app.authentication.mapper.toDialogMapper
import de.gematik.ti.erp.app.authentication.model.DialogParameter
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.utils.compose.preview.AuthenticationFailureDialogPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.preview.AuthenticationResultErrorData

@Composable
fun ConsentFailureDialog(
    error: DialogParameter,
    onClickAction: () -> Unit
) {
    val title = stringResource(error.title)
    val message = stringResource(error.message)
    val cancelText = error.cancelText?.let { stringResource(it) }
    val actionText = error.actionText?.let { stringResource(it) }

    if (actionText != null) {
        ErezeptAlertDialog(
            title = title,
            bodyText = message,
            confirmText = actionText,
            dismissText = cancelText ?: stringResource(R.string.cancel),
            onDismissRequest = onClickAction,
            onConfirmRequest = onClickAction
        )
    } else {
        ErezeptAlertDialog(
            title = title,
            body = message,
            okText = cancelText ?: stringResource(R.string.ok),
            onDismissRequest = onClickAction
        )
    }
}

@Suppress("MultipleEmitters")
@LightDarkPreview
@Composable
fun ConsentFailureDialogPreview(
    @PreviewParameter(AuthenticationFailureDialogPreviewParameterProvider::class) errorData: AuthenticationResultErrorData
) {
    PreviewTheme {
        AuthenticationFailureDialog(
            error = errorData.error.toDialogMapper() ?: DialogParameter(
                title = R.string.cdw_nfc_error_title_invalid_ocsp_response_of_health_card_certificate,
                message = R.string.cdw_nfc_error_body_invalid_ocsp_response_of_health_card_certificate
            ),
            onClickAction = {}
        )
    }
}
