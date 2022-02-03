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

package de.gematik.ti.erp.app.db.daos

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.Update
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.AuditEventWithMedicationText
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.MedicationDispenseSimple
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.db.entities.TaskWithMedicationDispense
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Dao
interface TaskDao {

    @Query("SELECT * from tasks WHERE profileName = :profileName ORDER BY authoredOn DESC")
    fun getAllTasks(profileName: String): Flow<List<Task>>

    @Query("SELECT taskId FROM tasks WHERE profileName = :profileName")
    suspend fun getAllTasksWithTaskIdOnly(profileName: String): List<String>

    @Query("SELECT taskId FROM tasks")
    suspend fun getAllTasksWithTaskIdOnly(): List<String>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(value = "SELECT taskId, profileName, accessCode, lastModified, organization, medicationText, expiresOn, acceptUntil, authoredOn, scannedOn, scanSessionEnd, nrInScanSession, scanSessionName, redeemedOn, status FROM tasks WHERE profileName = :profileName AND scannedOn IS NULL")
    fun getSyncedTasksWithoutBundle(profileName: String): Flow<List<Task>>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT taskId, profileName, accessCode, lastModified, organization, medicationText, expiresOn, acceptUntil, authoredOn, scannedOn, scanSessionEnd, nrInScanSession, scanSessionName, redeemedOn FROM tasks WHERE profileName = :profileName AND scannedOn IS NOT NULL")
    fun getScannedTasksWithoutBundle(profileName: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleTasks(vararg task: Task)

    @Transaction
    suspend fun insertTask(task: Task) {
        if (insertTaskIgnore(task) == -1L) {
            insertTaskUpdate(task)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskIgnore(task: Task): Long

    @Update
    suspend fun insertTaskUpdate(task: Task)

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditEvents(vararg auditEvent: AuditEventSimple)

    @Query("SELECT * FROM auditEvents WHERE taskId = :taskId AND locale = :locale ORDER BY timestamp DESC LIMIT 50")
    fun getAuditEventsInGivenLanguage(taskId: String, locale: String): Flow<List<AuditEventSimple>>

    @Query(
        "SELECT t.medicationText, ae.timestamp, ae.text " +
            "FROM auditEvents as ae LEFT JOIN tasks as t ON t.taskId = ae.taskId " +
            "WHERE ae.profileName = :profileName ORDER BY timestamp DESC"
    )
    fun getAuditEventsForProfileName(profileName: String): DataSource.Factory<Int, AuditEventWithMedicationText>

    @Query("SELECT timestamp FROM auditEvents ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestAuditEventTimeStamp(): OffsetDateTime

    @Query("SELECT timestamp FROM auditEvents WHERE profileName = :profileName ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestAuditEventTimeStamp(profileName: String): OffsetDateTime?

    @Query("SELECT * FROM lowDetailEvents WHERE taskId = :taskId")
    fun getLowDetailEvents(taskId: String): Flow<List<LowDetailEventSimple>>

    @Query("DELETE FROM lowDetailEvents WHERE taskId = :taskId")
    fun deleteLowDetailEvents(taskId: String)

    @Query("SELECT * FROM tasks WHERE profileName = :profileName AND redeemedOn = :redeemedOn")
    fun loadTasksForRedeemedOn(redeemedOn: OffsetDateTime, profileName: String): Flow<List<Task>>
}
