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

@file:Suppress("TooManyFunctions", "MagicNumber")

package de.gematik.ti.erp.app.demomode.repository.orders

import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq
import de.gematik.ti.erp.app.communication.model.CommunicationErpModel.CommunicationProfile.ErxCommunicationReply
import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.longerRandomTimeToday
import de.gematik.ti.erp.app.demomode.extensions.demo
import de.gematik.ti.erp.app.demomode.model.DemoModeProfileLinkedCommunication
import de.gematik.ti.erp.app.demomode.model.toProfile
import de.gematik.ti.erp.app.demomode.model.toSyncedTaskDataCommunication
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class DemoCommunicationRepository(
    private val dataSource: DemoModeDataSource,
    private val downloadCommunicationResource: DemoDownloadCommunicationResource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : CommunicationRepository {

    private val scope = CoroutineScope(dispatcher)

    override suspend fun downloadCommunications(profileId: ProfileIdentifier) = withContext(dispatcher) {
        Napier.demo { "Simulating communication download" }
        dataSource.communications.value = dataSource.communications.updateAndGet { communications ->
            val taskIds = dataSource.syncedTasks.first()
                .filter { it.profileId == profileId }
                .filter { it.status == TaskStatusEnum.InProgress }
                .map { it.taskId }

            taskIds.forEach { taskId ->
                if (communications.none { it.taskId == taskId && it.profile == ErxCommunicationReply }) {
                    val existingRequest = communications.firstOrNull { it.taskId == taskId && it.profile == ErxCommunicationDispReq }
                    val orderId = existingRequest?.orderId ?: UUID.randomUUID().toString()
                    val pharmacyId = existingRequest?.sender ?: "pharmacy-demo-gematik"

                    if (existingRequest == null) {
                        communications.add(
                            DemoModeDataSource.requestCommunication(
                                profileId = profileId,
                                taskId = taskId,
                                communicationId = UUID.randomUUID().toString(),
                                pharmacyId = pharmacyId
                            ).copy(orderId = orderId, pharmacyName = "Gematik Demo-Apotheke")
                        )
                    }

                    communications.addAll(
                        DemoModeDataSource.replyCommunications(
                            profileId = profileId,
                            taskId = taskId,
                            communicationId = UUID.randomUUID().toString(),
                            pharmacyId = pharmacyId,
                            orderId = orderId
                        )
                    )
                }
            }
            communications
        }
        delay(1000.milliseconds) // simulates a network delay of one second
        Result.success(Unit)
    }

    /**
     * Synced task communication
     */
    override suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ) = withContext(dispatcher) {
        Result.success(ResourcePaging.ResourceResult(0, Unit))
    }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        withContext(dispatcher) {
            val isSynced = Random.nextBoolean()
            when {
                isSynced -> longerRandomTimeToday
                else -> null
            }
        }

    override fun loadSyncedByTaskId(taskId: String): Flow<TaskErpModel.Synced.Prescription?> =
        try {
            dataSource.syncedTasks.map { syncedTasks ->
                syncedTasks.filterIsInstance<TaskErpModel.Synced.Prescription>().find { it.taskId == taskId }
            }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(null)
        }

    override fun loadScannedByTaskId(taskId: String): Flow<TaskErpModel.Scanned?> =
        try {
            dataSource.scannedTasks.map { scannedTask ->
                scannedTask.find { it.taskId == taskId }
            }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(null)
        }

    override fun loadDispReqCommunications(orderId: String): Flow<List<CommunicationErpModel>> =
        try {
            dataSource.communications.map { communications ->
                communications
                    .also { Napier.demo { "LoadDispReqCommunications ${it.size}" } }
                    .filter { it.orderId == orderId && it.profile == ErxCommunicationDispReq }
                    .map { it.toSyncedTaskDataCommunication() }
            }.flowOn(dispatcher)
        } catch (_: Throwable) {
            flowOf(emptyList())
        }

    override fun loadDispReqCommunicationsByProfileId(profileId: ProfileIdentifier): Flow<List<CommunicationErpModel>> =
        try {
            loadOrdersByProfileId(profileId).mapNotNull { communications ->
                communications.asSequence().filter {
                    it.profileId == profileId && it.profile == ErxCommunicationDispReq
                }
                    .map { it.toSyncedTaskDataCommunication() }
                    .sortedByDescending { it.timeStamp }
                    .distinctBy { it.orderId }
                    .toList()
            }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(emptyList())
        }

    override fun loadDispReqCommunicationsByTaskId(taskId: String): Flow<List<CommunicationErpModel>> =
        try {
            dataSource.communications.map { list ->
                list.filter { it.taskId == taskId && it.profile == ErxCommunicationDispReq }
                    .map { it.toSyncedTaskDataCommunication() }
            }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(emptyList())
        }

    override fun loadRepliedCommunications(orderId: String): Flow<List<CommunicationErpModel>> {
        return flowOf(emptyList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun loadRepliedCommunications(orderId: String, telematikId: String): Flow<List<CommunicationErpModel>> =
        taskIdsByOrder(orderId).flatMapLatest { taskIds ->
            loadRepliedCommunications(taskIds = taskIds, telematikId = telematikId)
        }.flowOn(dispatcher)

    override fun loadRepliedCommunicationsByProfileId(profileId: ProfileIdentifier): Flow<List<CommunicationErpModel>> =
        dataSource.communications.map { communications ->
            communications
                .filter { it.profileId == profileId && it.profile == ErxCommunicationReply }
                .map { it.toSyncedTaskDataCommunication() }
        }.flowOn(dispatcher)

    override fun loadRepliedCommunications(taskIds: List<String>, telematikId: String): Flow<List<CommunicationErpModel>> =
        try {
            dataSource.communications
                .map { communications ->
                    communications
                        .filter { it.taskId in taskIds && it.profile == ErxCommunicationReply }
                        .sortedByDescending { it.sentOn }
                        .map { it.toSyncedTaskDataCommunication() }
                }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(emptyList())
        }

    override fun loadAllRepliedCommunications(taskIds: List<String>): Flow<List<CommunicationErpModel>> =
        try {
            dataSource.communications
                .mapNotNull { communications ->
                    communications
                        .filter { it.taskId in taskIds && it.profile == ErxCommunicationReply }
                        .sortedByDescending { it.sentOn }
                        .map { it.toSyncedTaskDataCommunication() }
                }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(emptyList())
        }

    override fun hasUnreadDispenseMessage(taskIds: List<String>, orderId: String): Flow<Boolean> =
        try {
            dataSource.communications.mapNotNull { communications ->
                val booleans = taskIds.map { taskId ->
                    communications.find { it.taskId == taskId && !it.consumed }?.consumed == false
                }
                booleans.any { it }
            }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(false)
        }

    override fun hasUnreadDispenseMessage(profileId: ProfileIdentifier): Flow<Boolean> =
        try {
            dataSource.communications.mapNotNull { communications ->
                communications.any { it.profileId == profileId && !it.consumed }
            }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(false)
        }

    override fun unreadMessagesCount(): Flow<Long> =
        dataSource.communications.map { communications ->
            try {
                communications.toList() // Ensure it's never null
                    .filter {
                        !it.consumed && it.profile == ErxCommunicationDispReq
                    }
                    .distinctBy { it.orderId }
                    .size.toLong()
            } catch (e: Throwable) {
                0L // Return 0 on failure instead of crashing
            }
        }

    override fun unreadPrescriptionsInAllOrders(profileId: ProfileIdentifier): Flow<Long> =
        loadOrdersByProfileId(profileId).map { communications ->
            communications.count { it.profileId == profileId && !it.consumed }.toLong()
        }.flowOn(dispatcher)

    override fun taskIdsByOrder(orderId: String): Flow<List<String>> =
        dataSource.communications.map { communications ->
            communications.filter { it.orderId == orderId && it.profile == ErxCommunicationDispReq }
                .map { it.taskId }
        }.flowOn(dispatcher)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun profileByOrderId(orderId: String): Flow<ProfileErpModel> =
        dataSource.communications.mapNotNull { communications ->
            communications.find { communication -> communication.orderId == orderId }
        }.flatMapLatest { communication ->
            dataSource.profiles.mapNotNull { profiles ->
                profiles.find { it.id == communication.profileId }?.toProfile()
            }.flowOn(dispatcher)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun profileByTaskId(taskId: String): Flow<ProfileErpModel> =
        dataSource.communications.mapNotNull { communications ->
            communications.find { communication -> communication.taskId == taskId }
        }.flatMapLatest { communication ->
            dataSource.profiles.mapNotNull { profiles ->
                profiles.find { it.id == communication.profileId }?.toProfile()
            }.flowOn(dispatcher)
        }

    override fun getAllUnreadMessages(): Flow<List<CommunicationErpModel>> {
        return flowOf(emptyList())
    }

    override suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean) {
        withContext(dispatcher) {
            dataSource.communications.update { communications ->
                communications.map { communication ->
                    if (communication.communicationId == communicationId) {
                        communication.copy(consumed = consumed)
                    } else {
                        communication
                    }
                }.toMutableList()
            }
        }
    }

    /**
     * Scanned Task communication
     * From the scannedTasks get the task for the given [taskId]
     * Create a communication object request for the given [taskId] and [transactionId] and save it under
     * communications in the scanned-task and save this also in the communications repository
     */
    override suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String) {
        withContext(dispatcher) {
            val task = dataSource.scannedTasks
                .mapNotNull { scannedTasks ->
                    scannedTasks.find { it.taskId == taskId }
                }.first()
            val requestMessage = task.makeRequestCommunication(transactionId, pharmacyId)
            val replyCommunication = requestMessage.copy(
                communicationId = UUID.randomUUID().toString(),
                sentOn = Clock.System.now().plus(1.hours),
                payload = DemoModeDataSource.communicationPayload,
                profile = ErxCommunicationDispReq
            )
            dataSource.scannedTasks.value = dataSource.scannedTasks.updateAndGet { scannedTasks ->
                // TaskErpModel.Scanned has no communications field; just return as-is
                scannedTasks
            }
            dataSource.communications.update { communications ->
                (communications + listOf(requestMessage, replyCommunication)).toMutableList()
            }
        }
    }

    override suspend fun saveCommunications(communicationModels: List<CommunicationErpModel>): Int =
        withContext(dispatcher) {
            if (communicationModels.isEmpty()) return@withContext 0

            dataSource.communications.update { communications ->
                (
                    communications + communicationModels.map { communication ->
                        DemoModeProfileLinkedCommunication(
                            profileId = communication.profileId.orEmpty(),
                            taskId = communication.taskId,
                            communicationId = communication.communicationId,
                            orderId = communication.orderId,
                            profile = communication.profile,
                            sentOn = communication.timeStamp ?: Clock.System.now(),
                            sender = communication.senderTelematikId,
                            recipient = communication.recipient,
                            payload = communication.payload?.let { payload ->
                                de.gematik.ti.erp.app.fhir.constant.SafeJson.value.encodeToString(
                                    de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel.serializer(),
                                    payload
                                )
                            },
                            consumed = communication.consumed,
                            pharmacyName = communication.pharmacyName
                        )
                    }
                    ).toMutableList()
            }

            communicationModels.size
        }

    override suspend fun hasUnreadRepliedMessages(taskIds: List<String>, telematikId: String): Flow<Boolean> =
        dataSource.communications.map { communications ->
            communications.any { it.taskId in taskIds && it.profile == ErxCommunicationReply && !it.consumed }
        }.flowOn(dispatcher)

    override suspend fun updatePharmacyName(communicationId: String, pharmacyName: String) {
        withContext(dispatcher) {
            dataSource.communications.value = dataSource.communications.updateAndGet { communications ->
                communications
                    .indexOfFirst { it.communicationId == communicationId }
                    .takeIf { it != INDEX_OUT_OF_BOUNDS }
                    ?.let { index ->
                        communications[index] = communications[index].copy(pharmacyName = pharmacyName)
                    }
                communications
            }
        }
    }

    private fun TaskErpModel.Scanned.makeRequestCommunication(
        id: String,
        pharmacyId: String,
        consumed: Boolean = false
    ): DemoModeProfileLinkedCommunication =
        DemoModeProfileLinkedCommunication(
            profileId = profileId,
            taskId = taskId,
            communicationId = id,
            sentOn = Clock.System.now().minus(1.minutes),
            sender = pharmacyId,
            consumed = consumed,
            profile = ErxCommunicationDispReq,
            // these values are kept empty while saving them
            orderId = UUID.randomUUID().toString(),
            payload = "",
            recipient = "Mustermann"
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadOrdersForActiveProfile() = findActiveProfile().flatMapLatest { loadOrdersByProfileId(it.id) }

    /**
     * Method added so that demoModeProfile02 always loads with some communication
     * and for other profiles we have to add it. [downloadCommunications] method takes
     * in profileId so this can be changed to a different profile later too
     */
    private fun loadOrdersByProfileId(
        profileId: ProfileIdentifier
    ): Flow<MutableList<DemoModeProfileLinkedCommunication>> = dataSource.communications

    private fun findActiveProfile() = dataSource.profiles.mapNotNull { it.find { profile -> profile.active } }
}
