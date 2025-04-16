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

package de.gematik.ti.erp.app.authentication.model

import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState

sealed interface AuthenticationResult {

    sealed interface Error : AuthenticationResult {
        sealed interface ResetError : Error // this error means the user has to re-authenticate using the complete card-wall
        sealed interface RetryError : Error // this error means the user has to retry the current step
        sealed interface IgnoreError : Error
    }

    data object UnknownError : AuthenticationResult, Error.IgnoreError

    // results from the biometric prompt
    sealed interface BiometricResult : AuthenticationResult {
        data object BiometricStarted : BiometricResult
        data object BiometricSuccess : BiometricResult
        data class BiometricError(val error: String, val errorCode: Int) : BiometricResult, Error.IgnoreError
    }

    // results from the idp communication
    sealed interface IdpCommunicationUpdate : AuthenticationResult {
        data object IdpCommunicationStarted : IdpCommunicationUpdate
        data object IdpCommunicationUpdated : IdpCommunicationUpdate
        data object IdpCommunicationSuccess : IdpCommunicationUpdate
    }

    // errors from the idp communication
    sealed interface IdpCommunicationError : AuthenticationResult {
        data object AuthenticationNotSuccessful : IdpCommunicationError, Error.ResetError
        data object CommunicationFailure : IdpCommunicationError, Error.RetryError
        data object InvalidCertificate : IdpCommunicationError, Error.RetryError
        data object InvalidOCSP : IdpCommunicationError, Error.RetryError
        data object SecureElementFailure : IdpCommunicationError, Error.ResetError
        data object UserNotAuthenticated : IdpCommunicationError, Error.ResetError
        data class IllegalStateError(val state: AuthenticationState) : IdpCommunicationError, Error.ResetError
    }
}
