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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirRatioValue.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.constant.FhirConstants
import de.gematik.ti.erp.app.fhir.constant.FhirVersions
import de.gematik.ti.erp.app.fhir.constant.FhirVersions.SupportedFhirKbvMetaProfileVersions
import de.gematik.ti.erp.app.fhir.constant.FhirVersions.TASK_KBV_META_PROFILE_ERP_REGEX
import de.gematik.ti.erp.app.fhir.constant.FhirVersions.TASK_KBV_META_PROFILE_EVDGA_REGEX
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.constant.prescription.practitioner.FhirPractitionerConstants
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirKbvResourceType
import de.gematik.ti.erp.app.fhir.serializer.SafeFhirAddressLineSerializer
import de.gematik.ti.erp.app.fhir.support.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskKbvAddressErpModel
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
internal data class FhirBundleMetaProfile(
    @SerialName("profile") val profiles: List<String>?
) {
    companion object {
        /**
         * Extracts the profile list from a given `JsonElement` representing a FHIR `resource`.
         * If `meta` or `profile` is missing, it returns an empty list.
         *
         * @receiver The `JsonElement` representing a FHIR resource.
         * @return A list of profile URLs found in the `meta.profile` field.
         */
        internal fun JsonElement.getProfilesFromJson(): List<String> {
            return try {
                SafeJson.value.decodeFromJsonElement<FhirBundleMetaProfile>(
                    serializer(),
                    this.jsonObject["meta"] ?: return emptyList()
                ).profiles ?: emptyList()
            } catch (e: SerializationException) {
                Napier.e("Error parsing meta.profile: ${e.message}")
                emptyList()
            }
        }

        fun JsonElement.containsExpectedProfileVersionForTaskEntryPhase(): Boolean {
            return this.getProfilesFromJson().any { profile ->
                Napier.i(tag = "fhir-parser") { "profile version on entry $profile" }
                FhirVersions.TASK_ENTRY_PROFILE_REGEX.matchEntire(profile)
                    ?.groupValues?.get(1) in FhirVersions.SUPPORTED_TASK_ENTRY_PROFILE_VERSIONS
            }
        }

        /**
         * Checks whether the current [JsonElement] contains at least one known and supported KBV meta profile version
         * for a Task in the eRezept context.
         *
         * This method looks for profile URLs inside the FHIR resource's `meta.profile` field and determines if any of them
         * match the expected version patterns for either:
         * - **ERP bundles** (e.g., `https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2`), or
         * - **EVDGA bundles** (used when `DeviceRequest` is present, e.g., `https://fhir.kbv.de/StructureDefinition/KBV_PR_EVDGA_Bundle|1.1`)
         *
         * It separately checks:
         * - Non-DeviceRequest-related profiles against a list of known `SupportedFhirKbvMetaProfileVersions`.
         * - DeviceRequest-related profiles against a list of known `SupportedFhirKbvMetaDeviceRequestProfileVersions`.
         *
         * @return `true` if at least one matching and supported profile version is found; `false` otherwise.
         */
        fun JsonElement.containsExpectedProfileVersionForTaskKbvPhase(): Boolean {
            // profiles which don't have device request follow this rule
            val nonDeviceRequestMatches = this.getProfilesFromJson()
                .map { profile ->
                    TASK_KBV_META_PROFILE_ERP_REGEX.matchEntire(profile)
                        ?.groupValues?.get(1)
                }.firstNotNullOfOrNull { matchedVersion ->
                    SupportedFhirKbvMetaProfileVersions.entries.find { it.version == matchedVersion }
                }

            // profiles which have device request follow this rule
            val deviceRequestMatches = this.getProfilesFromJson()
                .map { profile ->
                    TASK_KBV_META_PROFILE_EVDGA_REGEX.matchEntire(profile)
                        ?.groupValues?.get(1)
                }.firstNotNullOfOrNull { matchedVersion ->
                    FhirVersions.SupportedFhirKbvMetaDeviceRequestProfileVersions.entries.find { it.version == matchedVersion }
                }

            return nonDeviceRequestMatches != null || deviceRequestMatches != null
        }
    }
}

