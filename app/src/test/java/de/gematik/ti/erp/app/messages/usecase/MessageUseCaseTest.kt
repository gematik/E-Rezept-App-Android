/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.messages.usecase

import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.messages.communicationOnPremise
import de.gematik.ti.erp.app.messages.listOfCommunicationsUnread
import de.gematik.ti.erp.app.messages.repository.MessageRepository
import de.gematik.ti.erp.app.messages.ui.models.ErrorUIMessage
import de.gematik.ti.erp.app.messages.ui.models.UIMessage
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import de.gematik.ti.erp.app.utils.communicationDelivery
import de.gematik.ti.erp.app.utils.communicationShipment
import de.gematik.ti.erp.app.utils.errorCommunicationDelivery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val SHIPMENT = "shipment"
private const val ON_PREMISE = "onPremise"
private const val DELIVERY = "delivery"
private const val ERROR = "none"

@ExperimentalCoroutinesApi
class MessageUseCaseTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var useCase: MessageUseCase
    private lateinit var repository: MessageRepository
    private lateinit var moshi: Moshi

    @Before
    fun setUp() {
        repository = mockk()
        moshi = Moshi.Builder().build()
        useCase = MessageUseCase(repository, moshi)
    }

    @Test
    fun `test loading communications - should return non empty list`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                emit(listOfCommunicationsUnread())
            }
            val result =
                useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply)
                    .toList()
            assertTrue(result.isNotEmpty())
        }

    @Test
    fun `test unread communications available - should return true`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadUnreadCommunications(any()) } returns flow {
                emit(listOfCommunicationsUnread())
            }
            val result =
                useCase.unreadCommunicationsAvailable(CommunicationProfile.ErxCommunicationReply)
                    .first()
            assertTrue(result)
        }

    @Test
    fun `test unread communications available - should return false`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadUnreadCommunications(any()) } returns flow {
                emit(listOf<Communication>())
            }
            val result =
                useCase.unreadCommunicationsAvailable(CommunicationProfile.ErxCommunicationReply)
                    .first()
            assertFalse(result)
        }

    @Test
    fun `test loading communications - should map to Shipment`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                emit(
                    listOf(communicationShipment())
                )
            }
            val result =
                useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply)
                    .first()
            val uiMessage = result.first()
            assertNotNull(uiMessage)
            assertTrue(uiMessage is UIMessage)
            assertTrue(uiMessage.supplyOptionsType == SHIPMENT)
        }

    @Test
    fun `test loading communications - should map to Delivery`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                emit(listOf(communicationDelivery()))
            }
            val result =
                useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply)
                    .first()
            val uiMessage = result.first()
            assertNotNull(uiMessage)
            assertTrue(uiMessage is UIMessage)
            assertTrue(uiMessage.supplyOptionsType == DELIVERY)
        }

    @Test
    fun `test loading communications - should map to OnPremise`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                emit(listOf(communicationOnPremise()))
            }
            val result =
                useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply)
                    .first()
            val uiMessage = result.first()
            assertNotNull(uiMessage)
            assertTrue(uiMessage is UIMessage)
            assertTrue(uiMessage.supplyOptionsType == ON_PREMISE)
        }

    @Test
    fun `test mapping communications - should map to OnPremise`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                emit(listOf(communicationOnPremise()))
            }
            val result =
                useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply)
                    .first()
            val uiMessage = result.first() as UIMessage
            assertNotNull(uiMessage)
            assertTrue(uiMessage.supplyOptionsType == ON_PREMISE)
            assertFalse(uiMessage.consumed)
            assertEquals(uiMessage.communicationId, "id")
            assertEquals(uiMessage.pickUpCodeDMC, "465465465f6s4g6df54gs65dfg")
            assertEquals(uiMessage.pickUpCodeHR, "12341234")
            assertFalse(uiMessage.message.isNullOrEmpty())
        }

    @Test
    fun `test mapping communications - null message`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                emit(listOf(communicationDelivery()))
            }
            val result =
                useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply)
                    .first()
            val uiMessage = result.first()
            assertNotNull(uiMessage)
            assertTrue(uiMessage is UIMessage)
            assertTrue(uiMessage.supplyOptionsType == DELIVERY)
            assertFalse(uiMessage.consumed)
            assertTrue(uiMessage.message.isNullOrEmpty())
        }

    @Test
    fun `test mapping communications - should map to Error`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                emit(listOf(errorCommunicationDelivery()))
            }
            val result =
                useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply)
                    .first()
            val message = result.first()
            assertNotNull(message)
            assertTrue(message is ErrorUIMessage)
            assertTrue(message.supplyOptionsType == ERROR)
            assertFalse(message.consumed)
        }
}
