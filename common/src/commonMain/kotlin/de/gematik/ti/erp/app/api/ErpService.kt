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

package de.gematik.ti.erp.app.api

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
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
import retrofit2.http.Url

@Requirement(
    "O.Purp_8#4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Interface of Erp(Fachdienst)-service"
)
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
        // https://gemspec.gematik.de/docs/gemSpec/gemSpec_FD_eRp/gemSpec_FD_eRp_V2.3.0/#5.10.1
        @Query("_sort") sort: String = "modified", // sorts based on Task.lastModified
        @Query("_count") count: Int? = null,
        @Query("__offset") offset: Int? = null
    ): Response<JsonElement>

    /**
     * @param url expects format like that https://erp-test.zentral.erp.splitdns.ti-dienste.de/Task?_sort=modified&_count=50&__offset=0
     * This is used to fetch tasks from a specific URL, which might be useful for pagination or specific queries.
     */
    @GET
    suspend fun getTasksByUrl(
        @Url url: String,
        @Tag profileId: ProfileIdentifier
    ): Response<JsonElement>

    /**
     * @param lastKnownDate expects format like that ge2021-01-31T10:00 where ge stays for Greater or Equal. Null value will remove this query parameter
     * @param sort refers to the date attribute ASC
     */
    @GET("AuditEvent")
    suspend fun getAuditEvents(
        @Tag profileId: ProfileIdentifier,
        @Header("Accept-Language") language: String,
        @Query("_sort") sort: String = "-date",
        @Query("_count") count: Int? = null,
        @Query("__offset") offset: Int? = null
    ): Response<JsonElement>

    @POST("Communication")
    suspend fun postCommunication(
        @Tag profileId: ProfileIdentifier,
        @Body communication: JsonElement,
        @Header("X-AccessCode") accessCode: String
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

    @GET("Medication")
    suspend fun getMedication(
        @Tag profileId: ProfileIdentifier,
        @Query("reference") id: String
    ): Response<JsonElement>

    // PKV consent
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

    // PKV Invoices
    @GET("ChargeItem")
    suspend fun getChargeItems(
        @Tag profileId: ProfileIdentifier,
        @Query("modified") lastUpdated: String?,
        @Query("_sort") sort: String = "modified",
        @Query("_count") count: Int? = null,
        @Query("__offset") offset: Int? = null
    ): Response<JsonElement>

    @GET("ChargeItem/{id}")
    suspend fun getChargeItemBundleById(
        @Tag profileId: ProfileIdentifier,
        @Path("id") id: String
    ): Response<JsonElement>

    @DELETE("ChargeItem/{id}")
    suspend fun deleteChargeItemById(
        @Tag profileId: ProfileIdentifier,
        @Path("id") id: String
    ): Response<Unit>
}
