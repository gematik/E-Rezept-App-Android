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

package de.gematik.ti.erp.app.pharmacy.usecase.model

import de.gematik.ti.erp.app.Requirement

@Requirement(
    "A_20285#8",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "The [LocationFilter]filter is to filter by location which is near or based on the location on the map."
)
data class LocationFilter(
    val latitude: Double,
    val longitude: Double,
    val units: String = "km",
    val radius: Double = DEFAULT_RADIUS_IN_KM // is only used on apo-vzd, progressive search takes over for fhir-vzd
) {
    val value = "$latitude|$longitude|${radius.toInt()}|$units" // 48.7695992|9.2002863|999|km
}

// this one source on how much should be the radius for a search
const val DEFAULT_RADIUS_IN_KM = 100.0 // 999 * 1000.0
