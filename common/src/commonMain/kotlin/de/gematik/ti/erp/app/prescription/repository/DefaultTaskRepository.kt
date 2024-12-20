/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.fhir.model.TaskStatus
import de.gematik.ti.erp.app.fhir.model.extractActualTaskData
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

private const val TasksMaxPageSize = 50

class DefaultTaskRepository(
    private val remoteDataSource: TaskRemoteDataSource,
    private val localDataSource: TaskLocalDataSource,
    private val dispatchers: DispatchProvider
) : ResourcePaging<Int>(dispatchers, TasksMaxPageSize, maxPages = 1), TaskRepository {

    override suspend fun downloadTasks(profileId: ProfileIdentifier) =
        downloadPaged(profileId) { prev: Int?, next: Int ->
            (prev ?: 0) + next
        }.map {
            it ?: 0
        }

    override val tag: String = javaClass::getSimpleName.name

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
            withContext(dispatchers.io) {
                val (total, taskData) = extractActualTaskData(fhirBundle)

                supervisorScope {
                    val results = taskData.map { data ->
                        async {
                            data.lastModified?.let {
                                if (data.status == TaskStatus.Canceled) {
                                    localDataSource.updateTaskStatus(data.taskId, data.status, data.lastModified)
                                    Result.success(Unit)
                                } else {
                                    downloadTaskWithKBVBundle(taskId = data.taskId, profileId = profileId).map {
                                        if (it.isCompleted || it.lastMedicationDispense != null) {
                                            // download medication dispenses only if task was successfully
                                            // downloaded and status is completed or last medication dispense
                                            downloadMedicationDispenses(
                                                profileId,
                                                data.taskId
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }.awaitAll()
                    // throw if any result is not parsed correctly
                    results.find { it?.isFailure ?: false }?.getOrThrow()

                    // return number of updated tasks
                    ResourceResult(total, results.size)
                }
            }
        }

    private suspend fun downloadTaskWithKBVBundle(
        taskId: String,
        profileId: ProfileIdentifier
    ): Result<TaskLocalDataSource.SaveTaskResult> = withContext(dispatchers.io) {
        remoteDataSource.taskWithKBVBundle(profileId, taskId).mapCatching { bundle ->
            requireNotNull(localDataSource.saveTask(profileId, bundle))
        }
    }

    private suspend fun downloadMedicationDispenses(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<Unit> = withContext(dispatchers.io) {
        remoteDataSource.loadBundleOfMedicationDispenses(profileId, taskId).map { bundle ->
            val resources = bundle.findAll("entry.resource").toList()
            val version = resources.first().contained("meta")
                .contained("profile")
                .containedString().split("|")[1]
            when (version) {
                "1.4" -> {
                    localDataSource.saveMedicationDispensesWithMedications(taskId, bundle)
                }
                else -> resources.forEach { dispense ->
                    localDataSource.saveMedicationDispense(taskId, dispense)
                }
            }
        }
    }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        localDataSource.latestTaskModifiedTimestamp(profileId).first()
}
