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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.lifecycle.SavedStateHandle
import de.gematik.ti.erp.app.common.usecase.HintUseCase
import de.gematik.ti.erp.app.db.entities.ProfileColorNames
import de.gematik.ti.erp.app.pharmacy.repository.model.PharmacyContacts
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class PharmacySearchViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: PharmacySearchViewModel
    private lateinit var useCase: PharmacySearchUseCase
    private lateinit var hintUseCase: HintUseCase
    private lateinit var profileUseCase: ProfilesUseCase
    private lateinit var savedStateHandle: SavedStateHandle

    private val profile = ProfilesUseCaseData.Profile(
        id = 0,
        name = "",
        insuranceInformation = ProfilesUseCaseData.ProfileInsuranceInformation(
            insurantName = null,
            insuranceIdentifier = null,
            insuranceName = null
        ),
        active = true,
        color = ProfileColorNames.SPRING_GRAY,
        lastAuthenticated = null,
        ssoToken = null,
        accessToken = null
    )

    private val tasks = listOf("A", "B", "C")
    private val prescriptions = listOf(
        PharmacyUseCaseData.PrescriptionOrder(
            taskId = "A", accessCode = "1234", title = "Test", substitutionsAllowed = false
        ),
        PharmacyUseCaseData.PrescriptionOrder(
            taskId = "B", accessCode = "1234", title = "Test", substitutionsAllowed = false
        ),
        PharmacyUseCaseData.PrescriptionOrder(
            taskId = "C", accessCode = "1234", title = "Test", substitutionsAllowed = false
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
        roleCode = listOf(),
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
        hintUseCase = mockk()
        profileUseCase = mockk()
        savedStateHandle = mockk(relaxed = true)
        every { savedStateHandle.get<Unit?>(any()) } returns null
        every { useCase.previousSearch } returns channelFlow { } // suspends
        viewModel = PharmacySearchViewModel(
            mockk(),
            useCase,
            profileUseCase,
            hintUseCase,
            coroutineRule.testDispatchProvider,
            savedStateHandle,
            mockk(relaxed = true)
        )
        coEvery { profileUseCase.profiles } returns flowOf(listOf(profile))
        coEvery { useCase.prescriptionDetailsForOrdering(tasks) } returns flowOf(
            PharmacyUseCaseData.OrderState(
                prescriptions = prescriptions,
                contact = null
            )
        )
        viewModel.onSelectPharmacy(pharmacy)
        viewModel.onSelectOrderOption(orderOption)
    }

    @Test
    fun `order screen state - default`() = runTest {
        val state = viewModel.orderScreenState(tasks).first()

        assertEquals(profile, state.activeProfile)
        assertEquals(null, state.contact)
        assertEquals(pharmacy, state.selectedPharmacy)
        assertEquals(orderOption, state.orderOption)
        assertEquals(prescriptions.map { Pair(it, true) }, state.prescriptions)
    }

    @Test
    fun `order screen state - select prescriptions`() = runTest {
        viewModel.onSelectOrder(prescriptions[0])
        viewModel.onSelectOrder(prescriptions[1])
        viewModel.onSelectOrder(prescriptions[2])

        viewModel.onDeselectOrder(prescriptions[0])

        val state = viewModel.orderScreenState(tasks).first()

        assertEquals(profile, state.activeProfile)
        assertEquals(null, state.contact)
        assertEquals(pharmacy, state.selectedPharmacy)
        assertEquals(orderOption, state.orderOption)
        assertEquals(
            listOf(Pair(prescriptions[0], false), Pair(prescriptions[1], true), Pair(prescriptions[2], true)),
            state.prescriptions
        )
    }

    @Test
    fun `order screen state - set contacts`() = runTest {
        coEvery { useCase.saveShippingContact(any()) } answers {}
        coEvery { useCase.prescriptionDetailsForOrdering(tasks) } returns flowOf(
            PharmacyUseCaseData.OrderState(
                prescriptions = prescriptions,
                contact = contacts
            )
        )

        viewModel.onSaveContact(contacts)

        coroutineRule.testDispatcher.scheduler.runCurrent()
        coVerify(exactly = 1) { useCase.saveShippingContact(contacts) }

        val state = viewModel.orderScreenState(tasks).first()

        assertEquals(profile, state.activeProfile)
        assertEquals(contacts, state.contact)
        assertEquals(pharmacy, state.selectedPharmacy)
        assertEquals(orderOption, state.orderOption)
        assertEquals(prescriptions.map { Pair(it, true) }, state.prescriptions)
    }
}
