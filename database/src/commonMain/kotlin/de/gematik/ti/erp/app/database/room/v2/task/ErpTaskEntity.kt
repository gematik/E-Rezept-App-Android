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

package de.gematik.ti.erp.app.database.room.v2.task

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.gematik.ti.erp.app.database.realm.v1.task.entity.TaskStatusV1
import de.gematik.ti.erp.app.database.room.v2.task.communication.ErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.organization.ErpOrganizationEntity
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import de.gematik.ti.erp.app.database.room.v2.task.util.TaskStatusConverter
import kotlinx.datetime.Instant

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey( // organization
            entity = ErpOrganizationEntity::class,
            parentColumns = ["organizationId"],
            childColumns = ["organizationId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey( // practitioner
            entity = ErpPractitionerEntity::class,
            parentColumns = ["practitionerId"],
            childColumns = ["practitionerId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey( // patient
            entity = ErpPatientEntity::class,
            parentColumns = ["patientId"],
            childColumns = ["patientId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),

        ForeignKey( // patient
            entity = ErpMultiplePrescriptionEntity::class,
            parentColumns = ["multiplePrescriptionId"],
            childColumns = ["multiplePrescriptionId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey( // accident_info
            entity = ErpAccidentInfoEntity::class,
            parentColumns = ["accidentInfoId"],
            childColumns = ["accidentInfoId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),

        ForeignKey( // insurance information
            entity = ErpTaskMedicationDeviceRequestEntity::class,
            parentColumns = ["deviceRequestId"],
            childColumns = ["deviceRequestId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey( // medication request
            entity = ErpMedicationEntity::class,
            parentColumns = ["medicationId"],
            childColumns = ["medicationId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),

        ForeignKey( // parent profile (back reference)
            entity = ErpCommunicationEntity::class,
            parentColumns = ["communicationId"],
            childColumns = ["communicationId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("organizationId"),
        Index("practitionerId"),
        Index("patientId"),
        Index("insuranceInformationId"),
        Index("deviceRequestId"),
        Index("parentProfileId"),
        Index(value = ["accessCode"]),
        Index(value = ["status"]),
        Index(value = ["lastModified"])
    ]
)
@TypeConverters(InstantConverter::class, TaskStatusConverter::class)
data class ErpTaskEntity(
    @PrimaryKey
    val taskId: String,
    // FKs for KBV bundle entities
    val organizationId: String?,
    val practitionerId: String?,
    val patientId: String?,
    val insuranceInformationId: String?,

    // Enum stored as String via TypeConverter
    val status: TaskStatusV1 = TaskStatusV1.Other,

    // Optional FKs
    val medicationId: String?,
    val accidentInfoId: String?,
    val deviceRequestId: String?,
    val multiplePrescriptionId: String?,
    val communicationId: String?,

    // Back reference to owning profile
    val parentProfileId: String?,

    val acceptedUntil: Instant?,
    val lastModified: Instant?,
    val authoredOn: Instant,
    val expiresOn: Instant?,
    val lastMedicationDispense: Instant?,
    val accessCode: String, //
    val author: String,
    val bvg: Boolean,
    val coPaymentStatus: String,
    val dispenseValidityEnd: String,
    val dosageInstruction: String,
    val flowType: String,
    val fullUrl: String,
    val identifier: String,
    val noctuFeeWaiver: String,
    val prescriptionId: String,
    val redeemedOn: String,
    val source: String,
    val substitutionAllowed: String
)
