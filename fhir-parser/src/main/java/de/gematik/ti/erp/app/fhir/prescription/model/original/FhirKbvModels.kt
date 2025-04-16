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

package de.gematik.ti.erp.app.fhir.prescription.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
internal data class FhirKbvBundle(
    @SerialName("entry") val entries: List<FhirKbvEntry> = emptyList()
) {
    companion object {
        fun JsonElement.parseFhirKbvBundle(): List<FhirKbvEntry> {
            return try {
                SafeJson.value.decodeFromJsonElement<FhirKbvBundle>(serializer(), this).entries
            } catch (e: Exception) {
                println("Error parsing FHIR Bundle: ${e.message}")
                emptyList()
            }
        }
    }
}

@Serializable
internal data class FhirKbvEntry(
    @SerialName("fullUrl") val fullUrl: String? = null,
    @SerialName("resource") val resource: JsonElement
) {
    private val resourceTypeValue = "resourceType"
    val resourceType: FhirKbvResourceType?
        get() {
            val type = resource.jsonObject[resourceTypeValue]?.jsonPrimitive?.content
            return type?.let {
                FhirKbvResourceType.entries.find { enumItem ->
                    enumItem.name.equals(it, ignoreCase = true)
                }
            }
        }

    // https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3 returns 1.0.3
    // https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.1.0 returns 1.1.0
    val version: String?
        get() {
            val versionRegex = Regex("""\|(\d+\.\d+\.\d+)""")
            val profiles = resource.jsonObject["meta"]?.let { metaElement ->
                SafeJson.value.decodeFromJsonElement(FhirMeta.serializer(), metaElement).profiles
            }
            return profiles?.firstNotNullOfOrNull {
                it.let { versionRegex.find(it)?.groupValues?.get(1) }
            }
        }
}

// mapped exactly from fhir "resourceType" in [FhirKbvEntry]
enum class FhirKbvResourceType {
    Composition,
    MedicationRequest,
    Medication,
    Patient,
    PractitionerRole,
    Practitioner,
    Organization,
    Coverage,
    DeviceRequest;
}
