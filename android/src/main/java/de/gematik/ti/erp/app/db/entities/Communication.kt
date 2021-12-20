/*
 * Copyright (c) 2021 gematik GmbH
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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

const val COMMUNICATION_TYPE_DISP_REQ = "https://gematik.de/fhir/StructureDefinition/ErxCommunicationDispReq"
const val COMMUNICATION_TYPE_REPLY = "https://gematik.de/fhir/StructureDefinition/ErxCommunicationReply"

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = arrayOf("taskId"),
            childColumns = arrayOf("taskId"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = arrayOf("name"),
            childColumns = arrayOf("profileName"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    tableName = "communications"
)
data class Communication(
    @PrimaryKey
    val communicationId: String,
    val profile: CommunicationProfile,
    @ColumnInfo(index = true)
    val profileName: String,
    val time: String,
    @ColumnInfo(index = true)
    val taskId: String,
    val telematicsId: String,
    val kbvUserId: String,
    val payload: String?,
    val consumed: Boolean = false
)

enum class CommunicationProfile {
    ErxCommunicationDispReq, ErxCommunicationReply
}
