/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.fhir.pharmacy.model.original

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.ConnectionType.Companion.getConnectionType
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdIdentifier.Companion.getTelematikId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// required only when we activate zuweisung-ohne-telematik-id
@Serializable
internal data class FhirVzdEndpoint(
    @SerialName("resourceType") val resourceType: String,
    @SerialName("name") val name: String,
    @SerialName("address") val url: String,
    @SerialName("identifier") val identifiers: List<FhirVzdIdentifier> = emptyList()
) {
    val telematikId: String
        get() = identifiers.getTelematikId().orEmpty()

    val connectionType: ConnectionType
        get() = getConnectionType(name)

    companion object {
        fun JsonElement.getEndpoint(): FhirVzdEndpoint {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }

        fun List<FhirVzdEndpoint>.filterByType(): List<FhirVzdEndpoint> {
            return filter { it.name.isNotEmpty() }
                .filter { it.connectionType in ConnectionType.validConnectionTypes() }
        }
    }
}

// https://gematik.de/fhir/directory/CodeSystem/EndpointDirectoryConnectionType
internal sealed class ConnectionType(open val value: String) {
    data object OnPremise : ConnectionType("eRP-onPremise")
    data object Delivery : ConnectionType("eRP-delivery")
    data object Shipment : ConnectionType("eRP-shipment")
    data class Others(override val value: String) : ConnectionType(value)

    companion object {

        fun validConnectionTypes(): Set<ConnectionType> = setOf(OnPremise, Delivery, Shipment)

        fun getConnectionType(value: String): ConnectionType {
            return when (value) {
                OnPremise.value -> OnPremise
                Delivery.value -> Delivery
                Shipment.value -> Shipment
                else -> Others(value)
            }
        }
    }
}
