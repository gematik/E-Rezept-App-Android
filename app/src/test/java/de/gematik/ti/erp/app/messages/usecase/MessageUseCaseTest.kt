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

import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.messages.communicationOnPremise
import de.gematik.ti.erp.app.messages.listOfCommunicationsRead
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
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

    @Before
    fun setUp() {
        repository = mockk()
        useCase = MessageUseCase(repository, mockk())
    }

    @Test
    fun `test loading communications - should return non empty list`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow { listOfCommunicationsUnread() }
            useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply).collect {
                assertTrue(it.isNotEmpty())
            }
        }

    @Test
    fun `test unread communications available - should return true`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadUnreadCommunications(any()) } returns flow { listOfCommunicationsUnread() }
            useCase.unreadCommunicationsAvailable(CommunicationProfile.ErxCommunicationReply)
                .collect {
                    assertTrue(it)
                }
        }

    @Test
    fun `test unread communications available - should return false`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadUnreadCommunications(any()) } returns flow { listOfCommunicationsRead() }
            useCase.unreadCommunicationsAvailable(CommunicationProfile.ErxCommunicationReply)
                .collect {
                    assertFalse(it)
                }
        }

    @Test
    fun `test loading communications - should map to Shipment`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                listOf(
                    communicationShipment()
                )
            }
            useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply).collect {
                val uiMessage = it.first()
                assertTrue(uiMessage is UIMessage)
                assertNotNull(uiMessage)
                assertTrue(uiMessage.supplyOptionsType == SHIPMENT)
            }
        }

    @Test
    fun `test loading communications - should map to Delivery`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                listOf(
                    communicationDelivery()
                )
            }
            useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply).collect {
                val uiMessage = it.first()
                assertTrue(uiMessage is UIMessage)
                assertNotNull(uiMessage)
                assertTrue(uiMessage.supplyOptionsType == DELIVERY)
            }
        }

    @Test
    fun `test loading communications - should map to OnPremise`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                listOf(
                    communicationOnPremise()
                )
            }
            useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply).collect {
                val uiMessage = it.first()
                assertTrue(uiMessage is UIMessage)
                assertNotNull(uiMessage)
                assertTrue(uiMessage.supplyOptionsType == ON_PREMISE)
            }
        }

    @Test
    fun `test mapping communications - should map to OnPremise`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                listOf(
                    communicationOnPremise()
                )
            }
            useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply).collect {
                val uiMessage = it.first() as UIMessage
                assertNotNull(uiMessage)
                assertTrue(uiMessage.supplyOptionsType == ON_PREMISE)
                assertFalse(uiMessage.consumed)
                assertEquals(uiMessage.communicationId, "id")
                assertEquals(uiMessage.pickUpCodeDMC, "465465465f6s4g6df54gs65dfg")
                assertEquals(uiMessage.pickUpCodeHR, "12341234")
                assertFalse(uiMessage.message.isNullOrEmpty())
            }
        }

    @Test
    fun `test mapping communications - null message`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                listOf(
                    communicationDelivery()
                )
            }
            useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply).collect {
                val uiMessage = it.first()
                assertTrue(uiMessage is UIMessage)
                assertNotNull(uiMessage)
                assertTrue(uiMessage.supplyOptionsType == DELIVERY)
                assertFalse(uiMessage.consumed)
                assertTrue(uiMessage.message.isNullOrEmpty())
            }
        }

    @Test
    fun `test mapping communications - should map to Error`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { repository.loadCommunications(any()) } returns flow {
                listOf(
                    errorCommunicationDelivery()
                )
            }
            useCase.loadCommunicationsLocally(CommunicationProfile.ErxCommunicationReply).collect {
                val message = it.first()
                assertTrue(message is ErrorUIMessage)
                assertNotNull(message)
                assertTrue(message.supplyOptionsType == ERROR)
                assertFalse(message.consumed)
                assertTrue(message.message.isNullOrEmpty())
            }
        }
}
