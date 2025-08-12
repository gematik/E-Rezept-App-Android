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
import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.longerRandomTimeToday
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo
import de.gematik.ti.erp.app.demomode.extensions.demo
import de.gematik.ti.erp.app.demomode.model.DemoModeProfileLinkedCommunication
import de.gematik.ti.erp.app.demomode.model.toProfile
import de.gematik.ti.erp.app.demomode.model.toSyncedTaskDataCommunication
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.messages.model.CommunicationProfile.ErxCommunicationDispReq
import de.gematik.ti.erp.app.messages.model.CommunicationProfile.ErxCommunicationReply
import de.gematik.ti.erp.app.messages.repository.CachedPharmacy
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
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
                .filter { it.status == SyncedTaskData.TaskStatus.InProgress }
                .map { it.taskId }
            communications.all { it.taskId in taskIds }

            taskIds.map { taskId ->
                when {
                    // if the task is not already in the list
                    communications.none { it.taskId == taskId && it.profile == ErxCommunicationReply } -> {
                        communications.add(
                            DemoModeDataSource.replyCommunications(
                                profileId = profileId,
                                taskId = taskId,
                                communicationId = UUID.randomUUID().toString(),
                                orderId = UUID.randomUUID().toString(),
                                pharmacyId = UUID.randomUUID().toString()
                            )[0]
                        )
                    }

                    communications.filter { it.taskId == taskId && it.profile == ErxCommunicationReply }.size == 1 -> {
                        val firstCommunication =
                            communications.first { it.taskId == taskId && it.profile == ErxCommunicationReply }

                        communications.add(
                            DemoModeDataSource.replyCommunications(
                                profileId = profileId,
                                taskId = taskId,
                                communicationId = UUID.randomUUID().toString(),
                                orderId = firstCommunication.orderId,
                                pharmacyId = firstCommunication.sender
                            )[1]
                        )
                    }

                    communications.filter { it.taskId == taskId && it.profile == ErxCommunicationReply }.size == 2 -> {
                        val firstCommunication =
                            communications.first { it.taskId == taskId && it.profile == ErxCommunicationReply }

                        communications.add(
                            DemoModeDataSource.replyCommunications(
                                profileId = profileId,
                                taskId = taskId,
                                communicationId = UUID.randomUUID().toString(),
                                orderId = firstCommunication.orderId,
                                pharmacyId = firstCommunication.sender
                            )[2]
                        )
                    }

                    else -> communications
                }
            }
            communications
        }
        delay(1000) // simulates a network delay of one second
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

    override fun loadPharmacies(): Flow<List<CachedPharmacy>> = dataSource.cachedPharmacies

    override fun loadSyncedByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?> =
        try {
            dataSource.syncedTasks.map { syncedTasks ->
                syncedTasks.find { it.taskId == taskId }
            }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(null)
        }

    override fun loadScannedByTaskId(taskId: String): Flow<ScannedTaskData.ScannedTask?> =
        try {
            dataSource.scannedTasks.map { scannedTask ->
                scannedTask.find { it.taskId == taskId }
            }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(null)
        }

    override fun loadDispReqCommunications(orderId: String): Flow<List<Communication>> =
        try {
            dataSource.communications.mapNotNull { communications ->
                communications
                    .also { Napier.demo { "LoadDispReqCommunications ${it.size}" } }
                    .filter { it.orderId == orderId && it.profile == ErxCommunicationDispReq }
                    .map { it.toSyncedTaskDataCommunication() }
            }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(emptyList())
        }

    override fun loadDispReqCommunicationsByProfileId(profileId: ProfileIdentifier): Flow<List<Communication>> =
        try {
            loadOrdersByProfileId(profileId).mapNotNull { communications ->
                communications.asSequence().filter {
                    it.profileId == profileId && it.profile == ErxCommunicationDispReq
                }
                    .map { it.toSyncedTaskDataCommunication() }
                    .sortedByDescending { it.sentOn }
                    .distinctBy { it.orderId }
                    .toList()
            }.flowOn(dispatcher)
        } catch (e: Throwable) {
            flowOf(emptyList())
        }

    override fun loadRepliedCommunications(taskIds: List<String>, telematikId: String): Flow<List<Communication>> =
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

    override fun loadAllRepliedCommunications(taskIds: List<String>): Flow<List<Communication>> =
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
                (communications.toList() ?: emptyList()) // Ensure it's never null
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
        loadOrdersByProfileId(profileId).mapNotNull { communications ->
            communications.count { it.profileId == profileId && !it.consumed }.toLong()
        }.flowOn(dispatcher)

    override fun taskIdsByOrder(orderId: String): Flow<List<String>> =
        dataSource.communications.mapNotNull { communications ->
            communications.filter { it.orderId == orderId && it.profile == ErxCommunicationDispReq }
                .map { it.taskId }
        }.flowOn(dispatcher)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun profileByOrderId(orderId: String): Flow<ProfilesData.Profile> =
        dataSource.communications.mapNotNull { communications ->
            communications.find { communication -> communication.orderId == orderId }
        }.flatMapLatest { communication ->
            dataSource.profiles.mapNotNull { profiles ->
                profiles.find { it.id == communication.profileId }?.toProfile()
            }.flowOn(dispatcher)
        }

    override fun getAllUnreadMessages(): Flow<List<Communication>> {
        return flowOf(emptyList())
    }

    override suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean) {
        withContext(dispatcher) {
            dataSource.communications.value = scope.async {
                loadOrdersForActiveProfile().mapNotNull { communications ->
                    communications
                        .indexOfFirst { it.communicationId == communicationId }
                        .takeIf { it != INDEX_OUT_OF_BOUNDS }
                        ?.let { index ->
                            communications[index] = communications[index].copy(
                                consumed = consumed
                            )
                        }
                    communications
                }
            }.await().first()
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
                val index = scannedTasks.indexOfFirst { it.taskId == taskId }.takeIf { it != INDEX_OUT_OF_BOUNDS }
                index?.let { nonNullIndex ->
                    scannedTasks[nonNullIndex] = scannedTasks[nonNullIndex].copy(
                        communications = listOf()
                    )
                }
                scannedTasks
            }
            dataSource.communications.value = dataSource.communications.updateAndGet { communications ->
                communications.add(requestMessage)
                communications.add(replyCommunication)
                communications
            }
        }
    }

    override suspend fun hasUnreadRepliedMessages(taskIds: List<String>, telematikId: String): Flow<Boolean> {
        return flowOf(false)
    }

    private fun ScannedTaskData.ScannedTask.makeRequestCommunication(
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
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadOrdersByProfileId(
        profileId: ProfileIdentifier
    ): Flow<MutableList<DemoModeProfileLinkedCommunication>> =
        // For the profile 2 we load it with some existing communications
        if (profileId == DemoProfileInfo.demoProfile02.id) {
            dataSource.profileCommunicationLog
                .map { it[profileId] }
                .flatMapLatest { communicationExists ->
                    when (communicationExists) {
                        true -> dataSource.communications
                        else -> downloadCommunicationResource(profileId = profileId)
                    }
                }
        } else {
            dataSource.communications
        }

    private fun findActiveProfile() = dataSource.profiles.mapNotNull { it.find { profile -> profile.active } }
}
