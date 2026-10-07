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

import de.gematik.ti.erp.app.debug.repository.CommunicationVersionRepository
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationVersions
import de.gematik.ti.erp.app.mocks.profile.model.MODEL_PROFILE
import de.gematik.ti.erp.app.pharmacy.model.ContactInformationErpModel
import de.gematik.ti.erp.app.pharmacy.model.OrderOptionErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyDetailsErpModel
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionInOrderErpModel
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals

class RedeemPrescriptionsOnLoggedInUseCaseTest {

    private val taskOperationsRepository: TaskOperationsRepository = mockk()
    private val pharmacyRepository: PharmacyRepository = mockk()
    private val communicationVersionRepository: CommunicationVersionRepository = mockk()

    private val dispatcher = StandardTestDispatcher()

    private lateinit var useCase: RedeemPrescriptionsOnLoggedInUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { pharmacyRepository.markPharmacyAsOftenUsed(any()) } returns Unit
        coEvery { communicationVersionRepository.getCommunicationVersion() } returns FhirCommunicationVersions.CommunicationVersion.V_1_5
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun extractPayloadJson(communication: JsonElement): Map<String, String> {
        val contentString = communication
            .jsonObject["payload"]!!
            .jsonArray[0]
            .jsonObject["contentString"]!!
            .jsonPrimitive.content
        return Json.parseToJsonElement(contentString).jsonObject.mapValues { it.value.jsonPrimitive.content }
    }

    @Test
    fun `v3 payload uses country selected from shipping contact instead of hardcoded DE`() = runTest(dispatcher) {
        val capturedCommunication = slot<JsonElement>()
        coEvery {
            taskOperationsRepository.redeem(
                profileId = any(),
                communication = capture(capturedCommunication),
                accessCode = any()
            )
        } returns Result.success(mockk(relaxed = true))

        useCase = RedeemPrescriptionsOnLoggedInUseCase(
            taskOperationsRepository = taskOperationsRepository,
            pharmacyRepository = pharmacyRepository,
            communicationVersionRepository = communicationVersionRepository,
            dispatcher = dispatcher
        )

        val contactWithAustria = testContact.copy(country = "AT")

        useCase.invoke(
            profileId = MODEL_PROFILE.id,
            redeemOption = OrderOptionErpModel.Delivery,
            orderId = UUID.randomUUID().toString(),
            prescriptionOrderInfos = listOf(testPrescription),
            contact = contactWithAustria,
            pharmacy = testPharmacy
        ).first()

        val payload = extractPayloadJson(capturedCommunication.captured)
        assertEquals("3", payload["version"])
        assertEquals("AT", payload["country"])
        assertEquals("Erika", payload["firstname"])
        assertEquals("Mustermann", payload["lastname"])
    }

    @Test
    fun `v3 payload sends null when country is blank`() = runTest(dispatcher) {
        val capturedCommunication = slot<JsonElement>()
        coEvery {
            taskOperationsRepository.redeem(
                profileId = any(),
                communication = capture(capturedCommunication),
                accessCode = any()
            )
        } returns Result.success(mockk(relaxed = true))

        useCase = RedeemPrescriptionsOnLoggedInUseCase(
            taskOperationsRepository = taskOperationsRepository,
            pharmacyRepository = pharmacyRepository,
            communicationVersionRepository = communicationVersionRepository,
            dispatcher = dispatcher
        )

        val contactWithEmptyCountry = testContact.copy(country = "")

        useCase.invoke(
            profileId = MODEL_PROFILE.id,
            redeemOption = OrderOptionErpModel.Delivery,
            orderId = UUID.randomUUID().toString(),
            prescriptionOrderInfos = listOf(testPrescription),
            contact = contactWithEmptyCountry,
            pharmacy = testPharmacy
        ).first()

        val payload = extractPayloadJson(capturedCommunication.captured)
        assertEquals("3", payload["version"])
        assertEquals(null, payload["country"])
    }

