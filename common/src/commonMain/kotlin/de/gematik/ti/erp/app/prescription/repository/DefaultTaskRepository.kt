/*
 * Copyright 2025, gematik GmbH
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
import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirTaskEntryDataErpModel
import de.gematik.ti.erp.app.fhir.model.TaskStatus
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskEPrescriptionParsers
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

private const val TasksMaxPageSize = 50

class DefaultTaskRepository(
    private val remoteDataSource: TaskRemoteDataSource,
    private val localDataSource: TaskLocalDataSource,
    private val parsers: TaskEPrescriptionParsers,
    private val dispatchers: DispatchProvider
) : ResourcePaging<Int>(dispatchers, TasksMaxPageSize, maxPages = 1), TaskRepository {

    override suspend fun downloadTasks(profileId: ProfileIdentifier) =
        downloadPaged(profileId) { prev: Int?, next: Int ->
            (prev ?: 0) + next
        }.map {
            it ?: 0
        }

    override val tag: String = javaClass::getSimpleName.name

    private suspend fun processTaskEntry(profileId: ProfileIdentifier, data: FhirTaskEntryDataErpModel): Result<Unit> {
        return data.lastModified?.let { lastModified ->
            if (data.status == TaskStatus.Canceled) {
                localDataSource.updateTaskStatus(data.id, data.status, lastModified)
                Result.success(Unit)
            } else {
                fetchKbvJsonDataForTaskId(profileId, data.id)
                    .mapCatching { taskMetaDataAndKbvBundle ->
                        processTaskBundle(profileId, data.id, taskMetaDataAndKbvBundle).getOrThrow()
                    }
            }
        } ?: Result.failure(IllegalStateException("Missing lastModified for task ${data.id}"))
    }

    private suspend fun processTaskBundle(profileId: String, taskId: String, taskMetaDataAndKbvBundle: JsonElement): Result<Unit> {
        return parsers.taskBundleSeparationParser.extract(taskMetaDataAndKbvBundle)?.let { (metaDataBundle, kbvDataBundle) ->
            val metaData = metaDataBundle.value.let(parsers.taskEPrescriptionMetadataParser::extract)
            val medicalData = kbvDataBundle.value.let(parsers.taskEPrescriptionMedicalDataParser::extract)
            if (metaData != null) {
                localDataSource.saveTaskEPrescriptionMetaData(profileId, metaData)
            } else {
                Result.failure(IllegalStateException("Failed to parse meta task bundle for taskId: $taskId"))
            }
            if (medicalData != null) {
                localDataSource.saveTaskEPrescriptionMedicalData(taskId, medicalData)
                    .map { taskResult ->
                        if (taskResult.isCompleted || taskResult.lastMedicationDispense != null) {
                            downloadMedicationDispenses(profileId, taskId)
                        }
                    }
            } else {
                localDataSource.markTaskAsIncomplete(taskId, IllegalStateException("Failed to parse KBV bundle for taskId: $taskId"))
                Result.failure(IllegalStateException("Failed to parse task bundle on null kbv data for taskId: $taskId"))
            }
        } ?: Result.failure(IllegalStateException("Failed to parse task bundle for taskId: $taskId"))
    }

    override suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourceResult<Int>> =
        remoteDataSource.getTasks(profileId = profileId, lastUpdated = timestamp, count = count)
            .mapCatching { fhirBundle ->
                withContext(dispatchers.io) {
                    val (totalEntries, taskEntries) = parsers.taskEntryParser.extract(fhirBundle)
                        .let { it.bundleTotal to it.taskEntries }

                    supervisorScope {
                        val results = taskEntries.map { data ->
                            async { processTaskEntry(profileId, data) }
                        }.awaitAll()

                        // throw if any result is not parsed correctly
                        results.find { it.isFailure }?.getOrThrow()

                        // return number of updated tasks
                        ResourceResult(totalEntries, results.size)
                    }
                }
            }

    private suspend fun fetchKbvJsonDataForTaskId(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<JsonElement> = remoteDataSource
        .taskWithKBVBundle(profileId, taskId)
        .mapCatching { it }

    private suspend fun downloadMedicationDispenses(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<Unit> = withContext(dispatchers.io) {
        remoteDataSource.loadBundleOfMedicationDispenses(profileId, taskId)
            .map { bundle ->
                val medicationDispenseCollection = parsers.taskDispenseParser.extract(bundle)
                if (medicationDispenseCollection != null) {
                    localDataSource.saveTaskMedicationDispense(taskId, medicationDispenseCollection)
                }
            }
    }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        localDataSource.latestTaskModifiedTimestamp(profileId).first()
}
