/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class PharmacyOverviewUseCase(
    private val repository: PharmacyRepository,
    private val dispatchers: DispatchProvider
) {
    fun oftenUsedPharmacies(): Flow<List<OverviewPharmacyData.OverviewPharmacy>> =
        repository.loadOftenUsedPharmacies().flowOn(dispatchers.io)

    fun favoritePharmacies(): Flow<List<OverviewPharmacyData.OverviewPharmacy>> =
        repository.loadFavoritePharmacies().flowOn(dispatchers.io)

    suspend fun saveOrUpdateUsedPharmacies(pharmacy: PharmacyUseCaseData.Pharmacy) {
        repository.markPharmacyAsOftenUsed(pharmacy)
    }

    suspend fun deleteOverviewPharmacy(overviewPharmacy: OverviewPharmacyData.OverviewPharmacy) {
        repository.deleteOverviewPharmacy(overviewPharmacy)
    }

    suspend fun searchPharmacyByTelematikId(
        telematikId: String
    ): Result<PharmacyUseCaseData.Pharmacy?> = withContext(dispatchers.io) {
        repository.searchPharmacyByTelematikId(telematikId)
            .map { it.pharmacies.toModel().firstOrNull() }
    }
}