@Serializable
data class FhirMeta(
    @SerialName("profile") val profiles: List<String> = emptyList()
) {
    companion object {
        fun JsonElement.getProfile() =
            SafeJson.value.decodeFromJsonElement(serializer(), this).profiles

        /**
         * Splits a profile string into its canonical URL and optional version component.
         *
         * Expected format: `url|version`. If no version is present, the second value will be `null`.
         *
         * @return a [Pair] of (url, version).
         */
        internal fun String.toProfile(): Pair<String, String?> {
            val parts = split('|', limit = 2)
            return parts[0] to parts.getOrNull(1) // url to optional version
        }

        internal fun (FhirMeta?).byProfile(
            url: String,
            version: String? = null
        ): Boolean = this?.profiles.orEmpty()
            .map { it.toProfile() }
            .any { (u, v) -> u == url && (version == null || v == version) }
    }
}

/**
 * A lightweight model representing a FHIR Task resource that contains a list of identifiers.
 *
 * This class is primarily used for extracting identifier values from a raw FHIR JSON element.
 *
 * @property identifiers A list of [FhirIdentifier] objects extracted from the "identifier" field in the resource.
 */
@Serializable
internal data class FhirTaskResource(
    @SerialName("identifier") val identifiers: List<FhirIdentifier> = emptyList()
) {
    companion object {
        /**
         * Parses a [JsonElement] into a [FhirTaskResource] to extract task-level identifiers.
         *
         * This function uses [SafeJson] for deserialization and assumes the element represents a valid
         * FHIR resource containing an "identifier" field.
         *
         * @receiver The raw JSON element representing a FHIR Task resource.
         * @return A [FhirTaskResource] instance with populated identifier list.
         * @throws SerializationException If the JSON structure doesn't match [FhirTaskResource].
         */
        fun JsonElement.getResourceIdentifiers(): FhirTaskResource {
            return SafeJson.value.decodeFromJsonElement<FhirTaskResource>(serializer(), this)
        }
    }
}

/**
 * Represents a FHIR-compliant identifier object used across various FHIR resources.
 *
 * An identifier typically includes the system that defines it (e.g., a URI), its actual value,
 * and optionally a coded concept that categorizes the type of identifier.
 *
 * @property system The system URI that defines the identifier namespace (e.g., KVNR, Telematik).
 * @property value The actual identifier value assigned by the system.
 * @property type An optional [FhirCodeableConcept] describing the nature of the identifier.
 */
// NOTE: Also used in communication dispense
@Serializable
data class FhirIdentifier(
    @SerialName("system") val system: String? = null,
    @SerialName("value") val value: String? = null,
    @SerialName("type") val type: FhirCodeableConcept? = null
) {
    companion object {

        /**
         * Finds the first identifier with the matching [system] and returns its non-blank value.
         *
         * @receiver List of [FhirIdentifier] to search through.
         * @param system The identifier system URI to match.
         * @return The corresponding identifier value, or `null` if not found or blank.
         */
        internal fun List<FhirIdentifier>.findIdentifierFromSystemUrl(system: String): String? {
            return this.firstOrNull { it.system == system }?.value?.takeIf { it.isNotBlank() }
        }

        /**
         * Searches for the identifier that represents a Prescription ID.
         *
         * @receiver List of [FhirIdentifier] objects.
         * @return The Prescription ID value, or `null` if not present.
         */
        fun List<FhirIdentifier>.findPrescriptionId(): String? {
            return findIdentifierFromSystemUrl(FhirConstants.PRESCRIPTION_ID_SYSTEM)
        }

        /**
         * Searches for the identifier that represents an Access Code.
         *
         * @receiver List of [FhirIdentifier] objects.
         * @return The Access Code value, or `null` if not present.
         */
        fun List<FhirIdentifier>.findAccessCode(): String? {
            return findIdentifierFromSystemUrl(FhirConstants.ACCESS_CODE_SYSTEM)
        }

        /**
         * Searches for the identifier that represents a Lifelong doctor number.
         *
         * @receiver List of [FhirIdentifier] objects.
         * @return The Lifelong doctor number, or `null` if not present.
         */
        fun List<FhirIdentifier>.findPractitionerLanr(): String? {
            return findIdentifierFromSystemUrl(FhirPractitionerConstants.PRACTITIONER_IDENTIFIER_LANR)
        }

        /**
         * Searches for the identifier that represents a dentist number.
         *
         * @receiver List of [FhirIdentifier] objects.
         * @return The dentist number, or `null` if not present.
         */
        fun List<FhirIdentifier>.findPractitionerZanr(): String? {
            return findIdentifierFromSystemUrl(FhirPractitionerConstants.PRACTITIONER_IDENTIFIER_ZANR)
        }

        /**
         * Searches for the identifier that represents a doctor's office.
         *
         * @receiver List of [FhirIdentifier] objects.
         * @return The telematik id, or `null` if not present.
         */
        fun List<FhirIdentifier>.findPractitionerTelematikId(): String? {
            return findIdentifierFromSystemUrl(FhirPractitionerConstants.PRACTITIONER_TELEMATIK_ID)
        }
    }
}

