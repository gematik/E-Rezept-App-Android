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

package de.gematik.ti.erp.app.fhir.communication

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDispenseRequest
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationPayload
import de.gematik.ti.erp.app.fhir.communication.model.Extension
import de.gematik.ti.erp.app.fhir.communication.model.Payload
import de.gematik.ti.erp.app.fhir.communication.model.Recipient
import de.gematik.ti.erp.app.fhir.communication.model.Reference
import de.gematik.ti.erp.app.fhir.communication.model.ValueCoding
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.serialization.json.JsonElement

object CommunicationDispenseRequest {

    /**
     * Creates a Communication dispense request JSON sent to the Fachdienst
     */
    fun createCommunicationDispenseRequest(
        orderId: String,
        taskId: String,
        accessCode: String,
        recipientTID: String,
        payloadContent: CommunicationPayload,
        flowTypeCode: String,
        flowTypeDisplay: String
    ): JsonElement {
        val request = CommunicationDispenseRequest(
            meta = FhirMeta(
                profiles = listOf("${FhirCommunicationConstants.COMMUNICATION_DISPENSE_PROFILE_BASE}|${FhirCommunicationVersions.COMMUNICATION_VERSION_1_4}")
            ),
            identifier = listOf(
                FhirIdentifier(
                    system = FhirCommunicationConstants.ORDER_ID_SYSTEM,
                    value = orderId
                )
            ),
            extension = listOf(
                Extension(
                    url = FhirCommunicationConstants.PRESCRIPTION_TYPE_EXTENSION,
                    valueCoding = ValueCoding(
                        system = FhirCommunicationConstants.FLOW_TYPE_SYSTEM,
                        code = flowTypeCode,
                        display = flowTypeDisplay
                    )
                )
            ),
            basedOn = listOf(
                Reference(
                    reference = "Task/$taskId/\$accept?ac=$accessCode"
                )
            ),
            recipient = listOf(
                Recipient(
                    identifier = FhirIdentifier(
                        system = FhirCommunicationConstants.RECIPIENT_IDENTIFIER_SYSTEM,
                        value = recipientTID
                    )
                )
            ),
            payload = listOf(
                Payload(
                    contentString = SafeJson.value.encodeToString(CommunicationPayload.serializer(), payloadContent)
                )
            )
        )

        val jsonString = SafeJson.value.encodeToString(CommunicationDispenseRequest.serializer(), request)

        return SafeJson.value.parseToJsonElement(jsonString)
    }
}
