/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.demomode.repository.pharmacy

import de.gematik.ti.erp.app.pharmacy.model.PharmacyData
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DemoShippingContactRepository : ShippingContactRepository {
    override fun shippingContact(): Flow<PharmacyData.ShippingContact?> {
        return flowOf(
            PharmacyData.ShippingContact(
                name = "Helga Schmetterling",
                line1 = "Schmetterlingweg 1",
                line2 = "2 Stockwerk rechts",
                postalCode = "12345",
                city = "Berlin",
                telephoneNumber = "123456789",
                mail = "schmetterling@butterfly.com",
                deliveryInformation = "Bitte klingeln"
            )
        )
    }

    override suspend fun saveShippingContact(contact: PharmacyData.ShippingContact) {
        // do nothing
    }
}
