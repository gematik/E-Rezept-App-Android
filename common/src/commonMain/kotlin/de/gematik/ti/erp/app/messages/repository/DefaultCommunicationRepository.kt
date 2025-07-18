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

package de.gematik.ti.erp.app.messages.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.diga.local.DigaLocalDataSource
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.parser.CommunicationParser
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionLocalDataSource
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRemoteDataSource
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

private const val COMMUNICATION_MAX_PAGE_SIZE = 50

@Suppress("TooManyFunctions")
class DefaultCommunicationRepository(
    private val taskRemoteDataSource: PrescriptionRemoteDataSource,
    private val taskLocalDataSource: PrescriptionLocalDataSource,
    private val communicationLocalDataSource: CommunicationLocalDataSource,
    private val digaLocalDataSource: DigaLocalDataSource,
    private val cacheLocalDataSource: PharmacyCacheLocalDataSource,
    private val communicationParser: CommunicationParser,
    dispatchers: DispatchProvider
) : ResourcePaging<Unit>(dispatchers, COMMUNICATION_MAX_PAGE_SIZE), CommunicationRepository {

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
            val communicationErpModel = communicationParser.extract(communications)
                ?: run {
                    Napier.w("Failed to parse non-empty communications bundle")
                    return@mapCatching 0
                }

            val prescriptionMessages = communicationErpModel.messages.filter { !it.isDiga }

            val digaMessages = communicationErpModel.messages.filter { it.isDiga }
            updateDigaMessageTimestamps(digaMessages)

            val totalMessages = prescriptionMessages.size

            if (totalMessages > 0) {
                taskLocalDataSource.saveCommunications(
                    communicationErpModel.copy(
                        messages = prescriptionMessages,
                        total = totalMessages
                    )
                )
            } else {
                0
            }
        }.map { savedCount ->
            ResourceResult(savedCount, Unit)
        }

    private suspend fun updateDigaMessageTimestamps(
        messages: List<FhirCommunicationEntryErpModel>
    ) {
        messages.forEach { message ->
            message.taskId?.let {
                digaLocalDataSource.updateDigaCommunicationSent(
                    taskId = it,
                    time = message.sent?.toInstant() ?: Clock.System.now()
                )
            }
        }
    }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        communicationLocalDataSource.latestCommunicationTimestamp(profileId).first()

    override fun loadPharmacies(): Flow<List<CachedPharmacy>> =
        cacheLocalDataSource.loadPharmacies()

    override fun loadSyncedByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?> =
        taskLocalDataSource.loadSyncedTaskByTaskId(taskId)

    override fun loadScannedByTaskId(taskId: String): Flow<ScannedTaskData.ScannedTask?> =
        taskLocalDataSource.loadScannedTaskByTaskId(taskId)

    override fun loadDispReqCommunications(orderId: String): Flow<List<Communication>> =
        communicationLocalDataSource.loadDispReqCommunications(orderId)

    override fun loadDispReqCommunicationsByProfileId(profileId: ProfileIdentifier): Flow<List<Communication>> =
        communicationLocalDataSource.loadDispReqCommunicationsByProfileId(profileId)

    override fun loadRepliedCommunications(taskIds: List<String>, telematikId: String): Flow<List<Communication>> =
        communicationLocalDataSource.loadRepliedCommunications(
            taskIds = taskIds,
            telematikId = telematikId
        )

    override fun loadAllRepliedCommunications(taskIds: List<String>): Flow<List<Communication>> =
        communicationLocalDataSource.loadAllRepliedCommunications(
            taskIds = taskIds
        )

    override fun hasUnreadDispenseMessage(taskIds: List<String>, orderId: String): Flow<Boolean> =
        communicationLocalDataSource.hasUnreadDispenseMessage(taskIds, orderId)

    override fun hasUnreadDispenseMessage(profileId: ProfileIdentifier): Flow<Boolean> =
        communicationLocalDataSource.hasUnreadDispenseMessage(profileId)

    override fun unreadMessagesCount(): Flow<Long> =
        communicationLocalDataSource.unreadMessagesCount()

    override fun getAllUnreadMessages(): Flow<List<Communication>> =
        communicationLocalDataSource.getAllUnreadMessages()

    override fun unreadPrescriptionsInAllOrders(profileId: ProfileIdentifier): Flow<Long> =
        communicationLocalDataSource.unreadPrescriptionsInAllOrders(profileId)

    override fun taskIdsByOrder(orderId: String): Flow<List<String>> =
        communicationLocalDataSource.taskIdsByOrder(orderId)

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
