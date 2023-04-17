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

import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.FhirTemporal
import de.gematik.ti.erp.app.fhir.parser.asFhirTemporal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class MedicationDispenseMapperTest {

    @Test
    fun `extract medication dispense`() {
        val medicationDispenseJson = Json.parseToJsonElement(medicationDispenseJson)
        val result = extractMedicationDispense(
            medicationDispenseJson,
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
            processMedication = { text, medicationProfile, medicationCategory, form, amount, vaccine,
                manufacturingInstructions, packaging, normSizeCode, uniqueIdentifier,
                ingredients, lotNumber, expirationDate ->
                assertEquals("Defamipin", text)
                assertEquals(MedicationProfile.PZN, medicationProfile)
                assertEquals(MedicationCategory.BTM, medicationCategory)
                assertEquals("FET", form)
                assertEquals(ReturnType.Ratio, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals("Sonstiges", normSizeCode)
                assertEquals("06491772", uniqueIdentifier)
                assertEquals(listOf(), ingredients)
                assertEquals("8521037577", lotNumber)
                assertEquals(FhirTemporal.Instant(Instant.parse("2023-05-02T06:26:06Z")), expirationDate)
                ReturnType.Medication
            },
            processMedicationDispense = { dispenseId, patientIdentifier, medication, wasSubstituted,
                dosageInstruction, performer, whenHandedOver ->
                assertEquals("160.000.000.031.686.59", dispenseId)
                assertEquals("X110535541", patientIdentifier)
                assertEquals(ReturnType.Medication, medication)
                assertEquals(false, wasSubstituted)
                assertEquals(null, dosageInstruction)
                assertEquals("3-SMC-B-Testkarte-883110000116873", performer)
                assertEquals(FhirTemporal.LocalDate(LocalDate.parse("2022-07-12")), whenHandedOver)
                ReturnType.MedicationDispense
            }
        )
        assertEquals(ReturnType.MedicationDispense, result)
    }

    @Test
    fun `extract medication dispense with unknown medication profile`() {
        val medicationDispenseJson = Json.parseToJsonElement(medicationDispenseWithUnknownMedicationProfileJson)
        val result = extractMedicationDispense(
            medicationDispenseJson,
            quantityFn = { _, _ ->
                ReturnType.Quantity
            },
            ratioFn = { numerator, denominator ->
                assertEquals(ReturnType.Quantity, numerator)
                assertEquals(ReturnType.Quantity, denominator)
                ReturnType.Ratio
            },
            ingredientFn = { _, _, _, _, _ ->
                ReturnType.Ingredient
            },
            processMedication = { text, medicationProfile, medicationCategory, form, amount, vaccine,
                manufacturingInstructions, packaging, normSizeCode, uniqueIdentifier,
                ingredients, lotNumber, expirationDate ->
                assertEquals("", text)
                assertEquals(MedicationProfile.UNKNOWN, medicationProfile)
                assertEquals(MedicationCategory.UNKNOWN, medicationCategory)
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
            },
            processMedicationDispense = { dispenseId, patientIdentifier, medication, wasSubstituted,
                dosageInstruction, performer, whenHandedOver ->
                assertEquals("160.000.000.031.686.59", dispenseId)
                assertEquals("X110535541", patientIdentifier)
                assertEquals(ReturnType.Medication, medication)
                assertEquals(false, wasSubstituted)
                assertEquals(null, dosageInstruction)
                assertEquals("3-SMC-B-Testkarte-883110000116873", performer)
                assertEquals("2022-07-12", whenHandedOver.formattedString())
                ReturnType.MedicationDispense
            }
        )
        assertEquals(ReturnType.MedicationDispense, result)
    }

    @Test
    fun `extract medication dispenses version 1_2`() {
        val medicationDispensesBundle = Json.parseToJsonElement(medDispenseBundleVersion_1_2)
        medicationDispensesBundle.findAll("entry.resource").apply {
            val result = extractMedicationDispense(
                this.elementAt(0),
                quantityFn = { _, _ ->
                    ReturnType.Quantity
                },
                ratioFn = { numerator, denominator ->
                    assertEquals(ReturnType.Quantity, numerator)
                    assertEquals(ReturnType.Quantity, denominator)
                    ReturnType.Ratio
                },
                ingredientFn = { _, _, _, _, _ ->
                    ReturnType.Ingredient
                },
                processMedication = { text, medicationProfile, medicationCategory, form, amount, vaccine,
                    manufacturingInstructions, packaging, normSizeCode, uniqueIdentifier,
                    ingredients, lotNumber, expirationDate ->
                    assertEquals("Sumatriptan-1a Pharma 100 mg Tabletten", text)
                    assertEquals(MedicationProfile.PZN, medicationProfile)
                    assertEquals(MedicationCategory.ARZNEI_UND_VERBAND_MITTEL, medicationCategory)
                    assertEquals("TAB", form)
                    assertEquals(ReturnType.Ratio, amount)
                    assertEquals(false, vaccine)
                    assertEquals(null, manufacturingInstructions)
                    assertEquals(null, packaging)
                    assertEquals("N1", normSizeCode)
                    assertEquals("06313728", uniqueIdentifier)
                    assertEquals(listOf(), ingredients)
                    assertEquals(null, lotNumber)
                    assertEquals(null, expirationDate)
                    ReturnType.Medication
                },
                processMedicationDispense = { dispenseId, patientIdentifier, medication, wasSubstituted,
                    dosageInstruction, performer, whenHandedOver ->
                    assertEquals("3465270a-11e7-4bbf-ae53-378f9cc52747", dispenseId)
                    assertEquals("X234567890", patientIdentifier)
                    assertEquals(ReturnType.Medication, medication)
                    assertEquals(false, wasSubstituted)
                    assertEquals("1-0-1-0", dosageInstruction)
                    assertEquals("3-abc-1234567890", performer)
                    assertEquals(LocalDate.parse("2022-02-28").asFhirTemporal(), whenHandedOver)
                    ReturnType.MedicationDispense
                }
            )
            assertEquals(ReturnType.MedicationDispense, result)
        }
    }

    @Test
    fun `extract medication dispense with unknown medication category`() {
        val medicationDispenseJson = Json.parseToJsonElement(medicationDispenseWithoutCategoryJson)
        val result = extractMedicationDispense(
            medicationDispenseJson,
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
            processMedication = { text, medicationProfile, medicationCategory, form, amount, vaccine,
                manufacturingInstructions, packaging, normSizeCode, uniqueIdentifier,
                ingredients, lotNumber, expirationDate ->
                assertEquals("Defamipin", text)
                assertEquals(MedicationProfile.PZN, medicationProfile)
                assertEquals(MedicationCategory.UNKNOWN, medicationCategory)
                assertEquals("FET", form)
                assertEquals(ReturnType.Ratio, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals("Sonstiges", normSizeCode)
                assertEquals("06491772", uniqueIdentifier)
                assertEquals(listOf(), ingredients)
                assertEquals("8521037577", lotNumber)
                assertEquals(Instant.parse("2023-05-02T06:26:06Z").asFhirTemporal(), expirationDate)
                ReturnType.Medication
            },
            processMedicationDispense = { dispenseId, patientIdentifier, medication, wasSubstituted,
                dosageInstruction, performer, whenHandedOver ->
                assertEquals("160.000.000.031.686.59", dispenseId)
                assertEquals("X110535541", patientIdentifier)
                assertEquals(ReturnType.Medication, medication)
                assertEquals(false, wasSubstituted)
                assertEquals(null, dosageInstruction)
                assertEquals("3-SMC-B-Testkarte-883110000116873", performer)
                assertEquals(LocalDate.parse("2022-07-12").asFhirTemporal(), whenHandedOver)
                ReturnType.MedicationDispense
            }
        )
        assertEquals(ReturnType.MedicationDispense, result)
    }
}
