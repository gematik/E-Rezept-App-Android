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

package de.gematik.ti.erp.app.orders.usecase

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.orders.repository.CommunicationRepository
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.pharmacy.repository.model.CommunicationPayloadInbox
import de.gematik.ti.erp.app.prescription.mapper.toPrescription
import de.gematik.ti.erp.app.prescription.model.Communication
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.net.URI

@OptIn(ExperimentalCoroutinesApi::class)
class OrderUseCase(
    private val repository: CommunicationRepository,
    private val dispatchers: DispatchProvider
) {
    fun orders(profileIdentifier: ProfileIdentifier): Flow<List<OrderUseCaseData.Order>> =
        combine(
            repository.loadFirstDispReqCommunications(profileIdentifier),
            repository.loadPharmacies()
        ) { communications, pharmacies ->
            communications.map { communication ->
                dispReqCommunicationToOrder(
                    communication = communication,
                    withMedicationNames = false,
                    pharmacyName = pharmacies.find { it.telematikId == communication.recipient }?.name
                )
            }
        }

    fun order(orderId: String): Flow<OrderUseCaseData.Order?> =
        combine(
            repository.loadDispReqCommunications(orderId),
            repository.loadPharmacies()
        ) { communications, pharmacies ->
            communications.firstOrNull()?.let { communication ->
                dispReqCommunicationToOrder(
                    communication = communication,
                    withMedicationNames = true,
                    pharmacyName = pharmacies.find { it.telematikId == communication.recipient }?.name
                )
            }
        }

    private suspend fun dispReqCommunicationToOrder(
        communication: Communication,
        withMedicationNames: Boolean,
        pharmacyName: String?
    ): OrderUseCaseData.Order {
        val taskIds = repository.taskIdsByOrder(communication.orderId).first()
        val hasUnreadMessages = repository.hasUnreadPrescription(taskIds, communication.orderId).first()
        val prescriptions = if (withMedicationNames) {
            taskIds.map {
                repository.loadSyncedByTaskId(it).first()?.toPrescription()
                    ?: repository.loadScannedByTaskId(it).first()?.toPrescription()
            }
        } else {
            emptyList()
        }

        if (pharmacyName == null) {
            repository.downloadMissingPharmacy(communication.recipient)
        }

        return communication.toOrder(
            prescriptions = prescriptions,
            hasUnreadMessages = hasUnreadMessages,
            taskIds = taskIds,
            pharmacyName = pharmacyName
        )
    }

    fun messages(
        orderId: String
    ): Flow<List<OrderUseCaseData.Message>> =
        repository.taskIdsByOrder(orderId).flatMapLatest {
            repository.loadRepliedCommunications(taskIds = it)
                .map { communications ->
                    communications.map { it.toMessage() }
                }
        }

    fun unreadPrescriptionAvailable(profileId: ProfileIdentifier) =
        repository.hasUnreadPrescription(profileId).flowOn(dispatchers.io)

    fun unreadOrders(profile: ProfilesUseCaseData.Profile) =
        repository.unreadOrders(profile.id).flowOn(dispatchers.io)
    fun unreadPrescriptionsInAllOrders(profileId: ProfileIdentifier) =
        repository.unreadPrescriptionsInAllOrders(profileId).flowOn(dispatchers.io)

    suspend fun consumeCommunication(communicationId: String) {
        withContext(dispatchers.io) {
            repository.setCommunicationStatus(communicationId, true)
        }
    }

    suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String) {
        repository.saveLocalCommunication(taskId, pharmacyId, transactionId)
    }

    suspend fun consumeOrder(orderId: String) {
        withContext(dispatchers.io) {
            repository.loadDispReqCommunications(orderId).first().forEach {
                repository.setCommunicationStatus(it.communicationId, true)
            }
        }
    }
}

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

fun Communication.toMessage() =
    payload?.let {
        try {
            val inbox = lenientJson.decodeFromString<CommunicationPayloadInbox>(it)

            OrderUseCaseData.Message(
                communicationId = communicationId,
                sentOn = sentOn,
                message = inbox.infoText?.ifBlank { null },
                code = inbox.pickUpCodeDMC?.ifBlank { null } ?: inbox.pickUpCodeHR?.ifBlank { null },
                link = inbox.url?.ifBlank { null }?.takeIf { isValidUrl(it) },
                consumed = consumed
            )
        } catch (ignored: SerializationException) {
            OrderUseCaseData.Message(
                communicationId = communicationId,
                sentOn = sentOn,
                message = null,
                code = null,
                link = null,
                consumed = consumed
            )
        }
    } ?: OrderUseCaseData.Message(
        communicationId = communicationId,
        sentOn = sentOn,
        message = null,
        code = null,
        link = null,
        consumed = consumed
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
