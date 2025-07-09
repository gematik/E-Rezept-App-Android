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

package de.gematik.ti.erp.app.fhir.pharmacy.model.erp

import kotlinx.serialization.Serializable

@Serializable
data class FhirPharmacyErpModel(
    val id: String?,
    val name: String,
    val telematikId: String,
    val position: FhirPositionErpModel?,
    val address: FhirPharmacyAddressErpModel?,
    val contact: FhirContactInformationErpModel,
    val specialities: List<FhirVzdSpecialtyType> = emptyList(),
    val hoursOfOperation: OpeningHoursErpModel? = null, // hoursOfOperation (only available from apo-vzd)
    val availableTime: OpeningHoursErpModel // availableTime
)

@Serializable
data class FhirPositionErpModel(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class FhirPharmacyAddressErpModel(
    val lineAddress: String,
    val postalCode: String?,
    val city: String?
)

@Serializable
data class FhirContactInformationErpModel(
    val phone: String,
    val mail: String,
    val url: String,
    // required only for zuweisung-ohne-ti
    val pickUpUrl: String?,
    val deliveryUrl: String?,
    val onlineServiceUrl: String?
)

// https://simplifier.net/packages/de.gematik.fhir.directory/0.11.24/files/2723324
enum class FhirVzdSpecialtyType(val value: String) {
    Pickup("10"),
    Delivery("30"),
    Shipment("40"),
    Others("0");

    companion object {
        private val lookup = entries.associateBy { it.value }

        fun fromCode(code: String?): FhirVzdSpecialtyType = lookup[code] ?: Others
    }
}
