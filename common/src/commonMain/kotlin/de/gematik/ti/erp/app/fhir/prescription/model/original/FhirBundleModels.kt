/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.fhir.prescription.model.original

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/*@Serializable
data class BundleEntry(
    val entry: List<BundleEntryResource>? = null
) {
    companion object {
        fun JsonElement.getBundleEntries() =
            SafeJson.value.decodeFromJsonElement(serializer(), this).entry
    }
}

@Serializable
data class BundleEntryResource(
    val resource: JsonElement? = null
)*/

@Serializable
data class FhirBundle(
    val entry: List<FhirEntry> = emptyList()
) {
    companion object {
        fun JsonElement.getBundleEntries() =
            SafeJson.value.decodeFromJsonElement(serializer(), this).entry
    }
}

@Serializable
data class FhirEntry(
    val resource: JsonElement
)

@Serializable
data class FhirBundleTaskData(
    val status: String,
    val lastModified: String?
) {
    companion object {
        fun JsonElement.getTaskData() =
            SafeJson.value.decodeFromJsonElement(serializer(), this)
    }
}
