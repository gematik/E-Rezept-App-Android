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

package de.gematik.ti.erp.app.fhir.common.model.original

import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdResourceType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

// TODO: Refactor duplicate of FhirResourceEntry
@Serializable
internal data class FhirFullUrlResourceEntry(
    @SerialName("fullUrl") val fullUrl: String?,
    @SerialName("resource") val resource: JsonElement?
) {
    companion object {
        fun FhirFullUrlResourceEntry.getFhirVzdResourceType(): FhirVzdResourceType {
            val resourceType = (this.resource as JsonObject)["resourceType"]?.jsonPrimitive?.contentOrNull
            return runCatching { FhirVzdResourceType.valueOf(resourceType.orEmpty()) }
                .getOrElse { FhirVzdResourceType.Unknown }
        }
    }
}
