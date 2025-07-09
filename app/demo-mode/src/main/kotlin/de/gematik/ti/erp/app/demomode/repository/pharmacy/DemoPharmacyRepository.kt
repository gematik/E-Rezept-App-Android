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

package de.gematik.ti.erp.app.demomode.repository.pharmacy

import android.content.res.AssetManager
import de.gematik.ti.erp.app.demomode.util.pharmacyFhirBundle
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirInsuranceProvider
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.pharmacy.parser.PharmacyBundleParser
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.messages.repository.CachedPharmacy
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.redeem.repository.datasource.RedeemLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DemoPharmacyRepository(
    private val parser: PharmacyBundleParser,
    private val redeemLocalDataSource: RedeemLocalDataSource,
    private val assetManager: AssetManager
) : PharmacyRepository {
    override suspend fun searchInsurances(filter: PharmacyFilter): Result<FhirPharmacyErpModelCollection> {
        return Result.success(parser.extract(pharmacyFhirBundle(assetManager)))
    }

    override suspend fun searchPharmacies(filter: PharmacyFilter): Result<FhirPharmacyErpModelCollection> {
        return Result.success(parser.extract(pharmacyFhirBundle(assetManager)))
    }

    override suspend fun searchPharmaciesByBundle(bundleId: String, offset: Int, count: Int): Result<FhirPharmacyErpModelCollection> {
        return Result.success(parser.extract(pharmacyFhirBundle(assetManager)))
    }

    override suspend fun searchBinaryCerts(locationId: String): Result<List<String>> {
        return Result.success(listOf(""))
    }

    override suspend fun redeemPrescriptionDirectly(url: String, message: ByteArray, pharmacyTelematikId: String, transactionId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun loadOftenUsedPharmacies(): Flow<List<OverviewPharmacyData.OverviewPharmacy>> {
        return flowOf(emptyList())
    }

    override fun loadFavoritePharmacies(): Flow<List<OverviewPharmacyData.OverviewPharmacy>> {
        return flowOf(emptyList())
    }

    override suspend fun markPharmacyAsOftenUsed(pharmacy: PharmacyUseCaseData.Pharmacy) {
        // do nothing
    }

    override suspend fun deleteOverviewPharmacy(overviewPharmacy: OverviewPharmacyData.OverviewPharmacy) {
        // do nothing
    }

    override suspend fun markPharmacyAsFavourite(pharmacy: PharmacyUseCaseData.Pharmacy) {
        // do nothing
    }

    override suspend fun deleteFavoritePharmacy(favoritePharmacy: PharmacyUseCaseData.Pharmacy) {
        // do nothing
    }

    override suspend fun searchInsuranceProviderByInstitutionIdentifier(iknr: String): Result<FhirInsuranceProvider?> {
        return Result.success(FhirInsuranceProvider("123456", "TK"))
    }

    override suspend fun searchPharmacyByTelematikId(telematikId: String): Result<FhirPharmacyErpModelCollection> {
        return parser.extract(pharmacyFhirBundle(assetManager))
            .entries
            .find { it.telematikId == telematikId }
            ?.let { pharmacy ->
                Result.success(
                    FhirPharmacyErpModelCollection(
                        type = PharmacyVzdService.FHIRVZD,
                        entries = listOf(pharmacy),
                        id = pharmacy.id,
                        total = 1
                    )
                )
            } ?: Result.failure(Exception("Not found"))
    }

    override fun isPharmacyInFavorites(pharmacy: PharmacyUseCaseData.Pharmacy): Flow<Boolean> {
        return flowOf(false)
    }

    override suspend fun markAsRedeemed(taskId: String) {
        redeemLocalDataSource.markAsRedeemed(taskId)
    }

    override fun getSelectedVzdPharmacyBackend(): PharmacyVzdService {
        return PharmacyVzdService.FHIRVZD
    }

    override suspend fun updateSelectedVzdPharmacyBackend(pharmacyVzdService: PharmacyVzdService) {
        // do nothing
    }

    override fun loadCachedPharmacies(): Flow<List<CachedPharmacy>> {
        return flowOf(emptyList())
    }

    override suspend fun savePharmacyToCache(cachedPharmacy: CachedPharmacy) {
        // do nothing
    }
}
