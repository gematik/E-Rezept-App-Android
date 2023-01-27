/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.idp.model

import org.bouncycastle.cert.X509CertificateHolder
import org.jose4j.base64url.Base64Url
import org.jose4j.jwx.JsonWebStructure
import java.time.Duration
import java.time.Instant

object IdpData {
    data class IdpConfiguration(
        var authorizationEndpoint: String,
        var ssoEndpoint: String,
        var tokenEndpoint: String,
        var pairingEndpoint: String,
        var authenticationEndpoint: String,
        var pukIdpEncEndpoint: String,
        var pukIdpSigEndpoint: String,
        var certificate: X509CertificateHolder,
        var expirationTimestamp: Instant,
        var issueTimestamp: Instant,
        var externalAuthorizationIDsEndpoint: String?,
        var thirdPartyAuthorizationEndpoint: String?
    )

    data class SingleSignOnToken(
        val token: String,
        val expiresOn: Instant = extractExpirationTimestamp(token),
        val validOn: Instant = extractValidOnTimestamp(token)
    ) {
        fun isValid(instant: Instant = Instant.now()) =
            instant < expiresOn && instant >= validOn
    }

    sealed interface SingleSignOnTokenScope {
        val token: SingleSignOnToken?
    }

    sealed interface TokenWithHealthCardScope : SingleSignOnTokenScope {
        val cardAccessNumber: String
        val healthCardCertificate: X509CertificateHolder
    }

    sealed interface TokenWithKeyStoreAliasScope : TokenWithHealthCardScope {
        val aliasOfSecureElementEntry: ByteArray

        fun aliasOfSecureElementEntryBase64(): String =
            Base64Url.encode(aliasOfSecureElementEntry) // url safe for compatibility with response from idp backend
    }

    data class DefaultToken(
        override val token: SingleSignOnToken?,
        override val cardAccessNumber: String,
        override val healthCardCertificate: X509CertificateHolder
    ) : TokenWithHealthCardScope {
        constructor(
            token: SingleSignOnToken?,
            cardAccessNumber: String,
            healthCardCertificate: ByteArray
        ) : this(
            token = token,
            cardAccessNumber = cardAccessNumber,
            healthCardCertificate = X509CertificateHolder(healthCardCertificate)
        )
    }

    data class ExternalAuthenticationToken(
        override val token: SingleSignOnToken?,
        val authenticatorId: String,
        val authenticatorName: String
    ) : SingleSignOnTokenScope

    data class AlternateAuthenticationToken(
        override val token: SingleSignOnToken?,
        override val cardAccessNumber: String,
        override val aliasOfSecureElementEntry: ByteArray,
        override val healthCardCertificate: X509CertificateHolder
    ) : TokenWithHealthCardScope, TokenWithKeyStoreAliasScope {
        constructor(
            token: SingleSignOnToken?,
            cardAccessNumber: String,
            aliasOfSecureElementEntry: ByteArray,
            healthCardCertificate: ByteArray
        ) : this(
            token = token,
            cardAccessNumber = cardAccessNumber,
            aliasOfSecureElementEntry = aliasOfSecureElementEntry,
            healthCardCertificate = X509CertificateHolder(healthCardCertificate)
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AlternateAuthenticationToken

            if (token != other.token) return false
            if (cardAccessNumber != other.cardAccessNumber) return false
            if (!aliasOfSecureElementEntry.contentEquals(other.aliasOfSecureElementEntry)) return false
            if (healthCardCertificate != other.healthCardCertificate) return false

            return true
        }

        override fun hashCode(): Int {
            var result = token?.hashCode() ?: 0
            result = 31 * result + cardAccessNumber.hashCode()
            result = 31 * result + aliasOfSecureElementEntry.contentHashCode()
            result = 31 * result + healthCardCertificate.hashCode()
            return result
        }
    }

    data class AlternateAuthenticationWithoutToken(
        override val cardAccessNumber: String,
        override val aliasOfSecureElementEntry: ByteArray,
        override val healthCardCertificate: X509CertificateHolder
    ) : TokenWithHealthCardScope, TokenWithKeyStoreAliasScope {
        override val token: SingleSignOnToken? = null

        constructor(
            cardAccessNumber: String,
            aliasOfSecureElementEntry: ByteArray,
            healthCardCertificate: ByteArray
        ) : this(
            cardAccessNumber = cardAccessNumber,
            aliasOfSecureElementEntry = aliasOfSecureElementEntry,
            healthCardCertificate = X509CertificateHolder(healthCardCertificate)
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AlternateAuthenticationWithoutToken

            if (cardAccessNumber != other.cardAccessNumber) return false
            if (!aliasOfSecureElementEntry.contentEquals(other.aliasOfSecureElementEntry)) return false
            if (healthCardCertificate != other.healthCardCertificate) return false
            if (token != other.token) return false

            return true
        }

        override fun hashCode(): Int {
            var result = cardAccessNumber.hashCode()
            result = 31 * result + aliasOfSecureElementEntry.contentHashCode()
            result = 31 * result + healthCardCertificate.hashCode()
            result = 31 * result + (token?.hashCode() ?: 0)
            return result
        }
    }

    data class AuthenticationData(
        val singleSignOnTokenScope: SingleSignOnTokenScope?
    )
}

fun extractExpirationTimestamp(ssoToken: String): Instant =
    Instant.ofEpochSecond(
        JsonWebStructure
            .fromCompactSerialization(ssoToken)
            .headers
            .getLongHeaderValue("exp")
    )

fun extractValidOnTimestamp(ssoToken: String): Instant =
    extractExpirationTimestamp(ssoToken) - Duration.ofHours(24)
