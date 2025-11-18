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

package de.gematik.ti.erp.app.fhir.prescription.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findPractitionerLanr
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findPractitionerTelematikId
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findPractitionerZanr
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirName
import de.gematik.ti.erp.app.fhir.common.model.original.FhirName.Companion.processName
import de.gematik.ti.erp.app.fhir.common.model.original.isResourceType
import de.gematik.ti.erp.app.fhir.common.model.original.isValidKbvResource
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPractitionerErpModel
import de.gematik.ti.erp.app.utils.Reference
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FhirResourceId(
    val id: String? = null
) {
    companion object {
        /**
         * Pass a resource bundle and get the resource ID
         */
        fun JsonElement.getResourceId(): String? {
            if (!isResourceType()) return null.also { Napier.w { "$this is not a resource type" } }
            return try {
                SafeJson.value.decodeFromJsonElement<FhirResourceId>(serializer(), this).id
            } catch (e: Exception) {
                Napier.w { "Error parsing FHIR resource ID: ${e.message}" }
                null
            }
        }
    }
}

@Serializable
internal data class FhirAuthorBundle(
    @SerialName("entry") val entries: List<FhirAuthorResource> = emptyList()
) {
    companion object {
        fun JsonElement.getAuthorReferences(): List<FhirAuthor?> {
            val bundle = SafeJson.value.decodeFromJsonElement(serializer(), this)
            return bundle.entries.flatMap { entry ->
                entry.resource?.authors?.map { author ->
                    author
                } ?: emptyList()
            }
        }
    }
}

@Serializable
internal data class FhirAuthorResource(
    val resource: FhirAuthorEntry? = null
)

@Serializable
internal data class FhirAuthorEntry(
    @SerialName("author") val authors: List<FhirAuthor> = emptyList()
)

@Serializable
internal data class FhirAuthor(
    val reference: String? = null,
    val type: String? = null
) {
    companion object {
        fun List<FhirAuthor?>.findAuthorReferenceByType(type: String): String? {
            return filter { it?.type == type }
                .firstNotNullOfOrNull { it?.reference?.substringAfterLast("/") }
        }
    }
}

@Reference(
    info = "Practitioner version 1.2.0",
    url = "https://simplifier.net/packages/kbv.ita.for/1.2.0/files/2777638"
)
@Serializable
internal data class FhirPractitioner(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("identifier") val identifiers: List<FhirIdentifier> = emptyList(),
    @SerialName("qualification") val qualifications: List<FhirQualificationCode> = emptyList(),
    @SerialName("name") val names: List<FhirName> = emptyList()
) {
    companion object {
        private fun JsonElement.isValidPractitioner(): Boolean = isValidKbvResource(
            FhirKbvResourceType.Practitioner
        )

        fun JsonElement.getPractitioner(): FhirPractitioner? {
            if (!isValidPractitioner()) return null
            return SafeJson.value.decodeFromJsonElement<FhirPractitioner>(serializer(), this)
        }

        /**
         * Extracts the qualification text from the FHIR Practitioner resource, if available.
         *
         * This function looks for the first qualification entry that has a non-null `code.text` field.
         * If an error occurs during access or parsing, it logs the error and returns `null`.
         *
         * @return The qualification text (e.g., "Hausarzt") if present, otherwise `null`.
         */
        private fun FhirPractitioner.getQualification(): String? {
            return try {
                qualifications.find { it.code?.text != null }?.code?.text
            } catch (e: Exception) {
                Napier.e("Error parsing FHIR Practitioner Qualification: ${e.message}")
                null
            }
        }

        /**
         * Retrieves and processes the practitioner's name from the FHIR Practitioner resource.
         *
         * This function takes the first name entry and processes it using the `processName()` utility.
         * In case of any exception during parsing, it logs the error and returns `null`.
         *
         * @return The practitioner's full name as a string if available, otherwise `null`.
         */
        private fun FhirPractitioner.getName(): String? {
            return try {
                names.firstOrNull()?.processName()
            } catch (e: Exception) {
                Napier.e("Error parsing FHIR Practitioner Name: ${e.message}")
                null
            }
        }

        fun FhirPractitioner.toErpModel(): FhirTaskKbvPractitionerErpModel {
            return FhirTaskKbvPractitionerErpModel(
                name = getName(),
                qualification = getQualification(),
                doctorIdentifier = identifiers.findPractitionerLanr(),
                dentistIdentifier = identifiers.findPractitionerZanr(),
                telematikId = identifiers.findPractitionerTelematikId()
            )
        }
    }
}

/**
 * A stripped down version of the json below where we look only for the text
 * {
 *             "code": {
 *               "coding": [
 *                 {
 *                   "system": "https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Berufsbezeichnung",
 *                   "code": "Berufsbezeichnung"
 *                 }
 *               ],
 *               "text": "Facharzt für Kinder- und Jugendmedizin"
 *             }
 *           }
 */
@Serializable
internal data class FhirQualificationCode(
    @SerialName("code") val code: FhirQualificationText? = null
)

@Serializable
internal data class FhirQualificationText(
    @SerialName("text") val text: String? = null
)

/**
 * Represents the FHIR `PractitionerRole` resource for EU profile version 1.0.
 *
 * In EU bundles, this resource acts as a bridge between:
 * - The individual pharmacist (Practitioner)
 * - The pharmacy organization (Organization)
 *
 * This is required because EU bundles use reference-based linking instead of
 * embedded identifiers (unlike German V1.4 profile).
 */
@Serializable
internal data class FhirPractitionerRole(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("practitioner") val practitioner: FhirPractitionerRoleReference? = null,
    @SerialName("organization") val organization: FhirPractitionerRoleReference? = null,
    @SerialName("code") val code: List<FhirCodeableConcept>? = null
) {
    companion object {
        /**
         * Extracts a PractitionerRole for EU profile.
         */
        fun JsonElement.extractEuPractitionerRole(): FhirPractitionerRole? {
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e("Error parsing EU FHIR PractitionerRole: ${e.message}")
                null
            }
        }
    }
}

/**
 * FHIR reference to another resource.
 * Format: "ResourceType/id" (e.g., "Organization/6a3c8c57-0870-476e-90e3-25b7562799d3")
 */
@Serializable
internal data class FhirPractitionerRoleReference(
    @SerialName("reference") val reference: String? = null
)
