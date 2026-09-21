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

import de.gematik.ti.erp.app.fhir.pharmacy.model.NotAvailablePeriodMetadata
import de.gematik.ti.erp.app.fhir.pharmacy.model.PharmacyAvailableServiceErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.PharmacyOnSiteFeatureErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.SpecialOpeningTimeMetadata
import de.gematik.ti.erp.app.pharmacy.model.PharmacyAddressErpModel.Companion.toAddressErpModel
import kotlinx.serialization.Serializable

@Serializable
data class PharmacyDetailsErpModel(
    val id: String,
    val name: String,
    val address: String?,
    val coordinates: PositionErpModel?,
    val distance: Double?,
    val contact: ContactInformationErpModel,
    val provides: List<PharmacyServiceErpModel>,
    val openingHours: PharmacyOpeningHoursErpModel?,
    val specialClosingTimes: List<NotAvailablePeriodMetadata> = emptyList(),
    val specialOpeningTimes: List<SpecialOpeningTimeMetadata> = emptyList(),
    val telematikId: String,
    val onSiteFeatures: List<PharmacyOnSiteFeatureErpModel> = emptyList(),
    val availableServices: List<PharmacyAvailableServiceErpModel> = emptyList()
) {
    val isPickupService
        get() = provides.any { it is PharmacyServiceErpModel.PickUpPharmacyServiceErpModel }

    val isDeliveryService
        get() = provides.any { it is PharmacyServiceErpModel.DeliveryPharmacyServiceErpModel }

    val isOnlineService
        get() = provides.any { it is PharmacyServiceErpModel.OnlinePharmacyServiceErpModel }

    fun singleLineAddress(): String =
        if (address.isNullOrEmpty()) {
            ""
        } else {
            address.replace("\n", ", ")
        }

    companion object {
        fun PharmacyDetailsErpModel.toPharmacyErpModel() = PharmacyErpModel(
            telematikId = telematikId,
            name = name,
            position = coordinates,
            contact = contact,
            address = address?.toAddressErpModel()
        )
    }
}
