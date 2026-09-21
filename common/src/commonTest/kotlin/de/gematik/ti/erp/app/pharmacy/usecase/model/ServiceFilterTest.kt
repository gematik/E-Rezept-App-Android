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

package de.gematik.ti.erp.app.pharmacy.usecase.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ServiceFilterTest {

    @Test
    fun `buildTextSearch - no services selected and no additional text returns null`() {
        val filter = CodedServiceFilter()
        assertNull(filter.buildTextSearch())
    }

    @Test
    fun `buildTextSearch - no services selected with null additional text returns null`() {
        val filter = CodedServiceFilter()
        assertNull(filter.buildTextSearch(null))
    }

    @Test
    fun `buildTextSearch - no services selected with blank additional text returns null`() {
        val filter = CodedServiceFilter()
        assertNull(filter.buildTextSearch("   "))
    }

    @Test
    fun `buildTextSearch - no services selected with additional text returns trimmed text`() {
        val filter = CodedServiceFilter()
        assertEquals("Apotheke Berlin", filter.buildTextSearch("  Apotheke Berlin  "))
    }

    @Test
    fun `buildTextSearch - pickup only returns Handverkauf`() {
        val filter = CodedServiceFilter(pickup = true)
        assertEquals("Handverkauf", filter.buildTextSearch())
    }

    @Test
    fun `buildTextSearch - courier only returns Botendienst`() {
        val filter = CodedServiceFilter(courier = true)
        assertEquals("Botendienst", filter.buildTextSearch())
    }

    @Test
    fun `buildTextSearch - shipment only returns Versand`() {
        val filter = CodedServiceFilter(shipment = true)
        assertEquals("Versand", filter.buildTextSearch())
    }

    @Test
    fun `buildTextSearch - all services returns all service texts joined by space`() {
        val filter = CodedServiceFilter(pickup = true, courier = true, shipment = true)
        assertEquals("Handverkauf Botendienst Versand", filter.buildTextSearch())
    }

    @Test
    fun `buildTextSearch - services with additional text appends text`() {
        val filter = CodedServiceFilter(pickup = true, courier = true)
        assertEquals("Handverkauf Botendienst Apotheke", filter.buildTextSearch("Apotheke"))
    }

    @Test
    fun `buildTextSearch - available service codes are mapped to German text keywords`() {
        val filter = CodedServiceFilter(availableServiceCodes = setOf("50", "60"))
        assertEquals("Sterilherstellung Bluthochdruck", filter.buildTextSearch())
    }

    @Test
    fun `buildTextSearch - services plus available service codes all mapped to text`() {
        val filter = CodedServiceFilter(pickup = true, availableServiceCodes = setOf("50"))
        assertEquals("Handverkauf Sterilherstellung", filter.buildTextSearch())
    }

    @Test
    fun `buildTextSearch - on-site feature codes are mapped to German text keywords`() {
        val filter = CodedServiceFilter()
        assertEquals(
            "Abholautomat Barrierefrei",
            filter.buildTextSearch(onSiteFeatureCodes = setOf("abholautomat", "barrierefrei"))
        )
    }

    @Test
    fun `buildTextSearch - all on-site features produce correct German keywords`() {
        val filter = CodedServiceFilter()
        assertEquals(
            "Abholautomat Barrierefrei ÖPNV Parkmöglichkeit",
            filter.buildTextSearch(
                onSiteFeatureCodes = setOf("abholautomat", "barrierefrei", "oepnv", "parkmoeglichkeit")
            )
        )
    }

    @Test
    fun `buildTextSearch - services with on-site features and additional text all combined`() {
        val filter = CodedServiceFilter(shipment = true, availableServiceCodes = setOf("impfung"))
        assertEquals(
            "Versand Impfung Barrierefrei Apotheke",
            filter.buildTextSearch("Apotheke", setOf("barrierefrei"))
        )
    }

    @Test
    fun `buildTextSearch - all available service codes map to correct German keywords`() {
        val mappings = mapOf(
            "50" to "Sterilherstellung",
            "60" to "Bluthochdruck",
            "70" to "Inhalationstechnik",
            "80" to "Polymedikation",
            "90" to "Krebstherapie",
            "100" to "Organtransplantation",
            "allergietest" to "Allergietest",
            "impfung" to "Impfung",
            "koerperwerte" to "Körperwerte",
            "reisemedizin-beratung" to "Reisemedizin"
        )
        for ((code, expectedText) in mappings) {
            val filter = CodedServiceFilter(availableServiceCodes = setOf(code))
            assertEquals(expectedText, filter.buildTextSearch(), "Code '$code' should map to '$expectedText'")
        }
    }

    @Test
    fun `buildTextSearch - unknown available service code falls back to code`() {
        val filter = CodedServiceFilter(availableServiceCodes = setOf("unknown-code"))
        assertEquals("unknown-code", filter.buildTextSearch())
    }

    @Test
    fun `buildTextSearch - unknown on-site feature code falls back to code`() {
        val filter = CodedServiceFilter()
        assertEquals("unknown-feature", filter.buildTextSearch(onSiteFeatureCodes = setOf("unknown-feature")))
    }

    @Test
    fun `buildTextSearch - mixed known and unknown service codes`() {
        val filter = CodedServiceFilter(
            courier = true,
            availableServiceCodes = setOf("50", "unknown")
        )
        assertEquals(
            "Botendienst Sterilherstellung unknown ÖPNV",
            filter.buildTextSearch(onSiteFeatureCodes = setOf("oepnv"))
        )
    }

    @Test
    fun `fhirVzdCourier returns code 30 when courier is true`() {
        val filter = CodedServiceFilter(courier = true)
        assertEquals("30", filter.fhirVzdCourier)
    }

    @Test
    fun `fhirVzdCourier returns null when courier is false`() {
        val filter = CodedServiceFilter(courier = false)
        assertNull(filter.fhirVzdCourier)
    }

    @Test
    fun `fhirVzdShipment returns code 40 when shipment is true`() {
        val filter = CodedServiceFilter(shipment = true)
        assertEquals("40", filter.fhirVzdShipment)
    }

    @Test
    fun `fhirVzdShipment returns null when shipment is false`() {
        val filter = CodedServiceFilter(shipment = false)
        assertNull(filter.fhirVzdShipment)
    }

    @Test
    fun `fhirVzdPickup returns code 10 when pickup is true`() {
        val filter = CodedServiceFilter(pickup = true)
        assertEquals("10", filter.fhirVzdPickup)
    }

    @Test
    fun `fhirVzdPickup returns null when pickup is false`() {
        val filter = CodedServiceFilter(pickup = false)
        assertNull(filter.fhirVzdPickup)
    }

    @Test
    fun `ServiceFilter create returns CodedServiceFilter`() {
        val filter = ServiceFilter.create(
            courier = true,
            shipment = false,
            pickup = true,
            availableServiceCodes = setOf("50")
        )
        assert(filter is CodedServiceFilter)
        assertEquals(true, filter.courier)
        assertEquals(false, filter.shipment)
        assertEquals(true, filter.pickup)
        assertEquals(setOf("50"), filter.availableServiceCodes)
    }
}
