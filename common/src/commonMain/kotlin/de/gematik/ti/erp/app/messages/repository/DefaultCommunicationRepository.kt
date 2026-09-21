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
import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSource
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.communication.model.FhirCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.parser.CommunicationParser
import de.gematik.ti.erp.app.fhir.pharmacy.parser.PharmacyBundleParser
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.PharmacyRemoteDataSource
import de.gematik.ti.erp.app.prescription.remote.PrescriptionRemoteDataSource
import de.gematik.ti.erp.app.database.api.CommunicationLocalDataSource as DbCommunicationLocalDataSource
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.task.model.TaskErpModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

private const val COMMUNICATION_MAX_PAGE_SIZE = 50

@Suppress("TooManyFunctions")
class DefaultCommunicationRepository(
    private val taskRemoteDataSource: PrescriptionRemoteDataSource,
    private val pharmacyRemoteDataSource: PharmacyRemoteDataSource,
    private val taskLocalDataSource: TaskLocalDataSource,
    private val communicationLocalDataSource: DbCommunicationLocalDataSource,
    private val communicationParser: CommunicationParser,
    private val pharmacyBundleParser: PharmacyBundleParser,
    private val profiles: ProfileRepository,
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

            val messages = communicationErpModel.messages
            val prescriptionMessages = messages.filter { !it.isDiga }
            val digaMessages = messages.filter { it.isDiga }

            updateDigaMessageTimestamps(digaMessages)
            val messagesWithPharmacyNames = updatePharmacyNames(prescriptionMessages)
            savePrescriptionMessages(communicationErpModel, messagesWithPharmacyNames)
        }.map { savedCount ->
            ResourceResult(savedCount, Unit)
        }

    private suspend fun savePrescriptionMessages(
        bundle: FhirCommunicationBundleErpModel,
        prescriptionMessages: List<FhirCommunicationEntryErpModel>
    ): Int {
        return if (prescriptionMessages.isNotEmpty()) {
            communicationLocalDataSource.saveCommunications(
                bundle.copy(
                    messages = prescriptionMessages,
                    total = prescriptionMessages.size
                )
            )
        } else {
            0
        }
    }

    private suspend fun updateDigaMessageTimestamps(
        messages: List<FhirCommunicationEntryErpModel>
    ) {
        messages.forEach { message ->
            message.taskId?.let {
                taskLocalDataSource.updateDigaCommunicationSent(
                    taskId = it,
                    time = message.sent?.toInstant() ?: Clock.System.now()
                )
            }
        }
    }

    private suspend fun updatePharmacyNames(
        messages: List<FhirCommunicationEntryErpModel>
    ): List<FhirCommunicationEntryErpModel> {
        messages.forEach { message ->

            val telematikId = if (message is FhirReplyCommunicationEntryErpModel) message.sender?.identifier else message.recipient?.identifier
            if (telematikId != null) {
                pharmacyRemoteDataSource.searchPharmacyByTelematikId(telematikId) {
                    // ignore unauthorized here, as we are in a background sync
                }.onSuccess { json ->
                    val pharmacyCollection = pharmacyBundleParser.extract(json) as FhirPharmacyErpModelCollection
                    message.pharmacyName = pharmacyCollection.entries.firstOrNull()?.name
                }
            }
        }
        return messages
    }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        communicationLocalDataSource.latestCommunicationTimestamp(profileId).first()

    override fun loadSyncedByTaskId(taskId: String): Flow<TaskErpModel.Synced.Prescription?> =
        taskLocalDataSource.loadTaskByTaskId(taskId).map { it as? TaskErpModel.Synced.Prescription }

    override fun loadScannedByTaskId(taskId: String): Flow<TaskErpModel.Scanned?> =
        taskLocalDataSource.loadTaskByTaskId(taskId).map { it as? TaskErpModel.Scanned }

    override fun loadDispReqCommunications(orderId: String): Flow<List<CommunicationErpModel>> =
        communicationLocalDataSource.loadDispReqCommunications(orderId)

    override fun loadDispReqCommunicationsByProfileId(profileId: ProfileIdentifier): Flow<List<CommunicationErpModel>> =
        communicationLocalDataSource.loadDispReqCommunicationsByProfileId(profileId)

    override fun loadDispReqCommunicationsByTaskId(taskId: String): Flow<List<CommunicationErpModel>> =
        communicationLocalDataSource.loadDispReqCommunicationsByTaskId(taskId)

    override fun loadRepliedCommunications(taskIds: List<String>, telematikId: String): Flow<List<CommunicationErpModel>> =
        communicationLocalDataSource.loadRepliedCommunications(
            taskIds = taskIds,
            telematikId = telematikId
        )

    override fun loadRepliedCommunications(orderId: String): Flow<List<CommunicationErpModel>> =
        communicationLocalDataSource.loadRepliedCommunications(orderId = orderId)

    override fun loadRepliedCommunications(orderId: String, telematikId: String): Flow<List<CommunicationErpModel>> =
        communicationLocalDataSource.loadRepliedCommunications(orderId = orderId, telematikId = telematikId)

    override fun loadRepliedCommunicationsByProfileId(profileId: ProfileIdentifier): Flow<List<CommunicationErpModel>> =
        communicationLocalDataSource.loadRepliedCommunicationsByProfileId(profileId = profileId)

    override fun loadAllRepliedCommunications(taskIds: List<String>): Flow<List<CommunicationErpModel>> =
        communicationLocalDataSource.loadAllRepliedCommunications(taskIds)

    override fun getAllUnreadMessages(): Flow<List<CommunicationErpModel>> =
        communicationLocalDataSource.getAllUnreadMessages()

    override fun unreadMessagesCount(): Flow<Long> =
        communicationLocalDataSource.unreadMessagesCount()

    override suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean) {
        communicationLocalDataSource.setCommunicationStatus(communicationId, consumed)
    }

    override suspend fun updatePharmacyName(communicationId: String, pharmacyName: String) {
        communicationLocalDataSource.updatePharmacyName(communicationId, pharmacyName)
    }

    override fun taskIdsByOrder(orderId: String): Flow<List<String>> =
        communicationLocalDataSource.taskIdsByOrder(orderId)

    override fun hasUnreadDispenseMessage(taskIds: List<String>, orderId: String): Flow<Boolean> =
        communicationLocalDataSource.hasUnreadDispenseMessage(taskIds, orderId)

    override fun hasUnreadDispenseMessage(profileId: ProfileIdentifier): Flow<Boolean> =
        communicationLocalDataSource.hasUnreadDispenseMessage(profileId)

    override fun unreadPrescriptionsInAllOrders(profileId: ProfileIdentifier): Flow<Long> =
        communicationLocalDataSource.unreadPrescriptionsInAllOrders(profileId)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun profileByOrderId(orderId: String): Flow<ProfileErpModel> =
        communicationLocalDataSource.getProfileIdByOrderId(orderId).flatMapLatest { pid ->
            if (pid == null) kotlinx.coroutines.flow.emptyFlow()
            else profiles.getProfileById(pid)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun profileByTaskId(taskId: String): Flow<ProfileErpModel> =
        communicationLocalDataSource.getProfileIdByTaskId(taskId).flatMapLatest { pid ->
            if (pid == null) kotlinx.coroutines.flow.emptyFlow()
            else profiles.getProfileById(pid)
        }

    override suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String) {
        // Persist via communication DB API (bridged V1/V2) so it works for Realm and Room
        communicationLocalDataSource.saveLocalCommunication(taskId, pharmacyId, transactionId)
    }

    override suspend fun hasUnreadRepliedMessages(taskIds: List<String>, telematikId: String): Flow<Boolean> =
        communicationLocalDataSource.hasUnreadRepliedMessages(taskIds, telematikId)
}
