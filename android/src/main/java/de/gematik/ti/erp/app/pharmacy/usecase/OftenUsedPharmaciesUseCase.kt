/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.fhir.model.Pharmacy
import de.gematik.ti.erp.app.pharmacy.model.OftenUsedPharmacyData
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class OftenUsedPharmaciesUseCase(
    private val repository: PharmacyRepository,
    private val dispatchers: DispatchProvider
) {
    fun oftenUsedPharmacies(): Flow<List<OftenUsedPharmacyData.OftenUsedPharmacy>> {
        return repository.loadOftenUsedPharmacies().flowOn(dispatchers.IO)
    }

    suspend fun saveOrUpdateUsedPharmacies(pharmacy: PharmacyUseCaseData.Pharmacy) {
        repository.saveOrUpdateOftenUsedPharmacy(pharmacy)
    }

    suspend fun deleteOftenUsedPharmacy(oftenUsedPharmacy: OftenUsedPharmacyData.OftenUsedPharmacy) =
        repository.deleteOftenUsedPharmacy(oftenUsedPharmacy)

    suspend fun searchPharmacyByTelematikId(
        telematikId: String
    ): Result<List<PharmacyUseCaseData.Pharmacy>> = withContext(dispatchers.IO) {
        repository.searchPharmacyByTelematikId(telematikId)
            .map { mapPharmacies(it.pharmacies) }
    }
}
