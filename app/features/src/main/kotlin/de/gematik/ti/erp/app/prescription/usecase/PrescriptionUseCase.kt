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
import de.gematik.ti.erp.app.prescription.mapper.toPrescription
import de.gematik.ti.erp.app.prescription.model.ParsedScannedQrCode
import de.gematik.ti.erp.app.prescription.model.ParserScannedDataMatrix
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import de.gematik.ti.erp.app.prescription.repository.TaskSyncRepository
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeValidator.Companion.taskPattern
import de.gematik.ti.erp.app.prescription.ui.ValidScannedCode
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import de.gematik.ti.erp.app.utils.letNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class PrescriptionUseCase(
    private val repository: TaskOperationsRepository,
    private val taskRepository: TaskSyncRepository,
    private val dispatchers: DispatchProvider
) {

    @Deprecated("Used only in tests")
    fun syncedActiveRecipes(
        profileId: ProfileIdentifier,
        now: Instant = Clock.System.now()
    ): Flow<List<PrescriptionUseCaseData.Prescription.Synced>> =
        syncedTasks(profileId).map { tasks ->
            tasks.filter { it.isActive() }
                .sortedWith(compareBy<TaskErpModel.Synced.Prescription> { it.expiresOn }.thenBy { it.authoredOn })
                .groupBy { it.practitioner?.name ?: it.organization?.name }
                .flatMap { (_, tasks) ->
                    tasks.map {
                        val prescription = it.toPrescription()
                        PrescriptionUseCaseData.Prescription.Synced(
                            taskId = it.taskId,
                            name = it.medicationName(),
                            isIncomplete = it.isIncomplete,
                            organization = it.organizationName() ?: "",
                            authoredOn = it.authoredOn,
                            redeemedOn = null,
                            expiresOn = it.expiresOn,
                            acceptUntil = it.acceptUntil,
                            state = prescription.state,
                            isDiga = false,
                            isDirectAssignment = it.isDirectAssignment(),
                            multiplePrescriptionState = PrescriptionUseCaseData.Prescription.MultiplePrescriptionState(
                                isPartOfMultiplePrescription = it.medicationRequest
                                    ?.multiplePrescriptionInfo?.indicator ?: false,
                                numerator = it.medicationRequest
                                    ?.multiplePrescriptionInfo?.numbering?.numerator?.value,
                                denominator = it.medicationRequest
                                    ?.multiplePrescriptionInfo?.numbering?.denominator?.value,
                                start = it.medicationRequest?.multiplePrescriptionInfo?.start
                            )
                        )
                    }
                }
        }

    @Deprecated("Used only in tests")
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
                        communications = emptyList()
                    )
                }
        }

    @Deprecated("Used only in tests")
    fun redeemedPrescriptions(
        profileId: ProfileIdentifier,
        now: Instant = Clock.System.now()
    ): Flow<List<PrescriptionUseCaseData.Prescription>> =
        combine(
            scannedTasks(profileId),
            syncedTasks(profileId)
        ) { scannedTasks, syncedTasks ->
            val syncedPrescriptions = syncedTasks
                .filter { !it.isActive() }
                .map {
                    val prescription = it.toPrescription()
                    PrescriptionUseCaseData.Prescription.Synced(
                        taskId = it.taskId,
                        isIncomplete = it.isIncomplete,
                        name = it.medicationName(),
                        organization = it.practitioner?.name ?: it.organization?.name ?: "",
                        authoredOn = it.authoredOn,
                        redeemedOn = it.redeemedOn,
                        expiresOn = it.expiresOn,
                        acceptUntil = it.acceptUntil,
                        state = prescription.state,
                        isDiga = false,
                        isDirectAssignment = it.isDirectAssignment(),
                        multiplePrescriptionState = PrescriptionUseCaseData.Prescription.MultiplePrescriptionState(
                            isPartOfMultiplePrescription = it.medicationRequest?.multiplePrescriptionInfo?.indicator ?: false,
                            numerator = it.medicationRequest?.multiplePrescriptionInfo?.numbering?.numerator?.value,
                            denominator = it.medicationRequest?.multiplePrescriptionInfo?.numbering?.denominator?.value,
                            start = it.medicationRequest?.multiplePrescriptionInfo?.start
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
                        communications = emptyList()
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

    // TODO: Only used in Share viewmodel, needs its own usecase
    suspend fun saveScannedTasks(
        profileId: ProfileIdentifier,
        tasks: List<TaskErpModel.Scanned>,
        medicationString: String
    ) {
        repository.saveScannedTaskList(profileId, tasks, medicationString)
    }

    // TODO: Used in scan controller, needs its own usecase
    suspend fun saveScannedCodes(
        profileId: ProfileIdentifier,
        scannedCodes: List<ValidScannedCode>,
        medicationString: String
    ) {
        val tasks = scannedCodes.flatMap { codesEmbeddedInScan ->
            codesEmbeddedInScan.codes.mapIndexed { index, parsedCode ->
                when (parsedCode) {
                    is ParsedScannedQrCode -> TaskErpModel.Scanned(
                        profileId = profileId,
                        taskId = parsedCode.taskId,
                        index = index,
                        name = if (parsedCode.name.isNotNullOrEmpty()) parsedCode.name else "", // if empty name will be set at database
                        accessCode = parsedCode.accessCode,
                        scannedOn = codesEmbeddedInScan.raw.scannedOn,
                        redeemedOn = null,
                        isEuRedeemable = false
                    )

                    is ParserScannedDataMatrix -> {
                        val match = taskPattern.matchEntire(parsedCode.taskUrl)
                        val taskId = match?.groupValues?.get(1)
                        val accessCode = match?.groupValues?.get(2)

                        letNotNull(taskId, accessCode) { task, taskAccessCode ->
                            TaskErpModel.Scanned(
                                profileId = profileId,
                                taskId = task,
                                index = index,
                                name = "", // name will be set at database
                                accessCode = taskAccessCode,
                                scannedOn = codesEmbeddedInScan.raw.scannedOn,
                                redeemedOn = null,
                                isEuRedeemable = false
                            )
                        }
                    }
                }
            }
        }.filterNotNull()
        tasks.takeIf { it.isNotEmpty() }?.let { saveScannedTasks(profileId, it, medicationString) }
    }

    fun scannedTasks(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Scanned>> =
        repository.loadScannedTaskListByProfileId(profileId).flowOn(dispatchers.io)

    fun syncedTasks(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Prescription>> =
        repository.loadSyncedTaskListByProfileId(profileId).flowOn(dispatchers.io)

    // TODO: used in debug, maybe can be moved to debug module?
    suspend fun downloadTasks(profileId: ProfileIdentifier): Result<Int> =
        taskRepository.downloadTasks(profileId)

    // TODO: Own usecase , used in share controller
    fun getAllTasksWithTaskIdOnly(): Flow<List<String>> =
        repository.loadTaskIds()
}
