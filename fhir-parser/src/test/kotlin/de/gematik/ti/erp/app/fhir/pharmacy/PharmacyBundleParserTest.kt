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

package de.gematik.ti.erp.app.fhir.pharmacy

import de.gematik.ti.erp.app.data.fhirVzdPharmacyBundle
import de.gematik.ti.erp.app.data.fhirVzdPharmacyNotAvailableTimesBundle
import de.gematik.ti.erp.app.fhir.pharmacy.parser.PharmacyBundleParser
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal.Instant
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporalSerializationType.FhirTemporalInstant
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporalSerializationType.FhirTemporalLocalDate
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlinx.datetime.Instant as KInstant

class PharmacyBundleParserTest {

    private val parser = PharmacyBundleParser()

    @Test
    fun `parse fhirVzd bundle for pharmacy bundle`() = runTest {
        val bundle = Json.parseToJsonElement(fhirVzdPharmacyBundle)
        val results = parser.extract(bundle)
        assertEquals(100, results.total)
        val firstPharmacy = results.entries[0]
        val lastPharmacy = results.entries[99]

        // check first pharmacy
        assertEquals("7025fc46-9809-4ee0-abb9-9e248798e5eb", firstPharmacy.id)
        assertEquals("3-17.2.1024109000.518", firstPharmacy.telematikId)
        assertEquals(51.987705, firstPharmacy.position?.latitude)
        assertEquals(8.485683, firstPharmacy.position?.longitude)
        assertEquals("33649", firstPharmacy.address?.postalCode)

        // check last pharmacy
        assertEquals("Apotheke an der Universität", lastPharmacy.name)
        assertEquals("Jakob-Kaiser-Str. 3", lastPharmacy.address?.lineAddress)
        assertEquals("apotheke@uniapo.com", lastPharmacy.contact.mail)
        assertEquals(2, lastPharmacy.specialities.size)
    }

    @Test
    fun `parse notAvailable periods with time correctly`() = runTest {
        val bundle = Json.parseToJsonElement(fhirVzdPharmacyNotAvailableTimesBundle)
        val results = parser.extract(bundle)
        val pharmacy = results.entries.find { it.id == "93336e26-497c-4c83-ac9a-a25a5ad238dd" }
        requireNotNull(pharmacy)
        assertEquals(1, pharmacy.notAvailablePeriods.size)
        val firstPeriod = pharmacy.notAvailablePeriods[0]
        assertEquals("Urlaub", firstPeriod.description)
        assertEquals(
            Instant(value = KInstant.parse("2025-06-22T14:13:53.164Z"), type = FhirTemporalInstant),
            firstPeriod.period.start
        )
        assertEquals(
            Instant(value = KInstant.parse("2025-06-28T14:13:53.164Z"), type = FhirTemporalInstant),
            firstPeriod.period.end
        )
    }

    @Test
    fun `parse notAvailable periods with only date correctly`() = runTest {
        val bundle = Json.parseToJsonElement(fhirVzdPharmacyNotAvailableTimesBundle)
        val results = parser.extract(bundle)
        val pharmacy = results.entries.find { it.id == "09712925-0b3a-4a3f-b463-b1008252ae38" }
        requireNotNull(pharmacy)
        assertEquals(1, pharmacy.notAvailablePeriods.size)
        val firstPeriod = pharmacy.notAvailablePeriods[0]
        assertEquals("RosenMontag", firstPeriod.description)
        assertEquals(
            FhirTemporal.LocalDate(value = LocalDate.parse("2025-06-22"), type = FhirTemporalLocalDate),
            firstPeriod.period.start
        )
        assertEquals(
            FhirTemporal.LocalDate(value = LocalDate.parse("2025-06-28"), type = FhirTemporalLocalDate),
            firstPeriod.period.end
        )
    }
}
