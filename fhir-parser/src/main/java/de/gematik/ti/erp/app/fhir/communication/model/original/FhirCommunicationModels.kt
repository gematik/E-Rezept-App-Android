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

package de.gematik.ti.erp.app.fhir.communication.model.original

import de.gematik.ti.erp.app.fhir.common.model.erp.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.communication.FhirCommunicationConstants
import de.gematik.ti.erp.app.fhir.communication.constants.CommunicationDigaConstants
import de.gematik.ti.erp.app.fhir.communication.model.erp.CommunicationParticipantErpModel
import de.gematik.ti.erp.app.fhir.communication.model.erp.DispenseCommunicationPayloadContentErpModel
import de.gematik.ti.erp.app.fhir.communication.model.erp.DispensePrescriptionTypeErpModel
import de.gematik.ti.erp.app.fhir.communication.model.erp.DispenseSupplyOptionsType
import de.gematik.ti.erp.app.fhir.communication.model.erp.ReplyCommunicationPayloadContentErpModel
import de.gematik.ti.erp.app.fhir.communication.model.erp.ReplyCommunicationSupplyOptionsErpModel
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.serializer.SafeFhirInstantSerializer
import de.gematik.ti.erp.app.fhir.serializer.SafeTaskIdSerializer
import de.gematik.ti.erp.app.utils.FhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class FhirCommunicationResourceType {
    Communication;

    companion object {
        fun isValidType(resourceType: String?): Boolean {
            return resourceType == Communication.name
        }
    }

    @Serializable
    internal data class FhirCommunicationParticipant(
        @SerialName("identifier") val identifier: FhirIdentifier? = null
    ) {
        fun toErpModel(): CommunicationParticipantErpModel {
            return CommunicationParticipantErpModel(
                identifier = identifier?.value,
                identifierSystem = identifier?.system
            )
        }
    }

    @Serializable
    internal data class FhirCommunicationPayload(
        @SerialName("contentString") val contentString: String? = null,
        @SerialName("extension") val extensions: List<FhirExtension> = emptyList()
    ) {
        fun toReplyErpModel(): ReplyCommunicationPayloadContentErpModel {
            val supplyOptionsExt = extensions.findExtensionByUrl(
                FhirCommunicationConstants.SUPPLY_OPTIONS_TYPE_EXTENSION
            )

            val supplyOptions = supplyOptionsExt?.let {
                val onPremise = it.extensions.findExtensionByUrl(FhirCommunicationConstants.EXT_ON_PREMISE)?.valueBoolean ?: false
                val shipment = it.extensions.findExtensionByUrl(FhirCommunicationConstants.EXT_SHIPMENT)?.valueBoolean ?: false
                val delivery = it.extensions.findExtensionByUrl(FhirCommunicationConstants.EXT_DELIVERY)?.valueBoolean ?: false

                ReplyCommunicationSupplyOptionsErpModel(
                    onPremise = onPremise,
                    shipment = shipment,
                    delivery = delivery
                )
            }

            return ReplyCommunicationPayloadContentErpModel(
                text = contentString,
                supplyOptions = supplyOptions
            )
        }

        fun toDispenseErpModel(): DispenseCommunicationPayloadContentErpModel {
            val parsedContent = contentString?.let {
                try {
                    Json.decodeFromString<DispensePayloadContent>(it)
                } catch (e: Exception) {
                    Napier.e("Error parsing dispense payload JSON: ${e.message}")
                    null
                }
            }

            return DispenseCommunicationPayloadContentErpModel(
                contentString = contentString,
                supplyOptionsType = DispenseSupplyOptionsType.fromString(parsedContent?.supplyOptionsType),
                name = parsedContent?.name,
                address = parsedContent?.address,
                phone = parsedContent?.phone
            )
        }
    }

    @Serializable
    internal data class DispensePayloadContent(
        @SerialName("version") val version: Int? = null,
        @SerialName("supplyOptionsType") val supplyOptionsType: String? = null,
        @SerialName("name") val name: String? = null,
        @SerialName("address") val address: List<String>? = null,
        @SerialName("hint") val hint: String? = null,
        @SerialName("phone") val phone: String? = null
    )

    @Serializable
    internal data class FhirCommunication(
        @SerialName("resourceType") val resourceType: String? = null,
        @SerialName("id") val id: String? = null,
        @SerialName("meta") val meta: FhirMeta? = null,
        @SerialName("status") val status: String? = null,
        @SerialName("identifier") val identifier: List<FhirIdentifier>? = null,
        @SerialName("sender") val sender: FhirCommunicationParticipant? = null,
        @SerialName("recipient") val recipient: List<FhirCommunicationParticipant>? = null,
        @SerialName("payload") val payload: List<FhirCommunicationPayload>? = null,
        @SerialName("extension") val extensions: List<FhirExtension>? = null,

        @SerialName("basedOn") @Serializable(with = SafeTaskIdSerializer::class)
        val taskId: String? = null,

        @SerialName("sent") @Serializable(with = SafeFhirInstantSerializer::class)
        val sent: FhirTemporal.Instant? = null,

        @SerialName("received") @Serializable(with = SafeFhirInstantSerializer::class)
        val received: FhirTemporal.Instant? = null

    ) {
        fun getProfileType(): CommunicationProfileType {
            return CommunicationProfileType.fromProfiles(meta?.profiles)
        }

        fun getProfileVersion(): String {
            val profiles = meta?.profiles ?: return ""
            val profileType = getProfileType()

            if (profileType == CommunicationProfileType.UNKNOWN) {
                return ""
            }

            return profiles.find { it.contains(profileType.profileIdentifier) }
                ?.let { profileType.getVersionFromProfile(it) }
                ?: ""
        }

        private fun getOrderId(): String? {
            return identifier?.find { it.system == FhirCommunicationConstants.ORDER_ID_SYSTEM }?.value
        }

        private fun getIsDiga(): Boolean {
            val noPayload = payload == null

            val hasFlowTypeForDiga = extensions
                ?.findExtensionByUrl(CommunicationDigaConstants.PRESCRIPTION_TYPE_VALUE_CODING)
                ?.valueCoding?.code == CommunicationDigaConstants.VALUE_CODING_TYPE_162

            return noPayload && hasFlowTypeForDiga
        }

        private fun getPrescriptionType(): DispensePrescriptionTypeErpModel? {
            val prescriptionExt = extensions?.find {
                it.url == FhirCommunicationConstants.PRESCRIPTION_TYPE_EXTENSION
            }

            return prescriptionExt?.valueCoding?.let { coding ->
                DispensePrescriptionTypeErpModel(
                    code = coding.code,
                    system = coding.system,
                    display = coding.display
                )
            }
        }

        fun toReplyErpModel(): FhirReplyCommunicationEntryErpModel {
            return FhirReplyCommunicationEntryErpModel(
                id = id ?: "",
                profile = getProfileVersion(),
                taskId = taskId,
                sent = sent,
                received = received,
                sender = sender?.toErpModel(),
                recipient = recipient?.firstOrNull()?.toErpModel(),
                payload = payload?.firstOrNull()?.toReplyErpModel() ?: ReplyCommunicationPayloadContentErpModel(),
                orderId = getOrderId()
            )
        }

        fun toDispenseErpModel(): FhirDispenseCommunicationEntryErpModel {
            return FhirDispenseCommunicationEntryErpModel(
                id = id ?: "",
                profile = getProfileVersion(),
                taskId = taskId,
                sender = sender?.toErpModel(),
                recipient = recipient?.firstOrNull()?.toErpModel(),
                payload = payload?.firstOrNull()?.toDispenseErpModel() ?: DispenseCommunicationPayloadContentErpModel(),
                prescriptionType = getPrescriptionType(),
                sent = sent,
                orderId = getOrderId(),
                isDiga = getIsDiga()
            )
        }

        companion object {
            fun JsonElement.getCommunication(): FhirCommunication? {
                val resourceType = this.jsonObject["resourceType"]?.jsonPrimitive?.content

                if (!isValidType(resourceType)) {
                    Napier.e("Invalid resource type: Expected '${Communication.name}', found '$resourceType'")
                    return null
                }

                return try {
                    SafeJson.value.decodeFromJsonElement(serializer(), this)
                } catch (e: Exception) {
                    Napier.e("Error parsing FHIR Communication: ${e.message}")
                    null
                }
            }
        }
    }
}
