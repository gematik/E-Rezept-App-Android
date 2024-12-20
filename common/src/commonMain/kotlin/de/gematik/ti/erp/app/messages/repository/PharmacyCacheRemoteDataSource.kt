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

package de.gematik.ti.erp.app.messages.repository

import de.gematik.ti.erp.app.api.PharmacySearchService
import de.gematik.ti.erp.app.api.safeApiCall
import kotlinx.serialization.json.JsonElement

class PharmacyCacheRemoteDataSource(
    private val searchService: PharmacySearchService
) {
    suspend fun searchPharmacy(
        telematikId: String
    ): Result<JsonElement> = safeApiCall("error searching pharmacy by telematikId") {
        if (telematikId.startsWith("3-SMC")) {
            searchService.search(names = listOf(telematikId), emptyMap())
        } else {
            searchService.searchByTelematikId(telematikId = telematikId)
        }
    }
}
