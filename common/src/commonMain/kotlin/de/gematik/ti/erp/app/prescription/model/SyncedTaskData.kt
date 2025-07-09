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

package de.gematik.ti.erp.app.prescription.model

import de.gematik.ti.erp.app.fhir.dispense.model.erp.FhirDispenseDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.toLocalDate
import de.gematik.ti.erp.app.utils.toStartOfDayInUTC
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.daysUntil
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

val CommunicationWaitStateDelta: Duration = 10.minutes

// gemSpec_FD_eRp: A_21267 Prozessparameter - Berechtigungen für Nutzer
const val DIRECT_ASSIGNMENT_INDICATOR = "169" // direct assignment taskID starts with 169
const val DIRECT_ASSIGNMENT_INDICATOR_PKV = "209" // pkv direct assignment taskID starts with 209

object SyncedTaskData {
    enum class TaskStatus {
        Ready, InProgress, Completed, Other, Draft, Requested, Received, Accepted, Rejected, Canceled, OnHold, Failed
    }

    enum class TaskStateSerializationType {
        Ready,
        Deleted,
        LaterRedeemable,
        Pending,
        InProgress,
        Expired,
        Provided,
        Other
    }

    enum class CoverageType {
        GKV, // Gesetzliche Krankenversicherung
        PKV, // Private Krankenversicherung
        BG, // Berufsgenossenschaft
        SEL, // Selbstzahler
        SOZ, // Sozialamt
        GPV, // Gesetzliche Pflegeversicherung
        PPV, // Private Pflegeversicherung
        BEI, // Beihilfe
        UNKNOWN;

        companion object {
            fun mapTo(value: String?): CoverageType =
                try {
                    valueOf(value ?: UNKNOWN.toString())
                } catch (e: Throwable) {
                    UNKNOWN
                }
        }
    }

