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

package de.gematik.ti.erp.app.api

import org.hl7.fhir.r4.model.Bundle
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ErpService {
    @GET("Task/{id}")
    suspend fun getTaskWithKBVBundle(@Path("id") id: String): Response<Bundle>

    @POST("Task/{id}/\$abort")
    suspend fun deleteTask(@Path("id") id: String): Response<Unit>

    @GET("Task")
    suspend fun getAllTasks(): Response<Bundle>

    @GET("AuditEvent")
    suspend fun getAllAuditEvents(
        @Query("_sort") sort: String = "-date",
        @Query("_count") count: Int? = null,
        @Query("__offset") offset: Int? = null
    ): Response<Bundle>

    @GET("MedicationDispense")
    suspend fun getAllMedicationDispenses(): Response<Bundle>

    @GET("Communication")
    suspend fun getAllCommunications(): Response<Bundle>
}
