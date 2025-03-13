/*
 * Copyright 2024, gematik GmbH
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

import de.gematik.ti.erp.app.fhir.constant.FhirConstants
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.KbvBundleVersion
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirAddress.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirName.Companion.processName
import de.gematik.ti.erp.app.fhir.prescription.util.ParserUtil.asFhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FhirPatient(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("identifier") val identifiers: List<FhirIdentifier>? = emptyList(),
    @SerialName("name") val names: List<FhirName>? = emptyList(),
    @SerialName("birthDate") val birthDate: String? = null,
    @SerialName("address") val addresses: List<FhirAddress>? = emptyList()
) {

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
