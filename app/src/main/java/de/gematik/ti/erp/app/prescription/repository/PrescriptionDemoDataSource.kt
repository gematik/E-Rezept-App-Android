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

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

private fun demoTasks(now: LocalDate, nowOffset: OffsetDateTime) = listOf(
    Task(
        taskId = "full detail rezept 1_1",
        accessCode = "594fd81d9cc3c991f8ffc90b52f12909d3c7a75bce88c6b98854ace5733be213",
        organization = "Praxis Dr. Roser",
        expiresOn = now.plusDays(70),
        acceptUntil = now.plusDays(12),
        authoredOn = nowOffset.minusDays(2),
        medicationText = "Paracetamol Gematikpharm 500mg Tabletten",
        rawKBVBundle = "{}".toByteArray()
    ),
    Task(
        taskId = "full detail rezept 1_2",
        accessCode = "909d3c7a75bce88c6b98854ace5733be213594fd81d9cc3c991f8ffc90b52f12",
        organization = "Praxis Dr. Roser",
        expiresOn = now.plusDays(70),
        acceptUntil = now.plusDays(12),
        authoredOn = nowOffset.minusDays(2),
        medicationText = "Ibuprofen 400mg Gematikpharm Filmtabletten",
        rawKBVBundle = "{}".toByteArray()
    ),
    Task(
        taskId = "full detail rezept 2_1",
        accessCode = "594fd81d7cc3c991f8ffc90b52f12909d3c7a75bce88c6b98854ace5733be213",
        organization = "Praxis Dr. Georg Backhaus",
        expiresOn = now.plusDays(70),
        acceptUntil = now.plusDays(12),
        authoredOn = nowOffset.minusHours(2),
        medicationText = "Amoxicillin Gematikpharm 1000 Filmtabletten",
        rawKBVBundle = "{}".toByteArray()
    ),
    Task(
        taskId = "full detail rezept 2_2",
        accessCode = "594fd81d9cb3c991f8ffc90b52f12909d3c7a75bce88c6b98854ace5733be213",
        organization = "Praxis Dr. Georg Backhaus",
        expiresOn = now.plusDays(70),
        acceptUntil = now.plusDays(12),
        authoredOn = nowOffset.minusHours(2),
        medicationText = "Metronidazol Gematikpharm 400 Tabletten",
        rawKBVBundle = "{}".toByteArray()
    ),
    Task(
        taskId = "full detail rezept 2_3",
        accessCode = "594fd82d9cc3c991f8ffc90b52f12909d3c7a75bce88c6b98854ace5733be213",
        organization = "Praxis Dr. Georg Backhaus",
        expiresOn = now.plusDays(70),
        acceptUntil = now.plusDays(12),
        authoredOn = nowOffset.minusHours(2),
        medicationText = "Clotrimazol 1% Creme",
        rawKBVBundle = "{}".toByteArray()
    ),
    Task(
        taskId = "full detail rezept 2_4",
        accessCode = "594fd82d9cc3c991f8ffc90b92f12909d3c7a75bce88c6b98854ace5733be213",
        organization = "Praxis Dr. Georg Backhaus",
        expiresOn = now.plusDays(70),
        acceptUntil = now.plusDays(2),
        authoredOn = nowOffset.minusHours(2),
        medicationText = "Betaisodona Salbe",
        rawKBVBundle = "{}".toByteArray()
    ),
    Task(
        taskId = "full detail rezept 3_1",
        accessCode = "594fd82d9cc3c991f8ffc90b92f12909d3c7a75bce88c6b98854ace5733be213",
        organization = "Praxis Dr. Georg Backhaus",
        expiresOn = now.plusDays(70),
        acceptUntil = now.plusDays(10),
        authoredOn = nowOffset.minusMinutes(1),
        medicationText = "Hyrimoz 40 Mg/0,8 ml Inj.-Lösung",
        rawKBVBundle = "{}".toByteArray()
    ),
    Task(
        taskId = "full detail rezept 3_2",
        accessCode = "594fd82d9cc3c991f8ffc90b92f12909d3c7a75bce88c6b98854ace5733be213",
        organization = "Praxis Prof. h. c. Dr. med. Schulte",
        expiresOn = now.plusDays(70),
        acceptUntil = now.plusDays(8),
        authoredOn = nowOffset.minusMinutes(1),
        medicationText = "Lenalidomid",
        rawKBVBundle = "{}".toByteArray()
    ),
    Task(
        taskId = "full detail rezept 4_1",
        accessCode = "594fd82d9cc3c991f8ffc90b92f12909d3c7a75bce88c6b98854ace5733be213",
        organization = "Praxis Dr. Mortuus est",
        expiresOn = now.plusDays(70),
        acceptUntil = now,
        authoredOn = nowOffset,
        medicationText = "Epinephrin 1mg/ml",
        rawKBVBundle = "{}".toByteArray()
    ),
    Task(
        taskId = "full detail rezept 4_2",
        accessCode = "594fd82d9cc3c991f8ffc90b92f12909d3c7a75bce88c6b98854ace5733be213",
        organization = "Praxis Dr. Mortuus est",
        expiresOn = now.plusDays(70),
        acceptUntil = now.minusDays(4),
        authoredOn = nowOffset,
        medicationText = "Amiodaron 200 Gematikpharm Tabl.",
        rawKBVBundle = "{}".toByteArray()
    ),
    Task(
        taskId = "full detail rezept 4_3",
        accessCode = "594fd82d9cc3c991f8ffc90b92f12909d3c7a75bce88c6b98854ace5733be213",
        organization = "Praxis Dr. Mortuus est",
        expiresOn = now.plusDays(1),
        acceptUntil = now.minusDays(70),
        authoredOn = nowOffset,
        medicationText = "Vasopressin",
        rawKBVBundle = "{}".toByteArray()
    )
)

