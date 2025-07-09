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

package de.gematik.ti.erp.app.fhir.communication

import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDigaDispenseRequest
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDigaDispenseRequest.Companion.getBasedOn
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDigaDispenseRequest.Companion.getDigaExtension
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDigaDispenseRequest.Companion.getOrderId
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDigaDispenseRequest.Companion.getRecipient
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDigaDispenseRequest.Companion.getSender
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDigaDispenseRequest.Companion.toFormattedDateTime
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDigaDispenseRequest.Companion.toJson
import de.gematik.ti.erp.app.fhir.communication.model.FhirDigaMeta.Companion.getDigaMeta
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

class DigaDispenseRequestBuilder {

    private fun resolveOrderId(existingOrderId: String): String = existingOrderId.uppercase()

    fun buildAsJson(
        orderId: String,
        telematikId: String,
        kvnrNumber: String,
        taskId: String,
        accessCode: String,
        sent: Instant = Clock.System.now()
    ): JsonElement {
        val communicationDigaDispenseRequest = build(
            orderId = orderId,
            telematikId = telematikId,
            kvnrNumber = kvnrNumber,
            taskId = taskId,
            accessCode = accessCode,
            sent = sent
        )
        return communicationDigaDispenseRequest.toJson()
    }

    internal fun build(
        orderId: String,
        telematikId: String,
        kvnrNumber: String,
        taskId: String,
        accessCode: String,
        sent: Instant = Clock.System.now()
    ): CommunicationDigaDispenseRequest {
        return CommunicationDigaDispenseRequest(
            id = orderId,
            meta = getDigaMeta(),
            status = "unknown",
            basedOn = getBasedOn(taskId, accessCode),
            recipient = getRecipient(telematikId),
            sender = getSender(kvnrNumber),
            sent = sent.toFormattedDateTime(),
            identifier = getOrderId(resolveOrderId(orderId)),
            extensions = getDigaExtension()
        )
    }
}
