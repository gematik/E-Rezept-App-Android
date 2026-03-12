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
 * It uses enums as the single source of truth for version management.
 * * **To add a new version:**
 * 1. Add the version to the appropriate enum (TaskVersion, KbvBundleVersion, etc.)
 * 2. All regexes and lists will auto-update from the enum
 */
object FhirVersions {

    /**
     * **Supported Task Versions**
     *
     * Single source of truth for **FHIR Task profile versions**.
     * * ⚠️ **Note:**
     * - Versions `1.2` and `1.3` will be **deprecated after 15 July 2025**.
     * - Ensure future updates comply with **GEMATIK guidelines**.
     * - Adding Meta profile version 1.5 (https://simplifier.net/packages/de.gematik.erezept-workflow.r4/1.5.0-rc1/~introduction)
     * * **To add a new version:** Simply add a new enum entry (e.g., `V_1_7("1.7")`)
     */
    enum class TaskVersion(val version: String) {
        V_1_2("1.2"),
        V_1_3("1.3"),
        V_1_4("1.4"),
        V_1_5("1.5"),
        V_1_6("1.6");

        val profileUrl: String
            get() = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task|$version"

        companion object {
            val all: List<String> = entries.map { it.version }
            val allProfileUrls: List<String> = entries.map { it.profileUrl }
        }
    }

    /**
     * **Supported KBV Bundle Versions**
     *
     * Single source of truth for **FHIR KBV PR ERP Bundle versions**.
     * * **To add a new version:** Add a new enum entry and include in `supported` list if needed
     */
    enum class KbvBundleVersion(val version: String) {
        V_1_0_2("1.0.2"),
        V_1_0_3("1.0.3"),
        V_1_1_0("1.1.0"),
        V_1_2("1.2"),
        V_1_3("1.3"),
        V_1_4("1.4"),
        UNKNOWN("");

        val profileUrl: String
            get() = "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|$version"

        companion object {
            val all: List<String> = entries.map { it.version }
            val supported: List<String> = listOf(
                V_1_0_2.version,
                V_1_1_0.version,
                V_1_3.version,
                V_1_4.version
            )
            val supportedProfileUrls: List<String> = entries
                .filter { it.version in supported }
                .map { it.profileUrl }
        }
    }

    /**
     * **Supported KBV Device Request Versions**
     *
     * Single source of truth for **KBV Device Request versions**.
     * * **To add a new version:** Add a new enum entry (e.g., `V_1_3("1.3")`)
     */
    enum class KbvDeviceRequestVersion(val version: String) {
        V_1_1("1.1"),
        V_1_2("1.2");

        val profileUrl: String
            get() = "https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_Bundle|$version"

        companion object {
            val all: List<String> = entries.map { it.version }
            val allProfileUrls: List<String> = entries.map { it.profileUrl }
        }
    }

    /**
     * **Task Version Regex Matcher**
     *
     * Auto-generated regex from `TaskVersion` enum.
     * Matches supported **FHIR Task** versions in profile URLs.
     */
    val TASK_VERSION_REGEX = Regex(""".*(${TaskVersion.all.joinToString("|")}).*""")

    /**
     * **KBV Version Regex Matcher**
     *
     * Auto-generated regex from `KbvBundleVersion.supported`.
     * Matches supported **FHIR KBV PR ERP Bundle** versions.
     */
    val KBV_VERSION_REGEX = Regex(""".*(${KbvBundleVersion.supported.joinToString("|")}).*""")

    /**
     * **KBV Device Request Version Regex Matcher**
     *
     * Auto-generated regex from `KbvDeviceRequestVersion` enum.
     */
    val KBV_DEVICE_REQUEST_VERSION_REGEX = Regex(""".*(${KbvDeviceRequestVersion.all.joinToString("|")}).*""")

    /**
     * **Supported Task Entry Profile Versions**
     *
     * Auto-generated list from `TaskVersion` enum.
     * Used for validating `Task.profile` values.
     */
    val SUPPORTED_TASK_ENTRY_PROFILE_VERSIONS: List<String> = TaskVersion.all

    /**
     * **Task Entry Profile Regex Matcher**
     *
     * Extracts and validates the **Task Entry Profile version** from FHIR data.
     * * **Example Matches:**
     * - `"https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task|1.2"` → Captures `"1.2"`
     */
    val TASK_ENTRY_PROFILE_REGEX = Regex("""https://gematik\.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task\|(\d+\.\d+)""")

    /**
     * **KBV Meta Profile Versions**
     *
     * Consolidated enums for KBV meta profile versions.
     */
    object KbvMetaProfileVersions {
        enum class SupportedFhirKbvMetaProfileVersions(val version: String) {
            V_102("1.0.2"),
            V_110("1.1.0"),
            V_12("1.2"),
            V_13("1.3"),
            V_14("1.4")
        }

        enum class SupportedFhirKbvMetaDeviceRequestProfileVersions(val version: String) {
            V_1_1_DEVICE_REQUEST("1.1"),
            V_1_2_DEVICE_REQUEST("1.2")
        }
    }

    val TASK_KBV_META_PROFILE_ERP_REGEX = Regex("""https://fhir\.kbv\.de/StructureDefinition/KBV_PR_ERP_Bundle\|(\d+\.\d+(?:\.\d+)?)""")
    val TASK_KBV_META_PROFILE_EVDGA_REGEX = Regex("""https://fhir\.kbv\.de/StructureDefinition/KBV_PR_EVDGA_Bundle(?:\|(\d+\.\d+(?:\.\d+)?))?""")

    val MEDICATION_DISPENSE_PROFILE_URLS: List<String> = FhirProfileUrls.MEDICATION_DISPENSE_PROFILE_URLS

    val CONSENT_PROFILE_URLS: List<String> = FhirProfileUrls.CONSENT_PROFILE_URLS

    val COMMUNICATION_PROFILE_URLS: List<String> = FhirProfileUrls.COMMUNICATION_PROFILE_URLS

    val COMMUNICATION_DIGA_PROFILE_URLS: List<String> = FhirProfileUrls.COMMUNICATION_DIGA_PROFILE_URLS

    val EUREDEEM_PROFILE_URLS: List<String> = FhirProfileUrls.EUREDEEM_PROFILE_URLS

    val MEDICATION_REQUEST_PROFILE_URLS: List<String> = FhirProfileUrls.MEDICATION_REQUEST_PROFILE_URLS
}
