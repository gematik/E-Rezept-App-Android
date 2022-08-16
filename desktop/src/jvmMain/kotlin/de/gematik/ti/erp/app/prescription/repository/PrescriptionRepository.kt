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

import de.gematik.ti.erp.app.core.DispatchersProvider
import de.gematik.ti.erp.app.fhir.FhirMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class PrescriptionRepository(
    private val dispatchProvider: DispatchersProvider,
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val mapper: FhirMapper
) {
    fun tasks() = localDataSource.loadTasks()
    fun auditEvents() = localDataSource.loadAuditEvents()
    fun medicationDispenses() = localDataSource.loadMedicationDispenses()

    suspend fun download(): Result<Unit> =
        loadTasksRemote().mapCatching { taskIds ->
            supervisorScope {
                withContext(dispatchProvider.io()) {
                    taskIds.map { taskId ->
                        async { downloadTaskWithKBVBundle(taskId) }
                    } + async {
                        downloadAuditEvents()
                    } + async {
                        downloadMedicationDispenses()
                    }
                }
                    .awaitAll()
                    .find { it.isFailure }
                    ?.getOrThrow()
            }
        }

    suspend fun delete(taskId: String) =
        remoteDataSource.deleteTask(taskId).mapCatching {
            localDataSource.deleteTask(taskId)
        }

    suspend fun loadTasksRemote(): Result<List<String>> =
        remoteDataSource.getTasks().mapCatching { bundle ->
            mapper.parseTaskIds(bundle)
        }

    suspend fun downloadTaskWithKBVBundle(taskId: String): Result<Unit> =
        remoteDataSource.getTaskWithKBVBundle(taskId).mapCatching {
            val task = mapper.mapFhirBundleToTaskWithKBVBundle(it)
            localDataSource.saveTask(task)
        }

    suspend fun downloadAuditEvents(): Result<Unit> =
        remoteDataSource.getAllAuditEvents().mapCatching {
            val auditEvents = mapper.mapFhirBundleToAuditEvents(it)
            localDataSource.saveAuditEvents(auditEvents)
        }

    suspend fun downloadMedicationDispenses(): Result<Unit> =
        remoteDataSource.getAllMedicationDispenses().mapCatching {
            val medicationDispenses = mapper.mapFhirMedicationDispenseToSimpleMedicationDispense(it)
            localDataSource.saveMedicationDispenses(medicationDispenses)
        }
    suspend fun invalidate() = localDataSource.invalidate()
}
