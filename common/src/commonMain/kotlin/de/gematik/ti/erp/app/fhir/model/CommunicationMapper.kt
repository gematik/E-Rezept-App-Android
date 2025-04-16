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

package de.gematik.ti.erp.app.fhir.model

/*
 * Copyright 2024, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 */

import de.gematik.ti.erp.app.fhir.communication.model.CommunicationPayload
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.containedStringOrNull
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.profileValue
import de.gematik.ti.erp.app.fhir.parser.stringValue
import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.asFhirInstant
import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Serializable
private data class CommunicationRequest(
    val resourceType: String = "Communication",
    val meta: CommunicationMeta,
    val identifier: List<CommunicationIdentifier>,
    val status: String = "unknown",
    val basedOn: List<CommunicationBasedOn>,
    val recipient: List<CommunicationRecipient>,
    val payload: List<CommunicationPayloadWrapper>
)

@Serializable
private data class CommunicationMeta(
    val profile: List<String>
)

@Serializable
private data class CommunicationIdentifier(
    val system: String,
    val value: String
)

@Serializable
private data class CommunicationBasedOn(
    val reference: String
)

@Serializable
private data class CommunicationRecipient(
    val identifier: CommunicationIdentifier
)

@Serializable
private data class CommunicationPayloadWrapper(
    val contentString: String
)

val json = Json {
    encodeDefaults = true
    prettyPrint = false
}

fun createCommunicationDispenseRequest(
    orderId: String,
    taskId: String,
    accessCode: String,
    recipientTID: String,
    payload: CommunicationPayload
): JsonElement {
    val communicationRequest = CommunicationRequest(
        meta = CommunicationMeta(
            profile = listOf("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq|1.2")
        ),
        identifier = listOf(
            CommunicationIdentifier(
                system = "https://gematik.de/fhir/NamingSystem/OrderID",
                value = orderId
            )
        ),
        basedOn = listOf(
            CommunicationBasedOn(
                reference = "Task/$taskId/\$accept?ac=$accessCode"
            )
        ),
        recipient = listOf(
            CommunicationRecipient(
                identifier = CommunicationIdentifier(
                    system = "https://gematik.de/fhir/sid/telematik-id",
                    value = recipientTID
                )
            )
        ),
        payload = listOf(
            CommunicationPayloadWrapper(
                contentString = json.encodeToString(payload)
            )
        )
    )

    return json.parseToJsonElement(json.encodeToString(communicationRequest))
}

enum class CommunicationProfile {
    ErxCommunicationDispReq, ErxCommunicationReply
}

fun extractCommunications(
    bundle: JsonElement,
    save: (
        taskId: String,
        communicationId: String,
        orderId: String?,
        profile: CommunicationProfile,
        sentOn: FhirTemporal.Instant,
        sender: String?,
        recipient: String,
        payload: String?
    ) -> Unit
): Int {
    Napier.d { "bundle $bundle" }
    val bundleTotal = bundle.containedArrayOrNull("entry")?.size ?: 0
    val resources = bundle.findAll("entry.resource")

    resources.forEach { resource ->
        val profileString = resource
            .contained("meta")
            .contained("profile")
            .contained()

        val profile = when {
            profileValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq",
                "1.2",
                "1.3",
                "1.4"
            ).invoke(profileString) ->
                CommunicationProfile.ErxCommunicationDispReq

            // TODO:
            profileValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_Reply",
                "1.2",
                "1.3",
                "1.4"
            ).invoke(profileString) ->
                CommunicationProfile.ErxCommunicationReply

            else -> error("Unknown communication profile $profileString")
        }

        val reference = resource.contained("basedOn").containedString("reference")
        val taskId = reference.split("/", limit = 3)[1]

        val orderId = resource
            .findAll("identifier")
            .filterWith("system", stringValue("https://gematik.de/fhir/NamingSystem/OrderID"))
            .firstOrNull()
            ?.containedString("value")

        val communicationId = resource.containedString("id")

        val sentOn = requireNotNull(resource.contained("sent").jsonPrimitive.asFhirInstant()) {
            "Communication `sent` field missing"
        }

        val sender = resource
            .containedOrNull("sender")
            ?.contained("identifier")
            ?.containedString("value")

        val recipient = resource
            .contained("recipient")
            .contained("identifier")
            .containedString("value")

        val payload = resource
            .contained("payload")
            .containedStringOrNull("contentString")

        save(
            taskId,
            communicationId,
            orderId,
            profile,
            sentOn,
            sender,
            recipient,
            payload
        )
    }
    return bundleTotal
}
