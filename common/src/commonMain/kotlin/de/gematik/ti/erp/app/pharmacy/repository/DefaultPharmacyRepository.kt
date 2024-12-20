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

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.fhir.model.PharmacyServices
import de.gematik.ti.erp.app.fhir.model.extractBinaryCertificatesAsBase64
import de.gematik.ti.erp.app.fhir.model.extractPharmacyServices
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.repository.datasource.FavouritePharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.OftenUsedPharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.PharmacyRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.redeem.repository.datasource.RedeemLocalDataSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow

class DefaultPharmacyRepository(
    private val remoteDataSource: PharmacyRemoteDataSource,
    private val redeemLocalDataSource: RedeemLocalDataSource,
    private val favouriteLocalDataSource: FavouritePharmacyLocalDataSource,
    private val oftenUsedLocalDataSource: OftenUsedPharmacyLocalDataSource
) : PharmacyRepository {
    override suspend fun searchPharmacies(
        names: List<String>,
        filter: Map<String, String>
    ): Result<PharmacyServices> = remoteDataSource.searchPharmacies(names, filter)
        .map { jsonElement ->
            extractPharmacyServices(
                bundle = jsonElement,
                onError = { it, cause ->
                    Napier.e(cause) {
                        it.toString()
                    }
                }
            )
        }

    override suspend fun searchPharmaciesByBundle(
        bundleId: String,
        offset: Int,
        count: Int
    ): Result<PharmacyServices> = remoteDataSource.searchPharmaciesContinued(
        bundleId = bundleId,
        offset = offset,
        count = count
    ).map {
        extractPharmacyServices(
            bundle = it,
            onError = { element, cause ->
                Napier.e(cause) {
                    element.toString()
                }
            }
        )
    }

    override suspend fun searchBinaryCerts(
        locationId: String
    ): Result<List<String>> = remoteDataSource.searchBinaryCert(locationId = locationId)
        .map {
            extractBinaryCertificatesAsBase64(bundle = it)
        }

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

    override suspend fun searchPharmacyByTelematikId(
        telematikId: String
    ): Result<PharmacyServices> = remoteDataSource.searchPharmacyByTelematikId(telematikId)
        .map {
            extractPharmacyServices(
                bundle = it,
                onError = { element, cause ->
                    Napier.e(element.toString(), cause)
                }
            )
        }

    override fun isPharmacyInFavorites(pharmacy: PharmacyUseCaseData.Pharmacy): Flow<Boolean> =
        favouriteLocalDataSource.isPharmacyInFavorites(pharmacy)

    override suspend fun markAsRedeemed(taskId: String) {
        redeemLocalDataSource.markAsRedeemed(taskId)
    }
}
