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

package de.gematik.ti.erp.app.data

// v1

val dispense_request_delivery_full_payload_v1 =
    """
    { 
    "version": 1, 
    "supplyOptionsType": "delivery",
    "name": "Max Mustermann",
    "address": ["Musterstraße 555", "55555 Musterhause", "DE"],
    "hint": "EG", 
    "phone": "+49555555555" 
    }
    """.trimIndent()

val dispense_request_delivery_minimal_payload_v1 =
    """
    { 
    "version": 1, 
    "supplyOptionsType": "delivery",
    "name": "Max Mustermann",
    "address": ["Musterstraße 555", "55555 Musterhause", "DE"]
    }
    """.trimIndent()

val reply_full_payload_v1 = """
    {
    "version":1,
    "supplyOptionsType":"onPremise",
    "info_text":"Hey patient, how are you? does the medicine takes an effect??",
    "url":"https://example.com",
    "pickUpCodeHR":"0815",
    "pickUpCodeDMC":"MACHINE_READABLE_CONTENT_OR_STRUCTURED_DATA"
    }
""".trimIndent()

val reply_pickup_code_hr_and_link_payload_v1 = """
    {
    "version":1,
    "supplyOptionsType":"onPremise",
    "info_text":"Hey patient, how are you? does the medicine takes an effect??",
    "url":"https://example.com",
    "pickUpCodeHR":"0815"
    }
""".trimIndent()

val reply_minimal_payload_v1 = """
    {
    "version":1,
    "supplyOptionsType":"onPremise",
    "info_text":"Hey patient, how are you? does the medicine takes an effect??"
    }
""".trimIndent()

val reply_without_supply_options_type_payload_v1 = """
    {
    "version":1,
    "info_text":"Hey patient, how are you? does the medicine takes an effect??",
    "pickUpCodeHR":"0815"
    }
""".trimIndent()

// Requests

val dispense_request_delivery_full_payload_v3 = """
    {
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "version": 3,
  "communicationType": "order",
  "supplyOptionsType": "delivery",
  "phone": "+49555555555",
  "text": "Need it now!",
  "email": "max.mustermann@mail.de",
  "firstname": "Max",
  "lastname": "Mustermann",
  "address": "Musterstraße 555",
  "postcode": "55555",
  "city": "Musterhause",
  "country": "DE",
  "hint": "EG"
}
""".trimIndent()

val dispense_request_delivery_minimal_payload_v3 = """
    {
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "version": 3,
  "communicationType": "order",
  "supplyOptionsType": "delivery",
  "phone": "+49555555555",
  "text": "Need it now!",
  "firstname": "Max",
  "lastname": "Mustermann",
  "address": "Musterstraße 555",
  "postcode": "55555",
  "city": "Musterhause",
  "country": "DE"
}
""".trimIndent()

val dispense_request_shipment_full_payload_v3 = """
    {
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "version": 3,
  "communicationType": "order",
  "supplyOptionsType": "shipment",
  "phone": "+49555555555",
   "email": "max.mustermann@mail.de",
  "text": "Need it now!",
  "firstname": "Max",
  "lastname": "Mustermann",
  "address": "Musterstraße 555",
  "postcode": "55555",
  "city": "Musterhause",
  "country": "DE",
  "hint": "EG"
}
""".trimIndent()

val dispense_request_shipment_minimal_payload_v3 = """
    {
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "version": 3,
  "communicationType": "order",
  "supplyOptionsType": "shipment",
  "phone": "+49555555555",
  "text": "Need it now!",
  "firstname": "Max",
  "lastname": "Mustermann",
  "address": "Musterstraße 555",
  "postcode": "55555",
  "city": "Musterhause",
  "country": "DE"
}
""".trimIndent()

val dispense_request_reservation_full_payload_v3 = """
    {
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "version": 3,
  "communicationType": "order",
  "supplyOptionsType": "onPremise",
  "phone": "+49555555555",
  "text": "Need it now!",
  "firstname": "Max",
  "lastname": "Mustermann",
  "address": "Musterstraße 555",
  "postcode": "55555",
  "city": "Musterhause",
  "country": "DE",
  "email": "max.mustermann@mail.de"
}
""".trimIndent()

