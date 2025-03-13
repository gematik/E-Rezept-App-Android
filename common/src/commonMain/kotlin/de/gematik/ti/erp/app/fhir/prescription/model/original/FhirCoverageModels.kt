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
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirCoverageErpModel
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FhirCoverageModel(
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("extension") val extensions: List<FhirCoverageExtension>? = emptyList(),
    @SerialName("type") val type: FhirCoverageType? = null,
    @SerialName("payor") val payer: List<FhirPayer>? = emptyList()
) {
    companion object {
        private fun JsonElement.isValidCoverage(): Boolean = isValidKbvResource(FhirKbvResourceType.Coverage)

        fun JsonElement.getCoverage(): FhirCoverageModel? {
            if (!this.isValidCoverage()) return null
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.w("Error parsing FHIR Coverage: ${e.message}")
                null
            }
        }

        fun FhirCoverageModel.toErpModel(): FhirCoverageErpModel? =
            FhirCoverageErpModel(
                name = payer?.firstOrNull()?.name,
                statusCode = extensions?.firstOrNull { it.coding?.system == FhirConstants.COVERAGE_KBV_STATUS_CODE_SYSTEM }?.coding?.code,
                coverageType = type?.coding?.firstOrNull()?.code
            )
    }
}

@Serializable
data class FhirCoverageType(
    @SerialName("coding") val coding: List<FhirCoding>? = emptyList()
)

@Serializable
data class FhirCoverageExtension(
    @SerialName("url") val url: String? = null,
    @SerialName("valueCoding") val coding: FhirCoding? = null
)

@Serializable
data class FhirPayer(
    @SerialName("display") val name: String? = null,
    @SerialName("identifier") val identifier: FhirIdentifier? = null
)
