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
import de.gematik.ti.erp.app.fhir.common.model.original.FhirName
import de.gematik.ti.erp.app.fhir.common.model.original.FhirName.Companion.processName
import de.gematik.ti.erp.app.fhir.common.model.original.isValidKbvResource
import de.gematik.ti.erp.app.fhir.constant.FhirConstants
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FhirPatient(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("identifier") val identifiers: List<FhirIdentifier>? = emptyList(),
    @SerialName("name") val names: List<FhirName>? = emptyList(),
    @SerialName("birthDate") val birthDate: String? = null,
    @SerialName("address") val addresses: List<FhirAddress>? = emptyList()
) {

    /**
     * Retrieves the KVNR (Krankenversichertennummer) from the list of identifiers based on the KBV bundle version.
     *
     * The KVNR is a unique identifier for insured individuals in the German healthcare system. This function
     * accounts for different formats of the KVNR based on the KBV version.
     *
     * - For version `V_1_0_3`, it looks for an identifier with a predefined system URL.
     * - For version `V_1_1_0`, it first determines whether the patient is privately (PKV) or publicly (GKV) insured
     *   based on the coding system and retrieves the KVNR from the corresponding system.
     * - For `UNKNOWN` versions, it returns `null`.
     *
     * @param version The KBV bundle version used to determine how to extract the KVNR.
     * @return The KVNR as a [String], or `null` if it cannot be found or the version is unknown.
     */
    fun getKvnr(version: KbvBundleVersion): String? {
        return when (version) {
            KbvBundleVersion.V_1_0_3 -> identifiers?.firstOrNull { it.system == FhirConstants.PATIENT_KVNR_NAME_103 }?.value
            KbvBundleVersion.V_1_1_0 -> {
                val kvnrSystem = identifiers?.flatMap { identifier ->
                    identifier.type?.coding?.filter {
                        it.system == FhirConstants.PATIENT_KVNR_CODE_SYSTEM_URL
                    } ?: emptyList()
                }?.firstOrNull()

                val systemUrl = when (kvnrSystem?.code) {
                    FhirConstants.PKV -> FhirConstants.PATIENT_KVNR_CODE_PKV
                    else -> FhirConstants.PATIENT_KVNR_CODE_GKV
                }

                return identifiers?.firstOrNull { it.system == systemUrl }?.value
            }

            KbvBundleVersion.UNKNOWN -> null
        }
    }

    companion object {

        private fun JsonElement.isValidPatient(): Boolean = isValidKbvResource(FhirKbvResourceType.Patient)

        fun JsonElement.getPatient(): FhirPatient? {
            if (!this.isValidPatient()) return null
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e("Error parsing FHIR Patient: ${e.message}")
                null
            }
        }

        private fun FhirPatient.processBirthdate() = birthDate?.asFhirTemporal()

        fun FhirPatient.toErpModel(version: KbvBundleVersion): FhirTaskKbvPatientErpModel {
            return FhirTaskKbvPatientErpModel(
                name = names?.firstOrNull()?.processName() ?: "",
                birthDate = processBirthdate(),
                address = addresses?.firstOrNull()?.toErpModel(),
                insuranceInformation = getKvnr(version)
            )
        }
    }
}
