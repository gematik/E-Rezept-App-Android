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
package de.gematik.ti.erp.app.database.bridge.task.communication

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.database.api.CommunicationLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.debug.model.DbMigrationFunctionalState
import de.gematik.ti.erp.app.debug.model.DbMigrationLogEntry
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

internal class CommunicationLocalDataSourceBridge(
    private val v1: CommunicationLocalDataSource,
    private val v2: CommunicationLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : CommunicationLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    private fun getCurrentDb() = if (useRoom) v2 else v1

    override fun loadDispReqCommunications(orderId: String): Flow<List<CommunicationErpModel>> =
        getCurrentDb().loadDispReqCommunications(orderId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun loadDispReqCommunicationsByProfileId(profileId: String): Flow<List<CommunicationErpModel>> =
        getCurrentDb().loadDispReqCommunicationsByProfileId(profileId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun loadDispReqCommunicationsByTaskId(taskId: String): Flow<List<CommunicationErpModel>> =
        getCurrentDb().loadDispReqCommunicationsByTaskId(taskId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun loadRepliedCommunications(taskIds: List<String>, telematikId: String?): Flow<List<CommunicationErpModel>> =
        getCurrentDb().loadRepliedCommunications(taskIds, telematikId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun loadRepliedCommunications(orderId: String?): Flow<List<CommunicationErpModel>> =
        getCurrentDb().loadRepliedCommunications(orderId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun loadRepliedCommunications(orderId: String, telematikId: String): Flow<List<CommunicationErpModel>> =
        getCurrentDb().loadRepliedCommunications(orderId, telematikId)

    override fun loadRepliedCommunicationsByProfileId(profileId: String): Flow<List<CommunicationErpModel>> {
        return getCurrentDb().loadRepliedCommunicationsByProfileId(profileId)
    }

    override fun loadAllRepliedCommunications(taskIds: List<String>): Flow<List<CommunicationErpModel>> =
        getCurrentDb().loadAllRepliedCommunications(taskIds).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun hasUnreadDispenseMessage(taskIds: List<String>, orderId: String): Flow<Boolean> =
        getCurrentDb().hasUnreadDispenseMessage(taskIds, orderId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun hasUnreadDispenseMessage(profileId: String): Flow<Boolean> =
        getCurrentDb().hasUnreadDispenseMessage(profileId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun unreadMessagesCount(): Flow<Long> =
        getCurrentDb().unreadMessagesCount().also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun getAllUnreadMessages(): Flow<List<CommunicationErpModel>> =
        getCurrentDb().getAllUnreadMessages().also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun unreadPrescriptionsInAllOrders(profileId: String): Flow<Long> =
        getCurrentDb().unreadPrescriptionsInAllOrders(profileId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun taskIdsByOrder(orderId: String): Flow<List<String>> =
        getCurrentDb().taskIdsByOrder(orderId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun getProfileIdByOrderId(orderId: String): Flow<String?> =
        getCurrentDb().getProfileIdByOrderId(orderId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun getProfileIdByTaskId(taskId: String): Flow<String?> =
        getCurrentDb().getProfileIdByTaskId(taskId).also {
            val op = getCurrentMethodName()
            runBlocking(Dispatchers.IO) {
                val r1 = v1.getProfileIdByTaskId(taskId).firstOrNull()
                val r2 = v2.getProfileIdByTaskId(taskId).firstOrNull()
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = op,
                        usesRoom = useRoom,
                        functionalState = DbMigrationFunctionalState.CheckFunctionalityForDifferentModels,
                        roomData = r2,
                        realmData = r1
                    )
                )
            }
        }

    override suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean) {
        val op = getCurrentMethodName()
        getCurrentDb().setCommunicationStatus(communicationId, consumed)
        logger.logOperation(op, useRoom)
    }

    override suspend fun updatePharmacyName(communicationId: String, pharmacyName: String) {
        val op = getCurrentMethodName()
        getCurrentDb().updatePharmacyName(communicationId, pharmacyName)
        logger.logOperation(op, useRoom)
    }

    override suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String) {
        val op = getCurrentMethodName()
        getCurrentDb().saveLocalCommunication(taskId, pharmacyId, transactionId)
        logger.logOperation(op, useRoom)
    }

    override suspend fun saveCommunications(communicationModels: List<CommunicationErpModel>): Int {
        val op = getCurrentMethodName()
        val result = getCurrentDb().saveCommunications(communicationModels)
        logger.logOperation(op, useRoom)
        return result
    }

    override suspend fun saveCommunications(entities: FhirCommunicationBundleErpModel): Int {
        val op = getCurrentMethodName()
        val result = getCurrentDb().saveCommunications(entities)
        logger.logOperation(op, useRoom)
        return result
    }

    override fun latestCommunicationTimestamp(profileId: String) =
        getCurrentDb().latestCommunicationTimestamp(profileId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }

    override fun hasUnreadRepliedMessages(taskIds: List<String>, telematikId: String?): Flow<Boolean> =
        getCurrentDb().hasUnreadRepliedMessages(taskIds, telematikId).also {
            val op = getCurrentMethodName()
            logger.logOperation(op, useRoom)
        }
}
