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

package de.gematik.ti.erp.app.fhir.consent.model.orignal

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FhirConsentModel(
    @SerialName("resourceType") val resourceType: String = "Consent",
    @SerialName("id") val id: String,
    @SerialName("meta") val meta: FhirMeta?,
    @SerialName("status") val status: String,
    @SerialName("patient") val patient: FhirPatientRef,
    @SerialName("scope") val scope: FhirCodeableConcept,
    @SerialName("category") val category: List<FhirCodeableConcept>,
    @SerialName("dateTime") val dateTime: String? = null,
    @SerialName("policyRule") val policyRule: FhirCodeableConcept
) {
    companion object {
        internal fun JsonElement.toConsent(): FhirConsentModel {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }
    }
}

@Serializable
internal data class FhirMeta(
    @SerialName("profile") val profile: List<String>
)

@Serializable
internal data class FhirPatientRef(
    @SerialName("identifier") val identifier: FhirIdentifier
)

@Serializable
internal data class FhirIdentifier(
    @SerialName("system") val system: String,
    @SerialName("value") val value: String
)

@Serializable
internal data class FhirCodeableConcept(
    @SerialName("coding") val coding: List<FhirCoding>
)

@Serializable
internal data class FhirCoding(
    @SerialName("system") val system: String,
    @SerialName("code") val code: String,
    @SerialName("display") val display: String? = null
)
