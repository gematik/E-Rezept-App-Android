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

package de.gematik.ti.erp.app.fhir.pharmacy.model.original

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPositionErpModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FhirVZDLocation(
    @SerialName("resourceType") val resourceType: String,
    @SerialName("id") val id: String?,
    @SerialName("address") val address: FhirVzdAddress?,
    @SerialName("identifier") val identifiers: List<FhirVzdIdentifier> = emptyList(),
    @SerialName("position") val position: FhirVzdPosition?
) {
    companion object {
        fun JsonElement.getLocation(): FhirVZDLocation {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }
    }
}

@Serializable
internal data class FhirVzdPosition(
    @SerialName("longitude") val longitude: Double,
    @SerialName("latitude") val latitude: Double
) {
    companion object {
        fun FhirVzdPosition.toErpModel(): FhirPositionErpModel {
            return FhirPositionErpModel(latitude, longitude)
        }
    }
}
