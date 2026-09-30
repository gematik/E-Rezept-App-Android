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

package de.gematik.ti.erp.app.fhir.communication.mocks

import de.gematik.ti.erp.app.communication.model.payload.CommunicationAvailabilityResponseErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationDeliveryStatusErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyLinkPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPaymentInfoPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeDMCPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeHRPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyReservationStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyTextPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestCommunicationPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestDeliveryPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestReservationPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestShipmentPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.InTransportETA
import de.gematik.ti.erp.app.communication.model.payload.InTransportPosition
import de.gematik.ti.erp.app.communication.model.payload.InfoAvailabilityRequestPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.PaymentMethod

object FhirCommunicationPayloadTestData {
    private const val TRANSACTION_ID = "ABCD-EFGH-IJKL-MNOP"
    private const val PHONE = "+49555555555"

    val dispenseRequestDeliveryFullPayloadV1 = DispenseRequestCommunicationPayloadV1ErpModel(
        supplyOptionsType = CommunicationSupplyOptionTypeErpModel.DELIVERY,
        name = "Max Mustermann",
        address = listOf(
            "Musterstraße 555",
            "55555 Musterhause",
            "DE"
        ),
        hint = "EG",
        phone = PHONE
    )

    val dispenseRequestDeliveryMinimalPayloadV1 = DispenseRequestCommunicationPayloadV1ErpModel(
        supplyOptionsType = CommunicationSupplyOptionTypeErpModel.DELIVERY,
        name = "Max Mustermann",
        address = listOf(
            "Musterstraße 555",
            "55555 Musterhause",
            "DE"
        )
    )

    val communicationReplyFullPayloadV1 = CommunicationReplyPayloadV1ErpModel(
        supplyOptionsType = CommunicationSupplyOptionTypeErpModel.ON_PREMISE,
        infoText = "Hey patient, how are you? does the medicine takes an effect??",
        url = "https://example.com",
        pickUpCodeHR = "0815",
        pickUpCodeDMC = "MACHINE_READABLE_CONTENT_OR_STRUCTURED_DATA"
    )

    val communicationReplyPickupCodeHrAndLinkPayloadV1 = CommunicationReplyPayloadV1ErpModel(
        supplyOptionsType = CommunicationSupplyOptionTypeErpModel.ON_PREMISE,
        infoText = "Hey patient, how are you? does the medicine takes an effect??",
        url = "https://example.com",
        pickUpCodeHR = "0815",
        pickUpCodeDMC = null
    )

    val communicationReplyMinimalPayloadV1 = CommunicationReplyTextPayloadErpModel(
        version = 1,
        transactionID = "",
        text = "Hey patient, how are you? does the medicine takes an effect??"
    )

    val communicationReplyWithoutSupplyOptionsTypePayloadV1 = CommunicationReplyPickupCodeHRPayloadErpModel(
        version = 1,
        transactionID = "",
        pickUpCode = "0815",
        text = "Hey patient, how are you? does the medicine takes an effect??"
    )

    // v3

    val dispenseRequestDeliveryFullPayloadV3 = DispenseRequestDeliveryPayloadErpModel(
        transactionID = TRANSACTION_ID,
        phone = PHONE,
        text = "Need it now!",
        mail = "max.mustermann@mail.de",
        firstname = "Max",
        lastname = "Mustermann",
        address = "Musterstraße 555",
        postcode = "55555",
        city = "Musterhause",
        country = "DE",
        hint = "EG"
    )

    val dispenseRequestDeliveryMinimalPayloadV3 = DispenseRequestDeliveryPayloadErpModel(
        transactionID = TRANSACTION_ID,
        phone = PHONE,
        text = "Need it now!",
        firstname = "Max",
        lastname = "Mustermann",
        address = "Musterstraße 555",
        postcode = "55555",
        city = "Musterhause",
        country = "DE"
    )

    val dispenseRequestShipmentFullPayloadV3 = DispenseRequestShipmentPayloadErpModel(
        transactionID = TRANSACTION_ID,
        phone = PHONE,
        text = "Need it now!",
        mail = "max.mustermann@mail.de",
        firstname = "Max",
        lastname = "Mustermann",
        address = "Musterstraße 555",
        postcode = "55555",
        city = "Musterhause",
        country = "DE",
        hint = "EG"
    )

    val dispenseRequestShipmentMinimalPayloadV3 = DispenseRequestShipmentPayloadErpModel(
        transactionID = TRANSACTION_ID,
        phone = PHONE,
        text = "Need it now!",
        firstname = "Max",
        lastname = "Mustermann",
        address = "Musterstraße 555",
        postcode = "55555",
        city = "Musterhause",
        country = "DE"
    )

