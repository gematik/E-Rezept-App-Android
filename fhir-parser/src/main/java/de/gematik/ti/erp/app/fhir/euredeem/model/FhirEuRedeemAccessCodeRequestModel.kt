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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCoding
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirParameter
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeRequestConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.random.Random

@Serializable
data class FhirEuRedeemAccessCodeRequestModel(
    @SerialName("resourceType") val resourceType: String,
    @SerialName("id") val id: String,
    @SerialName("meta") val meta: FhirMeta,
    @SerialName("parameter") val parameters: List<FhirParameter>
) {
    companion object {
        internal fun JsonElement.toFhirEuRedeemAccessCodeRequestModel(): FhirEuRedeemAccessCodeRequestModel {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }
    }
}

fun createEuRedeemAccessCodePayload(
    countryCode: String,
    accessCode: String
): JsonElement {
    val accessCodeRequestModel = FhirEuRedeemAccessCodeRequestModel(
        resourceType = FhirEuRedeemAccessCodeRequestConstants.RESOURCE_TYPE,
        id = FhirEuRedeemAccessCodeRequestConstants.ID,
        meta = FhirMeta(listOf(FhirEuRedeemAccessCodeRequestConstants.PROFILE_URL)),
        parameters = listOf(
            FhirParameter(
                name = FhirEuRedeemAccessCodeRequestConstants.CountryCodeParameter.NAME,
                valueCoding = FhirCoding(
                    coding = null,
                    system = FhirEuRedeemAccessCodeRequestConstants.CountryCodeParameter.SYSTEM,
                    code = countryCode
                )
            ),
            FhirParameter(
                name = FhirEuRedeemAccessCodeRequestConstants.AccessCodeParameter.NAME,
                valueIdentifier = FhirIdentifier(
                    system = FhirEuRedeemAccessCodeRequestConstants.AccessCodeParameter.SYSTEM,
                    value = accessCode
                )
            )
        )
    )
    val jsonString = SafeJson.value.encodeToString(FhirEuRedeemAccessCodeRequestModel.serializer(), accessCodeRequestModel)
    return SafeJson.value.parseToJsonElement(jsonString)
}

const val ACCESS_CODE_SIZE = 6

// No i I l O 0
val accessCodeRegex = Regex("[A-HJ-NP-Za-hjkm-z1-9]")

fun generateAccessCode(): String {
    val digits = ('0'..'9')
    val lowercaseLetters = ('a'..'z')
    val uppercaseLetters = ('A'..'Z')
    val allowedCharacters = (digits + lowercaseLetters + uppercaseLetters).toList().filter { accessCodeRegex.matches(it.toString()) }
    return String(
        CharArray(ACCESS_CODE_SIZE) {
            allowedCharacters.random(Random)
        }
    )
}