@Serializable
data class FhirCoding(
    @SerialName("coding") val coding: List<FhirCoding>? = emptyList(),
    @SerialName("system") val system: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("version") val version: String? = null,
    @SerialName("display") val display: String? = null
) {
    companion object {
        internal fun List<FhirCoding>.firstBySystem(systems: Collection<String>): FhirCoding? =
            firstOrNull { it.system in systems }
    }
}

@Serializable
data class FhirCodeableConcept(
    @SerialName("coding") val coding: List<FhirCoding>? = emptyList(),
    @SerialName("text") val text: String? = null
) {
    companion object {
        fun FhirCodeableConcept.getCodingByUrl(url: String) = coding?.find { it.system == url }
    }
}

@Serializable
internal data class FhirAddress(
    @SerialName("type") val type: String? = null,
    @SerialName("line") val line: List<String>? = emptyList(),
    @SerialName("city") val city: String? = null,
    @SerialName("postalCode") val postalCode: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("_line") @Serializable(with = SafeFhirAddressLineSerializer::class)
    val extractedLine: Map<String, String> = emptyMap() // Extracts multiple values from `_line`
) {
    companion object {
        fun FhirAddress.toErpModel(): FhirTaskKbvAddressErpModel {
            return FhirTaskKbvAddressErpModel(
                streetName = extractedLine.value("streetName"),
                houseNumber = extractedLine.value("houseNumber"),
                additionalAddressInformation = extractedLine.value("additionalLocator"),
                postalCode = this@toErpModel.postalCode,
                city = city
            )
        }

        private fun Map<String, String>.value(key: String): String? {
            return this[key] ?: this.entries
                .firstOrNull { it.key.contains(key, ignoreCase = true) }
                ?.value
        }
    }
}

@Serializable
internal data class FhirName(
    @SerialName("use") val use: String? = null,
    @SerialName("family") val family: String? = null,
    @SerialName("_family") val familyExtension: FhirNameFamilyExtension? = null,
    @SerialName("given") val given: List<String>? = emptyList(),
    @SerialName("prefix") val prefix: List<String>? = emptyList()
) {
    companion object {
        fun FhirName.processName(): String {
            val givenName = given?.joinToString(" ")
            val prefixName = prefix?.joinToString(" ")
            return listOfNotNull(prefixName, givenName, family).joinToString(" ").trim()
        }
    }
}

@Serializable
internal data class FhirNameFamilyExtension(
    @SerialName("extension") val extensions: List<FhirExtensionReduced>? = emptyList()
)

@Serializable
data class FhirExtensionReduced(
    @SerialName("url") val url: String? = null,
    @SerialName("valueString") val valueString: String? = null,
    @SerialName("valueCode") val valueCode: String? = null
) {
    companion object {
        fun List<FhirExtensionReduced>.findExtensionByUrl(url: String): FhirExtensionReduced? {
            return find { it.url?.lowercase() == url.lowercase() }
        }
    }
}

@Serializable
internal data class FhirExtension(
    @SerialName("url") val url: String? = null,
    @SerialName("valueCoding") val valueCoding: FhirCoding? = null,
    @SerialName("valueCodeableConcept") val valueCodeableConcept: FhirCoding? = null,
    @SerialName("valueCode") val valueCode: String? = null,
    @SerialName("valueString") val valueString: String? = null,
    @SerialName("valuePositiveInt") val valuePositiveInt: String? = null,
    @SerialName("valueUrl") val valueUrl: String? = null,
    @SerialName("valueDate") val valueDate: String? = null,
    @SerialName("valueMoney") val valueMoney: FhirMoney? = null,
    @SerialName("valueDecimal") val valueDecimal: String? = null,
    @SerialName("valueBoolean") val valueBoolean: Boolean? = null,
    @SerialName("valueRatio") val valueRatio: FhirRatio? = null,
    @SerialName("valuePeriod") val valuePeriod: FhirPeriod? = null,
    @SerialName("valueIdentifier") val valueIdentifier: FhirIdentifier? = null,
    @SerialName("valueReference") val valueReference: FhirReference? = null,
    @SerialName("extension") val extensions: List<FhirExtension> = emptyList()
) {
    companion object {
        fun List<FhirExtension>.findExtensionByUrl(url: String): FhirExtension? {
            return find { it.url?.lowercase() == url.lowercase() }
        }
    }
}

