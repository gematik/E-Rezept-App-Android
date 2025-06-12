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

package de.gematik.ti.erp.app.mocks.pharmacy.model

import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirContactInformationErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirPharmacyAddressErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.OpeningHoursErpModel
import de.gematik.ti.erp.app.mocks.order.model.PHARMACY_ID
import de.gematik.ti.erp.app.mocks.order.model.PHARMACY_NAME
import de.gematik.ti.erp.app.mocks.order.model.TELEMATIK_ID
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyContact

val PHARMACY_DATA = Pharmacy(
    id = PHARMACY_ID,
    name = PHARMACY_NAME,
    address = "",
    coordinates = null,
    distance = null,
    contact = PharmacyContact("", "", "", "", "", ""),
    provides = listOf(
        PharmacyUseCaseData.PharmacyService.LocalPharmacyService(
            name = PHARMACY_NAME,
            openingHours = PharmacyUseCaseData.OpeningHours(emptyMap())
        )
    ),
    openingHours = PharmacyUseCaseData.OpeningHours(emptyMap()),
    telematikId = TELEMATIK_ID
)

val PHARMACY_DATA_FHIR = FhirPharmacyErpModel(
    id = PHARMACY_ID,
    name = PHARMACY_NAME,
    address = FhirPharmacyAddressErpModel("", "", ""),
    contact = FhirContactInformationErpModel("", "", "", "", "", ""),
    specialities = emptyList(),
    position = null,
    availableTime = OpeningHoursErpModel(emptyMap()),
    telematikId = TELEMATIK_ID
)
