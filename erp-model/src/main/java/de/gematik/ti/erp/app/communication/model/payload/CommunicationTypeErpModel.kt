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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = CommunicationTypeSerializer::class)
enum class CommunicationTypeErpModel {
    Order,
    Text,
    Link,
    ReservationStatus,
    PickUpCodeHR,
    PickUpCodeDMC,
    DeliveryStatus,
    PaymentInfo,
    Unknown
    ;
}

object CommunicationTypeSerializer : KSerializer<CommunicationTypeErpModel> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CommunicationTypeErpModel", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: CommunicationTypeErpModel) {
        val serialName = when (value) {
            CommunicationTypeErpModel.Order -> "order"
            CommunicationTypeErpModel.Text -> "text"
            CommunicationTypeErpModel.Link -> "link"
            CommunicationTypeErpModel.ReservationStatus -> "reservationStatus"
            CommunicationTypeErpModel.PickUpCodeHR -> "pickUpCodeHR"
            CommunicationTypeErpModel.PickUpCodeDMC -> "pickUpCodeDMC"
            CommunicationTypeErpModel.DeliveryStatus -> "deliveryStatus"
            CommunicationTypeErpModel.PaymentInfo -> "paymentInfo"
            CommunicationTypeErpModel.Unknown -> "unknown"
        }
        encoder.encodeString(serialName)
    }

    override fun deserialize(decoder: Decoder): CommunicationTypeErpModel {
        val str = decoder.decodeString().lowercase()
        return when (str) {
            "order" -> CommunicationTypeErpModel.Order
            "text" -> CommunicationTypeErpModel.Text
            "link" -> CommunicationTypeErpModel.Link
            "reservationstatus", "reservation_status" -> CommunicationTypeErpModel.ReservationStatus
            "pickupcodehr", "pickup_code_hr" -> CommunicationTypeErpModel.PickUpCodeHR
            "pickupcodedmc", "pickup_code_dmc" -> CommunicationTypeErpModel.PickUpCodeDMC
            "deliverystatus", "delivery_status" -> CommunicationTypeErpModel.DeliveryStatus
            "paymentinfo", "payment_info" -> CommunicationTypeErpModel.PaymentInfo
            else -> CommunicationTypeErpModel.Unknown
        }
    }
}
