/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.api

import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

interface ErpService {

    @GET("Task/{id}")
    suspend fun getTaskWithKBVBundle(
        @Tag profileId: ProfileIdentifier,
        @Path("id") id: String
    ): Response<JsonElement>

    @POST("Task/{id}/\$abort")
    suspend fun deleteTask(@Tag profileId: ProfileIdentifier, @Path("id") id: String): Response<JsonElement>

    /**
     * @param lastUpdated expects format like that ge2021-01-31T10:00 where "ge" represents Greater or Equal
     */
    @GET("Task")
    suspend fun getTasks(
        @Tag profileId: ProfileIdentifier,
        @Query("modified") lastUpdated: String?,
        @Query("_sort") sort: String = "modified",
        @Query("_count") count: Int? = null,
        @Query("__offset") offset: Int? = null
    ): Response<JsonElement>

    /**
     * @param lastKnownDate expects format like that ge2021-01-31T10:00 where ge stays for Greater or Equal. Null value will remove this query parameter
     * @param sort refers to the date attribute ASC
     */
    @GET("AuditEvent")
    suspend fun getAuditEvents(
        @Tag profileId: ProfileIdentifier,
        @Query("date") lastKnownDate: String?,
        @Query("_sort") sort: String = "+date",
        @Query("_count") count: Int? = null,
        @Query("__offset") offset: Int? = null
    ): Response<JsonElement>

    @POST("Communication")
    suspend fun postCommunication(
        @Tag profileId: ProfileIdentifier,
        @Body communication: JsonElement,
        @Header("X-AccessCode") accessCode: String? = null
    ): Response<JsonElement>

    @GET("Communication")
    suspend fun getCommunications(
        @Tag profileId: ProfileIdentifier,
        @Query("sent") lastKnownDate: String?,
        @Query("_sort") sort: String = "+sent",
        @Query("_count") count: Int? = null,
        @Query("__offset") offset: Int? = null
    ): Response<JsonElement>

    @GET("MedicationDispense")
    suspend fun bundleOfMedicationDispenses(
        @Tag profileId: ProfileIdentifier,
        @Query("identifier") id: String
    ): Response<JsonElement>

    @GET("Consent")
    suspend fun getConsent(
        @Tag profileId: ProfileIdentifier
    ): Response<JsonElement>

    @POST("Consent")
    suspend fun grantConsent(
        @Tag profileId: ProfileIdentifier,
        @Body consent: JsonElement
    ): Response<Unit>

    @DELETE("Consent")
    suspend fun deleteConsent(
        @Tag profileId: ProfileIdentifier,
        @Query("category") category: String
    ): Response<Unit>
}
