/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.pharmacy.model.PharmacyData
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ShippingContactRepository(
    private val dispatchers: DispatchProvider,
    private val realm: Realm
) {
    fun shippingContact(): Flow<PharmacyData.ShippingContact?> =
        realm.query<ShippingContactEntityV1>()
            .first()
            .asFlow()
            .map {
                it.obj?.toShippingContact()
            }
            .flowOn(dispatchers.IO)

    suspend fun saveShippingContact(contact: PharmacyData.ShippingContact) {
        withContext(dispatchers.IO) {
            realm.write {
                queryFirst<SettingsEntityV1>()?.let { settings ->
                    val shippingContact = settings.shippingContact
                        ?: copyToRealm(ShippingContactEntityV1()).also {
                            settings.shippingContact = it
                        }

                    shippingContact.let {
                        it.address!!.line1 = contact.line1
                        it.address!!.line2 = contact.line2
                        it.address!!.postalCode = contact.postalCode
                        it.address!!.city = contact.city
                        it.name = contact.name
                        it.telephoneNumber = contact.telephoneNumber
                        it.mail = contact.mail
                        it.deliveryInformation = contact.deliveryInformation
                    }
                }
            }
        }
    }
}

fun ShippingContactEntityV1.toShippingContact() =
    PharmacyData.ShippingContact(
        name = this.name,
        line1 = this.address!!.line1,
        line2 = this.address!!.line2,
        postalCode = this.address!!.postalCode,
        city = this.address!!.city,
        telephoneNumber = this.telephoneNumber,
        mail = this.mail,
        deliveryInformation = this.deliveryInformation
    )
