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
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyReservationStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestCommunicationPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestDeliveryPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestReservationPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestShipmentPayloadErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.mapper.generatePreviewMessage
import de.gematik.ti.erp.app.messages.mapper.groupByPayloadForPreview
import de.gematik.ti.erp.app.messages.model.LastMessage
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
     * Observes the list of order-related communications by reactively following profile,
     * dispense-request and reply changes, so later status changes (e.g. a reply arriving)
     * are emitted without requiring the screen to be reopened.
     *
     * @return A [Flow] of [CommunicationErpModel] lists, sorted by the latest sent date and distinct by order ID.
     */
    fun observe(): Flow<List<CommunicationErpModel>> =
        profileRepository.profiles().flatMapLatest { profiles ->
            val dispenseFlows = profiles.map { profile ->
                communicationRepository.loadDispReqCommunicationsByProfileId(profile.id)
            }
            val replyFlows = profiles.map { profile ->
                communicationRepository.loadRepliedCommunicationsByProfileId(profile.id)
            }

            combineCommunicationLists(dispenseFlows, replyFlows)
        }.distinctUntilChanged()

    fun observeOrder(communication: CommunicationErpModel): Flow<OrderUseCaseData.Order> =
        flow {
            val pharmacyName = communication.pharmacyName?.takeIf { it.isNotBlank() } ?: resolvePharmacyName(
                pharmacyRepository = pharmacyRepository,
                communicationRepository = communicationRepository,
                communicationId = communication.communicationId,
                telematikId = when (communication.profile) {
                    CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq -> communication.recipient
                    CommunicationErpModel.CommunicationProfile.ErxCommunicationReply -> communication.senderTelematikId
                    else -> ""
                }
            ).orEmpty()

            emitAllObservedOrder(communication = communication, pharmacyName = pharmacyName)
        }.distinctUntilChanged()

    private suspend fun getSupplyOption(communication: CommunicationErpModel): CommunicationSupplyOptionTypeErpModel {
        val dispReq = if (communication.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq) {
            communication
        } else {
            communicationRepository.loadDispReqCommunicationsByTaskId(communication.taskId)
                .firstOrNull()?.firstOrNull()
        }

        val parsedPayload = dispReq?.payload ?: return CommunicationSupplyOptionTypeErpModel.UNKNOWN
        return when (parsedPayload) {
            is DispenseRequestCommunicationPayloadV1ErpModel -> parsedPayload.supplyOptionsType
            is DispenseRequestReservationPayloadErpModel -> parsedPayload.supplyOptionsType
            is DispenseRequestDeliveryPayloadErpModel -> parsedPayload.supplyOptionsType
            is DispenseRequestShipmentPayloadErpModel -> parsedPayload.supplyOptionsType
            else -> CommunicationSupplyOptionTypeErpModel.UNKNOWN
        }
    }

    private suspend fun FlowCollector<OrderUseCaseData.Order>.emitAllObservedOrder(
        communication: CommunicationErpModel,
        pharmacyName: String
    ) {
        when (communication.profile) {
            CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq -> {
                val telematikId = communication.recipient
                emitAll(
                    communicationRepository.taskIdsByOrder(communication.orderId).flatMapLatest { taskIds ->
                        combine(
                            observePrescriptions(taskIds),
                            observeUnreadStateForDispReq(taskIds, communication.orderId, telematikId),
                            observeLatestInvoice(taskIds),
                            observeLatestCommunicationMessageForDispense(
                                orderId = communication.orderId,
                                pharmacyName = pharmacyName,
                                telematikId = telematikId,
                                taskIds = taskIds
                            ),
                            observeLatestRepliedTimestamp(taskIds, telematikId, communication)
                        ) { prescriptions, hasUnreadMessages, latestInvoice, latestCommunicationMessage, latestMessageSentOnDate ->
                            val invoiceInfo = OrderUseCaseData.InvoiceInfo(
                                hasInvoice = latestInvoice != null,
                                invoiceSentOn = latestInvoice?.timestamp,
                                medicationName = latestInvoice?.medicationRequest?.medication?.name()
                            )

                            OrderUseCaseData.Order(
                                orderId = communication.orderId,
                                prescriptions = prescriptions,
                                sentOn = invoiceInfo.invoiceSentOn?.let {
                                    maxOf(it, latestMessageSentOnDate)
                                } ?: latestMessageSentOnDate,
                                pharmacy = OrderUseCaseData.Pharmacy(
                                    id = communication.recipient,
                                    name = pharmacyName
                                ),
                                hasUnreadMessages = hasUnreadMessages,
                                latestCommunicationMessage = latestCommunicationMessage,
                                invoiceInfo = invoiceInfo,
                                supplyOption = getSupplyOption(communication)
                            )
                        }
                    }
                )
            }

            CommunicationErpModel.CommunicationProfile.ErxCommunicationReply -> {
                val taskIds = listOf(communication.taskId)
                val telematikId = communication.senderTelematikId
                val latestMessageAndSupplyOption = combine(
                    observeLatestCommunicationMessageForReply(
                        pharmacyName = pharmacyName,
                        telematikId = telematikId,
                        taskIds = taskIds
                    ),
                    observeSupplyOptionForReply(communication)
                ) { latestCommunicationMessage, supplyOption ->
                    latestCommunicationMessage to supplyOption
                }
                emitAll(
                    combine(
                        observePrescriptions(taskIds),
                        observeUnreadStateForReply(taskIds, telematikId),
                        observeLatestInvoice(taskIds),
                        observeLatestRepliedTimestamp(taskIds, telematikId, communication),
                        latestMessageAndSupplyOption
                    ) { prescriptions, hasUnreadMessages, latestInvoice, latestMessageSentOnDate, latestMessageAndSupply ->
                        val (latestCommunicationMessage, supplyOption) = latestMessageAndSupply
                        val invoiceInfo = OrderUseCaseData.InvoiceInfo(
                            hasInvoice = latestInvoice != null,
                            invoiceSentOn = latestInvoice?.timestamp
                        )

                        OrderUseCaseData.Order(
                            orderId = communication.orderId,
                            prescriptions = prescriptions,
                            sentOn = invoiceInfo.invoiceSentOn?.let {
                                maxOf(it, latestMessageSentOnDate)
                            } ?: latestMessageSentOnDate,
                            pharmacy = OrderUseCaseData.Pharmacy(
                                id = communication.senderTelematikId,
                                name = pharmacyName.ifBlank { DEFAULT_PHARMACY_NAME }
                            ),
                            hasUnreadMessages = hasUnreadMessages,
                            latestCommunicationMessage = latestCommunicationMessage,
                            invoiceInfo = invoiceInfo,
                            supplyOption = supplyOption
                        )
                    }
                )
            }

            else -> Unit
        }
    }

    private fun combineCommunicationLists(
        dispenseFlows: List<Flow<List<CommunicationErpModel>>>,
        replyFlows: List<Flow<List<CommunicationErpModel>>>
    ): Flow<List<CommunicationErpModel>> =
        combine(
            combineCommunicationList(dispenseFlows),
            combineCommunicationList(replyFlows)
        ) { dispReqCommunications, repliedCommunications ->
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

    private fun combineCommunicationList(
        flows: List<Flow<List<CommunicationErpModel>>>
    ): Flow<List<CommunicationErpModel>> =
        if (flows.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(flows) { values ->
                values.flatMap { it }
            }
        }

    private fun observePrescriptions(
        taskIds: List<String>
    ): Flow<List<TaskErpModel?>> =
        if (taskIds.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(taskIds.map(::observeTaskById)) { prescriptions ->
                prescriptions.toList()
            }.distinctUntilChanged()
        }

    private fun observeTaskById(
        taskId: String
    ): Flow<TaskErpModel?> =
        combine(
            communicationRepository.loadSyncedByTaskId(taskId),
            communicationRepository.loadScannedByTaskId(taskId)
        ) { synced, scanned ->
            synced ?: scanned
        }.distinctUntilChanged()

    private fun observeLatestInvoice(
        taskIds: List<String>
    ): Flow<PKVInvoiceErpModel?> =
        if (taskIds.isEmpty()) {
            flowOf(null)
        } else {
            combine(taskIds.map(invoiceRepository::invoiceByTaskId)) { invoices ->
                invoices.filterNotNull().maxByOrNull { it.timestamp }
            }.distinctUntilChanged()
        }

    private fun observeUnreadStateForDispReq(
        taskIds: List<String>,
        orderId: String,
        telematikId: String
    ): Flow<Boolean> = flow {
        val hasUnreadReplies = communicationRepository.hasUnreadRepliedMessages(taskIds = taskIds, telematikId = telematikId)
        emitAll(
            combine(
                communicationRepository.hasUnreadDispenseMessage(taskIds, orderId),
                hasUnreadReplies,
                invoiceRepository.hasUnreadInvoiceMessages(taskIds)
            ) { hasUnreadDispenseMessage, hasUnreadMessages, hasUnreadInvoices ->
                hasUnreadDispenseMessage || hasUnreadMessages || hasUnreadInvoices
            }
        )
    }.distinctUntilChanged()

    private fun observeUnreadStateForReply(
        taskIds: List<String>,
        telematikId: String
    ): Flow<Boolean> = flow {
        val hasUnreadReplies = communicationRepository.hasUnreadRepliedMessages(taskIds = taskIds, telematikId = telematikId)
        emitAll(
            combine(
                hasUnreadReplies,
                invoiceRepository.hasUnreadInvoiceMessages(taskIds)
            ) { hasUnreadMessages, hasUnreadInvoices ->
                hasUnreadMessages || hasUnreadInvoices
            }
        )
    }.distinctUntilChanged()

    private fun observeLatestCommunicationMessageForDispense(
        orderId: String,
        pharmacyName: String,
        telematikId: String,
        taskIds: List<String>
    ): Flow<LastMessage?> =
        combine(
            communicationRepository.loadAllRepliedCommunications(taskIds),
            communicationRepository.loadDispReqCommunications(orderId)
        ) { repliedCommunications, dispReqCommunications ->
            (repliedCommunications + dispReqCommunications)
                .groupByPayloadForPreview()
        }.map { combinedCommunication ->
            val lastCommunication = combinedCommunication.maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST }
            val latestReservationComm = combinedCommunication
                .filter {
                    it.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationReply &&
                        it.payload is CommunicationReplyReservationStatusPayloadErpModel
                }
                .maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST }
            val reservationPayload = latestReservationComm?.payload as? CommunicationReplyReservationStatusPayloadErpModel

            val latestDeliveryComm = combinedCommunication
                .filter {
                    it.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationReply &&
                        it.payload is CommunicationReplyDeliveryStatusPayloadErpModel
                }
                .maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST }
            val deliveryPayload = latestDeliveryComm?.payload as? CommunicationReplyDeliveryStatusPayloadErpModel

            lastCommunication?.let { communication ->
                communication.generatePreviewMessage(pharmacyName)?.let { lastMessageDetails ->
                    LastMessage(
                        lastMessageDetails = lastMessageDetails,
                        profile = communication.profile,
                        payload = communication.payload,
                        latestReservationPayload = reservationPayload,
                        latestDeliveryPayload = deliveryPayload
                    )
                }
            }
        }.distinctUntilChanged()

    private fun observeLatestCommunicationMessageForReply(
        pharmacyName: String,
        telematikId: String,
        taskIds: List<String>
    ): Flow<LastMessage?> =
        communicationRepository.loadAllRepliedCommunications(taskIds).map { list ->
            val combinedCommunication = list.groupByPayloadForPreview()
            val lastCommunication = combinedCommunication.maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST }
            val latestReservationComm = combinedCommunication
                .filter {
                    it.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationReply &&
                        it.payload is CommunicationReplyReservationStatusPayloadErpModel
                }
                .maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST }
            val reservationPayload = latestReservationComm?.payload as? CommunicationReplyReservationStatusPayloadErpModel

            val latestDeliveryComm = combinedCommunication
                .filter {
                    it.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationReply &&
                        it.payload is CommunicationReplyDeliveryStatusPayloadErpModel
                }
                .maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST }
            val deliveryPayload = latestDeliveryComm?.payload as? CommunicationReplyDeliveryStatusPayloadErpModel

            lastCommunication?.let { communication ->
                communication.generatePreviewMessage(pharmacyName)?.let { lastMessageDetails ->
                    LastMessage(
                        lastMessageDetails = lastMessageDetails,
                        profile = communication.profile,
                        payload = communication.payload,
                        latestReservationPayload = reservationPayload,
                        latestDeliveryPayload = deliveryPayload
                    )
                }
            }
        }.distinctUntilChanged()

    private fun observeLatestRepliedTimestamp(
        taskIds: List<String>,
        telematikId: String,
        requestCommunication: CommunicationErpModel
    ): Flow<Instant> =
        communicationRepository.loadAllRepliedCommunications(taskIds).map { replies ->
            replies.maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST }
                ?.timeStamp
                ?.coerceAtLeast(requestCommunication.timeStamp ?: Instant.DISTANT_PAST)
                ?: (requestCommunication.timeStamp ?: Instant.DISTANT_PAST)
        }.distinctUntilChanged()

    private fun observeSupplyOptionForReply(
        communication: CommunicationErpModel
    ): Flow<CommunicationSupplyOptionTypeErpModel> =
        communicationRepository.loadDispReqCommunicationsByTaskId(communication.taskId).map { communications ->
            val dispReq = communications.firstOrNull()
            val parsedPayload = dispReq?.payload ?: return@map CommunicationSupplyOptionTypeErpModel.UNKNOWN
            when (parsedPayload) {
                is DispenseRequestCommunicationPayloadV1ErpModel -> parsedPayload.supplyOptionsType
                is DispenseRequestReservationPayloadErpModel -> parsedPayload.supplyOptionsType
                is DispenseRequestDeliveryPayloadErpModel -> parsedPayload.supplyOptionsType
                is DispenseRequestShipmentPayloadErpModel -> parsedPayload.supplyOptionsType
                else -> CommunicationSupplyOptionTypeErpModel.UNKNOWN
            }
        }.distinctUntilChanged()
}
