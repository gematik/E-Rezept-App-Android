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

package de.gematik.ti.erp.app.prescription.repository

import android.content.SharedPreferences
import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.MedicationDispenseSimple
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.db.entities.TaskWithMedicationDispense
import de.gematik.ti.erp.app.di.NetworkSecureSharedPreferences
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

private const val AUDIT_SYNC_ERROR_KEY = "AUDIT_SYNC_ERROR"
private const val AUDIT_SYNC_DATE_KEY = "AUDIT_SYNC_DATE"
private const val LAST_MODIFY_TASK_DATE = "LAST_MODIFY_TASK_DATE"

class LocalDataSource @Inject constructor(
    private val db: AppDatabase,
    @NetworkSecureSharedPreferences
    private var securePrefs: SharedPreferences
) {
    suspend fun saveTasks(tasks: List<EntityTask>) {
        db.taskDao().insertMultipleTasks(*tasks.toTypedArray())
    }

    suspend fun saveTask(task: EntityTask) {
        db.taskDao().insertTask(task)
    }

    suspend fun saveCommunications(communications: List<Communication>) {
        db.communicationsDao().insertMultipleCommunications(*communications.toTypedArray())
    }

    suspend fun saveAuditEvents(auditEvents: List<AuditEventSimple>) {
        db.taskDao().insertAuditEvents(*auditEvents.toTypedArray())
        securePrefs.edit()
            .putString(AUDIT_SYNC_ERROR_KEY, null)
            .putLong(AUDIT_SYNC_DATE_KEY, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)).apply()
    }

    suspend fun saveMedicationDispense(medicationDispense: MedicationDispenseSimple) {
        db.taskDao().insertMedicationDispenses(medicationDispense)
    }

    suspend fun saveLowDetailEvent(lowDetailEvent: LowDetailEventSimple) {
        db.taskDao().insertLowDetailEvent(lowDetailEvent)
    }

    fun loadLowDetailEvents(taskId: String): Flow<List<LowDetailEventSimple>> =
        db.taskDao().getLowDetailEvents(taskId)

    fun deleteLowDetailEvents(taskId: String) {
        db.taskDao().deleteLowDetailEvents(taskId)
    }

    fun getAuditSyncError(): Boolean {
        return securePrefs.getBoolean(AUDIT_SYNC_ERROR_KEY, false)
    }

    fun getAuditSyncDate(): LocalDateTime? {
        val storedDate = securePrefs.getLong(AUDIT_SYNC_DATE_KEY, -1)
        if (storedDate == -1L) return null
        return LocalDateTime.ofEpochSecond(storedDate, 0, ZoneOffset.UTC)
    }

    fun storeAuditEventSyncError() {
        securePrefs.edit().putBoolean(AUDIT_SYNC_ERROR_KEY, true).apply()
    }

    fun loadAuditEvents(taskId: String, locale: String): Flow<List<AuditEventSimple>> {
        return db.taskDao().getAuditEventsInGivenLanguage(taskId, locale)
    }

    fun loadTasks(): Flow<List<Task>> {
        return db.taskDao().getAllTasks()
    }

    fun loadScannedTasksWithoutBundle(): Flow<List<Task>> =
        db.taskDao().getScannedTasksWithoutBundle()

    fun loadSyncedTasksWithoutBundle(): Flow<List<Task>> =
        db.taskDao().getSyncedTasksWithoutBundle()

    fun loadTaskWithMedicationDispenseForTaskId(taskId: String): Flow<TaskWithMedicationDispense> {
        return db.taskDao().getTaskWithMedicationDispenseForTaskId(taskId)
    }

    fun loadTasksForTaskId(vararg taskIds: String): Flow<List<Task>> {
        return db.taskDao().getTasksForTaskId(*taskIds)
    }

    suspend fun deleteTaskByTaskId(taskId: String) {
        db.taskDao().deleteTaskByTaskId(taskId)
    }

    suspend fun updateRedeemedOnForAllTasks(taskIds: List<String>, tm: OffsetDateTime?) {
        db.taskDao().updateRedeemedOnForAllTasks(taskIds, tm)
    }

    suspend fun updateRedeemedOnForSingleTask(taskId: String, tm: OffsetDateTime?) {
        db.taskDao().updateRedeemedOnForSingleTask(taskId, tm)
    }

    fun loadTasksForRedeemedOn(redeemedOn: OffsetDateTime): Flow<List<Task>> {
        return db.taskDao().loadTasksForRedeemedOn(redeemedOn)
    }

    suspend fun getAllTasksWithTaskIdOnly(): List<String> {
        return db.taskDao().getAllTasksWithTaskIdOnly()
    }

    fun updateScanSessionName(name: String?, scanSessionEnd: OffsetDateTime) {
        db.taskDao().updateScanSessionName(name, scanSessionEnd)
    }

    fun loadCommunications(profile: CommunicationProfile): Flow<List<Communication>> {
        return db.communicationsDao().getAllCommunications(profile)
    }

    fun loadUnreadCommunications(profile: CommunicationProfile): Flow<List<Communication>> {
        return db.communicationsDao().getAllUnreadCommunications(profile)
    }

    suspend fun setCommunicationsAcknowledgedStatus(communicationId: String, consumed: Boolean) {
        db.communicationsDao().updateCommunication(communicationId, consumed)
    }

    var lastModifyTaskDate: Long
        get() {
            return securePrefs.getLong(LAST_MODIFY_TASK_DATE, 0)
        }
        set(value) {
            securePrefs.edit().putLong(LAST_MODIFY_TASK_DATE, value).apply()
        }
}
