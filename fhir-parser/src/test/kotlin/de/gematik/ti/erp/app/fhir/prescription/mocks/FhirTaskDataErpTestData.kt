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

import de.gematik.ti.erp.app.fhir.FhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirCoverageErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirMultiplePrescriptionInfoErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPractitionerErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskMedicationCategoryErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.RequestIntent
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.fhir.support.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.support.FhirTaskKbvAddressErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporalSerializationType
import kotlinx.datetime.LocalDate

object FhirTaskDataErpTestData {
    val fhirKbvBundle1_v102 = FhirTaskDataErpModel(
        pvsId = "Y/400/2107/36/999",
        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = FhirTemporal.LocalDate(
                value = LocalDate.parse("2021-04-03"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            dateOfAccident = null,
            location = null,
            accidentType = FhirTaskAccidentType.None,
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
        ),
        medication = FhirTaskKbvMedicationErpModel(
            text = "Januvia® 50 mg 28 Filmtabletten N1",
            type = "Medication_PZN",
            version = "1.0.2",
            form = "FTA",
            medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
            amount = FhirRatioErpModel(
                numerator = FhirQuantityErpModel(value = "30", unit = "Stück"),
                denominator = FhirQuantityErpModel(value = "1", unit = null)
            ),
            isVaccine = false,
            normSizeCode = "N1",
            compoundingInstructions = null,
            compoundingPackaging = null,
            ingredients = emptyList(),
            identifier = FhirMedicationIdentifierErpModel(
                pzn = "00814665",
                atc = null,
                ask = null,
                snomed = null
            ),
            lotNumber = null,
            expirationDate = null
        ),
        patient = FhirTaskKbvPatientErpModel(
            name = "Ludger Königsstein",
            birthDate = FhirTemporal.LocalDate(
                value = LocalDate.parse("1935-06-22"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            address = FhirTaskKbvAddressErpModel(
                streetName = "Blumenweg",
                houseNumber = null,
                postalCode = "26427",
                city = "Esens"
            ),
            insuranceInformation = "K220635158"
        ),
        practitioner = FhirTaskKbvPractitionerErpModel(
            name = "Dr. med. Paul Freiherr von Müller",
            qualification = "Facharzt für Innere Medizin: Kardiologie",
            practitionerIdentifier = "123456628"
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
            insuranceIdentifier = "109719018"
        ),
        deviceRequest = null
    )

    val fhirKbvBundle1_v102_missingFields = listOf<String>()

    val fhirKbvBundle1_incomplete_v102 = FhirTaskDataErpModel(
        pvsId = "Y/400/2107/36/999",
        medicationRequest = null,
        medication = null,
        patient = null,
        practitioner = null,
        organization = null,
        coverage = null,
        deviceRequest = null
    )

    val fhirKbvBundle1_incomplete_v102_missingFields = listOf(
        "medication",
        "patient",
        "practitioner",
        "organization",
        "coverage",
        "request"
    )

    val fhirKbvBundle1_v110 = FhirTaskDataErpModel(
        pvsId = "Y/400/2107/36/999",
        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = FhirTemporal.LocalDate(
                value = LocalDate.parse("2025-01-16"),
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
            birthDate = FhirTemporal.LocalDate(
                value = LocalDate.parse("1935-06-22"),
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
            insuranceIdentifier = "109719018"
        ),
        deviceRequest = null
    )

    val fhirKbvBundle1_v110_missingFields = listOf<String>()

    val fhirKbvBundle1_incomplete_v110 = FhirTaskDataErpModel(
        pvsId = "Y/400/2107/36/999",
        medicationRequest = null,
        medication = null,
        patient = null,
        practitioner = null,
        organization = null,
        coverage = null,
        deviceRequest = null
    )

    val fhirKbvBundle1_incomplete_v110_missingFields = listOf(
        "medication",
        "patient",
        "practitioner",
        "organization",
        "coverage",
        "request"
    )

    val fhirKbvBundle2_v110 = FhirTaskDataErpModel(
        pvsId = "Y/400/2107/36/999",
        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = FhirTemporal.LocalDate(
                value = LocalDate.parse("2025-01-17"),
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
            birthDate = FhirTemporal.LocalDate(
                value = LocalDate.parse("1935-06-22"),
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
            insuranceIdentifier = "168140346"
        ),
        deviceRequest = null
    )

    val fhirKbvBundle2_v110_missingFields = listOf<String>()

    val fhirKbvBundle3_v110 = FhirTaskDataErpModel(
        pvsId = "Y/400/2410/36/280",
        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = FhirTemporal.LocalDate(
                value = LocalDate.parse("2025-02-17"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
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
            birthDate = FhirTemporal.LocalDate(
                value = LocalDate.parse("1990-01-01"),
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
            insuranceIdentifier = "108077511"
        ),
        deviceRequest = null
    )

    val fhirKbvBundle3_v110_missingFields = listOf<String>()

    val fhirKbvBundle4_v110 = FhirTaskDataErpModel(
        pvsId = "Y/400/2107/36/999",
        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = FhirTemporal.LocalDate(
                value = LocalDate.parse("2022-08-15"),
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
            text = "Beloc-Zok® mite 47,5 mg, 30 Retardtabletten N1",
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
            birthDate = FhirTemporal.LocalDate(
                value = LocalDate.parse("1935-06-22"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            address = FhirTaskKbvAddressErpModel(
                streetName = "Blumenweg",
                houseNumber = null,
                postalCode = "26427",
                city = "Esens"
            ),
            insuranceInformation = "P123464113"
        ),
        practitioner = FhirTaskKbvPractitionerErpModel(
            name = "Dr. med. Emma Schneider",
            qualification = "Fachärztin für Innere Medizin",
            practitionerIdentifier = "987654423"
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
            name = "Allianz Private Krankenversicherung",
            statusCode = "5",
            coverageType = "PKV",
            insuranceIdentifier = "123456789"
        ),
        deviceRequest = null
    )

    val fhirKbvBundle4_v110_missingFields = listOf<String>()

    @Suppress("ktlint:max-line-length", "MaxLineLength")
    val fhirKbvBundle5_v110 = FhirTaskDataErpModel(
        pvsId = "Y/400/2107/36/999",
        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = FhirTemporal.LocalDate(
                value = LocalDate.parse("2022-08-15"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            dateOfAccident = null,
            location = null,
            accidentType = FhirTaskAccidentType.OccupationalDisease,
            emergencyFee = false,
            additionalFee = "1",
            substitutionAllowed = false,
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
        ),
        medication = FhirTaskKbvMedicationErpModel(
            text = null,
            type = "Medication_Ingredient",
            version = "1.1.0",
            form = "Tabletten",
            medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
            amount = null,
            isVaccine = false,
            normSizeCode = "N2",
            compoundingInstructions = null,
            compoundingPackaging = null,
            ingredients = listOf(
                FhirMedicationIngredientErpModel(
                    text = "Gabapentin",
                    amount = null,
                    form = null,
                    strengthRatio = FhirRatioErpModel(
                        numerator = FhirQuantityErpModel(value = "300", unit = "mg"),
                        denominator = FhirQuantityErpModel(value = "1", unit = null)
                    ),
                    identifier = FhirMedicationIdentifierErpModel(
                        pzn = null,
                        atc = null,
                        ask = "22308",
                        snomed = null
                    )
                ),
                FhirMedicationIngredientErpModel(
                    text = "Gabapentin",
                    amount = null,
                    form = null,
                    strengthRatio = FhirRatioErpModel(
                        numerator = FhirQuantityErpModel(value = "300", unit = "mg"),
                        denominator = FhirQuantityErpModel(value = "1", unit = null)
                    ),
                    identifier = FhirMedicationIdentifierErpModel(
                        pzn = null,
                        atc = null,
                        ask = "22308",
                        snomed = null
                    )
                )
            ),
            identifier = FhirMedicationIdentifierErpModel(
                pzn = null,
                atc = null,
                ask = "22308",
                snomed = null
            ),
            lotNumber = null,
            expirationDate = null
        ),
        patient = FhirTaskKbvPatientErpModel(
            name = "Prof. habil. Dr. med Friedrich-Wilhelm-Karl-Gustav-Justus-Gotfried " +
                "Grossherzog von und zu der Schaumberg-von-und-zu-Schaumburg-und-Radeberg",
            birthDate = FhirTemporal.LocalDate(
                value = LocalDate.parse("1951-07-12"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            address = FhirTaskKbvAddressErpModel(
                streetName = null,
                houseNumber = null,
                postalCode = "12489",
                city = "Berlin"
            ),
            insuranceInformation = "H030170228"
        ),
        practitioner = FhirTaskKbvPractitionerErpModel(
            name = "Ben Schulz",
            qualification = "Facharzt für Allgemeinmedizin",
            practitionerIdentifier = "754236701"
        ),
        organization = FhirTaskOrganizationErpModel(
            name = "Hausarztpraxis",
            address = FhirTaskKbvAddressErpModel(
                streetName = "Herbert-Lewin-Platz",
                houseNumber = "2",
                postalCode = "10623",
                city = "Berlin"
            ),
            bsnr = "724444400",
            iknr = null,
            phone = "030321654987",
            mail = "hausarztpraxis@e-mail.de"
        ),
        coverage = FhirCoverageErpModel(
            name = "Verwaltungs-BG",
            statusCode = "1",
            insuranceIdentifier = "108035612",
            coverageType = "BG"
        ),
        deviceRequest = null
    )

    val fhirKbvBundle5_v110_missingFields = listOf<String>()

    val fhirKbvBundle6_v110 = FhirTaskDataErpModel(
        pvsId = "Y/400/2107/36/999",
        medicationRequest = FhirTaskKbvMedicationRequestErpModel(
            authoredOn = FhirTemporal.LocalDate(
                value = LocalDate.parse("2022-05-20"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
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
                    numerator = FhirQuantityErpModel(value = "4", unit = null),
                    denominator = FhirQuantityErpModel(value = "4", unit = null)
                ),
                start = FhirTemporal.LocalDate(
                    value = LocalDate.parse("2022-04-01"),
                    type = FhirTemporalSerializationType.FhirTemporalLocalDate
                ),
                end = null
            ),
            bvg = false
        ),
        medication = FhirTaskKbvMedicationErpModel(
            text = "L-Thyroxin Henning 75 100 Tbl. N3",
            type = "Medication_PZN",
            version = "1.1.0",
            form = "TAB",
            medicationCategory = FhirTaskMedicationCategoryErpModel.ARZNEI_UND_VERBAND_MITTEL,
            amount = null,
            isVaccine = false,
            normSizeCode = "N3",
            compoundingInstructions = null,
            compoundingPackaging = null,
            ingredients = listOf(),
            identifier = FhirMedicationIdentifierErpModel(
                pzn = "02532741",
                atc = null,
                ask = null,
                snomed = null
            ),
            lotNumber = null,
            expirationDate = null
        ),
        patient = FhirTaskKbvPatientErpModel(
            name = "Prof. Dr. Dr. med Eva Kluge",
            birthDate = FhirTemporal.LocalDate(
                value = LocalDate.parse("1982-01-03"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            address = FhirTaskKbvAddressErpModel(
                streetName = "Pflasterhofweg",
                houseNumber = "111B",
                postalCode = "50999",
                city = "Köln"
            ),
            insuranceInformation = "K030182229"
        ),
        practitioner = FhirTaskKbvPractitionerErpModel(
            name = "Dr. med. Emma Schneider",
            qualification = "Fachärztin für Innere Medizin",
            practitionerIdentifier = "987654423"
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
            name = "Techniker Krankenkasse",
            statusCode = "3",
            insuranceIdentifier = "109777509",
            coverageType = "GKV"
        ),
        deviceRequest = null
    )

    val fhirKbvBundle6_v110_missingFields = listOf<String>()

    val fhirTaskDataErpModelV1_1_DeviceRequest = FhirTaskDataErpModel(
        pvsId = "Y/450/2501/36/523",
        medicationRequest = null,
        medication = null,
        patient = FhirTaskKbvPatientErpModel(
            name = "Ludger Königsstein",
            birthDate = FhirTemporal.LocalDate(
                value = LocalDate.parse("1935-06-22"),
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
            insuranceIdentifier = "104212059",
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
            authoredOn = FhirTemporal.LocalDate(
                value = LocalDate.parse("2025-03-31"),
                type = FhirTemporalSerializationType.FhirTemporalLocalDate
            ),
            isNew = true,
            isArchived = false
        )
    )
}
