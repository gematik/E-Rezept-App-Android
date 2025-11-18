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

import de.gematik.ti.erp.app.pharmacy.usecase.model.LocationFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.ServiceFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.TextFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.TextFilter.Companion.toSanitizedSearchText
import kotlinx.serialization.json.JsonElement
import retrofit2.Response

/**
 * Extension function to search for nearby pharmacies using all existing filter models:
 * - ServiceFilter: for service types (Handverkauf, Botendienst, Versand)
 * - LocationFilter: for location-based search (latitude, longitude, radius)
 * - TextFilter: for additional user-provided search text (properly sanitized)
 */
suspend fun FhirVzdService.searchPharmacyWithLocation(
    serviceFilter: ServiceFilter?,
    locationFilter: LocationFilter? = null,
    textFilter: TextFilter? = null,
    count: Int = 100
): Response<JsonElement> {
    val additionalText = textFilter?.toSanitizedSearchText()
    val textSearch = serviceFilter?.buildTextSearch(additionalText) ?: additionalText

    return searchNearPharmacy(
        textSearch = textSearch,
        longitude = locationFilter?.longitude,
        latitude = locationFilter?.latitude,
        distance = locationFilter?.radius?.toInt(),
        count = count
    )
}
