/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.fhir.prescription.model.erp

import de.gematik.ti.erp.app.fhir.constant.FhirVersions.KBV_BUNDLE_VERSION_103
import de.gematik.ti.erp.app.fhir.constant.FhirVersions.KBV_BUNDLE_VERSION_110
import de.gematik.ti.erp.app.fhir.model.TaskStatus
import de.gematik.ti.erp.app.fhir.prescription.model.erp.KbvBundleVersion.UNKNOWN
import de.gematik.ti.erp.app.utils.FhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Top-level sealed interface for all internal models
@Serializable
sealed interface FhirErpModel

// Interface for all Task-related internal models
@Serializable
sealed interface FhirTaskErpModel : FhirErpModel

data class FhirTaskEntryParserResultErpModel(
    val bundleTotal: Int,
    val taskEntries: List<FhirTaskEntryDataErpModel>
) : FhirErpModel

@Serializable
data class FhirTaskEntryDataErpModel(
    val id: String,
    val status: TaskStatus,
    val lastModified: FhirTemporal?
)

// Represents the FHIR Task + KBV bundle wrapper
@Serializable
data class FhirTaskPayloadErpModel(
    val taskBundle: FhirTaskMetaDataPayloadErpModel,
    val kbvBundle: FirTaskKbvPayloadErpModel
) : FhirTaskErpModel

// Represents a metadata (task-only info) bundle inside a task
@Serializable
data class FhirTaskMetaDataPayloadErpModel(val value: JsonElement) : FhirTaskErpModel

// Represents a KBV bundle inside a task
@Serializable
data class FirTaskKbvPayloadErpModel(val value: JsonElement) : FhirTaskErpModel

// Represents detailed task metadata
@Serializable
data class FhirTaskMetaDataErpModel(
    val taskId: String,
    val accessCode: String,
    val lastModified: FhirTemporal.Instant,
    val expiresOn: FhirTemporal.LocalDate? = null,
    val acceptUntil: FhirTemporal.LocalDate? = null,
    val authoredOn: FhirTemporal.Instant,
    val status: TaskStatus, // todo, using the TaskStatus from the fhir model, need to move it
    val lastMedicationDispense: FhirTemporal.Instant? = null
) : FhirTaskErpModel

@Serializable
data class FhirTaskKbvPatientErpModel(
    val name: String,
    val birthDate: FhirTemporal?,
    val address: FhirTaskKbvAddressErpModel?,
    val insuranceInformation: String?
)

@Serializable
data class FhirTaskKbvAddressErpModel(
    val streetName: String?,
    val houseNumber: String?,
    val postalCode: String?,
    val city: String?
) : FhirTaskErpModel

@Serializable
data class FhirTaskKbvPractitionerErpModel(
    val name: String?,
    val qualification: String?,
    val practitionerIdentifier: String?
) : FhirTaskErpModel

@Serializable
data class FhirTaskOrganizationErpModel(
    val name: String?,
    val address: FhirTaskKbvAddressErpModel?,
    val bsnr: String?,
    val iknr: String?,
    val phone: String?,
    val mail: String?
) : FhirTaskErpModel

@Serializable
data class FhirCoverageErpModel(
    val name: String?,
    val statusCode: String?,
    val coverageType: String?
)

@Serializable
data class FhirTaskKbvMedicationRequestErpModel(
    val authoredOn: FhirTemporal.LocalDate?,
    val dateOfAccident: FhirTemporal.LocalDate?,
    val location: String?,
    val accidentType: FhirTaskAccidentType,
    val emergencyFee: Boolean?,
    val additionalFee: String?,
    val substitutionAllowed: Boolean,
    val dosageInstruction: String?,
    val note: String?,
    val quantity: Int,
    val multiplePrescriptionInfo: FhirMultiplePrescriptionInfoErpModel?,
    val bvg: Boolean
)

@Serializable
data class FhirMultiplePrescriptionInfoErpModel(
    val indicator: Boolean,
    val numbering: FhirRatioErpModel?,
    val start: FhirTemporal?,
    val end: FhirTemporal?
)

@Serializable
data class FhirRatioErpModel(
    val numerator: FhirQuantityErpModel?,
    val denominator: FhirQuantityErpModel?
)

@Serializable
data class FhirQuantityErpModel(
    val value: String?,
    val unit: String?
)

