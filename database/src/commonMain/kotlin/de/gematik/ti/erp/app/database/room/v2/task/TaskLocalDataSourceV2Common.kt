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

package de.gematik.ti.erp.app.database.room.v2.task

import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceCommon
import de.gematik.ti.erp.app.database.room.v2.invoice.InvoiceDao
import de.gematik.ti.erp.app.database.room.v2.task.communication.CommunicationDao
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toErpModel
import de.gematik.ti.erp.app.database.room.v2.task.medication.MedicationDispenseDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefsDao
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class TaskLocalDataSourceV2Common(
    private val taskDao: ErpTaskDao,
    private val taskWithRefsDao: ErpTaskWithRefsDao,
    private val communicationDao: CommunicationDao,
    private val medicationDispenseDao: MedicationDispenseDao,
    private val invoiceDao: InvoiceDao
) : TaskLocalDataSourceCommon {

    override fun loadTaskListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel>> =
        taskWithRefsDao.observeAllWithRefsByProfile(profileId).map { list ->
            list.mapNotNull { runCatching { it.toErpModel() }.getOrNull() }
        }

    override fun loadTaskListByTaskIdList(taskIds: List<String>): Flow<List<TaskErpModel>> {
        if (taskIds.isEmpty()) return flowOf(emptyList())
        return taskWithRefsDao.observeAllWithRefsByTaskIds(taskIds).map { list ->
            list.mapNotNull { runCatching { it.toErpModel() }.getOrNull() }
        }
    }

    override fun loadTaskByTaskId(taskId: String): Flow<TaskErpModel?> =
        taskWithRefsDao.observeWithRefs(taskId).map { it?.let { runCatching { it.toErpModel() }.getOrNull() } }

    override suspend fun deleteTaskByTaskId(taskId: String) {
        taskDao.deleteByTaskId(taskId)
    }

    override fun loadATaskIdStringList(): Flow<List<String>> = taskDao.observeAllIds()

    override fun loadTaskIdStringListByProfileId(profileId: ProfileIdentifier): Flow<List<String>> =
        taskDao.observeIdsByProfile(profileId)

    override suspend fun saveCommunications(communicationModel: FhirCommunicationBundleErpModel): Int {
        val entitiesToSave = communicationModel.messages.mapNotNull { message ->
            val taskId = message.taskId ?: return@mapNotNull null
            val task = taskDao.getByTaskId(taskId) ?: return@mapNotNull null
            val profileId = task.parentProfileId ?: return@mapNotNull null
            when (message) {
                is FhirReplyCommunicationEntryErpModel -> message.toErpCommunicationEntity(task, profileId)
                is FhirDispenseCommunicationEntryErpModel -> message.toErpCommunicationEntity(task, profileId)
            }
        }
        communicationDao.upsertAll(entitiesToSave)
        return entitiesToSave.size
    }

    override suspend fun deleteTasksByProfileId(profileId: ProfileIdentifier) {
        taskDao.deleteByProfileId(profileId)
    }

    override suspend fun deleteCommunicationsByProfileId(profileId: ProfileIdentifier) {
        communicationDao.deleteByProfileId(profileId)
    }

    override suspend fun deleteInvoicesByProfileId(profileId: ProfileIdentifier) {
        invoiceDao.deleteByProfileId(profileId)
    }
}
