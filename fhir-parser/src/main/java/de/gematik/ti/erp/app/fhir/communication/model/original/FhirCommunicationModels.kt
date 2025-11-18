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

package de.gematik.ti.erp.app.fhir.communication.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.CommunicationParticipantErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.DispenseCommunicationPayloadContentErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.DispensePrescriptionTypeErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.DispenseSupplyOptionsType
import de.gematik.ti.erp.app.fhir.communication.model.support.ReplyCommunicationPayloadContentErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.ReplyCommunicationSupplyOptionsErpModel
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.constant.communication.CommunicationDigaConstants
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationConstants
import de.gematik.ti.erp.app.fhir.serializer.SafeFhirInstantSerializer
import de.gematik.ti.erp.app.fhir.serializer.SafeTaskIdSerializer
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Enum representing the supported FHIR communication resource types.
 * Currently supports only the "Communication" resource.
 */
enum class FhirCommunicationResourceType {
    Communication
    ;

    companion object {
        /**
         * Checks whether the given [resourceType] string matches the supported type.
         *
         * @param resourceType The string value of the resource type.
         * @return True if the resource type is valid, otherwise false.
         */
        fun isValidType(resourceType: String?): Boolean {
            return resourceType == Communication.name
        }
    }

    /**
     * FHIR participant model containing the identifier of a communication participant.
     *
     * @property identifier The identifier of the sender or recipient in the communication.
     */
    @Serializable
    internal data class FhirCommunicationParticipant(
        @SerialName("identifier") val identifier: FhirIdentifier? = null
    ) {
        /**
         * Converts this FHIR model to an ERP-specific [CommunicationParticipantErpModel].
         */
        fun toErpModel(): CommunicationParticipantErpModel {
            return CommunicationParticipantErpModel(
                identifier = identifier?.value,
                identifierSystem = identifier?.system
            )
        }
    }

    /**
     * Represents the payload of a FHIR Communication resource.
     *
     * Supports mapping to both reply and dispense communication ERP models depending on the context.
     *
     * @property contentString Optional plain text content of the communication.
     * @property extensions Additional FHIR extensions, used for extracting supply option types.
     */
    @Serializable
    internal data class FhirCommunicationPayload(
        @SerialName("contentString") val contentString: String? = null,
        @SerialName("extension") val extensions: List<FhirExtension> = emptyList()
    ) {
        /**
         * Converts this payload into an ERP reply communication model.
         */
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

        /**
         * Converts this payload into an ERP dispense communication model.
         */
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

    /**
     * JSON-parsed structure of the dispense communication payload content.
     * This structure is embedded within the content string of the FHIR communication.
     *
     * @property version Payload format version.
     * @property supplyOptionsType Optional supply option code (e.g. on-premise, shipment).
     * @property name Name of the patient or contact person.
     * @property address List of address lines.
     * @property hint Additional user-provided notes.
     * @property phone Contact phone number.
     */
    @Serializable
    internal data class DispensePayloadContent(
        @SerialName("version") val version: Int? = null,
        @SerialName("supplyOptionsType") val supplyOptionsType: String? = null,
        @SerialName("name") val name: String? = null,
        @SerialName("address") val address: List<String>? = null,
        @SerialName("hint") val hint: String? = null,
        @SerialName("phone") val phone: String? = null
    )

    /**
     * Data class representing the core FHIR Communication resource.
     *
     * This includes sender/recipient details, payload, status, task references, and communication metadata.
     *
     * Supports transformation into ERP-specific reply or dispense communication models.
     */
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

        /**
         * Extracts the order ID from the list of identifiers, if present.
         */
        private fun getOrderId(): String? {
            return identifier?.find { it.system == FhirCommunicationConstants.ORDER_ID_SYSTEM }?.value
        }

        /**
         * Determines whether this communication corresponds to a DiGA (Digitale Gesundheitsanwendung).
         *
         * A communication is considered a DiGA if it contains a matching prescription type extension.
         */
        private fun getIsDiga(): Boolean {
            val hasFlowTypeForDiga = extensions
                ?.findExtensionByUrl(CommunicationDigaConstants.PRESCRIPTION_TYPE_VALUE_CODING)
                ?.valueCoding?.code == CommunicationDigaConstants.VALUE_CODING_TYPE_162

            return hasFlowTypeForDiga
        }

        /**
         * Extracts the prescription type from FHIR extensions.
         *
         * @return A mapped ERP prescription type model or null if not found.
         */
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

        /**
         * Converts this communication into an ERP reply model.
         */
        internal fun toReplyErpModel(): FhirReplyCommunicationEntryErpModel {
            return FhirReplyCommunicationEntryErpModel(
                id = id ?: "",
                profile = getCommunicationProfileVersion(),
                taskId = taskId,
                sent = sent,
                received = received,
                sender = sender?.toErpModel(),
                recipient = recipient?.firstOrNull()?.toErpModel(),
                payload = payload?.firstOrNull()?.toReplyErpModel() ?: ReplyCommunicationPayloadContentErpModel(),
                orderId = getOrderId()
            )
        }

        /**
         * Converts this communication into an ERP dispense model.
         */
        internal fun toDispenseErpModel(): FhirDispenseCommunicationEntryErpModel {
            return FhirDispenseCommunicationEntryErpModel(
                id = id ?: "",
                profile = getCommunicationProfileVersion(),
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
            /**
             * Parses a [JsonElement] and converts it into a [FhirCommunication], validating the resource type.
             *
             * @return Parsed communication object, or null on error or invalid type.
             */
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

            /**
             * Determines the communication profile type based on FHIR meta profiles.
             */
            internal fun FhirCommunication.getCommunicationProfileType(): CommunicationProfileType {
                return CommunicationProfileType.fromProfiles(meta?.profiles)
            }

            /**
             * Extracts the version string from the communication profile.
             */
            internal fun FhirCommunication.getCommunicationProfileVersion(): String {
                val profiles = meta?.profiles ?: return ""
                val profileType = getCommunicationProfileType()

                if (profileType == CommunicationProfileType.UNKNOWN) {
                    return ""
                }

                return profiles.find { it.contains(profileType.identifier) }
                    ?.let { profileType.getVersionFromProfile(it) }
                    ?: ""
            }
        }
    }
}
