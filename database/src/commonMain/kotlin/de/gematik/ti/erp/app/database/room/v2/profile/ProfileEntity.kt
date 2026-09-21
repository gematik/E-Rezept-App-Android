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

package de.gematik.ti.erp.app.database.room.v2.profile

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import de.gematik.ti.erp.app.database.room.v2.userAuthentication.UserAuthenticationEntity
import kotlinx.datetime.Instant

@Entity(
    tableName = "profiles",
    indices = [
        Index("insuranceId"),
        Index("insuranceType")
    ]
)
@TypeConverters(InstantConverter::class)
data class ProfileEntity( // TODO: (Ümüt) Namings should be same as ErpProfileEntity
    @PrimaryKey
    val identifier: String, // unique profile ID // TODO: (Ümüt) Namings should be same

    val isNew: Boolean,
    val active: Boolean,

    val name: String,

    @Embedded val profileImageData: ProfileImageDataEmbeddable,

    @Embedded val insuranceData: ProfileInsuranceDataEmbeddable,
    val showInvoiceConsentDrawer: Boolean,

    val lastAuthenticated: Instant?,
    val lastAuditEventSynced: Instant?,
    val lastTaskSynced: Instant?
)

data class ProfileImageDataEmbeddable(
    val avatar: String,
    val image: ByteArray?,
    val color: String
)

data class ProfileInsuranceDataEmbeddable(
    val insurantName: String?,
    val insuranceName: String?,
    val insuranceId: String?,
    val insuranceType: String,
    val organizationId: String?
)

data class ProfileWithUserAuthenticationEntity(
    @Embedded val profile: ProfileEntity,
    @Relation(
        parentColumn = "identifier",
        entityColumn = "profileId"
    )
    val userAuthentication: UserAuthenticationEntity?
)
