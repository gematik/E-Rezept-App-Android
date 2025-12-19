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

package de.gematik.ti.erp.app.messages.mappers

import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.pharmacy.repository.model.CommunicationPayloadInbox
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val lenientJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

fun Communication.toOrderDetail(
    taskDetailedBundles: List<OrderUseCaseData.TaskDetailedBundle>,
    pharmacyName: String?
) =
    OrderUseCaseData.OrderDetail(
        orderId = orderId,
        taskDetailedBundles = taskDetailedBundles,
        sentOn = sentOn,
        pharmacy = OrderUseCaseData.Pharmacy(name = pharmacyName ?: "", id = this.recipient)
    )

fun Communication.toMessage(): OrderUseCaseData.Message {
    val defaultValues = OrderUseCaseData.Message(
        communicationId = communicationId,
        sentOn = sentOn,
        content = null,
        pickUpCodeDMC = null,
        pickUpCodeHR = null,
        link = null,
        consumed = consumed,
        prescriptions = emptyList<Prescription>(),
        taskIds = taskIds,
        isTaskIdCountMatching = isTaskIdCountMatching
    )

    return payload?.let { nonNullPayload ->
        try {
            val inbox = lenientJson.decodeFromString<CommunicationPayloadInbox>(nonNullPayload)
            OrderUseCaseData.Message(
                communicationId = communicationId,
                sentOn = sentOn,
                content = inbox.infoText?.takeUnless { it.isBlank() },
                pickUpCodeDMC = inbox.pickUpCodeDMC?.takeUnless { it.isBlank() },
                pickUpCodeHR = inbox.pickUpCodeHR?.takeUnless { it.isBlank() },
                link = inbox.url?.takeUnless { it.isBlank() }?.takeIf { isValidUrl(it) },
                consumed = consumed,
                prescriptions = emptyList<Prescription>(),
                taskIds = taskIds,
                isTaskIdCountMatching = isTaskIdCountMatching
            )
        } catch (ignored: SerializationException) {
            Napier.d { "No payload, default message" }
            defaultValues
        }
    } ?: run {
        Napier.d { "No payload, default message" }
        defaultValues
    }
}

/**
 * Check if a URL is valid and uses the HTTPS scheme.
 */
fun isValidUrl(url: String): Boolean =
    url.matches("^https://.*".toRegex())
