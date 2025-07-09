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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.containedStringOrNull
import de.gematik.ti.erp.app.fhir.parser.findAll
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
    fun `test organization address for both _line extensions and line fallback values in PZN-8 v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_8_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { _, _, _, kbvBundle, _ ->
            val organization = kbvBundle
                .findAll("entry")
                .find { it.containedOrNull("resource")?.containedString("resourceType") == "Organization" }
                ?.containedOrNull("resource")

            requireNotNull(organization)

            // Test address _line extensions
            val address = organization.containedOrNull("address")
            requireNotNull(address)

            val lineExt = address.containedArrayOrNull("_line")?.firstOrNull()?.containedArrayOrNull("extension")
            val streetName = lineExt?.find {
                it.containedString("url") == "http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName"
            }?.containedStringOrNull("valueString")
            val houseNumber = lineExt?.find {
                it.containedString("url") == "http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber"
            }?.containedStringOrNull("valueString")

            assertEquals("Herbert-Lewin-Platz", streetName)
            assertEquals("2", houseNumber)

            // Test address line (fallback)
            val originalLine = address.containedArrayOrNull("line")?.firstOrNull()?.containedString()
            assertEquals("Herbert-Lewin-Platz 2", originalLine)

            extractOrganization(
                organization,
                processAddress = { line, postalCode, city ->
                    assertEquals(listOf("Herbert-Lewin-Platz 2"), line)
                    assertEquals("10623", postalCode)
                    assertEquals("Berlin", city)
                    ReturnType.Address
                },
                processOrganization = { name, address, _, _, _, _ ->
                    assertEquals("MVZ", name)
                    assertEquals(ReturnType.Address, address)
                    ReturnType.Organization
                }
            )
        }
    }

    @Test
    fun `test patient address for both _line extensions and line fallback values in PZN-8 v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_8_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { _, _, _, kbvBundle, _ ->
            val patient = kbvBundle
                .findAll("entry")
                .find { it.containedOrNull("resource")?.containedString("resourceType") == "Patient" }
                ?.containedOrNull("resource")

            requireNotNull(patient)

            // Test address _line extensions
            val address = patient.containedOrNull("address")
            requireNotNull(address)

            val lineExt = address.containedArrayOrNull("_line")?.firstOrNull()?.containedArrayOrNull("extension")
            val streetName = lineExt?.find {
                it.containedString("url") == "http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName"
            }?.containedStringOrNull("valueString")
            val houseNumber = lineExt?.find {
                it.containedString("url") == "http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber"
            }?.containedStringOrNull("valueString")

            assertEquals("Blumenweg", streetName)
            assertEquals("18", houseNumber)

            // Test address line (fallback)
            val originalLine = address.containedArrayOrNull("line")?.firstOrNull()?.containedString()
            assertEquals("Blumenweg 18", originalLine)

            extractPatientVersion110(
                patient,
                processAddress = { line, postalCode, city ->
                    assertEquals(listOf("Blumenweg 18"), line)
                    assertEquals("26427", postalCode)
                    assertEquals("Esens", city)
                    ReturnType.Address
                },
                processPatient = { name, address, _, _ ->
                    assertEquals("Paula Privati", name)
                    assertEquals(ReturnType.Address, address)
                    ReturnType.Patient
                }
            )
        }
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
            processInsuranceInformation = { name, statusCode, coverageType ->
                assertEquals("HEK", name)
                assertEquals("3", statusCode)
                assertEquals("GKV", coverageType)

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
            ingredientFn = { text, form, identifier, amount, strength ->
                assertEquals("Wirkstoff Paulaner Weissbier", text)
                assertEquals(null, form)
                assertEquals(Identifier(ask = "37197"), identifier)
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
            ingredientFn = { text, form, identifier, amount, strength ->
                assertEquals("2-propanol 70 %", text)
                assertEquals(null, form)
                assertEquals(Identifier(), identifier)
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
