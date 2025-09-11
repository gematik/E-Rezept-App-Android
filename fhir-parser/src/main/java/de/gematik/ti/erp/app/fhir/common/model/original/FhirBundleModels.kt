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

package de.gematik.ti.erp.app.fhir.common.model.original

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a FHIR `Bundle` resource, which acts as a container for a collection of FHIR resources.
 *
 * The `entry` field contains the list of individual FHIR resources wrapped in [FhirBundleEntry] objects.
 * This model is commonly used to handle responses from FHIR servers that return multiple resources.
 *
 * @property entry A list of FHIR resources contained in the bundle. Defaults to an empty list.
 */
@Serializable
internal data class FhirBundle(
    @SerialName("entry") val entry: List<FhirBundleEntry> = emptyList(),
    @SerialName("link") val link: List<FhirBundleLink> = emptyList()
) {
    companion object {
        /**
         * Safely decodes a [JsonElement] into a [FhirBundle] and extracts its [entry] list.
         *
         * This function is fail-safe: if the JSON cannot be decoded into a [FhirBundle],
         * it logs a warning and returns an empty list instead of throwing an exception.
         *
         * @receiver A raw JSON element representing a FHIR bundle.
         * @return A list of [FhirBundleEntry]s parsed from the bundle, or an empty list if parsing fails.
         */
        internal fun JsonElement.getBundleEntries(): List<FhirBundleEntry> {
            return runCatching {
                SafeJson.value.decodeFromJsonElement(serializer(), this).entry
            }
                .onFailure { Napier.e(tag = "fhir-parser", message = "Failed to parse input as a bundle ${it.message}") }
                .getOrElse { emptyList() }
        }

        internal fun JsonElement.getBundleLinks(): List<FhirBundleLink> {
            return runCatching {
                SafeJson.value.decodeFromJsonElement(serializer(), this).link
            }
                .onFailure { Napier.e(tag = "fhir-parser", message = "Failed to parse input as a bundle paging ${it.message}") }
                .getOrElse { emptyList() }
        }
    }
}

/**
 * Represents an individual entry in a FHIR [FhirBundle].
 *
 * Each entry typically wraps a FHIR resource. Since the resource can be of any type,
 * it is stored as a raw [JsonElement] to allow for flexible decoding based on context.
 *
 * @property resource The raw JSON element of the contained FHIR resource.
 */
@Serializable
internal data class FhirBundleEntry(
    @SerialName("resource") val resource: JsonElement,
    @SerialName("fullUrl") val fullUrl: String? = null
)

/**
 * Represents task-specific metadata typically found inside a FHIR bundle related to the `Task` resource.
 *
 * This data class is a lightweight representation used to extract key fields such as task status
 * and the last modification timestamp from a bundled FHIR response.
 *
 * @property status The current status of the task (e.g., "completed", "in-progress").
 * @property lastModified An optional ISO 8601 timestamp indicating when the task was last modified.
 */
@Serializable
internal data class FhirBundleTaskData(
    val status: String,
    val lastModified: String?
) {
    /**
     * Decodes the given [JsonElement] into a [FhirBundleTaskData] object.
     *
     * @receiver A raw JSON payload representing a FHIR Task resource inside a bundle.
     * @return A decoded [FhirBundleTaskData] instance.
     */
    companion object {
        fun JsonElement.getTaskData() =
            SafeJson.value.decodeFromJsonElement(serializer(), this)
    }
}
