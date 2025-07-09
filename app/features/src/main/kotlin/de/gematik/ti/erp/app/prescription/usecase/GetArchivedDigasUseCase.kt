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

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.diga.repository.DigaRepository
import de.gematik.ti.erp.app.prescription.mapper.toPrescription
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.SyncedTask
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * The prescription [repository] obtains the active
 * scanned and synced prescriptions and sorts them
 * by the authored or scanned date in a descending order and then
 * by name.
 *
 */
class GetArchivedDigasUseCase(
    private val repository: DigaRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(
        id: ProfileIdentifier
    ): Flow<List<Prescription>> =
        repository
            .loadArchiveDigasByProfileId(id)
            .map { it.map(SyncedTask::toPrescription) }
            .flowOn(dispatcher)
}
