/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge

class GetPrescriptionByTaskIdUseCase(
    private val repository: PrescriptionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // TODO für DigaDitail
    operator fun invoke(taskId: String): Flow<PrescriptionData.Prescription> {
        val synced =
            repository
                .loadSyncedTaskByTaskId(taskId)
                .mapNotNull { it }
                .map(PrescriptionData::Synced)
                .flowOn(dispatcher)

        val scanned =
            repository
                .loadScannedTaskByTaskId(taskId)
                .mapNotNull { it }
                .map(PrescriptionData::Scanned)
                .flowOn(dispatcher)

        // We functionally know that
        return merge(synced, scanned).flowOn(dispatcher)
    }
}
