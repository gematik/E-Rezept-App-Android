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

package de.gematik.ti.erp.app.pharmacy.repository.datasource.remote

import de.gematik.ti.erp.app.api.UnauthorizedException
import de.gematik.ti.erp.app.api.nonFatalApiCall
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.pharmacy.api.FhirVzdPharmacySearchService
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.TextFilter.Companion.toSanitizedSearchText
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement
import retrofit2.Response

class FhirVzdRemoteDataSource(
    private val searchService: FhirVzdPharmacySearchService
) : PharmacyRemoteDataSource {

    private suspend fun <T : Any> safeApiCallWithUnauthorizedRetry(
        errorMessage: String,
        onUnauthorizedException: suspend () -> Unit,
        call: suspend () -> Response<T>
    ): Result<T> {
        return try {
            val result = nonFatalApiCall(errorMessage) { call() }
            if (result.isFailure && result.exceptionOrNull() is UnauthorizedException) {
                if (!handleUnauthorizedException(onUnauthorizedException)) {
                    return Result.failure(UnauthorizedException("Error handling unauthorized exception", null))
                }
                return safeApiCall(errorMessage) { call() }
            }
            result
        } catch (e: Throwable) {
            Napier.e { "API Call Error: ${e.message}" }
            Result.failure(e)
        }
    }

    private suspend fun handleUnauthorizedException(
        onUnauthorizedException: suspend () -> Unit
    ): Boolean {
        return try {
            onUnauthorizedException()
            true
        } catch (e: Exception) {
            Napier.e { "Error in onUnauthorizedException: ${e.message}" }
            false
        }
    }

    override suspend fun searchPharmacies(
        filter: PharmacyFilter,
        onUnauthorizedException: suspend () -> Unit
    ): Result<JsonElement> {
        return safeApiCallWithUnauthorizedRetry(
            errorMessage = "error on search Pharmacies on fhir-vzd",
            onUnauthorizedException = onUnauthorizedException
        ) {
            searchService.search(
                name = filter.textFilter?.toSanitizedSearchText(),
                serviceTypeShipment = filter.serviceFilter?.fhirVzdShipment,
                serviceTypeCourier = filter.serviceFilter?.fhirVzdCourier,
                serviceTypePickup = filter.serviceFilter?.fhirVzdPickup,
                position = filter.locationFilter?.value
            )
        }
    }

    override suspend fun searchPharmacyByTelematikId(
        telematikId: String,
        onUnauthorizedException: suspend () -> Unit
    ): Result<JsonElement> {
        return safeApiCallWithUnauthorizedRetry(
            errorMessage = "error on searchPharmacyByTelematikId on fhir-vzd",
            onUnauthorizedException = onUnauthorizedException
        ) {
            // not sure if we add the status="active" or not
            searchService.searchByTelematikId(telematikId = telematikId, status = null)
        }
    }

    // will not be called for fhir-vzd
    override suspend fun searchBinaryCert(locationId: String): Result<JsonElement> {
        return Result.failure(NotImplementedError())
    }

    // will not be called for fhir-vzd
    override suspend fun searchPharmaciesContinued(
        bundleId: String,
        offset: Int,
        count: Int
    ): Result<JsonElement> {
        return Result.failure(NotImplementedError())
    }

    // will not be called for fhir-vzd
    override suspend fun redeemPrescriptionDirectly(
        url: String,
        message: ByteArray,
        pharmacyTelematikId: String,
        transactionId: String
    ): Result<Unit> {
        return Result.failure(NotImplementedError())
    }
}
