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

import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Suppress("TooManyFunctions")
interface CommunicationRepository {

    suspend fun downloadCommunications(profileId: ProfileIdentifier): Result<Unit>
    suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourcePaging.ResourceResult<Unit>>

    suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant?
    fun loadSyncedByTaskId(taskId: String): Flow<TaskErpModel.Synced.Prescription?>
    fun loadScannedByTaskId(taskId: String): Flow<TaskErpModel.Scanned?>

    fun loadDispReqCommunications(orderId: String): Flow<List<CommunicationErpModel>>
    fun loadDispReqCommunicationsByProfileId(profileId: ProfileIdentifier): Flow<List<CommunicationErpModel>>
    fun loadDispReqCommunicationsByTaskId(taskId: String): Flow<List<CommunicationErpModel>>
    fun loadRepliedCommunications(taskIds: List<String>, telematikId: String): Flow<List<CommunicationErpModel>>
    fun loadRepliedCommunications(orderId: String): Flow<List<CommunicationErpModel>>
    fun loadRepliedCommunications(orderId: String, telematikId: String): Flow<List<CommunicationErpModel>>
    fun loadRepliedCommunicationsByProfileId(profileId: ProfileIdentifier): Flow<List<CommunicationErpModel>>

    fun taskIdsByOrder(orderId: String): Flow<List<String>>
    fun loadAllRepliedCommunications(taskIds: List<String>): Flow<List<CommunicationErpModel>>
    fun getAllUnreadMessages(): Flow<List<CommunicationErpModel>>
    fun unreadMessagesCount(): Flow<Long>
    fun hasUnreadDispenseMessage(taskIds: List<String>, orderId: String): Flow<Boolean>
    fun hasUnreadDispenseMessage(profileId: ProfileIdentifier): Flow<Boolean>
    fun unreadPrescriptionsInAllOrders(profileId: ProfileIdentifier): Flow<Long>
    fun profileByOrderId(orderId: String): Flow<ProfileErpModel>
    fun profileByTaskId(taskId: String): Flow<ProfileErpModel>
    suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean)
    suspend fun updatePharmacyName(communicationId: String, pharmacyName: String)
    suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String)
    suspend fun hasUnreadRepliedMessages(taskIds: List<String>, telematikId: String): Flow<Boolean>
}
