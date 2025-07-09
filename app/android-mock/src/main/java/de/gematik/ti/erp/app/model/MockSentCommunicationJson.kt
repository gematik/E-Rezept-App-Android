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

package de.gematik.ti.erp.app.model

import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class MockSentCommunicationJson(
    val resourceType: String,
    val meta: MockSentCommunicationMeta,
    val identifier: List<MockCommunicationOrderIdIdentifier>,
    val status: String,
    val basedOn: List<MockCommunicationTaskIdIdentifierReference>,
    val recipient: List<MockCommunicationRecipient>,
    val payload: List<MockCommunicationPayloadContent>
)

@Serializable
data class MockSentCommunicationMeta(
    val profile: List<String>
) {
    val isRequest = profile.any { it.contains("GEM_ERP_PR_Communication_DispReq") }
}

@Serializable
data class MockCommunicationOrderIdIdentifier(
    val system: String,
    // order-id
    val value: String
)

@Serializable
data class MockCommunicationTaskIdIdentifierReference(
    val reference: String
) {
    val taskId = reference.split('/').getOrNull(1) // task-id
}

@Serializable
data class MockCommunicationRecipient(
    val identifier: MockCommunicationRecipientBundle
)

@Serializable
data class MockCommunicationRecipientBundle(
    val system: String,
    val value: String // telematik.id
)

@Serializable
data class MockCommunicationPayloadContent(
    val contentString: String
) {
    private fun jsonObject() = JSONObject(contentString)
    val name: String
        get() = jsonObject().getString("name")

    val supplyOptionType: String
        get() = jsonObject().getString("supplyOptionsType")

    val address: SyncedTaskData.Address
        get() {
            val item = jsonObject().getString("address").split(',')
            return SyncedTaskData.Address(
                line1 = item[0],
                line2 = item[1],
                postalCode = item[2],
                city = item[3]
            )
        }
}
