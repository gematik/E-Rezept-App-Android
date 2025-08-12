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

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.TaskRepository
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeValidator
import de.gematik.ti.erp.app.prescription.ui.ValidScannedCode
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
                            isDiga = it.deviceRequest?.appName.isNotNullOrEmpty(),
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
                        isDiga = it.deviceRequest?.appName.isNotNullOrEmpty(),
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

    suspend fun saveScannedTasks(
        profileId: ProfileIdentifier,
        tasks: List<ScannedTaskData.ScannedTask>,
        medicationString: String
    ) {
        repository.saveScannedTasks(profileId, tasks, medicationString)
    }

    suspend fun saveScannedCodes(
        profileId: ProfileIdentifier,
        scannedCodes: List<ValidScannedCode>,
        medicationString: String
    ) {
        val tasks = scannedCodes.flatMap { code ->
            code.extract().mapIndexed { index, (_, taskId, accessCode) ->
                ScannedTaskData.ScannedTask(
                    profileId = profileId,
                    taskId = taskId,
                    index = index,
                    name = "", // name will be set later
                    accessCode = accessCode,
                    scannedOn = code.raw.scannedOn,
                    redeemedOn = null
                )
            }
        }
        tasks.takeIf { it.isNotEmpty() }?.let { saveScannedTasks(profileId, it, medicationString) }
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

    fun getAllTasksWithTaskIdOnly(): Flow<List<String>> =
        repository.loadTaskIds()
}
