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

import de.gematik.ti.erp.app.fhir.constant.FhirConstants
import de.gematik.ti.erp.app.fhir.constant.FhirVersions
import de.gematik.ti.erp.app.fhir.constant.FhirVersions.SupportedFhirKbvMetaProfileVersions
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirQuantityErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvAddressErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirRatioValue.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.serializer.SafeFhirAddressLineSerializer
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class FhirBundleMetaProfile(
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
        private fun JsonElement.getProfiles(): List<String> {
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
            return this.getProfiles().any { profile ->
                FhirVersions.TASK_ENTRY_PROFILE_REGEX.matchEntire(profile)?.groupValues?.get(1) in FhirVersions.SUPPORTED_TASK_ENTRY_PROFILE_VERSIONS
            }
        }

        fun JsonElement.containsExpectedProfileVersionForTaskKbvPhase(): SupportedFhirKbvMetaProfileVersions? {
            return this.getProfiles()
                .map { profile ->
                    FhirVersions.TASK_KBV_META_PROFILE_REGEX.matchEntire(profile)
                        ?.groupValues?.get(1)
                }.firstNotNullOfOrNull { matchedVersion ->
                    SupportedFhirKbvMetaProfileVersions.entries.find { it.version == matchedVersion }
                }
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
    }
}

@Serializable
data class FhirTaskResource(
    @SerialName("identifier") val identifiers: List<FhirIdentifier> = emptyList()
) {
    companion object {
        fun JsonElement.getResourceIdentifiers(): FhirTaskResource {
            return SafeJson.value.decodeFromJsonElement<FhirTaskResource>(serializer(), this)
        }
    }
}

@Serializable
data class FhirIdentifier(
    @SerialName("system") val system: String? = null,
    @SerialName("value") val value: String? = null,
    @SerialName("type") val type: FhirCodeableConcept? = null
) {
    companion object {

        private fun List<FhirIdentifier>.findIdentifierValue(system: String): String? {
            return this.firstOrNull { it.system == system }?.value?.takeIf { it.isNotBlank() }
        }

        fun List<FhirIdentifier>.findPrescriptionId(): String? {
            return findIdentifierValue(FhirConstants.PRESCRIPTION_ID_SYSTEM)
        }

        fun List<FhirIdentifier>.findAccessCode(): String? {
            return findIdentifierValue(FhirConstants.ACCESS_CODE_SYSTEM)
        }

        fun List<FhirIdentifier>.findPractitionerIdentifierValue(): String? {
            return findIdentifierValue(FhirConstants.PRACTITIONER_IDENTIFIER_NAME)
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
)

@Serializable
data class FhirCodeableConcept(
    @SerialName("coding") val coding: List<FhirCoding>? = emptyList(),
    @SerialName("text") val text: String? = null
)

@Serializable
data class FhirAddress(
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
                streetName = extractedLine["streetName"],
                houseNumber = extractedLine["houseNumber"],
                postalCode = this@toErpModel.postalCode,
                city = city
            )
        }
    }
}

@Serializable
data class FhirName(
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
data class FhirNameFamilyExtension(
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
data class FhirExtension(
    @SerialName("url") val url: String? = null,
    @SerialName("valueCoding") val valueCoding: FhirCoding? = null,
    @SerialName("valueCodeableConcept") val valueCodeableConcept: FhirCoding? = null,
    @SerialName("valueCode") val valueCode: String? = null,
    @SerialName("valueString") val valueString: String? = null,
    @SerialName("valueDate") val valueDate: String? = null,
    @SerialName("valueBoolean") val valueBoolean: Boolean? = null,
    @SerialName("valueRatio") val valueRatio: FhirRatio? = null,
    @SerialName("valuePeriod") val valuePeriod: FhirPeriod? = null,
    @SerialName("valueIdentifier") val valueIdentifier: FhirIdentifier? = null,
    @SerialName("extension") val extensions: List<FhirExtension> = emptyList()
) {
    companion object {
        fun List<FhirExtension>.findExtensionByUrl(url: String): FhirExtension? {
            return find { it.url?.lowercase() == url.lowercase() }
        }
    }
}

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

fun JsonElement.isValidKbvResource(expectedType: FhirKbvResourceType): Boolean {
    val resourceType = this.jsonObject["resourceType"]?.jsonPrimitive?.content
    return when (FhirKbvResourceType.entries.find { it.name.equals(resourceType, ignoreCase = true) }) {
        expectedType -> true
        else -> {
            Napier.w("Invalid resource type: Expected '${expectedType.name}', but found '$resourceType'")
            false
        }
    }
}

fun JsonElement.isResourceType(): Boolean {
    return (this as? JsonObject)?.containsKey("resourceType") == true
}
