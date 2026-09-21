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

package de.gematik.ti.erp.app.messages.domain.usecase

import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuTaskEventErpModel
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.flow.firstOrNull

/**
 * Builds a single-pass map of taskId → pharmacyName for [EuEventType.TASK_REDEEMED] events only.
 *
 * Each distinct taskId is resolved exactly once via [TaskOperationsRepository.loadTaskByTaskId]. The resulting
 * map is intended to be passed to [de.gematik.ti.erp.app.messages.mapper.EuOrderToMessagesMapper]
 * so it can perform O(1) look-ups — no extra loops inside the mapper.
 *
 * - Non-TASK_REDEEMED events are ignored entirely.
 * - Empty or blank taskIds are skipped.
 * - Duplicate taskIds across events are deduplicated so [TaskOperationsRepository.loadTaskByTaskId] is called
 *   at most once per distinct taskId.
 * - If the task has no dispense with a pharmacy name the map entry defaults to an empty string.
 *
 * @receiver The list of [EuTaskEvent] events for a single thread. May be the full order event list
 *           or a filtered sub-list.
 * @param taskOperationsRepository Repository used to load the persisted [TaskErpModel].
 * @return Immutable map of `taskId` to pharmacy name (never null values; empty string when unknown).
 */
internal suspend fun List<EuTaskEventErpModel>.resolvePharmacyNames(
    taskOperationsRepository: TaskOperationsRepository
): Map<String, String> {
    val taskIds = this
        .filter { it.type == EuEventType.TASK_REDEEMED }
        .mapNotNull { it.taskId.takeIf { id -> id.isNotEmpty() } }
        .distinct()

    val result = mutableMapOf<String, String>()
    for (taskId in taskIds) {
        val task = taskOperationsRepository.loadTaskByTaskId(taskId).firstOrNull()
        val pharmacyName = when (task) {
            is TaskErpModel.Synced.Prescription -> task.medicationDispenses
            is TaskErpModel.Synced.Diga -> task.medicationDispenses
            else -> emptyList()
        }.firstNotNullOfOrNull { it.pharmacyName?.takeIf { name -> name.isNotEmpty() } }
            .orEmpty()
        result[taskId] = pharmacyName
    }
    return result
}

/**
 * Resolves medication names for all taskIds in the given list of events.
 *
 * @receiver The list of [EuTaskEvent] events for a single thread.
 * @param taskOperationsRepository Repository used to load the persisted [TaskErpModel].
 * @return Immutable map of `taskId` to medication name.
 */
internal suspend fun List<EuTaskEventErpModel>.resolveMedicationNames(
    taskOperationsRepository: TaskOperationsRepository
): Map<String, String> {
    val taskIds = this
        .mapNotNull { it.taskId.takeIf { id -> id.isNotEmpty() } }
        .distinct()

    val result = mutableMapOf<String, String>()
    for (taskId in taskIds) {
        val medicationName = when (val task = taskOperationsRepository.loadTaskByTaskId(taskId).firstOrNull()) {
            is TaskErpModel.Synced.Prescription -> task.medicationName() ?: taskId
            is TaskErpModel.Scanned -> task.name ?: taskId
            else -> taskId
        }
        result[taskId] = medicationName
    }
    return result
}
