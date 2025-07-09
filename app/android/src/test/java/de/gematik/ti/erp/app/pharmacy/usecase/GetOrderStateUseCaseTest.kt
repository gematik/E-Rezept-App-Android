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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_SYNCED_TASK_DATA_01
import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_SYNCED_TASK_DATA_02
import de.gematik.ti.erp.app.pharmacy.mocks.MOCK_ACTIVE_PROFILE
import de.gematik.ti.erp.app.pharmacy.mocks.MOCK_SCANNED_TASK_DATA_REDEEMABLE_01
import de.gematik.ti.erp.app.pharmacy.mocks.MOCK_SCANNED_TASK_DATA_REDEEMABLE_02
import de.gematik.ti.erp.app.pharmacy.mocks.MOCK_SCANNED_TASK_DATA_REDEEMED_01
import de.gematik.ti.erp.app.pharmacy.mocks.MOCK_SHIPPING_CONTACT
import de.gematik.ti.erp.app.pharmacy.mocks.MOCK_SYNCED_TASK_DATA_REDEEMABLE_01
import de.gematik.ti.erp.app.pharmacy.mocks.MOCK_SYNCED_TASK_DATA_REDEEMABLE_02
import de.gematik.ti.erp.app.pharmacy.mocks.MOCK_SYNCED_TASK_DATA_REDEEMABLE_SELF_PAYER_03
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OrderState
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.ShippingContact
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetOrderStateUseCaseTest {
    private val dispatcher = StandardTestDispatcher()

    private val profileRepository: ProfileRepository = mockk()
    private val prescriptionRepository: PrescriptionRepository = mockk()
    private val shippingContactRepository: ShippingContactRepository = mockk()

    @InjectMockKs
    private lateinit var useCase: GetOrderStateUseCase

    @Before
    fun setup() {
        useCase = GetOrderStateUseCase(
            profileRepository,
            prescriptionRepository,
            shippingContactRepository,
            dispatcher
        )
        coEvery { profileRepository.activeProfile() } returns flowOf(MOCK_ACTIVE_PROFILE)
    }

    @Test
    fun `invoke should return OrderState with 5 orders (3 synced, 2 scanned, 1 SelfPayer) and shippingContact`() {
        coEvery { shippingContactRepository.shippingContact() } returns flowOf(MOCK_SHIPPING_CONTACT)
        coEvery { prescriptionRepository.syncedTasks(any()) } returns flowOf(
            listOf(
                MOCK_SYNCED_TASK_DATA_REDEEMABLE_01,
                MOCK_SYNCED_TASK_DATA_REDEEMABLE_02,
                MOCK_SYNCED_TASK_DATA_REDEEMABLE_SELF_PAYER_03,
                MOCK_SYNCED_TASK_DATA_01,
                MOCK_SYNCED_TASK_DATA_02
            )
        )
        coEvery { prescriptionRepository.scannedTasks(any()) } returns flowOf(
            listOf(
                MOCK_SCANNED_TASK_DATA_REDEEMABLE_01,
                MOCK_SCANNED_TASK_DATA_REDEEMABLE_02,
                MOCK_SCANNED_TASK_DATA_REDEEMED_01
            )
        )
        runTest(dispatcher) {
            val orderState = useCase().first()
            assert(orderState.prescriptionsInOrder.size == 5)
            assertEquals(MOCK_SHIPPING_CONTACT.toModel(), orderState.contact)
            assertEquals(1, orderState.selfPayerPrescriptionIds.size)
        }
    }

    @Test
    fun `invoke should return OrderState with empty Orders and shippingContact`() {
        coEvery { shippingContactRepository.shippingContact() } returns flowOf(null)
        coEvery { prescriptionRepository.syncedTasks(any()) } returns flowOf(
            listOf()
        )
        coEvery { prescriptionRepository.scannedTasks(any()) } returns flowOf(
            listOf()
        )
        runTest(dispatcher) {
            val orderState = useCase().first()

            assertEquals(
                OrderState(
                    prescriptionsInOrder = emptyList(),
                    selfPayerPrescriptionIds = emptyList(),
                    contact = ShippingContact.EmptyShippingContact
                ),
                orderState
            )
        }
    }
}
