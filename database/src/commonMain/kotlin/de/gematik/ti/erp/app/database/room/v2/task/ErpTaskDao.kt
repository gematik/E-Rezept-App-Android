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

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import de.gematik.ti.erp.app.database.room.v2.task.communication.ErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.organization.ErpOrganizationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ErpTaskDao {
    @Upsert
    suspend fun upsertAll(items: List<ErpTaskEntity>)

    @Query("SELECT * FROM tasks WHERE taskId = :profileId")
    suspend fun getByProfile(profileId: String): List<ErpTaskEntity>

    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getByTaskId(taskId: String): ErpTaskEntity?

    @Query("DELETE FROM tasks WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: String)

    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}

data class ErpTaskWithRefs(
    @Embedded val task: ErpTaskEntity,

    @Relation(
        parentColumn = "organizationId",
        entityColumn = "organizationId"
    )
    val organization: ErpOrganizationEntity?,

    @Relation(
        parentColumn = "practitionerId",
        entityColumn = "practitionerId"
    )
    val practitioner: ErpPractitionerEntity?,

    @Relation(
        parentColumn = "patientId",
        entityColumn = "patientId"
    )
    val patient: ErpPatientEntity?,

    @Relation(
        parentColumn = "medicationId",
        entityColumn = "medicationId"
    )
    val medication: ErpMedicationEntity?,

    @Relation(
        parentColumn = "deviceRequestId",
        entityColumn = "deviceRequestId"
    )
    val deviceRequest: ErpTaskMedicationDeviceRequestEntity?,

    @Relation(
        parentColumn = "multiplePrescriptionId",
        entityColumn = "multiplePrescriptionId"
    )
    val multiplePrescription: ErpMultiplePrescriptionEntity?,

    @Relation(
        parentColumn = "accidentInfoId",
        entityColumn = "accidentInfoId"
    )
    val accidentInfo: ErpAccidentInfoEntity?,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val communications: List<ErpCommunicationEntity>
)

@Dao
interface ErpTaskWithRefsDao {
    @Transaction
    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    fun observeWithRefs(taskId: String): Flow<ErpTaskWithRefs?>
}
