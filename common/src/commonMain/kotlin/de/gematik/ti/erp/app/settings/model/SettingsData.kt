/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.settings.model

import de.gematik.ti.erp.app.secureRandomInstance
import java.security.MessageDigest
import java.time.Instant

object SettingsData {
    data class General(
        val latestAppVersion: AppVersion,
        val onboardingShownIn: AppVersion?,
        val dataProtectionVersionAcceptedOn: Instant,
        val zoomEnabled: Boolean,
        val userHasAcceptedInsecureDevice: Boolean,
        val authenticationFails: Int
    )

    data class AppVersion(
        val code: Int,
        val name: String
    )

    data class PharmacySearch(
        val name: String,
        val locationEnabled: Boolean,
        val ready: Boolean = false,
        val deliveryService: Boolean = false,
        val onlineService: Boolean = false,
        val openNow: Boolean = false
    ) {
        fun isAnySet(): Boolean =
            ready || deliveryService || onlineService || openNow
    }

    sealed class AuthenticationMode {
        object DeviceSecurity : AuthenticationMode()
        class Password : AuthenticationMode {
            val hash: ByteArray
            val salt: ByteArray

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

            private fun hashWithSalt(password: String, salt: ByteArray): ByteArray {
                val combined = password.toByteArray() + salt
                return MessageDigest.getInstance("SHA-256").digest(combined)
            }
        }

        object Unspecified : AuthenticationMode()

        @Deprecated("replaced by deviceSecurity")
        object Biometrics : AuthenticationMode()

        @Deprecated("replaced by deviceSecurity")
        object DeviceCredentials : AuthenticationMode()

        @Deprecated("not available anymore")
        object None : AuthenticationMode()
    }
}
