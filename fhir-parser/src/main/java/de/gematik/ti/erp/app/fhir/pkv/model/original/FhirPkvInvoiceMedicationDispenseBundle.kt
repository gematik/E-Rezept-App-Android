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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.pkv.model.FhirPkvInvoiceMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirTemporal
import de.gematik.ti.erp.app.utils.Reference
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Reference(
    info = "Dispensierung eines E-Rezepts (GEM_ERP_PR_MedicationDispense)",
    url = "https://simplifier.net/erezept-workflow/gem_erp_pr_medicationdispense"
)
@SerialName("MedicationDispense")
@Serializable
internal data class FhirPkvInvoiceMedicationDispenseBundle(
    @SerialName("resourceType") val resourceType: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("identifier") val identifier: List<FhirIdentifier> = emptyList(),
    @SerialName("contained") val contained: List<FhirMedication> = emptyList(),
    @SerialName("status") val status: String? = null,
    @SerialName("medicationReference") val medicationReference: FhirPkvMedicationDispenseReference? = null,
    @SerialName("subject") val subject: FhirPkvMedicationDispenseActor? = null,
    @SerialName("performer") val performer: List<FhirPkvMedicationDispensePerformer> = emptyList(),
    @SerialName("whenHandedOver") val whenHandedOver: String? = null,
    @SerialName("substitution") val wasSubstituted: FhirPkvMedicationDispenseSubstitution? = null
) {
    companion object {
        internal fun JsonElement.getPkvInvoiceMedicationDispenseBundle(): FhirPkvInvoiceMedicationDispenseBundle? {
            return try {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            } catch (e: Exception) {
                Napier.e(tag = "fhir-parser") { "Error parsing FhirPkvBaseBundle: ${e.message}" }
                null
            }
        }

        internal fun FhirPkvInvoiceMedicationDispenseBundle.toErpModel(): FhirPkvInvoiceMedicationDispenseErpModel {
            return FhirPkvInvoiceMedicationDispenseErpModel(
                whenHandedOver = whenHandedOver?.asFhirTemporal()
            )
        }
    }
}

@Serializable
internal data class FhirPkvMedicationDispenseSubstitution(
    @SerialName("wasSubstituted") val wasSubstituted: Boolean? = null
)

@Serializable
internal data class FhirPkvMedicationDispensePerformer(
    @SerialName("actor") val actor: FhirPkvMedicationDispenseActor? = null
)

@Serializable
internal data class FhirPkvMedicationDispenseActor(
    @SerialName("identifier") val identifier: FhirIdentifier? = null
)

@Serializable
internal data class FhirPkvMedicationDispenseReference(
    @SerialName("reference") val reference: String? = null,
    @SerialName("display") val display: String? = null
)
