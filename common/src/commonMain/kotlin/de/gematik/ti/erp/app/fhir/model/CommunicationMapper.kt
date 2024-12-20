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

package de.gematik.ti.erp.app.fhir.model

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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

// TODO: Can use kotlinx.serialization to create a Communication object and then deserilize to string for better performance
/**
 * Template version 1.2
 * Changes
 * - profile
 * - recipient.system
 *
 */

// TODO: switch GEM_ERP_PR_Communication_DispReq to 1.4 between 15.01.2025 and 15.Jul.2025
//  (version 1.2 and 1.3 of GEM_ERP_PR_Communication_DispReq are valid until 15.Jul.2025)
private fun templateVersion12(
    orderId: String,
    reference: String,
    payload: String,
    recipientTID: String
) = """
{
  "resourceType": "Communication",
  "meta": {
    "profile": [
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq|1.2"
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
        "system": "https://gematik.de/fhir/sid/telematik-id",
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

    val templateString = templateVersion12(
        orderId = JsonPrimitive(orderId).toString(),
        reference = JsonPrimitive(reference).toString(),
        payload = JsonPrimitive(payloadString).toString(),
        recipientTID = JsonPrimitive(recipientTID).toString()
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
        sender: String?,
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

        // TODO: add version 1.4 for GEM_ERP_PR_Communication_DispReq and GEM_ERP_PR_Communication_Reply
        //  with changes and between 15.01.2025 and 15.Jul.2025
        // TODO: remove Version 1.2 and 1.3 after 15.Jul.2025
        val profile = when {
            profileValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq",
                "1.2",
                "1.3",
                "1.4"
            ).invoke(
                profileString
            ) ->
                CommunicationProfile.ErxCommunicationDispReq

            profileValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_Reply",
                "1.2",
                "1.3"
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
