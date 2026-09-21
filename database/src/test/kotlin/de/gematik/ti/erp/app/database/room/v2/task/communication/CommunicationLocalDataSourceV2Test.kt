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

package de.gematik.ti.erp.app.database.room.v2.task.communication

import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.database.api.CommunicationLocalDataSource
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class CommunicationLocalDataSourceV2Test {

    private class CommunicationStore {
        val communications = LinkedHashMap<String, ErpCommunicationEntity>()
        private val _flow = MutableStateFlow<List<ErpCommunicationEntity>>(emptyList())
        val flow: Flow<List<ErpCommunicationEntity>> get() = _flow

        fun put(entity: ErpCommunicationEntity) {
            communications[entity.communicationId] = entity
            _flow.value = communications.values.toList()
        }

        fun putAll(entities: List<ErpCommunicationEntity>) {
            entities.forEach { communications[it.communicationId] = it }
            _flow.value = communications.values.toList()
        }

        fun remove(taskId: String) {
            communications.remove(taskId)
            _flow.value = communications.values.toList()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shared in-memory store (holds all entity maps + reactive task flow)
    // ─────────────────────────────────────────────────────────────────────────

    private class FakeCommunicationDao(private val store: CommunicationStore) : CommunicationDao {
        override suspend fun upsertAll(items: List<ErpCommunicationEntity>) = store.putAll(items)
        override suspend fun getByTaskId(taskId: String): List<ErpCommunicationEntity> =
            store.communications.values.filter { it.taskId == taskId }

        override suspend fun getById(id: String): ErpCommunicationEntity? = store.communications[id]

        override suspend fun deleteByProfileId(profileId: String) {
            store.communications.values.filter { it.profileId == profileId }.forEach {
                store.remove(it.communicationId)
            }
        }

        override suspend fun getOrderIdByTaskIdAndTelematikId(taskId: String, telematikId: String): String? {
            return store.communications.values
                .find { it.taskId == taskId && it.telematikId == telematikId && it.orderId.isNotEmpty() }
                ?.orderId
        }

        override suspend fun getOrderIdByTaskIdAndProfile(
            taskId: String,
            profile: CommunicationProfileV1
        ): String? {
            return store.communications.values
                .find { it.taskId == taskId && it.profile == profile && it.orderId.isNotEmpty() }
                ?.orderId
        }

        override fun observeByOrderAndProfile(orderId: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>> =
            store.flow.map { list -> list.filter { it.orderId == orderId && it.profile == profile } }

        override fun observeByTaskIdAndProfile(taskId: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>> =
            store.flow.map { list -> list.filter { it.taskId == taskId && it.profile == profile } }

        override fun observeByInsuranceAndProfileSorted(insuranceId: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>> =
            store.flow.map { list ->
                list.filter { it.insuranceId == insuranceId && it.profile == profile }
                    .sortedByDescending { it.timeStamp }
            }

        override fun observeRepliesForTaskIds(taskIds: List<String>, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>> =
            store.flow.map { list ->
                list.filter { it.taskId in taskIds && it.profile == profile }
                    .sortedByDescending { it.timeStamp }
            }

        override fun observeRepliesForTaskIdsFromSender(
            taskIds: List<String>,
            sender: String,
            profile: CommunicationProfileV1
        ): Flow<List<ErpCommunicationEntity>> =
            store.flow.map { list ->
                list.filter { it.taskId in taskIds && it.profile == profile && it.telematikId == sender }
                    .sortedByDescending { it.timeStamp }
            }

        override fun observeRepliesForOrderIdFromSender(
            orderId: String,
            profile: CommunicationProfileV1
        ): Flow<List<ErpCommunicationEntity>> =
            store.flow.map { list ->
                list.filter { it.orderId == orderId && it.profile == profile }
                    .sortedByDescending { it.timeStamp }
            }

        override fun observeUnreadCountByOrderAndTaskIds(taskIds: List<String>, orderId: String): Flow<Long> =
            store.flow.map { list ->
                list.count { it.taskId in taskIds && it.orderId == orderId && !it.consumed }.toLong()
            }

        override fun observeUnreadCountByInsurance(insuranceId: String): Flow<Long> =
            store.flow.map { list ->
                list.count { !it.consumed && it.insuranceId == insuranceId }.toLong()
            }

        override fun observeAll(): Flow<List<ErpCommunicationEntity>> = store.flow

        override fun observeUnread(): Flow<List<ErpCommunicationEntity>> =
            store.flow.map { list -> list.filter { !it.consumed } }

        override fun observeDistinctTaskIdsByOrder(orderId: String): Flow<List<String>> =
            store.flow.map { list -> list.filter { it.orderId == orderId }.map { it.taskId }.distinct() }

        override fun observeParentProfileIdByOrder(orderId: String): Flow<String?> =
            // no-op on test, works on task
            flowOf("profile-1")

        override fun observeParentProfileIdByTaskId(taskId: String): Flow<String?> =
            // no-op on test, works on task
            flowOf("profile-1")

        override suspend fun updateConsumedForGroup(
            orderId: String,
            taskId: String,
            payload: String,
            sender: String,
            recipient: String,
            consumed: Boolean
        ): Int {
            val targets = store.communications.values.filter {
                it.orderId == orderId && it.taskId == taskId && it.payload == payload && it.telematikId == sender && it.recipient == recipient
            }
            targets.forEach {
                store.put(it.copy(consumed = consumed))
            }
            return targets.size
        }

        override suspend fun updatePharmacyName(communicationId: String, pharmacyName: String): Int {
            val existing = store.communications[communicationId] ?: return 0
            store.put(existing.copy(pharmacyName = pharmacyName))
            return 1
        }

        override fun observeMaxTimestampByInsurance(insuranceId: String): Flow<Instant?> =
            store.flow.map { list ->
                list.filter { it.insuranceId == insuranceId }.map { it.timeStamp }.maxOrNull()
            }

        override suspend fun getInsuranceIdByTaskId(taskId: String): String =
            store.flow.map { list ->
                list.find { it.taskId == taskId }?.insuranceId
            }.first() ?: "insurance-1"

        override suspend fun getTaskByTaskId(taskId: String): ErpTaskEntity {
            return ErpTaskEntity(
                taskId = taskId,
                taskType = "Prescription",
                parentProfileId = "profile-1",
                accessCode = "123",
                name = "Task Name",
                redeemedOn = null,
                isEuRedeemable = false,
                isEuRedeemableByPatientAuthorization = false,
                lastModified = null,
                lastMedicationDispense = null,
                expiresOn = null,
                acceptUntil = null,
                authoredOn = null,
                status = de.gematik.ti.erp.app.task.model.TaskStatusEnum.Other,
                isIncomplete = false,
                pvsIdentifier = "",
                failureToReport = "",
                organizationId = null,
                practitionerId = null,
                patientId = null,
                insuranceInformationId = "insurance-1",
                medicationId = null,
                medicationRequestId = null,
                accidentInfoId = null,
                deviceRequestId = null,
                multiplePrescriptionId = null,
                scannedOn = null,
                index = 0
            )
        }

        override fun observeUnreadRepliesCount(taskIds: List<String>, profile: CommunicationProfileV1): Flow<Long> =
            store.flow.map { list ->
                list.count { it.taskId in taskIds && it.profile == profile && !it.consumed }.toLong()
            }

        override fun observeUnreadRepliesCountFromSender(taskIds: List<String>, sender: String, profile: CommunicationProfileV1): Flow<Long> =
            store.flow.map { list ->
                list.count { it.taskId in taskIds && it.profile == profile && it.telematikId == sender && !it.consumed }.toLong()
            }
    }

    private fun buildSut(): Pair<CommunicationLocalDataSource, CommunicationStore> {
        val store = CommunicationStore()
        val sut = CommunicationLocalDataSourceV2(
            dao = FakeCommunicationDao(store)
        )
        return sut to store
    }

    private fun entity(
        id: String,
        orderId: String = "order-1",
        taskId: String = "task-1",
        profileId: String = "profile-1",
        telematikId: String = "pharmacy-1",
        kvnr: String = "kvnr-1",
        consumed: Boolean = false,
        payload: String = "payload",
        profile: CommunicationProfileV1 = CommunicationProfileV1.ErxCommunicationDispReq,
        recipient: String = "",
        insuranceId: String? = "profile-1",
        timeStamp: Instant = Clock.System.now()
    ) = ErpCommunicationEntity(
        communicationId = id,
        orderId = orderId,
        taskId = taskId,
        profileId = profileId,
        telematikId = telematikId,
        kvnr = kvnr,
        consumed = consumed,
        payload = payload,
        profile = profile,
        recipient = recipient,
        insuranceId = insuranceId,
        timeStamp = timeStamp
    )

    @Test
    fun loadDispReqCommunications_filtersByOrderAndProfile() = runTest {
        val (sut, store) = buildSut()
        store.putAll(
            listOf(
                entity("1", orderId = "O1", profile = CommunicationProfileV1.ErxCommunicationDispReq),
                entity("2", orderId = "O1", profile = CommunicationProfileV1.ErxCommunicationReply),
                entity("3", orderId = "O2", profile = CommunicationProfileV1.ErxCommunicationDispReq)
            )
        )

        val result = sut.loadDispReqCommunications("O1").first()
        assertEquals(1, result.size)
        assertEquals("1", result[0].communicationId)
    }

    @Test
    fun unreadMessagesCount_calculatesCorrectly() = runTest {
        val (sut, store) = buildSut()
        store.putAll(
            listOf(
                // Order 1: 2 unread dispReq -> 1 count
                entity("1", orderId = "O1", profile = CommunicationProfileV1.ErxCommunicationDispReq, consumed = false),
                entity("2", orderId = "O1", profile = CommunicationProfileV1.ErxCommunicationDispReq, consumed = false),
                // Order 2: 1 read dispReq -> 0 count
                entity("3", orderId = "O2", profile = CommunicationProfileV1.ErxCommunicationDispReq, consumed = true),
                // Replies: unique (taskId, payload) -> 2 unique unconsumed
                entity("4", taskId = "T1", payload = "P1", profile = CommunicationProfileV1.ErxCommunicationReply, consumed = false),
                entity("5", taskId = "T1", payload = "P1", profile = CommunicationProfileV1.ErxCommunicationReply, consumed = false), // Duplicate (T1, P1)
                entity("6", taskId = "T2", payload = "P2", profile = CommunicationProfileV1.ErxCommunicationReply, consumed = false)
            )
        )

        val count = sut.unreadMessagesCount().first()
        assertEquals(3, count) // 1 (Order O1) + 2 (Replies: (T1, P1) and (T2, P2))
    }

    @Test
    fun setCommunicationStatus_updatesGroup() = runTest {
        val (sut, store) = buildSut()
        val e1 = entity("1", orderId = "O1", taskId = "T1", payload = "P1", telematikId = "S1", consumed = false)
        val e2 = entity("2", orderId = "O1", taskId = "T1", payload = "P1", telematikId = "S1", consumed = false)
        val e3 = entity("3", orderId = "O1", taskId = "T2", payload = "P1", telematikId = "S1", consumed = false)
        store.putAll(listOf(e1, e2, e3))

        sut.setCommunicationStatus("1", true)

        assertTrue(store.communications["1"]!!.consumed)
        assertTrue(store.communications["2"]!!.consumed)
        assertFalse(store.communications["3"]!!.consumed) // Different taskId
    }

    @Test
    fun latestCommunicationTimestamp_returnsMax() = runTest {
        val (sut, store) = buildSut()
        val now = Clock.System.now()
        store.putAll(
            listOf(
                entity("1", insuranceId = "P1", timeStamp = now.minus(Duration.parse("1h"))),
                entity("2", insuranceId = "P1", timeStamp = now),
                entity("3", insuranceId = "P2", timeStamp = now.plus(Duration.parse("1h")))
            )
        )

        val latest = sut.latestCommunicationTimestamp("P1").first()
        assertEquals(now, latest)
    }

    @Test
    fun saveCommunications_savesAllMappedMessages() = runTest {
        val (sut, store) = buildSut()
        val bundle = de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel(
            total = 1,
            messages = listOf(
                de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel(
                    id = "msg-123",
                    profile = "dispense-profile",
                    taskId = "task-abc",
                    sender = de.gematik.ti.erp.app.fhir.communication.model.support.CommunicationParticipantErpModel("pharmacy-123"),
                    recipient = de.gematik.ti.erp.app.fhir.communication.model.support.CommunicationParticipantErpModel("patient-456"),
                    sent = null,
                    payload = de.gematik.ti.erp.app.fhir.communication.model.support.DispenseCommunicationPayloadContentErpModel("content-data")
                )
            )
        )

        val savedCount = sut.saveCommunications(bundle)
        assertEquals(1, savedCount)
        val saved = store.communications["msg-123"]
        assertNotNull(saved)
        assertEquals("task-abc", saved.taskId)
        assertEquals("content-data", saved.payload)
        assertEquals("pharmacy-123", saved.telematikId)
    }
}
