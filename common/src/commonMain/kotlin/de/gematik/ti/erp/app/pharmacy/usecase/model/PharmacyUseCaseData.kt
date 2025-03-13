/*
 * Copyright 2024, gematik GmbH
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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.model.Coordinates
import de.gematik.ti.erp.app.fhir.model.OpeningHours
import de.gematik.ti.erp.app.fhir.model.PharmacyContacts
import de.gematik.ti.erp.app.fhir.model.PharmacyService
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

private const val DefaultRadiusInMeter = 999 * 1000.0

object PharmacyUseCaseData {
    @Requirement(
        "A_20285#5",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = " .. filter pharmacies by different criteria."
    )
    @Immutable
    data class Filter(
        val nearBy: Boolean = false,
        val deliveryService: Boolean = false,
        val onlineService: Boolean = false,
        val openNow: Boolean = false,
        val directRedeem: Boolean = false
    ) {
        fun isAnySet(): Boolean =
            nearBy || deliveryService || onlineService || openNow || directRedeem
    }

    /**
     * Represents a pharmacy.
     */
    @Serializable
    @Immutable
    data class Pharmacy(
        val id: String,
        val name: String,
        val address: String?,
        val coordinates: Coordinates?,
        val distance: Double?,
        val contacts: PharmacyContacts,
        val provides: List<PharmacyService>,
        val openingHours: OpeningHours?,
        val telematikId: String
    ) {
        val isPickupService
            get() = provides.any { it is PharmacyService.PickUpPharmacyService }

        val isDeliveryService
            get() = provides.any { it is PharmacyService.DeliveryPharmacyService }

        val isOnlineService
            get() = provides.any { it is PharmacyService.OnlinePharmacyService }

        val directRedeemUrlsNotPresent: Boolean
            get() {
                val hasNoPickupContact = contacts.pickUpUrl.isEmpty()
                val hasNoDeliveryContact = contacts.deliveryUrl.isEmpty()
                val hasNoOnlineServiceContact = contacts.onlineServiceUrl.isEmpty()
                return listOf(
                    hasNoPickupContact,
                    hasNoDeliveryContact,
                    hasNoOnlineServiceContact
                ).all { it }
            }

        @Stable
        fun singleLineAddress(): String =
            if (address.isNullOrEmpty()) {
                ""
            } else {
                address.replace("\n", ", ")
            }
    }

    sealed class LocationMode {

        @Immutable
        data object Disabled : LocationMode()

        @Immutable
        data class Enabled(
            val coordinates: Coordinates,
            val radiusInMeter: Double = DefaultRadiusInMeter
        ) : LocationMode()
    }

    @Immutable
    data class SearchData(val name: String, val filter: Filter, val locationMode: LocationMode)

    @Immutable
    data class MapsSearchData(
        val name: String,
        val filter: Filter,
        val locationMode: LocationMode,
        val coordinates: Coordinates?
    )

    /**
     * State with list of pharmacies
     */
    @Immutable
    data class State(
        val search: SearchData
    )

    @Immutable
    data class PrescriptionInOrder(
        val taskId: String,
        val accessCode: String,
        val title: String?,
        val isSelfPayerPrescription: Boolean,
        val index: Int?,
        val timestamp: Instant,
        val substitutionsAllowed: Boolean,
        val isScanned: Boolean
    )

    @Immutable
    data class ShippingContact(
        val name: String,
        val line1: String,
        val line2: String,
        val postalCode: String,
        val city: String,
        val telephoneNumber: String,
        val mail: String,
        val deliveryInformation: String
    ) {
        @Stable
        fun address() = listOf(
            line1,
            line2,
            postalCode,
            city
        ).filter { it.isNotBlank() }

        @Stable
        fun other() = listOf(
            telephoneNumber,
            mail,
            deliveryInformation
        ).filter { it.isNotBlank() }

        @Stable
        fun isEmpty() = address().isEmpty() && other().isEmpty()

        companion object {
            val EmptyShippingContact = ShippingContact(
                name = "",
                line1 = "",
                line2 = "",
                postalCode = "",
                city = "",
                telephoneNumber = "",
                mail = "",
                deliveryInformation = ""
            )
        }
    }

    @Immutable
    data class OrderState(
        val prescriptionsInOrder: List<PrescriptionInOrder>,
        val selfPayerPrescriptionIds: List<String>,
        val contact: ShippingContact
    ) {
        val selfPayerPrescriptionNames = prescriptionsInOrder
            .filter { it.taskId in this.selfPayerPrescriptionIds }
            .mapNotNull { it.title }

        companion object {
            val Empty = OrderState(
                prescriptionsInOrder = emptyList(),
                selfPayerPrescriptionIds = emptyList(),
                contact = ShippingContact.EmptyShippingContact
            )
        }
    }
}
