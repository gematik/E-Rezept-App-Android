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

import de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestCommunicationPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestCommunicationPayloadV3ErpModel
import de.gematik.ti.erp.app.communication.model.payload.InfoAvailabilityRequestPayloadErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDispenseRequest
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationRecipient
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationReference
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationValueCoding
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationValueCodingExtension
import de.gematik.ti.erp.app.fhir.communication.model.PayloadForCommunication
import de.gematik.ti.erp.app.fhir.constant.FhirConstants
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationConstants
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationVersions
import kotlinx.serialization.json.JsonElement

object CommunicationDispenseRequest {
    private fun createCommunicationRequest(
        orderId: String,
        taskId: String,
        accessCode: String,
        recipientId: String,
        payloadContent: String,
        flowTypeCode: String,
        version: FhirCommunicationVersions.CommunicationVersion
    ): JsonElement {
        val request = CommunicationDispenseRequest(
            meta = FhirMeta(
                profiles = listOf(
                    "${FhirCommunicationConstants.COMMUNICATION_DISPENSE_WORKFLOW_PROFILE}|${version.version}"
                )
            ),
            identifier = listOf(
                FhirIdentifier(
                    system = FhirCommunicationConstants.ORDER_ID_SYSTEM,
                    value = orderId
                )
            ),
            extension = listOf(
                CommunicationValueCodingExtension(
                    url = FhirCommunicationConstants.PRESCRIPTION_TYPE_EXTENSION,
                    valueCoding = CommunicationValueCoding(
                        system = FhirCommunicationConstants.FLOW_TYPE_SYSTEM,
                        code = flowTypeCode
                    )
                )
            ),
            basedOn = listOf(
                CommunicationReference(
                    reference = "Task/$taskId/\$accept?ac=$accessCode"
                )
            ),
            recipient = listOf(
                CommunicationRecipient(
                    identifier = FhirIdentifier(
                        system = FhirConstants.TELEMATIK_ID_IDENTIFIER,
                        value = recipientId
                    )
                )
            ),
            payload = listOf(
                PayloadForCommunication(
                    contentString = payloadContent
                )
            )
        )

        val jsonString = SafeJson.value.encodeToString(CommunicationDispenseRequest.serializer(), request)

        return SafeJson.value.parseToJsonElement(jsonString)
    }

    /**
     * Creates a Communication dispense request JSON sent to the Fachdienst
     *
     * @param version Communication version to use (defaults to V_1_5 in production)
     * @param communicationPayloadVersion Communication *payload* version ("1" or "3").
     * When "3", [payloadContent] must be a [DispenseRequestCommunicationPayloadV3ErpModel];
     * otherwise it must be a [DispenseRequestCommunicationPayloadV1ErpModel].
     */
    fun createCommunicationDispenseRequest(
        orderId: String,
        taskId: String,
        accessCode: String,
        recipientId: String,
        communicationPayloadVersion: String,
        payloadContent: CommunicationPayloadErpModel,
        flowTypeCode: String,
        flowTypeDisplay: String,
        version: FhirCommunicationVersions.CommunicationVersion = FhirCommunicationVersions.CommunicationVersion.V_1_6
    ): JsonElement {
        val encodedPayload = when (communicationPayloadVersion) {
            "3" -> {
                require(payloadContent is DispenseRequestCommunicationPayloadV3ErpModel) {
                    "Expected DispenseRequestCommunicationPayloadV3ErpModel for payload version 3"
                }
                SafeJson.value.encodeToString(DispenseRequestCommunicationPayloadV3ErpModel.serializer(), payloadContent)
            }
            else -> {
                require(payloadContent is DispenseRequestCommunicationPayloadV1ErpModel) {
                    "Expected DispenseRequestCommunicationPayloadV1ErpModel for payload version 1"
                }
                SafeJson.value.encodeToString(DispenseRequestCommunicationPayloadV1ErpModel.serializer(), payloadContent)
            }
        }

        return createCommunicationRequest(
            orderId = orderId,
            taskId = taskId,
            accessCode = accessCode,
            recipientId = recipientId,
            payloadContent = encodedPayload,
            flowTypeCode = flowTypeCode,
            version = version
        )
    }

    fun createCommunicationReplyToPharmacyRequest(
        orderId: String,
        taskId: String,
        accessCode: String,
        recipientId: String,
        payloadContent: InfoAvailabilityRequestPayloadErpModel,
        flowTypeCode: String,
        flowTypeDisplay: String,
        version: FhirCommunicationVersions.CommunicationVersion = FhirCommunicationVersions.CommunicationVersion.V_1_6
    ): JsonElement = createCommunicationRequest(
        orderId = orderId,
        taskId = taskId,
        accessCode = accessCode,
        recipientId = recipientId,
        payloadContent = SafeJson.value.encodeToString(InfoAvailabilityRequestPayloadErpModel.serializer(), payloadContent),
        flowTypeCode = flowTypeCode,
        version = version
    )
}
