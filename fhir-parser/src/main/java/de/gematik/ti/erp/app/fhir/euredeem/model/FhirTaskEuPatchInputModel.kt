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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.common.model.original.FhirParameter
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirTaskEuPatchInputModelConstants
import de.gematik.ti.erp.app.utils.Reference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Reference(
    info = "Link to GEM ERPEU PR PAR PATCH Task Input version 1.0.0",
    url = "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_PATCH_Task_Input"
)
@Serializable
data class FhirTaskEuPatchInputModel(
    @SerialName("resourceType") val resourceType: String,
    @SerialName("id") val id: String,
    @SerialName("meta") val meta: FhirMeta,
    @SerialName("parameter") val parameters: List<FhirParameter>
) {
    companion object {
        internal fun JsonElement.toFhirTaskEuPatchInputModel(): FhirTaskEuPatchInputModel {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }
    }
}

fun createIsEuRedeemableByPatientAuthorizationPayload(
    isEuRedeemableByPatientAuthorization: Boolean
): JsonElement {
    val patchInputModel = FhirTaskEuPatchInputModel(
        resourceType = FhirTaskEuPatchInputModelConstants.RESOURCE_TYPE,
        id = FhirTaskEuPatchInputModelConstants.ID,
        meta = FhirMeta(listOf(FhirTaskEuPatchInputModelConstants.PROFILE_URL)),
        parameters = listOf(
            FhirParameter(
                name = FhirTaskEuPatchInputModelConstants.PARAMETER_NAME,
                valueBoolean = isEuRedeemableByPatientAuthorization
            )
        )
    )
    val jsonString = SafeJson.value.encodeToString(FhirTaskEuPatchInputModel.serializer(), patchInputModel)
    return SafeJson.value.parseToJsonElement(jsonString)
}
