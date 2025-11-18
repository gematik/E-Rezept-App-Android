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

package de.gematik.ti.erp.app.utils.compose.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult

class AuthenticationFailureDialogPreviewParameterProvider : PreviewParameterProvider<AuthenticationResultErrorData> {

    override val values = sequenceOf(
        AuthenticationResultErrorData(
            name = "Authentication not successful",
            error = AuthenticationResult.IdpCommunicationError.AuthenticationNotSuccessful
        ),
        AuthenticationResultErrorData(
            name = "Communication failure",
            error = AuthenticationResult.IdpCommunicationError.CommunicationFailure
        ),
        AuthenticationResultErrorData(
            name = "Invalid certificate",
            error = AuthenticationResult.IdpCommunicationError.InvalidCertificate
        ),
        AuthenticationResultErrorData(
            name = "Invalid OCSP",
            error = AuthenticationResult.IdpCommunicationError.InvalidOCSP
        ),
        AuthenticationResultErrorData(
            name = "Unknown",
            error = AuthenticationResult.IdpCommunicationError.Unknown
        )
    )
}

data class AuthenticationResultErrorData(
    val name: String,
    val error: AuthenticationResult.Error
)
