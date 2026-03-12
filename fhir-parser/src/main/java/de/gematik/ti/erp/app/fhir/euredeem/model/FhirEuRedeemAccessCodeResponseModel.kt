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

package de.gematik.ti.erp.app.fhir.euredeem.model

import de.gematik.ti.erp.app.fhir.FhirEuRedeemAccessCodeResponseErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirParameter
import de.gematik.ti.erp.app.fhir.common.model.original.FhirParameter.Companion.findParameterByName
import de.gematik.ti.erp.app.fhir.common.model.original.extractProfilesFromResourceMeta
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeResponseConstants
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeResponseConstants.FhirEuRedeemAccessCodeResponseMeta
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FhirEuRedeemAccessCodeResponseModel(
    @SerialName("resourceType") val resourceType: String,
    @SerialName("id") val id: String,
    @SerialName("meta") val meta: FhirMeta,
    @SerialName("parameter") val parameters: List<FhirParameter>
) {
    companion object {
        internal fun JsonElement.toFhirEuRedeemAccessCodeResponseModel(): FhirEuRedeemAccessCodeResponseModel? {
            return try {
                val allowedIdentifiers = FhirEuRedeemAccessCodeResponseMeta.entries.map { it.identifier }
                val profile = extractProfilesFromResourceMeta(this)
                val isValidEuRedeemAccessCodeResponse = profile.any { profileUrl ->
                    allowedIdentifiers.any { identifier -> profileUrl.contains(identifier) }
                }
                if (isValidEuRedeemAccessCodeResponse) {
                    SafeJson.value.decodeFromJsonElement(serializer(), this)
                } else {
                    Napier.e(tag = "fhir-parser") { "Invalid FhirEuRedeemAccessCodeResponseBundle" }
                    null
                }
            } catch (e: Exception) {
                Napier.e(
                    tag = "fhir-parser",
                    message = "Error parsing FhirEuRedeemAccessCodeResponseBundle: ${e.message}"
                )
                null
            }
        }

        private fun List<FhirParameter>.getCountryCode(): String? {
            val parameter = this.findParameterByName(FhirEuRedeemAccessCodeResponseConstants.CountryCodeParameter.NAME)
            return parameter?.valueCoding?.let {
                if (it.system == FhirEuRedeemAccessCodeResponseConstants.CountryCodeParameter.SYSTEM) {
                    it.code
                } else {
                    null
                }
            }
        }

        private fun List<FhirParameter>.getAccessCode(): String? {
            val parameter = this.findParameterByName(FhirEuRedeemAccessCodeResponseConstants.AccessCodeParameter.NAME)
            return parameter?.valueIdentifier?.let {
                if (it.system == FhirEuRedeemAccessCodeResponseConstants.AccessCodeParameter.SYSTEM) {
                    it.value
                } else {
                    null
                }
            }
        }

        private fun List<FhirParameter>.getValidUntil(): FhirTemporal.Instant? {
            val parameter = findParameterByName(FhirEuRedeemAccessCodeResponseConstants.ValidUntilParameter.NAME)
            return parameter?.valueInstant?.let { FhirTemporal.Instant(Instant.parse(it)) }
        }

        private fun List<FhirParameter>.getCreatedAt(): FhirTemporal.Instant? {
            val parameter = findParameterByName(FhirEuRedeemAccessCodeResponseConstants.CreatedAtParameter.NAME)
            return parameter?.valueInstant?.let { FhirTemporal.Instant(Instant.parse(it)) }
        }

        internal fun FhirEuRedeemAccessCodeResponseModel.toErpModel(): FhirEuRedeemAccessCodeResponseErpModel? {
            return try {
                FhirEuRedeemAccessCodeResponseErpModel(
                    countryCode = parameters.getCountryCode() ?: throw Exception("CountryCode could not be parsed"),
                    accessCode = parameters.getAccessCode() ?: throw Exception("AccessCode could not be parsed"),
                    validUntil = parameters.getValidUntil() ?: throw Exception("ValidUntil could not be parsed"),
                    createdAt = parameters.getCreatedAt() ?: throw Exception("CreatedAt could not be parsed")
                )
            } catch (e: Exception) {
                Napier.e(
                    tag = "fhir-parser",
                    message = "Error parsing FhirEuRedeemAccessCodeResponseBundle: ${e.message}"
                )
                null
            }
        }
    }
}
