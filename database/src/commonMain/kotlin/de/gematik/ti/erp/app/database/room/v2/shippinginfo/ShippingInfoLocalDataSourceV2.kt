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
package de.gematik.ti.erp.app.database.room.v2.shippinginfo

import de.gematik.ti.erp.app.database.api.ShippingInfoLocalDataSource
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation storing a single shipping contact entry.
 */
class ShippingInfoLocalDataSourceV2(
    private val dao: ShippingInfoDao
) : ShippingInfoLocalDataSource {

    override fun observeShippingInfo(): Flow<ShippingInfoErpModel?> =
        dao.observeById(SINGLETON_ID).map { it?.toErp() }

    override suspend fun getShippingInfo(): ShippingInfoErpModel? =
        dao.getById(SINGLETON_ID)?.toErp()

    override suspend fun saveShippingInfo(contact: ShippingInfoErpModel) {
        dao.upsert(contact.toEntity())
    }

    override suspend fun deleteShippingInfo() {
        dao.deleteById(SINGLETON_ID)
    }

    private fun ShippingInfoEntity.toErp() = ShippingInfoErpModel(
        name = name,
        street = street,
        addressDetail = addressDetail,
        zip = zip,
        city = city,
        phone = phone,
        mail = mail,
        deliveryInfo = deliveryInfo
    )

    private fun ShippingInfoErpModel.toEntity() = ShippingInfoEntity(
        id = SINGLETON_ID,
        name = name,
        mail = mail,
        phone = phone,
        street = street,
        addressDetail = addressDetail,
        city = city,
        zip = zip,
        deliveryInfo = deliveryInfo
    )

    private companion object {
        const val SINGLETON_ID = "shipping-info-singleton"
    }
}
