/*
 * Copyright (c) 2024 gematik GmbH
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
import kotlin.test.Test
import kotlin.test.assertEquals

class CommonRessourceMapperTest {

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
            processOrganization = { name, address, bsnr, iknr, phone, mail ->
                assertEquals("MVZ", name)
                assertEquals(ReturnType.Address, address)
                assertEquals("721111100", bsnr)
                assertEquals(null, iknr)
                assertEquals("0301234567", phone)
                assertEquals("mvz@e-mail.de", mail)

                ReturnType.Organization
            }
        )

        assertEquals(ReturnType.Organization, result)
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
    fun `process ingredient with amount extension`() {
        val ingredientJson = Json.parseToJsonElement(ingredientWithAmountJson)
        val result = ingredientJson.extractIngredient(
            quantityFn = { _, _ ->
                null
            },
            ratioFn = { _, _ ->
                null
            },
            ingredientFn = { text, form, number, amount, strength ->
                assertEquals("2-propanol 70 %", text)
                assertEquals(null, form)
                assertEquals(null, number)
                assertEquals("Ad 100 g", amount)
                assertEquals(null, strength)

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
            processMultiplePrescriptionInfo = { indicator, numbering, start, end ->
                assertEquals(true, indicator)
                assertEquals(ReturnType.Ratio, numbering)
                assertEquals("2022-08-17", start?.formattedString())
                assertEquals("2022-11-25", end?.formattedString())
                ReturnType.MultiplePrescriptionInfo
            }
        )
        assertEquals(ReturnType.MultiplePrescriptionInfo, result)
    }
}
