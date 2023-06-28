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

@Suppress("LargeClass")
class InvoiceMapperTest {
    @Test
    fun `process chargeItem pzn 1`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_1)

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.424.187.927.272.20", taskId)
            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                erpBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                invoiceBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                kbvBinary?.decodeToString()
            )

            extractInvoiceBundle(
                invoiceBundle,
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(21.04, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("03879429"),
                            "BELOC-ZOK mite 47,5 mg Retardtabletten 30 St",
                            1.0,
                            InvoiceData.PriceComponent(21.04, 19.0)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    PKVReturnType.Invoice
                },
                processDispense = { whenHandedOver ->
                    assertEquals(LocalDate.parse("2023-07-03").asFhirTemporal(), whenHandedOver)

                    PKVReturnType.Dispense
                },
                processPharmacyAddress = { line, postalCode, city ->
                    assertEquals(listOf("Taunusstraße 89"), line)
                    assertEquals("63225", postalCode)
                    assertEquals("Langen", city)

                    PKVReturnType.PharmacyAddress
                },
                processPharmacy = { name, address, bsnr, iknr, phone, mail ->
                    assertEquals("Adler-Apotheke", name)
                    assertEquals(PKVReturnType.PharmacyAddress, address)
                    assertEquals(null, bsnr)
                    assertEquals("308412345", iknr)
                    assertEquals(null, phone)
                    assertEquals(null, mail)

                    PKVReturnType.Pharmacy
                },
                save = { taskId, _, pharmacy, invoice, dispense ->
                    assertEquals("200.424.187.927.272.20", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        })
    }

    @Test
    fun `process chargeItem pzn 2`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_2)

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.457.180.497.994.96", taskId)
            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQM",
                erpBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                invoiceBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                kbvBinary?.decodeToString()
            )

            extractInvoiceBundle(
                invoiceBundle,
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(31.4, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("09494280"),
                            "VENLAFAXIN Heumann 75 mg Tabletten 100 St",
                            1.0,
                            InvoiceData.PriceComponent(31.4, 19.0)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    PKVReturnType.Invoice
                },
                processDispense = { whenHandedOver ->
                    assertEquals(LocalDate.parse("2023-07-03").asFhirTemporal(), whenHandedOver)

                    PKVReturnType.Dispense
                },
                processPharmacyAddress = { line, postalCode, city ->
                    assertEquals(listOf("Taunusstraße 89"), line)
                    assertEquals("63225", postalCode)
                    assertEquals("Langen", city)

                    PKVReturnType.PharmacyAddress
                },
                processPharmacy = { name, address, bsnr, iknr, phone, mail ->
                    assertEquals("Adler-Apotheke", name)
                    assertEquals(PKVReturnType.PharmacyAddress, address)
                    assertEquals(null, bsnr)
                    assertEquals("308412345", iknr)
                    assertEquals(null, phone)
                    assertEquals(null, mail)

                    PKVReturnType.Pharmacy
                },
                save = { taskId, _, pharmacy, invoice, dispense ->
                    assertEquals("200.457.180.497.994.96", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        })
    }

    @Test
    fun `process chargeItem pzn 3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_3)

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.279.187.481.423.80", taskId)
            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1",
                erpBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                invoiceBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                kbvBinary?.decodeToString()
            )

            extractInvoiceBundle(
                invoiceBundle,
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(24.32, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("03386388"),
                            "InfectoCortiKrupp® Zäpfchen 100 mg 3 St",
                            1.0,
                            InvoiceData.PriceComponent(21.82, 19.0)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.TA1("02567018"),
                            "Noctu-Gebühr",
                            1.0,
                            InvoiceData.PriceComponent(2.5, 19.0)
                        ),
                        items[1]
                    )
                    assertEquals(
                        true,
                        (items[1].description as InvoiceData.ChargeableItem.Description.TA1).isSpecialPZN()
                    )

                    PKVReturnType.Invoice
                },
                processDispense = { whenHandedOver ->
                    assertEquals(LocalDate.parse("2023-07-03").asFhirTemporal(), whenHandedOver)

                    PKVReturnType.Dispense
                },
                processPharmacyAddress = { line, postalCode, city ->
                    assertEquals(listOf("Taunusstraße 89"), line)
                    assertEquals("63225", postalCode)
                    assertEquals("Langen", city)

                    PKVReturnType.PharmacyAddress
                },
                processPharmacy = { name, address, bsnr, iknr, phone, mail ->
                    assertEquals("Adler-Apotheke", name)
                    assertEquals(PKVReturnType.PharmacyAddress, address)
                    assertEquals(null, bsnr)
                    assertEquals("308412345", iknr)
                    assertEquals(null, phone)
                    assertEquals(null, mail)

                    PKVReturnType.Pharmacy
                },
                save = { taskId, _, pharmacy, invoice, dispense ->
                    assertEquals("200.279.187.481.423.80", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        })
    }

    @Test
    fun `process chargeItem pzn 5`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_5)

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.625.688.123.368.48", taskId)
            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1",
                erpBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                invoiceBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                kbvBinary?.decodeToString()
            )

            extractInvoiceBundle(
                invoiceBundle,
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(82.68, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("00427833"),
                            "Viani 50µg/250µg 1 Diskus 60 ED N1",
                            2.0,
                            InvoiceData.PriceComponent(82.68, 19.0)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    PKVReturnType.Invoice
                },
                processDispense = { whenHandedOver ->
                    assertEquals(LocalDate.parse("2023-07-03").asFhirTemporal(), whenHandedOver)

                    PKVReturnType.Dispense
                },
                processPharmacyAddress = { line, postalCode, city ->
                    assertEquals(listOf("Taunusstraße 89"), line)
                    assertEquals("63225", postalCode)
                    assertEquals("Langen", city)

                    PKVReturnType.PharmacyAddress
                },
                processPharmacy = { name, address, bsnr, iknr, phone, mail ->
                    assertEquals("Adler-Apotheke", name)
                    assertEquals(PKVReturnType.PharmacyAddress, address)
                    assertEquals(null, bsnr)
                    assertEquals("308412345", iknr)
                    assertEquals(null, phone)
                    assertEquals(null, mail)

                    PKVReturnType.Pharmacy
                },
                save = { taskId, _, pharmacy, invoice, dispense ->
                    assertEquals("200.625.688.123.368.48", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        })
    }

    @Test
    fun `process chargeItem pzn 6`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_6)

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.280.604.133.110.12", taskId)
            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1",
                erpBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                invoiceBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                kbvBinary?.decodeToString()
            )

            extractInvoiceBundle(
                invoiceBundle,
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(42.77, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("02091840"),
                            "CONCOR 10 PLUS Filmtabletten 100 St",
                            1.0,
                            InvoiceData.PriceComponent(42.77, 19.0)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    PKVReturnType.Invoice
                },
                processDispense = { whenHandedOver ->
                    assertEquals(LocalDate.parse("2023-07-03").asFhirTemporal(), whenHandedOver)

                    PKVReturnType.Dispense
                },
                processPharmacyAddress = { line, postalCode, city ->
                    assertEquals(listOf("Taunusstraße 89"), line)
                    assertEquals("63225", postalCode)
                    assertEquals("Langen", city)

                    PKVReturnType.PharmacyAddress
                },
                processPharmacy = { name, address, bsnr, iknr, phone, mail ->
                    assertEquals("Adler-Apotheke", name)
                    assertEquals(PKVReturnType.PharmacyAddress, address)
                    assertEquals(null, bsnr)
                    assertEquals("308412345", iknr)
                    assertEquals(null, phone)
                    assertEquals(null, mail)

                    PKVReturnType.Pharmacy
                },
                save = { taskId, _, pharmacy, invoice, dispense ->
                    assertEquals("200.280.604.133.110.12", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        })
    }

    @Test
    fun `process chargeItem pzn 7`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_7)

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.339.908.107.779.64", taskId)
            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "aYDkjPosw3Sa5dX5EmSghwhVg7d9jhoXHdwszETXV/8=",
                erpBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                invoiceBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                kbvBinary?.decodeToString()
            )

            extractInvoiceBundle(
                invoiceBundle,
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(63.84, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("11514676"),
                            "Amoxicillin/Clavulansäure Heumann 875 mg/125 mg 10 St",
                            2.0,
                            InvoiceData.PriceComponent(61.34, 19.0)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.TA1("02567018"),
                            "Noctu-Gebühr",
                            1.0,
                            InvoiceData.PriceComponent(2.5, 19.0)
                        ),
                        items[1]
                    )
                    assertEquals(
                        true,
                        (items[1].description as InvoiceData.ChargeableItem.Description.TA1).isSpecialPZN()
                    )

                    PKVReturnType.Invoice
                },
                processDispense = { whenHandedOver ->
                    assertEquals(LocalDate.parse("2023-07-03").asFhirTemporal(), whenHandedOver)

                    PKVReturnType.Dispense
                },
                processPharmacyAddress = { line, postalCode, city ->
                    assertEquals(listOf("Taunusstraße 89"), line)
                    assertEquals("63225", postalCode)
                    assertEquals("Langen", city)

                    PKVReturnType.PharmacyAddress
                },
                processPharmacy = { name, address, bsnr, iknr, phone, mail ->
                    assertEquals("Adler-Apotheke", name)
                    assertEquals(PKVReturnType.PharmacyAddress, address)
                    assertEquals(null, bsnr)
                    assertEquals("308412345", iknr)
                    assertEquals(null, phone)
                    assertEquals(null, mail)

                    PKVReturnType.Pharmacy
                },
                save = { taskId, _, pharmacy, invoice, dispense ->
                    assertEquals("200.339.908.107.779.64", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        })
    }

    @Test
    fun `process chargeItem pzn 8`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_8)

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.108.757.032.088.60", taskId)
            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "aYDkjPosw3Sa5dX5EmSghwhVg7d9jhoXHdwszETXV/8=",
                erpBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                invoiceBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                kbvBinary?.decodeToString()
            )

            extractInvoiceBundle(
                invoiceBundle,
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, additionalItem ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(50.97, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.TA1("02567053"),
                            "Auseinzelung",
                            1.0,
                            InvoiceData.PriceComponent(50.97, 19.0)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.TA1).isSpecialPZN()
                    )

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("17543785"),
                            "",
                            0.1,
                            InvoiceData.PriceComponent(0.0, 0.0)
                        ),
                        additionalItem
                    )

                    assertEquals(
                        false,
                        (additionalItem?.description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    PKVReturnType.Invoice
                },
                processDispense = { whenHandedOver ->
                    assertEquals(LocalDate.parse("2023-07-03").asFhirTemporal(), whenHandedOver)

                    PKVReturnType.Dispense
                },
                processPharmacyAddress = { line, postalCode, city ->
                    assertEquals(listOf("Taunusstraße 89"), line)
                    assertEquals("63225", postalCode)
                    assertEquals("Langen", city)

                    PKVReturnType.PharmacyAddress
                },
                processPharmacy = { name, address, bsnr, iknr, phone, mail ->
                    assertEquals("Adler-Apotheke", name)
                    assertEquals(PKVReturnType.PharmacyAddress, address)
                    assertEquals(null, bsnr)
                    assertEquals("308412345", iknr)
                    assertEquals(null, phone)
                    assertEquals(null, mail)

                    PKVReturnType.Pharmacy
                },
                save = { taskId, _, pharmacy, invoice, dispense ->
                    assertEquals("200.108.757.032.088.60", taskId)
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

    @Test
    fun `process chargeItem freetext`() {
        val bundle = Json.parseToJsonElement(chargeItem_freetext)

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.334.138.469.717.92", taskId)
            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQM",
                erpBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                invoiceBinary?.decodeToString()
            )

            assertEquals(
                "Y2RjMTVjNThkMzlkMjllNDdjMTk1MjIzNDlkODRjMThiNTliYTZkMGFhZmI5NGYyZjM2NDFkNGJiZTk1ODhiMQ==",
                kbvBinary?.decodeToString()
            )

            extractInvoiceBundle(
                invoiceBundle,
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(36.15, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.TA1("09999117"),
                            "Einzelimport",
                            1.0,
                            InvoiceData.PriceComponent(27.58, 19.0)
                        ),
                        items[0]
                    )

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.TA1("09999637"),
                            "Beschaffungskosten",
                            1.0,
                            InvoiceData.PriceComponent(8.57, 19.0)
                        ),
                        items[1]
                    )

                    PKVReturnType.Invoice
                },
                processDispense = { whenHandedOver ->
                    assertEquals(LocalDate.parse("2023-07-07").asFhirTemporal(), whenHandedOver)

                    PKVReturnType.Dispense
                },
                processPharmacyAddress = { line, postalCode, city ->
                    assertEquals(listOf("Taunusstraße 89"), line)
                    assertEquals("63225", postalCode)
                    assertEquals("Langen", city)

                    PKVReturnType.PharmacyAddress
                },
                processPharmacy = { name, address, bsnr, iknr, phone, mail ->
                    assertEquals("Adler-Apotheke", name)
                    assertEquals(PKVReturnType.PharmacyAddress, address)
                    assertEquals(null, bsnr)
                    assertEquals("308412345", iknr)
                    assertEquals(null, phone)
                    assertEquals(null, mail)

                    PKVReturnType.Pharmacy
                },
                save = { taskId, _, pharmacy, invoice, dispense ->
                    assertEquals("200.334.138.469.717.92", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        })
    }
}
