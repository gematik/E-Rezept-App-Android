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
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

enum class PKVReturnType {
    InvoiceBundle, Invoice, Pharmacy, PharmacyAddress, Dispense
}

class InvoiceMapperTest {
    @Test
    fun `process pkv bundle version 1_1`() {
        val bundle = Json.parseToJsonElement(pkvAbgabedatenJson_vers_1_1)

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.000.001.205.203.40", taskId)
            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUmwYJKoZIhvcNAQcCoIIUjDCCFIgCAQUxDTALBglghkgBZQMEAgEwggp1Bgkqh",
                erpBinary?.decodeToString()
            )

            assertEquals(
                "MIIuswYJKoZIhvcNAQcCoIIupDCCLqACAQExDTALBglghkgBZQMEAgEwghlq",
                invoiceBinary?.decodeToString()
            )

            assertEquals(
                "MII01wYJKoZIhvcNAQcCoII0yDCCNMQCAQUxDTALBglghkgBZQM",
                kbvBinary?.decodeToString()
            )

            extractInvoiceBundle(
                invoiceBundle,
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items ->
                    assertEquals(217.69, totalAdditionalFee)
                    assertEquals(534.2, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("83251243"),
                            1.0,
                            InvoiceData.PriceComponent(6.23, 11.06)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("22894670"),
                            1.0,
                            InvoiceData.PriceComponent(527.97, 11.06)
                        ),
                        items[1]
                    )
                    assertEquals(
                        false,
                        (items[1].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    PKVReturnType.Invoice
                },
                processDispense = { whenHandedOver ->
                    assertEquals(LocalDate.parse("2023-02-17").asFhirTemporal(), whenHandedOver)

                    PKVReturnType.Dispense
                },
                processPharmacyAddress = { line, postalCode, city ->
                    assertEquals(listOf("Görresstr. 789"), line)
                    assertEquals("48480", postalCode)
                    assertEquals("Süd Eniefeld", city)

                    PKVReturnType.PharmacyAddress
                },
                processPharmacy = { name, address, bsnr, iknr, phone, mail ->
                    assertEquals("Apotheke Crystal Claire Waters", name)
                    assertEquals(PKVReturnType.PharmacyAddress, address)
                    assertEquals(null, bsnr)
                    assertEquals("833940499", iknr)
                    assertEquals(null, phone)
                    assertEquals(null, mail)

                    PKVReturnType.Pharmacy
                },
                save = { taskId, _, pharmacy, invoice, dispense ->
                    assertEquals("200.000.001.205.203.40", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        })
    }

    @Test
    fun `extract task id from chargeItem bundle`() {
        val bundle = Json.parseToJsonElement(charge_item_bundle_version_1_2)
        val (bundleTotal, taskIds) = extractTaskIdsFromChargeItemBundle(bundle)
        assertEquals(2, bundleTotal)
        assertEquals("200.086.824.605.539.20", taskIds[0])
        assertEquals("200.086.824.605.539.20", taskIds[1])
    }
}
