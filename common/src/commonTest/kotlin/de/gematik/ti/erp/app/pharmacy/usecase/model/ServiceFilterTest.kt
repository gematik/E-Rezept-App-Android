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
    fun `buildTextSearch - available service codes are included in text`() {
        val filter = CodedServiceFilter(availableServiceCodes = setOf("50", "60"))
        assertEquals("50 60", filter.buildTextSearch())
    }

    @Test
    fun `buildTextSearch - services plus available service codes are all included`() {
        val filter = CodedServiceFilter(pickup = true, availableServiceCodes = setOf("50"))
        assertEquals("Handverkauf 50", filter.buildTextSearch())
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
