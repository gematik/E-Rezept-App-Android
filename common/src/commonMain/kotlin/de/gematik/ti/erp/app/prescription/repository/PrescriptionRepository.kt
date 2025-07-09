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

import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

interface PrescriptionRepository {
    suspend fun saveScannedTasks(
        profileId: ProfileIdentifier,
        tasks: List<ScannedTaskData.ScannedTask>,
        medicationString: String
    )

    fun scannedTasks(profileId: ProfileIdentifier): Flow<List<ScannedTaskData.ScannedTask>>

    fun syncedTasks(profileId: ProfileIdentifier): Flow<List<SyncedTaskData.SyncedTask>>

    suspend fun redeem(
        profileId: ProfileIdentifier,
        communication: JsonElement,
        accessCode: String
    ): Result<JsonElement>

    suspend fun deleteRemoteTaskById(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<JsonElement?>

    suspend fun updateRedeemedOn(taskId: String, timestamp: Instant?)

    suspend fun updateScannedTaskName(taskId: String, name: String)

    fun loadSyncedTaskByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?>

    fun loadSyncedTasksByTaskIds(taskIds: List<String>): Flow<List<SyncedTaskData.SyncedTask>>

    fun loadScannedTasksByTaskIds(taskIds: List<String>): Flow<List<ScannedTaskData.ScannedTask>>

    fun loadScannedTaskByTaskId(taskId: String): Flow<ScannedTaskData.ScannedTask?>

    fun loadTaskIds(): Flow<List<String>>
    suspend fun deleteLocalTaskById(taskId: String)
    suspend fun wasProfileEverAuthenticated(profileId: ProfileIdentifier): Boolean
    suspend fun redeemScannedTasks(taskIds: List<String>)

    fun loadAllTaskIds(profileId: ProfileIdentifier): Flow<List<String>>
}
