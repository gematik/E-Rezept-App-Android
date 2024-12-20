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

package de.gematik.ti.erp.app.mocks.pharmacy.model

import de.gematik.ti.erp.app.fhir.model.Pharmacy
import de.gematik.ti.erp.app.fhir.model.PharmacyAddress
import de.gematik.ti.erp.app.fhir.model.PharmacyContacts
import de.gematik.ti.erp.app.mocks.order.model.PHARMACY_ID
import de.gematik.ti.erp.app.mocks.order.model.PHARMACY_NAME
import de.gematik.ti.erp.app.mocks.order.model.TELEMATIK_ID
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData

val PHARMACY_DATA = PharmacyUseCaseData.Pharmacy(
    id = PHARMACY_ID,
    name = PHARMACY_NAME,
    address = "\n ",
    coordinates = null,
    distance = null,
    contacts = PharmacyContacts("", "", "", "", "", ""),
    provides = emptyList(),
    openingHours = null,
    telematikId = TELEMATIK_ID
)

val PHARMACY_DATA_FHIR = Pharmacy(
    id = PHARMACY_ID,
    name = PHARMACY_NAME,
    address = PharmacyAddress(emptyList(), "", ""),
    contacts = PharmacyContacts("", "", "", "", "", ""),
    provides = emptyList(),
    telematikId = TELEMATIK_ID
)
