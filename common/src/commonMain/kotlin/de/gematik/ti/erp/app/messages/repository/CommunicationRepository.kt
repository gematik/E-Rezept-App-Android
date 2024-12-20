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

import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.fhir.model.Pharmacy
import de.gematik.ti.erp.app.prescription.model.Communication
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Suppress("TooManyFunctions")
interface CommunicationRepository {

    val pharmacyCacheError: Channel<Throwable>
    val pharmacyDownloaded: Channel<Pharmacy?>

    suspend fun downloadCommunications(profileId: ProfileIdentifier): Result<Unit>
    suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourcePaging.ResourceResult<Unit>>

    suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant?
    fun loadPharmacies(): Flow<List<CachedPharmacy>>
    suspend fun downloadMissingPharmacy(telematikId: String): Result<CachedPharmacy?>
    fun loadSyncedByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?>
    fun loadScannedByTaskId(taskId: String): Flow<ScannedTaskData.ScannedTask?>

    fun loadDispReqCommunications(orderId: String): Flow<List<Communication>>
    fun loadFirstDispReqCommunications(profileId: ProfileIdentifier): Flow<List<Communication>>
    fun loadRepliedCommunications(taskIds: List<String>, telematikId: String): Flow<List<Communication>>
    fun hasUnreadDispenseMessage(taskIds: List<String>, orderId: String): Flow<Boolean>
    fun hasUnreadDispenseMessage(profileId: ProfileIdentifier): Flow<Boolean>
    fun unreadMessagesCount(consumed: Boolean): Flow<Long>
    fun unreadPrescriptionsInAllOrders(profileId: ProfileIdentifier): Flow<Long>
    fun taskIdsByOrder(orderId: String): Flow<List<String>>
    fun profileByOrderId(orderId: String): Flow<ProfilesData.Profile>
    suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean)
    suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String)
    suspend fun hasUnreadRepliedMessages(taskIds: List<String>, telematikId: String): Flow<Boolean>
}
