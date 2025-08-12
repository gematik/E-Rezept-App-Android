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

package de.gematik.ti.erp.app.fhir.prescription.mocks

import de.gematik.ti.erp.app.fhir.prescription.model.FhirMultiplePrescriptionInfoErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationRequestErpModel
import de.gematik.ti.erp.app.fhir.support.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.datetime.LocalDate

object FhirMedicationRequestErpTestData {
    val erpMedicationRequestModelV102 = FhirTaskKbvMedicationRequestErpModel(
        authoredOn = FhirTemporal.LocalDate(LocalDate.parse("2022-08-17")),
        dateOfAccident = FhirTemporal.LocalDate(LocalDate.parse("2022-06-29")),
        location = "Dummy-Betrieb",
        accidentType = FhirTaskAccidentType.WorkAccident,
        emergencyFee = false,
        additionalFee = "2",
        substitutionAllowed = true,
        dosageInstruction = "1-2-1-2-0",
        note = "Bitte laengliche Tabletten.",
        quantity = 1,
        multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
            indicator = true,
            numbering = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "1", unit = null),
                denominator = FhirQuantityErpModel(value = "4", unit = null)
            ),
            start = FhirTemporal.LocalDate(LocalDate.parse("2022-08-17")),
            end = FhirTemporal.LocalDate(LocalDate.parse("2022-11-25"))
        ),
        bvg = true
    )
    val erpMedicationRequestModelV110 = FhirTaskKbvMedicationRequestErpModel(
        authoredOn = FhirTemporal.LocalDate(LocalDate.parse("2022-05-20")),
        dateOfAccident = null,
        location = null,
        accidentType = FhirTaskAccidentType.None,
        emergencyFee = false,
        additionalFee = "0",
        substitutionAllowed = false,
        dosageInstruction = null,
        note = null,
        quantity = 1,
        multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
            indicator = true,
            numbering = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "1", unit = null),
                denominator = FhirQuantityErpModel(value = "4", unit = null)
            ),
            start = FhirTemporal.LocalDate(LocalDate.parse("2022-05-20")),
            end = FhirTemporal.LocalDate(LocalDate.parse("2022-06-30"))
        ),
        bvg = false
    )

    val erpMedicationRequestWithAccidentInfoModelV110 = FhirTaskKbvMedicationRequestErpModel(
        authoredOn = FhirTemporal.LocalDate(LocalDate.parse("2025-04-14")),
        dateOfAccident = FhirTemporal.LocalDate(LocalDate.parse("2024-07-01")),
        location = null,
        accidentType = FhirTaskAccidentType.Accident,
        emergencyFee = false,
        additionalFee = "0",
        substitutionAllowed = true,
        dosageInstruction = null,
        note = null,
        quantity = 1,
        multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
            indicator = false,
            numbering = null,
            start = null,
            end = null
        ),
        bvg = false
    )
}