val dispense_request_reservation_minimal_payload_v3 = """
    {
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "version": 3,
  "communicationType": "order",
  "supplyOptionsType": "onPremise",
  "phone": "+49555555555",
  "text": "Need it now!"
}
""".trimIndent()

val info_request_full_payload_v3 = """
    {
  "version": 3,
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "text",
  "phone": "+49555555555",
  "text": "Gibt es noch Traubenzucker?",
  "firstname": "Max",
  "lastname": "Mustermann",
  "address": "Musterstraße 555",
  "postcode": "55555",
  "city": "Musterhause",
  "country": "DE",
  "email": "max.mustermann@mail.de"
}
""".trimIndent()

val info_request_minimal_payload_v3 = """
    {
  "version": 3,
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "text",
  "phone": "+49555555555",
  "text": "Gibt es noch Traubenzucker?"
}
""".trimIndent()

// Replies

val reply_text_full_payload_v3 = """
    {
  "version": 3,
  "text": "Vielen Dank für Ihre Bestellung!",
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "text"
}
""".trimIndent()

val reply_link_full_payload_v3 = """
    {
  "version": 3,
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "link",
  "url": "https://example.com",
  "text": "Hier finden sie Ihren Warenkorb."
}
""".trimIndent()

val reply_reservation_status_immediately_full_payload_v3 = """
    {
  "version": 3,
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "reservationStatus",
  "readyForCollection": "immediately"
}
""".trimIndent()

val reply_reservation_status_next_day_full_payload_v3 = """
    {
  "version": 3,
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "reservationStatus",
  "readyForCollection": "nextDay"
}
""".trimIndent()

val reply_pickup_code_full_payload_v3 = """
    {
  "version": 3,
  "text": "Some Text to state your request",
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "pickUpCodeHR",
  "pickupCodeHR": "0815"
}
""".trimIndent()

val reply_pickup_code_minimal_payload_v3 = """
    {
  "version": 3,
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "pickUpCodeHR",
  "pickupCodeHR": "0815"
}
""".trimIndent()

val reply_pickup_code_dmc_full_payload_v3 = """
    {
  "version": 3,
  "text": "Some Text to state your request",
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "pickUpCodeDMC",
  "pickupCodeDMC": "MACHINE_READABLE_CONTENT_OR_STRUCTURED_DATA"
}
""".trimIndent()

val reply_pickup_code_dmc_minimal_payload_v3 = """
    {
  "version": 3,
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "pickUpCodeDMC",
  "pickupCodeDMC": "MACHINE_READABLE_CONTENT_OR_STRUCTURED_DATA"
}
""".trimIndent()

val reply_delivery_status_full_payload_v3 = """
    {
  "version": 3,
  "text": "Some Text to state your request",
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "deliveryStatus",
  "deliveryStatus": "inTransport",
  "inTransportPosition": {
    "long": 13.387595793605172,
    "lat": 52.522529939635795
  },
  "inTransportETA": {
    "from": 1735736400,
    "to": 1735741800
  }
}
""".trimIndent()

val reply_delivery_status_minimal_payload_v3 = """
    {
  "version": 3,
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "deliveryStatus",
  "deliveryStatus": "incident"
}
""".trimIndent()

val reply_payment_info_full_payload_v3 = """
    {
  "version": 3,
  "text": "Some Text to state your request",
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "paymentInfo",
  "totalAmount": 12530,
  "paymentMethods": [
    {
      "method": "cash"
    },
    {
      "method": "bankaccount",
      "url": "https://my.payment.provider.de/pay/<payment_transaction_id>"
    },
    {
      "method": "paypal",
      "url": "https://paypal.me/<some_account>"
    }
  ]
}
""".trimIndent()

val reply_payment_info_minimal_payload_v3 = """
    {
  "version": 3,
  "transactionID": "ABCD-EFGH-IJKL-MNOP",
  "communicationType": "paymentInfo",
  "totalAmount": 12530,
  "paymentMethods": []
}
""".trimIndent()
