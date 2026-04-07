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

package de.gematik.ti.erp.app.pharmacy.presentation

import de.gematik.ti.erp.app.fhir.pharmacy.model.PharmacyAvailableServiceErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.PharmacyOnSiteFeatureErpModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PharmacyExtensionsTest {

    @Test
    fun `hasOnSiteFeature - empty filter codes returns true for any pharmacy`() {
        val pharmacy = pharmacyWith(
            onSiteFeatures = listOf(PharmacyOnSiteFeatureErpModel(code = "abholautomat"))
        )
        assertTrue(pharmacy.hasAllOnSiteFeatures(emptySet()))
    }

    @Test
    fun `hasOnSiteFeature - empty filter codes returns true for pharmacy with no features`() {
        val pharmacy = pharmacyWith(onSiteFeatures = emptyList())
        assertTrue(pharmacy.hasAllOnSiteFeatures(emptySet()))
    }

    @Test
    fun `hasOnSiteFeature - single filter code matches pharmacy with that feature`() {
        val pharmacy = pharmacyWith(
            onSiteFeatures = listOf(
                PharmacyOnSiteFeatureErpModel(code = "abholautomat"),
                PharmacyOnSiteFeatureErpModel(code = "barrierefrei")
            )
        )
        assertTrue(pharmacy.hasAllOnSiteFeatures(setOf("abholautomat")))
    }

    @Test
    fun `hasOnSiteFeature - single filter code does not match pharmacy without that feature`() {
        val pharmacy = pharmacyWith(
            onSiteFeatures = listOf(
                PharmacyOnSiteFeatureErpModel(code = "barrierefrei"),
                PharmacyOnSiteFeatureErpModel(code = "oepnv")
            )
        )
        assertFalse(pharmacy.hasAllOnSiteFeatures(setOf("abholautomat")))
    }

    @Test
    fun `hasOnSiteFeature - multiple filter codes all match pharmacy features`() {
        val pharmacy = pharmacyWith(
            onSiteFeatures = listOf(
                PharmacyOnSiteFeatureErpModel(code = "abholautomat"),
                PharmacyOnSiteFeatureErpModel(code = "barrierefrei"),
                PharmacyOnSiteFeatureErpModel(code = "oepnv"),
                PharmacyOnSiteFeatureErpModel(code = "parkmoeglichkeit")
            )
        )
        assertTrue(pharmacy.hasAllOnSiteFeatures(setOf("abholautomat", "barrierefrei")))
    }

    @Test
    fun `hasOnSiteFeature - multiple filter codes fail when pharmacy has only some`() {
        val pharmacy = pharmacyWith(
            onSiteFeatures = listOf(
                PharmacyOnSiteFeatureErpModel(code = "barrierefrei"),
                PharmacyOnSiteFeatureErpModel(code = "oepnv")
            )
        )
        assertFalse(pharmacy.hasAllOnSiteFeatures(setOf("abholautomat", "barrierefrei")))
    }

    @Test
    fun `hasOnSiteFeature - filter codes fail when pharmacy has no features at all`() {
        val pharmacy = pharmacyWith(onSiteFeatures = emptyList())
        assertFalse(pharmacy.hasAllOnSiteFeatures(setOf("abholautomat")))
    }

    @Test
    fun `hasOnSiteFeature - all four feature codes match pharmacy with all four features`() {
        val pharmacy = pharmacyWith(
            onSiteFeatures = listOf(
                PharmacyOnSiteFeatureErpModel(code = "abholautomat"),
                PharmacyOnSiteFeatureErpModel(code = "barrierefrei"),
                PharmacyOnSiteFeatureErpModel(code = "oepnv"),
                PharmacyOnSiteFeatureErpModel(code = "parkmoeglichkeit")
            )
        )
        assertTrue(
            pharmacy.hasAllOnSiteFeatures(
                setOf("abholautomat", "barrierefrei", "oepnv", "parkmoeglichkeit")
            )
        )
    }

    @Test
    fun `hasOnSiteFeature - all four feature codes fail when pharmacy missing one`() {
        val pharmacy = pharmacyWith(
            onSiteFeatures = listOf(
                PharmacyOnSiteFeatureErpModel(code = "abholautomat"),
                PharmacyOnSiteFeatureErpModel(code = "barrierefrei"),
                PharmacyOnSiteFeatureErpModel(code = "oepnv")
            )
        )
        assertFalse(
            pharmacy.hasAllOnSiteFeatures(
                setOf("abholautomat", "barrierefrei", "oepnv", "parkmoeglichkeit")
            )
        )
    }

    @Test
    fun `hasAvailableService - empty filter codes returns true for any pharmacy`() {
        val pharmacy = pharmacyWith(
            availableServices = listOf(PharmacyAvailableServiceErpModel(code = "allergietest"))
        )
        assertTrue(pharmacy.hasAllAvailableServices(emptySet()))
    }

    @Test
    fun `hasAvailableService - empty filter codes returns true for pharmacy with no services`() {
        val pharmacy = pharmacyWith(availableServices = emptyList())
        assertTrue(pharmacy.hasAllAvailableServices(emptySet()))
    }

    @Test
    fun `hasAvailableService - single filter code matches pharmacy with that service`() {
        val pharmacy = pharmacyWith(
            availableServices = listOf(
                PharmacyAvailableServiceErpModel(code = "allergietest"),
                PharmacyAvailableServiceErpModel(code = "Impfung")
            )
        )
        assertTrue(pharmacy.hasAllAvailableServices(setOf("allergietest")))
    }

    @Test
    fun `hasAvailableService - single filter code does not match pharmacy without that service`() {
        val pharmacy = pharmacyWith(
            availableServices = listOf(
                PharmacyAvailableServiceErpModel(code = "Impfung")
            )
        )
        assertFalse(pharmacy.hasAllAvailableServices(setOf("allergietest")))
    }

    @Test
    fun `hasAvailableService - multiple filter codes all match`() {
        val pharmacy = pharmacyWith(
            availableServices = listOf(
                PharmacyAvailableServiceErpModel(code = "allergietest"),
                PharmacyAvailableServiceErpModel(code = "Impfung"),
                PharmacyAvailableServiceErpModel(code = "sterilherstellung")
            )
        )
        assertTrue(pharmacy.hasAllAvailableServices(setOf("allergietest", "Impfung")))
    }

    @Test
    fun `hasAvailableService - multiple filter codes fail when pharmacy has only some`() {
        val pharmacy = pharmacyWith(
            availableServices = listOf(
                PharmacyAvailableServiceErpModel(code = "Impfung")
            )
        )
        assertFalse(pharmacy.hasAllAvailableServices(setOf("allergietest", "Impfung")))
    }

    @Test
    fun `hasAvailableService - filter codes fail when pharmacy has no services at all`() {
        val pharmacy = pharmacyWith(availableServices = emptyList())
        assertFalse(pharmacy.hasAllAvailableServices(setOf("allergietest")))
    }

    @Test
    fun `deliveryService - filter disabled returns true regardless of pharmacy services`() {
        val pharmacy = pharmacyWith()
        assertTrue(pharmacy.deliveryService(false))
    }

    @Test
    fun `deliveryService - filter enabled returns true when pharmacy has delivery service`() {
        val pharmacy = pharmacyWith(
            provides = listOf(
                PharmacyUseCaseData.PharmacyService.DeliveryPharmacyService(
                    name = "Botendienst",
                    openingHours = PharmacyUseCaseData.OpeningHours(emptyMap())
                )
            )
        )
        assertTrue(pharmacy.deliveryService(true))
    }

    @Test
    fun `deliveryService - filter enabled returns false when pharmacy has no delivery service`() {
        val pharmacy = pharmacyWith(
            provides = listOf(
                PharmacyUseCaseData.PharmacyService.PickUpPharmacyService(name = "PickUp")
            )
        )
        assertFalse(pharmacy.deliveryService(true))
    }

    @Test
    fun `onlineService - filter disabled returns true regardless of pharmacy services`() {
        val pharmacy = pharmacyWith()
        assertTrue(pharmacy.onlineService(false))
    }

    @Test
    fun `onlineService - filter enabled returns true when pharmacy has online service`() {
        val pharmacy = pharmacyWith(
            provides = listOf(
                PharmacyUseCaseData.PharmacyService.OnlinePharmacyService(name = "Versand")
            )
        )
        assertTrue(pharmacy.onlineService(true))
    }

    @Test
    fun `onlineService - filter enabled returns false when pharmacy has no online service`() {
        val pharmacy = pharmacyWith(
            provides = listOf(
                PharmacyUseCaseData.PharmacyService.PickUpPharmacyService(name = "PickUp")
            )
        )
        assertFalse(pharmacy.onlineService(true))
    }

    @Test
    fun `recentlyUsed - filter disabled returns true regardless`() {
        val pharmacy = pharmacyWith(telematikId = "id-123")
        assertTrue(pharmacy.recentlyUsed(false, emptySet()))
    }

    @Test
    fun `recentlyUsed - filter enabled returns true when pharmacy telematikId is in set`() {
        val pharmacy = pharmacyWith(telematikId = "id-123")
        assertTrue(pharmacy.recentlyUsed(true, setOf("id-123", "id-456")))
    }

    @Test
    fun `recentlyUsed - filter enabled returns false when pharmacy telematikId is not in set`() {
        val pharmacy = pharmacyWith(telematikId = "id-123")
        assertFalse(pharmacy.recentlyUsed(true, setOf("id-456")))
    }

    companion object {
        private fun pharmacyWith(
            onSiteFeatures: List<PharmacyOnSiteFeatureErpModel> = emptyList(),
            availableServices: List<PharmacyAvailableServiceErpModel> = emptyList(),
            provides: List<PharmacyUseCaseData.PharmacyService> = emptyList(),
            telematikId: String = "test-telematik-id"
        ) = PharmacyUseCaseData.Pharmacy(
            id = "test-id",
            name = "Test Apotheke",
            address = "Teststraße 1\n12345 Berlin",
            coordinates = null,
            distance = null,
            contact = PharmacyUseCaseData.PharmacyContact(
                phone = "",
                mail = "",
                url = ""
            ),
            provides = provides,
            openingHours = null,
            telematikId = telematikId,
            onSiteFeatures = onSiteFeatures,
            availableServices = availableServices
        )
    }
}
