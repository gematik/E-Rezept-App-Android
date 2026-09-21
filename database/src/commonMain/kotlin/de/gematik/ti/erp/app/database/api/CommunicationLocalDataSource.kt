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
package de.gematik.ti.erp.app.database.api

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface CommunicationLocalDataSource {
    fun loadDispReqCommunications(orderId: String): Flow<List<CommunicationErpModel>>
    fun loadDispReqCommunicationsByProfileId(profileId: String): Flow<List<CommunicationErpModel>>
    fun loadDispReqCommunicationsByTaskId(taskId: String): Flow<List<CommunicationErpModel>>
    fun loadRepliedCommunications(taskIds: List<String>, telematikId: String?): Flow<List<CommunicationErpModel>>
    fun loadRepliedCommunications(orderId: String?): Flow<List<CommunicationErpModel>>
    fun loadRepliedCommunications(orderId: String, telematikId: String): Flow<List<CommunicationErpModel>>
    fun loadRepliedCommunicationsByProfileId(profileId: String): Flow<List<CommunicationErpModel>>
    fun loadAllRepliedCommunications(taskIds: List<String>): Flow<List<CommunicationErpModel>>
    fun hasUnreadDispenseMessage(taskIds: List<String>, orderId: String): Flow<Boolean>
    fun hasUnreadDispenseMessage(profileId: String): Flow<Boolean>
    fun unreadMessagesCount(): Flow<Long>
    fun getAllUnreadMessages(): Flow<List<CommunicationErpModel>>
    fun unreadPrescriptionsInAllOrders(profileId: String): Flow<Long>
    fun taskIdsByOrder(orderId: String): Flow<List<String>>
    fun getProfileIdByOrderId(orderId: String): Flow<String?>
    fun getProfileIdByTaskId(taskId: String): Flow<String?>
    suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean)
    suspend fun updatePharmacyName(communicationId: String, pharmacyName: String)
    fun latestCommunicationTimestamp(profileId: String): Flow<Instant?>
    fun hasUnreadRepliedMessages(taskIds: List<String>, telematikId: String?): Flow<Boolean>

    // Save a locally created communication for a scanned task (DispReq)
    suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String)

    // Save communications from ERP models directly. needed for Migration
    suspend fun saveCommunications(communicationModels: List<CommunicationErpModel>): Int

    // Save downloaded communications (works for both Realm and Room via bridge). Returns number of saved items.
    suspend fun saveCommunications(entities: FhirCommunicationBundleErpModel): Int
}
