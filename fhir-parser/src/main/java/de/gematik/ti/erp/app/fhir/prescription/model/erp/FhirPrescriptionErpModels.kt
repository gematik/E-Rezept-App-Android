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

package de.gematik.ti.erp.app.fhir.prescription.model.erp

import de.gematik.ti.erp.app.fhir.common.model.erp.FhirAccidentInformationErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.constant.FhirVersions.KBV_BUNDLE_VERSION_103
import de.gematik.ti.erp.app.fhir.constant.FhirVersions.KBV_BUNDLE_VERSION_110
import de.gematik.ti.erp.app.utils.FhirTemporal
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Represents a metadata (task-only info) bundle inside a task
@Serializable
data class FhirTaskMetaDataPayloadErpModel(val value: JsonElement)

// Represents a KBV bundle inside a task
@Serializable
data class FirTaskKbvPayloadErpModel(val value: JsonElement)

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
)

@Serializable
data class FhirTaskKbvPractitionerErpModel(
    val name: String?,
    val qualification: String?,
    val practitionerIdentifier: String?
)

@Serializable
data class FhirTaskOrganizationErpModel(
    val name: String?,
    val address: FhirTaskKbvAddressErpModel?,
    val bsnr: String?,
    val iknr: String?,
    val phone: String?,
    val mail: String?
)

@Serializable
data class FhirCoverageErpModel(
    val name: String?,
    val statusCode: String?,
    val identifierNumber: String?, // required for diga as the iknr
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
data class FhirTaskKbvDeviceRequestErpModel(
    val id: String?,
    val intent: RequestIntent,
    val status: String,
    val pzn: String?,
    val appName: String?,
    val accident: FhirAccidentInformationErpModel?,
    val isSelfUse: Boolean,
    val authoredOn: FhirTemporal?
)

@Serializable
data class FhirMultiplePrescriptionInfoErpModel(
    val indicator: Boolean,
    val numbering: FhirRatioErpModel?,
    val start: FhirTemporal?,
    val end: FhirTemporal?
)

// todo move to common
@Serializable
data class FhirRatioErpModel(
    val numerator: FhirQuantityErpModel?,
    val denominator: FhirQuantityErpModel?
)

// todo move to common
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

// https://simplifier.net/packages/hl7.fhir.r4.core/4.0.1/files/81826
@Serializable
sealed class RequestIntent(val code: String) {
    @Serializable
    data object Proposal : RequestIntent("proposal")

    @Serializable
    data object Plan : RequestIntent("plan")

    @Serializable
    data object Directive : RequestIntent("directive")

    @Serializable
    data object Order : RequestIntent("order")

    @Serializable
    data object OriginalOrder : RequestIntent("original-order")

    @Serializable
    data object ReflexOrder : RequestIntent("reflex-order")

    @Serializable
    data object FillerOrder : RequestIntent("filler-order")

    @Serializable
    data object InstanceOrder : RequestIntent("instance-order")

    @Serializable
    data object Option : RequestIntent("option")

    @Serializable
    data class UnknownIntent(val value: String) : RequestIntent(value)

    companion object {
        private val knownIntents: Map<String, RequestIntent> = listOf(
            Proposal, Plan, Directive, Order, OriginalOrder, ReflexOrder,
            FillerOrder, InstanceOrder, Option
        ).associateBy { it.code }

        fun fromCode(code: String?): RequestIntent {
            return knownIntents[code] ?: UnknownIntent(code ?: "unknown")
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

fun isValidKbvVersion(version: String): Boolean {
    return KbvBundleVersion.entries
        .filter { it != KbvBundleVersion.UNKNOWN } // Exclude UNKNOWN
        .any { it.version == version }
}
