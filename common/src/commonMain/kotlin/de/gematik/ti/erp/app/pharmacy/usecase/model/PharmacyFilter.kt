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

package de.gematik.ti.erp.app.pharmacy.usecase.model

import de.gematik.ti.erp.app.Requirement

@Requirement(
    "A_20285#6",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "The [PharmacyFilter]filter is closely related to how the backend receives filtering mechanism."
)
data class PharmacyFilter(
    val locationFilter: LocationFilter? = null,
    val serviceFilter: ServiceFilter? = null,
    val textFilter: TextFilter? = null
) {
    companion object {
        /*
        Only valid in apo-vzd, switched off in fhir-vzd
         if (searchData.filter.directRedeem) {
                filterMap += "type" to "DELEGATOR"
            }
         */
        fun PharmacyFilter.buildApoVzdQueryMap(): Map<String, String> {
            val queryMap = mutableMapOf<String, String>()
            locationFilter?.let {
                queryMap += "near" to "" + locationFilter.value
            }
            serviceFilter?.apoVzdValue?.let {
                queryMap += it
            }
            return queryMap.toMap()
        }
    }
}

@Requirement(
    "A_20285#7",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "The [ServiceFilter]filter is to filter only by services that are provided."
)
// https://simplifier.net/packages/de.gematik.fhir.directory/0.11.24/files/2723324
data class ServiceFilter(
    val courier: Boolean = false, // (30)
    val shipment: Boolean = false, // mobl (40)
    val pickup: Boolean = false // (10)
) {
    val fhirVzdShipment: String? = if (shipment) "40" else null
    val fhirVzdCourier: String? = if (courier) "30" else null
    val fhirVzdPickup: String? = if (pickup) "10" else null

    // Generates a list of selected FHIR VZD specialty values (e.g., [30, 40])
    // all together is an OR search which gives results even if one of the service is present
    val fhirVzdServices: List<Int>? = buildList {
        if (courier) add(30)
        if (shipment) add(40)
        if (pickup) add(10)
    }.ifEmpty { null } // Returns null if no specialty is selected

    val apoVzdValue = if (shipment) "type" to "mobl" else null
}

// this one source on how much should be the radius for a search
const val DefaultRadiusInMeter = 20.0 // 999 * 1000.0

@Requirement(
    "A_20285#8",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "The [LocationFilter]filter is to filter by location which is near or based on the location on the map."
)
data class LocationFilter(
    val latitude: Double,
    val longitude: Double,
    val units: String = "km",
    val radius: Double = DefaultRadiusInMeter // is only used on apo-vzd, progressive search takes over for fhir-vzd
) {
    val value = "$latitude|$longitude|${radius.toInt()}|$units" // 48.7695992|9.2002863|999|km
}

@Requirement(
    "A_20285#9",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "The [TextFilter]filter is to filter by the text provided by the user."
)
data class TextFilter(
    val value: List<String> = emptyList()
) {
    companion object {

        // sanitization due to fhir-vzd rules:
        // https://github.com/gematik/api-vzd/blob/main/docs/FHIR_VZD_HOWTO_Search.adoc#fhir-vzd-search-endpoint-payload-type-attribute-display-text-based-search
        private fun String.sanitize(): String = replace(Regex("[.']"), "") // removing ., "" from the search string

        // adding quotes to the search string, to force the backend to search for the exact string
        private fun String.addQuotesSearch(): String? {
            return if (isEmpty()) null else "$this" // please do not remove the quotes, it is required for the search
        }

        fun String.toTextFilter() =
            TextFilter(
                value = split(" ").filter { it.isNotEmpty() }
            )

        fun TextFilter.toSanitizedSearchText(): String? = value.joinToString().sanitize().addQuotesSearch()
    }
}
