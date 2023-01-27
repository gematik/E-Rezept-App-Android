/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.flow.Flow
import org.kodein.di.compose.rememberInstance

@Stable
class PharmacyController(
    private val pharmacyRepository: PharmacyRepository
) {
    fun isPharmacyInFavorites(pharmacy: PharmacyUseCaseData.Pharmacy): Flow<Boolean> =
        pharmacyRepository.isPharmacyInFavorites(pharmacy)

    suspend fun markPharmacyAsFavorite(pharmacy: PharmacyUseCaseData.Pharmacy) {
        pharmacyRepository.saveOrUpdateFavoritePharmacy(pharmacy)
    }

    suspend fun unmarkPharmacyAsFavorite(pharmacy: PharmacyUseCaseData.Pharmacy) {
        pharmacyRepository.deleteFavoritePharmacy(pharmacy)
    }
}

@Composable
fun rememberPharmacyController(): PharmacyController {
    val pharmacyRepository by rememberInstance<PharmacyRepository>()
    return remember {
        PharmacyController(pharmacyRepository)
    }
}
