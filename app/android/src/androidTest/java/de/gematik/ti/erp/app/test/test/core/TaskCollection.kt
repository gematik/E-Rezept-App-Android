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

package de.gematik.ti.erp.app.test.test.core

import android.util.Log
import de.gematik.ti.erp.app.TestWrapper
import de.gematik.ti.erp.app.test.test.core.prescription.CommunicationPayloadInbox
import de.gematik.ti.erp.app.test.test.core.prescription.Prescription
import de.gematik.ti.erp.app.test.test.core.prescription.PrescriptionDoctorUseCase
import de.gematik.ti.erp.app.test.test.core.prescription.PrescriptionPharmacyUseCase
import de.gematik.ti.erp.app.test.test.core.prescription.Task
import de.gematik.ti.erp.app.test.test.core.prescription.retry
import org.junit.Assume
import org.junit.AssumptionViolatedException

private val doctorUseCase by lazy { PrescriptionDoctorUseCase() }
private val pharmacyUseCase by lazy { PrescriptionPharmacyUseCase() }

class TaskCollection(
    data: List<TaskAndPrescription>,
    private val testWrapper: TestWrapper
) {
    data class TaskAndPrescription(
        val task: Task,
        val prescription: Prescription,
        val isDispensed: Boolean = false
    )

    private val _taskData = mutableListOf<TaskAndPrescription>()
        .apply {
            addAll(data)
        }

    val taskData: List<TaskAndPrescription>
        get() = _taskData

    fun deleteAll() {
        // clean up - allowed to throw, so test won't fail
        taskData.forEach { (task, _, isDispensed) ->
            if (task.secret == null || isDispensed) {
                testWrapper.deleteTask(task.taskId)
            } else {
                runCatching {
                    pharmacyUseCase
                        .abortTask(taskId = task.taskId, accessCode = task.accessCode, secret = task.secret)
                }
            }
                .onFailure { Log.e("deleteTask", "Deleting task with id `${task.taskId}` failed", it) }
                .onSuccess { Log.d("deleteTask", "Task with id `${task.taskId}` deleted") }
        }
    }

    fun accept(data: TaskAndPrescription): Task {
        val taskWithSecret = pharmacyUseCase.acceptTask(taskId = data.task.taskId, accessCode = data.task.accessCode)
        requireNotNull(taskWithSecret.secret) { "Accepting a task must return secret!" }

        _taskData.replaceWith(data.copy(task = taskWithSecret))

        return taskWithSecret
    }

    fun reply(data: TaskAndPrescription, message: CommunicationPayloadInbox) {
        pharmacyUseCase.replyWithCommunication(
            taskId = data.task.taskId,
            kvNr = data.prescription.patient!!.kvnr!!,
            message = message
        )
    }

    fun dispense(data: TaskAndPrescription) {
        requireNotNull(data.task.secret) { "Task can be only dispensed with the `secret`" }
        pharmacyUseCase.dispenseMedication(
            taskId = data.task.taskId,
            accessCode = data.task.accessCode,
            secret = data.task.secret
        )
        _taskData.replaceWith(data.copy(isDispensed = true))
    }

    companion object {
        fun generate(count: Int, kvnr: String, testWrapper: TestWrapper): TaskCollection {
            require(count >= 1)

            val taskData = (1..count).mapNotNull {
                retry(3) { doctorUseCase.prescribeToPatient(kvnr) }
            }
            val prescriptions = taskData.mapNotNull { data ->
                retry(3) { doctorUseCase.prescriptionDetails(data.taskId) }
            }

            try {
                Assume.assumeTrue(
                    "Prescription server couldn't create all tasks",
                    taskData.size == prescriptions.size && taskData.size == count
                )
            } catch (e: AssumptionViolatedException) {
                taskData.forEach { data ->
                    val taskId = data.taskId
                    testWrapper.deleteTask(taskId)
                        .onFailure { Log.e("deleteTask", "Deleting task with id `$taskId` failed", it) }
                        .onSuccess { Log.d("deleteTask", "Task with id `$taskId` deleted") }
                }
                throw e
            }

            val data = taskData.zip(prescriptions) { tD: Task, p: Prescription ->
                TaskAndPrescription(
                    task = tD,
                    prescription = p
                )
            }

            Log.d("generate", "Prescriptions:\n${prescriptions.joinToString("\n\n") { it.toString() }}")

            return TaskCollection(
                data,
                testWrapper
            )
        }
    }
}

private fun MutableList<TaskCollection.TaskAndPrescription>.replaceWith(data: TaskCollection.TaskAndPrescription) {
    val ix = indexOfFirst { it.task.taskId == data.task.taskId }
    require(ix != -1)
    this[ix] = data
}