    @Test
    fun `v3 payload splits multi-part name into first word as firstname and rest as lastname`() = runTest(dispatcher) {
        val capturedCommunication = slot<JsonElement>()
        coEvery {
            taskOperationsRepository.redeem(
                profileId = any(),
                communication = capture(capturedCommunication),
                accessCode = any()
            )
        } returns Result.success(mockk(relaxed = true))

        useCase = RedeemPrescriptionsOnLoggedInUseCase(
            taskOperationsRepository = taskOperationsRepository,
            pharmacyRepository = pharmacyRepository,
            communicationVersionRepository = communicationVersionRepository,
            dispatcher = dispatcher
        )

        val contactWithMultiPartName = testContact.copy(
            name = "Hans muller schmidth",
            firstname = "",
            lastname = ""
        )

        useCase.invoke(
            profileId = MODEL_PROFILE.id,
            redeemOption = OrderOptionErpModel.Delivery,
            orderId = UUID.randomUUID().toString(),
            prescriptionOrderInfos = listOf(testPrescription),
            contact = contactWithMultiPartName,
            pharmacy = testPharmacy
        ).first()

        val payload = extractPayloadJson(capturedCommunication.captured)
        assertEquals("3", payload["version"])
        assertEquals("Hans", payload["firstname"])
        assertEquals("muller schmidth", payload["lastname"])
    }

    @Test
    fun `v3 payload formats phone number by removing spaces and replacing +49 with 0049`() = runTest(dispatcher) {
        val capturedCommunication = slot<JsonElement>()
        coEvery {
            taskOperationsRepository.redeem(
                profileId = any(),
                communication = capture(capturedCommunication),
                accessCode = any()
            )
        } returns Result.success(mockk(relaxed = true))

        useCase = RedeemPrescriptionsOnLoggedInUseCase(
            taskOperationsRepository = taskOperationsRepository,
            pharmacyRepository = pharmacyRepository,
            communicationVersionRepository = communicationVersionRepository,
            dispatcher = dispatcher
        )

        val contactWithFormattedPhone = testContact.copy(phone = "+49 160 94858168")

        useCase.invoke(
            profileId = MODEL_PROFILE.id,
            redeemOption = OrderOptionErpModel.Delivery,
            orderId = UUID.randomUUID().toString(),
            prescriptionOrderInfos = listOf(testPrescription),
            contact = contactWithFormattedPhone,
            pharmacy = testPharmacy
        ).first()

        val payload = extractPayloadJson(capturedCommunication.captured)
        assertEquals("3", payload["version"])
        assertEquals("004916094858168", payload["phone"])
    }

    companion object {
        private val testPrescription = PrescriptionInOrderErpModel(
            taskId = "160.000.006.394.157.15",
            accessCode = "access-code-1",
            title = "title-1",
            isSelfPayerPrescription = false,
            index = 1,
            timestamp = Instant.parse("2024-08-01T10:00:00Z"),
            substitutionsAllowed = false,
            isScanned = false,
            isTeratogenicPrescription = false
        )

        private val testPharmacy = PharmacyDetailsErpModel(
            id = "pharmacy-id-1",
            name = "pharmacy-name",
            address = "pharmacy-address",
            coordinates = null,
            distance = null,
            contact = ContactInformationErpModel(phone = "", mail = "", url = ""),
            provides = emptyList(),
            openingHours = null,
            telematikId = "9-2.58.00000040"
        )

        private val testContact = ShippingInfoErpModel(
            name = "Erika Mustermann",
            firstname = "Erika",
            lastname = "Mustermann",
            street = "Musterstr. 1",
            addressDetail = "2. OG",
            zip = "10117",
            city = "Berlin",
            country = "DE",
            phone = "+4916094858168",
            mail = "erika@example.com",
            deliveryInfo = "Bitte klingeln"
        )
    }
}
