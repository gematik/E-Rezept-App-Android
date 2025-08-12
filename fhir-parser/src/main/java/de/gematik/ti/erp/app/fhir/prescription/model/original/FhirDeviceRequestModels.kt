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

import de.gematik.ti.erp.app.fhir.common.model.erp.accidentInformationExtension
import de.gematik.ti.erp.app.fhir.common.model.erp.toAccidentInformation
import de.gematik.ti.erp.app.fhir.common.model.original.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.isValidKbvResource
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.RequestIntent
import de.gematik.ti.erp.app.fhir.support.AccidentTypeRequestFrom
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// https://simplifier.net/evdga/kbv_pr_evdga_healthapprequest
@Serializable
internal data class FhirDeviceRequestModel(
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val resourceType: FhirMeta? = null,
    @SerialName("extension") val extension: List<FhirExtension> = emptyList(),
    @SerialName("status") val status: String? = null,
    @SerialName("intent") val intent: String? = null, // RequestIntent
    @SerialName("authoredOn") val authoredOn: String? = null,
    @SerialName("codeCodeableConcept") val code: FhirCodeableConcept? = null
) {
    companion object {

        private const val IS_SELF_USE_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_EVDGA_SER"

        private fun JsonElement.isValidDeviceRequest(): Boolean = isValidKbvResource(
            FhirKbvResourceType.DeviceRequest
        )

        fun JsonElement.getDeviceRequest(): FhirDeviceRequestModel? {
            if (!this.isValidDeviceRequest()) return null
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e("Error parsing Diga: ${e.message}")
                null
            }
        }

        private fun List<FhirExtension>.isSelfUse() = firstOrNull { it.url == IS_SELF_USE_EXTENSION_URL }?.valueBoolean ?: false

        // note: accidentInformation is an extension inside an extension
        private fun FhirDeviceRequestModel.accidentData() = extension.accidentInformationExtension()?.extensions
            ?.toAccidentInformation(from = AccidentTypeRequestFrom.DeviceRequest)

        fun FhirDeviceRequestModel.toErpModel() = FhirTaskKbvDeviceRequestErpModel(
            id = id,
            intent = RequestIntent.fromCode(intent),
            pzn = code?.coding?.firstOrNull()?.code ?: "",
            appName = code?.text ?: "",
            accident = accidentData(),
            isSelfUse = extension.isSelfUse(),
            authoredOn = authoredOn?.asFhirTemporal(),
            status = status ?: "",
            isNew = true,
            isArchived = false
        )
    }
}
