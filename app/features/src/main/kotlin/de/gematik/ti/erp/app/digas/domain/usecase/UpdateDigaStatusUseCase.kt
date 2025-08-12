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

package de.gematik.ti.erp.app.digas.domain.usecase

import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.diga.repository.DigaRepository
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class UpdateDigaStatusUseCase(
    private val digaRepository: DigaRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(taskId: String, status: DigaStatus) {
        val syncedTask = digaRepository.loadDigaByTaskId(taskId).firstOrNull()

        // user is allowed to update only when task is completed or the user has send the comm request (InProgress)
        val canUpdate = when {
            syncedTask?.status == SyncedTaskData.TaskStatus.Completed -> true
            status is DigaStatus.InProgress -> true
            else -> false
        }

        withContext(dispatcher) {
            if (canUpdate) {
                digaRepository.updateDigaStatus(
                    taskId = taskId,
                    status = status,
                    lastModified = Clock.System.now()
                )
            } else {
                Napier.e { "Conditions not met to update the diga-status" }
            }
        }
    }
}
