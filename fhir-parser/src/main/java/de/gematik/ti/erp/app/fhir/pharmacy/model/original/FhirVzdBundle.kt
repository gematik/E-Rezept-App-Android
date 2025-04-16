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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
internal data class FhirVZDBundle(
    @SerialName("resourceType") val resourceType: String,
    @SerialName("id") val id: String,
    @SerialName("total") val numberOfEntries: Int,
    @SerialName("entry") val entries: List<FhirVZDEntry> = emptyList()
) {
    companion object {
        fun JsonElement.getBundle(): FhirVZDBundle {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }
    }
}

internal enum class FhirVzdResourceType {
    Organization,
    Location,
    HealthcareService,
    Endpoint,
    Unknown;
}

@Serializable
internal data class FhirVZDEntry(
    @SerialName("fullUrl") val fullUrl: String?,
    @SerialName("resource") val resource: JsonElement?
) {
    companion object {
        fun FhirVZDEntry.getResourceType(): FhirVzdResourceType {
            val resourceType = (this.resource as JsonObject)["resourceType"]?.jsonPrimitive?.contentOrNull
            return runCatching { FhirVzdResourceType.valueOf(resourceType.orEmpty()) }
                .getOrElse { FhirVzdResourceType.Unknown }
        }
    }
}
