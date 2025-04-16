/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.model

import de.gematik.ti.erp.app.datasource.data.MockConstants.MOCK_COMMUNICATION_ID_01
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * This is created to link the communication with a particular profile
 */
data class MockProfileLinkedCommunication(
    val profileId: String,
    val taskId: String,
    val communicationId: String,
    val orderId: String,
    val profile: CommunicationProfile,
    val sentOn: Instant,
    val sender: String,
    val recipient: String,
    val payload: String?,
    val consumed: Boolean
)

fun MockProfileLinkedCommunication.toSyncedTaskDataCommunication() =
    Communication(
        taskId = taskId,
        communicationId = communicationId,
        orderId = orderId,
        profile = profile,
        sentOn = sentOn,
        sender = sender,
        recipient = recipient,
        payload = payload,
        consumed = consumed
    )

internal fun MockSentCommunicationJson.toMockProfileLinkedCommunication(
    profileId: ProfileIdentifier
) =
    MockProfileLinkedCommunication(
        profileId = profileId,
        communicationId = MOCK_COMMUNICATION_ID_01,
        taskId = basedOn.firstNotNullOfOrNull { it.taskId } ?: "",
        orderId = identifier.firstNotNullOfOrNull { it.value } ?: "",
        profile = when (meta.isRequest) {
            true -> CommunicationProfile.ErxCommunicationDispReq
            false -> CommunicationProfile.ErxCommunicationReply
        },
        sentOn = Clock.System.now(),
        sender = payload.firstNotNullOfOrNull { it.name } ?: "",
        recipient = recipient.firstNotNullOfOrNull { it.identifier.value } ?: "",
        payload = payload.firstNotNullOfOrNull { it.contentString },
        consumed = false
    )
