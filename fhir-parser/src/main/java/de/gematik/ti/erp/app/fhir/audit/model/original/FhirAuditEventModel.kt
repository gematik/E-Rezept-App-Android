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

package de.gematik.ti.erp.app.fhir.audit.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirTypeCoding
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FhirAuditEventModel(
    @SerialName("resourceType") val resourceType: String?,
    @SerialName("id") val id: String?,
    @SerialName("meta") val meta: FhirTaggedMeta?,
    @SerialName("type") val type: FhirTypeCoding?,
    @SerialName("text") val text: FhirAuditText?,
    @SerialName("language") val language: String?,
    @SerialName("recorded") val recorded: String?,
    @SerialName("agent") val agent: List<FhirAgent> = emptyList(),
    @SerialName("entity") val entity: List<FhirAuditEventEntity> = emptyList()
) {
    companion object {
        internal fun JsonElement.toAuditEvent(): FhirAuditEventModel {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }
    }
}

@Serializable
internal data class FhirAgent(
    @SerialName("type") val type: FhirType?,
    @SerialName("who") val who: FhirAgentWho?,
    @SerialName("name") val name: String?,
    @SerialName("requestor") val requestor: Boolean?
)

@Serializable
internal data class FhirAuditEventEntity(
    @SerialName("what") val what: FhirAgentWhat?,
    @SerialName("name") val name: String?,
    @SerialName("description") val description: String?
)

@Serializable
internal data class FhirAgentWho(
    @SerialName("identifier") val identifier: FhirIdentifier?
)

@Serializable
internal data class FhirAgentWhat(
    @SerialName("reference") val reference: String?,
    @SerialName("identifier") val identifier: FhirIdentifier?
)

@Serializable
internal data class FhirType(
    @SerialName("coding") val codings: List<FhirTypeCoding> = emptyList()
)

@Serializable
internal data class FhirAuditText(
    @SerialName("status") val status: String,
    @SerialName("div") val div: String
)

@Serializable
internal data class FhirTaggedMeta(
    @SerialName("profile") val profiles: List<String> = emptyList(),
    @SerialName("tag") val tags: List<FhirDisplayTag> = emptyList()
)

@Serializable
internal data class FhirDisplayTag(
    @SerialName("display") val display: String?
)
