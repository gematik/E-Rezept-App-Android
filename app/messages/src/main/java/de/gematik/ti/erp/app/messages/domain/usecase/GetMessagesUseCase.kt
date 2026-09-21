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

package de.gematik.ti.erp.app.messages.domain.usecase

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.mapper.communicationReplyToOrder
import de.gematik.ti.erp.app.messages.mapper.dispenseRequestCommunicationToOrder
import de.gematik.ti.erp.app.messages.mapper.generatePreviewMessage
import de.gematik.ti.erp.app.messages.mapper.groupByPayloadForPreview
import de.gematik.ti.erp.app.messages.model.LastMessage
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
    private val pharmacyRepository: PharmacyRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val DEFAULT_PHARMACY_NAME = "Unbekannte Apotheke"
    }

    /**
     * Executes the use case to load a list of orders by processing profiles and communications.
     *
     * @return A list of [OrderUseCaseData.Order] objects, sorted by the latest sent date and distinct by order ID.
     */
    suspend operator fun invoke(): List<CommunicationErpModel> = withContext(dispatcher) {
        // Fetch all profiles from the repository. `first()` ensures we get the initial value from the flow.
        val profiles = profileRepository.profiles().first()
        val dispReqCommunications = profiles.flatMap { profile ->
            communicationRepository
                .loadDispReqCommunicationsByProfileId(profile.id)
                .first()
        }

        val repliedCommunications = profiles.flatMap { profile ->
            communicationRepository
                .loadRepliedCommunicationsByProfileId(profile.id)
                .first()
        }

        val dispReqTaskIds = dispReqCommunications.map { it.taskId }.toSet()
        val filteredRepliedCommunications = repliedCommunications.filter {
            it.taskId !in dispReqTaskIds
        }

        val groupedReplies = filteredRepliedCommunications.groupBy {
            listOf(it.taskId, it.recipient, it.payload)
        }.values.map { group ->
            group.maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST } ?: group.first()
        }.sortedByDescending { it.timeStamp }
            .distinctBy { it.taskId }

        val groupedDispenses = dispReqCommunications
            .groupBy {
                listOf(it.taskId, it.orderId, it.recipient, it.payload)
            }
            .values
            .map { group ->
                group.maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST } ?: group.first()
            }
            .sortedByDescending { it.timeStamp }
            .distinctBy { it.orderId }

        (groupedDispenses + groupedReplies)
            .sortedByDescending { it.timeStamp }
    }

    internal suspend fun mapDispenseCommunicationToOrder(
        communication: CommunicationErpModel,
        latestMessageSentOnDate: Instant
    ): OrderUseCaseData.Order {
        val pharmacyName = communication.pharmacyName?.takeIf { it.isNotBlank() } ?: resolvePharmacyName(
            pharmacyRepository = pharmacyRepository,
            communicationRepository = communicationRepository,
            communicationId = communication.communicationId,
            telematikId = communication.recipient
        )

        val taskIds = communicationRepository.taskIdsByOrder(communication.orderId).first()

        val (prescriptions, hasUnreadMessages) = communication.dispenseRequestCommunicationToOrder(
            communicationRepository = communicationRepository,
            invoiceRepository = invoiceRepository,
            withMedicationNames = true,
            dispatcher = dispatcher
        ).first()

        val latestInvoice = taskIds.mapNotNull { taskId ->
            invoiceRepository.invoiceByTaskId(taskId).first()
        }.maxByOrNull { it.timestamp }

        val invoiceInfo = OrderUseCaseData.InvoiceInfo(
            hasInvoice = latestInvoice != null,
            invoiceSentOn = latestInvoice?.timestamp,
            medicationName = latestInvoice?.medicationRequest?.medication?.name()
        )

        return OrderUseCaseData.Order(
            orderId = communication.orderId,
            prescriptions = prescriptions,
            sentOn = invoiceInfo.invoiceSentOn?.let {
                maxOf(it, latestMessageSentOnDate)
            } ?: latestMessageSentOnDate,
            pharmacy = OrderUseCaseData.Pharmacy(
                id = communication.recipient, // getting Pharmacy ID (telematikId) from communication.recipient
                name = pharmacyName ?: ""
            ),
            hasUnreadMessages = hasUnreadMessages,
            latestCommunicationMessage = getLatestCommunicationMessageForDispenseCommunication(
                orderId = communication.orderId,
                pharmacyName = pharmacyName ?: "",
                telematikId = communication.recipient,
                taskIds = taskIds
            ),
            invoiceInfo = invoiceInfo
        )
    }

    private suspend fun getLatestCommunicationMessageForDispenseCommunication(
        orderId: String,
        pharmacyName: String,
        telematikId: String,
        taskIds: List<String>
    ): LastMessage? =
        supervisorScope {
            return@supervisorScope combine(
                communicationRepository.loadRepliedCommunications(taskIds, telematikId),
                communicationRepository.loadDispReqCommunications(orderId)
            ) { repliedCommunications, dispReqCommunications ->
                (repliedCommunications + dispReqCommunications)
                    .groupByPayloadForPreview()
            }.mapNotNull { combinedCommunication ->
                val lastCommunication = combinedCommunication.maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST }
                lastCommunication?.let { communication ->
                    communication.generatePreviewMessage(pharmacyName)?.let { lastMessageDetails ->
                        LastMessage(
                            lastMessageDetails = lastMessageDetails,
                            profile = communication.profile
                        )
                    }
                }
            }.firstOrNull()
        }

    internal suspend fun mapReplyCommunicationToOrder(
        communication: CommunicationErpModel,
        latestMessageSentOnDate: Instant
    ): OrderUseCaseData.Order {
        val resolvedName = communication.pharmacyName?.takeIf { it.isNotBlank() } ?: resolvePharmacyName(
            pharmacyRepository = pharmacyRepository,
            communicationRepository = communicationRepository,
            communicationId = communication.communicationId,
            telematikId = communication.senderTelematikId
        )
        val pharmacyName = resolvedName?.ifBlank { DEFAULT_PHARMACY_NAME } ?: DEFAULT_PHARMACY_NAME

        val taskIds = listOf(communication.taskId)

        val (prescriptions, hasUnreadMessages) = communication.communicationReplyToOrder(
            taskIds = taskIds,
            communicationRepository = communicationRepository,
            invoiceRepository = invoiceRepository,
            withMedicationNames = true,
            dispatcher = dispatcher
        ).first()

        val latestInvoice = taskIds.mapNotNull { taskId ->
            invoiceRepository.invoiceByTaskId(taskId).first()
        }.maxByOrNull { it.timestamp }

        val invoiceInfo = OrderUseCaseData.InvoiceInfo(
            hasInvoice = latestInvoice != null,
            invoiceSentOn = latestInvoice?.timestamp
        )

        return OrderUseCaseData.Order(
            orderId = communication.orderId,
            prescriptions = prescriptions,
            sentOn = invoiceInfo.invoiceSentOn?.let {
                maxOf(it, latestMessageSentOnDate)
            } ?: latestMessageSentOnDate,
            pharmacy = OrderUseCaseData.Pharmacy(
                id = communication.senderTelematikId, // getting Pharmacy ID (telematikId) from communication.recipient
                name = pharmacyName
            ),
            hasUnreadMessages = hasUnreadMessages,
            latestCommunicationMessage = getLatestCommunicationMessageForReplies(
                pharmacyName = pharmacyName,
                telematikId = communication.senderTelematikId,
                taskIds = taskIds
            ),
            invoiceInfo = invoiceInfo
        )
    }

    private suspend fun getLatestCommunicationMessageForReplies(
        pharmacyName: String,
        telematikId: String,
        taskIds: List<String>
    ): LastMessage? =
        supervisorScope {
            return@supervisorScope communicationRepository.loadRepliedCommunications(taskIds, telematikId).mapNotNull {
                    list ->
                val combinedCommunication = list.groupByPayloadForPreview()
                val lastCommunication = combinedCommunication.maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST }
                lastCommunication?.let { communication ->
                    communication.generatePreviewMessage(pharmacyName)?.let { lastMessageDetails ->
                        LastMessage(
                            lastMessageDetails = lastMessageDetails,
                            profile = communication.profile
                        )
                    }
                }
            }
        }.firstOrNull()
}
