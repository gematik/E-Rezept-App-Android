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
import de.gematik.ti.erp.app.fhir.constant.FhirConstants
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants
import de.gematik.ti.erp.app.fhir.dispense.model.FhirMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.erp.toErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMediationDispenseResourceType
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMediationDispenseResourceType.Medication
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMediationDispenseResourceType.MedicationDispense
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseEuV10Model
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseEuV10Model.Companion.extractEuMedicationDispense
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseLegacyModel.Companion.getMedicationDispenseLegacy
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseLegacyModel.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseMedicationModel.Companion.extractDispensedMedication
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirMedicationDispenseV14V15DispenseModel.Companion.extractMedicationDispense
import de.gematik.ti.erp.app.fhir.dispense.model.original.medicationDispenseResourceTypeForV14V15
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirOrganization
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirOrganization.Companion.getOrganization
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPractitionerRole
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPractitionerRole.Companion.extractEuPractitionerRole
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement

@Requirement(
    "O.Source_2#11",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = """
        This parser securely processes structured FHIR `MedicationDispense` input by:
            • Differentiating between legacy [1.2], modern [1.4 or 1.5] and eu [1.0] resource versions using entry profile metadata.
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
            // Parse differently based on the profile version that we have
            val profile = entries.first().profile.orEmpty()
            return when (profile) {
                FhirMedicationDispenseConstants.MedicationDispenseProfileVersion.EU_V_1_0.profileUrl
                -> {
                    entries
                        .fromEuV10MedicationDispenseToErpModel()
                        .toCollection()
                }
                FhirMedicationDispenseConstants.MedicationDispenseProfileVersion.V_1_4.profileUrl,
                FhirMedicationDispenseConstants.MedicationDispenseProfileVersion.V_1_5.profileUrl,
                FhirMedicationDispenseConstants.MedicationDispenseProfileVersion.DIGA_V_1_4.profileUrl,
                FhirMedicationDispenseConstants.MedicationDispenseProfileVersion.DIGA_V_1_5.profileUrl
                -> {
                    entries
                        .fromV14V15MedicationDispenseToErpModel()
                        .toCollection()
                }
                else -> {
                    entries
                        .fromLegacyMedicationDispenseToErpModel()
                        .toCollection()
                }
            }
        } catch (e: Throwable) {
            Napier.e(tag = "fhir-parser", throwable = e) { "Error parsing MedicationDispense" }
            return null
        }
    }

    /**
     * Helper function to extract resources of a specific type from bundle entries.
     *
     * @param resourceType The type of resource to extract.
     * @param extractor Function to extract and parse the resource.
     * @return List of extracted resources.
     */
    private inline fun <reified T> List<FhirResourceEntry>.extractEuResources(
        resourceType: FhirMediationDispenseResourceType,
        extractor: (JsonElement) -> T?
    ): List<T> {
        return mapNotNull {
            it.resource
                .takeIf { _ -> it.medicationDispenseResourceTypeForV14V15() == resourceType }
                ?.let(extractor)
        }
    }

    private fun String.extractUuId(): String? = when {
        startsWith(FhirConstants.URN_UUID_PREFIX) -> substringAfter(FhirConstants.URN_UUID_PREFIX)
        contains("/") -> substringAfter("/")
        else -> null
    }

    /**
     * Helper function to resolve performer organization from EU V1.0 MedicationDispense bundles.
     *
     * Resolves the reference chain: MedicationDispense.performer → PractitionerRole → Organization
     *
     * Supports multiple reference formats found in EU bundles:
     * - UUID format: `urn:uuid:5581f231-583d-4105-83c8-d794313c29a3`
     * - Standard format: `PractitionerRole/5581f231-583d-4105-83c8-d794313c29a3`
     *
     * @param dispense The EU medication dispense resource containing the performer reference.
     * @param practitionerRoleById Map of practitioner roles indexed by their ID.
     * @param organizationById Map of organizations indexed by their ID.
     * @return The resolved [FhirOrganization], or `null` if the organization cannot be found or resolved.
     */
    private fun resolvePerformerData(
        dispense: FhirMedicationDispenseEuV10Model,
        practitionerRoleById: Map<String?, FhirPractitionerRole>,
        organizationById: Map<String?, FhirOrganization>
    ): FhirOrganization? = dispense.performer.firstOrNull()?.actor?.reference
        ?.extractUuId()
        ?.let { practitionerRoleById[it] }
        ?.organization?.reference?.extractUuId()
        ?.let { organizationById[it] }

    /**
     * Map EU v1.0 medication dispense bundle into [FhirMedicationDispenseErpModel] list.
     *
     * This function extracts all resources, resolves references, and converts to ErpModel.
     */
    private fun List<FhirResourceEntry>.fromEuV10MedicationDispenseToErpModel(): List<FhirMedicationDispenseErpModel> {
        // Extract all resources from bundle
        val dispenses = extractEuResources(MedicationDispense) { it.extractEuMedicationDispense() }
        val medications = extractEuResources(Medication) { it.extractDispensedMedication() }
        val organizations = extractEuResources(FhirMediationDispenseResourceType.Organization) { it.getOrganization() }
        val practitionerRoles = extractEuResources(FhirMediationDispenseResourceType.PractitionerRole) {
            it.extractEuPractitionerRole()
        }

        // Create lookup maps
        val medicationById = medications.associateBy { it.id }
        val organizationById = organizations.associateBy { it.id }
        val practitionerRoleById = practitionerRoles.associateBy { it.id }

        // Convert each dispense to ErpModel
        return dispenses.map { dispense ->
            val medication = medicationById[dispense.medicationReference?.medicationReferenceId]
            val organization = resolvePerformerData(dispense, practitionerRoleById, organizationById)
            dispense.toErpModel(medication, organization)
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
