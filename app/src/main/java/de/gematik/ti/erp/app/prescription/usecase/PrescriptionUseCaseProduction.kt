/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetail
import de.gematik.ti.erp.app.prescription.detail.ui.model.mapToUIPrescriptionDetailScanned
import de.gematik.ti.erp.app.prescription.detail.ui.model.mapToUIPrescriptionDetailSynced
import de.gematik.ti.erp.app.prescription.repository.Mapper
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.extractInsurance
import de.gematik.ti.erp.app.prescription.repository.extractMedication
import de.gematik.ti.erp.app.prescription.repository.extractMedicationRequest
import de.gematik.ti.erp.app.prescription.repository.extractOrganization
import de.gematik.ti.erp.app.prescription.repository.extractPatient
import de.gematik.ti.erp.app.prescription.repository.extractPractitioner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import java.time.OffsetDateTime
import javax.inject.Inject

class PrescriptionUseCaseProduction @Inject constructor(
    private val repository: PrescriptionRepository,
    private val mapper: Mapper
) : PrescriptionUseCase {

    override suspend fun saveScannedTasks(tasks: List<Task>) =
        repository.saveScannedTasks(tasks)

    override suspend fun capabilityStatement() = repository.loadCapabilityStatement()

    override fun tasks() = repository.tasks()

    override fun scannedTasks(): Flow<List<Task>> = repository.scannedTasksWithoutBundle()

    override fun syncedTasks(): Flow<List<Task>> = repository.syncedTasksWithoutBundle()

    override suspend fun downloadCommunications(): Result<Unit> =
        when (val r = repository.downloadCommunications()) {
            is Result.Error -> r
            is Result.Success -> r
        }

    override suspend fun downloadTasks(): Result<Unit> =
        when (val r = repository.downloadTasks()) {
            is Result.Error -> r
            is Result.Success -> r
        }

    override suspend fun loadLowDetailEvents(taskId: String): Flow<List<LowDetailEventSimple>> =
        repository.loadLowDetailEvents(taskId)

    override suspend fun deleteLowDetailEvents(taskId: String) {
        repository.deleteLowDetailEvents(taskId)
    }

    override suspend fun generatePrescriptionDetails(
        taskId: String,
    ): UIPrescriptionDetail {

        val (task, medicationDispense) = repository.loadTaskWithMedicationDispenseForTaskId(taskId).first()
        val payload = createDataMatrixPayload(task.taskId, task.accessCode)
        val matrix = createMatrixCode(payload)
        val unRedeemMorePossible = unRedeemMorePossible(task.taskId)

        return if (task.rawKBVBundle == null) {
            mapToUIPrescriptionDetailScanned(task, matrix, unRedeemMorePossible)
        } else {
            val bundle = mapper.parseKBVBundle(requireNotNull(task.rawKBVBundle))
            mapToUIPrescriptionDetailSynced(
                task,
                requireNotNull(bundle.extractMedication()),
                requireNotNull(bundle.extractMedicationRequest()),
                medicationDispense,
                requireNotNull(bundle.extractInsurance()),
                requireNotNull(bundle.extractOrganization()),
                requireNotNull(bundle.extractPatient()),
                requireNotNull(bundle.extractPractitioner()),
                matrix,
                repository.hasAuditEventsSyncError(),
                repository.lastSuccessfulAuditEventSyncDate()
            )
        }
    }

    override suspend fun deletePrescription(taskId: String, isRemoteTask: Boolean) =
        repository.deleteTaskByTaskId(taskId, isRemoteTask)

    override suspend fun redeem(taskIds: List<String>, redeem: Boolean, all: Boolean) {
        if (all) {
            redeemAll(taskIds, redeem)
        } else {
            redeemSingle(taskIds.first(), redeem, OffsetDateTime.now())
        }
    }

    private suspend fun redeemSingle(taskId: String, redeem: Boolean, tm: OffsetDateTime) {
        if (redeem) {
            repository.updateRedeemedOnForSingleTask(taskId, tm)
        } else {
            repository.updateRedeemedOnForSingleTask(taskId, null)
        }
    }

    private suspend fun redeemAll(taskIds: List<String>, redeem: Boolean) {
        val now = OffsetDateTime.now()
        if (redeem) {
            repository.updateRedeemedOnForAllTasks(taskIds, now)
        } else {
            repository.updateRedeemedOnForAllTasks(taskIds, null)
        }
    }

    override suspend fun unRedeemMorePossible(taskId: String): Boolean {
        var unRedeemMorePossible = false
        scannedTasks().take(1).collect { scannedTasks ->
            scannedTasks.forEach {
                if (it.taskId == taskId) {
                    val tasksForRedeemedOn = it.redeemedOn?.let { actualTask ->
                        loadTasksForRedeemedOn(actualTask)
                    }
                    tasksForRedeemedOn?.take(1)?.collect { tasksWithActualRedeemedOn ->
                        if (tasksWithActualRedeemedOn.size > 1) {
                            unRedeemMorePossible = true
                        }
                    }
                }
            }
        }
        return unRedeemMorePossible
    }

    override suspend fun editScannedPrescriptionsName(
        name: String,
        scanSessionEnd: OffsetDateTime
    ) {
        if (name.isBlank()) {
            repository.updateScanSessionName(null, scanSessionEnd)
        } else {
            repository.updateScanSessionName(name.trim(), scanSessionEnd)
        }
    }

    override fun loadTasksForRedeemedOn(redeemedOn: OffsetDateTime): Flow<List<Task>> {
        return repository.loadTasksForRedeemedOn(redeemedOn)
    }

    override fun loadAuditEvents(taskId: String): Flow<List<AuditEventSimple>> {
        // TODO: add language support
        return repository.loadAuditEvents(taskId, "de")
    }

    override suspend fun saveLowDetailEvent(lowDetailEvent: LowDetailEventSimple) {
        repository.saveLowDetailEvent(lowDetailEvent)
    }

    override suspend fun getAllTasksWithTaskIdOnly(): List<String> {
        return repository.getAllTasksWithTaskIdOnly()
    }
}
