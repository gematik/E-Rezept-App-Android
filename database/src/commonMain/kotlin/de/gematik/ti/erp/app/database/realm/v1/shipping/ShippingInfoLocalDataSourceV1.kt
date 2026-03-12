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
package de.gematik.ti.erp.app.database.realm.v1.shipping

import de.gematik.ti.erp.app.database.api.ShippingInfoLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.tryWrite
import de.gematik.ti.erp.app.database.realm.v1.AddressEntityV1
import de.gematik.ti.erp.app.database.realm.v1.SettingsEntityV1
import de.gematik.ti.erp.app.database.realm.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ShippingInfoLocalDataSourceV1(
    private val realm: Realm
) : ShippingInfoLocalDataSource {

    override fun observeShippingInfo(): Flow<ShippingInfoErpModel?> =
        realm.query<ShippingContactEntityV1>()
            .first()
            .asFlow()
            .map { it.obj?.toErpModel() }

    override suspend fun getShippingInfo(): ShippingInfoErpModel? =
        realm.queryFirst<ShippingContactEntityV1>()?.toErpModel()

    override suspend fun saveShippingInfo(contact: ShippingInfoErpModel) {
        realm.tryWrite<Unit> {
            queryFirst<SettingsEntityV1>()?.let { settings ->
                val shipping = settings.shippingContact
                    ?: copyToRealm(ShippingContactEntityV1()).also { settings.shippingContact = it }

                val address = shipping.address
                    ?: copyToRealm(AddressEntityV1()).also { shipping.address = it }

                shipping.updateFrom(contact, address)
            }
        }
    }

    override suspend fun deleteShippingInfo() {
        realm.tryWrite<Unit> {
            queryFirst<SettingsEntityV1>()?.let { settings ->
                settings.shippingContact?.let { delete(it) }
                settings.shippingContact = null
            }
        }
    }

    private fun ShippingContactEntityV1.updateFrom(contact: ShippingInfoErpModel, address: AddressEntityV1) {
        address.apply {
            line1 = contact.street
            line2 = contact.addressDetail
            postalCode = contact.zip
            city = contact.city
        }

        this.address = address
        this.name = contact.name
        this.telephoneNumber = contact.phone
        this.mail = contact.mail
        this.deliveryInformation = contact.deliveryInfo
    }

    private fun ShippingContactEntityV1.toErpModel() = ShippingInfoErpModel(
        name = this.name,
        street = this.address?.line1 ?: "",
        addressDetail = this.address?.line2 ?: "",
        zip = this.address?.postalCode ?: "",
        city = this.address?.city ?: "",
        phone = this.telephoneNumber,
        mail = this.mail,
        deliveryInfo = this.deliveryInformation
    )
}
