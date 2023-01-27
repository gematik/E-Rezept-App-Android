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

package de.gematik.ti.erp.app.pharmacy.ui

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.fhir.model.PharmacyContacts
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyOverviewUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class PharmacySearchViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var orderState: PharmacyOrderState
    private lateinit var useCase: PharmacySearchUseCase
    private lateinit var oftenUseCase: PharmacyOverviewUseCase
    private lateinit var profileUseCase: ProfilesUseCase

    private val profile = ProfilesUseCaseData.Profile(
        id = "",
        name = "",
        insuranceInformation = ProfilesUseCaseData.ProfileInsuranceInformation(),
        active = true,
        color = ProfilesData.ProfileColorNames.SPRING_GRAY,
        avatarFigure = ProfilesData.AvatarFigure.PersonalizedImage,
        lastAuthenticated = null,
        ssoTokenScope = null
    )

    private val prescriptions = listOf(
        PharmacyUseCaseData.PrescriptionOrder(
            taskId = "A",
            accessCode = "1234",
            title = "Test",
            timestamp = Instant.EPOCH,
            substitutionsAllowed = false
        ),
        PharmacyUseCaseData.PrescriptionOrder(
            taskId = "B",
            accessCode = "1234",
            title = "Test",
            timestamp = Instant.EPOCH,
            substitutionsAllowed = false
        ),
        PharmacyUseCaseData.PrescriptionOrder(
            taskId = "C",
            accessCode = "1234",
            title = "Test",
            timestamp = Instant.EPOCH,
            substitutionsAllowed = false
        )
    )

    private val pharmacy = PharmacyUseCaseData.Pharmacy(
        name = "Test - Pharmacy",
        address = null,
        location = null,
        distance = null,
        contacts = PharmacyContacts(phone = "", mail = "", url = ""),
        provides = listOf(),
        openingHours = null,
        telematikId = "",
        ready = false
    )

    private val orderOption = PharmacyScreenData.OrderOption.ReserveInPharmacy

    private val contacts = PharmacyUseCaseData.ShippingContact(
        name = "Beate Muster",
        line1 = "Friedrichstraße 136",
        line2 = "",
        postalCodeAndCity = "10117 Berlin",
        telephoneNumber = "",
        mail = "",
        deliveryInformation = ""
    )

    @Before
    fun setUp() {
        useCase = mockk()
        profileUseCase = mockk()
        oftenUseCase = mockk()
        coEvery { useCase.prescriptionDetailsForOrdering("0") } returns flowOf(
            PharmacyUseCaseData.OrderState(
                prescriptions = prescriptions,
                contact = contacts
            )
        )
        orderState = PharmacyOrderState(
            profileId = "0",
            useCase = useCase,
            scope = CoroutineScope(coroutineRule.dispatchers.Unconfined)
        )
        coEvery { profileUseCase.profiles } returns flowOf(listOf(profile))
        orderState.onSelectPharmacy(pharmacy, orderOption)
    }

    @Test
    fun `order screen state - default`() = runTest {
        val state = orderState.orderFlow.first()

        assertEquals(contacts, state.contact)
        assertEquals(prescriptions, state.prescriptions)
    }

    @Test
    fun `order screen state - select prescriptions`() = runTest {
        orderState.onSelectPrescription(prescriptions[0])
        orderState.onSelectPrescription(prescriptions[1])
        orderState.onSelectPrescription(prescriptions[2])

        orderState.onDeselectPrescription(prescriptions[0])

        val state = orderState.orderFlow.first()

        assertEquals(contacts, state.contact)
        assertEquals(
            listOf(prescriptions[1], prescriptions[2]),
            state.prescriptions
        )
    }

    @Test
    fun `order screen state - set contacts`() = runTest {
        coEvery { useCase.saveShippingContact(any()) } answers {}
        coEvery { useCase.prescriptionDetailsForOrdering("") } returns flowOf(
            PharmacyUseCaseData.OrderState(
                prescriptions = prescriptions,
                contact = contacts
            )
        )

        orderState.onSaveContact(contacts)

        coroutineRule.testDispatcher.scheduler.runCurrent()
        coVerify(exactly = 1) { useCase.saveShippingContact(contacts) }

        val state = orderState.orderFlow.first()

        assertEquals(contacts, state.contact)
        assertEquals(prescriptions, state.prescriptions)
    }
}
