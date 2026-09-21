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

package de.gematik.ti.erp.app.database.room.v2.idp

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import kotlinx.datetime.Instant

@Requirement(
    "A_20741#2",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Downloaded discovery document is saved in the database."
)
@Entity(
    tableName = "idpConfiguration"
)
@TypeConverters(InstantConverter::class)
data class IdpConfigurationEntity(
    @PrimaryKey
    val id: Long = 0L,

    val authorizationEndpoint: String?,
    val ssoEndpoint: String?,
    val tokenEndpoint: String?,
    val pairingEndpoint: String?,
    val authenticationEndpoint: String?,
    val pukIdpEncEndpoint: String?,
    val pukIdpSigEndpoint: String?,

    val certificateX509: ByteArray?,

    val expirationTimestamp: Instant?,
    val issueTimestamp: Instant?,

    val federationAuthorizationIDsEndpoint: String? = null,
    val federationAuthorizationEndpoint: String? = null,
    val externalAuthorizationIDsEndpoint: String? = null,
    val thirdPartyAuthorizationEndpoint: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IdpConfigurationEntity

        if (id != other.id) return false
        if (authorizationEndpoint != other.authorizationEndpoint) return false
        if (ssoEndpoint != other.ssoEndpoint) return false
        if (tokenEndpoint != other.tokenEndpoint) return false
        if (pairingEndpoint != other.pairingEndpoint) return false
        if (authenticationEndpoint != other.authenticationEndpoint) return false
        if (pukIdpEncEndpoint != other.pukIdpEncEndpoint) return false
        if (pukIdpSigEndpoint != other.pukIdpSigEndpoint) return false
        if (!certificateX509.contentEquals(other.certificateX509)) return false
        if (expirationTimestamp != other.expirationTimestamp) return false
        if (issueTimestamp != other.issueTimestamp) return false
        if (federationAuthorizationIDsEndpoint != other.federationAuthorizationIDsEndpoint) return false
        if (federationAuthorizationEndpoint != other.federationAuthorizationEndpoint) return false
        if (externalAuthorizationIDsEndpoint != other.externalAuthorizationIDsEndpoint) return false
        if (thirdPartyAuthorizationEndpoint != other.thirdPartyAuthorizationEndpoint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + authorizationEndpoint.hashCode()
        result = 31 * result + ssoEndpoint.hashCode()
        result = 31 * result + tokenEndpoint.hashCode()
        result = 31 * result + pairingEndpoint.hashCode()
        result = 31 * result + authenticationEndpoint.hashCode()
        result = 31 * result + pukIdpEncEndpoint.hashCode()
        result = 31 * result + pukIdpSigEndpoint.hashCode()
        result = 31 * result + certificateX509.contentHashCode()
        result = 31 * result + expirationTimestamp.hashCode()
        result = 31 * result + issueTimestamp.hashCode()
        result = 31 * result + (federationAuthorizationIDsEndpoint?.hashCode() ?: 0)
        result = 31 * result + (federationAuthorizationEndpoint?.hashCode() ?: 0)
        result = 31 * result + (externalAuthorizationIDsEndpoint?.hashCode() ?: 0)
        result = 31 * result + (thirdPartyAuthorizationEndpoint?.hashCode() ?: 0)
        return result
    }
}
