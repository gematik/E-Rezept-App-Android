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
import de.gematik.ti.erp.app.api.map
import de.gematik.ti.erp.app.api.mapCatching
import de.gematik.ti.erp.app.api.mapSuccessful
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.db.entities.TaskStatus
import de.gematik.ti.erp.app.db.entities.TaskWithMedicationDispense
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Communication
import org.hl7.fhir.r4.model.MedicationDispense

typealias FhirTask = org.hl7.fhir.r4.model.Task
typealias FhirCommunication = Communication

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
            requireNotNull(it.profileName)
            requireNotNull(it.scannedOn)
            requireNotNull(it.scanSessionEnd)
            require(it.rawKBVBundle == null)
        }

        localDataSource.saveTasks(tasks)
    }

    fun tasks(profileName: String) = localDataSource.loadTasks(profileName)
    fun scannedTasksWithoutBundle(profileName: String) =
        localDataSource.loadScannedTasksWithoutBundle(profileName)

    fun syncedTasksWithoutBundle(profileName: String) =
        localDataSource.loadSyncedTasksWithoutBundle(profileName)

    suspend fun redeemPrescription(
        profileName: String,
        communication: Communication
    ): Result<ResponseBody> {
        return remoteDataSource.communicate(profileName, communication)
    }

    /**
     * Communications will be downloaded and persisted local
     */
    suspend fun downloadCommunications(profileName: String): Result<Unit> =
        remoteDataSource.fetchCommunications(profileName).map {
            withContext(dispatchProvider.default()) {
                val communications = mapper.mapFhirBundleToCommunications(it, profileName)
                localDataSource.saveCommunications(communications)
                Result.Success(Unit)
            }
        }

    private fun lastKnownModifierDate(profileName: String) =
        LocalDateTime.ofEpochSecond(
            localDataSource.lastModifiedTaskDate(profileName), 0,
            ZoneOffset.UTC
        ).atOffset(ZoneOffset.UTC)

    /**
     * All Tasks with inherited KBV Bundle will be stored in the local storage.
     * Audit Event will be downloaded as well, so they can be related to the Tasks.
     * If the download of Audit Events fails, it fails silently.
     */
    suspend fun downloadTasks(profileName: String): Result<Int> =
        remoteDataSource.fetchTasks(lastKnownModifierDate(profileName), profileName).mapCatching { bundle ->
            val taskIds = mapper.parseTaskIds(bundle)

            supervisorScope {
                withContext(dispatchProvider.io()) {
                    taskIds.map { taskId ->
                        async {
                            downloadTaskWithKBVBundle(taskId, profileName).map {
                                deleteLowDetailEvents(taskId)

                                if (it.status == TaskStatus.Completed) {
                                    downloadMedicationDispense(
                                        profileName,
                                        taskId
                                    )
                                }

                                Result.Success(requireNotNull(it.lastModified))
                            }
                        }
                    }
                        .awaitAll()
                        .mapSuccessful { lastModified: List<OffsetDateTime> ->
                            lastModified.maxOrNull()?.let {
                                localDataSource.setLastModifiedTaskDate(profileName, it.toEpochSecond())
                            }
                            Result.Success(lastModified.size)
                        }
                }
            }
        }

    private suspend fun downloadTaskWithKBVBundle(
        taskId: String,
        profileName: String
    ): Result<Task> =
        remoteDataSource.taskWithKBVBundle(profileName, taskId).mapCatching {
            val task = mapper.mapFhirBundleToTaskWithKBVBundle(it, profileName)
            localDataSource.saveTask(task)
            Result.Success(task)
        }

    suspend fun downloadAuditEvents(
        profileName: String,
        count: Int? = null,
        offset: Int? = null
    ): Result<Any> {
        val syncedUpTo = localDataSource.auditEventsSyncedUpTo(profileName)
        return when (
            val result =
                remoteDataSource.allAuditEvents(
                    profileName,
                    syncedUpTo,
                    count,
                    offset
                )
        ) {
            is Result.Error -> {
                localDataSource.storeAuditEventSyncError()
                result
            }
            is Result.Success -> {
                try {
                    val auditEvents = mapper.mapFhirBundleToAuditEvents(profileName, result.data)
                    localDataSource.saveAuditEvents(auditEvents)
                    val nextLink = if (auditEvents.isEmpty()) {
                        localDataSource.setAllAuditEventsSyncedUpTo(profileName)
                        ""
                    } else {
                        result.data.link.filter { it.relation == "next" }[0].url
                    }
                    Result.Success(nextLink)
                } catch (e: Exception) {
                    localDataSource.storeAuditEventSyncError()
                    Result.Error(e)
                }
            }
        }
    }

    private suspend fun downloadMedicationDispense(
        profileName: String,
        taskId: String
    ): Result<Unit> {
        return when (val result = remoteDataSource.medicationDispense(profileName, taskId)) {
            is Result.Error -> result
            is Result.Success -> {
                try {
                    // FIXME cast can never succeed
                    mapper.mapMedicationDispenseToMedicationDispenseSimple(result.data as MedicationDispense)
                        .let {
                            localDataSource.saveMedicationDispense(it)
                            localDataSource.updateRedeemedOnForSingleTask(
                                taskId,
                                it.whenHandedOver
                            )
                        }
                    Result.Success(Unit)
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

    suspend fun deleteTaskByTaskId(
        profileName: String,
        taskId: String,
        isRemoteTask: Boolean
    ): Result<Unit> {
        val result = if (isRemoteTask) {
            when (val result = remoteDataSource.deleteTask(profileName, taskId)) {
                is Result.Success -> {
                    Result.Success(Unit)
                }
                is Result.Error -> result
            }
        } else {
            Result.Success(Unit)
        }

        if (result is Result.Success) {
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

    fun loadTasksForRedeemedOn(redeemedOn: OffsetDateTime, profileName: String): Flow<List<Task>> {
        return localDataSource.loadTasksForRedeemedOn(redeemedOn, profileName)
    }

    fun loadTaskWithMedicationDispenseForTaskId(taskId: String): Flow<TaskWithMedicationDispense> {
        return localDataSource.loadTaskWithMedicationDispenseForTaskId(taskId)
    }

    fun loadTasksForTaskId(vararg taskIds: String): Flow<List<Task>> {
        return localDataSource.loadTasksForTaskId(*taskIds)
    }

    suspend fun getAllTasksWithTaskIdOnly(profileName: String): List<String> {
        return localDataSource.getAllTasksWithTaskIdOnly(profileName)
    }

    fun loadAuditEvents(taskId: String, locale: String): Flow<List<AuditEventSimple>> {
        return localDataSource.loadAuditEvents(taskId = taskId, locale = locale)
    }

    fun updateScanSessionName(name: String?, scanSessionEnd: OffsetDateTime) {
        localDataSource.updateScanSessionName(name, scanSessionEnd)
    }
}
