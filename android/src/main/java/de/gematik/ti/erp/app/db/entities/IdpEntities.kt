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

package de.gematik.ti.erp.app.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.bouncycastle.cert.X509CertificateHolder
import java.time.Instant

@Entity(tableName = "idpConfiguration")
data class IdpConfiguration(
    val authorizationEndpoint: String,
    val ssoEndpoint: String,
    val tokenEndpoint: String,
    val pairingEndpoint: String,
    val authenticationEndpoint: String,
    val pukIdpEncEndpoint: String,
    val pukIdpSigEndpoint: String,
    val certificate: X509CertificateHolder,
    val expirationTimestamp: Instant,
    val issueTimestamp: Instant,
    val externalAuthorizationIDsEndpoint: String?,
    val thirdPartyAuthorizationEndpoint: String?
) {
    @PrimaryKey
    var id: Int = 0
}

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = arrayOf("name"),
            childColumns = arrayOf("profileName"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    tableName = "idpAuthenticationDataEntity"
)
data class IdpAuthenticationDataEntity(
    @PrimaryKey
    val profileName: String,
    val singleSignOnToken: String? = null,
    val singleSignOnTokenScope: SingleSignOnTokenScope? = null,
    val singleSignOnTokenExpiresOn: Instant? = null,
    val singleSignOnTokenValidOn: Instant? = null,
    val cardAccessNumber: String? = null,
    val healthCardCertificate: ByteArray? = null,
    val aliasOfSecureElementEntry: ByteArray? = null
) {
    enum class SingleSignOnTokenScope {
        Default,
        AlternateAuthentication
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IdpAuthenticationDataEntity

        if (singleSignOnToken != other.singleSignOnToken) return false
        if (singleSignOnTokenScope != other.singleSignOnTokenScope) return false
        if (healthCardCertificate != null) {
            if (other.healthCardCertificate == null) return false
            if (!healthCardCertificate.contentEquals(other.healthCardCertificate)) return false
        } else if (other.healthCardCertificate != null) return false
        if (aliasOfSecureElementEntry != null) {
            if (other.aliasOfSecureElementEntry == null) return false
            if (!aliasOfSecureElementEntry.contentEquals(other.aliasOfSecureElementEntry)) return false
        } else if (other.aliasOfSecureElementEntry != null) return false
        if (profileName != other.profileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = singleSignOnToken?.hashCode() ?: 0
        result = 31 * result + (singleSignOnTokenScope?.hashCode() ?: 0)
        result = 31 * result + (healthCardCertificate?.contentHashCode() ?: 0)
        result = 31 * result + (aliasOfSecureElementEntry?.contentHashCode() ?: 0)
        result = 31 * result + (profileName.hashCode())
        return result
    }
}
