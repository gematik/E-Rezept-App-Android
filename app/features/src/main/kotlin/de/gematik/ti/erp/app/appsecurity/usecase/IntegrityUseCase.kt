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
        val salt = provideSalt()
        val ourNonce = generateNonce(salt, nonceData())

        val integrityManager = IntegrityManagerFactory.create(context)

        val tokenResponse = integrityManager.requestIntegrityToken(
            IntegrityTokenRequest.builder()
                .setCloudProjectNumber(BuildKonfig.CLOUD_PROJECT_NUMBER.toLong())
                .setNonce(ourNonce)
                .build()
        ).await()

        val token = tokenResponse.token()

        @Requirement(
            "O.Cryp_1#6",
            "O.Cryp_4#8",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Signature via ecdh ephemeral-static [one time usage]"
        )
        val decryptionKeyBytes: ByteArray = Base64.decode(BuildKonfig.INTEGRITY_API_KEY, Base64.DEFAULT)

        val decryptionKey: SecretKey = SecretKeySpec(decryptionKeyBytes, AES)

        val encodedVerificationKey: ByteArray = Base64.decode(BuildKonfig.INTEGRITY_VERIFICATION_KEY, Base64.DEFAULT)

        val verificationKey: PublicKey = KeyFactory.getInstance(EC)
            .generatePublic(X509EncodedKeySpec(encodedVerificationKey))

        val jwe: JsonWebEncryption = JsonWebStructure.fromCompactSerialization(token) as JsonWebEncryption

        jwe.key = decryptionKey

        val compactJws = jwe.payload

        val jws = JsonWebStructure.fromCompactSerialization(compactJws)

        jws.key = verificationKey

        val requestDetails = JSONObject(jws.payload).getJSONObject(DEVICE_INTEGRITY)

        emit(requestDetails.toString().contains(MEETS_DEVICE_INTEGRITY))
    }.catch {
        Napier.e { "integrity check not done" }
        emit(true)
    }

    private fun nonceData() = ("GmtkEPrescriptionApp: " + System.currentTimeMillis()).toByteArray()

    private fun provideSalt() = ByteArray(REQUIRED_SALT_LENGTH).apply {
        secureRandomInstance().nextBytes(this)
    }

    private fun generateNonce(salt: ByteArray, word: ByteArray): String =
        MessageDigest.getInstance(SHA_256).digest((word + salt)).toLowerCaseHex().decodeToString()
}
