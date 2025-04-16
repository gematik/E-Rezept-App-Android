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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.settings.model

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.secureRandomInstance
import java.security.MessageDigest

object SettingsData {
    data class General(
        val latestAppVersion: AppVersion,
        val onboardingShownIn: AppVersion?,
        val welcomeDrawerShown: Boolean,
        val mainScreenTooltipsShown: Boolean,
        val zoomEnabled: Boolean,
        val userHasAcceptedInsecureDevice: Boolean,
        val userHasAcceptedIntegrityNotOk: Boolean,
        val mlKitAccepted: Boolean,
        val trackingAllowed: Boolean,
        val screenShotsAllowed: Boolean
    )

    data class AppVersion(
        val code: Int,
        val name: String
    )

    // is not used anywhere ???
    data class PharmacySearch(
        val name: String,
        val locationEnabled: Boolean,
        val deliveryService: Boolean = false,
        val onlineService: Boolean = false,
        val openNow: Boolean = false
    ) {
        fun isAnySet(): Boolean =
            deliveryService || onlineService || openNow
    }

    @Requirement(
        "O.Auth_7#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "App authentication is done on a domain-specific basis."
    )
    data class Authentication(
        val password: Password?,
        val deviceSecurity: Boolean,
        val failedAuthenticationAttempts: Int
    ) {
        val passwordIsSet: Boolean = password != null
        val methodIsDeviceSecurity: Boolean = deviceSecurity && !passwordIsSet
        val methodIsPassword: Boolean = passwordIsSet && !deviceSecurity
        val methodIsUnspecified: Boolean = !deviceSecurity && !passwordIsSet
        val bothMethodsAvailable: Boolean = deviceSecurity && passwordIsSet
        val showFailedAuthenticationAttemptsError: Boolean = failedAuthenticationAttempts >= 2

        class Password {
            val hash: ByteArray
            val salt: ByteArray

            @Requirement(
                "O.Pass_5#1",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Implementation of hashed password with salt as strong secure random value"
            )
            constructor(password: String) {
                salt = ByteArray(32).apply { secureRandomInstance().nextBytes(this) }
                hash = hashWithSalt(password, salt)
            }

            constructor(hash: ByteArray, salt: ByteArray) {
                this.hash = hash
                this.salt = salt
            }

            fun isValid(password: String): Boolean {
                val hash = hashWithSalt(password, salt)
                return hash.contentEquals(this.hash)
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
    }
}