@Serializable
enum class KbvBundleVersion(val version: String) {
    V_1_0_3(KBV_BUNDLE_VERSION_103),
    V_1_1_0(KBV_BUNDLE_VERSION_110),
    UNKNOWN("");
}

@Serializable
enum class FhirTaskAccidentType(val code: String) {
    Unfall("1"),
    Arbeitsunfall("2"),
    Berufskrankheit("3"),
    None("");

    companion object {
        fun getFhirTaskAccidentByType(type: String): FhirTaskAccidentType {
            return FhirTaskAccidentType.entries.find { it.code == type } ?: None
        }
    }
}

@Serializable
enum class FhirTaskMedicationCategoryErpModel(val code: String, val description: String) {
    ARZNEI_UND_VERBAND_MITTEL("00", "Arznei- und Verbandmittel"),
    BTM("01", "Betäubungsmittel (BTM)"),
    AMVV("02", "Arzneimittelverschreibungsverordnung (AMVV)"),
    SONSTIGES("03", "SONSTIGES"),
    UNKNOWN("", "Unknown");

    companion object {
        fun fromCode(code: String?): FhirTaskMedicationCategoryErpModel {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}

@Serializable
data class FhirMedicationIngredientErpModel(
    val text: String?,
    val amount: String?,
    val form: String?,
    val strengthRatio: FhirRatioErpModel?,
    val identifier: FhirMedicationIdentifierErpModel
)

// To be modified on DB update, right now can't change
@Serializable
data class FhirMedicationIdentifierErpModel(
    val pzn: String?,
    val atc: String?,
    val ask: String?,
    val snomed: String?
)

@Serializable
data class FhirTaskKbvMedicationErpModel(
    val text: String?,
    val type: String,
    val version: String,
    val form: String?,
    val medicationCategory: FhirTaskMedicationCategoryErpModel,
    val amount: FhirRatioErpModel?,
    val isVaccine: Boolean,
    val normSizeCode: String?,
    val compoundingInstructions: String?,
    val compoundingPackaging: String?,
    val ingredients: List<FhirMedicationIngredientErpModel>,
    val identifier: FhirMedicationIdentifierErpModel,
    val lotNumber: String?,
    val expirationDate: FhirTemporal?
)

@Serializable
data class FhirTaskDataErpModel(
    val pvsId: String?,
    val medicationRequest: FhirTaskKbvMedicationRequestErpModel?,
    val medication: FhirTaskKbvMedicationErpModel?,
    val patient: FhirTaskKbvPatientErpModel?,
    val practitioner: FhirTaskKbvPractitionerErpModel?,
    val organization: FhirTaskOrganizationErpModel?,
    val coverage: FhirCoverageErpModel?
) : FhirErpModel {
    companion object {
        fun createFhirTaskDataErpModel(
            pvsId: String?,
            medicationRequest: FhirTaskKbvMedicationRequestErpModel?,
            medication: FhirTaskKbvMedicationErpModel?,
            patient: FhirTaskKbvPatientErpModel?,
            practitioner: FhirTaskKbvPractitionerErpModel?,
            organization: FhirTaskOrganizationErpModel?,
            coverage: FhirCoverageErpModel?
        ): FhirTaskDataErpModel? {
            val missingItems = listOfNotNull(
                if (medicationRequest == null) "medicationRequest" else null,
                if (medication == null) "medication" else null,
                if (patient == null) "patient" else null,
                if (practitioner == null) "practitioner" else null,
                if (organization == null) "organization" else null,
                if (coverage == null) "coverage" else null
            )

            if (missingItems.isNotEmpty()) {
                Napier.i("Missing items in FhirTaskDataErpModel, missing items: $missingItems")
                return null
            } else {
                return FhirTaskDataErpModel(
                    pvsId = pvsId,
                    medicationRequest = medicationRequest as FhirTaskKbvMedicationRequestErpModel,
                    medication = medication as FhirTaskKbvMedicationErpModel,
                    patient = patient as FhirTaskKbvPatientErpModel,
                    practitioner = practitioner as FhirTaskKbvPractitionerErpModel,
                    organization = organization as FhirTaskOrganizationErpModel,
                    coverage = coverage as FhirCoverageErpModel
                )
            }
        }
    }
}

fun isValidKbvVersion(version: String): Boolean {
    return KbvBundleVersion.entries
        .filter { it != UNKNOWN } // Exclude UNKNOWN
        .any { it.version == version }
}
