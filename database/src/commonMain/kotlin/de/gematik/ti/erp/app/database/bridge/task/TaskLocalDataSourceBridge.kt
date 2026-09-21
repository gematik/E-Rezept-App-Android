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

package de.gematik.ti.erp.app.database.bridge.task

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.model.SaveTaskResult
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSource
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceCommon
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceDiga
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceScanned
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceSynced
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.FhirMedicationDispenseErpModelCollection
import de.gematik.ti.erp.app.fhir.FhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.FhirTaskMetaDataErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskStatusErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

class TaskLocalDataSourceBridge(
    private val taskLocalDataSourceV1: TaskLocalDataSource,
    private val taskLocalDataSourceV2: TaskLocalDataSource,
    protected val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : TaskLocalDataSource,
    TaskLocalDataSourceCommon by TaskLocalDataSourceBridgeCommon(taskLocalDataSourceV1, taskLocalDataSourceV2, logger, roomFeatureToggle),
    TaskLocalDataSourceScanned by TaskLocalDataSourceBridgeScanned(taskLocalDataSourceV1, taskLocalDataSourceV2, logger, roomFeatureToggle),
    TaskLocalDataSourceSynced by TaskLocalDataSourceBridgeSynced(taskLocalDataSourceV1, taskLocalDataSourceV2, logger, roomFeatureToggle),
    TaskLocalDataSourceDiga by TaskLocalDataSourceBridgeDiga(taskLocalDataSourceV1, taskLocalDataSourceV2, logger, roomFeatureToggle) {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    override suspend fun saveTask(task: TaskErpModel) {
        if (useRoom) {
            taskLocalDataSourceV2.saveTask(task)
        } else {
            taskLocalDataSourceV1.saveTask(task)
        }
    }
}

private abstract class TaskLocalDataSourceBridgeDelegate(
    protected val taskLocalDataSourceV1: TaskLocalDataSource,
    protected val taskLocalDataSourceV2: TaskLocalDataSource,
    protected val logger: DbMigrationLogHolder,
    protected val roomFeatureToggle: RoomFeatureToggle
) {

    protected val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    protected fun <T> chooseFlow(
        realmFlowProvider: () -> Flow<T>,
        roomFlowProvider: () -> Flow<T>
    ): Flow<T> =
        if (useRoom) {
            roomFlowProvider()
        } else {
            realmFlowProvider()
        }
}

private class TaskLocalDataSourceBridgeCommon(
    taskLocalDataSourceV1: TaskLocalDataSource,
    taskLocalDataSourceV2: TaskLocalDataSource,
    logger: DbMigrationLogHolder,
    roomFeatureToggle: RoomFeatureToggle
) : TaskLocalDataSourceBridgeDelegate(taskLocalDataSourceV1, taskLocalDataSourceV2, logger, roomFeatureToggle),
    TaskLocalDataSourceCommon {

    override fun loadTaskListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel>> {
        val operationName = getCurrentMethodName()
        val realmFlow = { taskLocalDataSourceV1.loadTaskListByProfileId(profileId) }
        val roomFlow = { taskLocalDataSourceV2.loadTaskListByProfileId(profileId) }
        logger.logOperation(operationName, useRoom)
        return chooseFlow(realmFlow, roomFlow)
    }

    override fun loadTaskListByTaskIdList(taskIds: List<String>): Flow<List<TaskErpModel>> {
        val operationName = getCurrentMethodName()
        val realmFlow = { taskLocalDataSourceV1.loadTaskListByTaskIdList(taskIds) }
        val roomFlow = { taskLocalDataSourceV2.loadTaskListByTaskIdList(taskIds) }
        logger.logOperation(operationName, useRoom)
        return chooseFlow(realmFlow, roomFlow)
    }

    override fun loadTaskByTaskId(taskId: String): Flow<TaskErpModel?> {
        val operationName = getCurrentMethodName()
        val realmFlow = taskLocalDataSourceV1.loadTaskByTaskId(taskId)
        val roomFlow = taskLocalDataSourceV2.loadTaskByTaskId(taskId)
        logger.logOperation(operationName, useRoom)
        return chooseFlow({ realmFlow }, { roomFlow })
    }

    override suspend fun deleteTaskByTaskId(taskId: String) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.deleteTaskByTaskId(taskId)
        } else {
            taskLocalDataSourceV1.deleteTaskByTaskId(taskId)
        }
        logger.logOperation(operationName, useRoom)
    }

    override fun loadATaskIdStringList(): Flow<List<String>> {
        val operationName = getCurrentMethodName()
        val realmFlow = { taskLocalDataSourceV1.loadATaskIdStringList() }
        val roomFlow = { taskLocalDataSourceV2.loadATaskIdStringList() }
        logger.logOperation(operationName, useRoom)
        return chooseFlow(realmFlow, roomFlow)
    }

    override fun loadTaskIdStringListByProfileId(profileId: ProfileIdentifier): Flow<List<String>> {
        val operationName = getCurrentMethodName()
        val realmFlow = { taskLocalDataSourceV1.loadTaskIdStringListByProfileId(profileId) }
        val roomFlow = { taskLocalDataSourceV2.loadTaskIdStringListByProfileId(profileId) }
        logger.logOperation(operationName, useRoom)
        return chooseFlow(realmFlow, roomFlow)
    }

    override suspend fun deleteTasksByProfileId(profileId: ProfileIdentifier) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.deleteTasksByProfileId(profileId)
        } else {
            taskLocalDataSourceV1.deleteTasksByProfileId(profileId)
        }
        logger.logOperation(operationName, useRoom)
    }

    override suspend fun deleteCommunicationsByProfileId(profileId: ProfileIdentifier) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.deleteCommunicationsByProfileId(profileId)
        } else {
            taskLocalDataSourceV1.deleteCommunicationsByProfileId(profileId)
        }
        logger.logOperation(operationName, useRoom)
    }

    override suspend fun deleteInvoicesByProfileId(profileId: ProfileIdentifier) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.deleteInvoicesByProfileId(profileId)
        } else {
            taskLocalDataSourceV1.deleteInvoicesByProfileId(profileId)
        }
        logger.logOperation(operationName, useRoom)
    }

    override suspend fun saveCommunications(communicationModel: FhirCommunicationBundleErpModel): Int {
        val result = if (useRoom) {
            taskLocalDataSourceV2.saveCommunications(communicationModel)
        } else {
            taskLocalDataSourceV1.saveCommunications(communicationModel)
        }
        return result
    }
}

