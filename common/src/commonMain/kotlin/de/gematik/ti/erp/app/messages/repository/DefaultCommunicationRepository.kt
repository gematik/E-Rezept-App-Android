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

package de.gematik.ti.erp.app.messages.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.fhir.model.Pharmacy
import de.gematik.ti.erp.app.fhir.model.extractPharmacyServices
import de.gematik.ti.erp.app.prescription.model.Communication
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionLocalDataSource
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRemoteDataSource
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Instant
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resumeWithException

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
    private val scope = CoroutineScope(dispatchers.io) // todo: forced scope, not testable
    private val queue = Channel<String>(capacity = Channel.BUFFERED)

    override val pharmacyCacheError = Channel<Throwable>()
    override val pharmacyDownloaded = Channel<Pharmacy?>()

    init {
        scope.launch {
            for (telematikId in queue) {
                cacheRemoteDataSource
                    .searchPharmacy(telematikId)
                    .onSuccess {
                        val pharmacy = extractPharmacyServices(it).pharmacies.firstOrNull()
                        pharmacy?.let {
                            cacheLocalDataSource.savePharmacy(pharmacy.telematikId, pharmacy.name)
                            pharmacyDownloaded.send(pharmacy)
                        } ?: run {
                            Napier.e("Pharmacy not found for telematikId $telematikId")
                            pharmacyDownloaded.send(null)
                        }
                    }
                    .onFailure {
                        Napier.e("Failed to download pharmacy for cache with telematikId $telematikId", it)
                        pharmacyCacheError.send(it)
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
            Napier.i("Communication Json: $communications")
            taskLocalDataSource.saveCommunications(communications)
        }.map {
            // we get the count of communications, but we don't use it??
            ResourceResult(it, Unit)
        }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        communicationLocalDataSource.latestCommunicationTimestamp(profileId).first()

    override fun loadPharmacies(): Flow<List<CachedPharmacy>> =
        cacheLocalDataSource.loadPharmacies().flowOn(dispatchers.io)

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun downloadMissingPharmacy(telematikId: String): Result<CachedPharmacy?> {
        queue.send(telematikId)
        return suspendCancellableCoroutine { continuation ->
            val resumed = AtomicBoolean(false) // Flag to track if continuation is already resumed
            // Launch a coroutine to listen for the first emit of success or error
            val successJob = scope.launch {
                try {
                    val pharmacy = pharmacyDownloaded.receive()
                    if (resumed.compareAndSet(false, true)) {
                        continuation.resume(Result.success(pharmacy?.toCachedPharmacy()), onCancellation = null)
                    }
                } catch (e: Throwable) {
                    if (resumed.compareAndSet(false, true)) {
                        continuation.resumeWithException(e)
                    }
                }
            }

            val errorJob = scope.launch {
                val result = pharmacyCacheError.receive()
                try {
                    if (resumed.compareAndSet(false, true)) {
                        continuation.resume(Result.failure(result), onCancellation = null)
                    }
                } catch (e: Throwable) {
                    if (resumed.compareAndSet(false, true)) {
                        continuation.resumeWithException(e)
                    }
                }
            }

            // If the coroutine is cancelled, cancel the jobs as well
            continuation.invokeOnCancellation {
                successJob.cancel()
                errorJob.cancel()
            }
        }
    }

    override fun loadSyncedByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?> =
        taskLocalDataSource.loadSyncedTaskByTaskId(taskId)
            .flowOn(dispatchers.io)

    override fun loadScannedByTaskId(taskId: String): Flow<ScannedTaskData.ScannedTask?> =
        taskLocalDataSource.loadScannedTaskByTaskId(taskId)
            .flowOn(dispatchers.io)

    override fun loadDispReqCommunications(orderId: String): Flow<List<Communication>> =
        communicationLocalDataSource.loadDispReqCommunications(orderId).flowOn(dispatchers.io)

    override fun loadDispReqCommunicationsByProfileId(profileId: ProfileIdentifier): Flow<List<Communication>> =
        communicationLocalDataSource.loadDispReqCommunicationsByProfileId(profileId).flowOn(dispatchers.io)

    override fun loadRepliedCommunications(taskIds: List<String>, telematikId: String): Flow<List<Communication>> =
        communicationLocalDataSource.loadRepliedCommunications(
            taskIds = taskIds,
            telematikId = telematikId
        ).flowOn(dispatchers.io)

    override fun loadAllRepliedCommunications(taskIds: List<String>): Flow<List<Communication>> =
        communicationLocalDataSource.loadAllRepliedCommunications(
            taskIds = taskIds
        ).flowOn(dispatchers.io)

    override fun hasUnreadDispenseMessage(taskIds: List<String>, orderId: String): Flow<Boolean> =
        communicationLocalDataSource.hasUnreadDispenseMessage(taskIds, orderId).flowOn(dispatchers.io)

    override fun hasUnreadDispenseMessage(profileId: ProfileIdentifier): Flow<Boolean> =
        communicationLocalDataSource.hasUnreadDispenseMessage(profileId).flowOn(dispatchers.io)

    override fun unreadMessagesCount(): Flow<Long> =
        communicationLocalDataSource.unreadMessagesCount()

    override fun getAllUnreadMessages(): Flow<List<Communication>> =
        communicationLocalDataSource.getAllUnreadMessages().flowOn(dispatchers.io)

    override fun unreadPrescriptionsInAllOrders(profileId: ProfileIdentifier): Flow<Long> =
        communicationLocalDataSource.unreadPrescriptionsInAllOrders(profileId).flowOn(dispatchers.io)

    override fun taskIdsByOrder(orderId: String): Flow<List<String>> =
        communicationLocalDataSource.taskIdsByOrder(orderId).flowOn(dispatchers.io)

    override fun profileByOrderId(orderId: String): Flow<ProfilesData.Profile> {
        return communicationLocalDataSource.getProfileByOrderId(orderId)
    }

    override suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean) {
        communicationLocalDataSource.setCommunicationStatus(communicationId, consumed)
    }

    override suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String) {
        taskLocalDataSource.saveLocalCommunication(taskId, pharmacyId, transactionId)
    }

    override suspend fun hasUnreadRepliedMessages(taskIds: List<String>, telematikId: String): Flow<Boolean> =
        communicationLocalDataSource.hasUnreadRepliedMessages(taskIds, telematikId)
}
