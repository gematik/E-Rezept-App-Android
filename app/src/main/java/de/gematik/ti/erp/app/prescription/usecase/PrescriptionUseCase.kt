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

import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetail
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hl7.fhir.r4.model.CapabilityStatement
import org.json.JSONArray
import org.json.JSONObject
import java.time.OffsetDateTime

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
     * Mapped to [PrescriptionUseCaseData.Recipe.Synced].
     */
    fun syncedRecipes(): Flow<List<PrescriptionUseCaseData.Recipe.Synced>> =
        syncedTasks().map { tasks ->
            tasks.filter { task -> task.redeemedOn == null }
                .sortedByDescending { it.authoredOn }
                .groupBy { requireNotNull(it.authoredOn) }
                .flatMap { (authoredOn, tasks) ->
                    tasks
                        .groupBy { it.organization }
                        .map { (organization, tasks) ->
                            val prescriptions = tasks.map {
                                PrescriptionUseCaseData.Prescription.Synced(
                                    taskId = it.taskId,
                                    name = it.medicationText ?: "",
                                    expiresOn = it.expiresOn,
                                )
                            }

                            PrescriptionUseCaseData.Recipe.Synced(
                                organization = organization ?: "",
                                authoredOn = authoredOn,
                                prescriptions = prescriptions,
                                redeemedOn = null
                            )
                        }
                }
        }

    /**
     * Tasks grouped by timestamp. Mapped to [PrescriptionUseCaseData.Recipe.Scanned].
     */
    fun scannedRecipes(): Flow<List<PrescriptionUseCaseData.Recipe.Scanned>> =
        scannedTasks().map { tasks ->
            tasks.filter { task -> task.redeemedOn == null }
                .groupBy { requireNotNull(it.scanSessionEnd) }
                .map { (scanSessionEnd, tasks) ->
                    val prescriptions = tasks
                        .sortedBy { requireNotNull(it.nrInScanSession) }
                        .map { task ->
                            PrescriptionUseCaseData.Prescription.Scanned(
                                taskId = task.taskId,
                                nr = requireNotNull(task.nrInScanSession),
                            )
                        }

                    PrescriptionUseCaseData.Recipe.Scanned(
                        title = tasks.first().scanSessionName,
                        scanSessionEnd = scanSessionEnd,
                        prescriptions = prescriptions,
                        redeemedOn = null
                    )
                }
                .sortedByDescending { it.scanSessionEnd }
        }

    /**
     * Redeemed Tasks grouped by timestamp. Mapped to [PrescriptionUseCaseData.RedeemedRecipe.Synced].
     */
    fun redeemedSyncedRecipes(): Flow<List<PrescriptionUseCaseData.Recipe.Synced>> =
        syncedTasks().map { tasks ->
            tasks.filter { task -> task.redeemedOn != null }.sortedByDescending { it.redeemedOn }
                .groupBy { requireNotNull(it.authoredOn) }
                .flatMap { (authoredOn, tasks) ->
                    tasks
                        .groupBy { it.organization }
                        .map { (organization, tasks) ->
                            val prescriptions = tasks.map {
                                PrescriptionUseCaseData.Prescription.Synced(
                                    taskId = it.taskId,
                                    name = it.medicationText ?: "",
                                    expiresOn = it.expiresOn,
                                )
                            }

                            PrescriptionUseCaseData.Recipe.Synced(
                                organization = requireNotNull(organization),
                                authoredOn = authoredOn,
                                prescriptions = prescriptions,
                                redeemedOn = tasks.first().redeemedOn
                            )
                        }
                }
        }

    /**
     * Redeemed Tasks grouped by timestamp. Mapped to [PrescriptionUseCaseData.RedeemedRecipe.Scanned].
     */
    fun redeemedScannedRecipes(): Flow<List<PrescriptionUseCaseData.Recipe.Scanned>> =
        scannedTasks().map { tasks ->
            tasks.filter { task -> task.redeemedOn != null }
                .groupBy { requireNotNull(it.redeemedOn) }
                .flatMap { (redeemed, tasks) ->
                    tasks.groupBy {
                        redeemed
                    }.map { (_, tasks) ->
                        val prescriptions = tasks
                            .map { task ->
                                PrescriptionUseCaseData.Prescription.Scanned(
                                    taskId = task.taskId,
                                    nr = requireNotNull(task.nrInScanSession),
                                )
                            }

                        PrescriptionUseCaseData.Recipe.Scanned(
                            title = tasks.first().scanSessionName,
                            scanSessionEnd = tasks.first().scanSessionEnd!!,
                            prescriptions = prescriptions,
                            redeemedOn = tasks.first().redeemedOn
                        )
                    }
                }
        }

    /**
     * Throws an exception if any task doesn't match the requirements.
     */
    suspend fun saveScannedTasks(tasks: List<Task>)

    suspend fun capabilityStatement(): Result<CapabilityStatement>

    /**
     * Fetch tasks from the backend and store them into the database.
     * The [Result] contains any errors
     */
    suspend fun downloadTasks(): Result<Unit>

    /**
     * Fetch communications from the backend and store them into the database.
     * The [Result] contains any errors
     */
    suspend fun downloadCommunications(): Result<Unit>

    suspend fun generatePrescriptionDetails(
        taskId: String,
    ): UIPrescriptionDetail

    suspend fun deletePrescription(taskId: String, isRemoteTask: Boolean): Result<Unit>

    fun loadTasksForRedeemedOn(redeemedOn: OffsetDateTime): Flow<List<Task>>

    fun loadAuditEvents(taskId: String): Flow<List<AuditEventSimple>>

    suspend fun saveLowDetailEvent(lowDetailEvent: LowDetailEventSimple)
    suspend fun loadLowDetailEvents(taskId: String): Flow<List<LowDetailEventSimple>>
    suspend fun deleteLowDetailEvents(taskId: String)

    suspend fun getAllTasksWithTaskIdOnly(): List<String>
    suspend fun redeem(taskIds: List<String>, redeem: Boolean, all: Boolean)
    suspend fun unRedeemMorePossible(taskId: String): Boolean
    suspend fun editScannedPrescriptionsName(name: String, scanSessionEnd: OffsetDateTime)
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
