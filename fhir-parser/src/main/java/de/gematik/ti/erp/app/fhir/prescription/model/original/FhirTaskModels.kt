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

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.serializer.SafeFhirInstantSerializer
import de.gematik.ti.erp.app.fhir.serializer.SafeFhirTaskExtensionArraySerializer
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

@Serializable
internal data class FhirTaskStatus(
    val status: String
) {
    companion object {
        fun JsonElement.getStatus() =
            SafeJson.value.decodeFromJsonElement(serializer(), this).status
    }
}

@Serializable(with = SafeFhirTaskExtensionArraySerializer::class)
internal data class FhirTaskExtensionValues(
    val acceptDate: String?,
    val expiryDate: String?,
    val lastMedicationDispense: String?,
    val prescriptionType: String?
) {
    fun acceptedDate() = acceptDate?.let { FhirTemporal.LocalDate(LocalDate.parse(it)) }
    fun expiryDate() = expiryDate?.let { FhirTemporal.LocalDate(LocalDate.parse(it)) }
    fun lastMedicationDispense() = lastMedicationDispense?.let { FhirTemporal.Instant(Instant.parse(it)) }

    companion object {
        fun JsonElement.getExtensionValues(): FhirTaskExtensionValues {
            val extensionArray = this.jsonObject["extension"]?.jsonArray ?: JsonArray(emptyList())
                .also { Napier.w("Extension array not found in Task resource") }
            val parserExtensions = SafeJson.value.decodeFromJsonElement(
                deserializer = serializer(),
                element = extensionArray
            )
            return parserExtensions
        }
    }
}

@Serializable
internal data class FhirTaskLifeCycleMetadata(
    @Serializable(with = SafeFhirInstantSerializer::class)
    val authoredOn: FhirTemporal.Instant?, // FHIR date-time format (ISO 8601)
    @Serializable(with = SafeFhirInstantSerializer::class)
    val lastModified: FhirTemporal.Instant?
) {
    companion object {
        fun JsonElement.getAuthoredOn() =
            SafeJson.value.decodeFromJsonElement(serializer(), this).authoredOn

        fun JsonElement.getLastModified() =
            SafeJson.value.decodeFromJsonElement(serializer(), this).lastModified
    }
}
