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

package de.gematik.ti.erp.app.fhir.constant

/**
 * **FHIR Version Management**
 *
 * This object defines the supported **FHIR Task and KBV Bundle versions** for compatibility checking.
 * It includes version lists and regex patterns for identifying supported FHIR resources.
 */
object FhirVersions {

    /**
     * **Supported Task Versions**
     *
     * A list of **supported FHIR Task profile versions**.
     * These versions determine which FHIR Task resources are accepted for processing.
     *
     * ⚠️ **Note:**
     * - Versions `1.2` and `1.3` will be **deprecated after 15 July 2025**.
     * - Ensure future updates comply with **GEMATIK guidelines**.
     */
    private val SUPPORTED_TASK_VERSIONS = listOf("1.2", "1.3", "1.4") // TODO: Remove versions 1.2, 1.3 after 15 Jul 2025

    /**
     * **Supported KBV Bundle Versions**
     *
     * A list of **supported FHIR KBV PR ERP Bundle versions**.
     * These versions determine which KBV-compliant prescription bundles are accepted.
     */
    private val SUPPORTED_KBV_VERSIONS = listOf("1.0.2", "1.1.0")
    private val SUPPORTED_KBV_DEVICE_REQUEST_VERSIONS = listOf("1.1")

    /**
     * **Task Version Regex Matcher**
     *
     * A **regular expression (regex)** that matches supported **FHIR Task** versions.
     * This dynamically builds a regex from `SUPPORTED_TASK_VERSIONS` to validate the **Task profile version** in FHIR data.
     *
     * **Example Match:**
     * - If `SUPPORTED_TASK_VERSIONS = ["1.2", "1.3", "1.4"]`
     * - The generated regex pattern will be: `".*(1.2|1.3|1.4).*"`
     * - It will match versions like `"https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task|1.3"`
     */
    val TASK_VERSION_REGEX = Regex(""".*(${SUPPORTED_TASK_VERSIONS.joinToString("|")}).*""")

    /**
     * **KBV Version Regex Matcher**
     *
     * A **regular expression (regex)** that matches supported **FHIR KBV PR ERP Bundle** versions.
     * This dynamically builds a regex from `SUPPORTED_KBV_VERSIONS` to validate the **KBV profile version** in FHIR data.
     *
     * **Example Match:**
     * - If `SUPPORTED_KBV_VERSIONS = ["1.0.2", "1.1.0"]`
     * - The generated regex pattern will be: `".*(1.0.2|1.1.0).*"`
     * - It will match versions like `"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2"`
     */
    val KBV_VERSION_REGEX = Regex(""".*(${SUPPORTED_KBV_VERSIONS.joinToString("|")}).*""")
    val KBV_DEVICE_REQUEST_VERSION_REGEX = Regex(""".*(${SUPPORTED_KBV_DEVICE_REQUEST_VERSIONS.joinToString("|")}).*""")

    /**
     * **Supported Task Entry Profile Versions**
     *
     * A list of supported **FHIR Task Entry Profile versions**.
     * These versions define the allowed **Task profile structures** in FHIR.
     *
     * **Usage:**
     * - Used in regex matching to validate `Task.profile` values.
     *
     * **Example:**
     * ```
     * SUPPORTED_TASK_ENTRY_PROFILE_VERSIONS = ["1.2", "1.3", "1.4"]
     * ```
     * - A profile like `"https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task|1.3"` is considered valid.
     */

    enum class SupportedFhirTaskEntryProfileVersions(val version: String) {
        V_1_2("1.2"),
        V_1_3("1.3"),
        V_1_4("1.4")
    }

    val SUPPORTED_TASK_ENTRY_PROFILE_VERSIONS = SupportedFhirTaskEntryProfileVersions.entries.map { it.version }

    /**
     * **Task Entry Profile Regex Matcher**
     *
     * A **regular expression (regex)** that extracts and validates the **Task Entry Profile version** from FHIR data.
     * This is used to check if a given **Task profile URL** contains a supported version.
     *
     * **Regex Pattern:**
     * - `"https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task\|(\d+\.\d+)"`
     * - Captures the version number after `GEM_ERP_PR_Task|`
     *
     * **Example Matches:**
     * - `"https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task|1.2"` → Captures `"1.2"`
     * - `"https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task|1.3"` → Captures `"1.3"`
     *
     * **Usage:**
     * - Can be used to extract and validate **Task profile versions** in FHIR processing.
     */
    val TASK_ENTRY_PROFILE_REGEX = Regex("""https://gematik\.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task\|(\d+\.\d+)""")

    enum class SupportedFhirKbvMetaProfileVersions(val version: String) {
        V_102("1.0.2"),
        V_110("1.1.0")
    }

    enum class SupportedFhirKbvMetaDeviceRequestProfileVersions(val version: String) { V_1_1_DEVICE_REQUEST("1.1") }

    // ERP bundle data
    val TASK_KBV_META_PROFILE_ERP_REGEX = Regex("""https://fhir\.kbv\.de/StructureDefinition/KBV_PR_ERP_Bundle\|(\d+\.\d+\.\d+)""")

    // ERP Device request bundle data
    val TASK_KBV_META_PROFILE_EVDGA_REGEX = Regex("""https://fhir\.kbv\.de/StructureDefinition/KBV_PR_EVDGA_Bundle(?:\|(\d+\.\d+(?:\.\d+)?))?""")

    const val KBV_BUNDLE_VERSION_103 = "1.0.3"
    const val KBV_BUNDLE_VERSION_110 = "1.1.0"
}
