/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.pharmacy.repository.model.DeliveryPharmacyService
import de.gematik.ti.erp.app.pharmacy.repository.model.EmergencyPharmacyService
import de.gematik.ti.erp.app.pharmacy.repository.model.LocalPharmacyService
import de.gematik.ti.erp.app.pharmacy.repository.model.Location
import de.gematik.ti.erp.app.pharmacy.repository.model.OpeningHours
import de.gematik.ti.erp.app.pharmacy.repository.model.OpeningTime
import de.gematik.ti.erp.app.utils.testPharmacySearchBundle
import org.hl7.fhir.r4.model.Bundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

private const val LOCATION_NAME = "Heide-Apotheke"
private const val ADDRESS_LINE = "Langener Landstraße 266"
private const val ADDRESS_CITY = "Bremerhaven"
private const val ADDRESS_POSTAL_CODE = "27578"
private const val TELEMATIK_ID = "3-05.2.1007600000.080"

class PharmacyMapperTest {

    lateinit var bundle: Bundle

    @Before
    fun setup() {
        bundle = testPharmacySearchBundle()
    }

    @Test
    fun `extract pharmacy one with 3 services`() {
        val (pharmacies, _, _) = PharmacyMapper.extractLocalPharmacyServices(bundle)
        assertEquals(10, pharmacies.size)

        val services = pharmacies[0].provides
        assertEquals(3, services.size)

        assertTrue(services[0] is LocalPharmacyService)
        assertTrue(services[1] is DeliveryPharmacyService)
        assertTrue(services[2] is EmergencyPharmacyService)

        val openingTime = OffsetDateTime.of(2021, 4, 20, 9, 20, 0, 0, ZoneOffset.of("+2"))
        val sunday = OffsetDateTime.of(2021, 4, 25, 9, 20, 0, 0, ZoneOffset.of("+2"))
        val saturdayEvening = OffsetDateTime.of(2021, 4, 24, 18, 20, 0, 0, ZoneOffset.of("+2"))

        assertTrue(services[0].isOpenAt(openingTime))
        assertFalse(services[0].isOpenAt(sunday))
        assertTrue(services[1].isOpenAt(openingTime))
        assertFalse(services[1].isOpenAt(sunday))
        assertTrue(services[2].isOpenAt(sunday))
        assertTrue(services[2].isOpenAt(saturdayEvening))

        assertTrue(services[2].isAllDayOpen(DayOfWeek.SUNDAY))
        assertEquals(LocalTime.of(20, 0), services[1].openUntil(openingTime))
        assertEquals(null, services[1].opensAt(openingTime))

        val timeOpenAt8 = OpeningTime(LocalTime.of(8, 0), LocalTime.of(12, 0))
        val timeOpenAt14 = OpeningTime(LocalTime.of(14, 0), LocalTime.of(18, 0))
        val hoursOpen = OpeningHours(
            mapOf(
                DayOfWeek.MONDAY to listOf(timeOpenAt8, timeOpenAt14),
                DayOfWeek.TUESDAY to listOf(timeOpenAt8, timeOpenAt14),
                DayOfWeek.WEDNESDAY to listOf(timeOpenAt8, timeOpenAt14),
                DayOfWeek.THURSDAY to listOf(timeOpenAt8, timeOpenAt14),
                DayOfWeek.FRIDAY to listOf(timeOpenAt8, timeOpenAt14),
                DayOfWeek.SATURDAY to listOf(timeOpenAt8),
            )
        )

        assertEquals(hoursOpen, services[0].openingHours)

        assertEquals(LOCATION_NAME, pharmacies[0].name)
        assertEquals(TELEMATIK_ID, pharmacies[0].telematikId)

        val location = pharmacies[0].location

        assertEquals(Location(8.597412, 53.590027), location)

        val address = pharmacies[0].address
        assertEquals(ADDRESS_LINE, address.lines[0])
        assertEquals(ADDRESS_CITY, address.city)
        assertEquals(ADDRESS_POSTAL_CODE, address.postalCode)
    }

    @Test
    fun `extract pharmacy one - with three roleCodes`() {
        val (pharmacies, _, _) = PharmacyMapper.extractLocalPharmacyServices(bundle)
        val roleCodes = pharmacies[0].roleCode
        assertTrue(roleCodes.size == 3)
    }

    @Test
    fun `compare locations`() {
        assertEquals(Location(1.12345678, 1.12345678), Location(1.12345678, 1.12345678))
        assertEquals(Location(1.12345678, 1.12345678), Location(1.123456789, 1.123456789))
        assertEquals(Location(1.12345678, 1.12345678), Location(1.123457, 1.123457))
        assertEquals(Location(-1.12345678, 1.12345678), Location(-1.123457, 1.123457))

        assertFalse(Location(1.12345678, 1.12345678) == Location(0.123457, 1.12345678))
        assertFalse(Location(1.12345678, 1.12345678) == Location(1.12345678, -1.12345678))
    }
}
