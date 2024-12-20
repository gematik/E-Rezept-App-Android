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
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

enum class RemoteRedeemOption(val type: String) {
    Local(type = "onPremise"),
    Shipment(type = "shipment"),
    Delivery(type = "delivery")
}

class DefaultPrescriptionRepository(
    private val dispatchers: DispatchProvider,
    private val localDataSource: PrescriptionLocalDataSource,
    private val remoteDataSource: PrescriptionRemoteDataSource
) : PrescriptionRepository {

    /**
     * Saves all scanned tasks. It doesn't matter if they already exist.
     */
    override suspend fun saveScannedTasks(
        profileId: ProfileIdentifier,
        tasks: List<ScannedTaskData.ScannedTask>,
        medicationString: String
    ) {
        withContext(dispatchers.io) {
            localDataSource.saveScannedTasks(profileId, tasks, medicationString)
        }
    }

    override fun scannedTasks(profileId: ProfileIdentifier) =
        localDataSource.loadScannedTasks(profileId)

    override fun syncedTasks(profileId: ProfileIdentifier) =
        localDataSource.loadSyncedTasks(profileId)

    override suspend fun redeemPrescription(
        profileId: ProfileIdentifier,
        communication: JsonElement,
        accessCode: String
    ): Result<JsonElement> = withContext(dispatchers.io) {
        remoteDataSource.communicate(profileId, communication, accessCode)
    }

    override suspend fun deleteRemoteTaskById(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<JsonElement?> =
        remoteDataSource.deleteTask(profileId, taskId).map { it }

    override suspend fun updateRedeemedOn(taskId: String, timestamp: Instant?) =
        localDataSource.updateRedeemedOn(taskId, timestamp)

    override suspend fun updateScannedTaskName(taskId: String, name: String) =
        localDataSource.updateScannedTaskName(taskId, name)

    override fun loadSyncedTaskByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?> =
        localDataSource.loadSyncedTaskByTaskId(taskId)

    override fun loadScannedTaskByTaskId(taskId: String): Flow<ScannedTaskData.ScannedTask?> =
        localDataSource.loadScannedTaskByTaskId(taskId)

    override fun loadTaskIds(): Flow<List<String>> = localDataSource.loadTaskIds().flowOn(dispatchers.io)

    override suspend fun deleteLocalTaskById(taskId: String) {
        localDataSource.deleteTask(taskId)
    }
    override suspend fun deleteLocalInvoicesById(taskId: String) {
        localDataSource.deleteInvoices(taskId)
    }

    override suspend fun wasProfileEverAuthenticated(profileId: ProfileIdentifier): Boolean =
        localDataSource.wasProfileEverAuthenticated(profileId)

    override suspend fun redeemScannedTasks(taskIds: List<String>) {
        localDataSource.redeemScannedTasks(taskIds)
    }

    override fun loadAllTaskIds(profileId: ProfileIdentifier): Flow<List<String>> =
        localDataSource.loadAllTaskIds(profileId)
}