    data class SyncedTask(
        val profileId: String,
        val taskId: String,
        val accessCode: String,
        val lastModified: Instant,
        val organization: Organization,
        val practitioner: Practitioner,
        val patient: Patient,
        val insuranceInformation: InsuranceInformation,
        val expiresOn: Instant?,
        val acceptUntil: Instant?,
        val authoredOn: Instant,
        val status: TaskStatus,
        var isIncomplete: Boolean,
        var pvsIdentifier: String,
        var failureToReport: String,
        val medicationRequest: MedicationRequest,
        val currentTime: Instant = Clock.System.now(), // TODO: figure out a way to remove this and make previews work
        val medicationDispenses: List<MedicationDispense> = emptyList(),
        val lastMedicationDispense: Instant?,
        val deviceRequest: FhirTaskKbvDeviceRequestErpModel? = null,
        val communications: List<Communication> = emptyList()
    ) {
        @Serializable(with = TaskStateSyncedTaskDataSerializer::class)
        sealed interface TaskState {
            val type: TaskStateSerializationType
        }

        @Serializable
        @SerialName("Ready")
        data class Ready(
            override val type: TaskStateSerializationType = TaskStateSerializationType.Ready,
            val expiresOn: Instant,
            val acceptUntil: Instant
        ) : TaskState {
            // -1 because on the day of acceptUntil, the prescription is not paid by the health insurance
            fun acceptDaysLeft(now: Instant): Int =
                now.toLocalDate().daysUntil(acceptUntil.minus(1.days).toLocalDate())

            // -1 because on the day of expiresOn, the prescription is not redeemable
            fun expiryDaysLeft(now: Instant): Int =
                now.toLocalDate().daysUntil(expiresOn.minus(1.days).toLocalDate())
        }

        @Serializable
        @SerialName("Deleted")
        data class Deleted(
            override val type: TaskStateSerializationType = TaskStateSerializationType.Deleted,
            val lastModified: Instant
        ) : TaskState

        @Serializable
        @SerialName("LaterRedeemable")
        data class LaterRedeemable(
            override val type: TaskStateSerializationType = TaskStateSerializationType.LaterRedeemable,
            val redeemableOn: Instant
        ) : TaskState

        @Serializable
        @SerialName("Pending")
        data class Pending(
            override val type: TaskStateSerializationType = TaskStateSerializationType.Pending,
            val sentOn: Instant,
            val toTelematikId: String
        ) : TaskState

        @Serializable
        @SerialName("InProgress")
        data class InProgress(
            override val type: TaskStateSerializationType = TaskStateSerializationType.InProgress,
            val lastModified: Instant
        ) : TaskState

        @Serializable
        @SerialName("Expired")
        data class Expired(
            override val type: TaskStateSerializationType = TaskStateSerializationType.Expired,
            val expiredOn: Instant
        ) : TaskState

        @Serializable
        @SerialName("InProgress")
        data class Provided(
            override val type: TaskStateSerializationType = TaskStateSerializationType.Provided,
            val lastMedicationDispense: Instant
        ) : TaskState

        @Serializable
        @SerialName("Other")
        data class Other(
            override val type: TaskStateSerializationType = TaskStateSerializationType.Other,
            val state: TaskStatus,
            val lastModified: Instant
        ) : TaskState

        object TaskStateSyncedTaskDataSerializer : JsonContentPolymorphicSerializer<TaskState>(
            TaskState::class
        ) {
            override fun selectDeserializer(element: JsonElement): KSerializer<out TaskState> {
                element.jsonObject["type"]?.jsonPrimitive?.content?.let { classType ->
                    return when (TaskStateSerializationType.valueOf(classType)) {
                        TaskStateSerializationType.Ready -> Ready.serializer()
                        TaskStateSerializationType.Deleted -> Deleted.serializer()
                        TaskStateSerializationType.LaterRedeemable -> LaterRedeemable.serializer()
                        TaskStateSerializationType.Pending -> Pending.serializer()
                        TaskStateSerializationType.InProgress -> InProgress.serializer()
                        TaskStateSerializationType.Expired -> Expired.serializer()
                        TaskStateSerializationType.Provided -> Provided.serializer()
                        TaskStateSerializationType.Other -> Other.serializer()
                    }
                } ?: throw SerializationException(
                    "TaskStateSyncedTaskDataSerializer: key 'type' not found or does not match any task state type"
                )
            }
        }

        @Suppress("CyclomaticComplexMethod")
        fun state(now: Instant = currentTime, delta: Duration = CommunicationWaitStateDelta): TaskState =
            when {
                medicationRequest.multiplePrescriptionInfo.indicator &&
                    medicationRequest.multiplePrescriptionInfo.start?.let { start ->
                    start > now
                } == true -> {
                    LaterRedeemable(redeemableOn = medicationRequest.multiplePrescriptionInfo.start)
                }

                // expiration date is issue day + 3 months until 0:00 AM on that day
                expiresOn != null && expiresOn <= now.toStartOfDayInUTC() && status != TaskStatus.Completed ->
                    Expired(expiredOn = expiresOn)

                status == TaskStatus.Ready && communications.any {
                    it.profile == CommunicationProfile.ErxCommunicationDispReq
                } && redeemState(now, delta) == RedeemState.RedeemableAfterDelta -> {
                    val comm = this.communications
                        .filter { it.profile == CommunicationProfile.ErxCommunicationDispReq }
                        .maxBy { it.sentOn }

                    Pending(
                        sentOn = comm.sentOn,
                        toTelematikId = comm.recipient
                    )
                }

                status == TaskStatus.Ready -> Ready(
                    expiresOn = requireNotNull(expiresOn) { "expiresOn is wrong" },
                    acceptUntil = requireNotNull(acceptUntil) { "acceptUntil is wrong" }
                )

                status == TaskStatus.Canceled -> Deleted(lastModified = this.lastModified)
                status != TaskStatus.Completed && lastMedicationDispense != null -> Provided(
                    lastMedicationDispense = this.lastMedicationDispense
                )

                status == TaskStatus.InProgress -> InProgress(lastModified = this.lastModified)
                else -> Other(state = status, lastModified = this.lastModified)
            }

        enum class RedeemState {
            NotRedeemable,
            RedeemableAndValid,
            RedeemableAfterDelta;

            fun isRedeemable() = this == RedeemableAndValid
        }

        fun redeemedOn() =
            if (status == TaskStatus.Completed) {
                lastModified
            } else {
                null
            }

        /**
         * The list of redeemable prescriptions. Should NOT be used as a filter for the active/archive tab!
         * See [isActive] for a decision it this prescription should be shown in the "Active" or "Archive" tab.
         */
        @Suppress("CyclomaticComplexMethod")
        fun redeemState(now: Instant = Clock.System.now(), delta: Duration = CommunicationWaitStateDelta): RedeemState {
            val expired = (expiresOn != null && expiresOn <= now.toStartOfDayInUTC())
            val redeemableLater = medicationRequest.multiplePrescriptionInfo.indicator &&
                medicationRequest.multiplePrescriptionInfo.start?.let {
                it > now
            } == true
            val ready = status == TaskStatus.Ready
            val inProgress = status == TaskStatus.InProgress
            val latestDispenseReqCommunication = communications
                .filter { it.profile == CommunicationProfile.ErxCommunicationDispReq }
                .maxOfOrNull { it.sentOn }

            val isDeltaLocked = latestDispenseReqCommunication?.let { lastModified < it && (it + delta) > now } ?: false
            val valid = accessCode.isNotEmpty()

            return when {
                redeemableLater || expired || inProgress -> RedeemState.NotRedeemable
                ready && valid && isDeltaLocked -> RedeemState.RedeemableAfterDelta
                ready && valid && !isDeltaLocked -> RedeemState.RedeemableAndValid
                else -> RedeemState.NotRedeemable
            }
        }

        fun isActive(now: Instant = Clock.System.now()): Boolean {
            val expired = expiresOn != null && expiresOn <= now.toStartOfDayInUTC()
            val wasActiveAndThenCanceled = !expired && medicationDispenses.isEmpty() && status == TaskStatus.Canceled
            val allowedStatus = status in setOf(TaskStatus.Ready, TaskStatus.InProgress)
            return (!expired && allowedStatus) || wasActiveAndThenCanceled
        }

        fun isDirectAssignment() =
            taskId.startsWith(DIRECT_ASSIGNMENT_INDICATOR) || taskId.startsWith(DIRECT_ASSIGNMENT_INDICATOR_PKV)

        fun isDeletable() =
            when {
                isDirectAssignment() -> status == TaskStatus.Completed
                else -> true
            }

        fun organizationName() = organization.name ?: practitioner.name
        fun medicationName(): String? = medicationRequest.medication?.name()
    }

