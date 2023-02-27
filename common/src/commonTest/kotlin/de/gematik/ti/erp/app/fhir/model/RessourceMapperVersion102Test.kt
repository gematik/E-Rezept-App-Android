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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.FhirTemporal
import de.gematik.ti.erp.app.fhir.parser.Year
import de.gematik.ti.erp.app.fhir.parser.asFhirTemporal
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

enum class ReturnType {
    Organization, Patient, Practitioner, InsuranceInformation, MedicationRequest, MedicationDispense,
    Medication, Ingredient, MultiplePrescriptionInfo, Quantity, Ratio, Address
}

class RessourceMapperVersion102Test {

    @Test
    fun `process patient version 1_0_2`() {
        val patient = Json.parseToJsonElement(patientJson_vers_1_0_2)
        val result = extractPatient(
            patient,
            processAddress = { line, postalCode, city ->
                assertEquals(listOf("Siegburger Str. 155"), line)
                assertEquals("51105", postalCode)
                assertEquals("Köln", city)

                ReturnType.Address
            },
            processPatient = { name, address, birthDate, insuranceIdentifier ->
                assertEquals("Prinzessin Lars Graf Freiherr von Schinder", name)
                assertEquals(ReturnType.Address, address)
                assertEquals(FhirTemporal.LocalDate(LocalDate.parse("1964-04-04")), birthDate)
                assertEquals("X110535541", insuranceIdentifier)

                ReturnType.Patient
            }
        )

        assertEquals(ReturnType.Patient, result)
    }

    @Test
    fun `process patient version 1_0_2 with incomplete date of birth`() {
        val patient = Json.parseToJsonElement(patientJson_vers_1_0_2_with_incomplete_birthDate)
        val result = extractPatient(
            patient,
            processAddress = { line, postalCode, city ->
                assertEquals(listOf("Siegburger Str. 155"), line)
                assertEquals("51105", postalCode)
                assertEquals("Köln", city)

                ReturnType.Address
            },
            processPatient = { name, address, birthDate, insuranceIdentifier ->
                assertEquals("Prinzessin Lars Graf Freiherr von Schinder", name)
                assertEquals(ReturnType.Address, address)
                assertEquals(Year.parse("1964").asFhirTemporal(), birthDate)
                assertEquals("X110535541", insuranceIdentifier)

                ReturnType.Patient
            }
        )

        assertEquals(ReturnType.Patient, result)
    }

