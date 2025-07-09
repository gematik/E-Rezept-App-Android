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

package de.gematik.ti.erp.app.pharmacy.repository.datasource.remote

import de.gematik.ti.erp.app.api.PharmacyRedeemService
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.pharmacy.api.ApoVzdPharmacySearchService
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyFilter.Companion.buildApoVzdQueryMap
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL

class ApoVzdRemoteDataSource(
    private val searchService: ApoVzdPharmacySearchService,
    private val redeemService: PharmacyRedeemService
) : PharmacyRemoteDataSource {

    companion object {
        internal const val PlaceholderTelematikId = "<ti_id>"
        internal const val PlaceholderTransactionId = "<transactionID>"
    }

    override suspend fun searchPharmacies(filter: PharmacyFilter, onUnauthorizedException: suspend () -> Unit): Result<JsonElement> =
        safeApiCall("error searching pharmacies") {
            searchService.search(
                names = filter.textFilter?.value.takeIf { it?.isNotEmpty() == true } ?: emptyList(),
                attributes = filter.buildApoVzdQueryMap()
            )
        }

    override suspend fun searchInsurances(filter: PharmacyFilter, onUnauthorizedException: suspend () -> Unit): Result<JsonElement> = error("not implemented")

    override suspend fun searchPharmaciesContinued(
        bundleId: String,
        offset: Int,
        count: Int
    ): Result<JsonElement> = safeApiCall("error searching pharmacies") {
        searchService.searchByBundle(bundleId = bundleId, offset = offset, count = count)
    }

    // The user is not login we are use cert
    override suspend fun searchBinaryCert(
        locationId: String
    ): Result<JsonElement> = safeApiCall("error searching binary") {
        if (locationId.startsWith("Location/")) {
            searchService.searchBinary(locationId = locationId)
        } else {
            searchService.searchBinary(locationId = "Location/$locationId")
        }
    }

    override suspend fun redeemPrescriptionDirectly(
        url: String,
        message: ByteArray,
        pharmacyTelematikId: String,
        transactionId: String
    ): Result<Unit> = safeApiCall("error redeeming prescription with $url") {
        val messageBody = message.toRequestBody("application/pkcs7-mime".toMediaType())

        val validatedUrl = url
            .replace(PlaceholderTelematikId, pharmacyTelematikId, ignoreCase = true)
            .replace(PlaceholderTransactionId, transactionId, ignoreCase = true)
            .let {
                URL(it)
            }

        redeemService.redeemDirectly(
            url = validatedUrl.toString(),
            message = messageBody
        )
    }

    override suspend fun searchPharmacyByTelematikId(telematikId: String, onUnauthorizedException: suspend () -> Unit): Result<JsonElement> =
        safeApiCall("error searching pharmacies") {
            if (telematikId.startsWith("3-SMC")) {
                searchService.search(names = listOf(telematikId), emptyMap())
            } else {
                searchService.searchByTelematikId(telematikId = telematikId)
            }
        }

    // should not be called for apo-vzd
    override suspend fun searchByInsuranceProvider(
        institutionIdentifier: String,
        onUnauthorizedException: suspend () -> Unit
    ): Result<JsonElement> {
        return Result.failure(NotImplementedError())
    }
}
