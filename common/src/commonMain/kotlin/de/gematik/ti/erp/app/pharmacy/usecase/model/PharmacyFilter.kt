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

        /**
         * Creates a ServiceFilter based on location mode.
         * Uses CodedServiceFilter for location-based searches (more efficient for FHIR VZD API)
         * Uses TextServiceFilter for text-based searches
         */
        private fun createServiceFilter(
            courier: Boolean = false,
            shipment: Boolean = false,
            pickup: Boolean = false
        ): ServiceFilter = CodedServiceFilter(courier = courier, shipment = shipment, pickup = pickup)

        /**
         * Creates a PharmacyFilter with the appropriate ServiceFilter implementation
         * based on whether location filtering is enabled
         */
        fun create(
            locationFilter: LocationFilter? = null,
            textFilter: TextFilter? = null,
            courier: Boolean = false,
            shipment: Boolean = false,
            pickup: Boolean = false
        ): PharmacyFilter = PharmacyFilter(
            locationFilter = locationFilter,
            serviceFilter = createServiceFilter(
                courier = courier,
                shipment = shipment,
                pickup = pickup
            ),
            textFilter = textFilter
        )
    }
}