    @Test
    fun `process medicationPzn version 1_0_2`() {
        val medicationPznJson = Json.parseToJsonElement(medicationPznJson_vers_1_0_2)
        val result = extractPZNMedication(
            medicationPznJson,
            quantityFn = { _, _ ->
                ReturnType.Quantity
            },
            ratioFn = { numerator, denominator ->
                assertEquals(ReturnType.Quantity, numerator)
                assertEquals(ReturnType.Quantity, denominator)
                ReturnType.Ratio
            },
            processMedication = {
                    text, medicationProfile, medicationCategory, form, amount, vaccine,
                    manufacturingInstructions, packaging, normSizeCode, uniqueIdentifier,
                    ingredients, lotnumber, expirationDate ->
                assertEquals("Ich bin in Einlösung", text)
                assertEquals(MedicationProfile.PZN, medicationProfile)
                assertEquals(MedicationCategory.ARZNEI_UND_VERBAND_MITTEL, medicationCategory)
                assertEquals("IHP", form)
                assertEquals(ReturnType.Ratio, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals("N1", normSizeCode)
                assertEquals("00427833", uniqueIdentifier)
                assertEquals(listOf(), ingredients)
                assertEquals(null, lotnumber)
                assertEquals(null, expirationDate)
                ReturnType.Medication
            }
        )
        assertEquals(ReturnType.Medication, result)
    }

    @Test
    fun `process medication ingredient version 1_0_2`() {
        val medicationIngredientJson = Json.parseToJsonElement(medicationIngredientJson_vers_1_0_2)
        val result = extractMedicationIngredient(
            medicationIngredientJson,
            quantityFn = { _, _ ->
                ReturnType.Quantity
            },
            ratioFn = { numerator, denominator ->
                assertEquals(ReturnType.Quantity, numerator)
                assertEquals(ReturnType.Quantity, denominator)
                ReturnType.Ratio
            },
            ingredientFn = { text, form, number, amount, strength ->
                assertEquals("Wirkstoff Paulaner Weissbier", text)
                assertEquals(null, form)
                assertEquals(null, amount)
                assertEquals("37197", number)
                assertEquals(ReturnType.Ratio, strength)
                ReturnType.Ingredient
            },
            processMedication = {
                    text, medicationProfile, medicationCategory, form, amount, vaccine,
                    manufacturingInstructions, packaging, normSizeCode, uniqueIdentifier,
                    ingredients, lotNumber, expirationDate ->
                assertEquals(null, text)
                assertEquals(MedicationProfile.INGREDIENT, medicationProfile)
                assertEquals(MedicationCategory.ARZNEI_UND_VERBAND_MITTEL, medicationCategory)
                assertEquals("Flüssigkeiten", form)
                assertEquals(null, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals("N1", normSizeCode)
                assertEquals(null, uniqueIdentifier)
                assertEquals(listOf(ReturnType.Ingredient), ingredients)

                assertEquals(null, lotNumber)
                assertEquals(null, expirationDate)
                ReturnType.Medication
            }
        )
        assertEquals(ReturnType.Medication, result)
    }

    @Test
    fun `process medication compounding version 1_0_2`() {
        val medicationCompoundingJson = Json.parseToJsonElement(medicationCompoundingJson_vers_1_0_2)
        val result = extractMedicationCompounding(
            medicationCompoundingJson,
            quantityFn = { _, _ ->
                ReturnType.Quantity
            },
            ratioFn = { _, _ ->
                ReturnType.Ratio
            },
            ingredientFn = { _, _, _, _, strength ->
                assertEquals(ReturnType.Ratio, strength)
                ReturnType.Ingredient
            },
            processMedication = {
                    text, medicationProfile, medicationCategory, form, amount, vaccine,
                    manufacturingInstructions, packaging, normSizeCode, uniqueIdentifier,
                    ingredients, lotNumber, expirationDate ->
                assertEquals(null, text)
                assertEquals(MedicationProfile.COMPOUNDING, medicationProfile)
                assertEquals(MedicationCategory.ARZNEI_UND_VERBAND_MITTEL, medicationCategory)
                assertEquals("Lösung", form)
                assertEquals(ReturnType.Ratio, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals(null, normSizeCode)
                assertEquals(null, uniqueIdentifier)
                assertEquals(listOf(ReturnType.Ingredient, ReturnType.Ingredient), ingredients)

                assertEquals(null, lotNumber)
                assertEquals(null, expirationDate)
                ReturnType.Medication
            }
        )
        assertEquals(ReturnType.Medication, result)
    }

    @Test
    fun `process medication freetext version 1_0_2`() {
        val medicationFreetextJson = Json.parseToJsonElement(medicationFreetextJson_vers_1_0_2)
        val result = extractMedicationFreetext(
            medicationFreetextJson,
            quantityFn = { _, _ ->
                ReturnType.Quantity
            },
            ratioFn = { _, _ ->
                ReturnType.Ratio
            },
            processMedication = {
                    text, medicationProfile, medicationCategory, form, amount, vaccine,
                    manufacturingInstructions, packaging, normSizeCode, uniqueIdentifier,
                    ingredients, lotNumber, expirationDate ->
                assertEquals("Freitext", text)
                assertEquals(MedicationProfile.FREETEXT, medicationProfile)
                assertEquals(MedicationCategory.ARZNEI_UND_VERBAND_MITTEL, medicationCategory)
                assertEquals(null, form)
                assertEquals(null, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals(null, normSizeCode)
                assertEquals(null, uniqueIdentifier)
                assertEquals(listOf(), ingredients)

                assertEquals(null, lotNumber)
                assertEquals(null, expirationDate)
                ReturnType.Medication
            }
        )
        assertEquals(ReturnType.Medication, result)
    }

    @Test
    fun `process medicationRequest version 1_0_2`() {
        val medicationRequestJson = Json.parseToJsonElement(medicationRequestJson_vers_1_0_2)
        val result = extractMedicationRequest(
            medicationRequestJson,
            quantityFn = { _, _ ->
                ReturnType.Quantity
            },
            ratioFn = { numerator, denominator ->
                assertEquals(ReturnType.Quantity, numerator)
                assertEquals(ReturnType.Quantity, denominator)
                ReturnType.Ratio
            },
            processMultiplePrescriptionInfo = { indicator, numbering, start ->
                assertTrue(indicator)
                assertEquals(ReturnType.Ratio, numbering)
                assertEquals(FhirTemporal.LocalDate(LocalDate.parse("2022-08-17")), start)
                ReturnType.MultiplePrescriptionInfo
            },
            processMedicationRequest = { dateOfAccident,
                location,
                accidentType,
                emergencyFee,
                substitutionAllowed,
                dosageInstruction,
                quantity,
                multiplePrescriptionInfo,
                note,
                bvg,
                additionalFee ->
                assertEquals(FhirTemporal.LocalDate(LocalDate.parse("2022-06-29")), dateOfAccident)
                assertEquals("Dummy-Betrieb", location)
                assertEquals(AccidentType.Arbeitsunfall, accidentType)
                assertEquals(false, emergencyFee)
                assertEquals(true, substitutionAllowed)
                assertEquals("1-2-1-2-0", dosageInstruction)
                assertEquals(12, quantity)
                assertEquals(ReturnType.MultiplePrescriptionInfo, multiplePrescriptionInfo)
                assertEquals("Bitte laengliche Tabletten.", note)
                assertEquals(true, bvg)
                assertEquals("2", additionalFee)
                ReturnType.MedicationRequest
            }
        )
        assertEquals(ReturnType.MedicationRequest, result)
    }
}
