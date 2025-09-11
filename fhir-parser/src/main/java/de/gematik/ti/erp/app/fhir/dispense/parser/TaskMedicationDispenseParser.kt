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

package de.gematik.ti.erp.app.fhir.dispense.parser

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.FhirMedicationDispenseErpModelCollection
import de.gematik.ti.erp.app.fhir.FhirMedicationDispenseErpModelCollection.Companion.toCollection
import de.gematik.ti.erp.app.fhir.common.model.original.FhirResourceBundle.Companion.parseResourceBundle
import de.gematik.ti.erp.app.fhir.common.model.original.FhirResourceEntry
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.MedicationDispenseVersion.Legacy
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.MedicationDispenseVersion.V_1_4
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.MedicationDispenseVersion.V_1_5
import de.gematik.ti.erp.app.fhir.dispense.model.FhirMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.erp.toErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMediationDispenseResourceType.Medication
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMediationDispenseResourceType.MedicationDispense
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseLegacyModel.Companion.getMedicationDispenseLegacy
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseLegacyModel.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseMedicationModel.Companion.extractDispensedMedication
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14V15DispenseModel.Companion.extractMedicationDispense
import de.gematik.ti.erp.app.fhir.dispense.model.original.medicationDispenseResourceTypeForV14V15
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement

@Requirement(
    "O.Source_2#11",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = """
        This parser securely processes structured FHIR `MedicationDispense` input by:
            • Differentiating between legacy [1.2] and modern [1.4] resource versions using entry metadata.
            • Parsing `MedicationDispense` and `Medication` elements separately and safely linking them via `medicationReference`.
            • Validating `device-request` before parsing them with the meta.profile url.
            • Handling structured extensions such as redeem codes and deep links through well-defined FHIR extension URLs.
            • Creating strongly typed ERP domain models [`FhirMedicationDispenseErpModel`] only after validating required fields and relationships.
            • Preventing processing of unsupported or malformed dispense entries by filtering based on FHIR type and version.       
    """
)
class TaskMedicationDispenseParser : BundleParser {

    override fun extract(bundle: JsonElement): FhirMedicationDispenseErpModelCollection? {
        try {
            val entries = bundle.parseResourceBundle()

            // (1) Find if we are in version 1.4, 1.5 or in legacy version
            val version = FhirMedicationDispenseConstants.MedicationDispenseVersion.fromNumber(entries.first().version)

            // (2) Parse differently based on the version that we have
            return when (version) {
                Legacy -> {
                    entries
                        .fromLegacyMedicationDispenseToErpModel()
                        .toCollection()
                }

                V_1_4, V_1_5 -> {
                    entries
                        .fromV14V15MedicationDispenseToErpModel()
                        .toCollection()
                }
            }
        } catch (e: Throwable) {
            Napier.e(tag = "fhir-parser", throwable = e) { "Error parsing MedicationDispense" }
            return null
        }
    }

    /**
     * Map the newer version (1.4, 1.5) medication dispense into a [FhirMedicationDispenseErpModel]
     */
    private fun List<FhirResourceEntry>.fromV14V15MedicationDispenseToErpModel(): List<FhirMedicationDispenseErpModel> {
        val entries = this
        // Extract dispense and medication models
        val dispenses = entries.mapNotNull {
            it.resource
                .takeIf { _ -> it.medicationDispenseResourceTypeForV14V15() == MedicationDispense }
                ?.extractMedicationDispense()
        }
        val medications = entries.mapNotNull {
            it.resource
                .takeIf { _ -> it.medicationDispenseResourceTypeForV14V15() == Medication }
                ?.extractDispensedMedication()
        }

        // Index medications by ID for faster lookup
        val medicationById = medications.associateBy { it.id }

        // Map each dispense and its medication together into a ErpModel
        return dispenses.map { dispense ->
            val medication = medicationById[dispense.medicationReference?.medicationReferenceId]
            dispense.toErpModel(medication)
        }
    }

    /**
     * Map a legacy (1.2) medication dispense into a [FhirMedicationDispenseErpModel]
     */
    private fun List<FhirResourceEntry>.fromLegacyMedicationDispenseToErpModel(): List<FhirMedicationDispenseErpModel> =
        map { entry ->
            entry.resource
                .getMedicationDispenseLegacy()
                .toErpModel()
        }
}
