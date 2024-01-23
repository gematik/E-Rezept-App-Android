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

package de.gematik.ti.erp.app.orders.usecase

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.orders.repository.CommunicationRepository
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class GetUnreadOrdersUseCaseTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val dispatcher = StandardTestDispatcher()

    private val repository: CommunicationRepository = mockk()

    @InjectMockKs
    private lateinit var useCase: GetUnreadOrdersUseCase

    private val profile: ProfilesUseCaseData.Profile = mockk()

    @Before
    fun setup() {
        coEvery { repository.unreadOrders(any()) } returns flowOf(10L)
        every { profile.id } returns "12"

        useCase = GetUnreadOrdersUseCase(repository, dispatcher)
    }

    @Test
    fun `invoke should return flow of unread orders`() = runTest(dispatcher) {
        val expectedUnreadCount = 10L

        val resultOrders = useCase(profile.id).single()

        assertEquals(expectedUnreadCount, resultOrders)
    }
}
