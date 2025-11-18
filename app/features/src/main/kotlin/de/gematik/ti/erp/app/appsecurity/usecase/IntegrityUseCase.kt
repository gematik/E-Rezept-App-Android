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

package de.gematik.ti.erp.app.appsecurity.usecase

import android.content.Context
import android.util.Base64
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.secureRandomInstance
import de.gematik.ti.erp.app.vau.toLowerCaseHex
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwx.JsonWebStructure
import org.json.JSONObject
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

private const val REQUIRED_SALT_LENGTH = 32
private const val DEVICE_INTEGRITY = "deviceIntegrity"
private const val MEETS_DEVICE_INTEGRITY = "MEETS_DEVICE_INTEGRITY"
private const val SHA_256 = "SHA-256"
private const val AES = "AES"
private const val EC = "EC"

class IntegrityUseCase(
    private val context: Context
) {
    @Requirement(
        "O.Arch_6#3",
        "O.Resi_2#1",
        "O.Resi_3#1",
        "O.Resi_4#1",
        "O.Resi_5#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Run integrity check against the IntegrityManagerFactory provided by Google Play Core Library."
    )
    fun runIntegrityAttestation(): Flow<Boolean> = flow {
        val nonce = generateSecureNonce()
        val token = requestIntegrityToken(nonce)
        val payload = decryptAndVerifyToken(token)
        val meetsIntegrity = verifyDeviceIntegrity(payload)

        emit(meetsIntegrity)
    }.catch { exception ->
        Napier.e(exception) {
            "Integrity check failed: ${exception.message}. " +
                "Allowing user to proceed as per fallback policy."
        }
        emit(true)
    }

    /**
     * Generates a secure nonce combining timestamp and random salt.
     * This ensures uniqueness and prevents replay attacks.
     */
    private fun generateSecureNonce(): String {
        val salt = provideSalt()
        val nonceData = createNonceData()
        return generateNonce(salt, nonceData)
    }

    /**
     * Requests an integrity token from Google Play Integrity API.
     */
    private suspend fun requestIntegrityToken(nonce: String): String {
        val integrityManager = IntegrityManagerFactory.create(context)

        val tokenResponse = integrityManager.requestIntegrityToken(
            IntegrityTokenRequest.builder()
                .setCloudProjectNumber(BuildKonfig.CLOUD_PROJECT_NUMBER.toLong())
                .setNonce(nonce)
                .build()
        ).await()

        return tokenResponse.token()
    }

    /**
     * Decrypts and verifies the integrity token using the configured keys.
     */
    @Requirement(
        "O.Cryp_1#6",
        "O.Cryp_4#8",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Signature via ecdh ephemeral-static [one time usage]"
    )
    private fun decryptAndVerifyToken(token: String): String {
        val decryptionKey = createDecryptionKey()
        val verificationKey = createVerificationKey()

        // Decrypt the JWE token
        val jwe = JsonWebStructure.fromCompactSerialization(token) as JsonWebEncryption
        jwe.key = decryptionKey
        val compactJws = jwe.payload

        // Verify the JWS signature
        val jws = JsonWebStructure.fromCompactSerialization(compactJws)
        jws.key = verificationKey

        return jws.payload
    }

    /**
     * Creates the AES decryption key from the configured API key.
     */
    private fun createDecryptionKey(): SecretKey {
        val decryptionKeyBytes = Base64.decode(BuildKonfig.INTEGRITY_API_KEY, Base64.DEFAULT)
        return SecretKeySpec(decryptionKeyBytes, AES)
    }

    /**
     * Creates the EC public key for signature verification.
     */
    private fun createVerificationKey(): PublicKey {
        val encodedVerificationKey = Base64.decode(BuildKonfig.INTEGRITY_VERIFICATION_KEY, Base64.DEFAULT)
        return KeyFactory.getInstance(EC)
            .generatePublic(X509EncodedKeySpec(encodedVerificationKey))
    }

    /**
     * Verifies if the device meets integrity requirements.
     */
    private fun verifyDeviceIntegrity(payload: String): Boolean {
        val payloadJson = JSONObject(payload)
        val deviceIntegrity = payloadJson.optJSONObject(DEVICE_INTEGRITY)
            ?: return false

        return deviceIntegrity.toString().contains(MEETS_DEVICE_INTEGRITY)
    }

    /**
     * Creates nonce data including app identifier and timestamp.
     */
    private fun createNonceData(): ByteArray =
        "GmtkEPrescriptionApp: ${System.currentTimeMillis()}".toByteArray()

    /**
     * Provides a cryptographically secure random salt.
     */
    private fun provideSalt(): ByteArray = ByteArray(REQUIRED_SALT_LENGTH).apply {
        secureRandomInstance().nextBytes(this)
    }

    /**
     * Generates a SHA-256 hash of the combined nonce data and salt.
     */
    private fun generateNonce(salt: ByteArray, word: ByteArray): String =
        MessageDigest.getInstance(SHA_256)
            .digest(word + salt)
            .toLowerCaseHex()
            .decodeToString()
}
