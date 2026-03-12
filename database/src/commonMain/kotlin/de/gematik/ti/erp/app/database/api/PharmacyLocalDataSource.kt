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
package de.gematik.ti.erp.app.database.api

import de.gematik.ti.erp.app.pharmacy.model.PharmacyErpModel
import de.gematik.ti.erp.app.pharmacy.model.TelematikId
import kotlinx.coroutines.flow.Flow

interface PharmacyLocalDataSource {
    fun loadPharmacies(): Flow<List<PharmacyErpModel>>
    fun getPharmacy(telematikId: TelematikId): Flow<PharmacyErpModel?>

    // Generic delete (legacy) – removes both flags/records
    suspend fun deletePharmacy(telematikId: TelematikId)

    // Targeted deletions following demark-or-delete rules
    suspend fun deleteFavoritePharmacy(telematikId: TelematikId)
    suspend fun deleteOftenUsedPharmacy(telematikId: TelematikId)

    suspend fun markPharmacyAsFavourite(pharmacy: PharmacyErpModel)
    suspend fun markPharmacyAsOftenUsed(pharmacy: PharmacyErpModel)
    fun isPharmacyInFavorites(pharmacy: PharmacyErpModel): Flow<Boolean>
    fun isPharmacyOftenUsed(pharmacy: PharmacyErpModel): Flow<Boolean>
}
