/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.orders.mappers

import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.pharmacy.repository.model.CommunicationPayloadInbox
import de.gematik.ti.erp.app.prescription.model.Communication
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.net.URI

private val lenientJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

fun Communication.toOrder(
    prescriptions: List<Prescription?>,
    hasUnreadMessages: Boolean,
    taskIds: List<String>,
    pharmacyName: String?
) =
    OrderUseCaseData.Order(
        orderId = orderId,
        taskIds = taskIds,
        prescriptions = prescriptions,
        sentOn = sentOn,
        pharmacy = OrderUseCaseData.Pharmacy(name = pharmacyName ?: "", id = this.recipient),
        hasUnreadMessages = hasUnreadMessages
    )

fun Communication.toOrderDetail(
    hasUnreadMessages: Boolean,
    taskDetailedBundles: List<OrderUseCaseData.TaskDetailedBundle>,
    pharmacyName: String?
) =
    OrderUseCaseData.OrderDetail(
        orderId = orderId,
        taskDetailedBundles = taskDetailedBundles,
        sentOn = sentOn,
        pharmacy = OrderUseCaseData.Pharmacy(name = pharmacyName ?: "", id = this.recipient),
        hasUnreadMessages = hasUnreadMessages
    )

fun Communication.toMessage(hasInvoice: Boolean = false) =
    payload?.let {
        try {
            val inbox = lenientJson.decodeFromString<CommunicationPayloadInbox>(it)

            OrderUseCaseData.Message(
                communicationId = communicationId,
                sentOn = sentOn,
                message = inbox.infoText?.ifBlank { null },
                code = inbox.pickUpCodeDMC?.ifBlank { null } ?: inbox.pickUpCodeHR?.ifBlank { null },
                link = inbox.url?.ifBlank { null }?.takeIf { isValidUrl(it) },
                consumed = consumed,
                hasInvoice = hasInvoice
            )
        } catch (ignored: SerializationException) {
            OrderUseCaseData.Message(
                communicationId = communicationId,
                sentOn = sentOn,
                message = null,
                code = null,
                link = null,
                consumed = consumed,
                hasInvoice = hasInvoice
            )
        }
    } ?: OrderUseCaseData.Message(
        communicationId = communicationId,
        sentOn = sentOn,
        message = null,
        code = null,
        link = null,
        consumed = consumed,
        hasInvoice = hasInvoice
    )

/**
 * Every url should be valid and the scheme is `https`.
 */
fun isValidUrl(url: String): Boolean =
    try {
        URI.create(url).scheme == "https"
    } catch (_: IllegalArgumentException) {
        false
    }
