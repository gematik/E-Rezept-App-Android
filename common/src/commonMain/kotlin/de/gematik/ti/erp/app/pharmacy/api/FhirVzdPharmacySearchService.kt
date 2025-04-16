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

package de.gematik.ti.erp.app.pharmacy.api

import de.gematik.ti.erp.app.Requirement
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

@Requirement(
    "O.Purp_8#7",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Interface of pharmacy search service"
)
interface FhirVzdPharmacySearchService {

    @GET("HealthcareService")
    suspend fun search(
        // this is a required filter
        @Query("organization.active") organizationActive: Boolean = true,
        // Include all related resources
        @Query("_include") includeOrganization: String = "HealthcareService:organization",
        @Query("_include") includeLocation: String = "HealthcareService:location",
        // filter for Pharmacies of type "offentliche Apotheke"
        @Query("_text") organizationType: String = FhirVzdPharmacyTypeCode.publicPharmacy,
        // filter by name
        @Query("_text") name: String?,
        // filter by service type for courier
        @Query("specialty") serviceTypeCourier: String?,
        // filter by service type for pickup
        @Query("specialty") serviceTypePickup: String?,
        // filter by service type for shipment
        @Query("specialty") serviceTypeShipment: String?,
        // filter by location
        @Query("location.near") position: String?,
        // sorting by distance when its from backend
        @Query("_sortby") sortBy: String = "near",
        // this makes the backend search to always give 100 (max value)
        @Query("_count") count: Int = 100
    ): Response<JsonElement>

    @GET("HealthcareService")
    suspend fun searchByTelematikId(
        // this is a required filter
        @Query("organization.active") organizationActive: Boolean = true,
        // Include all related resources
        @Query("_include") includeOrganization: String = "HealthcareService:organization",
        @Query("_include") includeLocation: String = "HealthcareService:location",
        // this is included only under certain conditions, we do not use it
        @Query("endpoint.status") status: String?,
        // filter by telematik-id
        @Query("organization.identifier") telematikId: String,
        // always gives only 1 result
        @Query("_count") count: Int = 1
    ): Response<JsonElement>
}
