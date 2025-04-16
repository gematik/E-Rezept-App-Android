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

package de.gematik.ti.erp.app.fhir.prescription.mocks

import de.gematik.ti.erp.app.fhir.common.model.erp.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirCoverageErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMultiplePrescriptionInfoErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvAddressErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvMedicationRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPractitionerErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskMedicationCategoryErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.RequestIntent
import de.gematik.ti.erp.app.utils.FhirTemporal.LocalDate
import de.gematik.ti.erp.app.utils.FhirTemporalSerializationType

object FhirTaskDataErpTestData {
    val fhirTaskDataErpModel = FhirTaskDataErpModel(
        pvsId = "Y/400/2107/36/999",
        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = LocalDate(
                value = kotlinx.datetime.LocalDate.parse("2025-01-16"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            dateOfAccident = null,
            location = null,
            accidentType = FhirTaskAccidentType.None,
            emergencyFee = false,
            additionalFee = "0",
            substitutionAllowed = true,
            dosageInstruction = null,
            note = "Bitte auf Anwendung schulen",
            quantity = 2,
            multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                indicator = false,
                numbering = null,
                start = null,
                end = null
            ),
            bvg = false
        ),
        medication = FhirTaskKbvMedicationErpModel(
            text = "Umgebung TU 169",
            type = "Medication_PZN",
            version = "1.1.0",
            form = "IHP",
            medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "1", unit = "Diskus"),
                denominator = FhirQuantityErpModel(value = "1", unit = "")
            ),
            isVaccine = false,
            normSizeCode = "N1",
            compoundingInstructions = null,
            compoundingPackaging = null,
            ingredients = emptyList(),
            identifier = FhirMedicationIdentifierErpModel(
                pzn = "00427833",
                atc = null,
                ask = null,
                snomed = null
            ),
            lotNumber = null,
            expirationDate = null
        ),
        patient = FhirTaskKbvPatientErpModel(
            name = "Ludger Königsstein",
            birthDate = LocalDate(
                value = kotlinx.datetime.LocalDate.parse("1935-06-22"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            address = FhirTaskKbvAddressErpModel(
                streetName = "Blumenweg",
                houseNumber = null,
                postalCode = "26427",
                city = "Esens"
            ),
            insuranceInformation = "X110411675"
        ),
        practitioner = FhirTaskKbvPractitionerErpModel(
            name = "Alexander Fischer",
            qualification = "Facharzt für Innere Medizin",
            practitionerIdentifier = null
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "MVZ",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Herbert-Lewin-Platz",
                houseNumber = "2",
                postalCode = "10623",
                city = "Berlin"
            ),
            bsnr = "721111100",
            iknr = null,
            phone = "0301234567",
            mail = "mvz@e-mail.de"
        ),
        coverage = FhirCoverageErpModel(
            name = "AOK Nordost",
            statusCode = "5",
            coverageType = "GKV",
            identifierNumber = "109719018"
        ),
        deviceRequest = null
    )

    val fhirTaskDataErpModelV2 = FhirTaskDataErpModel(
        pvsId = "Y/400/2107/36/999",
        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = LocalDate(
                value = kotlinx.datetime.LocalDate.parse("2025-01-17"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            dateOfAccident = null,
            location = null,
            accidentType = FhirTaskAccidentType.None,
            emergencyFee = false,
            additionalFee = "0",
            substitutionAllowed = true,
            dosageInstruction = "1-0-0-0",
            note = null,
            quantity = 1,
            multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                indicator = false,
                numbering = null,
                start = null,
                end = null
            ),
            bvg = false
        ),
        medication = FhirTaskKbvMedicationErpModel(
            text = "Zampa-Zok® mite 47,5 mg, 30 Retardtabletten N1",
            type = "Medication_PZN",
            version = "1.1.0",
            form = "RET",
            medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "30", unit = "Stück"),
                denominator = FhirQuantityErpModel(value = "1", unit = "")
            ),
            isVaccine = false,
            normSizeCode = "N1",
            compoundingInstructions = null,
            compoundingPackaging = null,
            ingredients = emptyList(),
            identifier = FhirMedicationIdentifierErpModel(
                pzn = "03879429",
                atc = null,
                ask = null,
                snomed = null
            ),
            lotNumber = null,
            expirationDate = null
        ),
        patient = FhirTaskKbvPatientErpModel(
            name = "Paula Privati",
            birthDate = LocalDate(
                value = kotlinx.datetime.LocalDate.parse("1935-06-22"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            address = FhirTaskKbvAddressErpModel(
                streetName = "Blumenweg",
                houseNumber = null,
                postalCode = "26427",
                city = "Esens"
            ),
            insuranceInformation = "X110411675"
        ),
        practitioner = FhirTaskKbvPractitionerErpModel(
            name = "Dr. med. Emma Schneider",
            qualification = "Fachärztin für Innere Medizin",
            practitionerIdentifier = "999999900"
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "MVZ",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Herbert-Lewin-Platz",
                houseNumber = "2",
                postalCode = "10623",
                city = "Berlin"
            ),
            bsnr = "999999900",
            iknr = null,
            phone = "0301234567",
            mail = "mvz@e-mail.de"
        ),
        coverage = FhirCoverageErpModel(
            name = "Allianz Private Krankenversicherung",
            statusCode = "1",
            coverageType = "PKV",
            identifierNumber = "168140346"
        ),
        deviceRequest = null
    )

    val fhirTaskDataErpModelV2WithDifferentFullUrl = FhirTaskDataErpModel(
        pvsId = "Y/400/2410/36/280",
        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = LocalDate(value = kotlinx.datetime.LocalDate.parse("2025-02-17"), type = FhirTemporalSerializationType.FhirTemporalLocalDate),
            dateOfAccident = null,
            location = null,
            accidentType = FhirTaskAccidentType.None,
            emergencyFee = false,
            additionalFee = "1",
            substitutionAllowed = true,
            dosageInstruction = "1-1-1",
            note = null,
            quantity = 1,
            multiplePrescriptionInfo = FhirMultiplePrescriptionInfoErpModel(
                indicator = false,
                numbering = null,
                start = null,
                end = null
            ),
            bvg = false
        ),
        medication = FhirTaskKbvMedicationErpModel(
            text = "Ultrim 100mg",
            type = "Medication_PZN",
            version = "1.1.0",
            form = "WKA",
            medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "30", unit = "St"),
                denominator = FhirQuantityErpModel(value = "1", unit = "")
            ),
            isVaccine = false,
            normSizeCode = "N1",
            compoundingInstructions = null,
            compoundingPackaging = null,
            ingredients = emptyList(),
            identifier = FhirMedicationIdentifierErpModel(
                pzn = "12547577",
                atc = null,
                ask = null,
                snomed = null
            ),
            lotNumber = null,
            expirationDate = null
        ),
        patient = FhirTaskKbvPatientErpModel(
            name = "Roman King Mann",
            birthDate = LocalDate(
                value = kotlinx.datetime.LocalDate.parse("1990-01-01"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            address = FhirTaskKbvAddressErpModel(
                streetName = "Berlinerstr.",
                houseNumber = "50",
                postalCode = "10111",
                city = "Berlin"
            ),
            insuranceInformation = "G802400712"
        ),
        practitioner = FhirTaskKbvPractitionerErpModel(
            name = "Dr. med. Sabine Ritz",
            qualification = "FA f. Frauenheilkunde und Geburtshilfe",
            practitionerIdentifier = "100"
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "DKS Medizinisches Versorgungszentrum gGmbH",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Bergstraße",
                houseNumber = "1",
                postalCode = "70176",
                city = "Berlin"
            ),
            bsnr = "613249100",
            iknr = null,
            phone = "0711/280402-0",
            mail = null
        ),
        coverage = FhirCoverageErpModel(
            name = "Techniker Krankenkasse",
            statusCode = "1",
            coverageType = "GKV",
            identifierNumber = "108077511"
        ),
        deviceRequest = null
    )

    val fhirTaskDataErpModelV1_1_DeviceRequest = FhirTaskDataErpModel(
        pvsId = "Y/450/2501/36/523",
        medicationRequest = null,
        medication = null,
        patient = FhirTaskKbvPatientErpModel(
            name = "Ludger Königsstein",
            birthDate = LocalDate(
                value = kotlinx.datetime.LocalDate.parse("1935-06-22"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            address = FhirTaskKbvAddressErpModel(
                streetName = "Musterstr.",
                houseNumber = "1",
                postalCode = "10623",
                city = "Berlin"
            ),
            insuranceInformation = "X110519788"
        ),
        practitioner = FhirTaskKbvPractitionerErpModel(
            name = "Dr. med. Hans Topp-Glücklich",
            qualification = "Hausarzt",
            practitionerIdentifier = "838382202"
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Hausarztpraxis Dr. Topp-Glücklich",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Musterstr.",
                houseNumber = "2",
                postalCode = "10623",
                city = "Berlin"
            ),
            bsnr = "031234567",
            iknr = null,
            phone = "0301234567",
            mail = null
        ),
        coverage = FhirCoverageErpModel(
            name = "AOK Rheinland/Hamburg",
            statusCode = "1",
            identifierNumber = "104212059",
            coverageType = "GKV"
        ),
        deviceRequest = FhirTaskKbvDeviceRequestErpModel(
            id = "a1533e28-4631-4afa-b5e6-f233fad87f53",
            intent = RequestIntent.Order,
            status = "active",
            pzn = "19205615",
            appName = "Vantis KHK und Herzinfarkt 001",
            accident = null,
            isSelfUse = false,
            authoredOn = LocalDate(
                value = kotlinx.datetime.LocalDate.parse("2025-03-31"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            )
        )
    )
}
