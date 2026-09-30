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

package de.gematik.ti.erp.app.database.room.v2.datasource

import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSource
import de.gematik.ti.erp.app.database.room.v2.invoice.InvoiceDao
import de.gematik.ti.erp.app.database.room.v2.invoice.InvoiceRoomEntity
import de.gematik.ti.erp.app.database.room.v2.task.TaskLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.task.accident.ErpAccidentInfoEntity
import de.gematik.ti.erp.app.database.room.v2.task.communication.CommunicationDao
import de.gematik.ti.erp.app.database.room.v2.task.communication.ErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.diga.ErpTaskMedicationDeviceRequestEntity
import de.gematik.ti.erp.app.database.room.v2.task.insuranceinformation.ErpInsuranceInformationEntity
import de.gematik.ti.erp.app.database.room.v2.task.insuranceinformation.InsuranceInformationDao
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpIngredientEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationDispenseEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationWithRefs
import de.gematik.ti.erp.app.database.room.v2.task.medication.IngredientDao
import de.gematik.ti.erp.app.database.room.v2.task.medication.MedicationDispenseDao
import de.gematik.ti.erp.app.database.room.v2.task.medicationrequest.ErpMedicationRequestEntity
import de.gematik.ti.erp.app.database.room.v2.task.multipleprescription.ErpMultiplePrescriptionEntity
import de.gematik.ti.erp.app.database.room.v2.task.organization.ErpOrganizationEntity
import de.gematik.ti.erp.app.database.room.v2.task.organization.OrganizationDao
import de.gematik.ti.erp.app.database.room.v2.task.patient.ErpPatientEntity
import de.gematik.ti.erp.app.database.room.v2.task.practitioner.ErpPractitionerEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefs
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefsDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.TaskTypeValues
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.FhirTaskDataErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyTextPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationTypeErpModel
import de.gematik.ti.erp.app.fhir.FhirTaskMetaDataErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.CommunicationParticipantErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirCoverageErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPractitionerErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskStatusErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val NOW: Instant = Instant.parse("2024-06-01T10:00:00Z")
private val AUTHORED_ON: Instant = Instant.parse("2024-05-01T08:00:00Z")
private val SCANNED_ON: Instant = Instant.parse("2024-05-15T12:00:00Z")

@OptIn(ExperimentalCoroutinesApi::class)
class TaskLocalDataSourceV2Test {

    // ─────────────────────────────────────────────────────────────────────────
    // Shared in-memory store (holds all entity maps + reactive task flow)
    // ─────────────────────────────────────────────────────────────────────────

    private class TaskStore {
        val tasks = LinkedHashMap<String, ErpTaskEntity>()
        val patients = LinkedHashMap<String, ErpPatientEntity>()
        val practitioners = LinkedHashMap<String, ErpPractitionerEntity>()
        val organizations = LinkedHashMap<String, ErpOrganizationEntity>()
        val insurances = LinkedHashMap<String, ErpInsuranceInformationEntity>()
        val communications = LinkedHashMap<String, ErpCommunicationEntity>()
        val medicationDispenses = LinkedHashMap<String, ErpMedicationDispenseEntity>()
        val invoices = LinkedHashMap<String, InvoiceRoomEntity>()

        private val _flow = MutableStateFlow<List<ErpTaskEntity>>(emptyList())
        val flow: Flow<List<ErpTaskEntity>> get() = _flow

        private val _communicationFlow = MutableStateFlow<List<ErpCommunicationEntity>>(emptyList())
        val communicationFlow: Flow<List<ErpCommunicationEntity>> get() = _communicationFlow

        fun put(entity: ErpTaskEntity) {
            tasks[entity.taskId] = entity
            _flow.value = tasks.values.toList()
        }

        fun putCommunication(entity: ErpCommunicationEntity) {
            communications[entity.communicationId] = entity
            _communicationFlow.value = communications.values.toList()
        }

        fun triggerCommunicationUpdate() {
            _communicationFlow.value = communications.values.toList()
        }

        fun remove(taskId: String) {
            tasks.remove(taskId)
            _flow.value = tasks.values.toList()
        }

