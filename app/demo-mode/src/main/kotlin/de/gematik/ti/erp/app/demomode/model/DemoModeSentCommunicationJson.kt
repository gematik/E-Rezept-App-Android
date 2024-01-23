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

package de.gematik.ti.erp.app.demomode.model

import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class DemoModeSentCommunicationJson(
    val resourceType: String,
    val meta: DemoModeSentCommunicationMeta,
    val identifier: List<DemoModeCommunicationOrderIdIdentifier>,
    val status: String,
    val basedOn: List<DemoModeCommunicationTaskIdIdentifierReference>,
    val recipient: List<DemoModeCommunicationRecipient>,
    val payload: List<DemoModeCommunicationPayloadContent>
)

@Serializable
data class DemoModeSentCommunicationMeta(
    val profile: List<String>
) {
    val isRequest = profile.any { it.contains("GEM_ERP_PR_Communication_DispReq") }
}

@Serializable
data class DemoModeCommunicationOrderIdIdentifier(
    val system: String,
    // order-id
    val value: String
)

@Serializable
data class DemoModeCommunicationTaskIdIdentifierReference(
    val reference: String
) {
    val taskId = reference.split('/').getOrNull(1) // task-id
}

@Serializable
data class DemoModeCommunicationRecipient(
    val identifier: DemoModeCommunicationRecipientBundle
)

@Serializable
data class DemoModeCommunicationRecipientBundle(
    val system: String,
    val value: String // telematik.id
)

@Serializable
data class DemoModeCommunicationPayloadContent(
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

