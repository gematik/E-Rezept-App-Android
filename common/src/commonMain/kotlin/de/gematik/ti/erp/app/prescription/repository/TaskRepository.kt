/*
 * Copyright (c) 2024 gematik GmbH
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
import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.fhir.model.extractTaskIds
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

private const val TasksMaxPageSize = 50

class TaskRepository(
    private val remoteDataSource: TaskRemoteDataSource,
    private val localDataSource: TaskLocalDataSource,
    private val dispatchers: DispatchProvider
) : ResourcePaging<Int>(dispatchers, TasksMaxPageSize, maxPages = 1) {

    suspend fun downloadTasks(profileId: ProfileIdentifier) = downloadPaged(profileId) { prev: Int?, next: Int ->
        (prev ?: 0) + next
    }.map {
        it ?: 0
    }

    override val tag: String = "TaskRepository"

    override suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourceResult<Int>> =
        remoteDataSource.getTasks(
            profileId = profileId,
            lastUpdated = timestamp,
            count = count
        ).mapCatching { fhirBundle ->
            withContext(dispatchers.IO) {
                val (total, taskIds) = extractTaskIds(fhirBundle)

                supervisorScope {
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

                    // return number of bundles saved to db
                    ResourceResult(total, results.size)
                }
            }
        }

    private suspend fun downloadTaskWithKBVBundle(
        taskId: String,
        profileId: ProfileIdentifier
    ): Result<TaskLocalDataSource.SaveTaskResult> = withContext(dispatchers.IO) {
        remoteDataSource.taskWithKBVBundle(profileId, taskId).mapCatching { bundle ->
            requireNotNull(localDataSource.saveTask(profileId, bundle))
        }
    }

    private suspend fun downloadMedicationDispenses(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<Unit> = withContext(dispatchers.IO) {
        remoteDataSource.loadBundleOfMedicationDispenses(profileId, taskId).map { bundle ->
            bundle.findAll("entry.resource")
                .forEach { dispense ->
                    localDataSource.saveMedicationDispense(taskId, dispense)
                }
        }
    }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        localDataSource.latestTaskModifiedTimestamp(profileId).first()
}
