/*
 * Copyright (c) 2023 gematik GmbH
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
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.model.SimpleAuditEvent
import de.gematik.ti.erp.app.prescription.repository.model.SimpleMedicationDispense
import de.gematik.ti.erp.app.prescription.repository.model.SimpleTask
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray

class PrescriptionUseCase(
    private val repository: PrescriptionRepository,
    private val mapper: PrescriptionMapper
) {
    enum class PrescriptionType {
        NotDispensed, Dispensed
    }

    fun tasks(): Flow<List<SimpleTask>> = repository.tasks()

    fun prescriptions(
        type: PrescriptionType = PrescriptionType.NotDispensed
    ): Flow<List<PrescriptionUseCaseData.Prescription>> =
        combine(tasks(), medicationDispenses()) { tasks, dispenses ->
            val dispenseIds = dispenses.map { it.taskId }
            tasks
                .filter {
                    when (type) {
                        PrescriptionType.NotDispensed -> it.taskId !in dispenseIds
                        PrescriptionType.Dispensed -> it.taskId in dispenseIds
                    }
                }
                .sortedByDescending { it.authoredOn }
                .map { task ->
                    when (type) {
                        PrescriptionType.NotDispensed -> mapper.mapSimpleTask(task, null)
                        PrescriptionType.Dispensed -> {
                            val d = dispenses.find { task.taskId == it.taskId }
                            mapper.mapSimpleTask(task, d?.whenHandedOver?.toLocalDate())
                        }
                    }
                }
        }

    fun prescriptionDetails(taskId: String): Flow<PrescriptionUseCaseData.PrescriptionDetails> =
        combineTransform(tasks(), medicationDispenses()) { tasks, dispenses ->
            tasks
                .find { it.taskId == taskId }
                ?.let { task ->
                    val d = dispenses.filter { task.taskId == it.taskId }
                    emit(mapper.mapSimpleTaskDetailed(task, d))
                }
        }

    fun auditEvents(): Flow<List<SimpleAuditEvent>> = repository.auditEvents()

    fun audits(taskId: String): Flow<List<PrescriptionUseCaseData.PrescriptionAudit>> =
        auditEvents().map { events ->
            events
                .filter { it.taskId == taskId }
                .map {
                    PrescriptionUseCaseData.PrescriptionAudit(
                        text = it.text,
                        timestamp = it.timestamp
                    )
                }
        }

    fun medicationDispenses(): Flow<List<SimpleMedicationDispense>> = repository.medicationDispenses()

    suspend fun update() = repository.download()
    suspend fun delete(taskId: String) = repository.delete(taskId)
}

fun createMatrixCode(taskId: String, accessCode: String): BitMatrix {
    return DataMatrixWriter().encode(buildDataMatrixPayload(taskId, accessCode), BarcodeFormat.DATA_MATRIX, 1, 1)
}

private fun buildDataMatrixPayload(taskId: String, accessCode: String): String =
    buildJsonObject {
        putJsonArray("urls") {
            add("Task/$taskId/\$accept?ac=$accessCode")
        }
    }.toString().replace("\\", "")
