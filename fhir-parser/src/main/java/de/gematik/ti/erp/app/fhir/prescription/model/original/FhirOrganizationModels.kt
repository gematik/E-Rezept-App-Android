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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirAddress
import de.gematik.ti.erp.app.fhir.common.model.original.FhirAddress.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.isValidKbvResource
import de.gematik.ti.erp.app.fhir.constant.FhirConstants
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTelecom.Companion.getMail
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTelecom.Companion.getPhone
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FhirOrganization(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("identifier") val identifiers: List<FhirIdentifier>? = emptyList(),
    @SerialName("name") val name: String? = null,
    @SerialName("telecom") val telecoms: List<FhirTelecom>? = emptyList(),
    @SerialName("address") val addresses: List<FhirAddress>? = emptyList()
) {

    val bsnr: String?
        get() = identifiers?.firstOrNull { it.system == FhirConstants.ORGANIZATION_IDENTIFIER_BSNR_NAME }?.value

    val iknr: String?
        get() = identifiers?.firstOrNull { it.system == FhirConstants.ORGANIZATION_IDENTIFIER_IKNR_NAME }?.value

    companion object {

        /**
         * Validates if the JsonElement represents a FHIR Organization using the FhirKbvResourceType enum.
         *
         * @receiver [JsonElement] representing a FHIR resource.
         * @return `true` if the resourceType matches [FhirKbvResourceType.Organization], otherwise `false`.
         */

        private fun JsonElement.isValidOrganization(): Boolean = isValidKbvResource(
            FhirKbvResourceType.Organization
        )

        fun JsonElement.getOrganization(): FhirOrganization? {
            if (!this.isValidOrganization()) return null
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e("Error parsing FHIR Organization: ${e.message}")
                null
            }
        }

        fun FhirOrganization.toErpModel() =
            FhirTaskOrganizationErpModel(
                name = name,
                address = addresses?.firstOrNull()?.toErpModel(),
                bsnr = bsnr,
                iknr = iknr,
                phone = telecoms?.getPhone(),
                mail = telecoms?.getMail()
            )
    }
}

@Serializable
internal data class FhirTelecom(
    @SerialName("system") val system: String? = null,
    @SerialName("value") val value: String? = null
) {
    companion object {
        fun List<FhirTelecom>.getPhone(): String? = firstOrNull { it.system == FhirConstants.TELECOM_PHONE }?.value
        fun List<FhirTelecom>.getMail(): String? = firstOrNull { it.system == FhirConstants.TELECOM_EMAIL }?.value
    }
}
