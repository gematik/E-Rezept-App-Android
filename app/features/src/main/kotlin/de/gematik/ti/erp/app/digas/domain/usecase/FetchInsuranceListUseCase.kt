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

import de.gematik.ti.erp.app.digas.ui.model.InsuranceUiModel
import de.gematik.ti.erp.app.digas.util.InsuranceDrawableUtil.getDrawableResourceId
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.SearchData.Companion.toPharmacyFilter
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FetchInsuranceListUseCase(
    private val repository: PharmacyRepository,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(term: String = ""): Flow<UiState<List<InsuranceUiModel>>> {
        val defaultSearchData = PharmacyUseCaseData.SearchData(
            name = term,
            filter = PharmacyUseCaseData.Filter(),
            locationMode = PharmacyUseCaseData.LocationMode.Disabled
        )
        return flow {
            repository.searchInsurances(defaultSearchData.toPharmacyFilter())
                .fold(
                    onSuccess = { result ->
                        if (result.entries.isEmpty()) {
                            emit(UiState.Empty())
                        }
                        val sortedInsuranceList =
                            result.entries.toModel(defaultSearchData.locationMode, result.type).sortedBy { it.name }.distinctBy { it.telematikId }
                        emit(
                            UiState.Data(
                                sortedInsuranceList.map { insurance ->
                                    InsuranceUiModel(
                                        pharmacy = insurance,
                                        drawableResourceId = insurance.getDrawableResourceId()
                                    )
                                }
                            )
                        )
                    },
                    onFailure = { exception ->
                        Napier.e("Failed to fetch insurance list: ${exception.message}", exception)
                        emit(UiState.Error(exception))
                    }
                )
        }
            .flowOn(dispatchers)
    }
}
