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

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.Communication
import org.hl7.fhir.r4.model.MedicationDispense
import java.time.Instant

enum class RemoteRedeemOption(val type: String) {
    Local(type = "onPremise"),
    Shipment(type = "shipment"),
    Delivery(type = "delivery")
}

const val PROFILE = "https://gematik.de/fhir/StructureDefinition/ErxCommunicationDispReq"

class PrescriptionRepository(
    private val dispatchers: DispatchProvider,
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    /**
     * Saves all scanned tasks. It doesn't matter if they already exist.
     */
    suspend fun saveScannedTasks(profileId: ProfileIdentifier, tasks: List<ScannedTaskData.ScannedTask>) {
        withContext(dispatchers.IO) {
            localDataSource.saveScannedTasks(profileId, tasks)
        }
    }

    fun scannedTasks(profileId: ProfileIdentifier) =
        localDataSource.loadScannedTasks(profileId).flowOn(dispatchers.IO)

    fun syncedTasks(profileId: ProfileIdentifier) =
        localDataSource.loadSyncedTasks(profileId).flowOn(dispatchers.IO)

    suspend fun redeemPrescription(
        profileId: ProfileIdentifier,
        communication: Communication,
        accessCode: String? = null
    ): Result<Unit> = withContext(dispatchers.IO) {
        remoteDataSource.communicate(profileId, communication, accessCode).map { }
    }

    /**
     * Communications will be downloaded and persisted local
     */
    suspend fun downloadCommunications(profileIdentifier: ProfileIdentifier): Result<Unit> =
        remoteDataSource.fetchCommunications(profileIdentifier).map { bundle ->
            withContext(dispatchers.IO) {
                localDataSource.saveCommunications(bundle)
            }
        }

    /**
     * Downloads all tasks and each referenced bundle. Each bundle is persisted locally.
     */
    suspend fun downloadTasks(profileId: ProfileIdentifier): Result<Int> =
        remoteDataSource.fetchTasks(localDataSource.taskSyncedUpTo(profileId).first(), profileId)
            .mapCatching { bundle ->
                val taskIds = bundle.extractResources<FhirTask>().map {
                    it.idElement.idPart
                }

                supervisorScope {
                    withContext(dispatchers.IO) {
                        val results = taskIds.map { taskId ->
                            async {
                                downloadTaskWithKBVBundle(taskId = taskId, profileId = profileId).map {
                                    if (it.isCompleted) {
                                        downloadMedicationDispenses(
                                            profileId,
                                            taskId
                                        )
                                    }

                                    requireNotNull(it.lastModified)
                                }
                            }
                        }.awaitAll()

                        // throw if any result is not parsed correctly
                        results.find { it.isFailure }?.getOrThrow()

                        val lastModified = results.map { it.getOrNull()!! }
                        lastModified.maxOrNull()?.let {
                            localDataSource.updateTaskSyncedUpTo(profileId, it)
                        }

                        // return number of bundles saved to db
                        lastModified.size
                    }
                }
            }

    private suspend fun downloadTaskWithKBVBundle(
        taskId: String,
        profileId: ProfileIdentifier
    ): Result<LocalDataSource.SaveTaskResult> = withContext(dispatchers.IO) {
        remoteDataSource.taskWithKBVBundle(profileId, taskId).mapCatching { bundle ->
            requireNotNull(localDataSource.saveTask(profileId, bundle))
        }
    }

    private suspend fun downloadMedicationDispenses(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<Unit> = withContext(dispatchers.IO) {
        remoteDataSource.loadBundleOfMedicationDispenses(profileId, taskId).map {
            it.extractResources<MedicationDispense>().forEach { dispense ->
                localDataSource.saveMedicationDispense(taskId, dispense)
            }
        }
    }

    suspend fun deleteTaskByTaskId(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<Unit> = withContext(dispatchers.IO) {
        remoteDataSource.deleteTask(profileId, taskId).map {
            localDataSource.deleteTask(taskId)
        }
    }

    suspend fun updateRedeemedOn(taskId: String, timestamp: Instant?) = withContext(dispatchers.IO) {
        localDataSource.updateRedeemedOn(taskId, timestamp)
    }

    fun loadSyncedTaskByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?> =
        localDataSource.loadSyncedTaskByTaskId(taskId).flowOn(dispatchers.IO)

    fun loadScannedTaskByTaskId(taskId: String): Flow<ScannedTaskData.ScannedTask?> =
        localDataSource.loadScannedTaskByTaskId(taskId).flowOn(dispatchers.IO)

    fun loadTaskIds(): Flow<List<String>> = localDataSource.loadTaskIds().flowOn(dispatchers.IO)
}
