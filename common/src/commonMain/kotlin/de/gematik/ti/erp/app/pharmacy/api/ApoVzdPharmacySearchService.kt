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

package de.gematik.ti.erp.app.pharmacy.api

import de.gematik.ti.erp.app.Requirement
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

@Requirement(
    "O.Purp_8#6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Interface of pharmacy search service"
)
interface ApoVzdPharmacySearchService {

    @GET("api/Location")
    suspend fun search(
        @Query("name") names: List<String>,
        @QueryMap attributes: Map<String, String>
    ): Response<JsonElement>

    @GET("api/Location")
    suspend fun searchByTelematikId(
        @Query("identifier") telematikId: String
    ): Response<JsonElement>

    // paging realised through session referenced by the previous bundle id
    @GET("api")
    suspend fun searchByBundle(
        @Query("_getpages") bundleId: String,
        @Query("_getpagesoffset") offset: Int,
        @Query("_count") count: Int
    ): Response<JsonElement>

    @GET("api/Binary")
    suspend fun searchBinary(
        @Query("_securityContext") locationId: String
    ): Response<JsonElement>
}
