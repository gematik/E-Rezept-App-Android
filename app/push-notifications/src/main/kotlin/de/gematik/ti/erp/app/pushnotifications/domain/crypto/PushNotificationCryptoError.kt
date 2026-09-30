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

package de.gematik.ti.erp.app.pushnotifications.domain.crypto

sealed class PushNotificationCryptoError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class NoKeyAvailable :
        PushNotificationCryptoError("No key generation available. Initialize the key chain first.")

    class KeyNotFoundForMonth(month: String) :
        PushNotificationCryptoError("Key generation for '$month' could not be derived.")

    class InvalidPayloadTooShort :
        PushNotificationCryptoError("Decrypted payload is too short to contain a valid PNM1 frame.")

    class InvalidPNM1Prefix :
        PushNotificationCryptoError("Decrypted payload does not start with the expected 'PNM1' prefix.")

    class InvalidPayloadLength :
        PushNotificationCryptoError("PNM1 padding length field exceeds the actual payload size.")

    class FutureMonthRejected(month: String, maxAllowed: String) :
        PushNotificationCryptoError(
            "Refusing to advance key chain: '$month' is too far in the future (max allowed: $maxAllowed)."
        )

    class StaleMonthRejected(month: String, latestMonth: String) :
        PushNotificationCryptoError(
            "Refusing to advance key chain backwards: '$month' is older than the latest derived month '$latestMonth'."
        )

    class KeyIdentifierMismatch(received: String, expected: String) :
        PushNotificationCryptoError(
            "key_identifier mismatch: received '$received', expected '$expected'."
        )

    class UnknownKeyIdentifier(received: String, known: Set<String>) :
        PushNotificationCryptoError(
            "Unknown key_identifier '$received'. Known identifiers: ${known.ifEmpty { setOf("<none>") }}."
        )

    class KeyStorageCommitFailed(operation: String, keyIdentifier: String) :
        PushNotificationCryptoError(
            "Push key storage commit failed during '$operation' for key_identifier '$keyIdentifier'."
        )

    class ReEnrollmentRequired(keyIdentifier: String, cause: Throwable? = null) :
        PushNotificationCryptoError(
            "Stored push key material for key_identifier '$keyIdentifier' is unreadable. Re-enrollment is required.",
            cause
        )
}
