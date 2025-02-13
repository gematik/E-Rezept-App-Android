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

@file:Suppress("ktlint:max-line-length")

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.utils.asFhirTemporal
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

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
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

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.457.180.497.994.96", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
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

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.279.187.481.423.80", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
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

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.625.688.123.368.48", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
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

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.280.604.133.110.12", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
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

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.339.908.107.779.64", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
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

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.108.757.032.088.60", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, additionalItems, _ ->
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
                            InvoiceData.PriceComponent(42.83, 0.0)
                        ),
                        additionalItems[0]
                    )

                    assertEquals(
                        false,
                        (additionalItems[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
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
        }
    }

    @Test
    fun `process chargeItem with GEM_ERP_PR_Bundle v1_4`() {
        val bundle = Json.parseToJsonElement(erp_charge_bundle_1_4)

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.000.000.144.754.78", taskId)
            assertEquals("86807c76ba82eca8264a55131482c4e6500a7ffe1159af14a76cd8944c601cc6", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUOgYJKoZIhvcNAQcCoIIUKzCCFCcCAQUxDTA",
                erpBinary?.decodeToString()
            )

            assertEquals(
                "MIIqXgYJKoZIhvcNAQcCoIIqTzCCKksCAQExDTALBglghkgBZQMEAgEwghnLB",
                invoiceBinary?.decodeToString()
            )

            assertEquals(
                "MII04AYJKoZIhvcNAQcCoII00TCCNM0CAQUxDTALBglghkgBZQMEAgEwgiq4B",
                kbvBinary?.decodeToString()
            )

            extractInvoiceBundle(
                invoiceBundle,
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
                    assertEquals(94.54, totalAdditionalFee)
                    assertEquals(144.07, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("12345679"),
                            "auch Apotheker müssen leben können",
                            1.0,
                            InvoiceData.PriceComponent(100.50, 12.90)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("53879328"),
                            "auch Apotheker müssen leben können",
                            1.0,
                            InvoiceData.PriceComponent(43.57, 12.90)
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
                    assertEquals(LocalDate.parse("2024-12-12").asFhirTemporal(), whenHandedOver)

                    PKVReturnType.Dispense
                },
                processPharmacyAddress = { line, postalCode, city ->
                    assertEquals(listOf("Am Vogelkreuz 60"), line)
                    assertEquals("65621", postalCode)
                    assertEquals("Schwandkefeld", city)

                    PKVReturnType.PharmacyAddress
                },
                processPharmacy = { name, address, bsnr, iknr, phone, mail ->
                    assertEquals("Adelheid Ulmendorfer", name)
                    assertEquals(PKVReturnType.PharmacyAddress, address)
                    assertEquals(null, bsnr)
                    assertEquals("545574356", iknr)
                    assertEquals(null, phone)
                    assertEquals(null, mail)

                    PKVReturnType.Pharmacy
                },
                save = { taskId, _, pharmacy, invoice, dispense ->
                    assertEquals("200.000.000.144.754.78", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        })
    }

    @Test
    fun `process chargeItem pzn 8 - v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_8_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.108.757.032.088.60", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1BgkqhkiG9w0BBwGgggpmBIIKYjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYzg2MDY3MTItMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDMrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9UYXNrLzIwMC4wMDAuMDAxLjIwNi4xMTIuMjkvJGNsb3NlLyIvPjwvbGluaz48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9InVybjp1dWlkOjFmMTA3YWQ3LWY0YmYtNDdlMy1hNjA2LTAxODFmNWRkNGY3MyIvPjxyZXNvdXJjZT48Q29tcG9zaXRpb24+PGlkIHZhbHVlPSIxZjEwN2FkNy1mNGJmLTQ3ZTMtYTYwNi0wMTgxZjVkZDRmNzMiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9Db21wb3NpdGlvbnwxLjIiLz48L21ldGE+PGV4dGVuc2lvbiB1cmw9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfRVhfQmVuZWZpY2lhcnkiPjx2YWx1ZUlkZW50aWZpZXI+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvc2lkL3RlbGVtYXRpay1pZCIvPjx2YWx1ZSB2YWx1ZT0iMy1TTUMtQi1UZXN0a2FydGUtODgzMTEwMDAwMTE2ODczIi8+PC92YWx1ZUlkZW50aWZpZXI+PC9leHRlbnNpb24+PHN0YXR1cyB2YWx1ZT0iZmluYWwiLz48dHlwZT48Y29kaW5nPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9Db2RlU3lzdGVtL0dFTV9FUlBfQ1NfRG9jdW1lbnRUeXBlIi8+PGNvZGUgdmFsdWU9IjMiLz48ZGlzcGxheSB2YWx1ZT0iUmVjZWlwdCIvPjwvY29kaW5nPjwvdHlwZT48ZGF0ZSB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDIrMDA6MDAiLz48YXV0aG9yPjxyZWZlcmVuY2UgdmFsdWU9Imh0dHBzOi8vZXJwLWRldi56ZW50cmFsLmVycC5zcGxpdGRucy50aS1kaWVuc3RlLmRlL0RldmljZS8xIi8+PC9hdXRob3I+PHRpdGxlIHZhbHVlPSJRdWl0dHVuZyIvPjxldmVudD48cGVyaW9kPjxzdGFydCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODoyOS44NDMrMDA6MDAiLz48ZW5kIHZhbHVlPSIyMDIzLTAyLTIzVDE1OjA4OjMwLjgwMiswMDowMCIvPjwvcGVyaW9kPjwvZXZlbnQ+PHNlY3Rpb24+PGVudHJ5PjxyZWZlcmVuY2UgdmFsdWU9IkJpbmFyeS9QcmVzY3JpcHRpb25EaWdlc3QtMjAwLjAwMC4wMDEuMjA2LjExMi4yOSIvPjwvZW50cnk+PC9zZWN0aW9uPjwvQ29tcG9zaXRpb24+PC9yZXNvdXJjZT48L2VudHJ5PjxlbnRyeT48ZnVsbFVybCB2YWx1ZT0iaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuOS4wIi8+PGRldmljZU5hbWU+PG5hbWUgdmFsdWU9IkUtUmV6ZXB0IEZhY2hkaWVuc3QiLz48dHlwZSB2YWx1ZT0idXNlci1mcmllbmRseS1uYW1lIi8+PC9kZXZpY2VOYW1lPjx2ZXJzaW9uPjx2YWx1ZSB2YWx1ZT0iMS45LjAiLz48L3ZlcnNpb24+PGNvbnRhY3Q+PHN5c3RlbSB2YWx1ZT0iZW1haWwiLz48dmFsdWUgdmFsdWU9ImJldHJpZWJAZ2VtYXRpay5kZSIvPjwvY29udGFjdD48L0RldmljZT48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0yMDAuMDAwLjAwMS4yMDYuMTEyLjI5Ii8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iYVlEa2pQb3N3M1NhNWRYNUVtU2dod2hWZzdkOWpob1hIZHdzekVUWFYvOD0iLz48L0JpbmFyeT48L3Jlc291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",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(54.81, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("18831500"),
                            "EFLUELDA Tetra 2024/2025 Inj.-Susp.i.e.F.-Sp.o.Kan N1",
                            1.0,
                            InvoiceData.PriceComponent(54.81, 19.0),
                            partialQuantityDelivery = true,
                            spenderPzn = "18831517"
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
                    assertEquals(LocalDate.parse("2024-11-03").asFhirTemporal(), whenHandedOver)

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
        }
    }

    @Test
    fun `process chargeItem pzn 1 - v1_3 `() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_1_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.424.187.927.272.20", taskId)
            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUoAYJKoZIhvcNAQcCoIIUkTCCFI0CAQUxDTALBglghkgBZQMEAgEwggp7BgkqhkiG9w0BBwGgggpsBIIKaDw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYTBlMGY4NjEtMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjE2MC4wMDAuMDA2LjQyMC43MDQuMTEiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0xMS0yN1QwNzoyNjo1OS4yMTkrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC10ZXN0LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvVGFzay8xNjAuMDAwLjAwNi40MjAuNzA0LjExLyRjbG9zZS8iLz48L2xpbms+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJ1cm46dXVpZDowMmI2NjBlNi0wZWYxLTQ5NmQtODAxYy0zNDg1MTI2NjY1YTciLz48cmVzb3VyY2U+PENvbXBvc2l0aW9uPjxpZCB2YWx1ZT0iMDJiNjYwZTYtMGVmMS00OTZkLTgwMWMtMzQ4NTEyNjY2NWE3Ii8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQ29tcG9zaXRpb258MS4yIi8+PC9tZXRhPjxleHRlbnNpb24gdXJsPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX0VYX0JlbmVmaWNpYXJ5Ij48dmFsdWVJZGVudGlmaWVyPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL3NpZC90ZWxlbWF0aWstaWQiLz48dmFsdWUgdmFsdWU9IjMtU01DLUItVGVzdGthcnRlLTg4MzExMDAwMDExNjg3MyIvPjwvdmFsdWVJZGVudGlmaWVyPjwvZXh0ZW5zaW9uPjxzdGF0dXMgdmFsdWU9ImZpbmFsIi8+PHR5cGU+PGNvZGluZz48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvQ29kZVN5c3RlbS9HRU1fRVJQX0NTX0RvY3VtZW50VHlwZSIvPjxjb2RlIHZhbHVlPSIzIi8+PGRpc3BsYXkgdmFsdWU9IlJlY2VpcHQiLz48L2NvZGluZz48L3R5cGU+PGRhdGUgdmFsdWU9IjIwMjMtMTEtMjdUMDc6MjY6NTkuMjE3KzAwOjAwIi8+PGF1dGhvcj48cmVmZXJlbmNlIHZhbHVlPSJodHRwczovL2VycC10ZXN0LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48L2F1dGhvcj48dGl0bGUgdmFsdWU9IlF1aXR0dW5nIi8+PGV2ZW50PjxwZXJpb2Q+PHN0YXJ0IHZhbHVlPSIyMDIzLTExLTI3VDA3OjI2OjU3LjY3NyswMDowMCIvPjxlbmQgdmFsdWU9IjIwMjMtMTEtMjdUMDc6MjY6NTkuMjE3KzAwOjAwIi8+PC9wZXJpb2Q+PC9ldmVudD48c2VjdGlvbj48ZW50cnk+PHJlZmVyZW5jZSB2YWx1ZT0iQmluYXJ5L1ByZXNjcmlwdGlvbkRpZ2VzdC0xNjAuMDAwLjAwNi40MjAuNzA0LjExIi8+PC9lbnRyeT48L3NlY3Rpb24+PC9Db21wb3NpdGlvbj48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC10ZXN0LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuMTIuMCIvPjxkZXZpY2VOYW1lPjxuYW1lIHZhbHVlPSJFLVJlemVwdCBGYWNoZGllbnN0Ii8+PHR5cGUgdmFsdWU9InVzZXItZnJpZW5kbHktbmFtZSIvPjwvZGV2aWNlTmFtZT48dmVyc2lvbj48dmFsdWUgdmFsdWU9IjEuMTIuMCIvPjwvdmVyc2lvbj48Y29udGFjdD48c3lzdGVtIHZhbHVlPSJlbWFpbCIvPjx2YWx1ZSB2YWx1ZT0iYmV0cmllYkBnZW1hdGlrLmRlIi8+PC9jb250YWN0PjwvRGV2aWNlPjwvcmVzb3VyY2U+PC9lbnRyeT48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9Imh0dHBzOi8vZXJwLXRlc3QuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTE2MC4wMDAuMDA2LjQyMC43MDQuMTEiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0xNjAuMDAwLjAwNi40MjAuNzA0LjExIi8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iRUgrRjZoK0RJbVdwbkZCN3F6Rlp3OHlqVE5JdlloRkFpbllmbXNtWHE3UT0iLz48L0JpbmFyeT48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",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
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
                    assertEquals(LocalDate.parse("2024-11-03").asFhirTemporal(), whenHandedOver)

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
        }
    }

    @Test
    fun `process chargeItem pzn 2 - v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_2_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.457.180.497.994.96", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1BgkqhkiG9w0BBwGgggpmBIIKYjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYzg2MDY3MTItMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDMrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9UYXNrLzIwMC4wMDAuMDAxLjIwNi4xMTIuMjkvJGNsb3NlLyIvPjwvbGluaz48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9InVybjp1dWlkOjFmMTA3YWQ3LWY0YmYtNDdlMy1hNjA2LTAxODFmNWRkNGY3MyIvPjxyZXNvdXJjZT48Q29tcG9zaXRpb24+PGlkIHZhbHVlPSIxZjEwN2FkNy1mNGJmLTQ3ZTMtYTYwNi0wMTgxZjVkZDRmNzMiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9Db21wb3NpdGlvbnwxLjIiLz48L21ldGE+PGV4dGVuc2lvbiB1cmw9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfRVhfQmVuZWZpY2lhcnkiPjx2YWx1ZUlkZW50aWZpZXI+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvc2lkL3RlbGVtYXRpay1pZCIvPjx2YWx1ZSB2YWx1ZT0iMy1TTUMtQi1UZXN0a2FydGUtODgzMTEwMDAwMTE2ODczIi8+PC92YWx1ZUlkZW50aWZpZXI+PC9leHRlbnNpb24+PHN0YXR1cyB2YWx1ZT0iZmluYWwiLz48dHlwZT48Y29kaW5nPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9Db2RlU3lzdGVtL0dFTV9FUlBfQ1NfRG9jdW1lbnRUeXBlIi8+PGNvZGUgdmFsdWU9IjMiLz48ZGlzcGxheSB2YWx1ZT0iUmVjZWlwdCIvPjwvY29kaW5nPjwvdHlwZT48ZGF0ZSB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDIrMDA6MDAiLz48YXV0aG9yPjxyZWZlcmVuY2UgdmFsdWU9Imh0dHBzOi8vZXJwLWRldi56ZW50cmFsLmVycC5zcGxpdGRucy50aS1kaWVuc3RlLmRlL0RldmljZS8xIi8+PC9hdXRob3I+PHRpdGxlIHZhbHVlPSJRdWl0dHVuZyIvPjxldmVudD48cGVyaW9kPjxzdGFydCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODoyOS44NDMrMDA6MDAiLz48ZW5kIHZhbHVlPSIyMDIzLTAyLTIzVDE1OjA4OjMwLjgwMiswMDowMCIvPjwvcGVyaW9kPjwvZXZlbnQ+PHNlY3Rpb24+PGVudHJ5PjxyZWZlcmVuY2UgdmFsdWU9IkJpbmFyeS9QcmVzY3JpcHRpb25EaWdlc3QtMjAwLjAwMC4wMDEuMjA2LjExMi4yOSIvPjwvZW50cnk+PC9zZWN0aW9uPjwvQ29tcG9zaXRpb24+PC9yZXNvdXJjZT48L2VudHJ5PjxlbnRyeT48ZnVsbFVybCB2YWx1ZT0iaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuOS4wIi8+PGRldmljZU5hbWU+PG5hbWUgdmFsdWU9IkUtUmV6ZXB0IEZhY2hkaWVuc3QiLz48dHlwZSB2YWx1ZT0idXNlci1mcmllbmRseS1uYW1lIi8+PC9kZXZpY2VOYW1lPjx2ZXJzaW9uPjx2YWx1ZSB2YWx1ZT0iMS45LjAiLz48L3ZlcnNpb24+PGNvbnRhY3Q+PHN5c3RlbSB2YWx1ZT0iZW1haWwiLz48dmFsdWUgdmFsdWU9ImJldHJpZWJAZ2VtYXRpay5kZSIvPjwvY29udGFjdD48L0RldmljZT48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0yMDAuMDAwLjAwMS4yMDYuMTEyLjI5Ii8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iYVlEa2pQb3N3M1NhNWRYNUVtU2dod2hWZzdkOWpob1hIZHdzekVUWFYvOD0iLz48L0JpbmFyeT48L3Jlc291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",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(31.40, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("09494280"),
                            "VENLAFAXIN Heumann 75 mg Tabletten 100 St",
                            1.0,
                            InvoiceData.PriceComponent(31.40, 19.0)
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
                    assertEquals(LocalDate.parse("2024-11-03").asFhirTemporal(), whenHandedOver)

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
        }
    }

    @Test
    fun `process chargeItem pzn 3 - v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_3_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.279.187.481.423.80", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1BgkqhkiG9w0BBwGgggpmBIIKYjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYzg2MDY3MTItMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDMrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9UYXNrLzIwMC4wMDAuMDAxLjIwNi4xMTIuMjkvJGNsb3NlLyIvPjwvbGluaz48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9InVybjp1dWlkOjFmMTA3YWQ3LWY0YmYtNDdlMy1hNjA2LTAxODFmNWRkNGY3MyIvPjxyZXNvdXJjZT48Q29tcG9zaXRpb24+PGlkIHZhbHVlPSIxZjEwN2FkNy1mNGJmLTQ3ZTMtYTYwNi0wMTgxZjVkZDRmNzMiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9Db21wb3NpdGlvbnwxLjIiLz48L21ldGE+PGV4dGVuc2lvbiB1cmw9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfRVhfQmVuZWZpY2lhcnkiPjx2YWx1ZUlkZW50aWZpZXI+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvc2lkL3RlbGVtYXRpay1pZCIvPjx2YWx1ZSB2YWx1ZT0iMy1TTUMtQi1UZXN0a2FydGUtODgzMTEwMDAwMTE2ODczIi8+PC92YWx1ZUlkZW50aWZpZXI+PC9leHRlbnNpb24+PHN0YXR1cyB2YWx1ZT0iZmluYWwiLz48dHlwZT48Y29kaW5nPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9Db2RlU3lzdGVtL0dFTV9FUlBfQ1NfRG9jdW1lbnRUeXBlIi8+PGNvZGUgdmFsdWU9IjMiLz48ZGlzcGxheSB2YWx1ZT0iUmVjZWlwdCIvPjwvY29kaW5nPjwvdHlwZT48ZGF0ZSB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDIrMDA6MDAiLz48YXV0aG9yPjxyZWZlcmVuY2UgdmFsdWU9Imh0dHBzOi8vZXJwLWRldi56ZW50cmFsLmVycC5zcGxpdGRucy50aS1kaWVuc3RlLmRlL0RldmljZS8xIi8+PC9hdXRob3I+PHRpdGxlIHZhbHVlPSJRdWl0dHVuZyIvPjxldmVudD48cGVyaW9kPjxzdGFydCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODoyOS44NDMrMDA6MDAiLz48ZW5kIHZhbHVlPSIyMDIzLTAyLTIzVDE1OjA4OjMwLjgwMiswMDowMCIvPjwvcGVyaW9kPjwvZXZlbnQ+PHNlY3Rpb24+PGVudHJ5PjxyZWZlcmVuY2UgdmFsdWU9IkJpbmFyeS9QcmVzY3JpcHRpb25EaWdlc3QtMjAwLjAwMC4wMDEuMjA2LjExMi4yOSIvPjwvZW50cnk+PC9zZWN0aW9uPjwvQ29tcG9zaXRpb24+PC9yZXNvdXJjZT48L2VudHJ5PjxlbnRyeT48ZnVsbFVybCB2YWx1ZT0iaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuOS4wIi8+PGRldmljZU5hbWU+PG5hbWUgdmFsdWU9IkUtUmV6ZXB0IEZhY2hkaWVuc3QiLz48dHlwZSB2YWx1ZT0idXNlci1mcmllbmRseS1uYW1lIi8+PC9kZXZpY2VOYW1lPjx2ZXJzaW9uPjx2YWx1ZSB2YWx1ZT0iMS45LjAiLz48L3ZlcnNpb24+PGNvbnRhY3Q+PHN5c3RlbSB2YWx1ZT0iZW1haWwiLz48dmFsdWUgdmFsdWU9ImJldHJpZWJAZ2VtYXRpay5kZSIvPjwvY29udGFjdD48L0RldmljZT48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0yMDAuMDAwLjAwMS4yMDYuMTEyLjI5Ii8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iYVlEa2pQb3N3M1NhNWRYNUVtU2dod2hWZzdkOWpob1hIZHdzekVUWFYvOD0iLz48L0JpbmFyeT48L3Jlc291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",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
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
                            InvoiceData.PriceComponent(2.50, 19.0)
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
                    assertEquals(LocalDate.parse("2024-11-03").asFhirTemporal(), whenHandedOver)

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
        }
    }

    @Test
    fun `process chargeItem pzn 5 - v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_5_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->
            assertEquals("200.625.688.123.368.48", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1BgkqhkiG9w0BBwGgggpmBIIKYjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYzg2MDY3MTItMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDMrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9UYXNrLzIwMC4wMDAuMDAxLjIwNi4xMTIuMjkvJGNsb3NlLyIvPjwvbGluaz48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9InVybjp1dWlkOjFmMTA3YWQ3LWY0YmYtNDdlMy1hNjA2LTAxODFmNWRkNGY3MyIvPjxyZXNvdXJjZT48Q29tcG9zaXRpb24+PGlkIHZhbHVlPSIxZjEwN2FkNy1mNGJmLTQ3ZTMtYTYwNi0wMTgxZjVkZDRmNzMiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9Db21wb3NpdGlvbnwxLjIiLz48L21ldGE+PGV4dGVuc2lvbiB1cmw9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfRVhfQmVuZWZpY2lhcnkiPjx2YWx1ZUlkZW50aWZpZXI+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvc2lkL3RlbGVtYXRpay1pZCIvPjx2YWx1ZSB2YWx1ZT0iMy1TTUMtQi1UZXN0a2FydGUtODgzMTEwMDAwMTE2ODczIi8+PC92YWx1ZUlkZW50aWZpZXI+PC9leHRlbnNpb24+PHN0YXR1cyB2YWx1ZT0iZmluYWwiLz48dHlwZT48Y29kaW5nPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9Db2RlU3lzdGVtL0dFTV9FUlBfQ1NfRG9jdW1lbnRUeXBlIi8+PGNvZGUgdmFsdWU9IjMiLz48ZGlzcGxheSB2YWx1ZT0iUmVjZWlwdCIvPjwvY29kaW5nPjwvdHlwZT48ZGF0ZSB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDIrMDA6MDAiLz48YXV0aG9yPjxyZWZlcmVuY2UgdmFsdWU9Imh0dHBzOi8vZXJwLWRldi56ZW50cmFsLmVycC5zcGxpdGRucy50aS1kaWVuc3RlLmRlL0RldmljZS8xIi8+PC9hdXRob3I+PHRpdGxlIHZhbHVlPSJRdWl0dHVuZyIvPjxldmVudD48cGVyaW9kPjxzdGFydCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODoyOS44NDMrMDA6MDAiLz48ZW5kIHZhbHVlPSIyMDIzLTAyLTIzVDE1OjA4OjMwLjgwMiswMDowMCIvPjwvcGVyaW9kPjwvZXZlbnQ+PHNlY3Rpb24+PGVudHJ5PjxyZWZlcmVuY2UgdmFsdWU9IkJpbmFyeS9QcmVzY3JpcHRpb25EaWdlc3QtMjAwLjAwMC4wMDEuMjA2LjExMi4yOSIvPjwvZW50cnk+PC9zZWN0aW9uPjwvQ29tcG9zaXRpb24+PC9yZXNvdXJjZT48L2VudHJ5PjxlbnRyeT48ZnVsbFVybCB2YWx1ZT0iaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuOS4wIi8+PGRldmljZU5hbWU+PG5hbWUgdmFsdWU9IkUtUmV6ZXB0IEZhY2hkaWVuc3QiLz48dHlwZSB2YWx1ZT0idXNlci1mcmllbmRseS1uYW1lIi8+PC9kZXZpY2VOYW1lPjx2ZXJzaW9uPjx2YWx1ZSB2YWx1ZT0iMS45LjAiLz48L3ZlcnNpb24+PGNvbnRhY3Q+PHN5c3RlbSB2YWx1ZT0iZW1haWwiLz48dmFsdWUgdmFsdWU9ImJldHJpZWJAZ2VtYXRpay5kZSIvPjwvY29udGFjdD48L0RldmljZT48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0yMDAuMDAwLjAwMS4yMDYuMTEyLjI5Ii8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iYVlEa2pQb3N3M1NhNWRYNUVtU2dod2hWZzdkOWpob1hIZHdzekVUWFYvOD0iLz48L0JpbmFyeT48L3Jlc291cmNlPjwvZW50cnk+PC9CdW5kbGU+CqCCAtwwggLYMIICf6ADAgECAgJsGTAKBggqhkjOPQQDAjCBhDELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxMjAwBgNVBAsMKUtvbXBvbmVudGVuLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uS09NUC1DQTUwIFRFU1QtT05MWTAeFw0yMzAxMjcxMjExMDdaFw0yODAxMjYxMjExMDZaMF8xCzAJBgNVBAYTAkRFMSIwIAYDVQQKDBlJQk0gVEVTVC1PTkxZIC0gTk9ULVZBTElEMRowGAYDVQQFExEwODc0Ny1SVURFVlNJRzAwNDEQMA4GA1UEAwwHZXJlemVwdDBaMBQGByqGSM49AgEGCSskAwMCCAEBBwNCAAQQC3XXQaZlbTf9GCLTy36zFWI7HsyC4OXoIlxUkjrJ4pIFCbXtEXN1Mn6XYIq8dTmsobgSVvdMAXlHCTc3zb6co4IBAjCB/zAdBgNVHQ4EFgQUPfaaLTi6Z1JB86K/muf1I9pI+ZwwHwYDVR0jBBgwFoAUOuKqJZJOrKmUfc8ZaeoTBrmoMNMwTwYIKwYBBQUHAQEEQzBBMD8GCCsGAQUFBzABhjNodHRwOi8vb2NzcDItdGVzdHJlZi5rb21wLWNhLnRlbGVtYXRpay10ZXN0L29jc3AvZWMwDgYDVR0PAQH/BAQDAgZAMCEGA1UdIAQaMBgwCgYIKoIUAEwEgSMwCgYIKoIUAEwEghswDAYDVR0TAQH/BAIwADArBgUrJAgDAwQiMCAwHjAcMBowGDAKDAhFLVJlemVwdDAKBggqghQATASCAzAKBggqhkjOPQQDAgNHADBEAiA41CnfaU+cHHF3Lqrasg/JGCrQwN/U2CO6yx6y9TqaQQIgHpqLEB8Yb0B7QT/XBCEU3jmSvzNQmw0z6Rpo+kb0/OehggRloYIEYQYIKwYBBQUHEAIwggRTCgEAoIIETDCCBEgGCSsGAQUFBzABAQSCBDkwggQ1MIIBDKFhMF8xCzAJBgNVBAYTAkRFMSYwJAYDVQQKDB1hcnZhdG8gU3lzdGVtcyBHbWJIIE5PVC1WQUxJRDEoMCYGA1UEAwwfS29tcC1QS0kgT0NTUC1TaWduZXI3IFRFU1QtT05MWRgPMjAyMzAyMjMxNDMwMjZaMIGVMIGSMDswCQYFKw4DAhoFAAQUHTzAAgAj+w+MR6YwoL4HbIFYsx8EFDriqiWSTqyplH3PGWnqEwa5qDDTAgJsGYAAGA8yMDIzMDIyMzE0MzAyNlqhQDA+MDwGBSskCAMNBDMwMTANBglghkgBZQMEAgEFAAQg9ei7y+RtvfRiw6uw7EGn1+/Ju00KNBlNNCtctgzjyMYwCgYIKoZIzj0EAwIDRwAwRAIgcCWkrxMzAbqOE/jkQB/MFyqNWxRkDIZ9N6Gg+8cs0zACIHRIbN/VLe2moxsVH05LYboV376yZBpMU5eWjDkzBayToIICzDCCAsgwggLEMIICaqADAgECAgE0MAoGCCqGSM49BAMCMIGEMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJRDEyMDAGA1UECwwpS29tcG9uZW50ZW4tQ0EgZGVyIFRlbGVtYXRpa2luZnJhc3RydWt0dXIxIDAeBgNVBAMMF0dFTS5LT01QLUNBMjggVEVTVC1PTkxZMB4XDTE4MTEyODEzMjQxN1oXDTIzMTEyNzEzMjQxNlowXzELMAkGA1UEBhMCREUxJjAkBgNVBAoMHWFydmF0byBTeXN0ZW1zIEdtYkggTk9ULVZBTElEMSgwJgYDVQQDDB9Lb21wLVBLSSBPQ1NQLVNpZ25lcjcgVEVTVC1PTkxZMFowFAYHKoZIzj0CAQYJKyQDAwIIAQEHA0IABBpVYuIJm++WTsGjB+s8Wlvl9Ug333kb+z6lQET02wIVCh1RVFfsJMLUaTWPaXb1F+Jxn/nox3H42GMYu3bLUfyjge8wgewwHQYDVR0OBBYEFHs1pHWpmxkVyjtzIlsS3E+ePjHQMB8GA1UdIwQYMBaAFABqOJDzma4hj1La7sGMboCtYSLJME8GCCsGAQUFBwEBBEMwQTA/BggrBgEFBQcwAYYzaHR0cDovL29jc3AyLXRlc3RyZWYua29tcC1jYS50ZWxlbWF0aWstdGVzdC9vY3NwL2VjMAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgZAMBMGA1UdJQQMMAoGCCsGAQUFBwMJMBUGA1UdIAQOMAwwCgYIKoIUAEwEgSMwDwYJKwYBBQUHMAEFBAIFADAKBggqhkjOPQQDAgNIADBFAiATnyGgy8+xH9E0Ydw681ygCQvZCVKm+JzVOQ9GnSD+9wIhAI208Rpd6dyQ/sWZArsYMN9otTnXjDiOKEOA/Fvl3W7jMYICsTCCAq0CAQEwgYswgYQxCzAJBgNVBAYTAkRFMR8wHQYDVQQKDBZnZW1hdGlrIEdtYkggTk9ULVZBTElEMTIwMAYDVQQLDClLb21wb25lbnRlbi1DQSBkZXIgVGVsZW1hdGlraW5mcmFzdHJ1a3R1cjEgMB4GA1UEAwwXR0VNLktPTVAtQ0E1MCBURVNULU9OTFkCAmwZMAsGCWCGSAFlAwQCAaCCAbYwGAYJKoZIhvcNAQkDMQsGCSqGSIb3DQEHATAcBgkqhkiG9w0BCQUxDxcNMjMwMjIzMTUwODMyWjAvBgkqhkiG9w0BCQQxIgQgzGNaHL68XydgMDLsbSxSVtaEsSKRYcgwYyL74MlvYJAweQYJKoZIhvcNAQkPMWwwajALBglghkgBZQMEASowCwYJYIZIAWUDBAEWMAsGCWCGSAFlAwQBAjAKBggqhkiG9w0DBzAOBggqhkiG9w0DAgICAIAwDQYIKoZIhvcNAwICAUAwBwYFKw4DAgcwDQYIKoZIhvcNAwICASgwgc8GCyqGSIb3DQEJEAIvMYG/MIG8MIG5MIG2BCD16LvL5G299GLDq7DsQafX78m7TQo0GU00K1y2DOPIxjCBkTCBiqSBhzCBhDELMAkGA1UEBhMCREUxHzAdBgNVBAoMFmdlbWF0aWsgR21iSCBOT1QtVkFMSUQxMjAwBgNVBAsMKUtvbXBvbmVudGVuLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uS09NUC1DQTUwIFRFU1QtT05MWQICbBkwCgYIKoZIzj0EAwIERzBFAiEAhDnazcEzJdRn+/29gJ9cE+vqffiGPK9HhfWgvysu+lQCIAhJWTFTj1p9311EJyfOcYx1wtBw0B2TuFSWNoW9f9ep",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
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
                    assertEquals(LocalDate.parse("2024-11-03").asFhirTemporal(), whenHandedOver)

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
        }
    }

    @Test
    fun `process chargeItem pzn 6 - v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_6_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.280.604.133.110.12", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1BgkqhkiG9w0BBwGgggpmBIIKYjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYzg2MDY3MTItMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDMrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9UYXNrLzIwMC4wMDAuMDAxLjIwNi4xMTIuMjkvJGNsb3NlLyIvPjwvbGluaz48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9InVybjp1dWlkOjFmMTA3YWQ3LWY0YmYtNDdlMy1hNjA2LTAxODFmNWRkNGY3MyIvPjxyZXNvdXJjZT48Q29tcG9zaXRpb24+PGlkIHZhbHVlPSIxZjEwN2FkNy1mNGJmLTQ3ZTMtYTYwNi0wMTgxZjVkZDRmNzMiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9Db21wb3NpdGlvbnwxLjIiLz48L21ldGE+PGV4dGVuc2lvbiB1cmw9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfRVhfQmVuZWZpY2lhcnkiPjx2YWx1ZUlkZW50aWZpZXI+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvc2lkL3RlbGVtYXRpay1pZCIvPjx2YWx1ZSB2YWx1ZT0iMy1TTUMtQi1UZXN0a2FydGUtODgzMTEwMDAwMTE2ODczIi8+PC92YWx1ZUlkZW50aWZpZXI+PC9leHRlbnNpb24+PHN0YXR1cyB2YWx1ZT0iZmluYWwiLz48dHlwZT48Y29kaW5nPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9Db2RlU3lzdGVtL0dFTV9FUlBfQ1NfRG9jdW1lbnRUeXBlIi8+PGNvZGUgdmFsdWU9IjMiLz48ZGlzcGxheSB2YWx1ZT0iUmVjZWlwdCIvPjwvY29kaW5nPjwvdHlwZT48ZGF0ZSB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDIrMDA6MDAiLz48YXV0aG9yPjxyZWZlcmVuY2UgdmFsdWU9Imh0dHBzOi8vZXJwLWRldi56ZW50cmFsLmVycC5zcGxpdGRucy50aS1kaWVuc3RlLmRlL0RldmljZS8xIi8+PC9hdXRob3I+PHRpdGxlIHZhbHVlPSJRdWl0dHVuZyIvPjxldmVudD48cGVyaW9kPjxzdGFydCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODoyOS44NDMrMDA6MDAiLz48ZW5kIHZhbHVlPSIyMDIzLTAyLTIzVDE1OjA4OjMwLjgwMiswMDowMCIvPjwvcGVyaW9kPjwvZXZlbnQ+PHNlY3Rpb24+PGVudHJ5PjxyZWZlcmVuY2UgdmFsdWU9IkJpbmFyeS9QcmVzY3JpcHRpb25EaWdlc3QtMjAwLjAwMC4wMDEuMjA2LjExMi4yOSIvPjwvZW50cnk+PC9zZWN0aW9uPjwvQ29tcG9zaXRpb24+PC9yZXNvdXJjZT48L2VudHJ5PjxlbnRyeT48ZnVsbFVybCB2YWx1ZT0iaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuOS4wIi8+PGRldmljZU5hbWU+PG5hbWUgdmFsdWU9IkUtUmV6ZXB0IEZhY2hkaWVuc3QiLz48dHlwZSB2YWx1ZT0idXNlci1mcmllbmRseS1uYW1lIi8+PC9kZXZpY2VOYW1lPjx2ZXJzaW9uPjx2YWx1ZSB2YWx1ZT0iMS45LjAiLz48L3ZlcnNpb24+PGNvbnRhY3Q+PHN5c3RlbSB2YWx1ZT0iZW1haWwiLz48dmFsdWUgdmFsdWU9ImJldHJpZWJAZ2VtYXRpay5kZSIvPjwvY29udGFjdD48L0RldmljZT48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0yMDAuMDAwLjAwMS4yMDYuMTEyLjI5Ii8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iYVlEa2pQb3N3M1NhNWRYNUVtU2dod2hWZzdkOWpob1hIZHdzekVUWFYvOD0iLz48L0JpbmFyeT48L3Jlc291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",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
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
                    assertEquals(LocalDate.parse("2024-11-03").asFhirTemporal(), whenHandedOver)

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
        }
    }

    @Test
    fun `process chargeItem pzn 7 - v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_7_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.339.908.107.779.64", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1BgkqhkiG9w0BBwGgggpmBIIKYjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYzg2MDY3MTItMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDMrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9UYXNrLzIwMC4wMDAuMDAxLjIwNi4xMTIuMjkvJGNsb3NlLyIvPjwvbGluaz48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9InVybjp1dWlkOjFmMTA3YWQ3LWY0YmYtNDdlMy1hNjA2LTAxODFmNWRkNGY3MyIvPjxyZXNvdXJjZT48Q29tcG9zaXRpb24+PGlkIHZhbHVlPSIxZjEwN2FkNy1mNGJmLTQ3ZTMtYTYwNi0wMTgxZjVkZDRmNzMiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9Db21wb3NpdGlvbnwxLjIiLz48L21ldGE+PGV4dGVuc2lvbiB1cmw9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfRVhfQmVuZWZpY2lhcnkiPjx2YWx1ZUlkZW50aWZpZXI+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvc2lkL3RlbGVtYXRpay1pZCIvPjx2YWx1ZSB2YWx1ZT0iMy1TTUMtQi1UZXN0a2FydGUtODgzMTEwMDAwMTE2ODczIi8+PC92YWx1ZUlkZW50aWZpZXI+PC9leHRlbnNpb24+PHN0YXR1cyB2YWx1ZT0iZmluYWwiLz48dHlwZT48Y29kaW5nPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9Db2RlU3lzdGVtL0dFTV9FUlBfQ1NfRG9jdW1lbnRUeXBlIi8+PGNvZGUgdmFsdWU9IjMiLz48ZGlzcGxheSB2YWx1ZT0iUmVjZWlwdCIvPjwvY29kaW5nPjwvdHlwZT48ZGF0ZSB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDIrMDA6MDAiLz48YXV0aG9yPjxyZWZlcmVuY2UgdmFsdWU9Imh0dHBzOi8vZXJwLWRldi56ZW50cmFsLmVycC5zcGxpdGRucy50aS1kaWVuc3RlLmRlL0RldmljZS8xIi8+PC9hdXRob3I+PHRpdGxlIHZhbHVlPSJRdWl0dHVuZyIvPjxldmVudD48cGVyaW9kPjxzdGFydCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODoyOS44NDMrMDA6MDAiLz48ZW5kIHZhbHVlPSIyMDIzLTAyLTIzVDE1OjA4OjMwLjgwMiswMDowMCIvPjwvcGVyaW9kPjwvZXZlbnQ+PHNlY3Rpb24+PGVudHJ5PjxyZWZlcmVuY2UgdmFsdWU9IkJpbmFyeS9QcmVzY3JpcHRpb25EaWdlc3QtMjAwLjAwMC4wMDEuMjA2LjExMi4yOSIvPjwvZW50cnk+PC9zZWN0aW9uPjwvQ29tcG9zaXRpb24+PC9yZXNvdXJjZT48L2VudHJ5PjxlbnRyeT48ZnVsbFVybCB2YWx1ZT0iaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuOS4wIi8+PGRldmljZU5hbWU+PG5hbWUgdmFsdWU9IkUtUmV6ZXB0IEZhY2hkaWVuc3QiLz48dHlwZSB2YWx1ZT0idXNlci1mcmllbmRseS1uYW1lIi8+PC9kZXZpY2VOYW1lPjx2ZXJzaW9uPjx2YWx1ZSB2YWx1ZT0iMS45LjAiLz48L3ZlcnNpb24+PGNvbnRhY3Q+PHN5c3RlbSB2YWx1ZT0iZW1haWwiLz48dmFsdWUgdmFsdWU9ImJldHJpZWJAZ2VtYXRpay5kZSIvPjwvY29udGFjdD48L0RldmljZT48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0yMDAuMDAwLjAwMS4yMDYuMTEyLjI5Ii8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iYVlEa2pQb3N3M1NhNWRYNUVtU2dod2hWZzdkOWpob1hIZHdzekVUWFYvOD0iLz48L0JpbmFyeT48L3Jlc291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",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(63.84, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("11514676"),
                            "Amoxicillin/Clavulansäure Heumann 875mg/125mg 10St",
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
                    assertEquals(LocalDate.parse("2024-11-03").asFhirTemporal(), whenHandedOver)

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
        }
    }

    @Test
    fun `process chargeItem pzn 14 - v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_14_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->
            assertEquals("200.085.048.660.160.92", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1BgkqhkiG9w0BBwGgggpmBIIKYjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYzg2MDY3MTItMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDMrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9UYXNrLzIwMC4wMDAuMDAxLjIwNi4xMTIuMjkvJGNsb3NlLyIvPjwvbGluaz48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9InVybjp1dWlkOjFmMTA3YWQ3LWY0YmYtNDdlMy1hNjA2LTAxODFmNWRkNGY3MyIvPjxyZXNvdXJjZT48Q29tcG9zaXRpb24+PGlkIHZhbHVlPSIxZjEwN2FkNy1mNGJmLTQ3ZTMtYTYwNi0wMTgxZjVkZDRmNzMiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9Db21wb3NpdGlvbnwxLjIiLz48L21ldGE+PGV4dGVuc2lvbiB1cmw9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfRVhfQmVuZWZpY2lhcnkiPjx2YWx1ZUlkZW50aWZpZXI+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvc2lkL3RlbGVtYXRpay1pZCIvPjx2YWx1ZSB2YWx1ZT0iMy1TTUMtQi1UZXN0a2FydGUtODgzMTEwMDAwMTE2ODczIi8+PC92YWx1ZUlkZW50aWZpZXI+PC9leHRlbnNpb24+PHN0YXR1cyB2YWx1ZT0iZmluYWwiLz48dHlwZT48Y29kaW5nPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9Db2RlU3lzdGVtL0dFTV9FUlBfQ1NfRG9jdW1lbnRUeXBlIi8+PGNvZGUgdmFsdWU9IjMiLz48ZGlzcGxheSB2YWx1ZT0iUmVjZWlwdCIvPjwvY29kaW5nPjwvdHlwZT48ZGF0ZSB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDIrMDA6MDAiLz48YXV0aG9yPjxyZWZlcmVuY2UgdmFsdWU9Imh0dHBzOi8vZXJwLWRldi56ZW50cmFsLmVycC5zcGxpdGRucy50aS1kaWVuc3RlLmRlL0RldmljZS8xIi8+PC9hdXRob3I+PHRpdGxlIHZhbHVlPSJRdWl0dHVuZyIvPjxldmVudD48cGVyaW9kPjxzdGFydCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODoyOS44NDMrMDA6MDAiLz48ZW5kIHZhbHVlPSIyMDIzLTAyLTIzVDE1OjA4OjMwLjgwMiswMDowMCIvPjwvcGVyaW9kPjwvZXZlbnQ+PHNlY3Rpb24+PGVudHJ5PjxyZWZlcmVuY2UgdmFsdWU9IkJpbmFyeS9QcmVzY3JpcHRpb25EaWdlc3QtMjAwLjAwMC4wMDEuMjA2LjExMi4yOSIvPjwvZW50cnk+PC9zZWN0aW9uPjwvQ29tcG9zaXRpb24+PC9yZXNvdXJjZT48L2VudHJ5PjxlbnRyeT48ZnVsbFVybCB2YWx1ZT0iaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuOS4wIi8+PGRldmljZU5hbWU+PG5hbWUgdmFsdWU9IkUtUmV6ZXB0IEZhY2hkaWVuc3QiLz48dHlwZSB2YWx1ZT0idXNlci1mcmllbmRseS1uYW1lIi8+PC9kZXZpY2VOYW1lPjx2ZXJzaW9uPjx2YWx1ZSB2YWx1ZT0iMS45LjAiLz48L3ZlcnNpb24+PGNvbnRhY3Q+PHN5c3RlbSB2YWx1ZT0iZW1haWwiLz48dmFsdWUgdmFsdWU9ImJldHJpZWJAZ2VtYXRpay5kZSIvPjwvY29udGFjdD48L0RldmljZT48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0yMDAuMDAwLjAwMS4yMDYuMTEyLjI5Ii8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iYVlEa2pQb3N3M1NhNWRYNUVtU2dod2hWZzdkOWpob1hIZHdzekVUWFYvOD0iLz48L0JpbmFyeT48L3Jlc291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",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(32.56, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("16598608"),
                            "Azithromycin Heumann 500 mg Filmtabletten N1",
                            2.0,
                            InvoiceData.PriceComponent(31.96, 19.0)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.TA1("17717446"),
                            "Lieferengpasspauschale",
                            1.0,
                            InvoiceData.PriceComponent(0.6, 19.0)
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
                    assertEquals(LocalDate.parse("2024-11-03").asFhirTemporal(), whenHandedOver)

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
                    assertEquals("200.085.048.660.160.92", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        }
    }

    @Test
    fun `process chargeItem pzn 15 - v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_15_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->
            assertEquals("200.385.450.404.964.44", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1BgkqhkiG9w0BBwGgggpmBIIKYjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYzg2MDY3MTItMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDMrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9UYXNrLzIwMC4wMDAuMDAxLjIwNi4xMTIuMjkvJGNsb3NlLyIvPjwvbGluaz48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9InVybjp1dWlkOjFmMTA3YWQ3LWY0YmYtNDdlMy1hNjA2LTAxODFmNWRkNGY3MyIvPjxyZXNvdXJjZT48Q29tcG9zaXRpb24+PGlkIHZhbHVlPSIxZjEwN2FkNy1mNGJmLTQ3ZTMtYTYwNi0wMTgxZjVkZDRmNzMiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9Db21wb3NpdGlvbnwxLjIiLz48L21ldGE+PGV4dGVuc2lvbiB1cmw9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfRVhfQmVuZWZpY2lhcnkiPjx2YWx1ZUlkZW50aWZpZXI+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvc2lkL3RlbGVtYXRpay1pZCIvPjx2YWx1ZSB2YWx1ZT0iMy1TTUMtQi1UZXN0a2FydGUtODgzMTEwMDAwMTE2ODczIi8+PC92YWx1ZUlkZW50aWZpZXI+PC9leHRlbnNpb24+PHN0YXR1cyB2YWx1ZT0iZmluYWwiLz48dHlwZT48Y29kaW5nPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9Db2RlU3lzdGVtL0dFTV9FUlBfQ1NfRG9jdW1lbnRUeXBlIi8+PGNvZGUgdmFsdWU9IjMiLz48ZGlzcGxheSB2YWx1ZT0iUmVjZWlwdCIvPjwvY29kaW5nPjwvdHlwZT48ZGF0ZSB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDIrMDA6MDAiLz48YXV0aG9yPjxyZWZlcmVuY2UgdmFsdWU9Imh0dHBzOi8vZXJwLWRldi56ZW50cmFsLmVycC5zcGxpdGRucy50aS1kaWVuc3RlLmRlL0RldmljZS8xIi8+PC9hdXRob3I+PHRpdGxlIHZhbHVlPSJRdWl0dHVuZyIvPjxldmVudD48cGVyaW9kPjxzdGFydCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODoyOS44NDMrMDA6MDAiLz48ZW5kIHZhbHVlPSIyMDIzLTAyLTIzVDE1OjA4OjMwLjgwMiswMDowMCIvPjwvcGVyaW9kPjwvZXZlbnQ+PHNlY3Rpb24+PGVudHJ5PjxyZWZlcmVuY2UgdmFsdWU9IkJpbmFyeS9QcmVzY3JpcHRpb25EaWdlc3QtMjAwLjAwMC4wMDEuMjA2LjExMi4yOSIvPjwvZW50cnk+PC9zZWN0aW9uPjwvQ29tcG9zaXRpb24+PC9yZXNvdXJjZT48L2VudHJ5PjxlbnRyeT48ZnVsbFVybCB2YWx1ZT0iaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuOS4wIi8+PGRldmljZU5hbWU+PG5hbWUgdmFsdWU9IkUtUmV6ZXB0IEZhY2hkaWVuc3QiLz48dHlwZSB2YWx1ZT0idXNlci1mcmllbmRseS1uYW1lIi8+PC9kZXZpY2VOYW1lPjx2ZXJzaW9uPjx2YWx1ZSB2YWx1ZT0iMS45LjAiLz48L3ZlcnNpb24+PGNvbnRhY3Q+PHN5c3RlbSB2YWx1ZT0iZW1haWwiLz48dmFsdWUgdmFsdWU9ImJldHJpZWJAZ2VtYXRpay5kZSIvPjwvY29udGFjdD48L0RldmljZT48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0yMDAuMDAwLjAwMS4yMDYuMTEyLjI5Ii8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iYVlEa2pQb3N3M1NhNWRYNUVtU2dod2hWZzdkOWpob1hIZHdzekVUWFYvOD0iLz48L0JpbmFyeT48L3Jlc291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",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(31.34, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("04351707"),
                            "Benazepril AL 10mg 98 Filmtabletten N3",
                            2.0,
                            InvoiceData.PriceComponent(30.74, 19.0)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.TA1("17717446"),
                            "Lieferengpasspauschale",
                            1.0,
                            InvoiceData.PriceComponent(0.6, 19.0)
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
                    assertEquals(LocalDate.parse("2024-11-03").asFhirTemporal(), whenHandedOver)

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
                    assertEquals("200.385.450.404.964.44", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        }
    }

    @Test
    fun `process chargeItem pzn 16 - v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_16_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.226.167.794.658.56", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1BgkqhkiG9w0BBwGgggpmBIIKYjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYzg2MDY3MTItMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDMrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9UYXNrLzIwMC4wMDAuMDAxLjIwNi4xMTIuMjkvJGNsb3NlLyIvPjwvbGluaz48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9InVybjp1dWlkOjFmMTA3YWQ3LWY0YmYtNDdlMy1hNjA2LTAxODFmNWRkNGY3MyIvPjxyZXNvdXJjZT48Q29tcG9zaXRpb24+PGlkIHZhbHVlPSIxZjEwN2FkNy1mNGJmLTQ3ZTMtYTYwNi0wMTgxZjVkZDRmNzMiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9Db21wb3NpdGlvbnwxLjIiLz48L21ldGE+PGV4dGVuc2lvbiB1cmw9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfRVhfQmVuZWZpY2lhcnkiPjx2YWx1ZUlkZW50aWZpZXI+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvc2lkL3RlbGVtYXRpay1pZCIvPjx2YWx1ZSB2YWx1ZT0iMy1TTUMtQi1UZXN0a2FydGUtODgzMTEwMDAwMTE2ODczIi8+PC92YWx1ZUlkZW50aWZpZXI+PC9leHRlbnNpb24+PHN0YXR1cyB2YWx1ZT0iZmluYWwiLz48dHlwZT48Y29kaW5nPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9Db2RlU3lzdGVtL0dFTV9FUlBfQ1NfRG9jdW1lbnRUeXBlIi8+PGNvZGUgdmFsdWU9IjMiLz48ZGlzcGxheSB2YWx1ZT0iUmVjZWlwdCIvPjwvY29kaW5nPjwvdHlwZT48ZGF0ZSB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDIrMDA6MDAiLz48YXV0aG9yPjxyZWZlcmVuY2UgdmFsdWU9Imh0dHBzOi8vZXJwLWRldi56ZW50cmFsLmVycC5zcGxpdGRucy50aS1kaWVuc3RlLmRlL0RldmljZS8xIi8+PC9hdXRob3I+PHRpdGxlIHZhbHVlPSJRdWl0dHVuZyIvPjxldmVudD48cGVyaW9kPjxzdGFydCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODoyOS44NDMrMDA6MDAiLz48ZW5kIHZhbHVlPSIyMDIzLTAyLTIzVDE1OjA4OjMwLjgwMiswMDowMCIvPjwvcGVyaW9kPjwvZXZlbnQ+PHNlY3Rpb24+PGVudHJ5PjxyZWZlcmVuY2UgdmFsdWU9IkJpbmFyeS9QcmVzY3JpcHRpb25EaWdlc3QtMjAwLjAwMC4wMDEuMjA2LjExMi4yOSIvPjwvZW50cnk+PC9zZWN0aW9uPjwvQ29tcG9zaXRpb24+PC9yZXNvdXJjZT48L2VudHJ5PjxlbnRyeT48ZnVsbFVybCB2YWx1ZT0iaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuOS4wIi8+PGRldmljZU5hbWU+PG5hbWUgdmFsdWU9IkUtUmV6ZXB0IEZhY2hkaWVuc3QiLz48dHlwZSB2YWx1ZT0idXNlci1mcmllbmRseS1uYW1lIi8+PC9kZXZpY2VOYW1lPjx2ZXJzaW9uPjx2YWx1ZSB2YWx1ZT0iMS45LjAiLz48L3ZlcnNpb24+PGNvbnRhY3Q+PHN5c3RlbSB2YWx1ZT0iZW1haWwiLz48dmFsdWUgdmFsdWU9ImJldHJpZWJAZ2VtYXRpay5kZSIvPjwvY29udGFjdD48L0RldmljZT48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0yMDAuMDAwLjAwMS4yMDYuMTEyLjI5Ii8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iYVlEa2pQb3N3M1NhNWRYNUVtU2dod2hWZzdkOWpob1hIZHdzekVUWFYvOD0iLz48L0JpbmFyeT48L3Jlc291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",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(17.05, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("03852301"),
                            "Tamoxifen AL 20 Tabletten N1",
                            1.0,
                            InvoiceData.PriceComponent(16.45, 19.0),
                            partialQuantityDelivery = true,
                            spenderPzn = "03852318"
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.PZN).isSpecialPZN()
                    )

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.TA1("17717446"),
                            "Lieferengpasspauschale",
                            1.0,
                            InvoiceData.PriceComponent(0.60, 19.0)
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
                    assertEquals(LocalDate.parse("2024-11-03").asFhirTemporal(), whenHandedOver)

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
                    assertEquals("200.226.167.794.658.56", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        }
    }

    @Test
    fun `process chargeItem pzn 18 - v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_pzn_18_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.357.872.211.630.88", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1BgkqhkiG9w0BBwGgggpmBIIKYjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYzg2MDY3MTItMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDMrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9UYXNrLzIwMC4wMDAuMDAxLjIwNi4xMTIuMjkvJGNsb3NlLyIvPjwvbGluaz48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9InVybjp1dWlkOjFmMTA3YWQ3LWY0YmYtNDdlMy1hNjA2LTAxODFmNWRkNGY3MyIvPjxyZXNvdXJjZT48Q29tcG9zaXRpb24+PGlkIHZhbHVlPSIxZjEwN2FkNy1mNGJmLTQ3ZTMtYTYwNi0wMTgxZjVkZDRmNzMiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9Db21wb3NpdGlvbnwxLjIiLz48L21ldGE+PGV4dGVuc2lvbiB1cmw9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfRVhfQmVuZWZpY2lhcnkiPjx2YWx1ZUlkZW50aWZpZXI+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvc2lkL3RlbGVtYXRpay1pZCIvPjx2YWx1ZSB2YWx1ZT0iMy1TTUMtQi1UZXN0a2FydGUtODgzMTEwMDAwMTE2ODczIi8+PC92YWx1ZUlkZW50aWZpZXI+PC9leHRlbnNpb24+PHN0YXR1cyB2YWx1ZT0iZmluYWwiLz48dHlwZT48Y29kaW5nPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9Db2RlU3lzdGVtL0dFTV9FUlBfQ1NfRG9jdW1lbnRUeXBlIi8+PGNvZGUgdmFsdWU9IjMiLz48ZGlzcGxheSB2YWx1ZT0iUmVjZWlwdCIvPjwvY29kaW5nPjwvdHlwZT48ZGF0ZSB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDIrMDA6MDAiLz48YXV0aG9yPjxyZWZlcmVuY2UgdmFsdWU9Imh0dHBzOi8vZXJwLWRldi56ZW50cmFsLmVycC5zcGxpdGRucy50aS1kaWVuc3RlLmRlL0RldmljZS8xIi8+PC9hdXRob3I+PHRpdGxlIHZhbHVlPSJRdWl0dHVuZyIvPjxldmVudD48cGVyaW9kPjxzdGFydCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODoyOS44NDMrMDA6MDAiLz48ZW5kIHZhbHVlPSIyMDIzLTAyLTIzVDE1OjA4OjMwLjgwMiswMDowMCIvPjwvcGVyaW9kPjwvZXZlbnQ+PHNlY3Rpb24+PGVudHJ5PjxyZWZlcmVuY2UgdmFsdWU9IkJpbmFyeS9QcmVzY3JpcHRpb25EaWdlc3QtMjAwLjAwMC4wMDEuMjA2LjExMi4yOSIvPjwvZW50cnk+PC9zZWN0aW9uPjwvQ29tcG9zaXRpb24+PC9yZXNvdXJjZT48L2VudHJ5PjxlbnRyeT48ZnVsbFVybCB2YWx1ZT0iaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuOS4wIi8+PGRldmljZU5hbWU+PG5hbWUgdmFsdWU9IkUtUmV6ZXB0IEZhY2hkaWVuc3QiLz48dHlwZSB2YWx1ZT0idXNlci1mcmllbmRseS1uYW1lIi8+PC9kZXZpY2VOYW1lPjx2ZXJzaW9uPjx2YWx1ZSB2YWx1ZT0iMS45LjAiLz48L3ZlcnNpb24+PGNvbnRhY3Q+PHN5c3RlbSB2YWx1ZT0iZW1haWwiLz48dmFsdWUgdmFsdWU9ImJldHJpZWJAZ2VtYXRpay5kZSIvPjwvY29udGFjdD48L0RldmljZT48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0yMDAuMDAwLjAwMS4yMDYuMTEyLjI5Ii8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iYVlEa2pQb3N3M1NhNWRYNUVtU2dod2hWZzdkOWpob1hIZHdzekVUWFYvOD0iLz48L0JpbmFyeT48L3Jlc291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",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(12.60, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.PZN("17550650"),
                            "COTRIM-ratiopharm 400 mg/80 mg Tabletten N2",
                            1.0,
                            InvoiceData.PriceComponent(12.60, 19.0)
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
                    assertEquals(LocalDate.parse("2024-11-03").asFhirTemporal(), whenHandedOver)

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
                    assertEquals("200.357.872.211.630.88", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        }
    }

    @Test
    fun `process chargeItem freetext - v1_3`() {
        val bundle = Json.parseToJsonElement(chargeItem_freetext_v1_3)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->
            assertEquals("200.334.138.469.717.92", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

            val erpBinary = extractBinary(erpPrBundle)
            val invoiceBinary = extractBinary(invoiceBundle)
            val kbvBinary = extractBinary(kbvBundle)

            assertEquals(
                "MIIUnAYJKoZIhvcNAQcCoIIUjTCCFIkCAQUxDTALBglghkgBZQMEAgEwggp1BgkqhkiG9w0BBwGgggpmBIIKYjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9InV0Zi04Ij8+CjxCdW5kbGUgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPjxpZCB2YWx1ZT0iYzg2MDY3MTItMDAwMC0wMDAwLTAwMDMtMDAwMDAwMDAwMDAwIi8+PG1ldGE+PHByb2ZpbGUgdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfUFJfQnVuZGxlfDEuMiIvPjwvbWV0YT48aWRlbnRpZmllcj48c3lzdGVtIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvTmFtaW5nU3lzdGVtL0dFTV9FUlBfTlNfUHJlc2NyaXB0aW9uSWQiLz48dmFsdWUgdmFsdWU9IjIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48L2lkZW50aWZpZXI+PHR5cGUgdmFsdWU9ImRvY3VtZW50Ii8+PHRpbWVzdGFtcCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDMrMDA6MDAiLz48bGluaz48cmVsYXRpb24gdmFsdWU9InNlbGYiLz48dXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9UYXNrLzIwMC4wMDAuMDAxLjIwNi4xMTIuMjkvJGNsb3NlLyIvPjwvbGluaz48ZW50cnk+PGZ1bGxVcmwgdmFsdWU9InVybjp1dWlkOjFmMTA3YWQ3LWY0YmYtNDdlMy1hNjA2LTAxODFmNWRkNGY3MyIvPjxyZXNvdXJjZT48Q29tcG9zaXRpb24+PGlkIHZhbHVlPSIxZjEwN2FkNy1mNGJmLTQ3ZTMtYTYwNi0wMTgxZjVkZDRmNzMiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9Db21wb3NpdGlvbnwxLjIiLz48L21ldGE+PGV4dGVuc2lvbiB1cmw9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9TdHJ1Y3R1cmVEZWZpbml0aW9uL0dFTV9FUlBfRVhfQmVuZWZpY2lhcnkiPjx2YWx1ZUlkZW50aWZpZXI+PHN5c3RlbSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvc2lkL3RlbGVtYXRpay1pZCIvPjx2YWx1ZSB2YWx1ZT0iMy1TTUMtQi1UZXN0a2FydGUtODgzMTEwMDAwMTE2ODczIi8+PC92YWx1ZUlkZW50aWZpZXI+PC9leHRlbnNpb24+PHN0YXR1cyB2YWx1ZT0iZmluYWwiLz48dHlwZT48Y29kaW5nPjxzeXN0ZW0gdmFsdWU9Imh0dHBzOi8vZ2VtYXRpay5kZS9maGlyL2VycC9Db2RlU3lzdGVtL0dFTV9FUlBfQ1NfRG9jdW1lbnRUeXBlIi8+PGNvZGUgdmFsdWU9IjMiLz48ZGlzcGxheSB2YWx1ZT0iUmVjZWlwdCIvPjwvY29kaW5nPjwvdHlwZT48ZGF0ZSB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODozMC44MDIrMDA6MDAiLz48YXV0aG9yPjxyZWZlcmVuY2UgdmFsdWU9Imh0dHBzOi8vZXJwLWRldi56ZW50cmFsLmVycC5zcGxpdGRucy50aS1kaWVuc3RlLmRlL0RldmljZS8xIi8+PC9hdXRob3I+PHRpdGxlIHZhbHVlPSJRdWl0dHVuZyIvPjxldmVudD48cGVyaW9kPjxzdGFydCB2YWx1ZT0iMjAyMy0wMi0yM1QxNTowODoyOS44NDMrMDA6MDAiLz48ZW5kIHZhbHVlPSIyMDIzLTAyLTIzVDE1OjA4OjMwLjgwMiswMDowMCIvPjwvcGVyaW9kPjwvZXZlbnQ+PHNlY3Rpb24+PGVudHJ5PjxyZWZlcmVuY2UgdmFsdWU9IkJpbmFyeS9QcmVzY3JpcHRpb25EaWdlc3QtMjAwLjAwMC4wMDEuMjA2LjExMi4yOSIvPjwvZW50cnk+PC9zZWN0aW9uPjwvQ29tcG9zaXRpb24+PC9yZXNvdXJjZT48L2VudHJ5PjxlbnRyeT48ZnVsbFVybCB2YWx1ZT0iaHR0cHM6Ly9lcnAtZGV2LnplbnRyYWwuZXJwLnNwbGl0ZG5zLnRpLWRpZW5zdGUuZGUvRGV2aWNlLzEiLz48cmVzb3VyY2U+PERldmljZT48aWQgdmFsdWU9IjEiLz48bWV0YT48cHJvZmlsZSB2YWx1ZT0iaHR0cHM6Ly9nZW1hdGlrLmRlL2ZoaXIvZXJwL1N0cnVjdHVyZURlZmluaXRpb24vR0VNX0VSUF9QUl9EZXZpY2V8MS4yIi8+PC9tZXRhPjxzdGF0dXMgdmFsdWU9ImFjdGl2ZSIvPjxzZXJpYWxOdW1iZXIgdmFsdWU9IjEuOS4wIi8+PGRldmljZU5hbWU+PG5hbWUgdmFsdWU9IkUtUmV6ZXB0IEZhY2hkaWVuc3QiLz48dHlwZSB2YWx1ZT0idXNlci1mcmllbmRseS1uYW1lIi8+PC9kZXZpY2VOYW1lPjx2ZXJzaW9uPjx2YWx1ZSB2YWx1ZT0iMS45LjAiLz48L3ZlcnNpb24+PGNvbnRhY3Q+PHN5c3RlbSB2YWx1ZT0iZW1haWwiLz48dmFsdWUgdmFsdWU9ImJldHJpZWJAZ2VtYXRpay5kZSIvPjwvY29udGFjdD48L0RldmljZT48L3Jlc291cmNlPjwvZW50cnk+PGVudHJ5PjxmdWxsVXJsIHZhbHVlPSJodHRwczovL2VycC1kZXYuemVudHJhbC5lcnAuc3BsaXRkbnMudGktZGllbnN0ZS5kZS9CaW5hcnkvUHJlc2NyaXB0aW9uRGlnZXN0LTIwMC4wMDAuMDAxLjIwNi4xMTIuMjkiLz48cmVzb3VyY2U+PEJpbmFyeT48aWQgdmFsdWU9IlByZXNjcmlwdGlvbkRpZ2VzdC0yMDAuMDAwLjAwMS4yMDYuMTEyLjI5Ii8+PG1ldGE+PHZlcnNpb25JZCB2YWx1ZT0iMSIvPjxwcm9maWxlIHZhbHVlPSJodHRwczovL2dlbWF0aWsuZGUvZmhpci9lcnAvU3RydWN0dXJlRGVmaW5pdGlvbi9HRU1fRVJQX1BSX0RpZ2VzdHwxLjIiLz48L21ldGE+PGNvbnRlbnRUeXBlIHZhbHVlPSJhcHBsaWNhdGlvbi9vY3RldC1zdHJlYW0iLz48ZGF0YSB2YWx1ZT0iYVlEa2pQb3N3M1NhNWRYNUVtU2dod2hWZzdkOWpob1hIZHdzekVUWFYvOD0iLz48L0JpbmFyeT48L3Jlc291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",
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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, additionalInfo ->
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
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.TA1).isSpecialPZN()
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
                    assertEquals(
                        true,
                        (items[1].description as InvoiceData.ChargeableItem.Description.TA1).isSpecialPZN()
                    )

                    PKVReturnType.Invoice
                },
                processDispense = { whenHandedOver ->
                    assertEquals(LocalDate.parse("2024-11-07").asFhirTemporal(), whenHandedOver)

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
        }
    }

    @Test
    fun `process chargeItem compounding`() {
        val bundle = Json.parseToJsonElement(chargeItem_compounding)

        extractInvoiceKBVAndErpPrBundle(bundle) { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.858.310.624.061.76", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, additionalInfo ->
                    assertEquals(0.0, totalAdditionalFee)
                    assertEquals(31.7, totalBruttoAmount)
                    assertEquals("EUR", currency)

                    assertEquals(
                        InvoiceData.ChargeableItem(
                            InvoiceData.ChargeableItem.Description.TA1("09999011"),
                            "Rezeptur",
                            1.0,
                            InvoiceData.PriceComponent(31.7, 19.0)
                        ),
                        items[0]
                    )
                    assertEquals(
                        false,
                        (items[0].description as InvoiceData.ChargeableItem.Description.TA1).isSpecialPZN()
                    )

                    assertEquals(
                        listOf(
                            "Bestandteile: 03110083 0.4328 5.84 / 01096858 0.39956 5.5 / " +
                                "00538343 1.0 0.95 / 06460518 1.0 6.0 / 06460518 1.0 8.35  (Nettopreise)"
                        ),
                        additionalInfo
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
                    assertEquals("200.858.310.624.061.76", taskId)
                    assertEquals(PKVReturnType.Pharmacy, pharmacy)
                    assertEquals(PKVReturnType.Invoice, invoice)
                    assertEquals(PKVReturnType.Dispense, dispense)

                    PKVReturnType.InvoiceBundle
                }
            )
        }
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

        extractInvoiceKBVAndErpPrBundle(bundle, process = { taskId, accessCode, invoiceBundle, kbvBundle, erpPrBundle ->

            assertEquals("200.334.138.469.717.92", taskId)
            assertEquals("abd4afed9f3f458114fc3407878213e110f238d1afa919fbed7282abbef68bfd", accessCode)

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
                processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items, _, _ ->
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
