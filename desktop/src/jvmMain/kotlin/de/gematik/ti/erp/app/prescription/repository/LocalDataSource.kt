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

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.prescription.repository.model.SimpleAuditEvent
import de.gematik.ti.erp.app.prescription.repository.model.SimpleMedicationDispense
import de.gematik.ti.erp.app.prescription.repository.model.SimpleTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LocalDataSource {
    private val auditEvents = MutableStateFlow(emptyList<SimpleAuditEvent>())
    private val medicationDispenses = MutableStateFlow(emptyList<SimpleMedicationDispense>())
    private val tasks = MutableStateFlow(emptyList<SimpleTask>())
    private val communications = MutableStateFlow(emptyList<SimpleCommunication>())
    private val lock = Mutex()

    suspend fun saveTask(task: SimpleTask) = lock.withLock {
        tasks.value = tasks.value.filter { it.taskId != task.taskId } + task
    }

    suspend fun deleteTask(taskId: String) = lock.withLock {
        tasks.value = tasks.value.filter { it.taskId != taskId }
    }

    suspend fun saveAuditEvents(auditEvents: List<SimpleAuditEvent>) = lock.withLock {
        val ids = auditEvents.map { it.id }
        this.auditEvents.value = this.auditEvents.value.filter { it.id !in ids } + auditEvents
    }

    suspend fun saveMedicationDispenses(medicationDispenses: List<SimpleMedicationDispense>) = lock.withLock {
        val ids = medicationDispenses.map { it.id }
        this.medicationDispenses.value = this.medicationDispenses.value.filter { it.id !in ids } + medicationDispenses
    }

    suspend fun saveCommunications(communications: List<SimpleCommunication>) = lock.withLock {
        val ids = communications.map { it.id }
        this.communications.value = this.communications.value.filter { it.id !in ids } + communications
    }

    fun loadAuditEvents(): Flow<List<SimpleAuditEvent>> {
        return auditEvents
    }

    fun loadTasks(): Flow<List<SimpleTask>> {
        return tasks
    }

    fun loadMedicationDispenses(): Flow<List<SimpleMedicationDispense>> {
        return medicationDispenses
    }

    fun loadCommunications(): Flow<List<SimpleCommunication>> {
        return communications
    }

    suspend fun invalidate() = lock.withLock {
        auditEvents.value = emptyList()
        medicationDispenses.value = emptyList()
        tasks.value = emptyList()
        communications.value = emptyList()
    }
}
