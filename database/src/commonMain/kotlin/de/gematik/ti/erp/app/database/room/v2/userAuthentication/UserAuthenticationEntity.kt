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

package de.gematik.ti.erp.app.database.room.v2.userAuthentication

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import de.gematik.ti.erp.app.database.room.v2.profile.ProfileEntity
import de.gematik.ti.erp.app.userauthentication.model.SingleSignOnTokenErpModel

@Entity(
    tableName = "userAuthentication",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["identifier"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]

)
data class UserAuthenticationEntity(
    @PrimaryKey
    val profileId: String, // Foreign Key to ErpProfileEntity.profileId
    val singleSignOnToken: String?,
    @Embedded val healthCardAuthenticationEntity: HealthCardAuthenticationEntity,
    @Embedded val externalAuthenticationEntity: ExternalAuthenticationEntity
) {
    fun isSingleSignOnTokenValid() = singleSignOnToken?.let { SingleSignOnTokenErpModel(singleSignOnToken).isValid() } ?: false
}

data class HealthCardAuthenticationEntity(
    val cardAccessNumber: String?,
    val healthCardCertificate: ByteArray?,
    val aliasOfSecureElementEntry: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HealthCardAuthenticationEntity

        if (cardAccessNumber != other.cardAccessNumber) return false
        if (!healthCardCertificate.contentEquals(other.healthCardCertificate)) return false
        if (!aliasOfSecureElementEntry.contentEquals(other.aliasOfSecureElementEntry)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cardAccessNumber?.hashCode() ?: 0
        result = 31 * result + (healthCardCertificate?.contentHashCode() ?: 0)
        result = 31 * result + (aliasOfSecureElementEntry?.contentHashCode() ?: 0)
        return result
    }
}

data class ExternalAuthenticationEntity(
    val externalAuthenticatorId: String?,
    val externalAuthenticatorName: String?
)
