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

package de.gematik.ti.erp.app.demomode.repository.orders

import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.demomode.datasource.data.DemoConstants.longerRandomTimeToday
import de.gematik.ti.erp.app.demomode.datasource.data.DemoPharmacyInfo.PHARMACY_NAMES
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo
import de.gematik.ti.erp.app.demomode.model.DemoModeProfileLinkedCommunication
import de.gematik.ti.erp.app.demomode.model.toSyncedTaskDataCommunication
import de.gematik.ti.erp.app.orders.repository.CachedPharmacy
import de.gematik.ti.erp.app.orders.repository.CommunicationRepository
import de.gematik.ti.erp.app.prescription.model.Communication
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile.ErxCommunicationDispReq
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile.ErxCommunicationReply
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random

class DemoCommunicationRepository(
    private val dataSource: DemoModeDataSource,
    private val downloadCommunicationResource: DemoDownloadCommunicationResource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : CommunicationRepository {

    private val scope = CoroutineScope(dispatcher)
    override val pharmacyCacheError = MutableSharedFlow<Throwable>()

    override suspend fun downloadCommunications(profileId: ProfileIdentifier) = withContext(dispatcher) {
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

    override suspend fun downloadMissingPharmacy(telematikId: String) {
        withContext(dispatcher) {
            dataSource.cachedPharmacies.value = dataSource.cachedPharmacies.updateAndGet { cachedPharmacies ->
                cachedPharmacies.add(
                    CachedPharmacy(
                        telematikId = telematikId,
                        name = PHARMACY_NAMES.random()
                    )
                )
                cachedPharmacies
            }
        }
    }

    override fun loadSyncedByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?> =
        dataSource.syncedTasks.map { syncedTasks ->
            syncedTasks.find { it.taskId == taskId }
        }.flowOn(dispatcher)


    override fun loadScannedByTaskId(taskId: String): Flow<ScannedTaskData.ScannedTask?> =
        dataSource.scannedTasks.map { scannedTask ->
            scannedTask.find { it.taskId == taskId }
        }.flowOn(dispatcher)

    override fun loadDispReqCommunications(orderId: String) =
        dataSource.communications.mapNotNull { communications ->
            communications
                .filter { it.orderId == orderId && it.profile == ErxCommunicationDispReq }
                .map { it.toSyncedTaskDataCommunication() }
        }.flowOn(dispatcher)

    override fun loadFirstDispReqCommunications(profileId: ProfileIdentifier): Flow<List<Communication>> {
        return loadOrdersByProfileId(profileId).mapNotNull { communications ->
            communications.asSequence().filter {
                it.profileId == profileId && it.profile == ErxCommunicationDispReq
            }
                .map { it.toSyncedTaskDataCommunication() }
                .sortedByDescending { it.sentOn }
                .distinctBy { it.orderId }
                .toList()
        }.flowOn(dispatcher)
    }

    override fun loadRepliedCommunications(taskIds: List<String>) =
        dataSource.communications.mapNotNull { communications ->
            taskIds.mapNotNull { taskId ->
                communications.find { it.taskId == taskId && it.profile == ErxCommunicationReply }
            }.sortedByDescending { it.sentOn }
                .map { it.toSyncedTaskDataCommunication() }
        }.flowOn(dispatcher)

    override fun hasUnreadPrescription(taskIds: List<String>, orderId: String): Flow<Boolean> =
        dataSource.communications.mapNotNull { communications ->
            val booleans = taskIds.map { taskId ->
                communications.find { it.taskId == taskId && !it.consumed }?.consumed == false
            }
            booleans.any { it }
        }.flowOn(dispatcher)

    override fun hasUnreadPrescription(profileId: ProfileIdentifier): Flow<Boolean> =
        dataSource.communications.mapNotNull { communications ->
            communications.any { it.profileId == profileId && !it.consumed }
        }.flowOn(dispatcher)

    override fun unreadOrders(profileId: ProfileIdentifier): Flow<Long> =
        dataSource.communications.mapNotNull { communications ->
            communications.filter { it.profileId == profileId && !it.consumed }
                .distinctBy { it.orderId }
                .size.toLong()
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
            val communicationForTask = DemoModeProfileLinkedCommunication(
                profileId = task.profileId,
                taskId = taskId,
                communicationId = transactionId,
                sentOn = Clock.System.now(),
                sender = pharmacyId,
                consumed = false,
                profile = ErxCommunicationDispReq,
                // these values are kept empty while saving them
                orderId = "",
                payload = "",
                recipient = ""
            )
            dataSource.scannedTasks.value = dataSource.scannedTasks.updateAndGet { scannedTasks ->
                val index = scannedTasks.indexOfFirst { it.taskId == taskId }.takeIf { it != INDEX_OUT_OF_BOUNDS }
                index?.let { nonNullIndex ->
                    scannedTasks[nonNullIndex] = scannedTasks[nonNullIndex].copy(
                        communications = emptyList()
                    )
                }
                scannedTasks
            }
            dataSource.communications.value = dataSource.communications.updateAndGet { communications ->
                communications.add(communicationForTask)
                communications
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadOrdersForActiveProfile() = findActiveProfile().flatMapLatest { loadOrdersByProfileId(it.id) }


    /**
     * Method added so that demoModeProfile02 always loads with some communication
     * and for other profiles we have to add it. [downloadCommunications] method takes
     * in profileId so this can be changed to a different profile later too
     */
    private fun loadOrdersByProfileId(profileId: ProfileIdentifier):
        Flow<MutableList<DemoModeProfileLinkedCommunication>> =
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
