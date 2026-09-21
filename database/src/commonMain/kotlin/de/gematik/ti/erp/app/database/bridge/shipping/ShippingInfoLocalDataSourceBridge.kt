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
package de.gematik.ti.erp.app.database.bridge.shipping

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.ShippingInfoLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import kotlinx.coroutines.flow.Flow

class ShippingInfoLocalDataSourceBridge(
    private val shippingInfoLocalDataSourceV1: ShippingInfoLocalDataSource,
    private val shippingInfoLocalDataSourceV2: ShippingInfoLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : ShippingInfoLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    override fun observeShippingInfo(): Flow<ShippingInfoErpModel?> {
        return when {
            useRoom -> shippingInfoLocalDataSourceV2.observeShippingInfo()
            else -> shippingInfoLocalDataSourceV1.observeShippingInfo()
        }
    }

    override suspend fun getShippingInfo(): ShippingInfoErpModel? {
        return when {
            useRoom -> shippingInfoLocalDataSourceV2.getShippingInfo()
            else -> shippingInfoLocalDataSourceV1.getShippingInfo()
        }
    }

    override suspend fun saveShippingInfo(contact: ShippingInfoErpModel) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> shippingInfoLocalDataSourceV2.saveShippingInfo(contact)
            else -> shippingInfoLocalDataSourceV1.saveShippingInfo(contact)
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }

    override suspend fun deleteShippingInfo() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> shippingInfoLocalDataSourceV2.deleteShippingInfo()
            else -> shippingInfoLocalDataSourceV1.deleteShippingInfo()
        }.also {
            logger.logOperation(operationName, useRoom)
        }
    }
}
