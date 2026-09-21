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

package de.gematik.ti.erp.app.redeem.usecase

import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.diga.repository.DigaRepository
import de.gematik.ti.erp.app.fhir.communication.DigaDispenseRequestBuilder
import de.gematik.ti.erp.app.fhir.constant.communication.CommunicationDigaConstants
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import de.gematik.ti.erp.app.redeem.model.BaseRedeemState
import de.gematik.ti.erp.app.redeem.model.DigaRedeemedPrescriptionState
import de.gematik.ti.erp.app.settings.repository.CommunicationDigaVersionRepository
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RedeemDigaUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: RedeemDigaUseCase

    private val mockPrescriptionRepository = mockk<TaskOperationsRepository>()
    private val mockDigaRepository = mockk<DigaRepository>()
    private val mockRequestBuilder = mockk<DigaDispenseRequestBuilder>()
    private val mockCommunicationDigaVersionRepository = mockk<CommunicationDigaVersionRepository>()
    private val mockDigaTask = TaskErpModel.Synced.Diga(
        profileId = "test-profile",
        name = "Diga task",
        taskId = taskId,
        accessCode = "test-access-code",
        isEuRedeemable = false,
        lastModified = MOCK_CURRENT_TIME,
        isEuRedeemableByPatientAuthorization = false,
        organization = null,
        practitioner = null,
        patient = PatientErpModel(
            name = "Max",
            address = null,
            dateOfBirth = null,
            insuranceIdentifier = "ins123"
        ),
        insuranceInformation = null,
        expiresOn = MOCK_FUTURE_INSTANT,
        acceptUntil = MOCK_FUTURE_INSTANT,
        authoredOn = MOCK_CURRENT_TIME,
        status = TaskStatusEnum.Ready,
        isIncomplete = false,
        pvsIdentifier = "",
        failureToReport = ""
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { mockDigaRepository.updateDigaCommunicationSent(taskId, any()) } returns Unit

        coEvery { mockDigaRepository.updateDigaStatus(taskId, any(), any()) } returns Unit

        coEvery { mockPrescriptionRepository.loadDigaTaskByTaskId(taskId) } returns flowOf(mockDigaTask)

        val mockJson: JsonElement = Json.parseToJsonElement("""{"key":"value"}""")
        every {
            mockRequestBuilder.buildAsJson(
                orderId = any(),
                telematikId = any(),
                kvnrNumber = any(),
                taskId = any(),
                accessCode = any(),
                sent = any(),
                version = any()
            )
        } returns mockJson

        coEvery { mockCommunicationDigaVersionRepository.getCommunicationDigaVersion() } returns
            CommunicationDigaConstants.DigaDispenseRequestVersion.PRODUCTION_DEFAULT

        coEvery { mockPrescriptionRepository.redeem(any(), any(), any()) } returns Result.success(mockJson)

        useCase = RedeemDigaUseCase(
            taskOperationsRepository = mockPrescriptionRepository,
            digaRepository = mockDigaRepository,
            digaDispenseRequestBuilder = mockRequestBuilder,
            communicationDigaVersionRepository = mockCommunicationDigaVersionRepository,
            dispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /*
    @Test
    fun `should return Success when prescription is valid and redeem succeeds`() = runTest {
        coEvery { mockFetchInsuranceProviderUseCase.invoke(kvnr) } returns
                mockk { every { id } returns telematikId }

        val args = RedeemDigaUseCase.RedeemDigaArguments(
            profileId = kvnr,
            taskId = taskId,
            orderId = orderId
        )
        val result = useCase(args)
        assertEquals(BaseRedeemState.Success, result)
    }

     */

    @Test
    fun `should return AlreadyRedeemed when prescription is complete`() = runTest {
        coEvery { mockPrescriptionRepository.loadDigaTaskByTaskId(taskId) } returns flowOf(
            mockDigaTask.copy(status = TaskStatusEnum.Completed)
        )
        val args = RedeemDigaUseCase.RedeemDigaArguments(
            profileId = kvnr,
            taskId = taskId,
            telematikId = telematikId,
            orderId = orderId
        )
        val result = useCase(args)

        assertEquals(
            DigaRedeemedPrescriptionState.AlreadyRedeemed(orderId),
            result
        )
    }

    @Test
    fun `should return Failure when redeem fails`() = runTest {
        coEvery { mockPrescriptionRepository.redeem(any(), any(), any()) } returns Result.failure(
            ApiCallException(
                message = "Error executing safe api call",
                response = Response.error<Any>(HTTP_BAD_REQUEST, "Error executing safe api call".toResponseBody(null))
            )
        )

        val args = RedeemDigaUseCase.RedeemDigaArguments(
            profileId = kvnr,
            taskId = taskId,
            telematikId = telematikId,
            orderId = orderId
        )
        val result = useCase(args)

        assertEquals(
            BaseRedeemState.Error(HttpErrorState.BadRequest),
            result
        )
    }

    companion object {
        private val MOCK_CURRENT_TIME = Instant.parse("2021-01-01T00:00:00Z")
        private val MOCK_FUTURE_INSTANT = Instant.parse("2099-01-01T00:00:00Z")
        private const val taskId = "task-id"
        private const val orderId = "order-id"
        private const val kvnr = "X123456789"
        private const val iknr = "123456789"
        private const val telematikId = "telematik-id"
    }
}
