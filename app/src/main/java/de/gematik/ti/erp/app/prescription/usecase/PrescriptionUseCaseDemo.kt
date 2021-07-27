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
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.idp.usecase.RefreshFlowException
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetail
import de.gematik.ti.erp.app.prescription.detail.ui.model.mapToUIPrescriptionDetailScanned
import de.gematik.ti.erp.app.prescription.detail.ui.model.mapToUIPrescriptionDetailSynced
import de.gematik.ti.erp.app.prescription.repository.InsuranceCompanyDetail
import de.gematik.ti.erp.app.prescription.repository.MedicationDetail
import de.gematik.ti.erp.app.prescription.repository.MedicationRequestDetail
import de.gematik.ti.erp.app.prescription.repository.OrganizationDetail
import de.gematik.ti.erp.app.prescription.repository.PatientDetail
import de.gematik.ti.erp.app.prescription.repository.PractitionerDetail
import de.gematik.ti.erp.app.prescription.repository.PrescriptionDemoDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import org.hl7.fhir.r4.model.CapabilityStatement
import java.io.IOException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import javax.inject.Inject

private const val DEMO_DELAY = 500L

class PrescriptionUseCaseDemo @Inject constructor(
    private val prescriptionDemoDataSource: PrescriptionDemoDataSource,
    private val demoUseCase: DemoUseCase
) : PrescriptionUseCase {

    override fun tasks(): Flow<List<Task>> = prescriptionDemoDataSource.tasks

    override fun scannedTasks(): Flow<List<Task>> = tasks().map { tasks ->
        tasks.filter {
            it.scannedOn != null
        }
    }

    override fun syncedTasks(): Flow<List<Task>> = tasks().map { tasks ->
        tasks.filter {
            it.scannedOn == null
        }
    }

    override suspend fun saveScannedTasks(tasks: List<Task>) {
        prescriptionDemoDataSource.saveTasks(tasks)
    }

    override suspend fun capabilityStatement(): Result<CapabilityStatement> =
        throw NotImplementedError()

    override suspend fun downloadCommunications(): Result<Unit> {
        throw NotImplementedError()
    }

    override suspend fun downloadTasks(): Result<Unit> {
        delay(DEMO_DELAY)

        return if (demoUseCase.authTokenReceived.value) {
            prescriptionDemoDataSource.incrementRefresh()
            Result.Success(Unit)
        } else {
            Result.Error(IOException(RefreshFlowException(true, null, "demo mode")))
        }
    }

    private fun loadTaskByTaskId(taskId: String) =
        prescriptionDemoDataSource.tasks.value
            .find { it.taskId == taskId }

    override suspend fun generatePrescriptionDetails(
        taskId: String,
    ): UIPrescriptionDetail {
        return loadTaskByTaskId(taskId)
            ?.let { task ->
                val payload = createDataMatrixPayload("Task/${task.taskId}", task.accessCode)
                val matrix = createMatrixCode(payload)

                if (task.rawKBVBundle != null) {
                    mapToUIPrescriptionDetailSynced(
                        task,
                        MedicationDetail(text = task.medicationText),
                        MedicationRequestDetail(),
                        null,
                        InsuranceCompanyDetail(),
                        OrganizationDetail(name = task.organization),
                        PatientDetail(),
                        PractitionerDetail(),
                        matrix,
                        false,
                        LocalDateTime.now()
                    )
                } else {
                    mapToUIPrescriptionDetailScanned(task, matrix, false)
                }
            } ?: error("task $taskId not found")
    }

    override suspend fun deletePrescription(taskId: String, isRemoteTask: Boolean): Result<Unit> {
        prescriptionDemoDataSource.deleteTaskByTaskId(taskId)
        return Result.Success(Unit)
    }

    override fun loadTasksForRedeemedOn(redeemedOn: OffsetDateTime): Flow<List<Task>> {
        return emptyFlow()
    }

    override fun loadAuditEvents(taskId: String): Flow<List<AuditEventSimple>> {
        return prescriptionDemoDataSource.loadAuditEvents(taskId)
    }

    override suspend fun saveLowDetailEvent(lowDetailEvent: LowDetailEventSimple) {
    }

    override suspend fun loadLowDetailEvents(taskId: String): Flow<List<LowDetailEventSimple>> {
        return emptyFlow()
    }

    override suspend fun deleteLowDetailEvents(taskId: String) {
    }

    override suspend fun getAllTasksWithTaskIdOnly(): List<String> {
        return prescriptionDemoDataSource.getAllTasksWithTaskIdOnly()
    }

    override suspend fun redeem(taskIds: List<String>, redeem: Boolean, all: Boolean) {
        return prescriptionDemoDataSource.redeem(taskIds, redeem, all)
    }

    override suspend fun unRedeemMorePossible(taskId: String): Boolean {
        return prescriptionDemoDataSource.unRedeemMorePossible(taskId)
    }

    override suspend fun editScannedPrescriptionsName(
        name: String,
        scanSessionEnd: OffsetDateTime
    ) {
        prescriptionDemoDataSource.editScannedPrescriptionsName(name, scanSessionEnd)
    }
}
