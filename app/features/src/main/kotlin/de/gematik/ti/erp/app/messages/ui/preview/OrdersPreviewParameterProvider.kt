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

package de.gematik.ti.erp.app.messages.ui.preview

import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import kotlinx.datetime.Instant

private val PREVIEW_PRESCRIPTION = Prescription.ScannedPrescription(
    taskId = "123",
    name = "Prescription",
    redeemedOn = Instant.parse("2023-07-08T15:20:00Z"),
    scannedOn = Instant.parse("2023-07-08T15:20:00Z"),
    index = 0,
    communications = emptyList()
)

private val PREVIEW_ORDER_1 = OrderUseCaseData.Order(
    orderId = "2",
    sentOn = Instant.parse("2023-07-03T15:20:00Z"),
    hasUnreadMessages = true,
    pharmacy = OrderUseCaseData.Pharmacy("123", "Apotheke"),
    prescriptions = emptyList(),
    latestCommunicationMessage = mockLastMessage
)

private val PREVIEW_ORDER_2 = OrderUseCaseData.Order(
    orderId = "2",
    sentOn = Instant.parse("2023-07-04T15:20:00Z"),
    hasUnreadMessages = true,
    pharmacy = OrderUseCaseData.Pharmacy("123", "Flughafen"),
    prescriptions = emptyList(),
    latestCommunicationMessage = mockLastMessageLong
)

private val PREVIEW_ORDER_3 = OrderUseCaseData.Order(
    orderId = "3",
    sentOn = Instant.parse("2023-07-05T15:20:00Z"),
    hasUnreadMessages = false,
    pharmacy = OrderUseCaseData.Pharmacy("123", "TaxiStand"),
    prescriptions = listOf(PREVIEW_PRESCRIPTION),
    latestCommunicationMessage = null
)

private val PREVIEW_ORDER_4 = OrderUseCaseData.Order(
    orderId = "4",
    sentOn = Instant.parse("2023-07-06T15:20:00Z"),
    hasUnreadMessages = true,
    pharmacy = OrderUseCaseData.Pharmacy("123", "HauptBahnhof"),
    prescriptions = listOf(PREVIEW_PRESCRIPTION),
    latestCommunicationMessage = mockLastMessage
)

private val PREVIEW_ORDER_5 = OrderUseCaseData.Order(
    orderId = "5",
    sentOn = Instant.parse("2023-07-08T15:20:00Z"),
    hasUnreadMessages = false,
    pharmacy = OrderUseCaseData.Pharmacy("123", "BusStation"),
    prescriptions = listOf(
        PREVIEW_PRESCRIPTION,
        PREVIEW_PRESCRIPTION.copy(index = 1),
        PREVIEW_PRESCRIPTION.copy(index = 2)
    ),
    latestCommunicationMessage = mockLastMessageLong
)
