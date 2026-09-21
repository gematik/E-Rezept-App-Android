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

package de.gematik.ti.erp.app.database.bridge

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.communication.model.InternalMessageErpModel
import de.gematik.ti.erp.app.database.api.InternalMessagesLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import kotlinx.coroutines.flow.Flow

class InternalMessageLocalDataBridge(
    private val v1: InternalMessagesLocalDataSource,
    private val v2: InternalMessagesLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : InternalMessagesLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()
    private fun getCurrentDb() = if (useRoom) v2 else v1

    override fun getInternalMessages(): Flow<List<InternalMessageErpModel>> =
        getCurrentDb().getInternalMessages().also {
            logger.logOperation(getCurrentMethodName(), useRoom)
        }

    override fun getUnreadInternalMessagesCount(): Flow<Long> =
        getCurrentDb().getUnreadInternalMessagesCount().also {
            logger.logOperation(getCurrentMethodName(), useRoom)
        }

    override fun getLastUpdatedVersion(): Flow<String?> =
        getCurrentDb().getLastUpdatedVersion().also {
            logger.logOperation(getCurrentMethodName(), useRoom)
        }

    override suspend fun setInternalMessagesAsRead() {
        val op = getCurrentMethodName()
        getCurrentDb().setInternalMessagesAsRead()
        logger.logOperation(op, useRoom)
    }

    override suspend fun updateInternalMessage(updatedModel: InternalMessageErpModel) {
        val op = getCurrentMethodName()
        getCurrentDb().updateInternalMessage(updatedModel)
        logger.logOperation(op, useRoom)
    }

    override suspend fun saveInternalMessage(internalMessageErpModel: InternalMessageErpModel) {
        val op = getCurrentMethodName()
        getCurrentDb().saveInternalMessage(internalMessageErpModel)
        logger.logOperation(op, useRoom)
    }
}
