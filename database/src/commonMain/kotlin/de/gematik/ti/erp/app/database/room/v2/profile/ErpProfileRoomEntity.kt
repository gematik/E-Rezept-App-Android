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
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.gematik.ti.erp.app.database.room.v2.task.ErpTaskEntity
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import kotlinx.datetime.Instant

@Entity(
    tableName = "profiles",
    foreignKeys = [
        ForeignKey( // organization
            entity = ErpTaskEntity::class,
            parentColumns = ["taskId"],
            childColumns = ["taskId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("insuranceId"),
        Index("insuranceType")
    ]
)
@TypeConverters(InstantConverter::class)
data class ErpProfileEntity(
    @PrimaryKey
    val profileId: String, // unique profile ID
    val taskId: String, // unique profile ID

    val created: Instant, // creation timestamp
    val lastAuthenticated: Instant, // last login/auth time

    val name: String,
    val familyName: String,
    val givenName: String,

    val image: String, // image URL/path
    val userImage: ByteArray?, // raw binary image data

    val hikePkvConsentDrawer: Boolean, // consent flag
    val color: String, // UI theme color or accent
    val insurance: String, // insurance display name
    val insuranceId: String, // e.g. KVNR or insurer key
    val insuranceType: String, // e.g. GKV, PKV, etc.
    val isNew: Boolean,
    @Embedded() val auth: AuthEntityEmbeddable,
    /*
    auth_isGid: String,
    auth_insuranceId: String,
    auth_insuranceName: String,
    auth_logoUrl: String,
     */
    @Embedded() val idp: IdpEntityEmbeddable
)