        fun buildWithRefs(task: ErpTaskEntity): ErpTaskWithRefs = ErpTaskWithRefs(
            task = task,
            organization = task.organizationId?.let { organizations[it] },
            practitioner = task.practitionerId?.let { practitioners[it] },
            patient = task.patientId?.let { patients[it] },
            insuranceInformation = task.insuranceInformationId?.let { insurances[it] },
            medication = null as ErpMedicationWithRefs?,
            medicationRequest = null,
            deviceRequest = null,
            multiplePrescription = null,
            accidentInfo = null,
            medicationDispenses = emptyList(),
            communications = emptyList()
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fake DAOs
    // ─────────────────────────────────────────────────────────────────────────

    private class FakeErpTaskDao(private val store: TaskStore) : ErpTaskDao {
        override suspend fun upsertDeviceRequests(items: List<ErpTaskMedicationDeviceRequestEntity>) {
            // no-op in fake DAO for now
        }

        override suspend fun upsertAccidentInfos(items: List<ErpAccidentInfoEntity>) {
            // no-op in fake DAO for now
        }

        override suspend fun upsertAll(items: List<ErpTaskEntity>) = items.forEach { store.put(it) }
        override suspend fun getByProfile(profileId: String) = store.tasks.values.filter { it.parentProfileId == profileId }
        override suspend fun getByTaskId(taskId: String) = store.tasks[taskId]
        override suspend fun deleteByTaskId(taskId: String) = store.remove(taskId)
        override suspend fun deleteByProfileId(profileId: String) {
            store.tasks.values.removeAll { it.parentProfileId == profileId }
        }

        override suspend fun clearAll() {
            store.tasks.clear(); store.put(ErpTaskEntity("__clear__", "", null, "", null, null)); store.remove("__clear__")
        }

        override fun observeAllIds(): Flow<List<String>> = store.flow.map { it.map { t -> t.taskId } }
        override fun observeIdsByProfile(profileId: String): Flow<List<String>> =
            store.flow.map { tasks -> tasks.filter { it.parentProfileId == profileId }.map { it.taskId } }

        override fun observeLatestModified(profileId: String): Flow<Instant?> =
            store.flow.map { tasks -> tasks.filter { it.parentProfileId == profileId }.mapNotNull { it.lastModified }.maxOrNull() }

        override suspend fun updateStatus(taskId: String, status: TaskStatusEnum, lastModified: Instant?) {
            val existing = store.tasks[taskId] ?: return
            store.put(existing.copy(status = status, lastModified = lastModified ?: existing.lastModified))
        }

        override suspend fun updateLastModified(taskId: String, lastModified: Instant?) {
            val existing = store.tasks[taskId] ?: return
            store.put(existing.copy(lastModified = lastModified ?: existing.lastModified))
        }

        override suspend fun setDeviceRequestIsNewFalseByTaskId(taskId: String) {
            // no-op in fake DAO
        }

        override suspend fun updateDeviceRequestAuthoredOnByTaskId(taskId: String, time: Instant) {
            // no-op in fake DAO
        }

        override suspend fun updateDeviceRequestSentCommunicationOnByTaskId(taskId: String, time: Instant) {
            // no-op in fake DAO
        }

        override suspend fun updateDeviceRequestStatusByTaskId(taskId: String, status: String) {
            // no-op in fake DAO
        }

        override suspend fun updateDeviceRequestUserActionStateByTaskId(taskId: String, state: Int?) {
            // no-op in fake DAO
        }

        override suspend fun upsertPatients(items: List<ErpPatientEntity>) = items.forEach { store.patients[it.patientId] = it }
        override suspend fun upsertPractitioners(items: List<ErpPractitionerEntity>) = items.forEach { store.practitioners[it.practitionerId] = it }
        override suspend fun upsertMedications(items: List<ErpMedicationEntity>) {
            // no-op in fake DAO for now; medications not stored in this test
        }

        override suspend fun upsertMedicationRequest(items: List<ErpMedicationRequestEntity>) {
            // no-op in fake DAO for now; medication requests not stored in this test
        }

        override suspend fun upsertMultiplePrescriptions(items: List<ErpMultiplePrescriptionEntity>) {
            // no-op in fake DAO for now
        }

        override suspend fun markAsRedeemed(taskIds: List<String>, redeemedOn: Instant) = taskIds.forEach { id ->
            store.tasks[id]?.let {
                store.put(
                    it.copy(redeemedOn = redeemedOn)
                )
            }
        }

        override suspend fun updateRedeemedOn(taskId: String, redeemedOn: Instant?) {
            store.tasks[taskId]?.let { store.put(it.copy(redeemedOn = redeemedOn)) }
        }

        override suspend fun updateName(taskId: String, name: String) {
            store.tasks[taskId]?.let { store.put(it.copy(name = name)) }
        }

        override suspend fun getInsurantIdByTaskId(taskId: String): String? {
            return store.tasks[taskId]?.insuranceInformationId?.let { store.insurances[it]?.insuranceInformationId }
        }

        override suspend fun markAsIncomplete(taskId: String, error: String) {
            store.tasks[taskId]?.let {
                store.put(
                    it.copy(isIncomplete = true, failureToReport = error)
                )
            }
        }
    }

    private class FakeErpTaskWithRefsDao(private val store: TaskStore) : ErpTaskWithRefsDao {
        override fun observeWithRefs(taskId: String): Flow<ErpTaskWithRefs?> = store.flow.map { tasks ->
            tasks.find { it.taskId == taskId }?.let {
                store.buildWithRefs(
                    it
                )
            }
        }

        override fun observeAllWithRefsByProfile(profileId: String): Flow<List<ErpTaskWithRefs>> = store.flow.map { tasks ->
            tasks.filter { it.parentProfileId == profileId }.map {
                store.buildWithRefs(
                    it
                )
            }
        }

        override fun observeAllWithRefsByTaskIds(taskIds: List<String>): Flow<List<ErpTaskWithRefs>> = store.flow.map { tasks ->
            tasks.filter { it.taskId in taskIds }.map {
                store.buildWithRefs(
                    it
                )
            }
        }

        override fun observeScannedByProfile(profileId: String): Flow<List<ErpTaskWithRefs>> = store.flow.map { tasks ->
            tasks.filter { it.parentProfileId == profileId && it.taskType == TaskTypeValues.SCANNED }.map {
                store.buildWithRefs(
                    it
                )
            }
        }

        override fun observeScannedByTaskIds(taskIds: List<String>): Flow<List<ErpTaskWithRefs>> = store.flow.map { tasks ->
            tasks.filter { it.taskId in taskIds && it.taskType == TaskTypeValues.SCANNED }.map {
                store.buildWithRefs(
                    it
                )
            }
        }
    }

    private class FakeOrganizationDao(private val store: TaskStore) : OrganizationDao {
        override suspend fun upsertAll(items: List<ErpOrganizationEntity>) = items.forEach { store.organizations[it.organizationId] = it }
        override suspend fun getByTaskId(taskId: String) = store.organizations[taskId]
        override suspend fun clearAll() = store.organizations.clear()
    }

    private class FakeInsuranceInformationDao(private val store: TaskStore) : InsuranceInformationDao {
        override suspend fun upsertAll(items: List<ErpInsuranceInformationEntity>) = items.forEach { store.insurances[it.insuranceInformationId] = it }
        override suspend fun getById(id: String) = store.insurances[id]
        override suspend fun clearAll() = store.insurances.clear()
    }

    private class FakeCommunicationDao(private val store: TaskStore) : CommunicationDao {
        override suspend fun upsertAll(items: List<ErpCommunicationEntity>) = items.forEach { store.putCommunication(it) }
        override suspend fun getByTaskId(taskId: String) = store.communications.values.filter { it.taskId == taskId }
        override suspend fun getById(id: String) = store.communications[id]
        override suspend fun deleteByProfileId(profileId: String) {
            store.communications.values.removeAll { it.profileId == profileId }
            store.triggerCommunicationUpdate()
        }

        override fun observeByOrderAndProfile(orderId: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>> =
            store.communicationFlow.map { list -> list.filter { it.orderId == orderId && it.profile == profile } }

        override fun observeByTaskIdAndProfile(taskId: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>> =
            store.communicationFlow.map { list -> list.filter { it.taskId == taskId && it.profile == profile } }

        override fun observeByInsuranceAndProfileSorted(insuranceId: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>> =
            store.communicationFlow.map { list -> list.filter { it.insuranceId == insuranceId && it.profile == profile }.sortedByDescending { it.timeStamp } }

        override fun observeRepliesForTaskIds(taskIds: List<String>, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>> =
            store.communicationFlow.map { list -> list.filter { it.taskId in taskIds && it.profile == profile }.sortedByDescending { it.timeStamp } }

        override fun observeRepliesForTaskIdsFromSender(
            taskIds: List<String>,
            sender: String,
            profile: CommunicationProfileV1
        ): Flow<List<ErpCommunicationEntity>> =
            store.communicationFlow.map { list ->
                list.filter { it.taskId in taskIds && it.profile == profile && it.telematikId == sender }.sortedByDescending { it.timeStamp }
            }

        override fun observeUnreadCountByOrderAndTaskIds(taskIds: List<String>, orderId: String): Flow<Long> =
            store.communicationFlow.map { list -> list.count { it.taskId in taskIds && it.orderId == orderId && !it.consumed }.toLong() }

        override fun observeUnreadCountByInsurance(insuranceId: String): Flow<Long> =
            store.communicationFlow.map { list -> list.count { it.insuranceId == insuranceId && !it.consumed }.toLong() }

        override fun observeAll(): Flow<List<ErpCommunicationEntity>> = store.communicationFlow

        override fun observeUnread(): Flow<List<ErpCommunicationEntity>> =
            store.communicationFlow.map { list -> list.filter { !it.consumed } }

        override fun observeDistinctTaskIdsByOrder(orderId: String): Flow<List<String>> =
            store.communicationFlow.map { list -> list.filter { it.orderId == orderId }.map { it.taskId }.distinct() }

        override fun observeParentProfileIdByOrder(orderId: String): Flow<String?> =
            store.communicationFlow.map { list -> list.find { it.orderId == orderId }?.profileId }

        override fun observeParentProfileIdByTaskId(taskId: String): Flow<String?> =
            store.communicationFlow.map { list -> list.find { it.taskId == taskId }?.profileId }

        override suspend fun updateConsumedForGroup(
            orderId: String,
            taskId: String,
            sender: String,
            recipient: String,
            consumed: Boolean
        ): Int {
            val targets = store.communications.values.filter {
                it.orderId == orderId && it.taskId == taskId && it.telematikId == sender && it.recipient == recipient
            }
            targets.forEach {
                store.putCommunication(it.copy(consumed = consumed))
            }
            return targets.size
        }

        override suspend fun updateConsumedById(communicationId: String, consumed: Boolean): Int {
            val existing = store.communications[communicationId] ?: return 0
            store.putCommunication(existing.copy(consumed = consumed))
            return 1
        }

        override suspend fun updatePharmacyName(communicationId: String, pharmacyName: String): Int {
            val existing = store.communications[communicationId] ?: return 0
            store.putCommunication(existing.copy(pharmacyName = pharmacyName))
            return 1
        }

        override fun observeMaxTimestampByInsurance(insuranceId: String): Flow<Instant?> =
            store.communicationFlow.map { list -> list.filter { it.insuranceId == insuranceId }.map { it.timeStamp }.maxOrNull() }

        override suspend fun getInsuranceIdByTaskId(taskId: String): String? =
            store.communicationFlow.map { list -> list.find { it.taskId == taskId }?.insuranceId }.first()

        override suspend fun getTaskByTaskId(taskId: String): ErpTaskEntity? =
            store.tasks[taskId]

        override suspend fun getOrderIdByTaskIdAndTelematikId(taskId: String, telematikId: String): String? =
            store.communications.values.find { it.taskId == taskId && it.telematikId == telematikId && it.orderId.isNotEmpty() }?.orderId

        override suspend fun getOrderIdByTaskIdAndProfile(taskId: String, profile: CommunicationProfileV1): String? =
            store.communications.values.find { it.taskId == taskId && it.profile == profile && it.orderId.isNotEmpty() }?.orderId

        override fun observeRepliesForOrderIdFromSender(orderId: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>> =
            store.communicationFlow.map { list -> list.filter { it.orderId == orderId && it.profile == profile }.sortedByDescending { it.timeStamp } }

        override fun observeUnreadRepliesCount(taskIds: List<String>, profile: CommunicationProfileV1): Flow<Long> =
            store.communicationFlow.map { list -> list.count { it.taskId in taskIds && it.profile == profile && !it.consumed }.toLong() }

        override fun observeUnreadRepliesCountFromSender(taskIds: List<String>, sender: String, profile: CommunicationProfileV1): Flow<Long> =
            store.communicationFlow.map { list ->
                list.count { it.taskId in taskIds && it.profile == profile && it.telematikId == sender && !it.consumed }.toLong()
            }
    }

    private class FakeIngredientDao : IngredientDao {
        private val store = mutableMapOf<String, ErpIngredientEntity>()
        override suspend fun upsertAll(items: List<ErpIngredientEntity>) = items.forEach { store[it.ingredientId] = it }
        override suspend fun getByMedicationId(medicationId: String) = store.values.filter { it.medicationId == medicationId }
        override suspend fun deleteByMedicationId(medicationId: String) {
            store.values.removeAll { it.medicationId == medicationId }
        }

        override suspend fun clearAll() = store.clear()
    }

    private class FakeMedicationDispenseDao(private val store: TaskStore) : MedicationDispenseDao {
        override suspend fun upsertAll(items: List<ErpMedicationDispenseEntity>) = items.forEach { store.medicationDispenses[it.dispenseId] = it }
        override suspend fun getByTaskId(taskId: String) = store.medicationDispenses.values.toList()
        override suspend fun deleteByProfileId(profileId: String) {
            store.medicationDispenses.values.removeAll { dispense ->
                store.tasks[dispense.taskId]?.parentProfileId == profileId
            }
        }

        override suspend fun clearAll() = store.medicationDispenses.clear()
    }

    private class FakeInvoiceDao(private val store: TaskStore) : InvoiceDao {
        override suspend fun upsertAll(items: List<InvoiceRoomEntity>) = items.forEach { store.invoices[it.taskId] = it }
        override suspend fun upsert(item: InvoiceRoomEntity) {
            store.invoices[item.taskId] = item
        }

        override suspend fun getByProfile(profileId: String) = store.invoices.values.filter { it.profileId == profileId }
        override fun observeByProfile(profileId: String) = kotlinx.coroutines.flow.flowOf(store.invoices.values.filter { it.profileId == profileId })
        override suspend fun getByTaskId(taskId: String) = store.invoices[taskId]
        override fun observeByTaskId(taskId: String) = kotlinx.coroutines.flow.flowOf(store.invoices[taskId])
        override fun latestInvoiceModifiedTimestamp(profileId: String) = kotlinx.coroutines.flow.flowOf<Long?>(null)
        override fun observeUnread() = kotlinx.coroutines.flow.flowOf(emptyList<InvoiceRoomEntity>())
        override suspend fun updateConsumedStatus(taskId: String, consumed: Boolean) {
            store.invoices[taskId]?.let { store.invoices[taskId] = it.copy(consumed = consumed) }
        }

        override fun countUnread(taskIds: List<String>) = kotlinx.coroutines.flow.flowOf(0)
        override suspend fun deleteByTaskId(taskId: String) {
            store.invoices.remove(taskId)
        }

        override suspend fun deleteByProfileId(profileId: String) {
            store.invoices.values.removeAll { it.profileId == profileId }
        }

        override suspend fun clearAll() = store.invoices.clear()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildSut(): Pair<TaskLocalDataSource, TaskStore> {
        val store = TaskStore()
        val sut = TaskLocalDataSourceV2(
            taskDao = FakeErpTaskDao(store),
            taskWithRefsDao = FakeErpTaskWithRefsDao(store),
            organizationDao = FakeOrganizationDao(store),
            insuranceInformationDao = FakeInsuranceInformationDao(store),
            ingredientDao = FakeIngredientDao(),
            communicationDao = FakeCommunicationDao(store),
            medicationDispenseDao = FakeMedicationDispenseDao(store),
            invoiceDao = FakeInvoiceDao(store)
        )
        return sut to store
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fixtures
    // ─────────────────────────────────────────────────────────────────────────

    private fun metaModel(
        taskId: String = "task-1",
        accessCode: String = "code-1",
        status: FhirTaskStatusErpModel = FhirTaskStatusErpModel.Ready,
        lastModified: Instant = NOW,
        authoredOn: Instant = AUTHORED_ON
    ) = FhirTaskMetaDataErpModel(
        taskId = taskId,
        accessCode = accessCode,
        lastModified = FhirTemporal.Instant(lastModified),
        authoredOn = FhirTemporal.Instant(authoredOn),
        status = status
    )

    private fun dataModel(
        pvsId: String? = "pvs-1",
        patient: FhirTaskKbvPatientErpModel? = null,
        practitioner: FhirTaskKbvPractitionerErpModel? = null,
        organization: FhirTaskOrganizationErpModel? = null,
        coverage: FhirCoverageErpModel? = null
    ) = FhirTaskDataErpModel(
        pvsId = pvsId,
        medicationRequest = null,
        medication = null,
        patient = patient,
        practitioner = practitioner,
        organization = organization,
        coverage = coverage,
        deviceRequest = null
    )

    private fun scannedModel(
        taskId: String = "scanned-1",
        profileId: String = "profile-1",
        accessCode: String = "scan-code",
        name: String? = "Ibuprofen",
        scannedOn: Instant = SCANNED_ON
    ) = TaskErpModel.Scanned(
        profileId = profileId,
        taskId = taskId,
        accessCode = accessCode,
        name = name,
        redeemedOn = null,
        isEuRedeemable = false,
        scannedOn = scannedOn,
        index = 0
    )

    /** Pre-seeded scanned entity with all required mapper fields. */
    private fun scannedEntity(
        taskId: String,
        profileId: String = "profile-1",
        name: String? = null,
        redeemedOn: Instant? = null
    ) = ErpTaskEntity(
        taskId = taskId,
        taskType = TaskTypeValues.SCANNED,
        parentProfileId = profileId,
        accessCode = "code-$taskId",
        name = name,
        redeemedOn = redeemedOn,
        scannedOn = SCANNED_ON,
        index = 0
    )

    /** Pre-seeded prescription entity with all required mapper fields. */
    private fun prescriptionEntity(
        taskId: String,
        profileId: String = "profile-1",
        status: TaskStatusEnum = TaskStatusEnum.Ready,
        lastModified: Instant = NOW,
        isIncomplete: Boolean = false
    ) = ErpTaskEntity(
        taskId = taskId,
        taskType = TaskTypeValues.PRESCRIPTION,
        parentProfileId = profileId,
        accessCode = "code-$taskId",
        name = null,
        redeemedOn = null,
        status = status,
        lastModified = lastModified,
        authoredOn = AUTHORED_ON,
        isIncomplete = isIncomplete
    )

    // ─────────────────────────────────────────────────────────────────────────
    // saveScannedPrescriptions
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun saveScannedPrescriptions_insertsNewTasksWithCorrectProfileId() = runTest {
        val (sut, store) = buildSut()
        sut.saveScannedTaskList("profile-A", listOf(scannedModel(taskId = "t1", profileId = "profile-B")), "Medikament")

        val saved = store.tasks["t1"]
        assertNotNull(saved)
        // Must use the profileId param, not the model's profileId.
        assertEquals("profile-A", saved.parentProfileId)
        assertEquals(TaskTypeValues.SCANNED, saved.taskType)
        assertEquals("scan-code", saved.accessCode)
        assertEquals(SCANNED_ON, saved.scannedOn)
    }

    @Test
    fun saveScannedPrescriptions_skipsExistingTasks() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("t1"))

        sut.saveScannedTaskList("profile-1", listOf(scannedModel(taskId = "t1"), scannedModel(taskId = "t2")), "Med")

        assertEquals(2, store.tasks.size)
        // t1 was already there, only t2 is new — t1 should not be overwritten
        assertEquals(SCANNED_ON, store.tasks["t1"]?.scannedOn) // original value preserved
    }

    @Test
    fun saveScannedPrescriptions_autoNamesEmptyNamedTasksWithMedicationString() = runTest {
        val (sut, store) = buildSut()
        // Seed one existing unnamed scanned task for the same medication string
        store.put(scannedEntity("existing-1", name = "Medikament 1"))

        sut.saveScannedTaskList(
            "profile-1",
            listOf(
                scannedModel(taskId = "t1", name = ""), // empty → should get auto-name
                scannedModel(taskId = "t2", name = null) // null → stays null
            ),
            "Medikament"
        )

        assertEquals("Medikament 2", store.tasks["t1"]?.name) // 1 existing → start at 2
        assertNull(store.tasks["t2"]?.name)
    }

    @Test
    fun saveScannedPrescriptions_noopForEmptyNewTaskList() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("t1"))

        sut.saveScannedTaskList("profile-1", listOf(scannedModel(taskId = "t1")), "Med")

        assertEquals(1, store.tasks.size) // nothing added
    }

    // ─────────────────────────────────────────────────────────────────────────
    // saveSyncedPrescriptionMetaData
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun saveSyncedPrescriptionMetaData_createsNewTaskWhenNotExisting() = runTest {
        val (sut, store) = buildSut()

        val result = sut.saveSyncedTaskMetaData("profile-1", metaModel("new-task"))

        assertTrue(result.isSuccess)
        val saved = store.tasks["new-task"]
        assertNotNull(saved)
        assertEquals("profile-1", saved.parentProfileId)
        assertEquals(TaskTypeValues.PRESCRIPTION, saved.taskType)
        assertEquals("code-1", saved.accessCode)
        assertEquals(TaskStatusEnum.Ready, saved.status)
        assertEquals(NOW, saved.lastModified)
    }

    @Test
    fun saveSyncedPrescriptionMetaData_preservesDisplayFieldsAndFkRefsOnUpdate() = runTest {
        val (sut, store) = buildSut()
        store.put(
            prescriptionEntity("t1").copy(
                name = "My Prescription",
                redeemedOn = Instant.parse("2024-01-01T00:00:00Z"),
                patientId = "patient_t1",
                organizationId = "org_t1"
            )
        )

        sut.saveSyncedTaskMetaData("profile-1", metaModel("t1"))

        val updated = store.tasks["t1"]!!
        // Display / user-set fields preserved
        assertEquals("My Prescription", updated.name)
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), updated.redeemedOn)
        // FK links preserved
        assertEquals("patient_t1", updated.patientId)
        assertEquals("org_t1", updated.organizationId)
        // Metadata refreshed
        assertEquals(NOW, updated.lastModified)
        assertEquals(TaskStatusEnum.Ready, updated.status)
    }

    @Test
    fun saveSyncedPrescriptionMetaData_upgradesScannedToPrescription() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("t1"))

        sut.saveSyncedTaskMetaData("profile-1", metaModel("t1"))

        assertEquals(TaskTypeValues.PRESCRIPTION, store.tasks["t1"]?.taskType)
    }

