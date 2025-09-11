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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirCoding
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FhirPkvInvoiceBundleSignature(
    @SerialName("type") val type: List<FhirCoding> = emptyList(),
    @SerialName("when") val `when`: String? = null,
    @SerialName("who") val who: FhirPkvInvoiceSignatureWho? = null,
    @SerialName("sigFormat") val sigFormat: String? = null,
    @SerialName("data") val data: String? = null
) {
    companion object {

        internal fun JsonElement.getPkvInvoiceBundleSignature(): FhirPkvInvoiceBundleSignature? {
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e(tag = "fhir-parser") { "Error parsing FhirPkvInvoiceBundleSignature: ${e.message}" }
                null
            }
        }

        internal fun FhirPkvInvoiceBundleSignature.getBinary() = data?.toByteArray()
    }
}

@Serializable
internal data class FhirPkvInvoiceSignatureWho(
    @SerialName("reference") val reference: String? = null
)
