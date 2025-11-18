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

@Requirement(
    "O.Purp_8#7",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Interface of pharmacy search service"
)
interface FhirVzdService {

    /**
     * Searches for pharmacies using the FHIR VZD (Verzeichnisdienst) API.
     *
     * This method performs a general pharmacy search with various filtering options including
     * service types (courier, pickup, shipment) and text-based search. Results are sorted by
     * proximity when location data is available.
     *
     * @param organizationActive Filter to include only active organizations (default: true)
     * @param includeOrganization Include organization resources in the response
     * @param includeLocation Include location resources in the response
     * @param type Filter for pharmacy type, defaults to public pharmacies ("offentliche Apotheke")
     * @param textSearch Optional text search filter for pharmacy names or other text fields
     * @param serviceTypeCourier Optional filter for pharmacies offering courier services (specialty code)
     * @param serviceTypePickup Optional filter for pharmacies offering pickup services (specialty code)
     * @param serviceTypeShipment Optional filter for pharmacies offering shipment services (specialty code)
     * @param sortBy Sorting criteria, defaults to "near" for distance-based sorting
     * @param count Maximum number of results to return (default: 100, which is the API maximum)
     * @return Response containing a JsonElement with the search results
     */
    @GET("HealthcareService")
    suspend fun search(
        @Query("organization.active") organizationActive: Boolean = true,
        @Query("_include") includeOrganization: String = "HealthcareService:organization",
        @Query("_include") includeLocation: String = "HealthcareService:location",
        @Query("organization.type") type: String = FhirVzdPharmacyTypeCode.publicPharmacy,
        @Query("_text") textSearch: String?,
        @Query("specialty") serviceTypeCourier: String?,
        @Query("specialty") serviceTypePickup: String?,
        @Query("specialty") serviceTypeShipment: String?,
        @Query("_sortby") sortBy: String = "near",
        @Query("_count") count: Int = 100
    ): Response<JsonElement>

    /**
     * Searches for insurance providers using the FHIR VZD API.
     *
     * This method specifically searches for healthcare services provided by insurance companies
     * (Krankenversicherungen) rather than pharmacies.
     *
     * @param organizationActive Filter to include only active organizations (default: true)
     * @param includeOrganization Include organization resources in the response
     * @param includeLocation Include location resources in the response
     * @param type Filter for organization type, set to insurance providers ("KrankenKassen")
     * @param textSearch Optional text search filter for insurance provider names
     * @return Response containing a JsonElement with the insurance provider search results
     */
    @GET("HealthcareService")
    suspend fun searchInsurance(
        @Query("organization.active") organizationActive: Boolean = true,
        @Query("_include") includeOrganization: String = "HealthcareService:organization",
        @Query("_include") includeLocation: String = "HealthcareService:location",
        @Query("organization.type") type: String = FhirVzdPharmacyTypeCode.insuranceProvider,
        @Query("_text") textSearch: String?
    ): Response<JsonElement>

    /**
     * Searches for a specific pharmacy by its Telematik-ID.
     *
     * The Telematik-ID is a unique identifier for healthcare providers in the German
     * healthcare system. This method returns exactly one result when a match is found.
     *
     * @param organizationActive Filter to include only active organizations (default: true)
     * @param includeOrganization Include organization resources in the response
     * @param includeLocation Include location resources in the response
     * @param status Optional endpoint status filter (conditionally included, not typically used)
     * @param telematikId The unique Telematik-ID of the pharmacy to search for
     * @param count Maximum number of results (default: 1, as only one pharmacy should match)
     * @return Response containing a JsonElement with the specific pharmacy data
     */
    @GET("HealthcareService")
    suspend fun searchByTelematikId(
        @Query("organization.active") organizationActive: Boolean = true,
        @Query("_include") includeOrganization: String = "HealthcareService:organization",
        @Query("_include") includeLocation: String = "HealthcareService:location",
        @Query("endpoint.status") status: String?,
        @Query("organization.identifier") telematikId: String,
        @Query("_count") count: Int = 1
    ): Response<JsonElement>

    /**
     * Searches for healthcare services by insurance provider using IKNR (Institutionskennzeichen).
     *
     * This method searches for services associated with a specific insurance provider,
     * identified by their institutional identifier (IKNR).
     *
     * @param organizationActive Filter to include only active organizations (default: true)
     * @param organizationType Filter for organization type, set to insurance providers
     * @param includeOrganization Include organization resources (required for Telematik-ID access)
     * @param status Optional endpoint status filter (default: null)
     * @param count Maximum number of results (default: 1)
     * @param telematikIdentifier Telematik-ID identifier prefix for filtering
     * @param organizationIdentifier The IKNR (institutional identifier) to search for
     * @return Response containing a JsonElement with the insurance provider service data
     */
    @GET("HealthcareService")
    suspend fun searchByInsuranceProvider(
        @Query("organization.active") organizationActive: Boolean = true,
        @Query("organization.type") organizationType: String = FhirVzdPharmacyTypeCode.insuranceProvider,
        @Query("_include") includeOrganization: String = "HealthcareService:organization",
        @Query("endpoint.status") status: String? = null,
        @Query("_count") count: Int = 1,
        @Query("organization.identifier") telematikIdentifier: String = "https://gematik.de/fhir/sid/telematik-id|",
        @Query("organization.identifier") organizationIdentifier: String
    ): Response<JsonElement>

    /**
     * Searches for available countries supported by pharmacy services.
     *
     * This method retrieves information about which countries are supported for
     * pharmaceutical services, typically used for international prescription handling.
     *
     * @param organizationActive Filter to include only active organizations (default: true)
     * @param includeOrganization Include organization resources in the response
     * @param type Filter for special provider type that handles country information
     * @param specialty Filter by specialty code "57833-6" for country availability services
     * @param count Maximum number of results (default: 100)
     * @return Response containing a JsonElement with available countries data
     */
    @GET("HealthcareService")
    suspend fun searchAvailableCountries(
        @Query("organization.active") organizationActive: Boolean = true,
        @Query("_include") includeOrganization: String = "HealthcareService:organization",
        @Query("organization.type") type: String = FhirVzdPharmacyTypeCode.availableCountriesProvider,
        @Query("specialty") specialty: String = "57833-6",
        @Query("_count") count: Int = 100
    ): Response<JsonElement>

    /**
     * Searches for nearby pharmacies using location-based proximity search.
     *
     * This method uses the specialized "nearPharmacy" query to find pharmacies within
     * a specified radius of given coordinates. It supports text-based filtering for
     * service types like "Handverkauf", "Botendienst", and "Versand".
     *
     * @param textSearch Optional text search for service types or pharmacy names
     *                   (e.g., "Handverkauf Botendienst Versand")
     * @param query The query type, fixed to "nearPharmacy" for location-based search
     * @param longitude Longitude coordinate for the search center point
     * @param latitude Latitude coordinate for the search center point
     * @param distance Search radius in kilometers from the specified coordinates
     * @param count Maximum number of results to return (default: 100)
     * @return Response containing a JsonElement with nearby pharmacy search results
     */
    @GET("HealthcareService")
    suspend fun searchNearPharmacy(
        @Query("text") textSearch: String?,
        @Query("_query") query: String = "nearPharmacy",
        @Query("longitude") longitude: Double?,
        @Query("latitude") latitude: Double?,
        @Query("distance") distance: Int?,
        @Query("_count") count: Int = 100
    ): Response<JsonElement>
}
