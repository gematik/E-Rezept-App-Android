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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PharmacyFilterTest {

    @Test
    fun `PharmacyFilter default constructor has empty onSiteFeatureCodes`() {
        val filter = PharmacyFilter()
        assertEquals(emptySet(), filter.onSiteFeatureCodes)
        assertNull(filter.locationFilter)
        assertNull(filter.serviceFilter)
        assertNull(filter.textFilter)
    }

    @Test
    fun `create - with no arguments returns empty filter`() {
        val filter = PharmacyFilter.create()
        assertNull(filter.locationFilter)
        assertNotNull(filter.serviceFilter)
        assertNull(filter.textFilter)
        assertEquals(emptySet(), filter.onSiteFeatureCodes)
    }

    @Test
    fun `create - with onSiteFeatureCodes passes them through`() {
        val features = setOf("abholautomat", "barrierefrei")
        val filter = PharmacyFilter.create(onSiteFeatureCodes = features)
        assertEquals(features, filter.onSiteFeatureCodes)
    }

    @Test
    fun `create - with all on-site features passes them through`() {
        val features = setOf("abholautomat", "barrierefrei", "oepnv", "parkmoeglichkeit")
        val filter = PharmacyFilter.create(onSiteFeatureCodes = features)
        assertEquals(features, filter.onSiteFeatureCodes)
    }

    @Test
    fun `create - with location filter sets it correctly`() {
        val locationFilter = LocationFilter(latitude = 52.52, longitude = 13.405)
        val filter = PharmacyFilter.create(locationFilter = locationFilter)
        assertEquals(locationFilter, filter.locationFilter)
    }

    @Test
    fun `create - with service flags sets service filter correctly`() {
        val filter = PharmacyFilter.create(
            courier = true,
            shipment = true,
            pickup = false
        )
        val serviceFilter = filter.serviceFilter
        assertNotNull(serviceFilter)
        assertTrue(serviceFilter.courier)
        assertTrue(serviceFilter.shipment)
        assertTrue(!serviceFilter.pickup)
    }

    @Test
    fun `create - with text filter sets it correctly`() {
        val textFilter = TextFilter(value = listOf("Apotheke", "Berlin"))
        val filter = PharmacyFilter.create(textFilter = textFilter)
        assertEquals(textFilter, filter.textFilter)
    }

    @Test
    fun `create - with availableServiceCodes passes them to service filter`() {
        val codes = setOf("50", "60")
        val filter = PharmacyFilter.create(availableServiceCodes = codes)
        assertEquals(codes, filter.serviceFilter?.availableServiceCodes)
    }

    @Test
    fun `create - with all parameters sets everything correctly`() {
        val locationFilter = LocationFilter(latitude = 52.52, longitude = 13.405)
        val textFilter = TextFilter(value = listOf("Apotheke"))
        val features = setOf("abholautomat", "parkmoeglichkeit")
        val serviceCodes = setOf("50")

        val filter = PharmacyFilter.create(
            locationFilter = locationFilter,
            textFilter = textFilter,
            courier = true,
            shipment = false,
            pickup = true,
            availableServiceCodes = serviceCodes,
            onSiteFeatureCodes = features
        )

        assertEquals(locationFilter, filter.locationFilter)
        assertEquals(textFilter, filter.textFilter)
        assertEquals(features, filter.onSiteFeatureCodes)
        val serviceFilter = assertNotNull(filter.serviceFilter)
        assertTrue(serviceFilter.courier)
        assertTrue(!serviceFilter.shipment)
        assertTrue(serviceFilter.pickup)
        assertEquals(serviceCodes, serviceFilter.availableServiceCodes)
    }

    @Test
    fun `SearchData toPharmacyFilter includes onSiteFeatures`() {
        val searchData = PharmacyUseCaseData.SearchData(
            name = "Test",
            filter = PharmacyUseCaseData.Filter(
                onSiteFeatures = setOf("abholautomat", "barrierefrei")
            ),
            locationMode = PharmacyUseCaseData.LocationMode.Disabled
        )

        val pharmacyFilter = with(PharmacyUseCaseData.SearchData) {
            searchData.toPharmacyFilter()
        }

        assertEquals(setOf("abholautomat", "barrierefrei"), pharmacyFilter.onSiteFeatureCodes)
    }

    @Test
    fun `SearchData toPharmacyFilter includes empty onSiteFeatures when none selected`() {
        val searchData = PharmacyUseCaseData.SearchData(
            name = "Test",
            filter = PharmacyUseCaseData.Filter(),
            locationMode = PharmacyUseCaseData.LocationMode.Disabled
        )

        val pharmacyFilter = with(PharmacyUseCaseData.SearchData) {
            searchData.toPharmacyFilter()
        }

        assertEquals(emptySet(), pharmacyFilter.onSiteFeatureCodes)
    }

    @Test
    fun `SearchData toPharmacyFilter with location and onSiteFeatures`() {
        val coordinates = PharmacyUseCaseData.Coordinates(latitude = 52.52, longitude = 13.405)
        val searchData = PharmacyUseCaseData.SearchData(
            name = "",
            filter = PharmacyUseCaseData.Filter(
                nearBy = true,
                onSiteFeatures = setOf("abholautomat")
            ),
            locationMode = PharmacyUseCaseData.LocationMode.Enabled(coordinates)
        )

        val pharmacyFilter = with(PharmacyUseCaseData.SearchData) {
            searchData.toPharmacyFilter()
        }

        assertNotNull(pharmacyFilter.locationFilter)
        assertEquals(setOf("abholautomat"), pharmacyFilter.onSiteFeatureCodes)
    }

    @Test
    fun `MapsSearchData toPharmacyFilter includes onSiteFeatures`() {
        val coordinates = PharmacyUseCaseData.Coordinates(latitude = 52.52, longitude = 13.405)
        val mapsSearchData = PharmacyUseCaseData.MapsSearchData(
            name = "",
            filter = PharmacyUseCaseData.Filter(
                onSiteFeatures = setOf("parkmoeglichkeit", "oepnv")
            ),
            locationMode = PharmacyUseCaseData.LocationMode.Enabled(coordinates),
            coordinates = coordinates
        )

        val pharmacyFilter = with(PharmacyUseCaseData.MapsSearchData) {
            mapsSearchData.toPharmacyFilter(forcedRadius = null)
        }

        assertEquals(setOf("parkmoeglichkeit", "oepnv"), pharmacyFilter.onSiteFeatureCodes)
        assertNotNull(pharmacyFilter.locationFilter)
    }

    @Test
    fun `MapsSearchData toPharmacyFilter with empty onSiteFeatures`() {
        val coordinates = PharmacyUseCaseData.Coordinates(latitude = 52.52, longitude = 13.405)
        val mapsSearchData = PharmacyUseCaseData.MapsSearchData(
            name = "",
            filter = PharmacyUseCaseData.Filter(),
            locationMode = PharmacyUseCaseData.LocationMode.Enabled(coordinates),
            coordinates = coordinates
        )

        val pharmacyFilter = with(PharmacyUseCaseData.MapsSearchData) {
            mapsSearchData.toPharmacyFilter(forcedRadius = 5.0)
        }

        assertEquals(emptySet(), pharmacyFilter.onSiteFeatureCodes)
    }
}
