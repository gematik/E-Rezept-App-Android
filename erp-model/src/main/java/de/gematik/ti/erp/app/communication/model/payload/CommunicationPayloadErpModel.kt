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

package de.gematik.ti.erp.app.communication.model.payload

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames

@Serializable
sealed interface CommunicationPayloadErpModel {
    val version: Int
    val communicationType: CommunicationTypeErpModel?
    val transactionID: String?
}

@Serializable
data class CommunicationReplyPayloadV1ErpModel(
    override val version: Int = 1,
    @Transient
    override val communicationType: CommunicationTypeErpModel? = null,
    @Transient
    override val transactionID: String? = null,
    val supplyOptionsType: CommunicationSupplyOptionTypeErpModel = CommunicationSupplyOptionTypeErpModel.ON_PREMISE,
    @SerialName("info_text") val infoText: String? = null,
    val url: String? = null,
    val pickUpCodeHR: String? = null,
    val pickUpCodeDMC: String? = null
) : CommunicationPayloadErpModel

@Serializable
data class DispenseRequestCommunicationPayloadV1ErpModel(
    override val version: Int = 1,
    @Transient
    override val communicationType: CommunicationTypeErpModel? = null,
    @Transient
    override val transactionID: String? = null,
    val supplyOptionsType: CommunicationSupplyOptionTypeErpModel = CommunicationSupplyOptionTypeErpModel.ON_PREMISE,
    val name: String? = null,
    val address: List<String>? = null,
    val hint: String = "",
    val phone: String? = null
) : CommunicationPayloadErpModel

/**
 * Dispense request payload for Communication Payload Version 3 ("v3").
 *
 * Differences to [DispenseRequestCommunicationPayloadV1ErpModel]:
 * - `name` is split into [firstname]/[lastname] (max 45 chars each).
 * - `address` is a plain street + house number String (3-100 chars) instead of a full
 *   address array; [postcode] (3-10 chars), [city] (2-100 chars) and [country]
 *   (ISO-3166-1 alpha-2, e.g. pattern `[A-Z]{2}`) are now separate fields.
 * - [hint] gets a max length of 100 chars.
 * - New optional [text] field (max 800 chars).
 * - [phone] gets a max length of 32 chars.
 */
@Serializable
data class DispenseRequestCommunicationPayloadV3ErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.Order,
    override val transactionID: String = "",
    val supplyOptionsType: CommunicationSupplyOptionTypeErpModel = CommunicationSupplyOptionTypeErpModel.ON_PREMISE,
    val firstname: String? = null,
    val lastname: String? = null,
    val address: String? = null,
    val postcode: String? = null,
    val city: String? = null,
    val country: String? = null,
    val hint: String? = null,
    val phone: String = "",
    val text: String? = null,
    @SerialName("email")
    val mail: String? = null
) : CommunicationPayloadErpModel

@Serializable
data class DispenseRequestReservationPayloadErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.Order,
    override val transactionID: String = "",
    val supplyOptionsType: CommunicationSupplyOptionTypeErpModel = CommunicationSupplyOptionTypeErpModel.ON_PREMISE,

    val firstname: String? = null,
    val lastname: String? = null,
    val address: String? = null,
    val postcode: String? = null,
    val city: String? = null,
    val country: String? = null,
    val hint: String? = null,

    val phone: String = "",
    val text: String = "",
    @SerialName("email")
    val mail: String? = null
) : CommunicationPayloadErpModel

@Serializable
data class DispenseRequestDeliveryPayloadErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.Order,
    override val transactionID: String = "",
    val supplyOptionsType: CommunicationSupplyOptionTypeErpModel = CommunicationSupplyOptionTypeErpModel.DELIVERY,

    val firstname: String? = null,
    val lastname: String? = null,
    val address: String? = null,
    val postcode: String? = null,
    val city: String? = null,
    val country: String? = null,
    val hint: String? = null,

    val phone: String = "",
    val text: String = "",
    @SerialName("email")
    val mail: String? = null
) : CommunicationPayloadErpModel

@Serializable
data class DispenseRequestShipmentPayloadErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.Order,
    override val transactionID: String = "",
    val supplyOptionsType: CommunicationSupplyOptionTypeErpModel = CommunicationSupplyOptionTypeErpModel.SHIPMENT,

    val firstname: String? = null,
    val lastname: String? = null,
    val address: String? = null,
    val postcode: String? = null,
    val city: String? = null,
    val country: String? = null,
    val hint: String? = null,

    val phone: String = "",
    val text: String = "",
    @SerialName("email")
    val mail: String? = null
) : CommunicationPayloadErpModel

@Serializable
data class InfoAvailabilityRequestPayloadErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.Text,
    override val transactionID: String = "",

    val firstname: String? = null,
    val lastname: String? = null,
    val address: String? = null,
    val postcode: String? = null,
    val city: String? = null,
    val country: String? = null,

    val phone: String = "",
    val text: String = "",
    @SerialName("email")
    val mail: String? = null
) : CommunicationPayloadErpModel

@Serializable
data class CommunicationReplyTextPayloadErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.Text,
    override val transactionID: String = "",

    val text: String = ""
) : CommunicationPayloadErpModel

@Serializable
data class CommunicationReplyLinkPayloadErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.Link,
    override val transactionID: String = "",

    val text: String = "",
    val url: String = ""
) : CommunicationPayloadErpModel

@Serializable
data class CommunicationReplyReservationStatusPayloadErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.ReservationStatus,
    override val transactionID: String = "",

    val readyForCollection: CommunicationAvailabilityResponseErpModel = CommunicationAvailabilityResponseErpModel.Unknown
) : CommunicationPayloadErpModel

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CommunicationReplyPickupCodeHRPayloadErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.PickUpCodeHR,
    override val transactionID: String = "",
    @SerialName("pickupCodeHR")
    @JsonNames("pickupCodeHR", "pickUpCodeHR", "pickUpCode")
    val pickUpCode: String = "",
    val text: String? = null
) : CommunicationPayloadErpModel

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CommunicationReplyPickupCodeDMCPayloadErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.PickUpCodeDMC,
    override val transactionID: String = "",
    @SerialName("pickupCodeDMC")
    @JsonNames("pickupCodeDMC", "pickUpCodeDMC", "pickUpCodeDmc")
    val pickUpCodeDmc: String = "",
    val text: String? = null
) : CommunicationPayloadErpModel

@Serializable
data class CommunicationReplyDeliveryStatusPayloadErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.DeliveryStatus,
    override val transactionID: String = "",
    val deliveryStatus: CommunicationDeliveryStatusErpModel = CommunicationDeliveryStatusErpModel.Unknown,
    val inTransportPosition: InTransportPosition? = null,
    val inTransportETA: InTransportETA? = null,
    val text: String? = null
) : CommunicationPayloadErpModel

@Serializable
data class CommunicationReplyPaymentInfoPayloadErpModel(
    override val version: Int = 3,
    override val communicationType: CommunicationTypeErpModel = CommunicationTypeErpModel.PaymentInfo,
    override val transactionID: String = "",
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val totalAmount: Double = 0.0,
    val text: String? = null
) : CommunicationPayloadErpModel

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PaymentMethod(
    @SerialName("method")
    @JsonNames("method", "paymentMethod")
    val paymentMethod: String? = null,
    val url: String? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class InTransportPosition(
    @SerialName("lat")
    @JsonNames("lat", "latitude")
    val latitude: Double? = null,

    @SerialName("long")
    @JsonNames("long", "longitude", "lng", "lon")
    val longitude: Double? = null
)

@Serializable
data class InTransportETA(
    val from: Long? = null,
    val to: Long? = null
)
