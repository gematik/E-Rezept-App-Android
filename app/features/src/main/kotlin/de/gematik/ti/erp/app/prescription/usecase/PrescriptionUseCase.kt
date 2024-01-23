/*
 * Copyright (c) 2024 gematik GmbH
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

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.TaskRepository
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeValidator
import de.gematik.ti.erp.app.prescription.ui.ValidScannedCode
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class PrescriptionUseCase(
    private val repository: PrescriptionRepository,
    private val taskRepository: TaskRepository,
    private val dispatchers: DispatchProvider
) {

    fun syncedActiveRecipes(
        profileId: ProfileIdentifier,
        now: Instant = Clock.System.now()
    ): Flow<List<PrescriptionUseCaseData.Prescription.Synced>> =
        syncedTasks(profileId).map { tasks ->
            tasks.filter { it.isActive(now) }
                .sortedWith(compareBy<SyncedTaskData.SyncedTask> { it.expiresOn }.thenBy { it.authoredOn })
                .groupBy { it.practitioner.name ?: it.organization.name }
                .flatMap { (_, tasks) ->
                    tasks.map {
                        PrescriptionUseCaseData.Prescription.Synced(
                            taskId = it.taskId,
                            name = it.medicationName(),
                            isIncomplete = it.isIncomplete,
                            organization = it.organizationName() ?: "",
                            authoredOn = it.authoredOn,
                            redeemedOn = null,
                            expiresOn = it.expiresOn,
                            acceptUntil = it.acceptUntil,
                            state = it.state(now = now),
                            isDirectAssignment = it.isDirectAssignment(),
                            multiplePrescriptionState = PrescriptionUseCaseData.Prescription.MultiplePrescriptionState(
                                isPartOfMultiplePrescription = it.medicationRequest
                                    .multiplePrescriptionInfo.indicator,
                                numerator = it.medicationRequest
                                    .multiplePrescriptionInfo.numbering?.numerator?.value,
                                denominator = it.medicationRequest
                                    .multiplePrescriptionInfo.numbering?.denominator?.value,
                                start = it.medicationRequest.multiplePrescriptionInfo.start
                            )
                        )
                    }
                }
        }

    /**
     * Tasks grouped by timestamp. Mapped to [PrescriptionUseCaseData.Prescription.Scanned].
     */
    fun scannedActiveRecipes(profileId: ProfileIdentifier): Flow<List<PrescriptionUseCaseData.Prescription.Scanned>> =
        scannedTasks(profileId).map { tasks ->
            tasks
                .filter { it.redeemedOn == null }
                .sortedByDescending { it.scannedOn }
                .map { task ->
                    PrescriptionUseCaseData.Prescription.Scanned(
                        taskId = task.taskId,
                        scannedOn = task.scannedOn,
                        redeemedOn = task.redeemedOn,
                        communications = task.communications
                    )
                }
        }

    fun redeemedPrescriptions(
        profileId: ProfileIdentifier,
        now: Instant = Clock.System.now()
    ): Flow<List<PrescriptionUseCaseData.Prescription>> =
        combine(
            scannedTasks(profileId),
            syncedTasks(profileId)
        ) { scannedTasks, syncedTasks ->
            val syncedPrescriptions = syncedTasks
                .filter { !it.isActive(now) }
                .map {
                    PrescriptionUseCaseData.Prescription.Synced(
                        taskId = it.taskId,
                        isIncomplete = it.isIncomplete,
                        name = it.medicationName(),
                        organization = it.practitioner.name ?: it.organization.name ?: "",
                        authoredOn = requireNotNull(it.authoredOn),
                        redeemedOn = it.redeemedOn(),
                        expiresOn = it.expiresOn,
                        acceptUntil = it.acceptUntil,
                        state = it.state(now),
                        isDirectAssignment = it.isDirectAssignment(),
                        multiplePrescriptionState = PrescriptionUseCaseData.Prescription.MultiplePrescriptionState(
                            isPartOfMultiplePrescription = it.medicationRequest.multiplePrescriptionInfo.indicator,
                            numerator = it.medicationRequest.multiplePrescriptionInfo.numbering?.numerator?.value,
                            denominator = it.medicationRequest.multiplePrescriptionInfo.numbering?.denominator?.value,
                            start = it.medicationRequest.multiplePrescriptionInfo.start
                        )
                    )
                }

            val scannedPrescriptions = scannedTasks
                .filter { task -> task.redeemedOn != null }
                .map { task ->
                    PrescriptionUseCaseData.Prescription.Scanned(
                        taskId = task.taskId,
                        scannedOn = task.scannedOn,
                        redeemedOn = task.redeemedOn,
                        communications = task.communications
                    )
                }

            (syncedPrescriptions + scannedPrescriptions)
                .sortedWith(
                    compareByDescending<PrescriptionUseCaseData.Prescription> {
                        it.redeemedOn ?: when (it) {
                            is PrescriptionUseCaseData.Prescription.Scanned -> it.scannedOn
                            is PrescriptionUseCaseData.Prescription.Synced -> it.authoredOn
                        }
                    }.thenBy { it.taskId }
                )
        }

    suspend fun saveScannedTasks(profileId: ProfileIdentifier, tasks: List<ScannedTaskData.ScannedTask>) =
        repository.saveScannedTasks(profileId, tasks)

    suspend fun saveScannedCodes(profileId: ProfileIdentifier, scannedCodes: List<ValidScannedCode>) {
        val tasks = scannedCodes.flatMap { code ->
            code.extract().mapIndexed { index, (_, taskId, accessCode) ->
                ScannedTaskData.ScannedTask(
                    profileId = profileId,
                    taskId = taskId,
                    index = index,
                    name = null,
                    accessCode = accessCode,
                    scannedOn = code.raw.scannedOn,
                    redeemedOn = null
                )
            }
        }
        tasks.takeIf { it.isNotEmpty() }?.let { saveScannedTasks(profileId, it) }
    }

    private fun ValidScannedCode.extract(): List<List<String>> =
        this.urls.mapNotNull {
            TwoDCodeValidator.taskPattern.matchEntire(it)?.groupValues
        }

    fun scannedTasks(profileId: ProfileIdentifier): Flow<List<ScannedTaskData.ScannedTask>> =
        repository.scannedTasks(profileId).flowOn(dispatchers.io)

    fun syncedTasks(profileId: ProfileIdentifier): Flow<List<SyncedTaskData.SyncedTask>> =
        repository.syncedTasks(profileId).flowOn(dispatchers.io)

    suspend fun downloadTasks(profileId: ProfileIdentifier): Result<Int> =
        taskRepository.downloadTasks(profileId)

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun generatePrescriptionDetails(
        taskId: String
    ): Flow<PrescriptionData.Prescription> =
        repository.loadSyncedTaskByTaskId(taskId).transformLatest { task ->
            if (task == null) {
                repository.loadScannedTaskByTaskId(taskId).collectLatest { scannedTask ->
                    if (scannedTask == null) {
                        Napier.w("No task `$taskId` found!")
                    } else {
                        emit(PrescriptionData.Scanned(task = scannedTask))
                    }
                }
            } else {
                emit(PrescriptionData.Synced(task = task))
            }
        }.flowOn(dispatchers.io)

    suspend fun deletePrescription(profileId: ProfileIdentifier, taskId: String): Result<Unit> {
        return repository.deleteTaskByTaskId(profileId, taskId)
    }

    suspend fun redeemScannedTask(taskId: String, redeem: Boolean) {
        repository.updateRedeemedOn(taskId, if (redeem) Clock.System.now() else null)
    }

    fun getAllTasksWithTaskIdOnly(): Flow<List<String>> =
        repository.loadTaskIds()
}
