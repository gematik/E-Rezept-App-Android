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
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetail
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import org.hl7.fhir.r4.model.CapabilityStatement
import java.time.OffsetDateTime
import javax.inject.Inject

/**
 * Manages Demo/Production switch until we can have module switching for our injection framework.
 * If you would return a flow it is necessary to combine the demo and the production flow with the
 * demoMode.demoModeActive flow to auto update
 */
@OptIn(FlowPreview::class)
class PrescriptionUseCaseDelegate @Inject constructor(
    private val demoDelegate: PrescriptionUseCaseDemo,
    private val productionDelegate: PrescriptionUseCaseProduction,
    private val demoUseCase: DemoUseCase
) : PrescriptionUseCase {

    private val delegate: PrescriptionUseCase
        get() = if (demoUseCase.isDemoModeActive) demoDelegate else productionDelegate

    override fun tasks(): Flow<List<Task>> =
        demoUseCase.demoModeActive.flatMapConcat { delegate.tasks() }

    override fun syncedTasks(): Flow<List<Task>> =
        demoUseCase.demoModeActive.flatMapConcat { delegate.syncedTasks() }

    override fun scannedTasks(): Flow<List<Task>> =
        demoUseCase.demoModeActive.flatMapConcat { delegate.scannedTasks() }

    override fun loadAuditEvents(taskId: String): Flow<List<AuditEventSimple>> {
        return demoUseCase.demoModeActive.flatMapConcat { delegate.loadAuditEvents(taskId) }
    }

    override suspend fun saveLowDetailEvent(lowDetailEvent: LowDetailEventSimple) {
        delegate.saveLowDetailEvent(lowDetailEvent)
    }

    override suspend fun loadLowDetailEvents(taskId: String): Flow<List<LowDetailEventSimple>> {
        return delegate.loadLowDetailEvents(taskId)
    }

    override suspend fun deleteLowDetailEvents(taskId: String) {
        delegate.deleteLowDetailEvents(taskId)
    }

    override suspend fun saveScannedTasks(tasks: List<Task>) {
        delegate.saveScannedTasks(tasks)
    }

    override suspend fun capabilityStatement(): Result<CapabilityStatement> {
        return delegate.capabilityStatement()
    }

    override suspend fun downloadTasks(): Result<Unit> {
        return delegate.downloadTasks()
    }

    override suspend fun downloadCommunications(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun generatePrescriptionDetails(
        taskId: String
    ): UIPrescriptionDetail {
        return delegate.generatePrescriptionDetails(taskId)
    }

    override suspend fun deletePrescription(taskId: String, isRemoteTask: Boolean) =
        delegate.deletePrescription(taskId, isRemoteTask)

    override fun loadTasksForRedeemedOn(redeemedOn: OffsetDateTime): Flow<List<Task>> {
        return delegate.loadTasksForRedeemedOn(redeemedOn)
    }

    override suspend fun getAllTasksWithTaskIdOnly(): List<String> {
        return delegate.getAllTasksWithTaskIdOnly()
    }

    override suspend fun redeem(taskIds: List<String>, redeem: Boolean, all: Boolean) {
        return delegate.redeem(taskIds, redeem, all)
    }

    override suspend fun unRedeemMorePossible(taskId: String): Boolean {
        return delegate.unRedeemMorePossible(taskId)
    }

    override suspend fun editScannedPrescriptionsName(
        name: String,
        scanSessionEnd: OffsetDateTime
    ) {
        delegate.editScannedPrescriptionsName(name, scanSessionEnd)
    }
}
