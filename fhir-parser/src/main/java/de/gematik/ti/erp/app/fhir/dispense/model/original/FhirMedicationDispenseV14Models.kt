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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirMeta
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.DIGA_DISPENSE_TYPE
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestDosageInstruction
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequestText
import de.gematik.ti.erp.app.fhir.serializer.MedicationReferenceSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@Serializable
sealed class FhirMedicationDispenseStandardModel(
    @Transient
    open val type: FhirMediationDispenseResourceType = FhirMediationDispenseResourceType.Unknown
)

/**
 * Represents the FHIR `MedicationDispense` resource for profile version 1.4 as defined in the
 * [gematik specification](https://simplifier.net/packages/de.gematik.erezept-workflow.r4/1.4.3/files/2550131).
 *
 * This model captures the dispense-related data of a medication, excluding the actual medication details,
 * which are expected to be resolved separately through a paired `Medication` resource.
 *
 * @property id Unique identifier for the dispense record.
 * @property meta Metadata related to the resource, including profile information.
 * @property identifier List of identifiers such as prescription IDs.
 * @property status The current dispense status (e.g., `completed`).
 * @property subject Reference to the patient (`subject.identifier`) the medication was dispensed for.
 * @property performer List of actors (e.g., pharmacists) who performed the dispensing.
 * @property whenHandedOver Date when the medication was handed over to the patient.
 * @property whenPrepared Date when the medication was prepared for dispense.
 * @property substitution Indicates if a substitution occurred and provides additional details.
 */
@Serializable
internal data class FhirMedicationDispenseV14V15DispenseModel(
    @SerialName("id") override val id: String? = null,
    @SerialName("meta") override val meta: FhirMeta? = null,
    @SerialName("identifier") override val identifier: List<FhirIdentifier> = emptyList(),
    @SerialName("status") override val status: String? = null,
    @SerialName("subject") override val subject: FhirMedicationDispenseIdentifier? = null,
    @SerialName("performer") override val performer: List<FhirMedicationDispenseActor> = emptyList(),
    @SerialName("whenHandedOver") override val whenHandedOver: String? = null,
    @SerialName("dosageInstruction") override val dosageInstruction: List<FhirMedicationRequestDosageInstruction> = emptyList(),
    @SerialName("whenPrepared") override val whenPrepared: String? = null,
    @SerialName("medicationReference")
    @Serializable(with = MedicationReferenceSerializer::class)
    override val medicationReference: FhirMedicationDispenseReference? = null,
    @SerialName("substitution") override val substitution: FhirMedicationDispenseSubstitution? = null,
    @SerialName("extension") override val extension: List<FhirExtension> = emptyList(),
    @SerialName("note") override val note: List<FhirMedicationRequestText> = emptyList()
) : FhirMedicationDispenseStandardModel(), MedicationDispenseCommon {
    override val type: FhirMediationDispenseResourceType
        get() = FhirMediationDispenseResourceType.MedicationDispense

    override fun isDigaType() =
        meta?.profiles?.any { it.contains(DIGA_DISPENSE_TYPE) } ?: false

    companion object Companion {
        /**
         * Decodes a `JsonElement` into a [FhirMedicationDispenseV14V15DispenseModel].
         *
         * @receiver The raw JSON payload of the `MedicationDispense` resource which comes as a pair with `Medication`.
         * @return Parsed [FhirMedicationDispenseV14V15DispenseModel] instance.
         */
        fun JsonElement.extractMedicationDispense(): FhirMedicationDispenseV14V15DispenseModel {
            return SafeJson.value.decodeFromJsonElement(serializer(), this)
        }
    }
}
