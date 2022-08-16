/*
 * Copyright (c) 2022 gematik GmbH
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

import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetail
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetailScanned
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetailSynced
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeValidator
import de.gematik.ti.erp.app.prescription.ui.ValidScannedCode
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import io.github.aakira.napier.Napier
import java.time.Instant

// gemSpec_FD_eRp: A_21267 Prozessparameter - Berechtigungen für Nutzer
const val DIRECT_ASSIGNMENT_INDICATOR = "169" // direct assignment taskID starts with 169

class PrescriptionUseCase(
    private val repository: PrescriptionRepository,
    private val dispatchers: DispatchProvider
) {

    fun syncedActiveRecipes(
        profileId: ProfileIdentifier,
        now: Instant = Instant.now()
    ): Flow<List<PrescriptionUseCaseData.Prescription.Synced>> =
        syncedTasks(profileId).map { tasks ->
            tasks.filter { it.isActive(now) }
                .sortedByDescending { it.authoredOn }
                .groupBy { it.practitioner.name ?: it.organization.name }
                .flatMap { (_, tasks) ->
                    tasks.map {
                        PrescriptionUseCaseData.Prescription.Synced(
                            taskId = it.taskId,
                            name = it.medicationName() ?: "",
                            organization = it.organizationName() ?: "",
                            authoredOn = it.authoredOn,
                            redeemedOn = null,
                            expiresOn = it.expiresOn,
                            acceptUntil = it.acceptUntil,
                            state = it.state(now = now),
                            isDirectAssignment = it.taskId.startsWith(DIRECT_ASSIGNMENT_INDICATOR)
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
                        redeemedOn = task.redeemedOn
                    )
                }
        }

    fun redeemedPrescriptions(
        profileId: ProfileIdentifier,
        now: Instant = Instant.now()
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
                        name = it.medicationRequest.medication?.text ?: "",
                        organization = it.practitioner.name ?: it.organization.name ?: "",
                        authoredOn = requireNotNull(it.authoredOn),
                        redeemedOn = it.redeemedOn(),
                        expiresOn = it.expiresOn,
                        acceptUntil = it.acceptUntil,
                        state = it.state(now),
                        isDirectAssignment = it.taskId.startsWith(DIRECT_ASSIGNMENT_INDICATOR)
                    )
                }

            val scannedPrescriptions = scannedTasks
                .filter { task -> task.redeemedOn != null }
                .map { task ->
                    PrescriptionUseCaseData.Prescription.Scanned(
                        taskId = task.taskId,
                        scannedOn = task.scannedOn,
                        redeemedOn = task.redeemedOn
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
        withContext(dispatchers.IO) {
            repository.saveScannedTasks(profileId, tasks)
        }

    suspend fun saveScannedCodes(profileId: ProfileIdentifier, scannedCodes: List<ValidScannedCode>) {
        val tasks = scannedCodes.flatMap { code ->
            code.extract().map { (_, taskId, accessCode) ->
                ScannedTaskData.ScannedTask(
                    profileId = "",
                    taskId = taskId,
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
        repository.scannedTasks(profileId).flowOn(dispatchers.IO)

    fun syncedTasks(profileId: ProfileIdentifier): Flow<List<SyncedTaskData.SyncedTask>> =
        repository.syncedTasks(profileId).flowOn(dispatchers.IO)

    suspend fun downloadTasks(profileId: ProfileIdentifier): Result<Int> =
        repository.downloadTasks(profileId)

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun generatePrescriptionDetails(
        taskId: String
    ): Flow<UIPrescriptionDetail> =
        repository.loadSyncedTaskByTaskId(taskId).transformLatest { task ->
            if (task == null) {
                repository.loadScannedTaskByTaskId(taskId).collectLatest { scannedTask ->
                    if (scannedTask == null) {
                        Napier.w("No task `$taskId` found!")
                    } else {
                        val payload = createDataMatrixPayload(scannedTask.taskId, scannedTask.accessCode)

                        emit(
                            UIPrescriptionDetailScanned(
                                profileId = scannedTask.profileId,
                                taskId = scannedTask.taskId,
                                redeemedOn = scannedTask.redeemedOn,
                                accessCode = scannedTask.accessCode,
                                matrixPayload = payload,
                                number = 1,
                                scannedOn = scannedTask.scannedOn
                            )
                        )
                    }
                }
            } else {
                val payload = task.accessCode?.let { createDataMatrixPayload(task.taskId, it) }

                emit(
                    UIPrescriptionDetailSynced(
                        profileId = task.profileId,
                        taskId = task.taskId,
                        redeemedOn = task.redeemedOn(),
                        accessCode = task.accessCode,
                        matrixPayload = payload,
                        expiresOn = task.expiresOn,
                        acceptUntil = task.acceptUntil,
                        patient = task.patient,
                        practitioner = task.practitioner,
                        insurance = task.insuranceInformation,
                        organization = task.organization,
                        medicationRequest = task.medicationRequest,
                        medicationDispenses = task.medicationDispenses,
                        taskStatus = task.status,
                        isRedeemableAndValid = task.redeemState().isRedeemable(),
                        state = task.state() // TODO pass now from calling function
                    )
                )
            }
        }.flowOn(dispatchers.IO)

    suspend fun deletePrescription(profileId: ProfileIdentifier, taskId: String): Result<Unit> {
        return repository.deleteTaskByTaskId(profileId, taskId)
    }

    suspend fun redeemScannedTask(taskId: String, redeem: Boolean) {
        repository.updateRedeemedOn(taskId, if (redeem) Instant.now() else null)
    }

    suspend fun getAllTasksWithTaskIdOnly(): Flow<List<String>> =
        repository.loadTaskIds()
}

fun createMatrixCode(payload: String): BitMatrix {
    return DataMatrixWriter().encode(payload, BarcodeFormat.DATA_MATRIX, 1, 1)
}

fun createDataMatrixPayload(taskId: String, code: String): String {
    val value = "Task/$taskId/\$accept?ac=$code"
    val rootObject = JSONObject()
    val urls = JSONArray()
    urls.put(value)
    rootObject.put("urls", urls)
    return rootObject.toString().replace("\\", "")
}
