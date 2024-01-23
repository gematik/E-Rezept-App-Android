/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.model

import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.toStartOfDayInUTC
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

val CommunicationWaitStateDelta: Duration = 10.minutes

// gemSpec_FD_eRp: A_21267 Prozessparameter - Berechtigungen für Nutzer
const val DIRECT_ASSIGNMENT_INDICATOR = "169" // direct assignment taskID starts with 169
const val DIRECT_ASSIGNMENT_INDICATOR_PKV = "209" // pkv direct assignment taskID starts with 209

object SyncedTaskData {
    enum class TaskStatus {
        Ready, InProgress, Completed, Other, Draft, Requested, Received, Accepted, Rejected, Canceled, OnHold, Failed;
    }

    data class SyncedTask(
        val profileId: String,
        val taskId: String,
        val accessCode: String?,
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
        val medicationDispenses: List<MedicationDispense> = emptyList(),
        val communications: List<Communication> = emptyList()
    ) {
        sealed interface TaskState

        data class Ready(val expiresOn: Instant, val acceptUntil: Instant) : TaskState
        data class LaterRedeemable(val redeemableOn: Instant) : TaskState

        data class Pending(val sentOn: Instant, val toTelematikId: String) : TaskState
        data class InProgress(val lastModified: Instant) : TaskState
        data class Expired(val expiredOn: Instant) : TaskState

        data class Other(val state: TaskStatus, val lastModified: Instant) : TaskState

        fun state(now: Instant = Clock.System.now(), delta: Duration = CommunicationWaitStateDelta): TaskState =
            when {
                medicationRequest.multiplePrescriptionInfo.indicator &&
                    medicationRequest.multiplePrescriptionInfo.start?.let { start ->
                    start > now
                } == true -> {
                    LaterRedeemable(medicationRequest.multiplePrescriptionInfo.start)
                }

                // expiration date is issue day + 3 months until 0:00 AM on that day
                expiresOn != null && expiresOn <= now.toStartOfDayInUTC() && status != TaskStatus.Completed ->
                    Expired(expiresOn)

                status == TaskStatus.Ready && accessCode != null &&
                    communications.any { it.profile == CommunicationProfile.ErxCommunicationDispReq } &&
                    redeemState(now, delta) == RedeemState.NotRedeemable -> {
                    val comm = this.communications
                        .filter { it.profile == CommunicationProfile.ErxCommunicationDispReq }
                        .maxBy { it.sentOn }

                    Pending(
                        sentOn = comm.sentOn,
                        toTelematikId = comm.recipient
                    )
                }

                status == TaskStatus.Ready -> Ready(
                    // Expires on "expiresOn"-day at 0:00 AM.
                    // Minus 1 day to use it as the last possible day of redeemability
                    expiresOn = requireNotNull(expiresOn?.minus(1.days)),
                    // Not Redeemable at the cost of the healthinsurancecompany (HI) on this day at 0:00 AM
                    // Minus 1 day to use it as the last possible day of redeemability at the cost of the HI
                    acceptUntil = requireNotNull(acceptUntil?.minus(1.days))
                )

                status == TaskStatus.InProgress -> InProgress(lastModified = this.lastModified)
                else -> Other(this.status, this.lastModified)
            }

        enum class RedeemState {
            NotRedeemable,
            RedeemableAndValid,
            RedeemableAfterDelta;

            fun isRedeemable() = this != NotRedeemable
        }

        fun redeemedOn() =
            if (status == TaskStatus.Completed) {
                medicationDispenses.firstOrNull()?.whenHandedOver?.toInstant() ?: lastModified
            } else {
                null
            }

        /**
         * The list of redeemable prescriptions. Should NOT be used as a filter for the active/archive tab!
         * See [isActive] for a decision it this prescription should be shown in the "Active" or "Archive" tab.
         */
        fun redeemState(now: Instant = Clock.System.now(), delta: Duration = CommunicationWaitStateDelta): RedeemState {
            val expired = (expiresOn != null && expiresOn <= now.toStartOfDayInUTC())
            val redeemableLater = medicationRequest.multiplePrescriptionInfo.indicator &&
                medicationRequest.multiplePrescriptionInfo.start?.let {
                it > now
            } == true
            val ready = status == TaskStatus.Ready
            val valid = accessCode != null
            val latestDispenseReqCommunication = communications
                .filter { it.profile == CommunicationProfile.ErxCommunicationDispReq }
                .maxOfOrNull { it.sentOn }
            // if lastModified is more recent than the latest disp req, we can be sure that something
            // happened with the task (e.g. claimed -> rejected)
            val isDeltaLocked = latestDispenseReqCommunication?.let { lastModified < it && (it + delta) > now }

            return when {
                redeemableLater || expired -> RedeemState.NotRedeemable
                ready && valid && latestDispenseReqCommunication == null -> RedeemState.RedeemableAndValid
                ready && valid && isDeltaLocked == false -> RedeemState.RedeemableAfterDelta
                ready && valid && isDeltaLocked == true -> RedeemState.NotRedeemable
                else -> RedeemState.NotRedeemable
            }
        }

        fun isActive(now: Instant = Clock.System.now()): Boolean {
            val expired = expiresOn != null && expiresOn <= now.toStartOfDayInUTC()
            val allowedStatus = status == TaskStatus.Ready || status == TaskStatus.InProgress
            return !expired && allowedStatus
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

    data class Organization(
        val name: String? = null,
        val address: Address? = null,
        val uniqueIdentifier: String? = null,
        val phone: String? = null,
        val mail: String? = null
    )

    data class Practitioner(
        val name: String?,
        val qualification: String?,
        val practitionerIdentifier: String?
    )

    data class Patient(
        val name: String?,
        val address: Address?,
        val birthdate: FhirTemporal?,
        val insuranceIdentifier: String?
    )

    data class InsuranceInformation(
        val name: String? = null,
        val status: String? = null
    )

    enum class AdditionalFee(val value: String?) {
        None(null),
        NotExempt("0"),
        Exempt("1"),
        ArtificialFertilization("2");

        companion object {
            fun valueOf(v: String?) =
                values().find {
                    it.value == v
                } ?: None
        }
    }

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

    data class MultiplePrescriptionInfo(
        val indicator: Boolean = false,
        val numbering: Ratio? = null,
        val start: Instant? = null,
        val end: Instant? = null
    )

    enum class AccidentType {
        Unfall,
        Arbeitsunfall,
        Berufskrankheit,
        None
    }

    data class MedicationDispense(
        val dispenseId: String?,
        val patientIdentifier: String,
        val medication: Medication?,
        val wasSubstituted: Boolean,
        val dosageInstruction: String?,
        val performer: String,
        val whenHandedOver: FhirTemporal?
    )

    enum class MedicationCategory {
        ARZNEI_UND_VERBAND_MITTEL,
        BTM,
        AMVV,
        SONSTIGES,
        UNKNOWN;
    }

    data class Quantity(
        val value: String,
        val unit: String
    )

    data class Ratio(
        val numerator: Quantity?,
        val denominator: Quantity?
    )

    data class Ingredient(
        var text: String,
        var form: String?,
        var number: String?,
        var amount: String?,
        var strength: Ratio?
    )

    sealed interface Medication {
        fun name(): String

        val category: MedicationCategory
        val vaccine: Boolean
        val text: String
        val form: String?
        val lotNumber: String?
        val expirationDate: FhirTemporal?
    }

    data class MedicationFreeText(
        override val category: MedicationCategory,
        override val vaccine: Boolean,
        override val text: String,
        override val form: String?,
        override val lotNumber: String?,
        override val expirationDate: FhirTemporal?
    ) : Medication {
        override fun name(): String = text
    }

    data class MedicationIngredient(
        override val category: MedicationCategory,
        override val vaccine: Boolean,
        override val text: String,
        override val form: String?,
        override val lotNumber: String?,
        override val expirationDate: FhirTemporal?,
        val normSizeCode: String?,
        val amount: Ratio?,
        val ingredients: List<Ingredient>

    ) : Medication {
        override fun name(): String = joinIngredientNames(ingredients)
    }

    data class MedicationCompounding(
        override val category: MedicationCategory,
        override val vaccine: Boolean,
        override val text: String,
        override val form: String?,
        override val lotNumber: String?,
        override val expirationDate: FhirTemporal?,
        val manufacturingInstructions: String?,
        val packaging: String?,
        val amount: Ratio?,
        val ingredients: List<Ingredient>

    ) : Medication {
        override fun name(): String = joinIngredientNames(ingredients)
    }

    data class MedicationPZN(
        override val category: MedicationCategory,
        override val vaccine: Boolean,
        override val text: String,
        override val form: String?,
        override val lotNumber: String?,
        override val expirationDate: FhirTemporal?,
        val uniqueIdentifier: String,
        val normSizeCode: String?,
        val amount: Ratio?

    ) : Medication {
        override fun name() = text
    }

    fun joinIngredientNames(ingredients: List<Ingredient>) =
        ingredients.joinToString(", ") { ingredient ->
            ingredient.text
        }
}