private class TaskLocalDataSourceBridgeScanned(
    taskLocalDataSourceV1: TaskLocalDataSource,
    taskLocalDataSourceV2: TaskLocalDataSource,
    logger: DbMigrationLogHolder,
    roomFeatureToggle: RoomFeatureToggle
) : TaskLocalDataSourceBridgeDelegate(taskLocalDataSourceV1, taskLocalDataSourceV2, logger, roomFeatureToggle),
    TaskLocalDataSourceScanned {

    override suspend fun redeemScannedTaskListByTaskIdList(taskIds: List<String>) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.redeemScannedTaskListByTaskIdList(taskIds)
        } else {
            taskLocalDataSourceV1.redeemScannedTaskListByTaskIdList(taskIds)
        }
        logger.logOperation(operationName, useRoom)
    }

    override suspend fun saveScannedTaskList(
        profileId: ProfileIdentifier,
        tasks: List<TaskErpModel.Scanned>,
        medicationString: String
    ) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.saveScannedTaskList(profileId, tasks, medicationString)
        } else {
            taskLocalDataSourceV1.saveScannedTaskList(profileId, tasks, medicationString)
        }
        logger.logOperation(operationName, useRoom)
    }

    override suspend fun updateScannedTaskName(taskId: String, name: String) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.updateScannedTaskName(taskId, name)
        } else {
            taskLocalDataSourceV1.updateScannedTaskName(taskId, name)
        }
        logger.logOperation(operationName, useRoom)
    }

    override fun loadScannedPrescriptionListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Scanned>> {
        val operationName = getCurrentMethodName()
        val realmFlow = { taskLocalDataSourceV1.loadScannedPrescriptionListByProfileId(profileId) }
        val roomFlow = { taskLocalDataSourceV2.loadScannedPrescriptionListByProfileId(profileId) }
        logger.logOperation(operationName, useRoom)
        return chooseFlow(realmFlow, roomFlow)
    }

    override fun loadScannedTaskListByTaskIdList(taskIds: List<String>): Flow<List<TaskErpModel.Scanned>> {
        val operationName = getCurrentMethodName()
        val realmFlow = { taskLocalDataSourceV1.loadScannedTaskListByTaskIdList(taskIds) }
        val roomFlow = { taskLocalDataSourceV2.loadScannedTaskListByTaskIdList(taskIds) }
        logger.logOperation(operationName, useRoom)
        return chooseFlow(realmFlow, roomFlow)
    }

    override fun loadScannedPrescriptionByTaskId(taskId: String): Flow<TaskErpModel.Scanned?> {
        val operationName = getCurrentMethodName()
        val realmFlow = { taskLocalDataSourceV1.loadScannedPrescriptionByTaskId(taskId) }
        val roomFlow = { taskLocalDataSourceV2.loadScannedPrescriptionByTaskId(taskId) }
        logger.logOperation(operationName, useRoom)
        return chooseFlow(realmFlow, roomFlow)
    }

    override suspend fun saveCommunicationForScannedTask(taskId: String, pharmacyId: String, transactionId: String) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.saveCommunicationForScannedTask(taskId, pharmacyId, transactionId)
        } else {
            taskLocalDataSourceV1.saveCommunicationForScannedTask(taskId, pharmacyId, transactionId)
        }
        logger.logOperation(operationName, useRoom)
    }

    override suspend fun updateScannedTaskRedeemedOn(taskId: String, timestamp: Instant?) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.updateScannedTaskRedeemedOn(taskId, timestamp)
        } else {
            taskLocalDataSourceV1.updateScannedTaskRedeemedOn(taskId, timestamp)
        }
        logger.logOperation(operationName, useRoom)
    }
}

