/*
 * Copyright (c) 2022 gematik GmbH
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

import kotlinx.serialization.json.Json
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
enum class ReturnType {
    Organization, Patient, Practitioner, InsuranceInformation, MedicationRequest, MedicationDispense,
    Medication, Ingredient, MultiplePrescriptionInfo, Quantity, Ratio, Address
}

class KBVMapperTest {

    @Test
    fun `process organization`() {
        val organization = Json.parseToJsonElement(organizationJson)

        val result = extractOrganization(
            organization,
            processAddress = { line, postalCode, city ->
                assertEquals(listOf("Herbert-Lewin-Platz 2"), line)
                assertEquals("10623", postalCode)
                assertEquals("Berlin", city)

                ReturnType.Address
            },
            processOrganization = { name, address, uniqueIdentifier, phone, mail ->
                assertEquals("MVZ", name)
                assertEquals(ReturnType.Address, address)
                assertEquals("721111100", uniqueIdentifier)
                assertEquals("0301234567", phone)
                assertEquals("mvz@e-mail.de", mail)

                ReturnType.Organization
            }
        )

        assertEquals(ReturnType.Organization, result)
    }

    @Test
    fun `process patient`() {
        val patient = Json.parseToJsonElement(patientJson)
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
                assertEquals(LocalDate.parse("1964-04-04"), birthDate)
                assertEquals("X110535541", insuranceIdentifier)

                ReturnType.Patient
            }
        )

        assertEquals(ReturnType.Patient, result)
    }

    @Test
    fun `process practitioner`() {
        val practitioner = Json.parseToJsonElement(practitionerJson)
        val result = extractPractitioner(
            practitioner,
            processPractitioner = { name, qualification, practitionerIdentifier ->
                assertEquals("Dr. med. Emma Schneider", name)
                assertEquals("Fachärztin für Innere Medizin", qualification)
                assertEquals("987654423", practitionerIdentifier)

                ReturnType.Practitioner
            }
        )
        assertEquals(ReturnType.Practitioner, result)
    }

    @Test
    fun `process insuranceInformation`() {
        val insuranceInformation = Json.parseToJsonElement(insuranceInformationJson)
        val result = extractInsuranceInformation(
            insuranceInformation,
            processInsuranceInformation = { name: String?, statusCode: String? ->
                assertEquals("HEK", name)
                assertEquals("3", statusCode)

                ReturnType.InsuranceInformation
            }
        )
        assertEquals(ReturnType.InsuranceInformation, result)
    }

    @Test
    fun `process quantity`() {
        val quantityJson = Json.parseToJsonElement(quantityJson)
        val result = quantityJson.extractQuantity { value, unit ->
            assertEquals("12", value)
            assertEquals("TAB", unit)
            ReturnType.Quantity
        }
        assertEquals(ReturnType.Quantity, result)
    }

    @Test
    fun `process ratio`() {
        val ratioJson = Json.parseToJsonElement(ratioJson)
        val result = ratioJson.extractRatio(
            quantityFn = { _, _ ->
                ReturnType.Quantity
            },
            ratioFn = { numerator, denominator ->
                assertEquals(ReturnType.Quantity, numerator)
                assertEquals(ReturnType.Quantity, denominator)

                ReturnType.Ratio
            }
        )
        assertEquals(ReturnType.Ratio, result)
    }

    @Test
    fun `process ingredient`() {
        val ingredientJson = Json.parseToJsonElement(ingredientJson)
        val result = ingredientJson.extractIngredient(
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
                assertEquals("37197", number)
                assertEquals(null, amount)
                assertEquals(ReturnType.Ratio, strength)
                ReturnType.Ingredient
            }
        )
        assertEquals(ReturnType.Ingredient, result)
    }

    @Test
    fun `process multi prescription info`() {
        val multiPrescriptionInfoJson = Json.parseToJsonElement(multiPrescriptionInfoJson)
        val result = multiPrescriptionInfoJson.extractMultiplePrescriptionInfo(
            quantityFn = { _, _ ->
                ReturnType.Quantity
            },
            ratioFn = { numerator, denominator ->
                assertEquals(ReturnType.Quantity, numerator)
                assertEquals(ReturnType.Quantity, denominator)
                ReturnType.Ratio
            },
            processMultiplePrescriptionInfo = {
                    indicator, numbering, start ->
                assertEquals(true, indicator)
                assertEquals(ReturnType.Ratio, numbering)
                assertEquals(LocalDate.parse("2022-08-17"), start)
                ReturnType.MultiplePrescriptionInfo
            }
        )
        assertEquals(ReturnType.MultiplePrescriptionInfo, result)
    }

    @Test
    fun `process medicationPzn`() {
        val medicationPznJson = Json.parseToJsonElement(medicationPznJson)
        val result = extractMedication(
            medicationPznJson,
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
                assertEquals("", number)
                assertEquals(null, amount)
                assertEquals(ReturnType.Ratio, strength)
                ReturnType.Ingredient
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
    fun `process medication ingredient`() {
        val medicationIngredientJson = Json.parseToJsonElement(medicationIngredientJson)
        val result = extractMedication(
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
    fun `process medication compounding`() {
        val medicationCompoundingJson = Json.parseToJsonElement(medicationCompoundingJson)
        val result = extractMedication(
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
    fun `process medication freetext`() {
        val medicationFreetextJson = Json.parseToJsonElement(medicationFreetextJson)
        val result = extractMedication(
            medicationFreetextJson,
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
    fun `process medicationRequest`() {
        val medicationRequestJson = Json.parseToJsonElement(medicationRequestJson)
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
                assertEquals(LocalDate.parse("2022-08-17"), start)
                ReturnType.MultiplePrescriptionInfo
            },
            processMedicationRequest = { dateOfAccident, location, emergencyFee, substitutionAllowed, dosageInstruction,
                multiplePrescriptionInfo, note, bvg, additionalFee ->
                assertEquals(LocalDate.parse("2022-06-29"), dateOfAccident)
                assertEquals("Dummy-Betrieb", location)
                assertEquals(false, emergencyFee)
                assertEquals(true, substitutionAllowed)
                assertEquals("1-2-1-2-0", dosageInstruction)
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
