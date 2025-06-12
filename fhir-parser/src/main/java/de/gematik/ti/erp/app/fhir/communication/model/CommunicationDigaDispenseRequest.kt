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

package de.gematik.ti.erp.app.fhir.communication.model

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.communication.FhirCommunicationConstants
import de.gematik.ti.erp.app.fhir.communication.constants.CommunicationDigaConstants
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.PATIENT_KVNR_CODE_GKV
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.TELEMATIK_ID_IDENTIFIER
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// https://simplifier.net/erezept-workflow/2be1c6ac-5d10-47f6-84ee-8318b2c22c76
@Serializable
internal data class CommunicationDigaDispenseRequest(
    @SerialName("resourceType") val resourceType: String = "Communication",
    @SerialName("id") val id: String,
    @SerialName("identifier") val identifier: List<FhirIdentifier>,
    @SerialName("meta") val meta: FhirDigaMeta,
    @SerialName("status") val status: String,
    @SerialName("extension") val extensions: List<CommunicationDigaDispenseRequestExtension>,
    @SerialName("basedOn") val basedOn: List<CommunicationReference>,
    @SerialName("recipient") val recipient: List<CommunicationRecipient>,
    @SerialName("sender") val sender: List<CommunicationRecipient>,
    @SerialName("sent") val sent: String
) {
    companion object {

        internal fun getBasedOn(taskId: String, accessCode: String) =
            listOf(CommunicationReference(reference = "Task/$taskId/\$accept?ac=$accessCode"))

        internal fun getRecipient(telematikId: String) = listOf(
            CommunicationRecipient(
                identifier = FhirIdentifier(
                    system = TELEMATIK_ID_IDENTIFIER,
                    value = telematikId
                )
            )
        )

        // DiGA not enabled for PKV
        internal fun getSender(kvnrNumber: String) = listOf(
            CommunicationRecipient(
                identifier = FhirIdentifier(
                    system = PATIENT_KVNR_CODE_GKV,
                    value = kvnrNumber
                )
            )
        )

        internal fun getDigaExtension() = listOf(
            CommunicationDigaDispenseRequestExtension(
                url = CommunicationDigaConstants.PRESCRIPTION_TYPE_VALUE_CODING,
                valueCoding = CommunicationDigaDispenseRequestValueCoding(
                    code = CommunicationDigaConstants.VALUE_CODING_TYPE_162,
                    system = FhirCommunicationConstants.FLOW_TYPE_SYSTEM
                )
            )
        )

        internal fun getOrderId(orderId: String) = listOf(
            FhirIdentifier(
                system = FhirCommunicationConstants.ORDER_ID_SYSTEM,
                value = orderId
            )
        )

        fun Instant.toFormattedDateTime(): String {
            val datetime = toLocalDateTime(TimeZone.UTC)

            return buildString {
                append("%04d-%02d-%02d".format(datetime.year, datetime.monthNumber, datetime.dayOfMonth))
                append("T")
                append("%02d:%02d:%02d".format(datetime.hour, datetime.minute, datetime.second))
                append(".%03d".format(datetime.nanosecond / 1_000_000)) // Milliseconds only
                append("+00:00") // Always UTC
            }
        }

        internal fun CommunicationDigaDispenseRequest.toJson(): JsonElement {
            val jsonString = SafeJson.value.encodeToString(this)
            return SafeJson.value.parseToJsonElement(jsonString)
        }
    }
}

@Serializable
internal data class FhirDigaMeta(
    @SerialName("profile") val profiles: List<String>
) {
    companion object {
        internal fun getDigaMeta() = FhirDigaMeta(
            profiles = listOf(CommunicationDigaConstants.COMMUNICATION_DIGA_DISP_REQUEST_VERSION)
        )
    }
}

@Serializable
internal data class CommunicationDigaDispenseRequestExtension(val url: String, val valueCoding: CommunicationDigaDispenseRequestValueCoding)

@Serializable
internal data class CommunicationDigaDispenseRequestValueCoding(val system: String, val code: String)
