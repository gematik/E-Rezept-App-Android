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
        @Query("_sort") sort: String = "-date"
    ): Response<Bundle>

    @GET("MedicationDispense")
    suspend fun getAllMedicationDispenses(): Response<Bundle>

    @GET("Communication")
    suspend fun getAllCommunications(): Response<Bundle>
}
