/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.api.FhirPagination
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskEPrescriptionParsers
import de.gematik.ti.erp.app.fhir.support.FhirTaskEntryDataErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskStatus
import de.gematik.ti.erp.app.utils.toFachdienstTimestampString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

/**
 * Default implementation of [TaskRepository], handling fetching and processing of
 * prescription tasks via FHIR endpoints.
 */
class DefaultTaskRepository(
    private val remoteDataSource: TaskRemoteDataSource,
    private val localDataSource: RealmLegacyTaskLocalDataSource,
    private val parsers: TaskEPrescriptionParsers,
    private val paginator: FhirPagination,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TaskRepository {

    override val tag: String = javaClass::getSimpleName.name

    /**
     * Public API: Download all tasks modified since last sync, returning the count.
     */
    override suspend fun downloadTasks(profileId: ProfileIdentifier): Result<Int> {
        return paginateTasks(
            profileId = profileId,
            lastUpdated = syncedUpTo(profileId).toFachdienstTimestampString()
        )
    }

    /**
     * Query the latest modified timestamp from the local store for the given profile.
     */
    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        localDataSource.latestTaskModifiedTimestamp(profileId).first()

    /**
     * Fetches all task bundles via pagination, processes each page on arrival,
     * and returns a Result with the total number of processed tasks, or an error.
     */
    private suspend fun paginateTasks(
        profileId: ProfileIdentifier,
        lastUpdated: String?
    ): Result<Int> = runCatching {
        var totalCount = 0

        paginator.paginate(
            onFirstPage = { remoteDataSource.getTasks(profileId = profileId, lastUpdated = lastUpdated) },
            onNextPage = { url -> remoteDataSource.getTasksByUrl(profileId = profileId, url = url) },
            nextOf = { parsers.taskEntryParser.extract(it).nextPageUrl }
        ) { bundle ->
            val entries = parsers.taskEntryParser.extract(bundle).taskEntries
            val pageCount = supervisorScope {
                entries
                    .map { async(dispatcher) { processTaskEntry(profileId, it) } }
                    .awaitAll()
                    .onEach { it.getOrThrow() } // fail fast on any entry error
                    .size
            }
            totalCount += pageCount
        }
        totalCount
    }

    /**
     * Process a single task entry: update status or fetch+process full bundle.
     */
    private suspend fun processTaskEntry(profileId: ProfileIdentifier, data: FhirTaskEntryDataErpModel): Result<Unit> {
        return data.lastModified?.let { lastModified ->
            if (data.status == TaskStatus.Canceled) {
                // If canceled, update local status only
                localDataSource.updateTaskStatus(data.id, data.status, lastModified)
                Result.success(Unit)
            } else {
                // Otherwise, fetch KBV bundle and process
                fetchKbvJsonDataForTaskId(profileId, data.id)
                    .mapCatching { taskMetaDataAndKbvBundle ->
                        processTaskBundle(profileId, data.id, taskMetaDataAndKbvBundle).getOrThrow()
                    }
            }
        } ?: Result.failure(IllegalStateException("Missing lastModified for task ${data.id}"))
    }

    /**
     * Parse and persist metadata and medical data from a combined FHIR bundle.
     * Handles Partial failures by marking tasks incomplete and saving what is valid.
     */
    private suspend fun processTaskBundle(profileId: String, taskId: String, taskMetaDataAndKbvBundle: JsonElement): Result<Unit> {
        return parsers.taskBundleSeparationParser.extract(taskMetaDataAndKbvBundle)?.let { (metaDataBundle, kbvDataBundle) ->
            // 1) Process metadata
            val metaData = metaDataBundle.value.let(parsers.taskEPrescriptionMetadataParser::extract)
            if (metaData != null) {
                localDataSource.saveTaskEPrescriptionMetaData(profileId, metaData)
            } else {
                Result.failure(IllegalStateException("Failed to parse meta task bundle for taskId: $taskId"))
            }

            // 2) Process medical data
            val medicalData = kbvDataBundle.value.let(parsers.taskEPrescriptionMedicalDataParser::extract)
            when {
                // Parsing failed -> mark incomplete
                medicalData == null -> {
                    localDataSource.markTaskAsIncomplete(taskId, IllegalStateException("Failed to parse KBV bundle for taskId: $taskId"))
                    Result.failure(IllegalStateException("Failed to parse task bundle on null kbv data for taskId: $taskId"))
                }

                // Partial data -> mark incomplete and save what we have
                medicalData.getMissingProperties().isNotEmpty() -> {
                    localDataSource.markTaskAsIncomplete(
                        taskId,
                        IllegalStateException("Failed to parse KBV bundle fields: ${medicalData.getMissingProperties()}")
                    )
                    localDataSource.saveTaskEPrescriptionMedicalData(taskId, medicalData)
                        .map { taskResult ->
                            if (taskResult.isCompleted || taskResult.lastMedicationDispense != null) {
                                downloadMedicationDispenses(profileId, taskId)
                            }
                        }
                }

                // Full data -> save and possibly fetch dispenses
                else -> {
                    localDataSource.saveTaskEPrescriptionMedicalData(taskId, medicalData)
                        .map { taskResult ->
                            if (taskResult.isCompleted || taskResult.lastMedicationDispense != null) {
                                downloadMedicationDispenses(profileId, taskId)
                            }
                        }
                }
            }
        } ?: Result.failure(IllegalStateException("Failed to parse task bundle for taskId: $taskId"))
    }

    /**
     * Fetch the combined task metadata + KBV bundle JSON from the remote.
     */
    private suspend fun fetchKbvJsonDataForTaskId(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<JsonElement> = remoteDataSource
        .taskWithKBVBundle(profileId, taskId)
        .mapCatching { it }

    /**
     * Download and persist any medication dispenses associated with the task.
     */
    private suspend fun downloadMedicationDispenses(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<Unit> = withContext(dispatcher) {
        remoteDataSource.loadBundleOfMedicationDispenses(profileId, taskId)
            .map { bundle ->
                val medicationDispenseCollection = parsers.taskDispenseParser.extract(bundle)
                if (medicationDispenseCollection != null) {
                    localDataSource.saveTaskMedicationDispense(taskId, medicationDispenseCollection)
                }
            }
    }
}
