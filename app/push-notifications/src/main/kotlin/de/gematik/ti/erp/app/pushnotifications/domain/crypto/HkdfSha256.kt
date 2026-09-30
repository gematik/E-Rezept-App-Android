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

import de.gematik.ti.erp.app.Requirement
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val HASH_LENGTH = 32 // SHA-256 output length in bytes
private const val HMAC_SHA256 = "HmacSHA256"

/**
 * HKDF (HMAC-based Key Derivation Function) using SHA-256.
 * Used for deriving monthly encryption keys from the shared secret.
 */
interface HkdfSha256 {
    fun derive(ikm: ByteArray, info: ByteArray, length: Int): ByteArray
}

@Requirement(
    "A_27170-01#1",
    sourceSpecification = "gemF_PushNotification",
    rationale = "Implements RFC 5869 HKDF-SHA256 with the zero-filled default salt.",
    codeLines = 31
)
class DefaultHkdfSha256 : HkdfSha256 {

    override fun derive(ikm: ByteArray, info: ByteArray, length: Int): ByteArray {
        require(length <= 255 * HASH_LENGTH) { "Requested length too large" }
        val prk = extract(salt = ByteArray(HASH_LENGTH), ikm = ikm)
        return expand(prk = prk, info = info, length = length)
    }

    private fun hmac(key: ByteArray): Mac =
        Mac.getInstance(HMAC_SHA256).also { it.init(SecretKeySpec(key, HMAC_SHA256)) }

    private fun extract(salt: ByteArray, ikm: ByteArray): ByteArray =
        hmac(salt).doFinal(ikm)

    private fun expand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
        val iterations = (length + HASH_LENGTH - 1) / HASH_LENGTH
        val mac = hmac(prk)
        var previous = byteArrayOf()

        return ByteArray(iterations * HASH_LENGTH).also { output ->
            for (i in 1..iterations) {
                mac.reset()
                mac.update(previous)
                mac.update(info)
                mac.update(i.toByte())
                previous = mac.doFinal()
                previous.copyInto(output, (i - 1) * HASH_LENGTH)
            }
        }.copyOfRange(0, length)
    }
}
