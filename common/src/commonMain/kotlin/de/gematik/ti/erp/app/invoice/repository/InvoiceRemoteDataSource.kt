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

package de.gematik.ti.erp.app.invoice.repository

import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.api.safeApiCallRaw
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import java.net.HttpURLConnection

class InvoiceRemoteDataSource(
    private val service: ErpService
) {
    suspend fun getChargeItems(
        profileId: ProfileIdentifier,
        lastUpdated: String?,
        count: Int? = null,
        offset: Int? = null
    ) = safeApiCall(
        errorMessage = "Error getting all chargeItems"
    ) {
        service.getChargeItems(
            profileId = profileId,
            lastUpdated = lastUpdated,
            count = count,
            offset = offset
        )
    }

    suspend fun getChargeItemBundleById(
        profileId: ProfileIdentifier,
        taskID: String
    ) = safeApiCall(
        errorMessage = "error while downloading ChargeItem for $taskID"
    ) { service.getChargeItemBundleById(profileId = profileId, id = taskID) }

    suspend fun deleteChargeItemById(
        profileId: ProfileIdentifier,
        taskId: String
    ) = safeApiCallRaw(
        errorMessage = "Error delete charge item"
    ) {
        val response = service.deleteChargeItemById(
            profileId = profileId,
            id = taskId
        )
        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
            Result.success(Unit)
        } else {
            Result.failure(
                ApiCallException(
                    "Expected no content but received: ${response.code()} ${response.message()}",
                    response
                )
            )
        }
    }
}
