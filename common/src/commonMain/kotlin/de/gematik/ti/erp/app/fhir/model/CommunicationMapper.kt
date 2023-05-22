/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.FhirTemporal
import de.gematik.ti.erp.app.fhir.parser.asFhirInstant
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.containedStringOrNull
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.profileValue
import de.gematik.ti.erp.app.fhir.parser.stringValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

private fun template(
    orderId: String,
    reference: String,
    payload: String,
    recipientTID: String
) = """
{
  "resourceType": "Communication",
  "meta": {
    "profile": [
      "https://gematik.de/fhir/StructureDefinition/ErxCommunicationDispReq"
    ]
  },
  "identifier": [
    {
      "system": "https://gematik.de/fhir/NamingSystem/OrderID",
      "value": $orderId
    }
  ],
  "status": "unknown",
  "basedOn": [
    {
      "reference": $reference
    }
  ],
  "recipient": [
    {
      "identifier": {
        "system": "https://gematik.de/fhir/NamingSystem/TelematikID",
        "value": $recipientTID
      }
    }
  ],
  "payload": [
    {
      "contentString": $payload
    }
  ]
}
""".trimIndent()

// private fun templateVersion1_2(
//    orderId: String,
//    reference: String,
//    payload: String,
//    recipientTID: String
// ) = """
// {
//  "resourceType": "Communication",
//  "meta": {
//    "profile": [
//      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq|1.2"
//    ]
//  },
//  "identifier": [
//    {
//      "system": "https://gematik.de/fhir/NamingSystem/OrderID",
//      "value": $orderId
//    }
//  ],
//  "status": "unknown",
//  "basedOn": [
//    {
//      "reference": $reference
//    }
//  ],
//  "recipient": [
//    {
//      "identifier": {
//        "system": "https://gematik.de/fhir/NamingSystem/TelematikID",
//        "value": $recipientTID
//      }
//    }
//  ],
//  "payload": [
//    {
//      "contentString": $payload
//    }
//  ]
// }
// """.trimIndent()

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
    val payloadString = json.encodeToString(payload)
    val reference = "Task/$taskId/\$accept?ac=$accessCode"

    // Todo: use template Version 1.2 if supported
    val templateString = template(
        orderId = JsonPrimitive(orderId).toString(),
        reference = JsonPrimitive(reference).toString(),
        recipientTID = JsonPrimitive(recipientTID).toString(),
        payload = JsonPrimitive(payloadString).toString()
    )

    return json.parseToJsonElement(templateString)
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
        sender: String,
        recipient: String,
        payload: String?
    ) -> Unit
): Int {
    val bundleTotal = bundle.containedArrayOrNull("entry")?.size ?: 0
    val resources = bundle
        .findAll("entry.resource")

    resources.forEach { resource ->
        val profileString = resource
            .contained("meta")
            .contained("profile")
            .contained()

        val profile = when {
            profileValue("https://gematik.de/fhir/StructureDefinition/ErxCommunicationDispReq").invoke(profileString) ->
                CommunicationProfile.ErxCommunicationDispReq

            profileValue(
                "https://gematik.de/fhir/StructureDefinition/ErxCommunicationDispReq",
                "1.1.1"
            ).invoke(
                profileString
            ) ->
                CommunicationProfile.ErxCommunicationDispReq

            profileValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq",
                "1.2"
            ).invoke(
                profileString
            ) ->
                CommunicationProfile.ErxCommunicationDispReq

            // without profile version
            profileValue(
                "https://gematik.de/fhir/StructureDefinition/ErxCommunicationReply"
            ).invoke(profileString) ->
                CommunicationProfile.ErxCommunicationReply

            profileValue(
                "https://gematik.de/fhir/StructureDefinition/ErxCommunicationReply",
                "1.1.1"
            ).invoke(profileString) ->
                CommunicationProfile.ErxCommunicationReply

            profileValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_Reply",
                "1.2"
            ).invoke(profileString) ->
                CommunicationProfile.ErxCommunicationReply

            else -> error("Unknown communication profile $profileString")
        }

        val reference = resource.contained("basedOn").containedString("reference")
        val taskId = reference.split("/", limit = 3)[1] // Task/160.000.000.036.519.13/$accept?ac=...

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
            .contained("sender")
            .contained("identifier")
            .containedString("value")

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