    @Serializable
    data class Address(
        val line1: String,
        val line2: String,
        val postalCode: String,
        val city: String
    ) {
        fun joinToString(): String =
            listOf(
                this.line1,
                this.line2,
                this.postalCode + " " + this.city
            ).filter {
                it.isNotEmpty()
            }.joinToString(System.getProperty("line.separator"))

        fun joinToHtmlString(): String =
            listOf(
                this.line1,
                this.line2,
                this.postalCode + " " + this.city
            ).filter {
                it.isNotEmpty()
            }.joinToString("<br>")
    }

    @Serializable
    data class Organization(
        val name: String? = null,
        val address: Address? = null,
        val uniqueIdentifier: String? = null,
        val phone: String? = null,
        val mail: String? = null
    )

    @Serializable
    data class Practitioner(
        val name: String?,
        val qualification: String?,
        val practitionerIdentifier: String?
    )

    @Serializable
    data class Patient(
        val name: String?,
        val address: Address?,
        val birthdate: FhirTemporal?,
        val insuranceIdentifier: String?
    )

    @Serializable
    data class InsuranceInformation(
        val name: String? = null,
        val status: String? = null,
        val identifierNumber: String? = null,
        val coverageType: CoverageType
    ) {
        val digaIdentifier = identifierNumber
    }

    @Serializable
    enum class AdditionalFee(val value: String?) {
        None(null),
        NotExempt("0"),
        Exempt("1"),
        ArtificialFertilization("2");

        companion object {
            fun valueOf(v: String?) =
                entries.find {
                    it.value == v
                } ?: None
        }
    }

    @Serializable
    @SerialName("MedicationRequest")
    data class MedicationRequest(
        val medication: Medication? = null,
        val authoredOn: FhirTemporal? = null,
        val dateOfAccident: Instant? = null,
        val accidentType: AccidentType = AccidentType.None,
        val location: String? = null,
        val emergencyFee: Boolean? = null,
        val substitutionAllowed: Boolean,
        val dosageInstruction: String? = null,
        val multiplePrescriptionInfo: MultiplePrescriptionInfo,
        val quantity: Int = 0,
        val note: String?,
        val bvg: Boolean? = null,
        val additionalFee: AdditionalFee = AdditionalFee.valueOf(null)
    )

    @Serializable
    data class MultiplePrescriptionInfo(
        val indicator: Boolean = false,
        val numbering: Ratio? = null,
        val start: Instant? = null,
        val end: Instant? = null
    )

    @Serializable
    enum class AccidentType {
        Unfall,
        Arbeitsunfall,
        Berufskrankheit,
        None
    }

    @Serializable
    @SerialName("MedicationDispense")
    data class MedicationDispense(
        val dispenseId: String?,
        val patientIdentifier: String,
        val medication: Medication?,
        val deviceRequest: FhirDispenseDeviceRequestErpModel?,
        val wasSubstituted: Boolean,
        val dosageInstruction: String?,
        val performer: String,
        val whenHandedOver: FhirTemporal?
    )

    @Serializable
    enum class MedicationCategory {
        ARZNEI_UND_VERBAND_MITTEL,
        BTM,
        AMVV,
        SONSTIGES,
        UNKNOWN
    }

    @Serializable
    data class Ingredient(
        var text: String,
        var form: String?,
        var number: String?,
        var amount: String?,
        var strength: Ratio?
    )

    @Serializable
    data class Identifier(
        var pzn: String? = null,
        var atc: String? = null,
        var ask: String? = null,
        var snomed: String? = null
    )

    @Serializable
    @SerialName("Medication")
    data class Medication(
        val category: MedicationCategory,
        val vaccine: Boolean,
        val text: String,
        val form: String?,
        val lotNumber: String?,
        val expirationDate: FhirTemporal?,
        val identifier: Identifier,
        val normSizeCode: String?,
        val amount: Ratio?,
        val manufacturingInstructions: String?,
        val packaging: String?,
        val ingredientMedications: List<Medication?>,
        val ingredients: List<Ingredient>
    ) {
        fun name() = text.ifEmpty {
            joinIngredientNames(ingredients)
        }
    }

    fun joinIngredientNames(ingredients: List<Ingredient>) =
        ingredients.joinToString(", ") { ingredient ->
            ingredient.text
        }
}
