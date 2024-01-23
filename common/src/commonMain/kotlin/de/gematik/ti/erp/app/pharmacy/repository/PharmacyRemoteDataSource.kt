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

import de.gematik.ti.erp.app.api.PharmacyRedeemService
import de.gematik.ti.erp.app.api.PharmacySearchService
import de.gematik.ti.erp.app.api.safeApiCall
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL

private const val PlaceholderTelematikId = "<ti_id>"
private const val PlaceholderTransactionId = "<transactionID>"

class PharmacyRemoteDataSource(
    private val searchService: PharmacySearchService,
    private val redeemService: PharmacyRedeemService
) {

    suspend fun searchPharmacies(
        names: List<String>,
        filter: Map<String, String>
    ): Result<JsonElement> = safeApiCall("error searching pharmacies") {
        searchService.search(names, filter)
    }

    suspend fun searchPharmaciesContinued(
        bundleId: String,
        offset: Int,
        count: Int
    ): Result<JsonElement> = safeApiCall("error searching pharmacies") {
        searchService.searchByBundle(bundleId = bundleId, offset = offset, count = count)
    }

    suspend fun searchBinaryCert(
        locationId: String
    ): Result<JsonElement> = safeApiCall("error searching binary") {
        if (locationId.startsWith("Location/")) {
            searchService.searchBinary(locationId = locationId)
        } else {
            searchService.searchBinary(locationId = "Location/$locationId")
        }
    }

    suspend fun redeemPrescriptionDirectly(
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

    suspend fun searchPharmacyByTelematikId(
        telematikId: String
    ): Result<JsonElement> = safeApiCall("error searching pharmacies") {
        if (telematikId.startsWith("3-SMC")) {
            searchService.search(names = listOf(telematikId), emptyMap())
        } else {
            searchService.searchByTelematikId(telematikId = telematikId)
        }
    }
}
