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

package de.gematik.ti.erp.app.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.MedicationDispenseSimple
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.db.entities.TaskWithMedicationDispense
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY authoredOn DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT taskId FROM tasks")
    suspend fun getAllTasksWithTaskIdOnly(): List<String>

    @Query("SELECT taskId, accessCode, lastModified, organization, medicationText, expiresOn, authoredOn, scannedOn, scanSessionEnd, nrInScanSession, scanSessionName, redeemedOn FROM tasks")
    fun getAllTasksWithoutBundle(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleTasks(vararg task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(vararg task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditEvents(vararg auditEvent: AuditEventSimple)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationDispenses(medicationDispense: MedicationDispenseSimple)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLowDetailEvent(vararg lowDetailEvent: LowDetailEventSimple)

    @Query("DELETE FROM tasks WHERE taskId IN (:taskId)")
    suspend fun deleteMultipleTasksByTaskId(vararg taskId: String)

    @Query("DELETE FROM auditEvents WHERE taskId = :taskId")
    suspend fun deleteAuditEvents(taskId: String)

    @Transaction
    @Query("SELECT * FROM tasks WHERE taskID = :taskId")
    fun getTaskWithMedicationDispenseForTaskId(taskId: String): Flow<TaskWithMedicationDispense>

    @Query("SELECT * FROM tasks WHERE taskID IN (:taskIds)")
    fun getTasksForTaskId(vararg taskIds: String): Flow<List<Task>>

    @Query("DELETE FROM tasks WHERE taskId = :taskId")
    suspend fun deleteTaskByTaskId(taskId: String)

    @Query("UPDATE tasks SET redeemedOn = :redeemed WHERE scanSessionEnd IN (SELECT scanSessionEnd from tasks WHERE taskId IN (:taskIds) )")
    suspend fun updateRedeemedOnForAllTasks(taskIds: List<String>, redeemed: OffsetDateTime?)

    @Query("UPDATE tasks SET redeemedOn = :redeemed WHERE taskId = :taskId")
    suspend fun updateRedeemedOnForSingleTask(taskId: String, redeemed: OffsetDateTime?)

    @Query("UPDATE tasks SET scanSessionName = :name WHERE scanSessionEnd = :scanSessionEnd")
    fun updateScanSessionName(name: String?, scanSessionEnd: OffsetDateTime)

    @Query("SELECT * FROM auditEvents WHERE taskId = :taskId AND locale = :locale")
    fun getAuditEventsInGivenLanguage(taskId: String, locale: String): Flow<List<AuditEventSimple>>

    @Query("SELECT * FROM lowDetailEvents WHERE taskId = :taskId")
    fun getLowDetailEvents(taskId: String): Flow<List<LowDetailEventSimple>>

//    @Query("SELECT * FROM medicationDispense WHERE taskId = :taskId")
//    fun loadMedicationDispense(taskId: String): Flow<MedicationDispenseSimple>

    @Query("DELETE FROM lowDetailEvents WHERE taskId = :taskId")
    fun deleteLowDetailEvents(taskId: String)

    @Query("SELECT * FROM tasks WHERE redeemedOn = :redeemedOn")
    fun loadTasksForRedeemedOn(redeemedOn: OffsetDateTime): Flow<List<Task>>
}
