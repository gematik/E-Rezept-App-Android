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
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.db.entities.TaskStatus
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetail
import de.gematik.ti.erp.app.prescription.ui.ValidScannedCode
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime

// gemSpec_FD_eRp: A_21267 Prozessparameter - Berechtigungen für Nutzer
const val DIRECT_ASSIGNMENT_INDICATOR = "169" // direct assignment taskID starts with 169

interface PrescriptionUseCase {

    fun tasks(): Flow<List<Task>>

    /**
     * With the backend synchronized tasks.
     */
    fun syncedTasks(): Flow<List<Task>>

    /**
     * Scanned codes from the offline e-prescription paper.
     */
    fun scannedTasks(): Flow<List<Task>>

    /**
     * Tasks grouped by timestamp and organization (e.g. doctor).
     * Mapped to [PrescriptionUseCaseData.Prescription.Synced].
     */
    fun syncedRecipes(): Flow<List<PrescriptionUseCaseData.Prescription.Synced>> =
        syncedTasks().map { tasks ->
            tasks.filter { it.isSyncedTaskRedeemable() }
                .sortedByDescending { it.authoredOn }
                .groupBy { it.organization }
                .flatMap { (_, tasks) ->
                    tasks.map {
                        PrescriptionUseCaseData.Prescription.Synced(
                            taskId = it.taskId,
                            name = it.medicationText ?: "",
                            organization = it.organization ?: "",
                            authoredOn = requireNotNull(it.authoredOn),
                            redeemedOn = null,
                            expiresOn = it.expiresOn,
                            acceptUntil = it.acceptUntil,
                            status = mapStatus(it.status),
                            isDirectAssignment = it.taskId.startsWith(DIRECT_ASSIGNMENT_INDICATOR)
                        )
                    }
                }
        }

    /**
     * Tasks grouped by timestamp. Mapped to [PrescriptionUseCaseData.Prescription.Scanned].
     */
    fun scannedRecipes(): Flow<List<PrescriptionUseCaseData.Prescription.Scanned>> =
        scannedTasks().map { tasks ->
            tasks
                .filter { it.isScannedTaskRedeemable() }
                .sortedWith(compareByDescending<Task> { it.scanSessionEnd }.thenBy { requireNotNull(it.nrInScanSession) })
                .map { task ->
                    PrescriptionUseCaseData.Prescription.Scanned(
                        taskId = task.taskId,
                        scannedOn = requireNotNull(task.scanSessionEnd),
                        redeemedOn = task.redeemedOn
                    )
                }
        }

    fun redeemedPrescriptions(): Flow<List<PrescriptionUseCaseData.Prescription>> =
        combine(
            scannedTasks(),
            syncedTasks()
        ) { scannedTasks, syncedTasks ->
            val syncedPrescriptions = syncedTasks
                .filter { it.isSyncedTaskRedeemed() }
                .map {
                    PrescriptionUseCaseData.Prescription.Synced(
                        taskId = it.taskId,
                        name = it.medicationText ?: "",
                        organization = it.organization ?: "",
                        authoredOn = requireNotNull(it.authoredOn),
                        redeemedOn = it.redeemedOn,
                        expiresOn = it.expiresOn,
                        acceptUntil = it.acceptUntil,
                        status = mapStatus(it.status),
                        isDirectAssignment = it.taskId.startsWith(DIRECT_ASSIGNMENT_INDICATOR)
                    )
                }

            val scannedPrescriptions = scannedTasks
                .filter { task -> task.redeemedOn != null }
                .map { task ->
                    PrescriptionUseCaseData.Prescription.Scanned(
                        taskId = task.taskId,
                        scannedOn = requireNotNull(task.scanSessionEnd),
                        redeemedOn = task.redeemedOn
                    )
                }

            (syncedPrescriptions + scannedPrescriptions)
                .sortedWith(compareByDescending<PrescriptionUseCaseData.Prescription> { it.redeemedOn }.thenBy { it.taskId })
        }

    fun redeemableAndValidSyncedTaskIds(): Flow<List<String>> =
        syncedTasks().map { tasks ->
            tasks.filter { it.isRedeemableAndValid() }.map { it.taskId }
        }

    fun redeemableScannedTaskIds(): Flow<List<String>> =
        scannedTasks().map { tasks ->
            tasks.filter { it.isScannedTaskRedeemable() }.map { it.taskId }
        }

    private fun Task.isSyncedTaskRedeemable(): Boolean = with(expiresOn) {
        this != null && this.toEpochDay() >= LocalDate.now().toEpochDay() &&
            (status == TaskStatus.Ready || status == TaskStatus.InProgress)
    }

    private fun Task.isScannedTaskRedeemable(): Boolean = when (this.status) {
        TaskStatus.Completed -> false
        else -> this.redeemedOn == null
    }

    private fun Task.isRedeemableAndValid(): Boolean = isSyncedTaskRedeemable() && accessCode != null

    private fun Task.isSyncedTaskRedeemed(): Boolean = !isSyncedTaskRedeemable()

    private fun mapStatus(status: TaskStatus?): PrescriptionUseCaseData.Prescription.Synced.Status =
        when (status) {
            TaskStatus.Ready -> PrescriptionUseCaseData.Prescription.Synced.Status.Ready
            TaskStatus.InProgress -> PrescriptionUseCaseData.Prescription.Synced.Status.InProgress
            TaskStatus.Completed -> PrescriptionUseCaseData.Prescription.Synced.Status.Completed
            else -> PrescriptionUseCaseData.Prescription.Synced.Status.Unknown
        }

    /**
     * Throws an exception if any task doesn't match the requirements.
     */
    suspend fun saveScannedTasks(tasks: List<Task>)

    /**
     * Fetch tasks from the backend and store them into the database.
     * The [Result] contains any errors
     */
    suspend fun downloadTasks(profileName: String): Result<Int>

    /**
     * Fetch communications from the backend and store them into the database.
     */
    suspend fun downloadCommunications(profileName: String): Result<Unit>

    /**
     * Fetch audit events from the backend and store them into the database.
     */
    fun downloadAllAuditEvents(
        profileName: String
    )

    suspend fun generatePrescriptionDetails(
        taskId: String,
    ): UIPrescriptionDetail

    suspend fun deletePrescription(taskId: String, isRemoteTask: Boolean): Result<Unit>

    fun loadTasksForRedeemedOn(
        redeemedOn: OffsetDateTime,
        profileName: String
    ): Flow<List<Task>>

    suspend fun saveLowDetailEvent(lowDetailEvent: LowDetailEventSimple)
    suspend fun loadLowDetailEvents(taskId: String): Flow<List<LowDetailEventSimple>>
    suspend fun deleteLowDetailEvents(taskId: String)

    suspend fun getAllTasksWithTaskIdOnly(): List<String>
    suspend fun redeem(taskIds: List<String>, redeem: Boolean, all: Boolean)
    suspend fun unRedeemMorePossible(taskId: String, profileName: String): Boolean
    suspend fun editScannedPrescriptionsName(name: String, scanSessionEnd: OffsetDateTime)
    suspend fun mapScannedCodeToTask(scannedCodes: List<ValidScannedCode>)
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
