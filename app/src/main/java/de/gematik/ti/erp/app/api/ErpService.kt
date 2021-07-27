/*
 * Copyright (c) 2021 gematik GmbH
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

import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CapabilityStatement
import org.hl7.fhir.r4.model.Communication
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ErpService {

    @GET("metadata")
    suspend fun capability(): Response<CapabilityStatement>

    @GET("Task/{id}")
    suspend fun taskWithKBVBundle(@Path("id") id: String): Response<Bundle>

    @POST("Task/{id}/\$abort")
    suspend fun deleteTask(@Path("id") id: String): Response<Bundle>

    /**
     * @param lastUpdated expects format like that ge2021-01-31T10:00 where "ge" represents Greater or Equal
     */
    @GET("Task")
    suspend fun allTasks(@Query("modified") lastUpdated: String?): Response<Bundle>

    /**
     * @param lastKnownDate expects format like that ge2021-01-31T10:00 where ge stays for Greater or Equal. Null value will remove this query parameter
     * @param sort refers to the date attribute DESC
     */
    @GET("AuditEvent")
    suspend fun allAuditEvents(
        @Query("date") lastKnownDate: String?,
        @Query("_sort") sort: String = "-date"
    ): Response<Bundle>

    suspend fun allAuditEvents(): Response<Bundle>

    @POST("Communication")
    suspend fun communication(@Body communication: Communication): Response<ResponseBody>

    @GET("Communication")
    suspend fun communication(): Response<Bundle>

    @GET("MedicationDispense/{id}")
    suspend fun medicationDispense(@Path("id") id: String): Response<Bundle>
}
