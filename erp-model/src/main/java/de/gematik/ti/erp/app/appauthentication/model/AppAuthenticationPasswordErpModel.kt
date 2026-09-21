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

package de.gematik.ti.erp.app.appauthentication.model

import de.gematik.ti.erp.app.Requirement
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.security.SecureRandom

@Serializable
data class AppAuthenticationPasswordErpModel(
    val hash: ByteArray,
    val salt: ByteArray
) {
    companion object {
        @Requirement(
            "O.Pass_5#1",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Implementation of hashed password with salt as strong secure random value"
        )
        fun fromPassword(password: String): AppAuthenticationPasswordErpModel {
            val salt = ByteArray(32).apply { secureRandomInstance().nextBytes(this) }
            val hash = hashWithSalt(password, salt)
            return AppAuthenticationPasswordErpModel(hash, salt)
        }

        @Requirement(
            "O.Pass_5#2",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "one-way hash function that take arbitrary-sized data and " +
                "output a fixed-length hash value."
        )
        private fun hashWithSalt(password: String, salt: ByteArray): ByteArray {
            val combined = password.toByteArray() + salt
            return MessageDigest.getInstance("SHA-256").digest(combined)
        }
    }

    fun isValid(password: String): Boolean {
        val hash = hashWithSalt(password, salt)
        return hash.contentEquals(this.hash)
    }
}

@Requirement(
    "O.Rand_1#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Generation of random values by secure random generator specified in FIPS 140-2, " +
        "Security Requirements for Cryptographic Modules, section 4.9.1."
)
private fun secureRandomInstance(): SecureRandom =
    SecureRandom.getInstanceStrong()
