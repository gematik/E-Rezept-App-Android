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

package de.gematik.ti.erp.app.fhir.communication.parser

import de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyLinkPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPaymentInfoPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeDMCPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeHRPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyReservationStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyTextPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestCommunicationPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestDeliveryPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestReservationPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestShipmentPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.InfoAvailabilityRequestPayloadErpModel
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import io.github.aakira.napier.Napier
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object CommunicationPayloadParser {

    fun extract(
        payloadJson: String,
        isRequest: Boolean = false
    ): CommunicationPayloadErpModel? {
        return runCatching {
            val jsonObject = SafeJson.value.parseToJsonElement(payloadJson).jsonObject
            val version = jsonObject["version"]?.jsonPrimitive?.content?.toIntOrNull()

            if (version == 3 ||
                jsonObject.containsKey("communicationType") ||
                jsonObject.containsKey("text") ||
                jsonObject.containsKey("pickupCodeHR") ||
                jsonObject.containsKey("pickupCodeDMC")
            ) {
                parseVersion3(jsonObject, isRequest) ?: parseVersion1(jsonObject, isRequest)
            } else {
                when (version) {
                    1 -> parseVersion1(jsonObject, isRequest)
                    3 -> parseVersion3(jsonObject, isRequest)
                    else -> {
                        parseVersion3(jsonObject, isRequest) ?: parseVersion1(jsonObject, isRequest)
                    }
                }
            }
        }.onFailure { e ->
            Napier.e(tag = "communication-payload-serializer", throwable = e) { "Error selecting deserializer" }
        }.getOrNull()
    }

    private fun parseVersion1(
        jsonObject: JsonObject,
        isRequest: Boolean
    ): CommunicationPayloadErpModel? {
        return runCatching {
            if (isRequest) {
                SafeJson.value.decodeFromJsonElement<DispenseRequestCommunicationPayloadV1ErpModel>(jsonObject)
            } else {
                val reply = SafeJson.value.decodeFromJsonElement<CommunicationReplyPayloadV1ErpModel>(jsonObject)
                mapV1ToV3ReplyModel(reply)
            }
        }.onFailure { e ->
            Napier.e(tag = "communication-payload-serializer", throwable = e) { "Error selecting deserializer" }
        }.getOrNull()
    }

    private fun mapV1ToV3ReplyModel(
        communicationReplyPayloadV1ErpModel: CommunicationReplyPayloadV1ErpModel
    ): CommunicationPayloadErpModel? {
        val pickUpCodeHR = communicationReplyPayloadV1ErpModel.pickUpCodeHR
        val pickUpCodeDMC = communicationReplyPayloadV1ErpModel.pickUpCodeDMC
        val url = communicationReplyPayloadV1ErpModel.url
        val infoText = communicationReplyPayloadV1ErpModel.infoText

        return when {
            // TODO CommResV3 When there is more than 1 unique Element, don't return the Version1.
            //  Split into two Messages so it is able to be shown in V3 Design
            listOfNotNull(pickUpCodeHR, pickUpCodeDMC, url).size > 1 -> communicationReplyPayloadV1ErpModel
            pickUpCodeHR != null -> CommunicationReplyPickupCodeHRPayloadErpModel(
                version = 1,
                transactionID = "",
                pickUpCode = pickUpCodeHR,
                text = infoText
            )

            pickUpCodeDMC != null -> CommunicationReplyPickupCodeDMCPayloadErpModel(
                version = 1,
                transactionID = "",
                pickUpCodeDmc = pickUpCodeDMC,
                text = infoText
            )

            url != null -> CommunicationReplyLinkPayloadErpModel(
                version = 1,
                transactionID = "",
                text = infoText ?: "",
                url = url
            )

            infoText != null -> CommunicationReplyTextPayloadErpModel(
                version = 1,
                transactionID = "",
                text = infoText
            )

            else -> communicationReplyPayloadV1ErpModel
        }
    }

    private fun parseVersion3(jsonObject: JsonObject, isRequest: Boolean): CommunicationPayloadErpModel? {
        val deserializer = selectV3Deserializer(jsonObject, isRequest) ?: return null
        return SafeJson.value.decodeFromJsonElement(deserializer, jsonObject)
    }

    private fun selectV3Deserializer(
        jsonObject: JsonObject,
        isRequest: Boolean
    ): DeserializationStrategy<CommunicationPayloadErpModel>? {
        val typeStr = jsonObject["communicationType"]?.jsonPrimitive?.content?.lowercase()
        return when (typeStr) {
            "order" -> selectV3OrderDeserializer(jsonObject)
            "text" -> {
                if (isRequest || jsonObject.containsKey("phone")) {
                    InfoAvailabilityRequestPayloadErpModel.serializer()
                } else {
                    CommunicationReplyTextPayloadErpModel.serializer()
                }
            }

            "link" -> CommunicationReplyLinkPayloadErpModel.serializer()
            "reservationstatus", "reservation_status" -> CommunicationReplyReservationStatusPayloadErpModel.serializer()
            "pickupcodehr", "pickup_code_hr" -> CommunicationReplyPickupCodeHRPayloadErpModel.serializer()
            "pickupcodedmc", "pickup_code_dmc" -> CommunicationReplyPickupCodeDMCPayloadErpModel.serializer()
            "deliverystatus", "delivery_status" -> CommunicationReplyDeliveryStatusPayloadErpModel.serializer()
            "paymentinfo", "payment_info" -> CommunicationReplyPaymentInfoPayloadErpModel.serializer()
            else -> {
                val communicationType = jsonObject["communicationType"]?.jsonPrimitive?.let {
                    runCatching { SafeJson.value.decodeFromJsonElement<CommunicationTypeErpModel>(it) }.getOrNull()
                }
                when (communicationType) {
                    CommunicationTypeErpModel.Order -> selectV3OrderDeserializer(jsonObject)
                    CommunicationTypeErpModel.Text -> {
                        if (isRequest || jsonObject.containsKey("phone")) {
                            InfoAvailabilityRequestPayloadErpModel.serializer()
                        } else {
                            CommunicationReplyTextPayloadErpModel.serializer()
                        }
                    }

                    CommunicationTypeErpModel.Link -> CommunicationReplyLinkPayloadErpModel.serializer()
                    CommunicationTypeErpModel.ReservationStatus -> CommunicationReplyReservationStatusPayloadErpModel.serializer()
                    CommunicationTypeErpModel.PickUpCodeHR -> CommunicationReplyPickupCodeHRPayloadErpModel.serializer()
                    CommunicationTypeErpModel.PickUpCodeDMC -> CommunicationReplyPickupCodeDMCPayloadErpModel.serializer()
                    CommunicationTypeErpModel.DeliveryStatus -> CommunicationReplyDeliveryStatusPayloadErpModel.serializer()
                    CommunicationTypeErpModel.PaymentInfo -> CommunicationReplyPaymentInfoPayloadErpModel.serializer()
                    else -> {
                        Napier.w("Unknown v3 communication type: $typeStr")
                        null
                    }
                }
            }
        }
    }

    private fun selectV3OrderDeserializer(
        jsonObject: JsonObject
    ): DeserializationStrategy<CommunicationPayloadErpModel>? {
        val supplyOptionsType = jsonObject["supplyOptionsType"]?.jsonPrimitive?.let {
            runCatching { SafeJson.value.decodeFromJsonElement<CommunicationSupplyOptionTypeErpModel>(it) }.getOrNull()
        }
        return when (supplyOptionsType) {
            CommunicationSupplyOptionTypeErpModel.ON_PREMISE -> DispenseRequestReservationPayloadErpModel.serializer()
            CommunicationSupplyOptionTypeErpModel.DELIVERY -> DispenseRequestDeliveryPayloadErpModel.serializer()
            CommunicationSupplyOptionTypeErpModel.SHIPMENT -> DispenseRequestShipmentPayloadErpModel.serializer()
            else -> null
        }
    }
}
