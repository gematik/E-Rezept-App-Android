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

package de.gematik.ti.erp.app.fhir.prescription.model.original

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMultiplePrescriptionInfoErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskAccidentType.Companion.getFhirTaskAccidentByType
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvMedicationRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirExtension.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequest.FhirMedicationRequestConstants.ACCIDENT_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequest.FhirMedicationRequestConstants.ADDITIONAL_FEE_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequest.FhirMedicationRequestConstants.EMERGENCY_FEE_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequest.FhirMedicationRequestConstants.IS_BVG_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequest.FhirMedicationRequestConstants.MULTIPLE_PRESCRIPTION_INFO_EXTENSION_URL
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestExtension.findAccidentDate
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestExtension.findAccidentLocation
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestExtension.findAccidentType
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestExtension.findPrescriptionIndicator
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestExtension.findPrescriptionPeriod
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestExtension.findPrescriptionRatio
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirRatio.Companion.toErpModel
import de.gematik.ti.erp.app.utils.FhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FhirMedicationRequest(
    @SerialName("_id") val commentsSection: FhirMedicationRequestComments? = null,
    @SerialName("meta") val resourceType: FhirMeta? = null,
    @SerialName("extension") val extensions: List<FhirExtension> = emptyList(),
    @SerialName("status") val status: String? = null,
    @SerialName("intent") val intent: String? = null,
    @SerialName("authoredOn") val authoredOn: String? = null,
    @SerialName("dosageInstruction") val dosageInstruction: List<FhirMedicationRequestDosageInstruction> = emptyList(),
    @SerialName("note") val note: List<FhirMedicationRequestText> = emptyList(),
    @SerialName("dispenseRequest") val dispenseRequest: FhirMedicationRequestDispenseRequest? = null,
    @SerialName("substitution") val substitution: FhirMedicationRequestSubstitution? = null
) {
    private object FhirMedicationRequestConstants {
        const val ACCIDENT_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident"
        const val MULTIPLE_PRESCRIPTION_INFO_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription"
        const val EMERGENCY_FEE_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee"
        const val IS_BVG_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG"
        const val ADDITIONAL_FEE_EXTENSION_URL = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_StatusCoPayment"
    }

    fun accidentInformationExtension() = extensions.find { it.url == ACCIDENT_EXTENSION_URL }
    fun multiplePrescriptionInfoExtension() = extensions.find { it.url == MULTIPLE_PRESCRIPTION_INFO_EXTENSION_URL }

    val isEmergencyFee = extensions.find { it.url == EMERGENCY_FEE_EXTENSION_URL }?.valueBoolean
    val isBvg = extensions.find { it.url == IS_BVG_EXTENSION_URL }?.valueBoolean == true
    val additionalFee = extensions.find { it.url == ADDITIONAL_FEE_EXTENSION_URL }?.valueCoding?.code
    val quantity = (dispenseRequest?.quantity?.value)?.toDoubleOrNull()?.toInt() ?: 0
    val dosageInstructionText = dosageInstruction.map { it.text }.firstOrNull()
    val noteText = note.map { it.text }.firstOrNull()

    companion object {
        private fun JsonElement.isValidMedicationRequest(): Boolean = isValidKbvResource(FhirKbvResourceType.MedicationRequest)

        fun JsonElement.getMedicationRequest(): FhirMedicationRequest? {
            if (!this.isValidMedicationRequest()) return null
            return runCatching {
                SafeJson.value.decodeFromJsonElement(serializer(), this)
            }.onFailure { Napier.e("Error parsing MedicationRequest: ${it.message}") }
                .getOrNull()
        }

        private fun toMultiplePrescriptionInfoErpModel(
            multiplePrescriptionInfo: FhirExtension?,
            period: FhirPeriod?,
            ratio: FhirRatio?
        ) = FhirMultiplePrescriptionInfoErpModel(
            indicator = multiplePrescriptionInfo?.extensions?.findPrescriptionIndicator() == true,
            start = period?.start?.let { FhirTemporal.LocalDate(LocalDate.parse(it)) },
            end = period?.end?.let { FhirTemporal.LocalDate(LocalDate.parse(it)) },
            numbering = ratio?.toErpModel()
        )

        fun FhirMedicationRequest.toErpModel(): FhirTaskKbvMedicationRequestErpModel? = runCatching {
            val accidentInformationExtension = accidentInformationExtension()
            val multiplePrescriptionInfoExtension = multiplePrescriptionInfoExtension()
            val period = multiplePrescriptionInfoExtension?.extensions?.findPrescriptionPeriod()
            val ratio = multiplePrescriptionInfoExtension?.extensions?.findPrescriptionRatio()

            return FhirTaskKbvMedicationRequestErpModel(
                authoredOn = authoredOn?.let { FhirTemporal.LocalDate(LocalDate.parse(it)) },
                dateOfAccident = accidentInformationExtension?.extensions?.findAccidentDate(),
                location = accidentInformationExtension?.extensions?.findAccidentLocation(),
                accidentType = accidentInformationExtension?.extensions?.findAccidentType() ?: FhirTaskAccidentType.None,
                emergencyFee = isEmergencyFee,
                substitutionAllowed = substitution?.allowed == true,
                dosageInstruction = dosageInstructionText,
                quantity = quantity,
                multiplePrescriptionInfo = toMultiplePrescriptionInfoErpModel(
                    multiplePrescriptionInfo = multiplePrescriptionInfoExtension,
                    period = period,
                    ratio = ratio
                ),
                additionalFee = additionalFee,
                bvg = isBvg,
                note = noteText
            )
        }.onFailure { Napier.e("Error parsing FhirTaskKbvMedicationRequestErpModel: ${it.message}") }.getOrNull()
    }
}

