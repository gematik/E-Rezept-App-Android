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

package de.gematik.ti.erp.app.database.bridge.eurezept

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.eurezept.EuTaskLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.debug.model.DbMigrationFunctionalState.CheckFunctionalityForDifferentModels
import de.gematik.ti.erp.app.debug.model.DbMigrationFunctionalState.OperationNoCheck
import de.gematik.ti.erp.app.debug.model.DbMigrationLogEntry
import de.gematik.ti.erp.app.eurezept.model.EuAccessCodeErpModel
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrderErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class EuTaskLocalDataSourceBridge(
    private val euTaskLocalDataSourceV1: EuTaskLocalDataSource,
    private val euTaskLocalDataSourceV2: EuTaskLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : EuTaskLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    override fun observeEuOrder(orderId: String): Flow<EuOrderErpModel?> = flow {
        val operationName = getCurrentMethodName()
        val v1Val = euTaskLocalDataSourceV1.observeEuOrder(orderId).firstOrNull()
        val v2Val = euTaskLocalDataSourceV2.observeEuOrder(orderId).firstOrNull()

        logger.addLog(
            DbMigrationLogEntry(
                operation = operationName,
                usesRoom = useRoom,
                functionalState = CheckFunctionalityForDifferentModels,
                roomData = v2Val?.toString(),
                realmData = v1Val?.toString()
            )
        )
        emitAll(if (useRoom) euTaskLocalDataSourceV2.observeEuOrder(orderId) else euTaskLocalDataSourceV1.observeEuOrder(orderId))
    }

    override fun observeAllEuOrders(): Flow<List<EuOrderErpModel>> {
        return when {
            useRoom -> euTaskLocalDataSourceV2.observeAllEuOrders()
            else -> euTaskLocalDataSourceV1.observeAllEuOrders()
        }
    }

    override fun getLatestEuAccessCodeByProfileIdAndCountry(
        profileId: ProfileIdentifier,
        countryCode: String
    ): Flow<EuAccessCodeErpModel?> {
        return when {
            useRoom -> euTaskLocalDataSourceV2.getLatestEuAccessCodeByProfileIdAndCountry(profileId, countryCode)
            else -> euTaskLocalDataSourceV1.getLatestEuAccessCodeByProfileIdAndCountry(profileId, countryCode)
        }
    }

    override fun getOrdersForProfileCountryAndTasks(
        profileId: ProfileIdentifier,
        countryCode: String,
        taskIds: List<String>
    ): Flow<List<EuOrderErpModel>> {
        return when {
            useRoom -> euTaskLocalDataSourceV2.getOrdersForProfileCountryAndTasks(profileId, countryCode, taskIds)
            else -> euTaskLocalDataSourceV1.getOrdersForProfileCountryAndTasks(profileId, countryCode, taskIds)
        }
    }

    override suspend fun deleteEuAccessCodeByProfileId(profileId: ProfileIdentifier) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> euTaskLocalDataSourceV2.deleteEuAccessCodeByProfileId(profileId)
            else -> euTaskLocalDataSourceV1.deleteEuAccessCodeByProfileId(profileId)
        }.also {
            logger.addLog(
                DbMigrationLogEntry(
                    operation = operationName,
                    usesRoom = useRoom,
                    functionalState = OperationNoCheck,
                    roomData = if (useRoom) "Deleting access code for profileId: $profileId" else null,
                    realmData = if (!useRoom) "Deleting access code for profileId: $profileId" else null
                )
            )
        }
    }

    override suspend fun saveEuOrder(euOrder: EuOrderErpModel, eventType: EuEventType) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> euTaskLocalDataSourceV2.saveEuOrder(euOrder, eventType)
            else -> euTaskLocalDataSourceV1.saveEuOrder(euOrder, eventType)
        }.also {
            logger.addLog(
                DbMigrationLogEntry(
                    operation = operationName,
                    usesRoom = useRoom,
                    functionalState = OperationNoCheck,
                    roomData = if (useRoom) "Saving order: ${euOrder.orderId} with type: ${eventType.name}" else null,
                    realmData = if (!useRoom) "Saving order: ${euOrder.orderId} with type: ${eventType.name}" else null
                )
            )
        }
    }

    override suspend fun markEventsAsRead(eventIds: List<String>) {
        when {
            useRoom -> euTaskLocalDataSourceV2.markEventsAsRead(eventIds)
            else -> euTaskLocalDataSourceV1.markEventsAsRead(eventIds)
        }
    }

    override suspend fun addEventToValidOrders(
        profileId: ProfileIdentifier,
        taskIds: List<String>,
        eventType: EuEventType
    ) {
        when {
            useRoom -> euTaskLocalDataSourceV2.addEventToValidOrders(profileId, taskIds, eventType)
            else -> euTaskLocalDataSourceV1.addEventToValidOrders(profileId, taskIds, eventType)
        }
    }

    override suspend fun addRedeemedEventIfValidOrderExists(
        profileId: ProfileIdentifier,
        countryCode: String,
        taskId: String
    ) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> euTaskLocalDataSourceV2.addRedeemedEventIfValidOrderExists(profileId, countryCode, taskId)
            else -> euTaskLocalDataSourceV1.addRedeemedEventIfValidOrderExists(profileId, countryCode, taskId)
        }.also {
            logger.addLog(
                DbMigrationLogEntry(
                    operation = operationName,
                    usesRoom = useRoom,
                    functionalState = OperationNoCheck,
                    roomData = if (useRoom) "Adding TASK_REDEEMED event for profile=$profileId, country=$countryCode, task=$taskId" else null,
                    realmData = if (!useRoom) "Adding TASK_REDEEMED event for profile=$profileId, country=$countryCode, task=$taskId" else null
                )
            )
        }
    }

    override fun getEuAccessCode(accessCode: String): Flow<EuAccessCodeErpModel?> = flow {
        val operationName = getCurrentMethodName()
        val v1Val = euTaskLocalDataSourceV1.getEuAccessCode(accessCode).firstOrNull()
        val v2Val = euTaskLocalDataSourceV2.getEuAccessCode(accessCode).firstOrNull()

        logger.addLog(
            DbMigrationLogEntry(
                operation = operationName,
                usesRoom = useRoom,
                functionalState = CheckFunctionalityForDifferentModels,
                roomData = v2Val?.toString(),
                realmData = v1Val?.toString()
            )
        )
        emitAll(if (useRoom) euTaskLocalDataSourceV2.getEuAccessCode(accessCode) else euTaskLocalDataSourceV1.getEuAccessCode(accessCode))
    }
}
