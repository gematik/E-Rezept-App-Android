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

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.fhir.common.model.erp.FhirInstitutionTelematikId
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.model.extractBinaryCertificatesAsBase64
import de.gematik.ti.erp.app.fhir.model.extractPharmacyServices
import de.gematik.ti.erp.app.fhir.pharmacy.parser.PharmacyParsers
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService.APOVZD
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService.FHIRVZD
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.FavouritePharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.OftenUsedPharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.PharmacyRemoteSelectorLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.ApoVzdRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.FhirVzdRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.PharmacyRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.redeem.repository.datasource.RedeemLocalDataSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

class DefaultPharmacyRepository(
    private val remoteDataSourceSelector: PharmacyRemoteSelectorLocalDataSource,
    private val apoVzdRemoteDataSource: ApoVzdRemoteDataSource,
    private val fhirVzdRemoteDataSource: FhirVzdRemoteDataSource,
    private val searchAccessTokenLocalDataSource: PharmacySearchAccessTokenLocalDataSource,
    private val redeemLocalDataSource: RedeemLocalDataSource,
    private val favouriteLocalDataSource: FavouritePharmacyLocalDataSource,
    private val oftenUsedLocalDataSource: OftenUsedPharmacyLocalDataSource,
    private val parsers: PharmacyParsers
) : PharmacyRepository {

    companion object {
        private const val MAX_LOCATION_RESULT_COUNT = 100
        private const val MIN_ENTRIES_RETURN_THRESHOLD = 51
        private const val MAX_ENTRIES_RETURN_THRESHOLD = 99
    }

    private val vzdServiceSelection: PharmacyVzdService = getSelectedVzdPharmacyBackend()

    // repository decides which remote data source to use
    private val remoteDataSource: PharmacyRemoteDataSource by lazy {
        try {
            when (vzdServiceSelection) {
                APOVZD -> apoVzdRemoteDataSource
                FHIRVZD -> fhirVzdRemoteDataSource
            }
        } catch (e: Exception) {
            Napier.e(e) { "error on getting remote data source selection" }
            apoVzdRemoteDataSource
        }
    }

    private fun apoVzdExtractor(jsonElement: JsonElement): FhirPharmacyErpModelCollection = extractPharmacyServices(
        bundle = jsonElement,
        onError = { it, cause ->
            Napier.e(cause) {
                it.toString()
            }
        }
    )

    private fun parseData(
        jsonElement: JsonElement
    ): FhirPharmacyErpModelCollection =
        when (vzdServiceSelection) {
            APOVZD -> apoVzdExtractor(jsonElement)
            FHIRVZD -> parsers.bundleParser.extract(jsonElement)
        }

    /**
     * Performs a location-based pharmacy search with progressively increasing radius.
     *
     * - Starts with a **small radius (2km )** to optimize performance.
     * - If results are insufficient, expands to **50km**.
     * - Search results are increased step-by-step until 51 - 99 pharmacies are found`.
     *
     * @param filter The search filter containing location details.
     * @param onUnauthorizedException A suspend function handling unauthorized API responses.
     * @return A [Result] containing a [FhirPharmacyErpModelCollection] with pharmacies, or an error.
     */
    private suspend fun progressiveRadiusSearch(
        filter: PharmacyFilter,
        onUnauthorizedException: suspend () -> Unit
    ): Result<FhirPharmacyErpModelCollection> = runCatching {
        @Suppress("MagicNumber")
        val kmRadiusLevels = listOf(2.0, 3.0, 5.0, 10.0, 20.0, 50.0) // Progressive radius search
        var lastCollection: FhirPharmacyErpModelCollection? = null

        for (radius in kmRadiusLevels) {
            val updatedFilter = filter.copy(locationFilter = filter.locationFilter?.copy(radius = radius))
            val collection = fetchPharmacies(updatedFilter, onUnauthorizedException)
            when {
                collection.entries.size in MIN_ENTRIES_RETURN_THRESHOLD..MAX_ENTRIES_RETURN_THRESHOLD -> {
                    return@runCatching collection.copy(total = collection.entries.size)
                }
                collection.entries.size >= MAX_LOCATION_RESULT_COUNT -> {
                    return@runCatching lastCollection ?: collection.copy(total = collection.entries.size)
                }
                else -> {
                    lastCollection = collection.copy(total = collection.entries.size)
                }
            }
        }
        return@runCatching lastCollection ?: FhirPharmacyErpModelCollection.emptyCollection()
    }

    private suspend fun fetchPharmacies(
        filter: PharmacyFilter,
        onUnauthorizedException: suspend () -> Unit
    ): FhirPharmacyErpModelCollection {
        return remoteDataSource.searchPharmacies(filter, onUnauthorizedException)
            .map(::parseData)
            .getOrElse { FhirPharmacyErpModelCollection.emptyCollection() }
    }

    override fun getSelectedVzdPharmacyBackend(): PharmacyVzdService =
        remoteDataSourceSelector.getPharmacyVzdService()

    override suspend fun updateSelectedVzdPharmacyBackend(pharmacyVzdService: PharmacyVzdService) {
        remoteDataSourceSelector.updatePharmacyService(pharmacyVzdService)
    }

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
        val onUnauthorized = searchAccessTokenLocalDataSource::clearToken

        return if (filter.locationFilter == null || vzdServiceSelection == APOVZD) {
            remoteDataSource.searchPharmacies(
                filter = filter,
                onUnauthorizedException = onUnauthorized
            ).map(::parseData)
        } else {
            progressiveRadiusSearch(
                filter = filter,
                onUnauthorizedException = onUnauthorized
            )
        }
    }

    // will only work with APOVZD
    override suspend fun searchPharmaciesByBundle(
        bundleId: String,
        offset: Int,
        count: Int
    ): Result<FhirPharmacyErpModelCollection> = remoteDataSource.searchPharmaciesContinued(
        bundleId = bundleId,
        offset = offset,
        count = count
    ).map(::parseData)

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
        return remoteDataSource.searchPharmacyByTelematikId(
            telematikId = telematikId,
            onUnauthorizedException = searchAccessTokenLocalDataSource::clearToken
        ).map(::parseData)
    }

    override suspend fun searchInsuranceProviderByInstitutionIdentifier(
        iknr: String
    ): Result<FhirInstitutionTelematikId?> {
        return remoteDataSource.searchByInsuranceProvider(
            institutionIdentifier = iknr,
            onUnauthorizedException = searchAccessTokenLocalDataSource::clearToken
        ).map(parsers.organizationParser::extract)
    }

    // will only work with APOVZD
    override suspend fun searchBinaryCerts(
        locationId: String
    ): Result<List<String>> = remoteDataSource.searchBinaryCert(locationId = locationId)
        .map {
            extractBinaryCertificatesAsBase64(bundle = it)
        }

    // will only work with APOVZD
    override suspend fun redeemPrescriptionDirectly(
        url: String,
        message: ByteArray,
        pharmacyTelematikId: String,
        transactionId: String
    ): Result<Unit> = remoteDataSource.redeemPrescriptionDirectly(
        url = url,
        message = message,
        pharmacyTelematikId = pharmacyTelematikId,
        transactionId = transactionId
    )

    override fun loadOftenUsedPharmacies() = oftenUsedLocalDataSource.loadOftenUsedPharmacies()

    override fun loadFavoritePharmacies() = favouriteLocalDataSource.loadFavoritePharmacies()

    override suspend fun markPharmacyAsOftenUsed(pharmacy: PharmacyUseCaseData.Pharmacy) {
        oftenUsedLocalDataSource.markPharmacyAsOftenUsed(pharmacy)
    }

    override suspend fun deleteOverviewPharmacy(overviewPharmacy: OverviewPharmacyData.OverviewPharmacy) {
        oftenUsedLocalDataSource.deleteOverviewPharmacy(overviewPharmacy)
    }

    override suspend fun markPharmacyAsFavourite(pharmacy: PharmacyUseCaseData.Pharmacy) {
        favouriteLocalDataSource.markPharmacyAsFavourite(pharmacy)
    }

    override suspend fun deleteFavoritePharmacy(favoritePharmacy: PharmacyUseCaseData.Pharmacy) {
        favouriteLocalDataSource.deleteFavoritePharmacy(favoritePharmacy)
    }

    override fun isPharmacyInFavorites(pharmacy: PharmacyUseCaseData.Pharmacy): Flow<Boolean> =
        favouriteLocalDataSource.isPharmacyInFavorites(pharmacy)

    override suspend fun markAsRedeemed(taskId: String) {
        redeemLocalDataSource.markAsRedeemed(taskId)
    }
}
