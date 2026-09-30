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

package de.gematik.ti.erp.app.database.room.v2.task.communication

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel
import de.gematik.ti.erp.app.database.room.v2.profile.ProfileEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskEntity
import de.gematik.ti.erp.app.database.room.v2.task.util.CommunicationPayloadConverter
import de.gematik.ti.erp.app.database.room.v2.task.util.CommunicationProfileConverter
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import kotlinx.datetime.Instant

@Entity(
    tableName = "communications",
    foreignKeys = [
        ForeignKey(
            entity = ErpTaskEntity::class,
            parentColumns = ["taskId"],
            childColumns = ["taskId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["identifier"], // TODO: Namings should be same
            childColumns = ["profileId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("orderId"),
        Index("taskId"),
        Index("profile"),
        Index("insuranceId"),
        Index("profileId"),
        Index("communicationId")
    ]
)
@TypeConverters(InstantConverter::class, CommunicationProfileConverter::class, CommunicationPayloadConverter::class)
data class ErpCommunicationEntity(
    @PrimaryKey val communicationId: String,
    val orderId: String,
    val taskId: String,
    val profileId: String,
    val telematikId: String,
    val kvnr: String,
    val consumed: Boolean,
    val payload: CommunicationPayloadErpModel? = null,
    // Transitional compatibility column introduced in Room migration 11->12 and kept through 12->13
    // so app updates from older builds can preserve legacy communication payload data safely.
    val payload_structured: String? = null,
    val profile: CommunicationProfileV1,
    var recipient: String = "",
    // TODO CommResV3 CleanUp of Migration: DB Insurance is the wrong name here, should be insurant or profileId
    val insuranceId: String? = null,
    val timeStamp: Instant,
    val pharmacyName: String? = null
)