@Serializable
data class FhirMedicationRequestText(
    @SerialName("text") val text: String? = null
)

@Serializable
data class FhirMedicationRequestSubstitution(
    @SerialName("allowedBoolean") val allowed: Boolean? = null
)

@Serializable
data class FhirMedicationRequestDispenseRequest(
    @SerialName("quantity") val quantity: FhirMedicationRequestQuantityValue? = null
)

@Serializable
data class FhirMedicationRequestQuantityValue(
    @SerialName("value") val value: String? = null,
    @SerialName("system") val system: String? = null,
    @SerialName("code") val code: String? = null
)

@Serializable
data class FhirMedicationRequestDosageInstruction(
    @SerialName("text") val text: String? = null,
    // only valueBoolean is used in the code
    @SerialName("extension") val extensions: List<FhirExtension> = emptyList()
)

@Serializable
data class FhirMedicationRequestComments(
    @SerialName("fhir_comments") val comments: List<String>? = emptyList()
)

object FhirMedicationRequestExtension {
    private const val PRESCRIPTION_PERIOD = "zeitraum"
    private const val PRESCRIPTION_RATIO = "nummerierung"
    private const val ACCIDENT_DATE = "unfalltag"
    private const val ACCIDENT_LOCATION = "unfallbetrieb"
    private const val ACCIDENT_TYPE = "unfallkennzeichen"
    private const val PRESCRIPTION_INDICATOR = "kennzeichen"

    fun List<FhirExtension>.findPrescriptionPeriod(): FhirPeriod? =
        findExtensionByUrl(PRESCRIPTION_PERIOD)?.valuePeriod

    fun List<FhirExtension>.findPrescriptionRatio(): FhirRatio? =
        findExtensionByUrl(PRESCRIPTION_RATIO)?.valueRatio

    fun List<FhirExtension>.findPrescriptionIndicator(): Boolean? =
        findExtensionByUrl(PRESCRIPTION_INDICATOR)?.valueBoolean

    fun List<FhirExtension>.findAccidentDate() =
        findExtensionByUrl(ACCIDENT_DATE)?.valueDate?.let { FhirTemporal.LocalDate(LocalDate.parse(it)) }

    fun List<FhirExtension>.findAccidentLocation() =
        findExtensionByUrl(ACCIDENT_LOCATION)?.valueString

    fun List<FhirExtension>.findAccidentType() =
        findExtensionByUrl(ACCIDENT_TYPE)?.valueString?.let { getFhirTaskAccidentByType(it) } ?: FhirTaskAccidentType.None
}
