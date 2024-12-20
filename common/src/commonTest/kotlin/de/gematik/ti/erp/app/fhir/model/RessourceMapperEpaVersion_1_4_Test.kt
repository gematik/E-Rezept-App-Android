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

import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.stringValue
import de.gematik.ti.erp.app.utils.toFhirTemporal
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("ClassNaming")
class RessourceMapperEpaVersion_1_4_Test {

    @Test
    fun `process simple medication version 1_4`() {
        val medicationJson = Json.parseToJsonElement(simpleMedication_1_4)
        extractDispenseMedication(
            medicationJson,
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
            processMedication = { text,
                medicationCategory,
                form,
                amount,
                vaccine,
                manufacturingInstructions,
                packaging,
                normSizeCode,
                identifier,
                ingredientMedication,
                ingredients,
                lotNumber,
                expirationDate ->
                assertEquals("82082973", text)
                assertEquals(MedicationCategory.UNKNOWN, medicationCategory)
                assertEquals(null, form)
                assertEquals(null, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals(null, normSizeCode)
                assertEquals(Identifier("82082973"), identifier)
                assertEquals(listOf(), ingredients)
                assertTrue(ingredientMedication.isEmpty())

                assertEquals("1419556306", lotNumber)
                assertEquals("2024-12-25T02:35:18+00:00".toFhirTemporal(), expirationDate)

                ReturnType.Medication
            }
        )
    }

    @Test
    fun `process pharmaceutical product`() {
        val medicationJson = Json.parseToJsonElement(pharmaceuticalProduct)
        extractDispenseMedication(
            medicationJson,
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
            processMedication = { text,
                medicationCategory,
                form,
                amount,
                vaccine,
                manufacturingInstructions,
                packaging,
                normSizeCode,
                identifier,
                ingredientMedication,
                ingredients,
                lotNumber,
                expirationDate ->
                assertEquals("01746517-1", text)
                assertEquals(MedicationCategory.UNKNOWN, medicationCategory)
                assertEquals(null, form)
                assertEquals(null, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals(null, normSizeCode)
                assertEquals(Identifier(), identifier)
                assertEquals(1, ingredients.size)
                assertTrue(ingredientMedication.isEmpty())

                assertEquals(null, lotNumber)
                assertEquals(null, expirationDate)
            }
        )
        val ingredient = medicationJson.findAll("ingredient").toList()[0]

        ingredient.extractEpaIngredient(
            quantityFn = { _, _ ->
                ReturnType.Quantity
            },
            ratioFn = { numerator, denominator ->
                assertEquals(ReturnType.Quantity, numerator)
                assertEquals(ReturnType.Quantity, denominator)
                ReturnType.Ratio
            },
            ingredientFn = { text,
                form,
                identifier,
                amount,
                strength ->

                assertEquals("Natriumcromoglicat", text)
                assertEquals(null, form)
                assertEquals(Identifier(atc = "R01AC01"), identifier)
                assertEquals(null, amount)
                assertEquals(ReturnType.Ratio, strength)
                ReturnType.Ingredient
            }
        )
    }

    @Test
    fun `process complex medication version 1_4`() {
        val medicationJson = Json.parseToJsonElement(complexMedication_1_4)
        extractDispenseMedication(
            medicationJson,
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
            processMedication = { _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _,
                _ ->

                ReturnType.Medication
            }
        )
    }

    @Test
    fun `process contained medication version 1_4`() {
        val medicationJson = Json.parseToJsonElement(complexMedication_1_4)

        val containedMedications = medicationJson.findAll("contained")
            .filterWith(
                "resourceType",
                stringValue("Medication")
            ).toList()

        extractContainedMedication(
            containedMedications[0],
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
            processIngredientMedication = { text,
                medicationCategory,
                form,
                amount,
                vaccine,
                manufacturingInstructions,
                packaging,
                normSizeCode,
                identifier,
                ingredientMedication,
                ingredients,
                lotNumber,
                expirationDate ->

                assertEquals(null, text)
                assertEquals(MedicationCategory.UNKNOWN, medicationCategory)
                assertEquals(null, form)
                assertEquals(null, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals(null, normSizeCode)
                assertEquals(Identifier(), identifier)
                assertTrue(ingredientMedication.isEmpty())
                assertTrue(ingredients.isNotEmpty())
                assertEquals(null, lotNumber)
                assertEquals(null, expirationDate)
            }
        )

        extractContainedMedication(
            containedMedications[1],
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
            processIngredientMedication = { text,
                medicationCategory,
                form,
                amount,
                vaccine,
                manufacturingInstructions,
                packaging,
                normSizeCode,
                identifier,
                ingredientMedication,
                ingredients,
                lotNumber,
                expirationDate ->

                assertEquals(null, text)
                assertEquals(MedicationCategory.UNKNOWN, medicationCategory)
                assertEquals(null, form)
                assertEquals(null, amount)
                assertEquals(false, vaccine)
                assertEquals(null, manufacturingInstructions)
                assertEquals(null, packaging)
                assertEquals(null, normSizeCode)
                assertEquals(Identifier(), identifier)
                assertTrue(ingredientMedication.isEmpty())
                assertTrue(ingredients.isNotEmpty())
                assertEquals(null, lotNumber)
                assertEquals(null, expirationDate)
            }

        )

        val ingredientsFromFirstContained = containedMedications[0].findAll("ingredient").toList()
        assertEquals(1, ingredientsFromFirstContained.size)

        val ingredientsFromSecondContained = containedMedications[1].findAll("ingredient").toList()
        assertEquals(1, ingredientsFromSecondContained.size)

        ingredientsFromFirstContained[0].extractEpaIngredient(
            quantityFn = { _, _ ->
                ReturnType.Quantity
            },
            ratioFn = { numerator, denominator ->
                assertEquals(ReturnType.Quantity, numerator)
                assertEquals(ReturnType.Quantity, denominator)
                ReturnType.Ratio
            },
            ingredientFn = { text, form, identifier, amount, strength ->
                assertEquals("Natriumcromoglicat", text)
                assertEquals(null, form)
                assertEquals(Identifier(atc = "R01AC01"), identifier)
                assertEquals(null, amount)
                assertEquals(ReturnType.Ratio, strength)
            }
        )

        ingredientsFromSecondContained[0].extractEpaIngredient(
            quantityFn = { _, _ ->
                ReturnType.Quantity
            },
            ratioFn = { numerator, denominator ->
                assertEquals(ReturnType.Quantity, numerator)
                assertEquals(ReturnType.Quantity, denominator)
                ReturnType.Ratio
            },
            ingredientFn = { text, form, identifier, amount, strength ->
                assertEquals("Natriumcromoglicat", text)
                assertEquals(null, form)
                assertEquals(Identifier(atc = "R01AC01"), identifier)
                assertEquals(null, amount)
                assertEquals(ReturnType.Ratio, strength)
            }
        )
    }
}
