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

package de.gematik.ti.erp.app.fhir.pharmacy.model.original

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirTypeCoding
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyAddressErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdIdentifier.Companion.getTelematikId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Requirement(
    "A_19984#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Validate incoming Pharmacy data using serialization and filtering out invalid data."
)
@Serializable
internal data class FhirVzdOrganization(
    @SerialName("resourceType") val resourceType: String,
    @SerialName("id") val id: String?,
    @SerialName("identifier") val identifiers: List<FhirVzdIdentifier> = emptyList(),
    @SerialName("name") val name: String,
    @SerialName("extension") val extensions: List<FhirExtension> = emptyList()
) {

    val telematikId: String
        get() = identifiers.getTelematikId().orEmpty()

    companion object {
        fun JsonElement.getOrganization(): FhirVzdOrganization {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }
    }
}

@Serializable
internal data class FhirVzdIdentifier(
    @SerialName("system") val system: String,
    @SerialName("value") val value: String
) {
    companion object {

        private const val LDAP_UID = "https://gematik.de/fhir/directory/CodeSystem/ldapUID"
        private const val TELEMATIK_UID = "https://gematik.de/fhir/sid/telematik-id"

        fun List<FhirVzdIdentifier>.getUid(): String = this.find { it.system == LDAP_UID }?.value?.trim() ?: ""

        fun List<FhirVzdIdentifier>.getTelematikId(): String? = this.find { it.system == TELEMATIK_UID }?.value?.trim()
    }
}

@Serializable
internal data class FhirVzdType(
    @SerialName("coding") val coding: List<FhirTypeCoding>
)

@Serializable
internal data class FhirVzdAddress(
    @SerialName("use") val use: String?,
    @SerialName("type") val type: String?,
    @SerialName("text") val text: String?,
    @SerialName("line") val line: List<String> = emptyList(),
    @SerialName("city") val city: String?,
    @SerialName("state") val state: String?,
    @SerialName("postalCode") val postalCode: String?,
    @SerialName("country") val country: String?
) {
    companion object {
        fun FhirVzdAddress.toErpModel(): FhirPharmacyAddressErpModel {
            return FhirPharmacyAddressErpModel(
                lineAddress = line.joinToString(",").trim(),
                postalCode = postalCode,
                city = city
            )
        }
    }
}
