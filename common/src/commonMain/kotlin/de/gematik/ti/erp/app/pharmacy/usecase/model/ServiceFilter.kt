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
    "A_20285#7",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "The [ServiceFilter] filter is to filter only by services that are provided."
)
sealed class ServiceFilter(
    open val courier: Boolean = false,
    open val shipment: Boolean = false,
    open val pickup: Boolean = false
) {

    enum class ServiceType(val code: String, val text: String) {
        PICKUP(code = "10", text = "Handverkauf"),
        COURIER(code = "30", text = "Botendienst"),
        SHIPMENT(code = "40", text = "Versand")
    }

    // Common properties for FHIR VZD mapping
    val fhirVzdCourier: String? get() = if (courier) courierCode else null
    val fhirVzdShipment: String? get() = if (shipment) shipmentCode else null
    val fhirVzdPickup: String? get() = if (pickup) pickupCode else null

    // Abstract properties to be implemented by subclasses
    abstract val courierCode: String
    abstract val shipmentCode: String
    abstract val pickupCode: String

    // Generate text search string for FHIR VZD search
    fun buildTextSearch(additionalText: String? = null): String? {
        val serviceTexts = buildList {
            if (pickup) add(ServiceType.PICKUP.text)
            if (courier) add(ServiceType.COURIER.text)
            if (shipment) add(ServiceType.SHIPMENT.text)
        }

        return when {
            serviceTexts.isEmpty() && additionalText.isNullOrBlank() -> null
            serviceTexts.isEmpty() -> additionalText?.trim()
            additionalText.isNullOrBlank() -> serviceTexts.joinToString(" ")
            else -> "${serviceTexts.joinToString(" ")} ${additionalText.trim()}"
        }
    }

    companion object {
        /**
         * Creates the appropriate ServiceFilter implementation based on search context
         */
        fun create(
            courier: Boolean = false,
            shipment: Boolean = false,
            pickup: Boolean = false
        ): ServiceFilter = CodedServiceFilter(courier = courier, shipment = shipment, pickup = pickup)
    }
}

// https://simplifier.net/packages/de.gematik.fhir.directory/0.11.24/files/2723324
data class CodedServiceFilter(
    override val courier: Boolean = false, // (30)
    override val shipment: Boolean = false, // (40)
    override val pickup: Boolean = false // (10)
) : ServiceFilter(courier, shipment, pickup) {

    override val courierCode: String = ServiceType.COURIER.code
    override val shipmentCode: String = ServiceType.SHIPMENT.code
    override val pickupCode: String = ServiceType.PICKUP.code
}
