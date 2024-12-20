/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.messages.domain.usecase

import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.mappers.toMessage
import de.gematik.ti.erp.app.messages.repository.CachedPharmacy
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.prescription.model.Communication
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class GetMessagesUseCase(
    private val communicationRepository: CommunicationRepository,
    private val invoiceRepository: InvoiceRepository,
    private val profileRepository: ProfileRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): List<OrderUseCaseData.Order> = withContext(dispatcher) {
        val profiles = profileRepository.profiles().first()
        val pharmacies: List<CachedPharmacy> = communicationRepository.loadPharmacies().first()

        profiles.flatMap { profile ->
            communicationRepository.loadFirstDispReqCommunications(profile.id).first()
                .groupBy { it.taskId to it.orderId to it.recipient to it.payload }
                .map { (_, communications) ->
                    val latestCommunication = communications.maxByOrNull { it.sentOn }
                    latestCommunication?.let {
                        mapCommunicationToOrder(it, pharmacies)
                    }
                }
                .filterNotNull()
        }.sortedByDescending { it.sentOn }
    }

    private suspend fun mapCommunicationToOrder(
        communication: Communication,
        pharmacies: List<CachedPharmacy>
    ): OrderUseCaseData.Order {
        val pharmacyName = pharmacies.getPharmacyName(communication)
        val (prescriptions, hasUnreadMessages) = communication.dispenseRequestCommunicationToOrder(
            communicationRepository = communicationRepository,
            invoiceRepository = invoiceRepository,
            withMedicationNames = true,
            dispatcher = dispatcher
        ).first()

        return OrderUseCaseData.Order(
            orderId = communication.orderId,
            prescriptions = prescriptions,
            sentOn = communication.sentOn,
            pharmacy = OrderUseCaseData.Pharmacy(
                id = communication.recipient, // getting Pharmacy ID (telematikId) from communication.recipient
                name = pharmacyName
            ),
            hasUnreadMessages = hasUnreadMessages,
            latestCommunicationMessage = getLatestCommunicationMessage(
                communication.orderId,
                pharmacyName,
                communication.recipient
            )
        )
    }

    private suspend fun List<CachedPharmacy>.getPharmacyName(communication: Communication): String {
        return try {
            // Try to find the pharmacy in the existing list
            val pharmacy = this.find { it.telematikId == communication.recipient }

            // If the pharmacy is found, return its name; otherwise, attempt to download and find it again
            pharmacy?.name ?: run {
                communicationRepository.downloadMissingPharmacy(communication.recipient).getOrNull()?.name ?: ""
            }
        } catch (e: Throwable) {
            Napier.e { "error on getting pharmacy name ${e.message}" }
            // Return empty string if any exception occurs
            ""
        }
    }

    private suspend fun getLatestCommunicationMessage(orderId: String, pharmacyName: String, telematikId: String): OrderUseCaseData.LastMessage? =
        supervisorScope {
            val taskIds = communicationRepository.taskIdsByOrder(orderId).first()

            return@supervisorScope combine(
                communicationRepository.loadRepliedCommunications(taskIds, telematikId),
                communicationRepository.loadDispReqCommunications(orderId)
            ) { repliedCommunications, dispReqCommunications ->
                repliedCommunications + dispReqCommunications
            }.mapNotNull { combinedCommunication ->
                val lastMessage = combinedCommunication.maxByOrNull { it.sentOn }
                lastMessage?.let {
                    it.generateQuickMessage(pharmacyName)?.let { lastMessage ->
                        OrderUseCaseData.LastMessage(
                            lastMessageDetails = lastMessage,
                            profile = it.profile
                        )
                    }
                }
            }.firstOrNull()
        }

    private fun Communication?.generateQuickMessage(pharmacyName: String): OrderUseCaseData.LastMessageDetails? {
        return when (this?.profile) {
            CommunicationProfile.ErxCommunicationDispReq -> OrderUseCaseData.LastMessageDetails(
                message = pharmacyName,
                pickUpCodeDMC = null,
                pickUpCodeHR = null,
                link = null
            )

            CommunicationProfile.ErxCommunicationReply -> {
                val messageData = this.toMessage()
                OrderUseCaseData.LastMessageDetails(
                    message = messageData.message,
                    pickUpCodeDMC = messageData.pickUpCodeDMC,
                    pickUpCodeHR = messageData.pickUpCodeHR,
                    link = messageData.link
                )
            }

            else -> null
        }
    }
}
