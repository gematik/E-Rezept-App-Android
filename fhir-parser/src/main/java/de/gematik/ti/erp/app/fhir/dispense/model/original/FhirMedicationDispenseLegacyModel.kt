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

package de.gematik.ti.erp.app.fhir.dispense.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.dispense.model.erp.FhirMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseMedicationModel.Companion.toTypedErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestDosageInstruction
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestQuantityValue
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirTemporal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents the legacy structure of a FHIR `MedicationDispense` resource used in eRezept (ePrescription) workflows.
 *
 * This model conforms to the GEM_ERP_PR_MedicationDispense profile defined by gematik:
 * - Specification: [GEM_ERP_PR_MedicationDispense](https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense)
 * - Examples and updates: [Simplifier.net](https://simplifier.net/erezept-workflow/~resources?text=medicationdispense&sortBy=LastUpdateDate_desc)
 *
 * It is used for parsing version 1.2 bundles, where the medication is embedded within the same resource (as `contained`).
 *
 * @property id Unique identifier of the `MedicationDispense` resource.
 * @property meta Metadata about the resource (e.g., profile conformance).
 * @property prescriptionId List of prescription identifiers (KVNR or related).
 * @property status Current status of the medication dispense.
 * @property subject Reference to the patient who received the medication.
 * @property performer List of actors who performed the dispense.
 * @property whenHandedOver Timestamp when the medication was handed over.
 * @property dosageInstruction Instructions for administering the medication.
 * @property substitution Substitution details, if medication was substituted.
 * @property quantity Quantity of medication dispensed.
 * @property medications List of contained `Medication` resources (inlined within this resource).
 */
@Serializable
internal data class FhirMedicationDispenseLegacyModel(
    @SerialName("id") val id: String? = null,
    @SerialName("meta") val meta: FhirMeta? = null,
    @SerialName("identifier") val prescriptionId: List<FhirIdentifier> = emptyList(),
    @SerialName("status") val status: String? = null,
    @SerialName("subject") val subject: FhirMedicationDispenseIdentifier? = null,
    @SerialName("performer") val performer: List<FhirMedicationDispenseActor> = emptyList(),
    @SerialName("whenHandedOver") val whenHandedOver: String? = null,
    @SerialName("dosageInstruction") val dosageInstruction: List<FhirMedicationRequestDosageInstruction> = emptyList(),
    @SerialName("substitution") val substitution: FhirMedicationDispenseSubstitution? = null,
    @SerialName("quantity") val quantity: FhirMedicationRequestQuantityValue? = null,
    @SerialName("contained") val medications: List<FhirMedicationDispenseMedicationModel> = emptyList()
) {
    companion object {
        /**
         * Decodes a `JsonElement` into a [FhirMedicationDispenseLegacyModel].
         *
         * @receiver The raw JSON payload of the `MedicationDispense` resource.
         * @return Parsed [FhirMedicationDispenseLegacyModel] instance.
         */
        fun JsonElement.getMedicationDispenseLegacy(): FhirMedicationDispenseLegacyModel {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }

        /**
         * Mapping the fhir object into [FhirMedicationDispenseErpModel] that can be parsed in the app
         */
        fun FhirMedicationDispenseLegacyModel.toErpModel(): FhirMedicationDispenseErpModel {
            return FhirMedicationDispenseErpModel(
                dispenseId = id ?: "",
                patientId = subject?.identifier?.value ?: "",
                substitutionAllowed = substitution?.wasSubstituted ?: false,
                dosageInstruction = dosageInstruction.map { it.text }.firstOrNull(),
                performer = performer.map { it.actor?.identifier?.value }.firstOrNull(),
                handedOver = whenHandedOver?.asFhirTemporal(),
                dispensedMedication = medications.mapNotNull { it.toTypedErpModel() },
                // these values are present only from version 1.4 and above which support diga
                dispensedDeviceRequest = null
            )
        }
    }
}
