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

import de.gematik.ti.erp.app.digas.data.repository.DigaInformationRepository
import de.gematik.ti.erp.app.digas.mapper.toDigaBfarmUiModel
import de.gematik.ti.erp.app.digas.ui.model.DigaBfarmUiModel
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// TODO: UiState needs to be extracted to controller
class FetchDigaByPznUseCase(
    private val repository: DigaInformationRepository,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) {

    suspend operator fun invoke(pzn: String?): UiState<DigaBfarmUiModel> {
        if (pzn.isNullOrEmpty()) {
            Napier.e { "Cannot fetch DiGA data: PZN is null" }
            return UiState.Empty()
        }

        return withContext(dispatchers) {
            repository.fetchDigaByPzn(pzn).fold(
                onSuccess = {
                    return@withContext UiState.Data(it.toDigaBfarmUiModel())
                },
                onFailure = {
                    Napier.e { "Error fetching DiGA data for PZN: $pzn. Error: ${it.javaClass.simpleName} - ${it.message.orEmpty()}" }
                    return@withContext UiState.Empty()
                }
            )
        }
    }
}
