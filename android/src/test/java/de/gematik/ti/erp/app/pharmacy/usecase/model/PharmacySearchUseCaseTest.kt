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

package de.gematik.ti.erp.app.pharmacy.usecase.model

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.prescription.repository.PROFILE
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hl7.fhir.r4.model.Communication
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Meta
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.StringType
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class PharmacySearchUseCaseTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var useCase: PharmacySearchUseCase

    @MockK(relaxed = true)
    private lateinit var repository: PrescriptionRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = PharmacySearchUseCase(
            repository = mockk(),
            shippingContactRepository = mockk(),
            prescriptionRepository = repository,
            settingsUseCase = mockk(relaxed = true),
            dispatchers = coroutineRule.dispatchers
        )
    }

    @Test
    fun `redeem prescription`() = runTest {
        val communicationSlot = slot<Communication>()

        coEvery { repository.redeemPrescription("1234567890", capture(communicationSlot)) } answers {
            Result.success(
                Unit
            )
        }

        val orderId = UUID.randomUUID()
        useCase.redeemPrescription(
            profileId = "1234567890",
            redeemOption = RemoteRedeemOption.Local,
            orderId = orderId,
            order = PharmacyUseCaseData.PrescriptionOrder(
                taskId = "",
                accessCode = "",
                title = "",
                substitutionsAllowed = false
            ),
            contact = PharmacyUseCaseData.ShippingContact(
                name = "Test-Name",
                line1 = "",
                line2 = "",
                postalCodeAndCity = "123456",
                telephoneNumber = "",
                mail = "",
                deliveryInformation = ""
            ),
            pharmacyTelematikId = "TID-1234567890"
        )

        val payload = """
            {
                "version": "1",
                "supplyOptionsType": "onPremise",
                "name": "Test-Name",
                "address": [
                    "",
                    "",
                    "123456"
                ],
                "hint": "",
                "phone": ""
            }
        """.trimIndent().replace("\\s".toRegex(), "")

        val expected = Communication().apply {
            meta = Meta().addProfile(PROFILE)
            identifier = listOf(
                Identifier().apply {
                    system = "https://gematik.de/fhir/NamingSystem/OrderID"
                    value = orderId.toString()
                }
            )
            addBasedOn(Reference("Task/\$accept?ac="))
            addPayload(
                Communication.CommunicationPayloadComponent().apply {
                    content = StringType(payload)
                }
            )
            status = Communication.CommunicationStatus.UNKNOWN
            addRecipient(
                Reference().setIdentifier(
                    Identifier().apply {
                        system = "https://gematik.de/fhir/NamingSystem/TelematikID"
                        value = "TID-1234567890"
                    }
                )
            )
        }

        assertTrue { communicationSlot.captured.equalsDeep(expected) }
    }
}
