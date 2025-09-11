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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta.Companion.getProfile
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Extracts the `"profile"` values from the `"meta"` section of a FHIR resource.
 *
 * This function retrieves the `"meta"` object from a FHIR resource and extracts the list of
 * profile URLs. If `"meta"` is missing, it returns an empty list.
 *
 * ### **Processing Steps:**
 * 1. Retrieves the `"meta"` field from the JSON resource.
 * 2. Uses Kotlinx Serialization to parse it into a [FhirMeta] object.
 * 3. Returns the extracted list of profile URLs.
 *
 * @param resource The [JsonElement] representing a FHIR resource.
 * @return A list of profile URLs (`List<String>`) found in the `"meta.profile"` field.
 * Returns an empty list if `"meta.profile"` is missing or malformed.
 *
 * ### **Error Handling**
 * - If `"meta.profile"` is missing, the function returns `emptyList()`.
 * - If JSON parsing fails, the error is logged using [Napier].
 *
 * ### **Example Usage**
 * ```kotlin
 * val resource: JsonElement = getResourceJson()
 * val profiles = extractProfilesFromResourceMeta(resource)
 * profiles.forEach { println(it) }
 * ```
 */
internal fun extractProfilesFromResourceMeta(resource: JsonElement): List<String> {
    val metaElement = resource.jsonObject["meta"] ?: return emptyList()

    return try {
        metaElement.getProfile() // Returns a List<String> directly
    } catch (e: Throwable) {
        Napier.e { "FHIR Parsing Error : parsing 'meta.profile' from resource: $resource ${e.message}" }
        emptyList() // Fallback in case of parsing errors
    }
}

/**
 * Extracts all resources from a given FHIR bundle JSON.
 *
 * This method deserializes the input JSON into a [FhirBundle] and retrieves the list of
 * contained FHIR resources.
 *
 * ### **Processing Steps**
 *
 * 1. Parses the JSON bundle into a [FhirBundle] using Kotlinx Serialization.
 * 2. Extracts all `"resource"` elements from the `"entry"` array.
 *
 * @param bundle The [JsonElement] representing the FHIR bundle.
 * @return A list of [JsonElement]s representing the extracted resources.
 * @throws kotlinx.serialization.json.JsonDecodingException If the JSON format is invalid.
 *
 * ### **Example Usage**
 *
 * ```kotlin
 * val bundle: JsonElement = Json.parseToJsonElement(sampleJsonString)
 * val resources = extractResources(bundle)
 * resources.forEach { println(it) }
 * ```
 */
internal fun extractResources(bundle: JsonElement): List<JsonElement> {
    try {
        // Deserialize JSON into `FhirBundle`
        val fhirBundle = SafeJson.value.decodeFromJsonElement<FhirBundle>(FhirBundle.serializer(), bundle)

        // Extract all resources
        return fhirBundle.entry.map { it.resource }
    } catch (e: Throwable) {
        Napier.e { "FHIR Parsing Error : parsing 'task-bundle' from resource: $bundle ${e.message}" }
        return emptyList()
    }
}
