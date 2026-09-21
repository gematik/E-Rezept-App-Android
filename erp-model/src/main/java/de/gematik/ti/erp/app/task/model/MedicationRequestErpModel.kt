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

package de.gematik.ti.erp.app.task.model

import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO: https://service.gematik.de/browse/ERA-13163

/**
 * copied [SyncedTaskData.MedicationRequest]
 */
@Serializable
@SerialName("MedicationRequest")
data class MedicationRequestErpModel(
    val medication: MedicationErpModel? = null,
    val authoredOn: FhirTemporal? = null,
    val dateOfAccident: Instant? = null,
    val accidentType: AccidentType = AccidentType.None,
    val location: String? = null,
    val emergencyFee: Boolean? = null,
    val substitutionAllowed: Boolean = true,
    val dosageInstruction: String? = null,
    val multiplePrescriptionInfo: MultiplePrescriptionInfo = MultiplePrescriptionInfo(),
    val quantity: Int = 0,
    val note: String? = null,
    val bvg: Boolean? = null,
    val additionalFee: AdditionalFeeErpModel = AdditionalFeeErpModel.None,
    val teratogenicPrescription: TeratogenicPrescriptionErpModel? = null
) {
    fun isTeratogenic(): Boolean = teratogenicPrescription != null
}

/**
 * copied [SyncedTaskData.TeratogenicPrescriptionErpModel]
 */
@Serializable
data class TeratogenicPrescriptionErpModel(
    val offLabel: Boolean,
    val gebaerfaehigeFrau: Boolean,
    val einhaltungSicherheitsmassnahmen: Boolean,
    val aushaendigungInformationsmaterialien: Boolean,
    val erklaerungSachkenntnis: Boolean
)

/**
 * copied [SyncedTaskData.MultiplePrescriptionInfo]
 */
@Serializable
data class MultiplePrescriptionInfo(
    val indicator: Boolean = false,
    val numbering: RatioErpModel? = null, // still in common
    val start: Instant? = null,
    val end: Instant? = null
)

// todo: check if we need to map again here
/**
 * copied [SyncedTaskData.AccidentType]
 */
@Serializable
enum class AccidentType {
    Unfall,
    Arbeitsunfall,
    Berufskrankheit,
    None
}

/**
 * copied [SyncedTaskData.AdditionalFee]
 */
@Serializable
enum class AdditionalFeeErpModel(val value: String?) {
    None(null),
    NotExempt("0"),
    Exempt("1"),
    ArtificialFertilization("2")
    ;

    companion object {
        fun valueOf(v: String?) =
            entries.find {
                it.value == v
            } ?: None
    }
}
