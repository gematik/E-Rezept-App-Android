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

package de.gematik.ti.erp.app.fhir.pkv.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirResourceEntry
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.serializer.SafeSingleOrListSerializer
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FhirPkvBaseBundle(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @Serializable(with = SafeSingleOrListSerializer::class)
    @SerialName("identifier")
    val identifier: List<FhirIdentifier> = emptyList(),
    @Serializable(with = SafeSingleOrListSerializer::class)
    @SerialName("subject")
    val subject: List<FhirIdentifier> = emptyList(),
    @Serializable(with = SafeSingleOrListSerializer::class)
    @SerialName("enterer")
    val enterer: List<FhirIdentifier> = emptyList(),
    @SerialName("type") val type: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("supportingInformation") val supportingInformation: List<FhirPkvBundleReference> = emptyList(),
    @SerialName("entry") val entries: List<FhirResourceEntry> = emptyList(),
    // added support to make it easier
    val fullUrl: String? = null,
    val originalBundle: JsonElement? = null,
    val bundleType: FhirPkvBaseBundleType = FhirPkvBaseBundleType.Unknown
) {
    companion object {
        internal fun JsonElement.getBaseBundle(): FhirPkvBaseBundle? {
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e(tag = "fhir-parser") { "Error parsing FhirPkvBaseBundle: ${e.message}" }
                null
            }
        }
    }
}

@Serializable
internal data class FhirPkvBundleReference(
    @SerialName("reference") val reference: String? = null,
    @SerialName("display") val display: String? = null
) {

    companion object {
        fun List<FhirPkvBundleReference>.getReferenceByUrl(url: String) = find { it.display?.trim() == url.trim() }?.reference
    }
}
