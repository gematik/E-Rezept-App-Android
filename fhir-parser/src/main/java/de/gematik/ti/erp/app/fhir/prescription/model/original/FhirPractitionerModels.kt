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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findPractitionerIdentifierValue
import de.gematik.ti.erp.app.fhir.common.model.original.FhirName
import de.gematik.ti.erp.app.fhir.common.model.original.FhirName.Companion.processName
import de.gematik.ti.erp.app.fhir.common.model.original.FhirTaskResource.Companion.getResourceIdentifiers
import de.gematik.ti.erp.app.fhir.common.model.original.isResourceType
import de.gematik.ti.erp.app.fhir.common.model.original.isValidKbvResource
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPractitionerErpModel
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

@Serializable
internal data class FhirPractitioner(
    @SerialName("qualification") val qualifications: List<FhirQualificationCode> = emptyList(),
    @SerialName("name") val names: List<FhirName> = emptyList()
) {
    companion object {
        private fun JsonElement.isValidPractitioner(): Boolean = isValidKbvResource(
            FhirKbvResourceType.Practitioner
        )

        fun JsonElement.getPractitioner(): Pair<FhirPractitioner, JsonElement>? {
            if (!isValidPractitioner()) return null
            return Pair(
                SafeJson.value.decodeFromJsonElement<FhirPractitioner>(serializer(), this),
                this
            )
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

        fun Pair<FhirPractitioner, JsonElement>.toErpModel() =
            FhirTaskKbvPractitionerErpModel(
                name = first.getName(),
                qualification = first.getQualification(),
                practitionerIdentifier = second.getResourceIdentifiers().identifiers.findPractitionerIdentifierValue()
            )
    }
}

@Serializable
internal data class FhirQualificationCode(
    @SerialName("code") val code: FhirQualificationText? = null
)

@Serializable
internal data class FhirQualificationText(
    @SerialName("text") val text: String? = null
)
