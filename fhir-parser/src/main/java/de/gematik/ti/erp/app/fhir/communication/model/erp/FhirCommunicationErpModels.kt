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

package de.gematik.ti.erp.app.fhir.communication.model.erp

import kotlinx.serialization.Serializable

@Serializable
data class CommunicationParticipantErpModel(
    val identifier: String? = null,
    val identifierSystem: String? = null
)

// For communication reply ERP model
@Serializable
data class ReplyCommunicationPayloadContentErpModel(
    val text: String? = null,
    val supplyOptions: ReplyCommunicationSupplyOptionsErpModel? = null
)

@Serializable
data class ReplyCommunicationSupplyOptionsErpModel(
    val onPremise: Boolean = false,
    val shipment: Boolean = false,
    val delivery: Boolean = false
)

// For communication Dispense ERP model
@Serializable
data class DispenseCommunicationPayloadContentErpModel(
    val contentString: String? = null,
    val supplyOptionsType: DispenseSupplyOptionsType = DispenseSupplyOptionsType.UNKNOWN,
    val name: String? = null,
    val address: List<String>? = null,
    val phone: String? = null
)

@Serializable
enum class DispenseSupplyOptionsType(val value: String) {
    ON_PREMISE("onPremise"),
    SHIPMENT("shipment"),
    DELIVERY("delivery"),
    UNKNOWN("");

    companion object {
        fun fromString(value: String?): DispenseSupplyOptionsType {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}

@Serializable
data class DispensePrescriptionTypeErpModel(
    val code: String?,
    val system: String?,
    val display: String?
)
