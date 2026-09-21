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

package de.gematik.ti.erp.app.mocks.pharmacy.model

import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirContactInformationErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyAddressErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.OpeningHoursErpModel
import de.gematik.ti.erp.app.mocks.order.model.PHARMACY_ID
import de.gematik.ti.erp.app.pharmacy.model.PharmacyOpeningHoursErpModel
import de.gematik.ti.erp.app.mocks.order.model.PHARMACY_NAME
import de.gematik.ti.erp.app.mocks.order.model.TELEMATIK_ID
import de.gematik.ti.erp.app.pharmacy.model.ContactInformationErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyDetailsErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyServiceErpModel

val PHARMACY_DATA = PharmacyDetailsErpModel(
    id = PHARMACY_ID,
    name = PHARMACY_NAME,
    address = "",
    coordinates = null,
    distance = null,
    contact = ContactInformationErpModel("", "", ""),
    provides = listOf(
        PharmacyServiceErpModel.LocalPharmacyServiceErpModel(
            name = PHARMACY_NAME,
            openingHours = PharmacyOpeningHoursErpModel(emptyMap())
        )
    ),
    openingHours = PharmacyOpeningHoursErpModel(emptyMap()),
    telematikId = TELEMATIK_ID
)

val PHARMACY_DATA_FHIR = FhirPharmacyErpModel(
    id = PHARMACY_ID,
    name = PHARMACY_NAME,
    address = FhirPharmacyAddressErpModel("", "", ""),
    contact = FhirContactInformationErpModel("", "", ""),
    specialities = emptyList(),
    position = null,
    availableTime = OpeningHoursErpModel(emptyMap()),
    telematikId = TELEMATIK_ID
)
