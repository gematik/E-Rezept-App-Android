/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.usecase.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.pharmacy.repository.model.Location
import de.gematik.ti.erp.app.pharmacy.repository.model.OpeningHours
import de.gematik.ti.erp.app.pharmacy.repository.model.PharmacyContacts
import de.gematik.ti.erp.app.pharmacy.repository.model.PharmacyService
import de.gematik.ti.erp.app.pharmacy.repository.model.RoleCode
import kotlinx.parcelize.Parcelize

object PharmacyUseCaseData {
    @Parcelize
    @Immutable
    data class Filter(
        val ready: Boolean = false,
        val deliveryService: Boolean = false,
        val onlineService: Boolean = false,
        val openNow: Boolean = false,
    ) : Parcelable {
        fun isAnySet(): Boolean =
            ready || deliveryService || onlineService || openNow
    }

    /**
     * Represents a pharmacy.
     */
    @Parcelize
    @Immutable
    data class Pharmacy(
        val name: String,
        val address: String?,
        val location: Location?,
        val distance: Double?,
        val contacts: PharmacyContacts,
        val provides: List<PharmacyService>,
        val openingHours: OpeningHours?,
        val telematikId: String,
        val roleCode: List<RoleCode>,
        val ready: Boolean
    ) : Parcelable {

        @Stable
        fun removeLineBreaksFromAddress(): String {
            if (address.isNullOrEmpty()) return ""
            return address.replace("\n", ", ")
        }
    }

    sealed class LocationMode {
        /**
         * We only store the information if gps was enabled and not the actual position.
         */
        @Parcelize
        @Immutable
        object EnabledWithoutPosition : LocationMode(), Parcelable
        @Parcelize
        @Immutable
        object Disabled : LocationMode(), Parcelable
        @Parcelize
        @Immutable
        class Enabled(val location: Location) : LocationMode(), Parcelable
    }

    @Immutable
    data class SearchData(val name: String, val filter: Filter, val locationMode: LocationMode)

    /**
     * State with list of pharmacies
     */
    @Immutable
    data class State(
        val search: SearchData,
        val showLocationHint: Boolean
    )

    @Immutable
    data class PrescriptionOrder(
        val taskId: String,
        val accessCode: String,
        val title: String,
        val substitutionsAllowed: Boolean
    )

    @Immutable
    @Parcelize
    data class ShippingContact(
        val name: String,
        val line1: String,
        val line2: String,
        val postalCodeAndCity: String,
        val telephoneNumber: String,
        val mail: String,
        val deliveryInformation: String
    ) : Parcelable {
        @Stable
        fun toList() = listOf(
            name,
            line1,
            line2,
            postalCodeAndCity,
            telephoneNumber,
            mail,
            deliveryInformation
        ).filter { it.isNotBlank() }

        @Stable
        fun address() = listOf(
            line1,
            line2,
            postalCodeAndCity
        ).filter { it.isNotBlank() }

        @Stable
        fun other() = listOf(
            telephoneNumber,
            mail,
            deliveryInformation
        ).filter { it.isNotBlank() }

        @Stable
        fun phoneOrAddressMissing() = telephoneNumber.isBlank() || name.isBlank() || line1.isBlank() || postalCodeAndCity.isBlank()
    }

    @Immutable
    data class OrderState(
        val prescriptions: List<PrescriptionOrder>,
        val contact: ShippingContact?
    )
}
