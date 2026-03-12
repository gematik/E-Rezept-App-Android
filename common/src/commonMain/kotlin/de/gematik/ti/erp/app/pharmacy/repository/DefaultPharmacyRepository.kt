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

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.database.api.PharmacyLocalDataSource
import de.gematik.ti.erp.app.database.api.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.fhir.FhirInsuranceProvider
import de.gematik.ti.erp.app.fhir.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.pharmacy.parser.PharmacyParsers
import de.gematik.ti.erp.app.messages.repository.CachedPharmacy
import de.gematik.ti.erp.app.messages.repository.PharmacyCacheLocalDataSource
import de.gematik.ti.erp.app.pharmacy.model.PharmacyErpModel
import de.gematik.ti.erp.app.pharmacy.model.TelematikId
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.PharmacyRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy.Companion.toErpModel
import de.gematik.ti.erp.app.redeem.repository.datasource.RedeemLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

class DefaultPharmacyRepository(
    private val pharmacyLocalDataSource: PharmacyLocalDataSource,
    private val pharmacyRemoteDataSource: PharmacyRemoteDataSource,
    private val pharmacySearchAccessTokenLocalDataSource: PharmacySearchAccessTokenLocalDataSource,
    private val redeemLocalDataSource: RedeemLocalDataSource,
    private val cachedPharmacyLocalDataSource: PharmacyCacheLocalDataSource, // todo: check if we need this one
    private val parsers: PharmacyParsers
) : PharmacyRepository {

    private fun parseData(jsonElement: JsonElement): FhirPharmacyErpModelCollection = parsers.bundleParser.extract(jsonElement)

    /**
     * Searches for pharmacies based on the provided filter.
     *
     * - If `locationFilter` is `null` and `vzdServiceSelection` is `APOVZD`, the function directly queries the remote data source.
     * - Otherwise, it performs a **location-based search** using `locationModeSearch`, which applies a smaller radius first and expands if needed.
     *
     * @param filter The criteria used to search for pharmacies, including location and service selection.
     * @return A [Result] containing a [FhirPharmacyErpModelCollection] with the list of pharmacies or an error.
     */
    override suspend fun searchPharmacies(
        filter: PharmacyFilter
    ): Result<FhirPharmacyErpModelCollection> {
        val onUnauthorized = pharmacySearchAccessTokenLocalDataSource::clearToken

        return pharmacyRemoteDataSource.searchPharmacies(
            filter = filter,
            onUnauthorizedException = onUnauthorized
        ).map(::parseData)
    }

    override suspend fun searchInsurances(
        filter: PharmacyFilter
    ): Result<FhirPharmacyErpModelCollection> {
        val onUnauthorized = pharmacySearchAccessTokenLocalDataSource::clearToken

        return pharmacyRemoteDataSource.searchInsurances(
            filter = filter,
            onUnauthorizedException = onUnauthorized
        ).mapCatching(::parseData)
    }

    /**
     * Searches for a pharmacy by its unique `telematikId`.
     *
     * - Fetches the pharmacy details from the remote data source.
     * - Ensures that only one entry exists in the returned [FhirPharmacyErpModelCollection].
     * - If the request is unauthorized, it clears the stored access token.
     * - Parses the received JSON response into a structured model.
     *
     * @param telematikId The unique identifier of the pharmacy.
     * @return A [Result] containing a [FhirPharmacyErpModelCollection] with exactly **one** pharmacy entry,
     *         or an error if the request fails.
     */
    override suspend fun searchPharmacyByTelematikId(
        telematikId: String
    ): Result<FhirPharmacyErpModelCollection> {
        return pharmacyRemoteDataSource.searchPharmacyByTelematikId(
            telematikId = telematikId,
            onUnauthorizedException = pharmacySearchAccessTokenLocalDataSource::clearToken
        ).map(::parseData)
    }

    override suspend fun searchInsuranceProviderByInstitutionIdentifier(
        iknr: String
    ): Result<FhirInsuranceProvider?> {
        return pharmacyRemoteDataSource.searchByInsuranceProvider(
            institutionIdentifier = iknr,
            onUnauthorizedException = pharmacySearchAccessTokenLocalDataSource::clearToken
        ).map(parsers.organizationParser::extract)
    }

    override suspend fun savePharmacyToCache(cachedPharmacy: CachedPharmacy) {
        cachedPharmacyLocalDataSource.savePharmacy(
            telematikId = cachedPharmacy.telematikId,
            name = cachedPharmacy.name
        )
    }

    override fun loadCachedPharmacies(): Flow<List<CachedPharmacy>> =
        cachedPharmacyLocalDataSource.loadPharmacies()

    override fun loadPharmacies(): Flow<List<PharmacyErpModel>> = pharmacyLocalDataSource.loadPharmacies()

    override suspend fun markPharmacyAsOftenUsed(pharmacy: PharmacyUseCaseData.Pharmacy) {
        pharmacyLocalDataSource.markPharmacyAsOftenUsed(pharmacy.toErpModel())
    }

    override suspend fun deleteOverviewPharmacy(overviewPharmacy: PharmacyErpModel) {
        // User intends to remove OftenUsed flag; demark or delete depending on current Favorite flag
        pharmacyLocalDataSource.deleteOftenUsedPharmacy(TelematikId(overviewPharmacy.telematikId))
    }

    override suspend fun markPharmacyAsFavourite(pharmacy: PharmacyUseCaseData.Pharmacy) {
        pharmacyLocalDataSource.markPharmacyAsFavourite(pharmacy.toErpModel())
    }

    override suspend fun deleteFavoritePharmacy(favoritePharmacy: PharmacyUseCaseData.Pharmacy) {
        // User intends to remove Favorite flag; demark or delete depending on current OftenUsed flag
        pharmacyLocalDataSource.deleteFavoritePharmacy(TelematikId(favoritePharmacy.telematikId))
    }

    override fun isPharmacyInFavorites(pharmacy: PharmacyUseCaseData.Pharmacy): Flow<Boolean> =
        pharmacyLocalDataSource.isPharmacyInFavorites(pharmacy.toErpModel())

    override suspend fun markAsRedeemed(taskId: String) {
        redeemLocalDataSource.markAsRedeemed(taskId)
    }
}