private val demoAuditEvents = listOf(
    AuditEventSimple(id = "egal", "egal", "Dr Mortuss hat das Rezept erstellt", LocalDateTime.now().minusDays(2), "egal"),
    AuditEventSimple(id = "egal", "egal", "Dr Mortuss hat das Rezept übermittelt", LocalDateTime.now().minusDays(1), "egal")
)

@Singleton
class PrescriptionDemoDataSource @Inject constructor() {
    private var _tasks = listOf<Task>()
    val tasks = MutableStateFlow<List<Task>>(listOf())

    private var timesRefreshed = 0

    fun incrementRefresh() {
        if (timesRefreshed == 0) {
            _tasks = demoTasks(LocalDate.now(), OffsetDateTime.now())
        }
        val firstTasks = _tasks.subList(0, 2)
        val secondTasks = _tasks.subList(2, 6)
        val thirdTasks = _tasks.subList(6, 8)
        val fourthTasks = _tasks.subList(8, _tasks.size)

        timesRefreshed += 1

        when (timesRefreshed) {
            1 -> {
                tasks.value += firstTasks
            }
            2 -> {
                tasks.value += secondTasks
            }
            3 -> {
                tasks.value += thirdTasks
            }
            4 -> {
                tasks.value += fourthTasks
            }
        }
    }

    fun saveTasks(tasks: List<Task>) {
        this.tasks.value += tasks
    }

    /**
     * Called by [DemoUseCase].
     */
    internal fun reset() {
        tasks.value = listOf()
        timesRefreshed = 0
    }

    fun deleteTaskByTaskId(taskId: String) {
        tasks.value -= tasks.value.filter { task ->
            task.taskId == taskId
        }
    }

    fun loadTaskForTaskId(taskId: String): Flow<Task> {
        return flowOf(tasks.value.filter { task -> task.taskId == taskId }[0])
    }

    fun loadTasksForScanSessionEnd(scanSessionEnd: OffsetDateTime): Flow<List<Task>> {
        return flowOf(tasks.value.filter { task -> task.scanSessionEnd == scanSessionEnd })
    }

    fun redeem(taskIds: List<String>, redeem: Boolean, all: Boolean) {
        // TODO: not implemented
    }

    fun unRedeemMorePossible(taskId: String): Boolean {
        return true
    }

    fun getAllTasksWithTaskIdOnly(): List<String> {
        return tasks.value.map { it.taskId }
    }

    fun loadAuditEvents(taskId: String): Flow<List<AuditEventSimple>> {
        return flowOf(demoAuditEvents)
    }

    fun editScannedPrescriptionsName(name: String, scanSessionEnd: OffsetDateTime) {
        // TODO: not implemented
    }
}
