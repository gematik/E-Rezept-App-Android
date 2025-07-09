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

package de.gematik.ti.erp.app.redeem.presentation

import app.cash.turbine.test
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.HTTP_INTERNAL_ERROR
import de.gematik.ti.erp.app.base.usecase.DownloadAllResourcesUseCase
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK
import de.gematik.ti.erp.app.mocks.profile.model.MODEL_PROFILE
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionRedeemArguments
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.redeem.mocks.RedeemMocks.testCertificate
import de.gematik.ti.erp.app.redeem.model.BaseRedeemState
import de.gematik.ti.erp.app.redeem.model.RedeemedPrescriptionState
import de.gematik.ti.erp.app.redeem.usecase.GetReadyPrescriptionsByTaskIdsUseCase
import de.gematik.ti.erp.app.redeem.usecase.RedeemPrescriptionsOnDirectUseCase
import de.gematik.ti.erp.app.redeem.usecase.RedeemPrescriptionsOnLoggedInUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT
import java.net.HttpURLConnection.HTTP_CONFLICT
import java.net.HttpURLConnection.HTTP_GONE
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.UUID
import kotlin.test.assertEquals

class RedeemPrescriptionListControllerTest {

    private val prescriptionRepository: PrescriptionRepository = mockk()
    private val communicationRepository: CommunicationRepository = mockk()
    private val pharmacyRepository: PharmacyRepository = mockk()
    private val downloadAllResourcesUseCase: DownloadAllResourcesUseCase = mockk()

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var redeemPrescriptionsOnLoggedInUseCase: RedeemPrescriptionsOnLoggedInUseCase
    private lateinit var redeemPrescriptionsOnDirectUseCase: RedeemPrescriptionsOnDirectUseCase
    private lateinit var getReadyPrescriptionsByTaskIdsUseCase: GetReadyPrescriptionsByTaskIdsUseCase

