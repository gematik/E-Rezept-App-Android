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

package de.gematik.ti.erp.app.fhir.pkv.parser

import de.gematik.ti.erp.app.data.Bundle_abgabedaten_1_4_ad80703d_8c62_44a3_b12b_2ea66eda0aa2_12164329
import de.gematik.ti.erp.app.data.Bundle_abgabedaten_1_4_f548dde3_a319_486b_8624_6176ff41ad90_4508766
import de.gematik.ti.erp.app.data.Bundle_abgabendaten_1_5_72bd741c_7ad8_41d8_97c3_9aabbdd0f5b4_10483995
import de.gematik.ti.erp.app.data.Bundle_abgabendaten_1_5_edd55212_965f_4018_a287_6b08e7f5c53c_56751409
import de.gematik.ti.erp.app.data.Bundle_abgabendaten_1_5_fe4a04af_0828_4977_a5ce_bfeed16ebf10_58406714
import de.gematik.ti.erp.app.data.pkvChargeItem_14_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_15_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_16_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_17_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_18_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_1_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_1_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_2_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_2_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_3_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_3_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_5_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_5_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_6_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_6_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_7_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_7_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_8_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_8_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_1_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_1_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_2_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_2_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_3_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_3_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_4_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_4_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_Rezeptur_Verordnung_Nr_1_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_Rezeptur_Verordnung_Nr_2_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_Rezeptur_parenterale_Zytostatika_Rezeptur_parenterale_Zytostatika_1_V_1_3_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_Rezeptur_parenterale_Zytostatika_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_Wirkstoff_Verordnung_V_1_2_Bundle
import de.gematik.ti.erp.app.data.pkvChargeItem_freitext_V_1_3_Bundle
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.basic_1_2_taskData
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.erpmodelInvoice_v1_4
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.erpmodelInvoice_v1_4_example2
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvChargeItem_Einzelimport_Yellox_V_1_3
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_1_V_1_3
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvChargeItem_Rezeptur_Ramipril_V_1_3
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvChargeItem_Rezeptur_parenterale_Zytostatika_1_V_1_3_invoice
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvChargeItems_2_V_1_2_Collection
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvChargeItems_3_V_1_2_Collection
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvChargeItems_5_V_1_2_Collection
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvChargeItems_6_V_1_2_Collection
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvChargeItems_7_V_1_2_Collection
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvChargeItems_8_V_1_2_Collection
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_Amoxiclav_Noctu
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_Azithromycin
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_BelocZok
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_Benazepril
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_Concor10Plus
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_Cotrim
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_Doxycyclin
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_EflueldaTetra
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_InfectoCortiKrupp_Noctu
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_Tamoxifen
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_Venlafaxin
import de.gematik.ti.erp.app.fhir.pkv.mocks.ChargeItemBundleParserMocks.pkvInvoice_Viani50ug250ug
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceBundle.Companion.getPkvInvoiceBundle
import de.gematik.ti.erp.app.fhir.pkv.model.original.FhirPkvInvoiceBundle.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ChargeItemBundleParserTest {

    private val parser = ChargeItemBundleParser()

    @Test
    fun `parse charge item bundle version 1_2 with multiple charge items`() = runTest {
        val bundle = Json.parseToJsonElement(pkvChargeItem_1_V_1_2_Bundle)
        val result = parser.extract(bundle)
        assertEquals(2, result.chargeItems.size)
        val firstChargeItem = result.chargeItems.first()
        // dispense check
        assertEquals(
            FhirTemporal.LocalDate(LocalDate.parse("2023-07-03")),
            firstChargeItem.medicationDispenseErpModel?.whenHandedOver
        )
        // Quittung check
        assertArrayEquals(
            byteArrayOf(
                77, 73, 73, 85, 110, 65, 89, 74, 75, 111, 90, 73, 104, 118, 99, 78,
                65, 81, 99, 67, 111, 73, 73, 85, 106, 84, 67, 67, 70, 73, 107, 67,
                65, 81, 85, 120, 68, 84, 65, 76, 66, 103, 108, 103, 104, 107, 103,
                66, 90, 81, 77
            ),
            firstChargeItem.invoiceBinaryErpModel?.binary
        )
        // invoice check
        val firstChargeItemInvoice = firstChargeItem.invoiceErpModel
        assertEquals("200.424.187.927.272.20", firstChargeItemInvoice?.taskId)
        assertEquals("Adler-Apotheke", firstChargeItemInvoice?.organization?.name)
        assertEquals("Taunusstraße", firstChargeItemInvoice?.organization?.address?.streetName)
        assertEquals("89", firstChargeItemInvoice?.organization?.address?.houseNumber)
        assertEquals("63225", firstChargeItemInvoice?.organization?.address?.postalCode)
        assertEquals("Langen", firstChargeItemInvoice?.organization?.address?.city)
        assertEquals("308412345", firstChargeItemInvoice?.organization?.iknr)
        // price check
        val firstChargeItemInvoicePriceInfo = firstChargeItem.invoiceErpModel?.lineItems?.first()
        assertEquals("21.04", firstChargeItemInvoicePriceInfo?.price)
        assertEquals("19", firstChargeItemInvoicePriceInfo?.tax)
        assertEquals("1", firstChargeItemInvoicePriceInfo?.factor)
        assertEquals(false, firstChargeItemInvoicePriceInfo?.isPartialQuantityDelivery)
        assertEquals(null, firstChargeItemInvoicePriceInfo?.spenderPzn)
        assertEquals("03879429", firstChargeItemInvoicePriceInfo?.chargeItemCode?.code)
        assertEquals(
            "BELOC-ZOK mite 47,5 mg Retardtabletten 30 St",
            firstChargeItemInvoicePriceInfo?.chargeItemCode?.text
        )
        // Abrechnung check
        assertArrayEquals(
            byteArrayOf(
                89, 50, 82, 106, 77, 84, 86, 106, 78, 84, 104, 107, 77, 122,
                108, 107, 77, 106, 108, 108, 78, 68, 100, 106, 77, 84, 107,
                49, 77, 106, 73, 122, 78, 68, 108, 107, 79, 68, 82, 106, 77,
                84, 104, 105, 78, 84, 108, 105, 89, 84, 90, 107, 77, 71, 70,
                104, 90, 109, 73, 53, 78, 71, 89, 121, 90, 106, 77, 50, 78,
                68, 70, 107, 78, 71, 74, 105, 90, 84, 107, 49, 79, 68, 104, 105, 77, 81, 61, 61
            ),
            firstChargeItemInvoice?.binary
        )
        assertEquals(basic_1_2_taskData, firstChargeItem.kbvDataErpModel)

        val secondChargeItem = result.chargeItems[1]

        assertEquals(
            FhirTemporal.LocalDate(LocalDate.parse("2023-07-03")),
            secondChargeItem.medicationDispenseErpModel?.whenHandedOver
        )
    }

    @Test
    fun `parse charge item bundle version 1_2 multiple medications`() {
        val bundle = Json.parseToJsonElement(pkvChargeItem_7_V_1_2_Bundle)
        val result = parser.extract(bundle)
        assertEquals(pkvChargeItems_7_V_1_2_Collection, result)
    }

    @Test
    fun `parse charge item bundle version 1_2 additional information`() {
        val bundle = Json.parseToJsonElement(pkvChargeItem_8_V_1_2_Bundle)
        val result = parser.extract(bundle)
        assertEquals(pkvChargeItems_8_V_1_2_Collection, result)
    }

    @Test
    fun `parse charge item bundle version 1_2`() = runTest {
        val bundle2 = Json.parseToJsonElement(pkvChargeItem_2_V_1_2_Bundle)
        val bundle3 = Json.parseToJsonElement(pkvChargeItem_3_V_1_2_Bundle)
        val bundle5 = Json.parseToJsonElement(pkvChargeItem_5_V_1_2_Bundle)
        val bundle6 = Json.parseToJsonElement(pkvChargeItem_6_V_1_2_Bundle)

        val result2 = parser.extract(bundle2)
        val result3 = parser.extract(bundle3)
        val result5 = parser.extract(bundle5)
        val result6 = parser.extract(bundle6)

        assertEquals(pkvChargeItems_2_V_1_2_Collection, result2)
        assertEquals(pkvChargeItems_3_V_1_2_Collection, result3)
        assertEquals(pkvChargeItems_5_V_1_2_Collection, result5)
        assertEquals(pkvChargeItems_6_V_1_2_Collection, result6)
    }

    @Test
    fun `parse charge item PZN_Mehrfachverordnung bundle version 1_2`() = runTest {
        val bundle1 =
            Json.parseToJsonElement(pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_1_V_1_2_Bundle)
        val bundle2 =
            Json.parseToJsonElement(pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_2_V_1_2_Bundle)
        val bundle3 =
            Json.parseToJsonElement(pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_3_V_1_2_Bundle)
        val bundle4 =
            Json.parseToJsonElement(pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_4_V_1_2_Bundle)
        val result1 = parser.extract(bundle1)
        val result2 = parser.extract(bundle2)
        val result3 = parser.extract(bundle3)
        val result4 = parser.extract(bundle4)
        assertEquals(1, result1.chargeItems.size)
        assertEquals(1, result2.chargeItems.size)
        assertEquals(1, result3.chargeItems.size)
        assertEquals(1, result4.chargeItems.size)
        assertEquals(
            pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_1_V_1_3.chargeItems.first().invoiceErpModel,
            result1.chargeItems.first().invoiceErpModel
        )
        assertEquals("200.497.827.696.678.76", result2.chargeItems.first().invoiceErpModel?.taskId)
        assertEquals("308412345", result2.chargeItems.first().invoiceErpModel?.organization?.iknr)
        assertEquals("15.4", result2.chargeItems.first().invoiceErpModel?.lineItems?.first()?.price)
        assertEquals(
            "L-thyroxin 75 Henning Tabletten 100 St",
            result2.chargeItems.first().invoiceErpModel?.lineItems?.first()?.chargeItemCode?.text
        )
        assertEquals(
            "200.529.639.126.950.56",
            result3.chargeItems.first().invoiceErpModel?.taskId
        )
        assertEquals(
            "200.020.918.309.115.84",
            result4.chargeItems.first().invoiceErpModel?.taskId
        )
        assertNotNull(result1.chargeItems.first().medicationDispenseErpModel)
        assertEquals(
            FhirTemporal.LocalDate(LocalDate.parse("2023-07-03")),
            result1.chargeItems.first().medicationDispenseErpModel?.whenHandedOver
        )
        assertNotNull(result2.chargeItems.first().medicationDispenseErpModel)
        assertEquals(
            FhirTemporal.LocalDate(LocalDate.parse("2023-09-11")),
            result2.chargeItems.first().medicationDispenseErpModel?.whenHandedOver
        )
        assertNotNull(result3.chargeItems.first().medicationDispenseErpModel)
        assertEquals(
            FhirTemporal.LocalDate(LocalDate.parse("2023-12-11")),
            result3.chargeItems.first().medicationDispenseErpModel?.whenHandedOver
        )
        assertNotNull(result4.chargeItems.first().medicationDispenseErpModel)
        assertEquals(
            FhirTemporal.LocalDate(LocalDate.parse("2024-03-04")),
            result4.chargeItems.first().medicationDispenseErpModel?.whenHandedOver
        )
    }

    // test for TA1__PARENTALE_CYTOSTSTICS_CODE
    @Test
    fun `parse charge item Rezeptur_parenterale_Zytostatika bundle version 1_2`() = runTest {
        val bundle =
            Json.parseToJsonElement(pkvChargeItem_Rezeptur_parenterale_Zytostatika_V_1_2_Bundle)
        val result = parser.extract(bundle)
        assertEquals(1, result.chargeItems.size)
        assertEquals(
            listOf(
                "Bestandteile (Nettopreise):",
                "Herstellung 1 - 2023-07-04T12:00:00.000+00:00: 1 01131365 11 17.33 € / 1 09477471 11 1.36 € / 1 06460518 11 90 €",
                "Herstellung 2 - 2023-07-05T09:00:00.000+00:00: 1 01131365 11 17.33 € / 1 09477471 11 1.36 € / 1 06460518 11 90 €",
                "Herstellung 3 - 2023-07-06T10:00:00.000+00:00: 1 01131365 11 17.33 € / 1 01131365 99 0.96 € / 1 09477471 11 1.36 € / 1 06460518 11 90 €"
            ),
            result.chargeItems.first().invoiceErpModel?.additionalInvoiceInformation
        )
        assertEquals(
            emptyList(),
            result.chargeItems.first().invoiceErpModel?.additionalDispenseItems
        )
    }

    @Test
    fun `parse charge item Rezeptur_Verordnung bundle version 1_2`() = runTest {
        val bundle1 = Json.parseToJsonElement(pkvChargeItem_Rezeptur_Verordnung_Nr_1_V_1_2_Bundle)
        val bundle2 = Json.parseToJsonElement(pkvChargeItem_Rezeptur_Verordnung_Nr_2_V_1_2_Bundle)
        val result1 = parser.extract(bundle1)
        val result2 = parser.extract(bundle2)
        assertEquals("200.858.310.624.061.76", result1.chargeItems.first().invoiceErpModel?.taskId)
        assertEquals("200.800.419.351.304.52", result2.chargeItems.first().invoiceErpModel?.taskId)
        assertEquals(
            "89",
            result1.chargeItems.first().invoiceErpModel?.organization?.address?.houseNumber
        )
        assertEquals("Y/400/2107/36/999", result1.chargeItems.first().kbvDataErpModel?.pvsId)
    }

    @Test
    fun `parse charge item Wirkstoff_Verordnung bundle version 1_2`() = runTest {
        val bundle = Json.parseToJsonElement(pkvChargeItem_Wirkstoff_Verordnung_V_1_2_Bundle)
        val result = parser.extract(bundle)
        assertEquals(pkvChargeItem_Rezeptur_Ramipril_V_1_3, result)
    }

    @Test
    fun `parse charge item bundle for version 1_3`() = runTest {
        val bundle1 = Json.parseToJsonElement(pkvChargeItem_1_V_1_3_Bundle)
        val bundle2 = Json.parseToJsonElement(pkvChargeItem_2_V_1_3_Bundle)
        val bundle3 = Json.parseToJsonElement(pkvChargeItem_3_V_1_3_Bundle)
        val bundle5 = Json.parseToJsonElement(pkvChargeItem_5_V_1_3_Bundle)
        val bundle6 = Json.parseToJsonElement(pkvChargeItem_6_V_1_3_Bundle)
        val bundle7 = Json.parseToJsonElement(pkvChargeItem_7_V_1_3_Bundle)
        val bundle8 = Json.parseToJsonElement(pkvChargeItem_8_V_1_3_Bundle)
        val bundle14 = Json.parseToJsonElement(pkvChargeItem_14_V_1_3_Bundle)
        val bundle15 = Json.parseToJsonElement(pkvChargeItem_15_V_1_3_Bundle)
        val bundle16 = Json.parseToJsonElement(pkvChargeItem_16_V_1_3_Bundle)
        val bundle17 = Json.parseToJsonElement(pkvChargeItem_17_V_1_3_Bundle)
        val bundle18 = Json.parseToJsonElement(pkvChargeItem_18_V_1_3_Bundle)
        val result1 = parser.extract(bundle1)
        val result2 = parser.extract(bundle2)
        val result3 = parser.extract(bundle3)
        val result5 = parser.extract(bundle5)
        val result6 = parser.extract(bundle6)
        val result7 = parser.extract(bundle7)
        val result8 = parser.extract(bundle8)
        val result14 = parser.extract(bundle14)
        val result15 = parser.extract(bundle15)
        val result16 = parser.extract(bundle16)
        val result17 = parser.extract(bundle17)
        val result18 = parser.extract(bundle18)
        assertEquals(pkvInvoice_BelocZok, result1.chargeItems.first().invoiceErpModel)
        assertEquals(pkvInvoice_Venlafaxin, result2.chargeItems.first().invoiceErpModel)
        assertEquals(
            pkvInvoice_InfectoCortiKrupp_Noctu,
            result3.chargeItems.first().invoiceErpModel
        )
        assertEquals(pkvInvoice_Viani50ug250ug, result5.chargeItems.first().invoiceErpModel)
        assertEquals(pkvInvoice_Concor10Plus, result6.chargeItems.first().invoiceErpModel)
        assertEquals(pkvInvoice_Amoxiclav_Noctu, result7.chargeItems.first().invoiceErpModel)
        assertEquals(pkvInvoice_EflueldaTetra, result8.chargeItems.first().invoiceErpModel)
        assertEquals(pkvInvoice_Azithromycin, result14.chargeItems.first().invoiceErpModel)
        assertEquals(pkvInvoice_Benazepril, result15.chargeItems.first().invoiceErpModel)
        assertEquals(pkvInvoice_Tamoxifen, result16.chargeItems.first().invoiceErpModel)
        assertEquals(pkvInvoice_Doxycyclin, result17.chargeItems.first().invoiceErpModel)
        assertEquals(pkvInvoice_Cotrim, result18.chargeItems.first().invoiceErpModel)
    }

    @Test
    fun `parse charge item free text for version 1_3`() = runTest {
        val bundle = Json.parseToJsonElement(pkvChargeItem_freitext_V_1_3_Bundle)
        val result = parser.extract(bundle)
        assertEquals(
            pkvChargeItem_Einzelimport_Yellox_V_1_3.chargeItems.first().invoiceErpModel,
            result.chargeItems.first().invoiceErpModel
        )
        assertEquals(
            pkvChargeItem_Einzelimport_Yellox_V_1_3.chargeItems.first().kbvDataErpModel,
            result.chargeItems.first().kbvDataErpModel
        )
        assertEquals(
            pkvChargeItem_Einzelimport_Yellox_V_1_3.chargeItems.first().medicationDispenseErpModel,
            result.chargeItems.first().medicationDispenseErpModel
        )
    }

    @Test
    fun `parse charge item Mehrfachverordnung_PZN for version 1_3`() = runTest {
        val bundle1 =
            Json.parseToJsonElement(pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_1_V_1_3_Bundle)
        val bundle2 =
            Json.parseToJsonElement(pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_2_V_1_3_Bundle)
        val bundle3 =
            Json.parseToJsonElement(pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_3_V_1_3_Bundle)
        val bundle4 =
            Json.parseToJsonElement(pkvChargeItem_PZN_Mehrfachverordnung_PZN_MV_4_V_1_3_Bundle)
        val result1 = parser.extract(bundle1)
        val result2 = parser.extract(bundle2)
        val result3 = parser.extract(bundle3)
        val result4 = parser.extract(bundle4)
        // result 1
        assertEquals(
            "Y/400/2107/36/999",
            result1.chargeItems.first().kbvDataErpModel?.pvsId
        )
        assertEquals(
            "L-Thyroxin Henning 75 100 Tbl. N3",
            result1.chargeItems.first().kbvDataErpModel?.medication?.text
        )
        assertEquals(
            "02532741",
            result1.chargeItems.first().kbvDataErpModel?.medication?.identifier?.pzn
        )
        assertEquals(
            "L-thyroxin 75 Henning Tabletten 100 St",
            result1.chargeItems.first().invoiceErpModel?.lineItems?.first()?.chargeItemCode?.text
        )
        // result 2
        assertEquals(
            "200.497.827.696.678.76",
            result2.chargeItems.first().invoiceErpModel?.taskId
        )
        assertEquals(
            "308412345",
            result2.chargeItems.first().invoiceErpModel?.organization?.iknr
        )
        assertEquals(
            "L-thyroxin 75 Henning Tabletten 100 St",
            result2.chargeItems.first().invoiceErpModel?.lineItems?.first()?.chargeItemCode?.text
        )
        assertEquals(
            "L-Thyroxin Henning 75 100 Tbl. N3",
            result2.chargeItems.first().kbvDataErpModel?.medication?.text
        )
        assertEquals(
            "N3",
            result2.chargeItems.first().kbvDataErpModel?.medication?.normSizeCode
        )
        // result 3
        assertEquals(
            "02532741",
            result3.chargeItems.first().kbvDataErpModel?.medication?.identifier?.pzn
        )
        assertEquals(
            "02532741",
            result3.chargeItems.first().invoiceErpModel?.lineItems?.first()?.chargeItemCode?.code
        )
        // result 4
        assertEquals(
            "15.40",
            result4.chargeItems.first().invoiceErpModel?.lineItems?.first()?.price
        )
        assertEquals(
            "19.00",
            result4.chargeItems.first().invoiceErpModel?.lineItems?.first()?.tax
        )
    }

    @Test
    fun `parse charge item Zytostatika_Rezeptur_parenterale_Zytostatika for version 1_3`() =
        runTest {
            val bundle = Json.parseToJsonElement(
                pkvChargeItem_Rezeptur_parenterale_Zytostatika_Rezeptur_parenterale_Zytostatika_1_V_1_3_Bundle
            )
            val result = parser.extract(bundle)
            assertEquals(1, result.chargeItems.size)
            assertNull(result.chargeItems.first().medicationDispenseErpModel)
            assertEquals(
                pkvChargeItem_Rezeptur_parenterale_Zytostatika_1_V_1_3_invoice,
                result.chargeItems.first().invoiceErpModel
            )
            assertIs<ByteArray>(result.chargeItems.first().invoiceBinaryErpModel?.binary)
        }

    @Test
    fun `invoice parsing version 1_4`() {
        val bundle = Json.parseToJsonElement(
            Bundle_abgabedaten_1_4_ad80703d_8c62_44a3_b12b_2ea66eda0aa2_12164329
        )
        val fhirModel = bundle.getPkvInvoiceBundle()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(erpmodelInvoice_v1_4, erpModel)
    }

    @Test
    fun `invoice parsing version 1_4 second`() {
        val bundle = Json.parseToJsonElement(
            Bundle_abgabedaten_1_4_f548dde3_a319_486b_8624_6176ff41ad90_4508766
        )
        val fhirModel = bundle.getPkvInvoiceBundle()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(erpmodelInvoice_v1_4_example2, erpModel)
    }

    @Test
    fun `invoice parsing version 1_5`() {
        val bundle = Json.parseToJsonElement(
            Bundle_abgabendaten_1_5_72bd741c_7ad8_41d8_97c3_9aabbdd0f5b4_10483995
        )
        val fhirModel = bundle.getPkvInvoiceBundle()
        val erpModel = fhirModel?.toErpModel()
    }

    @Test
    fun `invoice parsing version 1_5 second`() {
        val bundle = Json.parseToJsonElement(
            Bundle_abgabendaten_1_5_edd55212_965f_4018_a287_6b08e7f5c53c_56751409
        )
        val fhirModel = bundle.getPkvInvoiceBundle()
        val erpModel = fhirModel?.toErpModel()
    }

    @Test
    fun `invoice parsing version 1_5 third`() {
        val bundle = Json.parseToJsonElement(
            Bundle_abgabendaten_1_5_fe4a04af_0828_4977_a5ce_bfeed16ebf10_58406714
        )
        val fhirModel = bundle.getPkvInvoiceBundle()
        val erpModel = fhirModel?.toErpModel()
    }
}
