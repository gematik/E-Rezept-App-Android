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

import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

interface TaskOperationsRepository {
    suspend fun saveScannedTaskList(
        profileId: ProfileIdentifier,
        tasks: List<TaskErpModel.Scanned>,
        medicationString: String
    )

    fun loadScannedTaskListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Scanned>>

    fun loadSyncedTaskListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Prescription>>

    fun loadDigaTaskListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Diga>>

    suspend fun redeem(
        profileId: ProfileIdentifier,
        communication: JsonElement,
        accessCode: String
    ): Result<JsonElement>

    suspend fun deleteRemoteTaskById(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<JsonElement?>

    suspend fun updateScannedTaskRedeemedOn(taskId: String, timestamp: Instant?)

    suspend fun updateScannedTaskName(taskId: String, name: String)

    fun loadSyncedTaskByTaskId(taskId: String): Flow<TaskErpModel.Synced.Prescription?>

    fun loadDigaTaskByTaskId(taskId: String): Flow<TaskErpModel.Synced.Diga?>

    fun loadSyncedTasksByTaskIds(taskIds: List<String>): Flow<List<TaskErpModel.Synced.Prescription>>

    fun loadScannedTasksByTaskIds(taskIds: List<String>): Flow<List<TaskErpModel.Scanned>>

    fun loadScannedTaskByTaskId(taskId: String): Flow<TaskErpModel.Scanned?>

    fun loadTaskIds(): Flow<List<String>>

    suspend fun deleteTaskByTaskIdOnlyInLocalDatabase(taskId: String)

    suspend fun redeemScannedTaskListByTaskIdList(taskIds: List<String>)

    fun loadTaskIdStringListByProfileId(profileId: ProfileIdentifier): Flow<List<String>>

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

    fun loadTaskByTaskId(taskId: String): Flow<TaskErpModel?>
}
