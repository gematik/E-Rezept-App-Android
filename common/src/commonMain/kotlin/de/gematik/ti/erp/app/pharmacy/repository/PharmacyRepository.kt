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

@file:Suppress("NewLineAtEndOfFile")

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.fhir.common.model.erp.FhirInstitutionTelematikId
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.flow.Flow

interface PharmacyRepository {

    suspend fun searchPharmacies(filter: PharmacyFilter): Result<FhirPharmacyErpModelCollection>

    suspend fun searchPharmaciesByBundle(bundleId: String, offset: Int, count: Int): Result<FhirPharmacyErpModelCollection>

    suspend fun searchBinaryCerts(locationId: String): Result<List<String>>

    suspend fun redeemPrescriptionDirectly(url: String, message: ByteArray, pharmacyTelematikId: String, transactionId: String): Result<Unit>

    fun loadOftenUsedPharmacies(): Flow<List<OverviewPharmacyData.OverviewPharmacy>>

    fun loadFavoritePharmacies(): Flow<List<OverviewPharmacyData.OverviewPharmacy>>

    suspend fun markPharmacyAsOftenUsed(pharmacy: PharmacyUseCaseData.Pharmacy)

    suspend fun deleteOverviewPharmacy(overviewPharmacy: OverviewPharmacyData.OverviewPharmacy)

    suspend fun markPharmacyAsFavourite(pharmacy: PharmacyUseCaseData.Pharmacy)

    suspend fun deleteFavoritePharmacy(favoritePharmacy: PharmacyUseCaseData.Pharmacy)

    suspend fun searchInsuranceProviderByInstitutionIdentifier(iknr: String): Result<FhirInstitutionTelematikId?>

    suspend fun searchPharmacyByTelematikId(telematikId: String): Result<FhirPharmacyErpModelCollection>

    fun isPharmacyInFavorites(pharmacy: PharmacyUseCaseData.Pharmacy): Flow<Boolean>

    suspend fun markAsRedeemed(taskId: String)

    fun getSelectedVzdPharmacyBackend(): PharmacyVzdService

    suspend fun updateSelectedVzdPharmacyBackend(pharmacyVzdService: PharmacyVzdService)
}
