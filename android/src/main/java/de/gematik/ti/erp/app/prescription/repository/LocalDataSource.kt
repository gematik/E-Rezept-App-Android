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
import androidx.room.withTransaction
import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.MedicationDispenseSimple
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.db.entities.TaskWithMedicationDispense
import de.gematik.ti.erp.app.di.NetworkSecureSharedPreferences
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

private const val AUDIT_SYNC_ERROR_KEY = "AUDIT_SYNC_ERROR"
private const val AUDIT_SYNC_DATE_KEY = "AUDIT_SYNC_DATE"

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
        db.withTransaction {
            val tasks = db.taskDao().getAllTasksWithTaskIdOnly()
            val communicationsWithTask = communications.filter {
                it.taskId in tasks
            }
            db.communicationsDao()
                .insertMultipleCommunications(*communicationsWithTask.toTypedArray())
        }
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

    fun loadTasks(profileName: String): Flow<List<Task>> {
        return db.taskDao().getAllTasks(profileName)
    }

    fun loadScannedTasksWithoutBundle(profileName: String): Flow<List<Task>> =
        db.taskDao().getScannedTasksWithoutBundle(profileName)

    fun loadSyncedTasksWithoutBundle(profileName: String): Flow<List<Task>> =
        db.taskDao().getSyncedTasksWithoutBundle(profileName)

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

    fun loadTasksForRedeemedOn(redeemedOn: OffsetDateTime, profileName: String): Flow<List<Task>> {
        return db.taskDao().loadTasksForRedeemedOn(redeemedOn, profileName)
    }

    suspend fun getAllTasksWithTaskIdOnly(profileName: String): List<String> {
        return db.taskDao().getAllTasksWithTaskIdOnly(profileName)
    }

    fun updateScanSessionName(name: String?, scanSessionEnd: OffsetDateTime) {
        db.taskDao().updateScanSessionName(name, scanSessionEnd)
    }

    fun loadCommunications(
        profile: CommunicationProfile,
        userProfile: String
    ): Flow<List<Communication>> {
        return db.communicationsDao().getAllCommunications(profile, userProfile)
    }

    fun loadUnreadCommunications(
        profile: CommunicationProfile,
        userProfile: String
    ): Flow<List<Communication>> {
        return db.communicationsDao()
            .getAllUnreadCommunications(profile = profile, userProfile = userProfile)
    }

    suspend fun setCommunicationsAcknowledgedStatus(communicationId: String, consumed: Boolean) {
        db.communicationsDao().updateCommunication(communicationId, consumed)
    }

    // TODO maybe use something else then secure prefs
    fun setLastModifiedTaskDate(profileName: String, value: Long) {
        securePrefs.edit().putLong(profileName, value).apply()
    }

    // TODO maybe use something else then secure prefs
    fun lastModifiedTaskDate(profileName: String): Long {
        return securePrefs.getLong(profileName, 0)
    }

    suspend fun setAllAuditEventsSyncedUpTo(profileName: String) {
        val timestamp = db.taskDao().getLatestAuditEventTimeStamp()
        db.profileDao().updateAuditEventSynced(timestamp, profileName)
    }

    suspend fun auditEventsSyncedUpTo(profileName: String): OffsetDateTime {
        return db.profileDao().getLastAuditEventSynced(profileName)
            ?: Instant.ofEpochSecond(0).atOffset(ZoneOffset.UTC)
    }
}
