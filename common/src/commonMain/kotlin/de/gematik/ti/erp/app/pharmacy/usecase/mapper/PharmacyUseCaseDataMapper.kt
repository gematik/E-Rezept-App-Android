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

package de.gematik.ti.erp.app.pharmacy.usecase.mapper

import de.gematik.ti.erp.app.fhir.model.Pharmacy
import de.gematik.ti.erp.app.fhir.model.PharmacyService
import de.gematik.ti.erp.app.pharmacy.model.PharmacyData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData

// can't be modified; the backend will always return 80 entries on the first page
const val PharmacyInitialResultsPerPage = 80
const val PharmacyNextResultsPerPage = 10

fun List<Pharmacy>.toModel(): List<PharmacyUseCaseData.Pharmacy> =
    map { pharmacy ->
        PharmacyUseCaseData.Pharmacy(
            id = pharmacy.id,
            name = pharmacy.name,
            address = pharmacy.address.let {
                "${it.lines.joinToString()}\n${it.postalCode} ${it.city}"
            },
            coordinates = pharmacy.coordinates,
            distance = null,
            contacts = pharmacy.contacts,
            provides = pharmacy.provides,
            openingHours = (
                pharmacy.provides.find {
                    it is PharmacyService.LocalPharmacyService
                } as? PharmacyService.LocalPharmacyService
                )?.openingHours,
            telematikId = pharmacy.telematikId
        )
    }

fun PharmacyData.ShippingContact.toModel() =
    PharmacyUseCaseData.ShippingContact(
        name = name,
        line1 = line1,
        line2 = line2,
        postalCode = postalCode,
        city = city,
        telephoneNumber = telephoneNumber,
        mail = mail,
        deliveryInformation = deliveryInformation
    )
