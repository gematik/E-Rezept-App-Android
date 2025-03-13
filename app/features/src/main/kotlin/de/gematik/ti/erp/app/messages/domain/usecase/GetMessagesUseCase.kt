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
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.mappers.toMessage
import de.gematik.ti.erp.app.messages.repository.CachedPharmacy
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.prescription.model.Communication
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class GetMessagesUseCase(
    private val communicationRepository: CommunicationRepository,
    private val invoiceRepository: InvoiceRepository,
    private val profileRepository: ProfileRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Executes the use case to load a list of orders by processing profiles and communications.
     *
     * @return A list of [OrderUseCaseData.Order] objects, sorted by the latest sent date and distinct by order ID.
     */
    suspend operator fun invoke(): List<OrderUseCaseData.Order> = withContext(dispatcher) {
        // Fetch all profiles from the repository. `first()` ensures we get the initial value from the flow.
        val profiles = profileRepository.profiles().first()

        // Load all cached pharmacies. `first()` ensures we retrieve the current list from the flow.
        val pharmacies: List<CachedPharmacy> = communicationRepository.loadPharmacies().first()

        // Process each profile to load and transform their communications into orders.
        profiles.flatMap { profile ->
            // Load all DispReq communications for the given profile ID and group them by unique task and order details.
            communicationRepository.loadDispReqCommunicationsByProfileId(profile.id).first()
                .groupBy { it.taskId to it.orderId to it.recipient to it.payload }
                .map { (_, communications) ->
                    // Find the latest communication based on the "sentOn" timestamp.
                    communications.maxByOrNull { it.sentOn }?.let { latestCommunication ->
                        // For the latest communication, fetch the latest replied communication's timestamp.
                        val latestSentOnDate = communicationRepository
                            .loadRepliedCommunications(
                                taskIds = listOf(latestCommunication.taskId),
                                telematikId = latestCommunication.recipient // since this is request message, recipient is pharmacy
                            )
                            .getLatestTimestamp(latestCommunication)
                        mapCommunicationToOrder(
                            communication = latestCommunication,
                            pharmacies = pharmacies,
                            latestMessageSentOnDate = latestSentOnDate ?: latestCommunication.sentOn
                        )
                    }
                }
                .filterNotNull() // Remove any null values from the mapped results.
        }
            // Sort the orders by the "sentOn" timestamp in descending order (most recent first).
            .sortedByDescending { it.sentOn }
            // This step ensures we don't have duplicate orders in the final result.
            .distinctBy { it.orderId } // we need to do this as the last step to ensure that we have orders with latest dates only
    }

    private suspend fun mapCommunicationToOrder(
        communication: Communication,
        latestMessageSentOnDate: Instant,
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
            sentOn = latestMessageSentOnDate,
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

    private suspend fun Flow<List<Communication>>.getLatestTimestamp(
        requestCommunication: Communication
    ): Instant? {
        return firstOrNull()
            ?.maxByOrNull { reply -> reply.sentOn }
            ?.sentOn
            ?.coerceAtLeast(requestCommunication.sentOn) // Ensure we always use the latest date
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
                    it.generatePreviewMessage(pharmacyName)?.let { lastMessage ->
                        OrderUseCaseData.LastMessage(
                            lastMessageDetails = lastMessage,
                            profile = it.profile
                        )
                    }
                }
            }.firstOrNull()
        }

    private fun Communication?.generatePreviewMessage(pharmacyName: String): OrderUseCaseData.LastMessageDetails? {
        return when (this?.profile) {
            CommunicationProfile.ErxCommunicationDispReq -> OrderUseCaseData.LastMessageDetails(
                content = pharmacyName,
                pickUpCodeDMC = null,
                pickUpCodeHR = null,
                link = null
            )

            CommunicationProfile.ErxCommunicationReply -> {
                val messageData = toMessage()
                OrderUseCaseData.LastMessageDetails(
                    content = messageData.content,
                    pickUpCodeDMC = messageData.pickUpCodeDMC,
                    pickUpCodeHR = messageData.pickUpCodeHR,
                    link = messageData.link
                )
            }

            else -> null
        }
    }
}
