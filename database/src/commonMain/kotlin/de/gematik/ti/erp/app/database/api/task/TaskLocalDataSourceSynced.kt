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

package de.gematik.ti.erp.app.database.api.task

import de.gematik.ti.erp.app.database.api.model.SaveTaskResult
import de.gematik.ti.erp.app.fhir.FhirMedicationDispenseErpModelCollection
import de.gematik.ti.erp.app.fhir.FhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.FhirTaskMetaDataErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskStatusErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

interface TaskLocalDataSourceSynced {

    /**
     * Observe the latest last-modified timestamp among all synced tasks for the profile.
     */
    fun getLatestTaskModifiedTimestamp(profileId: ProfileIdentifier): Flow<Instant?>

    /**
     * Update the status of a synced task. Implementations may also update its last-modified time
     * when `lastModified` is provided by the caller.
     */
    suspend fun updateSyncedTaskStatus(taskId: String, status: FhirTaskStatusErpModel, lastModified: FhirTemporal?)

    /**
     * Mark a synced task as incomplete due to an error condition and persist diagnostic details.
     */
    suspend fun markSyncedTaskAsIncomplete(taskId: String, error: Throwable, originalBundle: JsonElement): Result<Unit>

    /**
     * Persist or update server-provided meta data of a task for the given profile.
     */
    suspend fun saveSyncedTaskMetaData(
        profileId: ProfileIdentifier,
        model: FhirTaskMetaDataErpModel
    ): Result<Unit>

    /**
     * Persist or update the medical data (e.g., patient, practitioner, medication, coverage) of a
     * synced task. Returns a summary describing the resulting state and timestamps.
     */
    suspend fun saveSyncedTaskKBVData(
        taskId: String,
        model: FhirTaskDataErpModel
    ): Result<SaveTaskResult>

    /**
     * Persist medication dispense records for the given task. Implementations should avoid storing
     * duplicates when the same dispense is received multiple times.
     */
    suspend fun saveSyncedTaskMedicationDispense(taskId: String, dispenseCollection: FhirMedicationDispenseErpModelCollection)
}
