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

import de.gematik.ti.erp.app.fhir.parser.asFhirTemporal
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RessourceMapperVersion110Test {
// Do not remove please (needed for debugging faulty resources)
//    @Test
//    fun `process kbvBundle version 1_1_0`() {
//        val kbvBundle = Json.parseToJsonElement(testKBVBundleJson_vers_1_1_0)
//
//        extractKBVBundle(
//            kbvBundle,
//            processAddress = { _, _, _ ->
//                ReturnType.Address
//            },
//            processIngredient = { _, _, _, _, _->
//               ReturnType.Ingredient
//            },
//            processInsuranceInformation = { _, _ ->
//                   ReturnType.InsuranceInformation
//            },
//            processMedication = {_, _, _, _, _, _, _, _, _, _, _, _, _->
//                ReturnType.Medication
//            },
//            processMedicationRequest = { _, _, _, _, _, _, _, _, _, _, _, _->
//                ReturnType.MedicationRequest
//            },
//            processOrganization = { _, _, _, _, _, _->
//                ReturnType.Organization
//            },
//            processPatient = { _, _, _, _->
//                ReturnType.Patient
//            },
//            processPractitioner = {  _, _, _ ->
//                ReturnType.Practitioner
//            },
//            processQuantity = { _, _->
//                ReturnType.Quantity
//            },
//            processRatio = {_, _ ->
//                ReturnType.Ratio
//            },
//            processMultiplePrescriptionInfo = { _, _, _, _ ->
//                ReturnType.MultiplePrescriptionInfo
//            },
//            savePVSIdentifier = {},
//            save = {_, _, _, _, _,_, ->
//
//            }
//        )
//    }

    @Test
    fun `process patient version 1_1_0`() {
        val patient = Json.parseToJsonElement(patientJson_vers_1_1_0)
        val result = extractPatientVersion110(
            patient,
            processAddress = { line, postalCode, city ->
                assertEquals(listOf("Blumenweg"), line)
                assertEquals("26427", postalCode)
                assertEquals("Esens", city)

                ReturnType.Address
            },
            processPatient = { name, address, birthDate, insuranceIdentifier ->
                assertEquals("Ludger Königsstein", name)
                assertEquals(ReturnType.Address, address)
                assertEquals(LocalDate.parse("1935-06-22").asFhirTemporal(), birthDate)
                assertEquals("K220635158", insuranceIdentifier)

                ReturnType.Patient
            }
        )

        assertEquals(ReturnType.Patient, result)
    }

    @Test
    fun `process medicationPzn version 1_1_0`() {
        val medicationPznJson = Json.parseToJsonElement(medicationPznJson_vers_1_1_0)
        val result = extractPZNMedicationVersion110(
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
                assertEquals("Novaminsulfon 500 mg Lichtenstein 100 ml Tropf. N3", text)
                assertEquals(MedicationProfile.PZN, medicationProfile)
                assertEquals(MedicationCategory.ARZNEI_UND_VERBAND_MITTEL, medicationCategory)
                assertEquals("TEI", form)
                assertEquals(ReturnType.Ratio, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals(null, normSizeCode)
                assertEquals("03507952", uniqueIdentifier)
                assertEquals(listOf(), ingredients)
                assertEquals(null, lotnumber)
                assertEquals(null, expirationDate)
                ReturnType.Medication
            }
        )
        assertEquals(ReturnType.Medication, result)
    }

    @Test
    fun `process medication ingredient version 1_1_0`() {
        val medicationIngredientJson = Json.parseToJsonElement(medicationIngredientJson_vers_1_1_0)
        val result = extractMedicationIngredientVersion110(
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
                assertEquals("Ramipril", text)
                assertEquals(null, form)
                assertEquals(null, amount)
                assertEquals("22686", number)
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
                assertEquals("Tabletten", form)
                assertEquals(ReturnType.Ratio, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals("N3", normSizeCode)
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
    fun `process medication compounding version 1_1_0`() {
        val medicationCompoundingJson = Json.parseToJsonElement(medicationCompoundingJson_vers_1_1_0)
        val result = extractMedicationCompoundingVersion110(
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
                assertEquals("Kapseln", form)
                assertEquals(ReturnType.Ratio, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals(null, normSizeCode)
                assertEquals(null, uniqueIdentifier)
                assertEquals(listOf(ReturnType.Ingredient, ReturnType.Ingredient, ReturnType.Ingredient), ingredients)

                assertEquals(null, lotNumber)
                assertEquals(null, expirationDate)
                ReturnType.Medication
            }
        )
        assertEquals(ReturnType.Medication, result)
    }

    @Test
    fun `process medication freetext version 1_1_0`() {
        val medicationFreetextJson = Json.parseToJsonElement(medicationFreetextJson_vers_1_1_0)
        val result = extractMedicationFreetextVersion110(
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
                assertEquals("Metformin 850mg Tabletten N3", text)
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
    fun `process medicationRequest version 1_1_0`() {
        val medicationRequestJson = Json.parseToJsonElement(medicationRequestJson_vers_1_1_0)
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
            processMultiplePrescriptionInfo = { indicator, numbering, start, end ->
                assertTrue(indicator)
                assertEquals(ReturnType.Ratio, numbering)
                assertEquals(LocalDate.parse("2022-05-20").asFhirTemporal(), start)
                assertEquals(LocalDate.parse("2022-06-30").asFhirTemporal(), end)
                ReturnType.MultiplePrescriptionInfo
            },
            processMedicationRequest = {
                    authoredOn,
                    dateOfAccident,
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
                assertEquals(null, authoredOn)
                assertEquals(null, dateOfAccident)
                assertEquals(null, location)
                assertEquals(AccidentType.None, accidentType)
                assertEquals(false, emergencyFee)
                assertEquals(false, substitutionAllowed)
                assertEquals(null, dosageInstruction)
                assertEquals(1, quantity)
                assertEquals(ReturnType.MultiplePrescriptionInfo, multiplePrescriptionInfo)
                assertEquals(null, note)
                assertEquals(false, bvg)
                assertEquals(null, additionalFee)
                ReturnType.MedicationRequest
            }
        )
        assertEquals(ReturnType.MedicationRequest, result)
    }
}
