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

package de.gematik.ti.erp.app.database.api.task

import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface TaskLocalDataSourceScanned {
    /**
     * Mark the provided scanned tasks as redeemed using the current timestamp.
     */
    suspend fun redeemScannedTaskListByTaskIdList(taskIds: List<String>)

    suspend fun updateScannedTaskRedeemedOn(taskId: String, timestamp: Instant?)

    /**
     * Persist newly scanned tasks for the given profile.
     *
     * Implementations should avoid inserting duplicates and may generate a human-readable name
     * using `medicationString` when the scanned task does not provide one.
     */
    suspend fun saveScannedTaskList(
        profileId: ProfileIdentifier,
        tasks: List<TaskErpModel.Scanned>,
        medicationString: String
    )

    /**
     * Update the display name of a scanned task.
     */
    suspend fun updateScannedTaskName(taskId: String, name: String)

    /**
     * Observe scanned tasks belonging to the given profile.
     */
    @Deprecated("Use loadByProfileId and then filterByInstance")
    fun loadScannedPrescriptionListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Scanned>>

    /**
     * Observe scanned tasks for the provided task identifiers.
     */
    fun loadScannedTaskListByTaskIdList(taskIds: List<String>): Flow<List<TaskErpModel.Scanned>>

    /**
     * Observe a scanned task by its identifier. Emits `null` when it does not exist.
     */
    fun loadScannedPrescriptionByTaskId(taskId: String): Flow<TaskErpModel.Scanned?>

    suspend fun saveCommunicationForScannedTask(taskId: String, pharmacyId: String, transactionId: String)
}
