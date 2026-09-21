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

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over the local persistence of prescription tasks.
 *
 * This interface intentionally avoids any database-specific concepts so that implementations can
 * be backed by any storage technology (Realm, SQL, in-memory, etc.).
 *
 * All read operations that can change over time return a cold `Flow` to allow consumers to observe
 * updates. Write operations are declared as `suspend` and should be transactional on the
 * implementation side.
 */
interface TaskLocalDataSourceCommon {

    // common

    /**
     * Observe all tasks (synced and scanned) belonging to the given profile.
     *
     * The returned flow emits a new list whenever the underlying data for the profile changes.
     */
    fun loadTaskListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel>>

    /**
     * Observe tasks for the provided list of task identifiers.
     *
     * Implementations should de-duplicate results when an id appears in multiple sources.
     */
    fun loadTaskListByTaskIdList(taskIds: List<String>): Flow<List<TaskErpModel>>

    /**
     * Observe a single task by its identifier. Emits `null` when the task does not exist.
     */
    fun loadTaskByTaskId(taskId: String): Flow<TaskErpModel?>

    /**
     * Observe all known task identifiers across all profiles and sources.
     */
    fun loadATaskIdStringList(): Flow<List<String>>

    /**
     * Observe all task identifiers that belong to the given profile.
     */
    fun loadTaskIdStringListByProfileId(profileId: ProfileIdentifier): Flow<List<String>>

    @Requirement(
        "A_19229-01#2",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "User can delete a locally stored prescription and all its linked resources."
    )
    /**
     * Permanently remove a task and all of its locally linked resources by id.
     */
    suspend fun deleteTaskByTaskId(taskId: String)

    suspend fun saveCommunications(communicationModel: FhirCommunicationBundleErpModel): Int

    /**
     * Delete all tasks associated with a profile.
     */
    suspend fun deleteTasksByProfileId(profileId: ProfileIdentifier)

    /**
     * Delete all communications associated with a profile.
     */
    suspend fun deleteCommunicationsByProfileId(profileId: ProfileIdentifier)

    /**
     * Delete all invoices (medication dispenses) associated with a profile.
     */
    suspend fun deleteInvoicesByProfileId(profileId: ProfileIdentifier)
}
