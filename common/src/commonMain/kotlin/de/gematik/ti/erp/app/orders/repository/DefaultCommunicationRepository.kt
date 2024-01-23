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

package de.gematik.ti.erp.app.orders.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.fhir.model.extractPharmacyServices
import de.gematik.ti.erp.app.prescription.model.Communication
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionLocalDataSource
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRemoteDataSource
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

private const val COMMUNICATION_MAX_PAGE_SIZE = 50

@Suppress("TooManyFunctions")
class DefaultCommunicationRepository(
    private val taskLocalDataSource: PrescriptionLocalDataSource,
    private val taskRemoteDataSource: PrescriptionRemoteDataSource,
    private val communicationLocalDataSource: CommunicationLocalDataSource,
    private val cacheLocalDataSource: PharmacyCacheLocalDataSource,
    private val cacheRemoteDataSource: PharmacyCacheRemoteDataSource,
    private val dispatchers: DispatchProvider
) : ResourcePaging<Unit>(dispatchers, COMMUNICATION_MAX_PAGE_SIZE), CommunicationRepository {
    private val scope = CoroutineScope(dispatchers.io)
    private val queue = Channel<String>(capacity = Channel.BUFFERED)

    override val pharmacyCacheError = MutableSharedFlow<Throwable>()

    init {
        scope.launch {
            for (telematikId in queue) {
                cacheRemoteDataSource
                    .searchPharmacy(telematikId)
                    .onSuccess {
                        val pharmacy = extractPharmacyServices(it).pharmacies.firstOrNull()
                        pharmacy?.let {
                            cacheLocalDataSource.savePharmacy(pharmacy.telematikId, pharmacy.name)
                        }
                    }
                    .onFailure {
                        Napier.e("Failed to download pharmacy for cache with telematikId $telematikId", it)
                        pharmacyCacheError.tryEmit(it)
                    }
            }
        }
    }

    override val tag: String = "CommunicationRepository"

    override suspend fun downloadCommunications(profileId: ProfileIdentifier) = downloadPaged(profileId)

    override suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourceResult<Unit>> =
        taskRemoteDataSource.fetchCommunications(
            profileId = profileId,
            count = count,
            lastKnownUpdate = timestamp
        ).mapCatching { communications ->
            taskLocalDataSource.saveCommunications(communications)
        }.map {
            ResourceResult(it, Unit)
        }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        communicationLocalDataSource.latestCommunicationTimestamp(profileId).first()

    override fun loadPharmacies(): Flow<List<CachedPharmacy>> =
        cacheLocalDataSource.loadPharmacies().flowOn(dispatchers.io)

    override suspend fun downloadMissingPharmacy(telematikId: String) {
        queue.send(telematikId)
    }

    override fun loadSyncedByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?> =
        taskLocalDataSource.loadSyncedTaskByTaskId(taskId)
            .flowOn(dispatchers.io)

    override fun loadScannedByTaskId(taskId: String): Flow<ScannedTaskData.ScannedTask?> =
        taskLocalDataSource.loadScannedTaskByTaskId(taskId)
            .flowOn(dispatchers.io)

    override fun loadDispReqCommunications(orderId: String): Flow<List<Communication>> =
        communicationLocalDataSource.loadDispReqCommunications(orderId).flowOn(dispatchers.io)

    override fun loadFirstDispReqCommunications(profileId: ProfileIdentifier): Flow<List<Communication>> =
        communicationLocalDataSource.loadFirstDispReqCommunications(profileId).flowOn(dispatchers.io)

    override fun loadRepliedCommunications(taskIds: List<String>): Flow<List<Communication>> =
        communicationLocalDataSource.loadRepliedCommunications(taskIds = taskIds).flowOn(dispatchers.io)

    override fun loadCommunicationsWithTaskId(taskIds: List<String>): Flow<List<Communication>> =
        communicationLocalDataSource.loadCommunicationsWithTaskId(taskIds = taskIds).flowOn(dispatchers.io)

    override fun hasUnreadPrescription(taskIds: List<String>, orderId: String): Flow<Boolean> =
        communicationLocalDataSource.hasUnreadPrescription(taskIds, orderId).flowOn(dispatchers.io)

    override fun hasUnreadPrescription(profileId: ProfileIdentifier): Flow<Boolean> =
        communicationLocalDataSource.hasUnreadPrescription(profileId).flowOn(dispatchers.io)

    override fun unreadOrders(profileId: ProfileIdentifier): Flow<Long> =
        communicationLocalDataSource.unreadOrders(profileId).flowOn(dispatchers.io)
    override fun unreadPrescriptionsInAllOrders(profileId: ProfileIdentifier): Flow<Long> =
        communicationLocalDataSource.unreadPrescriptionsInAllOrders(profileId).flowOn(dispatchers.io)

    override fun taskIdsByOrder(orderId: String): Flow<List<String>> =
        communicationLocalDataSource.taskIdsByOrder(orderId).flowOn(dispatchers.io)

    override suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean) {
        withContext(dispatchers.io) {
            communicationLocalDataSource.setCommunicationStatus(communicationId, consumed)
        }
    }

    override suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String) {
        taskLocalDataSource.saveLocalCommunication(taskId, pharmacyId, transactionId)
    }
}