@Serializable
data class FhirParameter(
    @SerialName("name") val name: String? = null,
    @SerialName("valueCoding") val valueCoding: FhirCoding? = null,
    @SerialName("valueCodeableConcept") val valueCodeableConcept: FhirCoding? = null,
    @SerialName("valueCode") val valueCode: String? = null,
    @SerialName("valueString") val valueString: String? = null,
    @SerialName("valuePositiveInt") val valuePositiveInt: String? = null,
    @SerialName("valueUrl") val valueUrl: String? = null,
    @SerialName("valueDate") val valueDate: String? = null,
    @SerialName("valueMoney") val valueMoney: FhirMoney? = null,
    @SerialName("valueDecimal") val valueDecimal: String? = null,
    @SerialName("valueBoolean") val valueBoolean: Boolean? = null,
    @SerialName("valueRatio") val valueRatio: FhirRatio? = null,
    @SerialName("valuePeriod") val valuePeriod: FhirPeriod? = null,
    @SerialName("valueIdentifier") val valueIdentifier: FhirIdentifier? = null,
    @SerialName("valueReference") val valueReference: FhirReference? = null,
    @SerialName("valueInstant") val valueInstant: String? = null
) {
    companion object {
        fun List<FhirParameter>.findParameterByName(name: String): FhirParameter? {
            return find { it.name?.lowercase() == name.lowercase() }
        }
    }
}

@Serializable
data class FhirMoney(
    @SerialName("value") val value: String? = null,
    @SerialName("currency") val unit: String? = null
)

@Serializable
data class FhirReference(
    @SerialName("reference") val value: String? = null
)

@Serializable
data class FhirRatio(
    @SerialName("extension") val extensions: List<FhirExtensionReduced> = emptyList(),
    @SerialName("numerator") val numerator: FhirRatioValue? = null,
    @SerialName("denominator") val denominator: FhirRatioValue? = null
) {
    companion object {
        fun FhirRatio.toErpModel(): FhirRatioErpModel {
            return FhirRatioErpModel(
                numerator = numerator?.toErpModel(),
                denominator = denominator?.toErpModel()
            )
        }
    }
}

@Serializable
data class FhirRatioValue(
    @SerialName("value") val value: String? = null,
    @SerialName("unit") val unit: String? = null
) {
    companion object {
        fun FhirRatioValue.toErpModel(): FhirQuantityErpModel {
            return FhirQuantityErpModel(
                value = value,
                unit = unit
            )
        }
    }
}

@Serializable
data class FhirPeriod(
    @SerialName("start") val start: String? = null,
    @SerialName("end") val end: String? = null
)

/**
 * Validates if the JSON element represents a KBV FHIR resource of the expected type.
 *
 * This function checks whether the `resourceType` field of the [JsonElement] matches
 * the expected [FhirKbvResourceType]. If it does not match, a warning is logged.
 *
 * @param expectedType The expected type of the FHIR KBV resource.
 * @return `true` if the resource type matches the expected type, `false` otherwise.
 */
fun JsonElement.isValidKbvResource(expectedType: FhirKbvResourceType): Boolean {
    val resourceType = this.jsonObject["resourceType"]?.jsonPrimitive?.content
    return when (
        FhirKbvResourceType.entries.find {
            it.name.equals(
                resourceType,
                ignoreCase = true
            )
        }
    ) {
        expectedType -> true
        else -> {
            Napier.w("Invalid resource type: Expected '${expectedType.name}', but found '$resourceType'")
            false
        }
    }
}

/**
 * Checks whether this [JsonElement] contains a valid FHIR `resourceType` field.
 *
 * This is a lightweight validation to ensure the element can be interpreted as a FHIR resource.
 *
 * @return `true` if the `resourceType` field exists, `false` otherwise.
 */
fun JsonElement.isResourceType(): Boolean {
    return (this as? JsonObject)?.containsKey("resourceType") == true
}