private class TaskLocalDataSourceBridgeSynced(
    taskLocalDataSourceV1: TaskLocalDataSource,
    taskLocalDataSourceV2: TaskLocalDataSource,
    logger: DbMigrationLogHolder,
    roomFeatureToggle: RoomFeatureToggle
) : TaskLocalDataSourceBridgeDelegate(taskLocalDataSourceV1, taskLocalDataSourceV2, logger, roomFeatureToggle),
    TaskLocalDataSourceSynced {

    override fun getLatestTaskModifiedTimestamp(profileId: ProfileIdentifier): Flow<Instant?> {
        val operationName = getCurrentMethodName()
        val realmFlow = { taskLocalDataSourceV1.getLatestTaskModifiedTimestamp(profileId) }
        val roomFlow = { taskLocalDataSourceV2.getLatestTaskModifiedTimestamp(profileId) }
        logger.logOperation(operationName, useRoom)
        return chooseFlow(realmFlow, roomFlow)
    }

    override suspend fun updateSyncedTaskStatus(
        taskId: String,
        status: FhirTaskStatusErpModel,
        lastModified: FhirTemporal?
    ) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.updateSyncedTaskStatus(taskId, status, lastModified)
        } else {
            taskLocalDataSourceV1.updateSyncedTaskStatus(taskId, status, lastModified)
        }
        logger.logOperation(operationName, useRoom)
    }

    override suspend fun markSyncedTaskAsIncomplete(
        taskId: String,
        error: Throwable,
        originalBundle: JsonElement
    ): Result<Unit> {
        val operationName = getCurrentMethodName()
        val result = if (useRoom) {
            taskLocalDataSourceV2.markSyncedTaskAsIncomplete(taskId, error, originalBundle)
        } else {
            taskLocalDataSourceV1.markSyncedTaskAsIncomplete(taskId, error, originalBundle)
        }
        logger.logOperation(operationName, useRoom)
        return result
    }

    override suspend fun saveSyncedTaskMetaData(
        profileId: ProfileIdentifier,
        model: FhirTaskMetaDataErpModel
    ): Result<Unit> {
        val operationName = getCurrentMethodName()
        val result = if (useRoom) {
            taskLocalDataSourceV2.saveSyncedTaskMetaData(profileId, model)
        } else {
            taskLocalDataSourceV1.saveSyncedTaskMetaData(profileId, model)
        }
        logger.logOperation(operationName, useRoom)
        return result
    }

    override suspend fun saveSyncedTaskKBVData(
        taskId: String,
        model: FhirTaskDataErpModel
    ): Result<SaveTaskResult> {
        val operationName = getCurrentMethodName()
        val result = if (useRoom) {
            taskLocalDataSourceV2.saveSyncedTaskKBVData(taskId, model)
        } else {
            taskLocalDataSourceV1.saveSyncedTaskKBVData(taskId, model)
        }
        logger.logOperation(operationName, useRoom)
        return result
    }

    override suspend fun saveSyncedTaskMedicationDispense(
        taskId: String,
        dispenseCollection: FhirMedicationDispenseErpModelCollection
    ) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.saveSyncedTaskMedicationDispense(taskId, dispenseCollection)
        } else {
            taskLocalDataSourceV1.saveSyncedTaskMedicationDispense(taskId, dispenseCollection)
        }
        logger.logOperation(operationName, useRoom)
    }
}

