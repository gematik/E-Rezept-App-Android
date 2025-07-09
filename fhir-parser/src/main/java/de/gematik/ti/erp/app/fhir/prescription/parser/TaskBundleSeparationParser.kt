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

package de.gematik.ti.erp.app.fhir.prescription.parser

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirTaskPayloadErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundle
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta.Companion.getProfile
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.KBV_BUNDLE
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.KBV_BUNDLE_DEVICE_REQUEST
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.TASK
import de.gematik.ti.erp.app.fhir.constant.FhirVersions.KBV_DEVICE_REQUEST_VERSION_REGEX
import de.gematik.ti.erp.app.fhir.constant.FhirVersions.KBV_VERSION_REGEX
import de.gematik.ti.erp.app.fhir.constant.FhirVersions.TASK_VERSION_REGEX
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskMetaDataPayloadErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FirTaskKbvPayloadErpModel
import de.gematik.ti.erp.app.utils.letNotNull
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

/**
 * A utility class that extracts specific FHIR resources from a given JSON bundle.
 *
 * This mapper processes a FHIR bundle (`JsonElement`) and extracts:
 * - **Task Resource (`GEM_ERP_PR_Task`)**
 * - **KBV Bundle (`KBV_PR_ERP_Bundle`)**
 *
 * The extracted data is returned as a [FhirTaskPayloadErpModel], or `null` if required resources are missing.
 */
@Requirement(
    "O.Source_2#8",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = """
        This parser validates and sanitizes structured FHIR bundle input by:
            • Parsing the full task-bundle received via the task ID.
            • Separating it into `Task` and `KBV Bundle` parts based on profile version checks.
            • Accepting only entries matching valid profile versions [`GEM_ERP_PR_Task` and `KBV_PR_ERP_Bundle`] via strict matching against regex filters.
            • Ignoring or rejecting malformed resources or profiles outside of the specification scope.      
    """,
    codeLines = 66
)
class TaskBundleSeparationParser : BundleParser {
    /**
     * Extracts FHIR task and KBV bundle resources from a given JSON bundle.
     * This parses the data that we fetch using the task-id.
     *
     * Once the [TaskEntryParser] provides the task-id, the task-bundle is fetched from the server.
     * This is then fed to this parser which separates this into two parts called taskBundle and kbvBundle.
     * These parts are then further processed by [TaskEPrescriptionMetadataParser] and [TaskEPrescriptionMedicalDataParser] respectively.
     *
     * This function parses the `"entry.resource"` section of the JSON, identifies resources
     * based on their FHIR profile URLs, and returns a structured [FhirTaskPayloadErpModel].
     *
     * ### **Extraction Criteria**
     * - A **Task Resource** is identified by the profile URL:
     *   - `https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task`
     *   - Versions: `1.2`, `1.3`, `1.4`
     *   - **Note:** Versions `1.2` and `1.3` will be deprecated after **15 July 2025**.
     *
     * - A **KBV Bundle** is identified by the profile URL:
     *   - `https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle`
     *   - Versions: `1.0.2`, `1.1.0`
     *
     * @param bundle A [JsonElement] containing the FHIR bundle data.
     * @return A [FhirTaskPayloadErpModel] if both task and KBV bundle resources are found, otherwise `null`.
     * @throws kotlinx.serialization.json.JsonDecodingException if the JSON structure is invalid.
     *
     * ### **Example Usage**
     * ```kotlin
     * val bundleParser = TaskBundleSeparationParser()
     * val result = bundleParser.extract(jsonBundle)
     * if (result != null) {
     *     println("Task found: ${result.taskBundle}")
     *     println("KBV Bundle found: ${result.kbvBundle}")
     * } else {
     *     println("Required resources not found in the bundle.")
     * }
     * ```
     */
    override fun extract(bundle: JsonElement): FhirTaskPayloadErpModel? {
        try {
            val resources = extractResources(bundle)

            var taskBundle: JsonElement? = null
            var kbvBundle: JsonElement? = null

            for (resource in resources) {
                val profileValues = extractProfiles(resource)
                when {
                    isTaskResource(profileValues) -> taskBundle = resource // will be unwrapped by [TaskMetadataExtractor]
                    isKBVBundleResource(profileValues) -> kbvBundle = resource // will be unwrapped by [TaskKBVExtractor]
                }
            }

            return letNotNull(
                taskBundle,
                kbvBundle
            ) { task, kbv ->
                FhirTaskPayloadErpModel(
                    taskBundle = FhirTaskMetaDataPayloadErpModel(task),
                    kbvBundle = FirTaskKbvPayloadErpModel(kbv)
                )
            }
        } catch (e: Throwable) {
            Napier.e { "FHIR Parsing Error : parsing 'task-bundle' from resource: $bundle ${e.message}" }
            return null
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
    private fun extractResources(bundle: JsonElement): List<JsonElement> {
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
     * val profiles = extractProfiles(resource)
     * profiles.forEach { println(it) }
     * ```
     */
    private fun extractProfiles(resource: JsonElement): List<String> {
        val metaElement = resource.jsonObject["meta"] ?: return emptyList()

        return try {
            metaElement.getProfile() // Returns a List<String> directly
        } catch (e: Throwable) {
            Napier.e { "FHIR Parsing Error : parsing 'meta.profile' from resource: $resource ${e.message}" }
            emptyList() // Fallback in case of parsing errors
        }
    }

    private fun isFhirResource(profileValues: List<String>, resourceType: String, versionRegex: Regex): Boolean {
        return profileValues.any {
            it.startsWith(resourceType) && it.matches(versionRegex)
        }
    }

    /**
     * Checks if a resource belongs to the Task profile (`GEM_ERP_PR_Task`).
     *
     * @param profileValues List of profile URLs extracted from the resource.
     * @return `true` if the resource is a Task resource, otherwise `false`.
     */
    private fun isTaskResource(profileValues: List<String>): Boolean {
        val result = isFhirResource(profileValues, TASK, TASK_VERSION_REGEX)
        return result
    }

    /**
     * Checks if a resource belongs to the KBV Bundle profile (`KBV_PR_ERP_Bundle`).
     *
     * @param profileValues List of profile URLs extracted from the resource.
     * @return `true` if the resource is a KBV Bundle resource, otherwise `false`.
     */
    private fun isKBVBundleResource(profileValues: List<String>): Boolean {
        val result = isFhirResource(profileValues, KBV_BUNDLE, KBV_VERSION_REGEX)
        val resultDeviceRequest = isFhirResource(profileValues, KBV_BUNDLE_DEVICE_REQUEST, KBV_DEVICE_REQUEST_VERSION_REGEX)
        return result || resultDeviceRequest
    }
}
