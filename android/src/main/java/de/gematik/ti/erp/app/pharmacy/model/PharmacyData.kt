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

package de.gematik.ti.erp.app.pharmacy.model

import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import java.time.Instant

object PharmacyData {
    data class ShippingContact(
        val name: String,
        val line1: String,
        val line2: String,
        val postalCodeAndCity: String,
        val telephoneNumber: String,
        val mail: String,
        val deliveryInformation: String
    )
}

fun SyncedTaskData.SyncedTask.shippingContact() =
    PharmacyData.ShippingContact(
        name = this.patient.name ?: "",
        line1 = this.patient.address?.line1 ?: "",
        line2 = this.patient.address?.line2 ?: "",
        postalCodeAndCity = this.patient.address?.postalCodeAndCity ?: "",
        telephoneNumber = "",
        mail = "",
        deliveryInformation = ""
    )

object OverviewPharmacyData {
    data class OverviewPharmacy(
        val lastUsed: Instant,
        val isFavorite: Boolean,
        val usageCount: Int,
        val telematikId: String,
        val pharmacyName: String,
        val address: String
    )
}
