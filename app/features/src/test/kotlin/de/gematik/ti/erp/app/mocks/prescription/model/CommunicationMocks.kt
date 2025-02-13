/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.mocks.prescription.model

import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.mocks.DATE_2024_01_01
import de.gematik.ti.erp.app.prescription.model.Communication
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
import kotlinx.datetime.Instant

private const val MOCK_COMMUNICATION_ID_01 = "CID-123-001"

val COMMUNICATION_DATA = Communication(
    taskId = "taskId1",
    orderId = "",
    communicationId = "communicationId1",
    profile = CommunicationProfile.ErxCommunicationReply,
    sentOn = DATE_2024_01_01,
    sender = "ABC123456",
    recipient = "ABC654321",
    payload = "payload1",
    consumed = false
)

val MOCK_MESSAGE_01 = OrderUseCaseData.Message(
    communicationId = "MOCK_COMMUNICATION_ID_01",
    sentOn = Instant.fromEpochSeconds(123456),
    message = "mock message.",
    pickUpCodeDMC = "Test_01___Rezept_01___abcdefg12345",
    pickUpCodeHR = "T01__R01",
    link = "https://www.tree.fm/forest/33",
    consumed = false,
    prescriptions = emptyList()
)
