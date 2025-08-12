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
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirResourceBundle.Companion.parseResourceBundle
import de.gematik.ti.erp.app.fhir.common.model.original.FhirResourceEntry
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.DIGA_DEEP_LINK
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.DIGA_REDEEM_CODE
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispenseDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMediationDispenseResourceType.Medication
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMediationDispenseResourceType.MedicationDispense
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseLegacyModel.Companion.getMedicationDispenseLegacy
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseLegacyModel.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseMedicationModel.Companion.getMedicationDispensedMedication
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseMedicationModel.Companion.toTypedErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14DispenseModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14DispenseModel.Companion.getMedicationDispenseV14
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14DispenseModel.Companion.isDigaType
import de.gematik.ti.erp.app.fhir.dispense.model.original.medicationDispenseResourceTypeForV14
import de.gematik.ti.erp.app.fhir.error.fhirError
import de.gematik.ti.erp.app.utils.ParserUtil.asFhirTemporal
import de.gematik.ti.erp.app.utils.ParserUtil.asNullableFhirTemporal
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
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

            Napier.e { "device request dispense $entries" }

            // (1) Find if we are in version 1.4 or in legacy version
            val version = entries.first().version

            // (2) Parse differently based on the version that we have
            return when (version) {
                FhirMedicationDispenseConstants.VERSION_1_4 ->
                    entries
                        .fromV14MedicationDispenseToErpModel()
                        .toCollection()

                else ->
                    entries
                        .fromLegacyMedicationDispenseToErpModel()
                        .toCollection()
            }
        } catch (e: Throwable) {
            Napier.e(e) { "Error parsing MedicationDispense" }
            return null
        }
    }

    /**
     * Map the newer version (1.4) medication dispense into a [FhirMedicationDispenseErpModel]
     */
    private fun List<FhirResourceEntry>.fromV14MedicationDispenseToErpModel(): List<FhirMedicationDispenseErpModel> {
        val entries = this
        // Extract dispense and medication models
        val dispenses = entries.mapNotNull {
            it.resource
                .takeIf { _ -> it.medicationDispenseResourceTypeForV14() == MedicationDispense }
                ?.getMedicationDispenseV14()
        }
        val medications = entries.mapNotNull {
            it.resource
                .takeIf { _ -> it.medicationDispenseResourceTypeForV14() == Medication }
                ?.getMedicationDispensedMedication()
        }

        // Index medications by ID for faster lookup
        val medicationById = medications.associateBy { it.id }

        // Map each dispense and its medication together into a ErpModel
        return dispenses.map { dispense ->
            val medication = medicationById[dispense.medicationReference?.medicationReferenceId]
            FhirMedicationDispenseErpModel(
                dispenseId = dispense.id ?: "",
                patientId = dispense.subject?.identifier?.value ?: "",
                substitutionAllowed = dispense.substitution?.wasSubstituted ?: false,
                dosageInstruction = dispense.dosageInstruction.map { it.text }.firstOrNull(),
                performer = dispense.performer.map { it.actor?.identifier?.value }.firstOrNull(),
                handedOver = dispense.whenHandedOver?.asFhirTemporal(),
                dispensedMedication = medication?.toTypedErpModel()?.let { listOf(it) } ?: emptyList(),
                dispensedDeviceRequest = if (dispense.isDigaType()) {
                    Napier.e { "device request dispense ${dispense.toDeviceRequestErpModel()}" }
                    dispense.toDeviceRequestErpModel()
                } else null
            )
        }
    }

    private fun FhirMedicationDispenseV14DispenseModel.toDeviceRequestErpModel(): FhirDispenseDeviceRequestErpModel {
        return FhirDispenseDeviceRequestErpModel(
            declineCode = dataAbsentCodeIfNoRedeemCode(),
            referencePzn = if (isRedeemCodePresent()) medicationReference!!.pzn else medicationReference?.pzn,
            display = if (isRedeemCodePresent()) medicationReference!!.displayInfo else medicationReference?.displayInfo,
            note = fallbackNoteIfNoActivationCode(),
            redeemCode = extension.findExtensionByUrl(DIGA_REDEEM_CODE)?.valueString,
            deepLink = extension.findExtensionByUrl(DIGA_DEEP_LINK)?.valueUrl,
            modifiedDate = whenPrepared?.asNullableFhirTemporal() ?: whenHandedOver?.asNullableFhirTemporal(),
            status = status
        )
    }

    // https://gemspec.gematik.de/docs/gemF/gemF_eRp_DiGA/latest/#7.7.1.2.1
    private fun FhirMedicationDispenseV14DispenseModel.isRedeemCodePresent() =
        extension.findExtensionByUrl(DIGA_REDEEM_CODE)?.valueString.isNotNullOrEmpty()

    // https://gemspec.gematik.de/docs/gemF/gemF_eRp_DiGA/latest/#7.7.1.2.1
    private fun FhirMedicationDispenseV14DispenseModel.fallbackNoteIfNoActivationCode(): String? {
        val redeemCode = extension.findExtensionByUrl(DIGA_REDEEM_CODE)?.valueString

        if (!redeemCode.isNullOrEmpty()) {
            return null
        }

        val noteText = note.firstOrNull()?.text
        return noteText ?: fhirError("Diga medication dispense note rule failed: required note is missing")
    }

    // https://gemspec.gematik.de/docs/gemF/gemF_eRp_DiGA/latest/#7.7.1.2.1
    private fun FhirMedicationDispenseV14DispenseModel.dataAbsentCodeIfNoRedeemCode(): String? {
        val redeemCode = extension.findExtensionByUrl(DIGA_REDEEM_CODE)?.valueString

        if (!redeemCode.isNullOrEmpty()) {
            return null
        }

        val declineCode = medicationReference?.declineCode
        return declineCode ?: fhirError("Diga medication dispense decline code rule failed: required decline code is missing")
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
