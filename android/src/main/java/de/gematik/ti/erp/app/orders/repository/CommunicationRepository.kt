/*
 * Copyright (c) 2023 gematik GmbH
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
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import de.gematik.ti.erp.app.prescription.repository.LocalDataSource
import de.gematik.ti.erp.app.prescription.repository.RemoteDataSource
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant

private const val CommunicationsMaxPageSize = 50

class CommunicationRepository(
    private val taskLocalDataSource: LocalDataSource,
    private val taskRemoteDataSource: RemoteDataSource,
    private val communicationLocalDataSource: CommunicationLocalDataSource,
    private val cacheLocalDataSource: PharmacyCacheLocalDataSource,
    private val cacheRemoteDataSource: PharmacyCacheRemoteDataSource,
    private val dispatchers: DispatchProvider
) : ResourcePaging<Unit>(dispatchers, CommunicationsMaxPageSize) {
    private val scope = CoroutineScope(dispatchers.IO)
    private val queue = Channel<String>(capacity = Channel.BUFFERED)

    val pharmacyCacheError = MutableSharedFlow<Throwable>()

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

    suspend fun downloadCommunications(profileId: ProfileIdentifier) = downloadPaged(profileId)

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

    fun loadPharmacies(): Flow<List<CachedPharmacy>> =
        cacheLocalDataSource.loadPharmacies().flowOn(dispatchers.IO)

    suspend fun downloadMissingPharmacy(telematikId: String) {
        queue.send(telematikId)
    }

    fun loadPrescriptionName(taskId: String) =
        taskLocalDataSource.loadSyncedTaskByTaskId(taskId).map {
            it?.medicationName()
        }.flowOn(dispatchers.IO)

    fun loadDispReqCommunications(orderId: String) =
        communicationLocalDataSource.loadDispReqCommunications(orderId).flowOn(dispatchers.IO)

    fun loadFirstDispReqCommunications(profileId: ProfileIdentifier) =
        communicationLocalDataSource.loadFirstDispReqCommunications(profileId).flowOn(dispatchers.IO)

    fun loadRepliedCommunications(taskIds: List<String>) =
        communicationLocalDataSource.loadRepliedCommunications(taskIds = taskIds).flowOn(dispatchers.IO)

    fun hasUnreadMessages(taskIds: List<String>, orderId: String) =
        communicationLocalDataSource.hasUnreadMessages(taskIds, orderId).flowOn(dispatchers.IO)

    fun hasUnreadMessages(profileId: ProfileIdentifier) =
        communicationLocalDataSource.hasUnreadMessages(profileId).flowOn(dispatchers.IO)

    fun taskIdsByOrder(orderId: String) =
        communicationLocalDataSource.taskIdsByOrder(orderId).flowOn(dispatchers.IO)

    suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean) {
        withContext(dispatchers.IO) {
            communicationLocalDataSource.setCommunicationStatus(communicationId, consumed)
        }
    }
}
