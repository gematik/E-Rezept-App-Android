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

package de.gematik.ti.erp.app.userauthentication.model

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.serialization.Serializable

@Serializable
sealed interface UserAuthenticationErpModel {
    companion object {
        fun UserAuthenticationErpModel.toJson(): String = SafeJson.value.encodeToString(this)
    }
    val singleSignOnTokenErpModel: SingleSignOnTokenErpModel?

    @Serializable
    data class HealthCard(
        override val singleSignOnTokenErpModel: SingleSignOnTokenErpModel?,
        val cardAccessNumber: String,
        val healthCardCertificate: ByteArray
    ) : UserAuthenticationErpModel {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HealthCard

            if (singleSignOnTokenErpModel != other.singleSignOnTokenErpModel) return false
            if (cardAccessNumber != other.cardAccessNumber) return false
            if (!healthCardCertificate.contentEquals(other.healthCardCertificate)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = singleSignOnTokenErpModel.hashCode()
            result = 31 * result + cardAccessNumber.hashCode()
            result = 31 * result + healthCardCertificate.contentHashCode()
            return result
        }
    }

    @Serializable
    data class HealthCardWithSavedCredentials(
        override val singleSignOnTokenErpModel: SingleSignOnTokenErpModel?, // can be with or without token
        val cardAccessNumber: String,
        val healthCardCertificate: ByteArray,
        val aliasOfSecureElementEntry: ByteArray
    ) : UserAuthenticationErpModel {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HealthCardWithSavedCredentials

            if (singleSignOnTokenErpModel != other.singleSignOnTokenErpModel) return false
            if (cardAccessNumber != other.cardAccessNumber) return false
            if (!healthCardCertificate.contentEquals(other.healthCardCertificate)) return false
            if (!aliasOfSecureElementEntry.contentEquals(other.aliasOfSecureElementEntry)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = singleSignOnTokenErpModel?.hashCode() ?: 0
            result = 31 * result + cardAccessNumber.hashCode()
            result = 31 * result + healthCardCertificate.contentHashCode()
            result = 31 * result + aliasOfSecureElementEntry.hashCode()
            return result
        }
    }

    @Serializable
    data class External(
        override val singleSignOnTokenErpModel: SingleSignOnTokenErpModel?,
        var externalAuthenticatorId: String,
        var externalAuthenticatorName: String
    ) : UserAuthenticationErpModel

    @Serializable
    data object NotInitialized : UserAuthenticationErpModel {
        override val singleSignOnTokenErpModel: SingleSignOnTokenErpModel?
            get() = null
    }
}
