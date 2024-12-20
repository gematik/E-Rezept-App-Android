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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.pharmacy.model.PharmacyData
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SaveShippingContactUseCase(
    private val repository: ShippingContactRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(contact: PharmacyUseCaseData.ShippingContact) {
        withContext(dispatcher) {
            repository.saveShippingContact(contact.model())
        }
    }

    companion object {
        private fun PharmacyUseCaseData.ShippingContact.model() =
            PharmacyData.ShippingContact(
                name = name.trim(),
                line1 = line1.trim(),
                line2 = line2.trim(),
                postalCode = postalCode.trim(),
                city = city.trim(),
                telephoneNumber = telephoneNumber.trim(),
                mail = mail.trim(),
                deliveryInformation = deliveryInformation.trim()
            )
    }
}
