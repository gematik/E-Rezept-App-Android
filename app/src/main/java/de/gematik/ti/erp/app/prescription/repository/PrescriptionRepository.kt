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

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.api.Result.Success
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.db.entities.TaskWithMedicationDispense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Communication
import org.hl7.fhir.r4.model.Task.TaskStatus
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import org.hl7.fhir.r4.model.MedicationDispense
import timber.log.Timber

typealias FhirTask = org.hl7.fhir.r4.model.Task
typealias FhirCommunication = org.hl7.fhir.r4.model.Communication

enum class RemoteRedeemOption(val type: String) {
    Local(type = "onPremise"),
    Shipment(type = "shipment"),
    Delivery(type = "delivery")
}

const val PROFILE = "https://gematik.de/fhir/StructureDefinition/ErxCommunicationDispReq"

class PrescriptionRepository @Inject constructor(
    private val dispatchProvider: DispatchProvider,
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val mapper: Mapper
) {

    fun hasAuditEventsSyncError(): Boolean {
        return localDataSource.getAuditSyncError()
    }

    fun lastSuccessfulAuditEventSyncDate(): LocalDateTime? {
        return localDataSource.getAuditSyncDate()
    }

    /**
     * Saves all scanned tasks. It doesn't matter if they already exist.
     */
    suspend fun saveScannedTasks(tasks: List<Task>) {
        tasks.forEach {
            requireNotNull(it.taskId)
            requireNotNull(it.scannedOn)
            requireNotNull(it.scanSessionEnd)
            require(it.rawKBVBundle == null)
        }

        localDataSource.saveTasks(tasks)
    }

    fun tasks() = localDataSource.loadTasks()
    fun scannedTasksWithoutBundle() = localDataSource.loadScannedTasksWithoutBundle()
    fun syncedTasksWithoutBundle() = localDataSource.loadSyncedTasksWithoutBundle()

    suspend fun redeemPrescription(
        communication: Communication
    ): Result<ResponseBody> {
        return remoteDataSource.communicate(communication)
    }

    /**
     * Communications will be downloaded and persisted local
     */
    suspend fun downloadCommunications(): Result<Unit> {
        return when (val r = remoteDataSource.fetchCommunications()) {
            is Result.Error -> return r
            is Success -> {
                withContext(dispatchProvider.default()) {
                    Timber.d("mapping communications")
                    val communications = mapper.mapFhirBundleToCommunications(r.data)
                    Timber.d("saving communications: ${communications.size}")

                    localDataSource.saveCommunications(communications)
                    Success(Unit)
                }
            }
        }
    }

    /**
     * All Tasks with inherited KBV Bundle will be stored in the local storage.
     * Audit Event will be downloaded as well, so they can be related to the Tasks.
     * If the download of Audit Events fails, it fails silently.
     */
    suspend fun downloadTasks(): Result<Unit> {
        val lastKnownModifierDate = LocalDateTime.ofEpochSecond(
            localDataSource.lastModifyTaskDate, 0,
            ZoneOffset.UTC
        ).atOffset(ZoneOffset.UTC)

        return when (val result = remoteDataSource.fetchTasks(lastKnownModifierDate)) {
            is Success -> {
                try {
                    val taskIds = mapper.parseTaskIds(result.data)
                    supervisorScope {
                        launch(dispatchProvider.io()) {
                            downloadAuditEvents(lastKnownModifierDate)
                        }
                        launch(dispatchProvider.io()) {
                            downloadCommunications()
                        }
                    }
                    val errorResults = arrayListOf<Result.Error>()

                    var newLastModifyDate: Long = 0

                    for (taskId in taskIds) {
                        supervisorScope {
                            launch(dispatchProvider.io()) { deleteLowDetailEvents(taskId) }
                        }

                        when (val kbvResult = downloadTaskWithKBVBundle(taskId)) {
                            is Result.Error -> errorResults.add(kbvResult)
                            is Success -> {
                                kbvResult.data.lastModified?.toEpochSecond()?.let { lastModified ->
                                    if (lastModified > newLastModifyDate) {
                                        newLastModifyDate = lastModified
                                    }
                                }
                                if (kbvResult.data.status == TaskStatus.COMPLETED.definition) {
                                    supervisorScope {
                                        launch(dispatchProvider.io()) { downloadMedicationDispense(taskId) }
                                    }
                                }
                            }
                        }
                    }

                    if (errorResults.size > 0) {
                        Result.Error(errorResults.first().exception)
                    } else {
                        if (newLastModifyDate > 0) {
                            localDataSource.lastModifyTaskDate = newLastModifyDate
                        }
                        Success(Unit)
                    }
                } catch (e: Exception) {
                    Result.Error(e)
                }
            }
            is Result.Error -> result
        }
    }

    private suspend fun downloadTaskWithKBVBundle(taskId: String): Result<Task> {
        return when (val result = remoteDataSource.taskWithKBVBundle(taskId)) {
            is Result.Error -> return result
            is Success -> {
                try {
                    val task = mapper.mapFhirBundleToTaskWithKBVBundle(result.data)
                    localDataSource.saveTask(task)
                    Success(task)
                } catch (e: Exception) {
                    Result.Error(e)
                }
            }
        }
    }

    private suspend fun downloadAuditEvents(lastKnownModifierDate: OffsetDateTime) {
        when (val result = remoteDataSource.allAuditEvents(lastKnownModifierDate)) {
            is Result.Error -> localDataSource.storeAuditEventSyncError()
            is Success -> {
                try {
                    val auditEvents = mapper.mapFhirBundleToAuditEvents(result.data)
                    localDataSource.saveAuditEvents(auditEvents)
                } catch (e: Exception) {
                    localDataSource.storeAuditEventSyncError()
                }
            }
        }
    }

    private suspend fun downloadMedicationDispense(taskId: String): Result<Unit> {
        return when (val result = remoteDataSource.medicationDispense(taskId)) {
            is Result.Error -> result
            is Success -> {
                try {
                    mapper.mapMedicationDispenseToMedicationDispenseSimple(result.data as MedicationDispense)
                        .let {
                            localDataSource.saveMedicationDispense(it)
                            localDataSource.updateRedeemedOnForSingleTask(
                                taskId,
                                it.whenHandedOver
                            )
                        }
                    Success(Unit)
                } catch (e: Exception) {
                    Result.Error(e)
                }
            }
        }
    }

    suspend fun saveLowDetailEvent(lowDetailEvent: LowDetailEventSimple) {
        localDataSource.saveLowDetailEvent(lowDetailEvent)
    }

    fun loadLowDetailEvents(taskId: String): Flow<List<LowDetailEventSimple>> =
        localDataSource.loadLowDetailEvents(taskId)

    fun deleteLowDetailEvents(taskId: String) {
        localDataSource.deleteLowDetailEvents(taskId)
    }

    suspend fun loadCapabilityStatement() = remoteDataSource.loadCapabilityStatement()

    suspend fun deleteTaskByTaskId(taskId: String, isRemoteTask: Boolean): Result<Unit> {

        val result = if (isRemoteTask) {
            when (val result = remoteDataSource.deleteTask(taskId)) {
                is Success -> {
                    Success(Unit)
                }
                is Result.Error -> result
            }
        } else {
            Success(Unit)
        }

        if (result is Success) {
            localDataSource.deleteTaskByTaskId(taskId)
        }
        return result
    }

    suspend fun updateRedeemedOnForAllTasks(taskIds: List<String>, tm: OffsetDateTime?) {
        localDataSource.updateRedeemedOnForAllTasks(taskIds, tm)
    }

    suspend fun updateRedeemedOnForSingleTask(taskId: String, tm: OffsetDateTime?) {
        localDataSource.updateRedeemedOnForSingleTask(taskId, tm)
    }

    fun loadTasksForRedeemedOn(redeemedOn: OffsetDateTime): Flow<List<Task>> {
        return localDataSource.loadTasksForRedeemedOn(redeemedOn)
    }

    fun loadTaskWithMedicationDispenseForTaskId(taskId: String): Flow<TaskWithMedicationDispense> {
        return localDataSource.loadTaskWithMedicationDispenseForTaskId(taskId)
    }

    fun loadTasksForTaskId(vararg taskIds: String): Flow<List<Task>> {
        return localDataSource.loadTasksForTaskId(*taskIds)
    }

    suspend fun getAllTasksWithTaskIdOnly(): List<String> {
        return localDataSource.getAllTasksWithTaskIdOnly()
    }

    fun loadAuditEvents(taskId: String, locale: String): Flow<List<AuditEventSimple>> {
        return localDataSource.loadAuditEvents(taskId, locale)
    }

    fun updateScanSessionName(name: String?, scanSessionEnd: OffsetDateTime) {
        localDataSource.updateScanSessionName(name, scanSessionEnd)
    }
}