private class TaskLocalDataSourceBridgeDiga(
    taskLocalDataSourceV1: TaskLocalDataSource,
    taskLocalDataSourceV2: TaskLocalDataSource,
    logger: DbMigrationLogHolder,
    roomFeatureToggle: RoomFeatureToggle
) : TaskLocalDataSourceBridgeDelegate(taskLocalDataSourceV1, taskLocalDataSourceV2, logger, roomFeatureToggle),
    TaskLocalDataSourceDiga {

    override suspend fun setDigaAsNotNew(taskId: String) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.setDigaAsNotNew(taskId)
        } else {
            taskLocalDataSourceV1.setDigaAsNotNew(taskId)
        }
        logger.logOperation(operationName, useRoom)
    }

    override suspend fun updateDigaStatus(taskId: String, status: DigaStatus, lastModified: Instant?) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.updateDigaStatus(taskId, status, lastModified)
        } else {
            taskLocalDataSourceV1.updateDigaStatus(taskId, status, lastModified)
        }
        logger.logOperation(operationName, useRoom)
    }

    override suspend fun updateDigaArchiveStatus(taskId: String, lastModified: Instant, isArchive: Boolean) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.updateDigaArchiveStatus(taskId, lastModified, isArchive)
        } else {
            taskLocalDataSourceV1.updateDigaArchiveStatus(taskId, lastModified, isArchive)
        }
        logger.logOperation(operationName, useRoom)
    }

    override suspend fun updateDigaCommunicationSent(taskId: String, time: Instant) {
        val operationName = getCurrentMethodName()
        if (useRoom) {
            taskLocalDataSourceV2.updateDigaCommunicationSent(taskId, time)
        } else {
            taskLocalDataSourceV1.updateDigaCommunicationSent(taskId, time)
        }
        logger.logOperation(operationName, useRoom)
    }

    override fun loadDigaByTaskId(taskId: String): Flow<TaskErpModel.Synced.Diga?> {
        val operationName = getCurrentMethodName()
        val realmFlow = { taskLocalDataSourceV1.loadDigaByTaskId(taskId) }
        val roomFlow = { taskLocalDataSourceV2.loadDigaByTaskId(taskId) }
        logger.logOperation(operationName, useRoom)
        return chooseFlow(realmFlow, roomFlow)
    }

    override fun loadDigaListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Diga>> {
        val operationName = getCurrentMethodName()
        val realmFlow = { taskLocalDataSourceV1.loadDigaListByProfileId(profileId) }
        val roomFlow = { taskLocalDataSourceV2.loadDigaListByProfileId(profileId) }
        logger.logOperation(operationName, useRoom)
        return chooseFlow(realmFlow, roomFlow)
    }

    override fun loadArchiveDigaListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Diga>> {
        val operationName = getCurrentMethodName()
        val realmFlow = { taskLocalDataSourceV1.loadArchiveDigaListByProfileId(profileId) }
        val roomFlow = { taskLocalDataSourceV2.loadArchiveDigaListByProfileId(profileId) }
        logger.logOperation(operationName, useRoom)
        return chooseFlow(realmFlow, roomFlow)
    }
}
