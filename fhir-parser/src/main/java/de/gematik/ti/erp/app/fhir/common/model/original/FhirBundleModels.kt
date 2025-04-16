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

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FhirBundle(
    val entry: List<FhirEntry> = emptyList()
) {
    companion object {
        fun JsonElement.getBundleEntries(): List<FhirEntry> {
            return runCatching {
                SafeJson.value.decodeFromJsonElement(serializer(), this).entry
            }
                .onFailure { Napier.w("Failed to parse input as a bundle ${it.message}") }
                .getOrElse { emptyList() }
        }
    }
}

@Serializable
internal data class FhirEntry(
    val resource: JsonElement
)

@Serializable
internal data class FhirBundleTaskData(
    val status: String,
    val lastModified: String?
) {
    companion object {
        fun JsonElement.getTaskData() =
            SafeJson.value.decodeFromJsonElement(serializer(), this)
    }
}
