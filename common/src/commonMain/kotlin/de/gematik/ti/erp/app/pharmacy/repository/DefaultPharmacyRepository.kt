/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.fhir.model.PharmacyServices
import de.gematik.ti.erp.app.fhir.model.extractBinaryCertificatesAsBase64
import de.gematik.ti.erp.app.fhir.model.extractPharmacyServices
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DefaultPharmacyRepository(
    private val remoteDataSource: PharmacyRemoteDataSource,
    private val localDataSource: PharmacyLocalDataSource,
    private val dispatchers: DispatchProvider
) : PharmacyRepository {
    override suspend fun searchPharmacies(
        names: List<String>,
        filter: Map<String, String>
    ): Result<PharmacyServices> =
        remoteDataSource.searchPharmacies(names, filter)
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
    ): Result<PharmacyServices> =
        withContext(dispatchers.IO) {
            remoteDataSource.searchPharmaciesContinued(
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
        }

    override suspend fun searchBinaryCerts(
        locationId: String
    ): Result<List<String>> =
        withContext(dispatchers.IO) {
            remoteDataSource.searchBinaryCert(
                locationId = locationId
            ).map {
                extractBinaryCertificatesAsBase64(
                    bundle = it
                )
            }
        }

    override suspend fun redeemPrescriptionDirectly(
        url: String,
        message: ByteArray,
        pharmacyTelematikId: String,
        transactionId: String
    ): Result<Unit> =
        remoteDataSource.redeemPrescriptionDirectly(
            url = url,
            message = message,
            pharmacyTelematikId = pharmacyTelematikId,
            transactionId = transactionId
        )

    override fun loadOftenUsedPharmacies() =
        localDataSource.loadOftenUsedPharmacies().flowOn(dispatchers.IO)

    override fun loadFavoritePharmacies() =
        localDataSource.loadFavoritePharmacies().flowOn(dispatchers.IO)

    override suspend fun saveOrUpdateOftenUsedPharmacy(pharmacy: PharmacyUseCaseData.Pharmacy) {
        withContext(dispatchers.IO) {
            localDataSource.saveOrUpdateOftenUsedPharmacy(pharmacy)
        }
    }

    override suspend fun deleteOverviewPharmacy(overviewPharmacy: OverviewPharmacyData.OverviewPharmacy) {
        withContext(dispatchers.IO) {
            localDataSource.deleteOverviewPharmacy(overviewPharmacy)
        }
    }

    override suspend fun saveOrUpdateFavoritePharmacy(pharmacy: PharmacyUseCaseData.Pharmacy) {
        withContext(dispatchers.IO) {
            localDataSource.saveOrUpdateFavoritePharmacy(pharmacy)
        }
    }

    override suspend fun deleteFavoritePharmacy(favoritePharmacy: PharmacyUseCaseData.Pharmacy) {
        withContext(dispatchers.IO) {
            localDataSource.deleteFavoritePharmacy(favoritePharmacy)
        }
    }

    override suspend fun searchPharmacyByTelematikId(
        telematikId: String
    ): Result<PharmacyServices> =
        withContext(dispatchers.IO) {
            remoteDataSource.searchPharmacyByTelematikId(telematikId)
                .map {
                    extractPharmacyServices(
                        bundle = it,
                        onError = { element, cause ->
                            Napier.e(element.toString(), cause)
                        }
                    )
                }
        }

    override fun isPharmacyInFavorites(pharmacy: PharmacyUseCaseData.Pharmacy): Flow<Boolean> =
        localDataSource.isPharmacyInFavorites(pharmacy).flowOn(dispatchers.IO)

    override suspend fun markAsRedeemed(taskId: String) {
        localDataSource.markAsRedeemed(taskId)
    }
}