    private lateinit var controllerUnderTest: RedeemPrescriptionsController

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)

        coEvery { pharmacyRepository.markPharmacyAsOftenUsed(any()) } returns Unit
        coEvery { downloadAllResourcesUseCase.invoke(any()) } returns Result.success(1)
        coEvery { prescriptionRepository.loadSyncedTasksByTaskIds(any()) } returns flowOf(listOf(API_ACTIVE_SYNCED_TASK))
        coEvery { prescriptionRepository.loadScannedTasksByTaskIds(any()) } returns flowOf(emptyList())

        redeemPrescriptionsOnLoggedInUseCase = spyk(RedeemPrescriptionsOnLoggedInUseCase(prescriptionRepository, pharmacyRepository, dispatcher))
        redeemPrescriptionsOnDirectUseCase = spyk(RedeemPrescriptionsOnDirectUseCase(communicationRepository, pharmacyRepository, dispatcher))
        getReadyPrescriptionsByTaskIdsUseCase = spyk(GetReadyPrescriptionsByTaskIdsUseCase(prescriptionRepository, dispatcher))
        controllerUnderTest = RedeemPrescriptionsController(
            redeemPrescriptionsOnLoggedInUseCase,
            redeemPrescriptionsOnDirectUseCase,
            downloadAllResourcesUseCase,
            getReadyPrescriptionsByTaskIdsUseCase
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `success - direct redemption`() {
        coEvery { pharmacyRepository.searchBinaryCerts(any()) } returns Result.success(listOf(testCertificate))

        coEvery {
            pharmacyRepository.redeemPrescriptionDirectly(
                url = any(),
                message = any(),
                pharmacyTelematikId = telematikId,
                transactionId = orderId.toString()
            )
        } returns Result.success(Unit)

        coEvery {
            communicationRepository.saveLocalCommunication(
                taskId = taskId,
                pharmacyId = pharmacyId,
                transactionId = orderId.toString()
            )
        } returns Unit

        testScope.runTest {
            advanceUntilIdle()

            controllerUnderTest.processPrescriptionRedemptions(directRedeemArguments)

            controllerUnderTest.redeemedState.test {
                // initial state
                assertEquals(BaseRedeemState.Init, awaitItem())

                // state after processing
                val emittedState = awaitItem()
                assert(emittedState is RedeemedPrescriptionState.OrderCompleted)

                // results within the state
                val prescriptionResultState = (emittedState as RedeemedPrescriptionState.OrderCompleted).results.values.first()
                assert(prescriptionResultState is BaseRedeemState.Success)
                assertEquals(orderId.toString(), emittedState.orderId)

                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 1) {
                redeemPrescriptionsOnDirectUseCase.invoke(
                    orderId = directRedeemArguments.orderId,
                    redeemOption = directRedeemArguments.redeemOption,
                    prescriptionOrderInfos = directRedeemArguments.prescriptionOrderInfos,
                    contact = directRedeemArguments.contact,
                    pharmacy = directRedeemArguments.pharmacy,
                    onRedeemProcessStart = any(),
                    onRedeemProcessEnd = any()
                )
            }

            coVerify(exactly = 0) {
                redeemPrescriptionsOnLoggedInUseCase.invoke(
                    profileId = any(),
                    orderId = any(),
                    redeemOption = any(),
                    prescriptionOrderInfos = any(),
                    contact = any(),
                    pharmacy = any(),
                    onRedeemProcessStart = any(),
                    onRedeemProcessEnd = any()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `success - logged-in redemption`() {
        coEvery {
            prescriptionRepository.redeem(
                profileId = loggedInRedeemArguments.profile.id,
                communication = any(),
                accessCode = any()
            )
        } returns Result.success(JsonPrimitive("jsonElement")) // the json element is not the right structure, but it is not relevant for the test

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.processPrescriptionRedemptions(loggedInRedeemArguments)

            controllerUnderTest.redeemedState.test {
                // initial state
                assertEquals(BaseRedeemState.Init, awaitItem())

                // state after processing
                val emittedState = awaitItem()
                println(emittedState)
                assert(emittedState is RedeemedPrescriptionState.OrderCompleted)

                // results within the state
                val prescriptionResultState = (emittedState as RedeemedPrescriptionState.OrderCompleted).results.values.first()
                assert(prescriptionResultState is BaseRedeemState.Success)
                assertEquals(orderId.toString(), emittedState.orderId)

                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 0) {
                redeemPrescriptionsOnDirectUseCase.invoke(
                    orderId = any(),
                    redeemOption = any(),
                    prescriptionOrderInfos = any(),
                    contact = any(),
                    pharmacy = any(),
                    onRedeemProcessStart = any(),
                    onRedeemProcessEnd = any()
                )
            }

            coVerify(exactly = 1) {
                redeemPrescriptionsOnLoggedInUseCase.invoke(
                    profileId = loggedInRedeemArguments.profile.id,
                    orderId = loggedInRedeemArguments.orderId,
                    redeemOption = loggedInRedeemArguments.redeemOption,
                    prescriptionOrderInfos = loggedInRedeemArguments.prescriptionOrderInfos,
                    contact = loggedInRedeemArguments.contact,
                    pharmacy = loggedInRedeemArguments.pharmacy,
                    onRedeemProcessStart = any(),
                    onRedeemProcessEnd = any()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `failure - logged in redemption HTTP_BAD_REQUEST`() {
        coEvery {
            prescriptionRepository.redeem(
                profileId = loggedInRedeemArguments.profile.id,
                communication = any(),
                accessCode = any()
            )
        } returns Result.failure(
            ApiCallException(
                message = "Error executing safe api call",
                response = Response.error<Any>(HTTP_BAD_REQUEST, "Error executing safe api call".toResponseBody(null))
            )
        )

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.processPrescriptionRedemptions(loggedInRedeemArguments)

            controllerUnderTest.redeemedState.test {
                // initial state
                assertEquals(BaseRedeemState.Init, awaitItem())

                val emittedState = awaitItem()
                assert(emittedState is RedeemedPrescriptionState.OrderCompleted)

                // results within the state
                val prescriptionResultState = (emittedState as RedeemedPrescriptionState.OrderCompleted).results.values.first()
                assert(prescriptionResultState is BaseRedeemState.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `failure - logged in redemption HTTP_UNAUTHORIZED`() {
        coEvery {
            prescriptionRepository.redeem(
                profileId = loggedInRedeemArguments.profile.id,
                communication = any(),
                accessCode = any()
            )
        } returns Result.failure(
            ApiCallException(
                message = "Error executing safe api call",
                response = Response.error<Any>(HTTP_UNAUTHORIZED, "Error executing safe api call".toResponseBody(null))
            )
        )

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.processPrescriptionRedemptions(loggedInRedeemArguments)

            controllerUnderTest.redeemedState.test {
                // initial state
                assertEquals(BaseRedeemState.Init, awaitItem())

                val emittedState = awaitItem()
                assert(emittedState is RedeemedPrescriptionState.OrderCompleted)

                // results within the state
                val prescriptionResultState = (emittedState as RedeemedPrescriptionState.OrderCompleted).results.values.first()
                assert(prescriptionResultState is BaseRedeemState.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `failure - logged in redemption HTTP_CLIENT_TIMEOUT`() {
        coEvery {
            prescriptionRepository.redeem(
                profileId = loggedInRedeemArguments.profile.id,
                communication = any(),
                accessCode = any()
            )
        } returns Result.failure(
            ApiCallException(
                message = "Error executing safe api call",
                response = Response.error<Any>(HTTP_CLIENT_TIMEOUT, "Error executing safe api call".toResponseBody(null))
            )
        )

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.processPrescriptionRedemptions(loggedInRedeemArguments)

            controllerUnderTest.redeemedState.test {
                // initial state
                assertEquals(BaseRedeemState.Init, awaitItem())

                val emittedState = awaitItem()
                assert(emittedState is RedeemedPrescriptionState.OrderCompleted)

                // results within the state
                val prescriptionResultState = (emittedState as RedeemedPrescriptionState.OrderCompleted).results.values.first()
                assert(prescriptionResultState is BaseRedeemState.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `failure - logged in redemption HTTP_CONFLICT`() {
        coEvery {
            prescriptionRepository.redeem(
                profileId = loggedInRedeemArguments.profile.id,
                communication = any(),
                accessCode = any()
            )
        } returns Result.failure(
            ApiCallException(
                message = "Error executing safe api call",
                response = Response.error<Any>(HTTP_CONFLICT, "Error executing safe api call".toResponseBody(null))
            )
        )

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.processPrescriptionRedemptions(loggedInRedeemArguments)

            controllerUnderTest.redeemedState.test {
                // initial state
                assertEquals(BaseRedeemState.Init, awaitItem())

                val emittedState = awaitItem()
                assert(emittedState is RedeemedPrescriptionState.OrderCompleted)

                // results within the state
                val prescriptionResultState = (emittedState as RedeemedPrescriptionState.OrderCompleted).results.values.first()
                assert(prescriptionResultState is BaseRedeemState.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `failure - logged in redemption HTTP_GONE`() {
        coEvery {
            prescriptionRepository.redeem(
                profileId = loggedInRedeemArguments.profile.id,
                communication = any(),
                accessCode = any()
            )
        } returns Result.failure(
            ApiCallException(
                message = "Error executing safe api call",
                response = Response.error<Any>(HTTP_GONE, "Error executing safe api call".toResponseBody(null))
            )
        )

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.processPrescriptionRedemptions(loggedInRedeemArguments)

            controllerUnderTest.redeemedState.test {
                // initial state
                assertEquals(BaseRedeemState.Init, awaitItem())

                val emittedState = awaitItem()
                assert(emittedState is RedeemedPrescriptionState.OrderCompleted)

                // results within the state
                val prescriptionResultState = (emittedState as RedeemedPrescriptionState.OrderCompleted).results.values.first()
                assert(prescriptionResultState is BaseRedeemState.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `failure - logged in redemption HTTP_INTERNAL_ERROR`() {
        coEvery {
            prescriptionRepository.redeem(
                profileId = loggedInRedeemArguments.profile.id,
                communication = any(),
                accessCode = any()
            )
        } returns Result.failure(
            ApiCallException(
                message = "Error executing safe api call",
                response = Response.error<Any>(HTTP_INTERNAL_ERROR, "Error executing safe api call".toResponseBody(null))
            )
        )

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.processPrescriptionRedemptions(loggedInRedeemArguments)

            controllerUnderTest.redeemedState.test {
                // initial state
                assertEquals(BaseRedeemState.Init, awaitItem())

                val emittedState = awaitItem()
                assert(emittedState is RedeemedPrescriptionState.OrderCompleted)

                // results within the state
                val prescriptionResultState = (emittedState as RedeemedPrescriptionState.OrderCompleted).results.values.first()
                assert(prescriptionResultState is BaseRedeemState.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    companion object {
        private val orderId = UUID.randomUUID()

        private const val telematikId = "9-2.58.00000040"
        private const val taskId = "active-synced-task-id-1"
        private const val pharmacyId = "pharmacy-id-1"
        private val prescriptionsForOrders = listOf(
            PharmacyUseCaseData.PrescriptionInOrder(
                taskId = taskId,
                accessCode = "access-code-1",
                title = "title-1",
                isSelfPayerPrescription = false,
                index = 1,
                timestamp = Instant.parse("2024-08-01T10:00:00Z"),
                substitutionsAllowed = false,
                isScanned = false
            )
        )
        private val pharmacy = PharmacyUseCaseData.Pharmacy(
            id = pharmacyId,
            name = "pharmacy-name",
            address = "pharmacy-address",
            coordinates = null,
            distance = null,
            contact = PharmacyUseCaseData.PharmacyContact(
                phone = "pharmacy-phone",
                mail = "pharmacy-mail",
                url = "pharmacy-url",
                pickUpUrl = "pharmacy-pickup-url",
                deliveryUrl = "pharmacy-delivery-url",
                onlineServiceUrl = "pharmacy-online-service-url"
            ),
            provides = emptyList(),
            openingHours = null,
            telematikId = telematikId
        )
        private val contact = PharmacyUseCaseData.ShippingContact(
            name = "contact-name",
            line1 = "contact-line1",
            line2 = "contact-line2",
            postalCode = "contact-postal-code",
            city = "contact-city",
            telephoneNumber = "contact-telephone-number",
            mail = "contact-mail",
            deliveryInformation = "contact-delivery-information"
        )

        private val directRedeemArguments = PrescriptionRedeemArguments.DirectRedemptionArguments(
            orderId = orderId,
            prescriptionOrderInfos = prescriptionsForOrders,
            redeemOption = PharmacyScreenData.OrderOption.PickupService,
            pharmacy = pharmacy,
            contact = contact
        )

        private val loggedInRedeemArguments = PrescriptionRedeemArguments.LoggedInUserRedemptionArguments(
            profile = MODEL_PROFILE,
            orderId = orderId,
            prescriptionOrderInfos = prescriptionsForOrders,
            redeemOption = PharmacyScreenData.OrderOption.PickupService,
            pharmacy = pharmacy,
            contact = contact
        )
    }
}
