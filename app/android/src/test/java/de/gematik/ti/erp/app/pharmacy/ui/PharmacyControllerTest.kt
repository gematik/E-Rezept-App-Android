/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.fhir.model.PharmacyContacts
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyOrderController
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.GetOrderStateUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyOverviewUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class PharmacySearchViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var pharmacyOrderController: PharmacyOrderController
    private lateinit var pharmacySearchUseCase: PharmacySearchUseCase
    private lateinit var pharmacyOverviewUseCase: PharmacyOverviewUseCase
    private lateinit var profileUseCase: ProfilesUseCase
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getOrderStateUseCase: GetOrderStateUseCase
    private lateinit var getShippingContactValidationUseCase: GetShippingContactValidationUseCase

    @Before
    fun setUp() {
        pharmacySearchUseCase = mockk()
        profileUseCase = mockk()
        getActiveProfileUseCase = mockk()
        pharmacyOverviewUseCase = mockk()
        getOrderStateUseCase = mockk()
        getShippingContactValidationUseCase = mockk()
        every { pharmacySearchUseCase.prescriptionDetailsForOrdering(any()) } returns flowOf(orderState)
        every { getActiveProfileUseCase.invoke() } returns flowOf(activeProfile)
        every { profileUseCase.profiles } returns flowOf(listOf(profile))
        every { getOrderStateUseCase.invoke() } returns flowOf(orderState)

        pharmacyOrderController = PharmacyOrderController(
            getActiveProfileUseCase = getActiveProfileUseCase,
            pharmacySearchUseCase = pharmacySearchUseCase,
            getOrderStateUseCase = getOrderStateUseCase,
            getShippingContactValidationUseCase = getShippingContactValidationUseCase,
            scope = TestScope()
        )

        pharmacyOrderController.onSelectPharmacy(pharmacy, orderOption)
    }

    @Test
    fun `order screen state - default`() = runTest {
        val result = pharmacyOrderController.updatedOrdersForTest.first()
        assertEquals(contacts, result.contact)
        assertEquals(prescriptions, result.orders)
    }

    @Test
    fun `order screen state - select prescriptions`() = runTest {
        pharmacyOrderController.onSelectPrescription(prescriptions[0])
        pharmacyOrderController.onSelectPrescription(prescriptions[1])
        pharmacyOrderController.onSelectPrescription(prescriptions[2])

        pharmacyOrderController.onDeselectPrescription(prescriptions[0])

        val state = pharmacyOrderController.updatedOrdersForTest.first()

        assertEquals(contacts, state.contact)
        assertEquals(
            listOf(prescriptions[1], prescriptions[2]),
            state.orders
        )
    }

    @Test
    fun `order screen state - set contacts`() = runTest {
        coEvery { pharmacySearchUseCase.saveShippingContact(any()) } answers { Unit }
        coEvery { pharmacySearchUseCase.prescriptionDetailsForOrdering("") } returns flowOf(
            PharmacyUseCaseData.OrderState(
                orders = prescriptions,
                contact = contacts
            )
        )

        pharmacyOrderController.onSaveContact(contacts)

        coroutineRule.testDispatcher.scheduler.runCurrent()
        coVerify(exactly = 1) { pharmacySearchUseCase.saveShippingContact(contacts) }

        val state = pharmacyOrderController.updatedOrdersForTest.first()

        assertEquals(contacts, state.contact)
        assertEquals(prescriptions, state.orders)
    }

    companion object {
        private val activeProfile = ProfilesUseCaseData.Profile(
            id = "test-active-profile",
            name = "Active Profile User",
            insurance = ProfileInsuranceInformation(),
            active = true,
            color = ProfilesData.ProfileColorNames.PINK,
            lastAuthenticated = null,
            ssoTokenScope = null,
            image = null,
            avatar = ProfilesData.Avatar.PersonalizedImage
        )
        private val profile = ProfilesUseCaseData.Profile(
            id = "test-inactive-profile",
            name = "Inactive Profile User",
            insurance = ProfileInsuranceInformation(
                insuranceType = ProfilesUseCaseData.InsuranceType.NONE
            ),
            active = false,
            color = ProfilesData.ProfileColorNames.SPRING_GRAY,
            avatar = ProfilesData.Avatar.PersonalizedImage,
            lastAuthenticated = null,
            ssoTokenScope = null
        )

        private val prescriptions = listOf(
            PharmacyUseCaseData.PrescriptionOrder(
                taskId = "A",
                accessCode = "1234",
                title = "Test",
                timestamp = Instant.fromEpochSeconds(0, 0),
                substitutionsAllowed = false,
                index = 0
            ),
            PharmacyUseCaseData.PrescriptionOrder(
                taskId = "B",
                accessCode = "1234",
                title = "Test",
                timestamp = Instant.fromEpochSeconds(0, 0),
                substitutionsAllowed = false,
                index = 0

            ),
            PharmacyUseCaseData.PrescriptionOrder(
                taskId = "C",
                accessCode = "1234",
                title = "Test",
                timestamp = Instant.fromEpochSeconds(0, 0),
                substitutionsAllowed = false,
                index = 0

            )
        )

        private val pharmacy = PharmacyUseCaseData.Pharmacy(
            id = "pharmacy",
            name = "Test - Pharmacy",
            address = null,
            location = null,
            distance = null,
            contacts = PharmacyContacts(
                phone = "0123456",
                mail = "mail@mail.com",
                url = "https://website.com",
                pickUpUrl = "https://pickup.com",
                deliveryUrl = "https://delivery.com",
                onlineServiceUrl = "https://online.com"
            ),
            provides = listOf(),
            openingHours = null,
            telematikId = "telematik-id"
        )

        private val orderOption = PharmacyScreenData.OrderOption.PickupService

        private val contacts = PharmacyUseCaseData.ShippingContact(
            name = "Beate Muster",
            line1 = "Friedrichstraße 136",
            line2 = "",
            postalCode = "10117",
            city = "Berlin",
            telephoneNumber = "0123456789",
            mail = "mail@mail.com",
            deliveryInformation = "Bitte!"
        )

        private val orderState = PharmacyUseCaseData.OrderState(
            orders = prescriptions,
            contact = contacts
        )
    }
}