    val dispenseRequestReservationFullPayloadV3 = DispenseRequestReservationPayloadErpModel(
        transactionID = TRANSACTION_ID,
        phone = PHONE,
        text = "Need it now!",
        mail = "max.mustermann@mail.de",
        firstname = "Max",
        lastname = "Mustermann",
        address = "Musterstraße 555",
        postcode = "55555",
        city = "Musterhause",
        country = "DE"
    )

    val dispenseRequestReservationMinimalPayloadV3 = DispenseRequestReservationPayloadErpModel(
        transactionID = TRANSACTION_ID,
        phone = PHONE,
        text = "Need it now!"
    )

    val infoRequestFullPayloadV3 = InfoAvailabilityRequestPayloadErpModel(
        transactionID = TRANSACTION_ID,
        phone = PHONE,
        text = "Gibt es noch Traubenzucker?",
        mail = "max.mustermann@mail.de",
        firstname = "Max",
        lastname = "Mustermann",
        address = "Musterstraße 555",
        postcode = "55555",
        city = "Musterhause",
        country = "DE"
    )

    val infoRequestMinimalPayloadV3 = InfoAvailabilityRequestPayloadErpModel(
        transactionID = TRANSACTION_ID,
        phone = PHONE,
        text = "Gibt es noch Traubenzucker?"
    )

    val replyTextFullPayloadV3 = CommunicationReplyTextPayloadErpModel(
        transactionID = TRANSACTION_ID,
        text = "Vielen Dank für Ihre Bestellung!"
    )

    val replyLinkFullPayloadV3 = CommunicationReplyLinkPayloadErpModel(
        transactionID = TRANSACTION_ID,
        text = "Hier finden sie Ihren Warenkorb.",
        url = "https://example.com"
    )

    val replyReservationStatusImmediatelyFullPayloadV3 = CommunicationReplyReservationStatusPayloadErpModel(
        transactionID = TRANSACTION_ID,
        readyForCollection = CommunicationAvailabilityResponseErpModel.Immediately
    )

    val replyReservationStatusNextDayFullPayloadV3 = CommunicationReplyReservationStatusPayloadErpModel(
        transactionID = TRANSACTION_ID,
        readyForCollection = CommunicationAvailabilityResponseErpModel.NextDay
    )

    val replyPickupCodeFullPayloadV3 = CommunicationReplyPickupCodeHRPayloadErpModel(
        transactionID = TRANSACTION_ID,
        pickUpCode = "0815",
        text = "Some Text to state your request"
    )

    val replyPickupCodeMinimalPayloadV3 = CommunicationReplyPickupCodeHRPayloadErpModel(
        transactionID = TRANSACTION_ID,
        pickUpCode = "0815"
    )

    val replyPickupCodeDmcFullPayloadV3 = CommunicationReplyPickupCodeDMCPayloadErpModel(
        transactionID = TRANSACTION_ID,
        pickUpCodeDmc = "MACHINE_READABLE_CONTENT_OR_STRUCTURED_DATA",
        text = "Some Text to state your request"
    )

    val replyPickupCodeDmcMinimalPayloadV3 = CommunicationReplyPickupCodeDMCPayloadErpModel(
        transactionID = TRANSACTION_ID,
        pickUpCodeDmc = "MACHINE_READABLE_CONTENT_OR_STRUCTURED_DATA"
    )

    val replyDeliveryStatusFullPayloadV3 = CommunicationReplyDeliveryStatusPayloadErpModel(
        transactionID = TRANSACTION_ID,
        deliveryStatus = CommunicationDeliveryStatusErpModel.InTransport,
        inTransportETA = InTransportETA(
            from = 1735736400,
            to = 1735741800
        ),
        inTransportPosition = InTransportPosition(
            longitude = 13.387595793605172,
            latitude = 52.522529939635795
        ),
        text = "Some Text to state your request"
    )

    val replyDeliveryStatusMinimalPayloadV3 = CommunicationReplyDeliveryStatusPayloadErpModel(
        transactionID = TRANSACTION_ID,
        deliveryStatus = CommunicationDeliveryStatusErpModel.Incident
    )

    val replyPaymentInfoFullPayloadV3 = CommunicationReplyPaymentInfoPayloadErpModel(
        transactionID = TRANSACTION_ID,
        paymentMethods = listOf(
            PaymentMethod(paymentMethod = "cash"),
            PaymentMethod(
                paymentMethod = "bankaccount",
                url = "https://my.payment.provider.de/pay/<payment_transaction_id>"
            ),
            PaymentMethod(
                paymentMethod = "paypal",
                url = "https://paypal.me/<some_account>"
            )
        ),
        totalAmount = 12530.0,
        text = "Some Text to state your request"
    )

    val replyPaymentInfoMinimalPayloadV3 = CommunicationReplyPaymentInfoPayloadErpModel(
        transactionID = TRANSACTION_ID,
        paymentMethods = emptyList(),
        totalAmount = 12530.0
    )
}