    @Test
    fun saveSyncedPrescriptionMetaData_preservesExistingPrescriptionTaskType() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("t1").copy(taskType = TaskTypeValues.DIGA))

        sut.saveSyncedTaskMetaData("profile-1", metaModel("t1"))

        assertEquals(TaskTypeValues.DIGA, store.tasks["t1"]?.taskType)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // saveSyncedPrescriptionMedicalData
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun saveSyncedPrescriptionMedicalData_returnsFailureWhenTaskNotFound() = runTest {
        val (sut, _) = buildSut()

        val result = sut.saveSyncedTaskKBVData("missing-task", dataModel())

        assertTrue(result.isFailure)
    }

    @Test
    fun saveSyncedPrescriptionMedicalData_persistsChildEntitiesAndUpdatesFkColumns() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("t1"))

        val fhirPatient = FhirTaskKbvPatientErpModel("Max Muster", null, null, "X123")
        val fhirOrg = FhirTaskOrganizationErpModel("Praxis", null, "BSNR-1", null, null, null, null)
        val fhirCoverage = FhirCoverageErpModel("AOK", "1", "IK-123", "GKV")

        val result = sut.saveSyncedTaskKBVData(
            "t1",
            dataModel(patient = fhirPatient, organization = fhirOrg, coverage = fhirCoverage)
        )

        assertTrue(result.isSuccess)
        // Child entities persisted
        assertNotNull(store.patients["patient_t1"])
        assertNotNull(store.organizations["organization_t1"])
        assertNotNull(store.insurances["insurance_t1"])
        // FK columns on task updated
        val updated = store.tasks["t1"]!!
        assertEquals("patient_t1", updated.patientId)
        assertEquals("organization_t1", updated.organizationId)
        assertEquals("insurance_t1", updated.insuranceInformationId)
        assertNull(updated.practitionerId) // no practitioner in model
    }

    @Test
    fun saveSyncedPrescriptionMedicalData_setsPvsIdentifier() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("t1"))

        sut.saveSyncedTaskKBVData("t1", dataModel(pvsId = "my-pvs"))

        assertEquals("my-pvs", store.tasks["t1"]?.pvsIdentifier)
    }

    @Test
    fun saveSyncedPrescriptionMedicalData_returnsSaveTaskResultWithLastModified() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("t1", status = TaskStatusEnum.Completed))

        val result = sut.saveSyncedTaskKBVData("t1", dataModel())

        assertTrue(result.isSuccess)
        val saveResult = result.getOrThrow()
        assertTrue(saveResult.isCompleted)
        assertEquals(NOW, saveResult.lastModified)
    }

    @Test
    fun saveSyncedPrescriptionMedicalData_upgradesScannedToPresciption() = runTest {
        val (sut, store) = buildSut()
        // Task was previously scanned but metadata has been saved (upgrading to PRESCRIPTION)
        store.put(scannedEntity("t1").copy(taskType = TaskTypeValues.PRESCRIPTION, lastModified = NOW, authoredOn = AUTHORED_ON))

        sut.saveSyncedTaskKBVData("t1", dataModel())

        assertEquals(TaskTypeValues.PRESCRIPTION, store.tasks["t1"]?.taskType)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateSyncedPrescriptionStatus
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun updateSyncedPrescriptionStatus_skipsUpdateWhenLastModifiedIsNull() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("t1", status = TaskStatusEnum.Ready))

        sut.updateSyncedTaskStatus("t1", FhirTaskStatusErpModel.Completed, lastModified = null)

        // Status must not change when no lastModified is provided (V1 parity).
        assertEquals(TaskStatusEnum.Ready, store.tasks["t1"]?.status)
    }

    @Test
    fun updateSyncedPrescriptionStatus_updatesStatusAndTimestampWhenProvided() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("t1", status = TaskStatusEnum.Ready, lastModified = NOW))

        val newTimestamp = Instant.parse("2024-07-01T00:00:00Z")
        sut.updateSyncedTaskStatus("t1", FhirTaskStatusErpModel.Completed, FhirTemporal.Instant(newTimestamp))

        assertEquals(TaskStatusEnum.Completed, store.tasks["t1"]?.status)
        assertEquals(newTimestamp, store.tasks["t1"]?.lastModified)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // markSyncedPrescriptionAsIncomplete
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun markSyncedPrescriptionAsIncomplete_returnsFailureWhenTaskNotFound() = runTest {
        val (sut, _) = buildSut()

        val result = sut.markSyncedTaskAsIncomplete("no-such-task", RuntimeException("oops"), JsonPrimitive(""))

        assertTrue(result.isFailure)
    }

    @Test
    fun markSyncedPrescriptionAsIncomplete_marksTaskWithError() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("t1"))

        val result = sut.markSyncedTaskAsIncomplete("t1", RuntimeException("parse error"), JsonPrimitive(""))

        assertTrue(result.isSuccess)
        val updated = store.tasks["t1"]!!
        assertTrue(updated.isIncomplete)
        assertEquals("parse error", updated.failureToReport)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // redeemScannedPrescriptions
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun redeemScannedPrescriptions_setsRedeemedOnForEachId() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("t1"))
        store.put(scannedEntity("t2"))

        sut.redeemScannedTaskListByTaskIdList(listOf("t1", "t2"))

        assertNotNull(store.tasks["t1"]?.redeemedOn)
        assertNotNull(store.tasks["t2"]?.redeemedOn)
    }

    @Test
    fun redeemScannedPrescriptions_noopForEmptyList() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("t1"))

        sut.redeemScannedTaskListByTaskIdList(emptyList())

        assertNull(store.tasks["t1"]?.redeemedOn)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deletePrescriptionByTaskId
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun deletePrescriptionByTaskId_removesOnlyTarget() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("t1"))
        store.put(prescriptionEntity("t2"))

        sut.deleteTaskByTaskId("t1")

        assertNull(store.tasks["t1"])
        assertNotNull(store.tasks["t2"])
    }

    // ─────────────────────────────────────────────────────────────────────────
    // loadAllTasksByProfileId
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun loadAllTasksByProfileId_returnsOnlyTasksForThatProfile() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("t1", profileId = "profile-A"))
        store.put(prescriptionEntity("t2", profileId = "profile-B"))
        store.put(scannedEntity("t3", profileId = "profile-A"))

        val tasks = sut.loadTaskListByProfileId("profile-A").first()

        assertEquals(2, tasks.size)
        assertTrue(tasks.any { it.taskId == "t1" })
        assertTrue(tasks.any { it.taskId == "t3" })
    }

    @Test
    fun loadAllTasksByProfileId_reactsToNewTaskBeingAdded() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("t1"))

        val flow = sut.loadTaskListByProfileId("profile-1")

        assertEquals(1, flow.first().size)

        store.put(prescriptionEntity("t2"))
        assertEquals(2, flow.first().size)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // loadTaskByTaskId
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun loadTaskByTaskId_returnsNullWhenNotFound() = runTest {
        val (sut, _) = buildSut()

        val result = sut.loadTaskByTaskId("missing").first()

        assertNull(result)
    }

    @Test
    fun loadTaskByTaskId_returnsTaskWhenFound() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("t1"))

        val result = sut.loadTaskByTaskId("t1").first()

        assertNotNull(result)
        assertIs<TaskErpModel.Scanned>(result)
        assertEquals("t1", result.taskId)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getLatestTaskModifiedTimestamp
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun getLatestTaskModifiedTimestamp_returnsMaxAcrossProfile() = runTest {
        val (sut, store) = buildSut()
        val t1 = Instant.parse("2024-05-01T00:00:00Z")
        val t2 = Instant.parse("2024-06-15T00:00:00Z")
        store.put(prescriptionEntity("t1", lastModified = t1))
        store.put(prescriptionEntity("t2", lastModified = t2))
        store.put(prescriptionEntity("t3", profileId = "other", lastModified = Instant.parse("2025-01-01T00:00:00Z")))

        val latest = sut.getLatestTaskModifiedTimestamp("profile-1").first()

        assertEquals(t2, latest)
    }

    @Test
    fun getLatestTaskModifiedTimestamp_returnsNullWhenNoTasksForProfile() = runTest {
        val (sut, _) = buildSut()

        val latest = sut.getLatestTaskModifiedTimestamp("profile-1").first()

        assertNull(latest)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateScannedPrescriptionName
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun updateScannedPrescriptionName_updatesName() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("t1", name = "Old Name"))

        sut.updateScannedTaskName("t1", "New Name")

        assertEquals("New Name", store.tasks["t1"]?.name)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // loadTaskIdsByProfileId
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun loadTaskIdsByProfileId_returnsOnlyIdsForThatProfile() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("t1", profileId = "profile-1"))
        store.put(prescriptionEntity("t2", profileId = "profile-2"))
        store.put(scannedEntity("t3", profileId = "profile-1"))

        val ids = sut.loadTaskIdStringListByProfileId("profile-1").first()

        assertEquals(setOf("t1", "t3"), ids.toSet())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // loadScannedPrescriptionListByProfileId
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun loadScannedPrescriptionListByProfileId_returnsOnlyScannedTasks() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("s1"))
        store.put(prescriptionEntity("p1"))

        val list = sut.loadScannedPrescriptionListByProfileId("profile-1").first()

        assertEquals(1, list.size)
        assertEquals("s1", list.first().taskId)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // End-to-end: meta → medical → observe
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun endToEnd_metaThenMedicalData_taskIsObservableWithChildRefs() = runTest {
        val (sut, store) = buildSut()

        // Step 1: save metadata (creates the task row)
        sut.saveSyncedTaskMetaData("profile-1", metaModel("t1")).getOrThrow()

        // Step 2: save medical data (child entities + FK updates)
        val fhirPatient = FhirTaskKbvPatientErpModel("Erika Muster", null, null, "X123")
        val fhirOrg = FhirTaskOrganizationErpModel("Praxis", null, "BSNR", null, null, null, null)
        sut.saveSyncedTaskKBVData("t1", dataModel(patient = fhirPatient, organization = fhirOrg)).getOrThrow()

        // Step 3: observable flow should reflect the full task
        val task = sut.loadTaskByTaskId("t1").first()
        assertNotNull(task)
        assertIs<TaskErpModel.Synced.Prescription>(task)
        assertNotNull(task.patient)
        assertEquals("Erika Muster", task.patient!!.name)
        assertNotNull(task.organization)

        // Child entities are accessible directly from the store too
        assertNotNull(store.patients["patient_t1"])
        assertNotNull(store.organizations["organization_t1"])
    }

    // ─────────────────────────────────────────────────────────────────────────
    // saveCommunications
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun saveCommunications_savesReplyForKnownTask() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("task-1"))

        val bundle = FhirCommunicationBundleErpModel(
            total = 1,
            messages = listOf(replyMessage("comm-1", taskId = "task-1"))
        )
        val count = sut.saveCommunications(bundle)

        assertEquals(1, count)
        assertNotNull(store.communications["comm-1"])
        assertEquals("task-1", store.communications["comm-1"]?.taskId)
    }

    @Test
    fun saveCommunications_savesDispenseForKnownTask() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("task-2"))

        val bundle = FhirCommunicationBundleErpModel(
            total = 1,
            messages = listOf(dispenseMessage("comm-2", taskId = "task-2"))
        )
        val count = sut.saveCommunications(bundle)

        assertEquals(1, count)
        assertNotNull(store.communications["comm-2"])
        assertEquals("task-2", store.communications["comm-2"]?.taskId)
    }

    @Test
    fun saveCommunications_skipsMessageForUnknownTask() = runTest {
        val (sut, store) = buildSut()

        val bundle = FhirCommunicationBundleErpModel(
            total = 1,
            messages = listOf(replyMessage("comm-3", taskId = "unknown-task"))
        )
        val count = sut.saveCommunications(bundle)

        assertEquals(0, count)
        assertTrue(store.communications.isEmpty())
    }

    @Test
    fun saveCommunications_skipsMessageWithNullTaskId() = runTest {
        val (sut, store) = buildSut()

        val bundle = FhirCommunicationBundleErpModel(
            total = 1,
            messages = listOf(replyMessage("comm-4", taskId = null))
        )
        val count = sut.saveCommunications(bundle)

        assertEquals(0, count)
        assertTrue(store.communications.isEmpty())
    }

    @Test
    fun saveCommunications_savesOnlyKnownTasks_fromMixedBundle() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("task-a"))

        val bundle = FhirCommunicationBundleErpModel(
            total = 2,
            messages = listOf(
                replyMessage("comm-a", taskId = "task-a"),
                replyMessage("comm-b", taskId = "task-unknown")
            )
        )
        val count = sut.saveCommunications(bundle)

        assertEquals(1, count)
        assertNotNull(store.communications["comm-a"])
        assertNull(store.communications["comm-b"])
    }

    @Test
    fun saveCommunications_upserts_existingCommunication() = runTest {
        val (sut, store) = buildSut()
        store.put(prescriptionEntity("task-1"))

        val bundle1 = FhirCommunicationBundleErpModel(
            total = 1,
            messages = listOf(
                replyMessage("comm-1", taskId = "task-1", payloadText = """{"version":1,"communicationType":"text","transactionID":"t-0","text":"first"}""")
            )
        )
        sut.saveCommunications(bundle1)

        val bundle2 = FhirCommunicationBundleErpModel(
            total = 1,
            messages = listOf(
                replyMessage("comm-1", taskId = "task-1", payloadText = """{"version":3,"communicationType":"text","transactionID":"t-1","text":"updated"}""")
            )
        )
        sut.saveCommunications(bundle2)

        assertEquals(
            CommunicationReplyTextPayloadErpModel(version = 3, communicationType = CommunicationTypeErpModel.Text, transactionID = "t-1", text = "updated"),
            store.communications["comm-1"]?.payload
        )
    }

    @Test
    fun saveCommunications_returnsZero_forEmptyBundle() = runTest {
        val (sut, store) = buildSut()

        val count = sut.saveCommunications(FhirCommunicationBundleErpModel(total = 0, messages = emptyList()))

        assertEquals(0, count)
        assertTrue(store.communications.isEmpty())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Communication fixtures
    // ─────────────────────────────────────────────────────────────────────────

    private fun replyMessage(
        id: String,
        taskId: String?,
        payloadText: String = """{"version":1,"communicationType":"text","transactionID":"t-0","text":"reply-payload"}"""
    ) = FhirReplyCommunicationEntryErpModel(
        id = id,
        profile = "ErxCommunicationReply",
        taskId = taskId,
        sender = CommunicationParticipantErpModel(identifier = "telematik-id"),
        recipient = CommunicationParticipantErpModel(identifier = "recipient-id"),
        orderId = "order-$id",
        sent = FhirTemporal.Instant(NOW),
        received = null,
        payload = payloadText
    )

    private fun dispenseMessage(
        id: String,
        taskId: String?,
        contentString: String = """{"version":1,"communicationType":"dispense","transactionID":"t-0","content":"dispense-payload"}"""
    ) = FhirDispenseCommunicationEntryErpModel(
        id = id,
        profile = "ErxCommunicationDispReq",
        taskId = taskId,
        sender = CommunicationParticipantErpModel(identifier = "telematik-id"),
        recipient = CommunicationParticipantErpModel(identifier = "recipient-id"),
        orderId = "order-$id",
        sent = FhirTemporal.Instant(NOW),
        payload = contentString
    )

    // ─────────────────────────────────────────────────────────────────────────
    // saveCommunicationForScannedPrescription
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun saveCommunicationForScannedPrescription_savesForKnownScannedTask() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("scan-1"))

        sut.saveCommunicationForScannedTask("scan-1", "pharmacy-id", "transaction-1")

        val saved = store.communications["transaction-1"]
        assertNotNull(saved)
        assertEquals("scan-1", saved.taskId)
        assertEquals("pharmacy-id", saved.telematikId)
        assertEquals("transaction-1", saved.communicationId)
        assertEquals(CommunicationProfileV1.ErxCommunicationDispReq, saved.profile)
        assertEquals("", saved.orderId)
        assertEquals("profile-1", saved.profileId)
    }

    @Test
    fun saveCommunicationForScannedPrescription_noopForUnknownTask() = runTest {
        val (sut, store) = buildSut()

        sut.saveCommunicationForScannedTask("unknown-task", "pharmacy-id", "transaction-2")

        assertTrue(store.communications.isEmpty())
    }

    @Test
    fun saveCommunicationForScannedPrescription_upserts_existingTransaction() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("scan-1"))

        sut.saveCommunicationForScannedTask("scan-1", "pharmacy-a", "txn-1")
        sut.saveCommunicationForScannedTask("scan-1", "pharmacy-b", "txn-1")

        assertEquals(1, store.communications.size)
        assertEquals("pharmacy-b", store.communications["txn-1"]?.telematikId)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateScannedTaskRedeemedOn
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun updateScannedTaskRedeemedOn_setsTimestamp_forKnownTask() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("scan-1"))

        sut.updateScannedTaskRedeemedOn("scan-1", NOW)

        assertEquals(NOW, store.tasks["scan-1"]?.redeemedOn)
    }

    @Test
    fun updateScannedTaskRedeemedOn_clearsTimestamp_whenNullPassed() = runTest {
        val (sut, store) = buildSut()
        store.put(scannedEntity("scan-1", redeemedOn = NOW))

        sut.updateScannedTaskRedeemedOn("scan-1", null)

        assertNull(store.tasks["scan-1"]?.redeemedOn)
    }

    @Test
    fun updateScannedTaskRedeemedOn_noopForUnknownTask() = runTest {
        val (sut, store) = buildSut()

        sut.updateScannedTaskRedeemedOn("unknown-task", NOW)

        assertTrue(store.tasks.isEmpty())
    }

    @Test
    fun saveSyncedTaskMedicationDispense_savesEuCountryCode() = runTest {
        val (sut, store) = buildSut()

        val dispenseModel = de.gematik.ti.erp.app.fhir.dispense.model.FhirMedicationDispenseErpModel(
            dispenseId = "dispense-1",
            patientId = "patient-1",
            substitutionAllowed = false,
            dosageInstruction = "dosage-1",
            performer = "performer-1",
            handedOver = de.gematik.ti.erp.app.fhir.temporal.FhirTemporal.Instant(NOW),
            dispensedMedication = emptyList(),
            dispensedDeviceRequest = null,
            euCountryCode = "IT"
        )
        val dispenseCollection = de.gematik.ti.erp.app.fhir.FhirMedicationDispenseErpModelCollection(
            dispensedMedications = listOf(dispenseModel)
        )

        sut.saveSyncedTaskMedicationDispense("taskId-1", dispenseCollection)

        val savedDispense = store.medicationDispenses["dispense-1"]
        assertNotNull(savedDispense)
        assertEquals("IT", savedDispense.euCountryCode)
    }
}
