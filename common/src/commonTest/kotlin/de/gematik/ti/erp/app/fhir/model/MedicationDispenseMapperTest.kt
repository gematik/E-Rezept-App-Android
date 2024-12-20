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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.asFhirTemporal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertTrue

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
            ingredientFn = { text, form, identifier, amount, strength ->
                assertEquals("Wirkstoff Paulaner Weissbier", text)
                assertEquals(null, form)
                assertEquals(null, amount)
                assertEquals(Identifier(), identifier)
                assertEquals(ReturnType.Ratio, strength)
                ReturnType.Ingredient
            },
            processMedication = { text, medicationCategory, form, amount, vaccine,
                manufacturingInstructions, packaging, normSizeCode, identifier, ingredientMedications,
                ingredients, lotNumber, expirationDate ->
                assertEquals("Defamipin", text)
                assertEquals(MedicationCategory.BTM, medicationCategory)
                assertEquals("FET", form)
                assertEquals(ReturnType.Ratio, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals("Sonstiges", normSizeCode)
                assertEquals("06491772", identifier.pzn)
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
            processMedication = { text, medicationCategory, form, amount, vaccine,
                manufacturingInstructions, packaging, normSizeCode, identifier, ingredientMedications,
                ingredients, lotNumber, expirationDate ->
                assertEquals("", text)
                assertEquals(MedicationCategory.UNKNOWN, medicationCategory)
                assertEquals(null, form)
                assertEquals(null, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals(null, normSizeCode)
                assertEquals(Identifier(), identifier)
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
                processMedication = { text, medicationCategory, form, amount, vaccine,
                    manufacturingInstructions, packaging, normSizeCode, identifier, ingredientMedications,
                    ingredients, lotNumber, expirationDate ->
                    assertEquals("Sumatriptan-1a Pharma 100 mg Tabletten", text)
                    assertEquals(MedicationCategory.ARZNEI_UND_VERBAND_MITTEL, medicationCategory)
                    assertEquals("TAB", form)
                    assertEquals(ReturnType.Ratio, amount)
                    assertEquals(false, vaccine)
                    assertEquals(null, manufacturingInstructions)
                    assertEquals(null, packaging)
                    assertEquals("N1", normSizeCode)
                    assertEquals("06313728", identifier.pzn)
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
            ingredientFn = { text, form, identifier, amount, strength ->
                assertEquals("Wirkstoff Paulaner Weissbier", text)
                assertEquals(null, form)
                assertEquals(Identifier(), identifier)
                assertEquals(null, amount)
                assertEquals(ReturnType.Ratio, strength)
                ReturnType.Ingredient
            },
            processMedication = { text, medicationCategory, form, amount, vaccine,
                manufacturingInstructions, packaging, normSizeCode, identifier, ingredientMedications,
                ingredients, lotNumber, expirationDate ->
                assertEquals("Defamipin", text)
                assertEquals(MedicationCategory.UNKNOWN, medicationCategory)
                assertEquals("FET", form)
                assertEquals(ReturnType.Ratio, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals("Sonstiges", normSizeCode)
                assertEquals("06491772", identifier.pzn)
                assertEquals(listOf(), ingredients)
                assertEquals("8521037577", lotNumber)
                assertEquals(Instant.parse("2023-05-02T06:26:06Z"), expirationDate?.toInstant())
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

    @Test
    fun `extract medication dispense simple medication version 1_4`() {
        val medicationDispensesBundle = Json.parseToJsonElement(dispenseSimpleMedication_1_4)
        val dispensesWithMedication = extractMedicationDispensePairs(medicationDispensesBundle)

        assertEquals(1, dispensesWithMedication.size)

        dispensesWithMedication.forEach { (dispense, dispenseMedication) ->
            val result = extractMedicationDispenseWithMedication(
                dispense,
                dispenseMedication,
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
                processMedication = { text, medicationCategory, form, amount, vaccine,
                    manufacturingInstructions, packaging, normSizeCode, identifier, ingredientMedications,
                    ingredients, lotNumber, expirationDate ->
                    assertEquals("06313728", text)
                    assertEquals(MedicationCategory.UNKNOWN, medicationCategory)
                    assertEquals(null, form)
                    assertEquals(null, amount)
                    assertEquals(false, vaccine)
                    assertEquals(null, manufacturingInstructions)
                    assertEquals(null, packaging)
                    assertEquals(null, normSizeCode)
                    assertEquals("06313728", identifier.pzn)
                    assertEquals(listOf(), ingredients)
                    assertTrue(ingredientMedications.isEmpty())
                    assertEquals(null, lotNumber)
                    assertEquals(null, expirationDate)
                    ReturnType.Medication
                },
                processMedicationDispense = { dispenseId, patientIdentifier, medication, wasSubstituted,
                    dosageInstruction, performer, whenHandedOver ->
                    assertEquals("160.000.000.000.000.01", dispenseId)
                    assertEquals("X123456789", patientIdentifier)
                    assertEquals(ReturnType.Medication, medication)
                    assertEquals(false, wasSubstituted)
                    assertEquals(null, dosageInstruction)
                    assertEquals("3-SMC-B-Testkarte-883110000095957", performer)
                    assertEquals(LocalDate.parse("2025-09-06").asFhirTemporal(), whenHandedOver)
                    ReturnType.MedicationDispense
                }
            )
            assertEquals(ReturnType.MedicationDispense, result)
        }
    }

    @Test
    fun `extract medication dispense compounding medication version 1_4`() {
        val medicationDispensesBundle = Json.parseToJsonElement(dispenseCompoundingMedication_1_4)
        val dispensesWithMedication = extractMedicationDispensePairs(medicationDispensesBundle)

        assertEquals(1, dispensesWithMedication.size)

        dispensesWithMedication.forEach { (dispense, dispenseMedication) ->
            val result = extractMedicationDispenseWithMedication(
                dispense,
                dispenseMedication,
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
                processMedication = { _, _, _, _, _,
                    _, _, _, _, _,
                    _, _, _ ->
                    ReturnType.Medication
                },
                processMedicationDispense = { dispenseId, patientIdentifier, medication, wasSubstituted,
                    dosageInstruction, performer, whenHandedOver ->
                    assertEquals("160.000.000.000.000.03", dispenseId)
                    assertEquals("X123456789", patientIdentifier)
                    assertEquals(ReturnType.Medication, medication)
                    assertEquals(false, wasSubstituted)
                    assertEquals(null, dosageInstruction)
                    assertEquals("3-SMC-B-Testkarte-883110000095957", performer)
                    assertEquals(LocalDate.parse("2025-09-06").asFhirTemporal(), whenHandedOver)
                    ReturnType.MedicationDispense
                }
            )
            assertEquals(ReturnType.MedicationDispense, result)
        }
    }

    @Test
    fun `extract multiple dispenses with medications version 1_4`() {
        val medicationDispensesBundle = Json.parseToJsonElement(dispenseMultipleMedication_1_4)
        val dispensesWithMedication = extractMedicationDispensePairs(medicationDispensesBundle)

        assertEquals(2, dispensesWithMedication.size)

        dispensesWithMedication.forEachIndexed() { index, (dispense, dispenseMedication) ->
            val result = extractMedicationDispenseWithMedication(
                dispense,
                dispenseMedication,
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
                processMedication = { _, _, _, _, _,
                    _, _, _, _, _,
                    _, _, _ ->
                    ReturnType.Medication
                },
                processMedicationDispense = { dispenseId, patientIdentifier, medication, wasSubstituted,
                    dosageInstruction, performer, whenHandedOver ->

                    if (index == 0) {
                        assertEquals("160.000.000.000.000.01", dispenseId)
                    } else {
                        assertEquals("160.000.000.000.000.02", dispenseId)
                    }
                    assertEquals("X123456789", patientIdentifier)
                    assertEquals(ReturnType.Medication, medication)
                    assertEquals(false, wasSubstituted)
                    assertEquals(null, dosageInstruction)
                    assertEquals("3-SMC-B-Testkarte-883110000095957", performer)
                    assertEquals(LocalDate.parse("2025-09-06").asFhirTemporal(), whenHandedOver)
                    ReturnType.MedicationDispense
                }
            )
            assertEquals(ReturnType.MedicationDispense, result)
        }
    }
}
