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

package de.gematik.ti.erp.app.pharmacy.model

import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import kotlinx.datetime.Instant

fun SyncedTaskData.SyncedTask.shippingContact() =
    ShippingInfoErpModel(
        name = this.patient.name ?: "",
        street = this.patient.address?.line1 ?: "",
        addressDetail = this.patient.address?.line2 ?: "",
        zip = this.patient.address?.postalCode ?: "",
        city = this.patient.address?.city ?: "",
        phone = "",
        mail = "",
        deliveryInfo = ""
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
