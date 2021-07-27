/*
 * Copyright (c) 2021 gematik GmbH
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
import de.gematik.ti.erp.app.pharmacy.repository.model.Location
import de.gematik.ti.erp.app.pharmacy.repository.model.OpeningHours
import de.gematik.ti.erp.app.pharmacy.repository.model.PharmacyContacts
import de.gematik.ti.erp.app.pharmacy.repository.model.PharmacyService
import de.gematik.ti.erp.app.pharmacy.repository.model.RoleCode
import kotlinx.parcelize.Parcelize
import javax.annotation.concurrent.Immutable

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
     *  Represents a pharmacy.
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
        fun removeLineBreaksFromAddress(): String {
            if (address.isNullOrEmpty()) return ""
            return address.replace("\n", ", ")
        }
    }

    data class SearchData(val name: String, val filter: Filter, val location: Location?)

    /**
     *  State with list of pharmacies
     */
    data class State(
        val search: SearchData?,
        val showLocationHint: Boolean
    )
}
