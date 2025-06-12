/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.fhir.prescription.parser

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirTaskDataErpModel.Companion.createFhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleMetaProfile.Companion.containsExpectedProfileVersionForTaskKbvPhase
import de.gematik.ti.erp.app.fhir.common.model.original.FhirResourceBundle.Companion.parseResourceBundle
import de.gematik.ti.erp.app.fhir.common.model.original.FhirResourceEntry
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.PvsIdentifier
import de.gematik.ti.erp.app.fhir.constant.FhirVersions
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirCoverageErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvMedicationRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvPractitionerErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.KbvBundleVersion
import de.gematik.ti.erp.app.fhir.prescription.model.erp.isValidKbvVersion
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirAuthor.Companion.findAuthorReferenceByType
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirAuthorBundle.Companion.getAuthorReferences
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirCoverageModel.Companion.getCoverage
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirCoverageModel.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirDeviceRequestModel.Companion.getDeviceRequest
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirDeviceRequestModel.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirKbvResourceType
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.getMedication
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequest.Companion.getMedicationRequest
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequest.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirOrganization.Companion.getOrganization
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirOrganization.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPatient.Companion.getPatient
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPatient.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPractitioner.Companion.getPractitioner
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPractitioner.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirResourceId.Companion.getResourceId
import de.gematik.ti.erp.app.fhir.prescription.model.original.kbvResourceType
import de.gematik.ti.erp.app.utils.ParserUtil.findValueByUrl
import de.gematik.ti.erp.app.utils.sanitize
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement

@Requirement(
    "O.Source_2#10",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = """
        This parser validates and extracts prescription-related medical data from a KBV Bundle by:
            • Filtering only supported KBV resources such as `MedicationRequest`, `Medication`, `Patient`, `Practitioner`,`Coverage` ...
            • Applying strict profile-version checks for each resource type, such as `1.0.3` and `1.1.0` for `Practitioner` and `Coverage`.
            • Using sanitized IDs to match `Practitioner` entries when more than one instance is present.           
            • Creating domain-specific ERP models only when valid resources are found, skipping unsupported or malformed ones.
        This ensures that only valid, structured, and specification-compliant prescription data is processed.
    """
)
class TaskEPrescriptionMedicalDataParser : BundleParser {

    @Suppress("CyclomaticComplexMethod")
    override fun extract(bundle: JsonElement): FhirTaskDataErpModel? {
        return runCatching {
            if (!bundle.containsExpectedProfileVersionForTaskKbvPhase()) {
                Napier.e { "TaskKBVParser: bundle does not contain expected profile version for Task KBV phase" }
                return null
            }

            val entries = bundle.parseResourceBundle()

            // The practitionerId obtained here is used to identify the correct Practitioner resource for 110 version
            val practitionerId = bundle
                .getAuthorReferences()
                .findAuthorReferenceByType(FhirKbvResourceType.Practitioner.name)
                ?.sanitize()

            val pvsId = findValueByUrl(bundle, PvsIdentifier.FULL_URL.value, PvsIdentifier.ITEM_KEY.value, PvsIdentifier.ITEM_VALUE.value)

            var medicationRequest: FhirTaskKbvMedicationRequestErpModel? = null
            var medication: FhirTaskKbvMedicationErpModel? = null
            var patient: FhirTaskKbvPatientErpModel? = null
            var practitioner: FhirTaskKbvPractitionerErpModel? = null
            var organization: FhirTaskOrganizationErpModel? = null
            var coverage: FhirCoverageErpModel? = null
            var deviceRequest: FhirTaskKbvDeviceRequestErpModel? = null

            entries.map { entry ->
                when (entry.kbvResourceType()) {
                    FhirKbvResourceType.MedicationRequest -> {
                        val medicationRequestBundle = entry.resource
                        medicationRequest = medicationRequestBundle.getMedicationRequest()?.toErpModel()
                    }

                    FhirKbvResourceType.Medication -> {
                        val medicationBundle = entry.resource
                        medication = medicationBundle.getMedication()?.toErpModel()
                    }

                    FhirKbvResourceType.Patient -> {
                        val patientBundle = entry.resource
                        val version = entry.kbvBundleVersion()
                        patient = patientBundle.getPatient()?.toErpModel(version)
                    }

                    FhirKbvResourceType.PractitionerRole -> null // not processing
                    FhirKbvResourceType.Practitioner -> {
                        // there are 2 Practitioner resources in the same bundle, only one will be used based on matches
                        val practitionerBundle = entry.resource
                        val isPractitionerVersion103 = entry.isPractitionerVersion103()
                        val isPractitionerVersion110 = entry.isPractitionerVersion110(practitionerId)

                        if (isPractitionerVersion103 || isPractitionerVersion110) {
                            practitioner = practitionerBundle.getPractitioner()?.toErpModel()
                        } else {
                            Napier.w("Unsupported Practitioner version: ${entry.version}")
                        }
                    }

                    FhirKbvResourceType.Organization -> {
                        val organizationBundle = entry.resource
                        organization = organizationBundle.getOrganization()?.toErpModel()
                    }

                    FhirKbvResourceType.Coverage -> {
                        val coverageBundle = entry.resource
                        Napier.e { "device request coverage ${entry.resource}" }
                        val areValidVersions = entry.areIncludedCoveragesVersion()
                        if (areValidVersions) {
                            coverage = coverageBundle.getCoverage()?.toErpModel()
                        } else {
                            Napier.w("Unsupported Coverage version: ${entry.version}")
                        }
                    }

                    FhirKbvResourceType.DeviceRequest -> {
                        val deviceRequestBundle = entry.resource
                        Napier.e { "device request raw ${entry.resource}" }
                        Napier.e { "device request parsed ${deviceRequestBundle.getDeviceRequest()?.toErpModel()}" }
                        deviceRequest = deviceRequestBundle.getDeviceRequest()?.toErpModel()
                    }

                    else -> null
                }
            }

            return createFhirTaskDataErpModel(
                pvsId = pvsId,
                medicationRequest = medicationRequest,
                medication = medication,
                patient = patient,
                practitioner = practitioner,
                organization = organization,
                coverage = coverage,
                deviceRequest = deviceRequest
            )
        }.onFailure {
            Napier.e { "TaskKBVParser: error: ${it.message}" }
        }.getOrNull()
    }

    private fun FhirResourceEntry.kbvBundleVersion(): KbvBundleVersion {
        return KbvBundleVersion.entries.find { it.version == version } ?: KbvBundleVersion.UNKNOWN
    }

    private fun FhirResourceEntry.areIncludedCoveragesVersion() = version?.let { isValidKbvVersion(it) } == true

    private fun FhirResourceEntry.isPractitionerVersion103() = version == FhirVersions.KBV_BUNDLE_VERSION_103

    private fun FhirResourceEntry.isPractitionerVersion110(practitionerId: String?) =
        version == FhirVersions.KBV_BUNDLE_VERSION_110 && resource.getResourceId()?.sanitize() == practitionerId
}
