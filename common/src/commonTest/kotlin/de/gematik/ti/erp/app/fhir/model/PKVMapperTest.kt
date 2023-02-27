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

enum class PKVReturnType {
    InvoiceBundle, Invoice, Pharmacy, PharmacyAddress, Dispense
}

class PKVMapperTest {
    @Test
    fun `process pkv bundle version 1_1`() {
        val bundle = Json.parseToJsonElement(pkvAbgabedatenJson_vers_1_1)
        val result = extractPKVInvoiceBundle(
            bundle,
            processInvoice = { totalAdditionalFee, totalBruttoAmount, currency, items ->
                assertEquals(0.0, totalAdditionalFee)
                assertEquals(51.48, totalBruttoAmount)
                assertEquals("EUR", currency)

                assertEquals(
                    ChargeableItem(ChargeableItem.Description.PZN("11514676"), 2.0, PriceComponent(48.98, 19.0)),
                    items[0]
                )
                assertEquals(false, (items[0].description as ChargeableItem.Description.PZN).isSpecialPZN())

                assertEquals(
                    ChargeableItem(ChargeableItem.Description.PZN("02567018"), 1.0, PriceComponent(2.50, 19.0)),
                    items[1]
                )
                assertEquals(true, (items[1].description as ChargeableItem.Description.PZN).isSpecialPZN())

                PKVReturnType.Invoice
            },
            processDispense = { whenHandedOver ->
                assertEquals(LocalDate.parse("2022-03-25").asFhirTemporal(), whenHandedOver)

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
                assertEquals("123456789", iknr)
                assertEquals(null, phone)
                assertEquals(null, mail)

                PKVReturnType.Pharmacy
            },
            save = { pharmacy, invoice, dispense ->
                assertEquals(PKVReturnType.Pharmacy, pharmacy)
                assertEquals(PKVReturnType.Invoice, invoice)
                assertEquals(PKVReturnType.Dispense, dispense)

                PKVReturnType.InvoiceBundle
            }
        )

        assertEquals(PKVReturnType.InvoiceBundle, result)
    }
}
