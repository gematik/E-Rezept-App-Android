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

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel

import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.diga.model.mapToDigaStatus
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskStatusErpModel
import de.gematik.ti.erp.app.fhir.temporal.toStartOfDayInUTC
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Time delta used for determining if a task's communication is in a wait state.
 */
internal val CommunicationWaitStateDelta: Duration = 10.minutes

const val DIRECT_ASSIGNMENT_INDICATOR = "169" // direct assignment taskID starts with 169
const val DIRECT_ASSIGNMENT_INDICATOR_PKV = "209" // pkv direct assignment taskID starts with 209

/**
 * Maps [TaskStatusEnum] to [FhirTaskStatusErpModel] for use in [TaskStateErpModel.Other].
 */
private fun TaskStatusEnum.toFhirTaskStatusErpModel(): FhirTaskStatusErpModel = when (this) {
    TaskStatusEnum.Ready -> FhirTaskStatusErpModel.Ready
    TaskStatusEnum.InProgress -> FhirTaskStatusErpModel.InProgress
    TaskStatusEnum.Completed -> FhirTaskStatusErpModel.Completed
    TaskStatusEnum.Canceled -> FhirTaskStatusErpModel.Canceled
    TaskStatusEnum.Accepted -> FhirTaskStatusErpModel.Accepted
    TaskStatusEnum.Draft -> FhirTaskStatusErpModel.Draft
    TaskStatusEnum.Failed -> FhirTaskStatusErpModel.Failed
    TaskStatusEnum.OnHold -> FhirTaskStatusErpModel.OnHold
    TaskStatusEnum.Requested -> FhirTaskStatusErpModel.Requested
    TaskStatusEnum.Received -> FhirTaskStatusErpModel.Received
    TaskStatusEnum.Rejected -> FhirTaskStatusErpModel.Rejected
    TaskStatusEnum.Other -> FhirTaskStatusErpModel.Other
}

// merged from [SyncedTaskData] and [ScannedTaskData] and [PrescriptionData]

/**
 * Represents the e-receipt task model in the ERP app.
 *
 * This model is a consolidated representation of prescription tasks, which can be either
 * [Scanned] from a physical QR code or [Synced] from the Telematik Infrastructure (TI).
 *
 * The [TaskErpModel] provides common properties shared across all types of tasks,
 * such as task identifiers, access codes, and redemption status.
 *
 * It serves as the domain model used within the app's task-related use cases and UI.
 */
@Serializable
sealed interface TaskErpModel {

    val profileId: ProfileIdentifier
    val name: String?
    val taskId: String
    val redeemedOn: Instant?
    val accessCode: String
    val isEuRedeemable: Boolean

    /**
     * A combination of scannedOn and authoredOn
     */
    val startedOn: Instant?

    /**
     * A combination of redeemedOn and expiresOn
     */
    val endedOn: Instant?

    val communications: List<CommunicationErpModel>

    /**
     * Checks if the task is currently active (i.e., not yet redeemed or expired).
     */
    fun isActive(): Boolean

    /**
     * Checks if the task is ready for processing or redemption.
     */
    fun isReady(): Boolean

    /**
     * Returns the display name of the medication or prescription.
     */
    fun medicationName(): String?

    /**
     * Represents a task that was added to the app by scanning a QR code.
     *
     * Scanned tasks initially contain minimal information until they are synchronized with the TI.
     *
     * @property profileId The identifier of the owner profile.
     * @property taskId The unique task identifier.
     * @property redeemedOn The redemption timestamp, if available.
     * @property accessCode The access code from the scanned QR code.
     * @property name An optional label for the task.
     * @property isEuRedeemable Whether the scanned task is marked for EU redemption.
     * @property scannedOn The timestamp when the QR code was scanned.
     * @property index An internal index used for ordering scanned tasks.
     */
    @Serializable
    data class Scanned(
        override val profileId: ProfileIdentifier,
        override val taskId: String,
        override val redeemedOn: Instant?,
        override val accessCode: String,
        override val name: String?,
        override val isEuRedeemable: Boolean,
        override val communications: List<CommunicationErpModel> = emptyList(),
        val scannedOn: Instant,
        val index: Int
    ) : TaskErpModel {
        override fun isActive(): Boolean = redeemedOn == null
        override fun isReady(): Boolean = redeemedOn == null
        override fun medicationName(): String? = name
        override val startedOn: Instant get() = scannedOn
        override val endedOn: Instant? get() = redeemedOn

        fun isRedeemable() = redeemedOn == null
    }

    /**
     * Represents a task that has been synchronized with the Task Service.
     *
     * [Synced] tasks contain detailed clinical and administrative information retrieved from the TI,
     * including patient info, practitioner details, and expiry dates.
     */
    @Serializable
    sealed interface Synced : TaskErpModel {
        val lastModified: Instant
        val isEuRedeemableByPatientAuthorization: Boolean
        val organization: OrganizationErpModel?
        val practitioner: PractitionerErpModel?
        val patient: PatientErpModel?
        val insuranceInformation: InsuranceErpModel?
        val expiresOn: Instant?
        val acceptUntil: Instant?
        val authoredOn: Instant
        val status: TaskStatusEnum
        var isIncomplete: Boolean
        var pvsIdentifier: String
        var failureToReport: String
        val currentTime: Instant

        /**
         * Calculates the current state of the task (e.g., waiting, ready, overdue).
         *
         * Implementation logic originally located at [SyncedTaskData line 213].
         *
         * @param now The reference time for the calculation.
         * @param delta The time buffer used for state transitions.
         */
        fun state(now: Instant = currentTime, delta: Duration = CommunicationWaitStateDelta): TaskStateErpModel

        /**
         * Validates or retrieves the redemption timestamp.
         * Returns [lastModified] if the task is [TaskStatusEnum.Completed], otherwise null.
         */
        fun redeemedOn(): Instant?

        /**
         * Determines the specific redemption state of the task.
         *
         * @param now The reference time for the calculation.
         * @param delta The time buffer used for state transitions.
         */
        fun redeemState(now: Instant = currentTime, delta: Duration = CommunicationWaitStateDelta): RedeemStateErpModel

        /**
         * Checks if the task is a direct assignment (Direktzuweisung).
         */
        fun isDirectAssignment(): Boolean

        /**
         * Checks if the task can be safely deleted by the user.
         */
        fun isDeletable(): Boolean

        /**
         * Returns the name of the associated organization.
         */
        fun organizationName(): String

        /**
         * Returns the name of the prescribed medication.
         */
        override fun medicationName(): String?

        /**
         * A synchronized [Prescription] task.
         *
         * This is the most common type of synced task, representing a standard medication prescription.
         *
         * @property medicationRequest The clinical request for medication.
         * @property medicationDispenses List of dispense records associated with this prescription.
         * @property lastMedicationDispense Timestamp of the most recent dispense action.
         */
        @Serializable
        data class Prescription(
            override val profileId: ProfileIdentifier,
            override val name: String?,
            override val taskId: String,
            override val accessCode: String,
            override val isEuRedeemable: Boolean,
            override val communications: List<CommunicationErpModel> = emptyList(),
            override val lastModified: Instant,
            override val isEuRedeemableByPatientAuthorization: Boolean,
            override val organization: OrganizationErpModel?,
            override val practitioner: PractitionerErpModel?,
            override val patient: PatientErpModel?,
            override val insuranceInformation: InsuranceErpModel?,
            override val expiresOn: Instant?,
            override val acceptUntil: Instant?,
            override val authoredOn: Instant,
            override val status: TaskStatusEnum,
            override var isIncomplete: Boolean,
            override var pvsIdentifier: String,
            override var failureToReport: String,
            override val currentTime: Instant = Clock.System.now(),
            val medicationRequest: MedicationRequestErpModel?,
            val medicationDispenses: List<MedicationDispenseErpModel> = emptyList(),
            val lastMedicationDispense: Instant? = null
        ) : Synced {
            override val redeemedOn: Instant? get() = redeemedOn()
            override val startedOn: Instant get() = authoredOn
            override val endedOn: Instant? get() = redeemedOn() ?: expiresOn

            override fun state(now: Instant, delta: Duration): TaskStateErpModel {
                val isPending = status == TaskStatusEnum.Ready && communications.any {
                    it.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq
                } && redeemState(now, delta) == RedeemStateErpModel.RedeemableAfterDelta

                return when {
                    medicationRequest?.multiplePrescriptionInfo?.indicator == true &&
                        medicationRequest.multiplePrescriptionInfo.start?.let { it > now } == true ->
                        TaskStateErpModel.LaterRedeemable(
                            redeemableOn = requireNotNull(medicationRequest.multiplePrescriptionInfo.start)
                        )

                    expiresOn != null && expiresOn <= now.toStartOfDayInUTC() && status != TaskStatusEnum.Completed ->
                        TaskStateErpModel.Expired(expiredOn = expiresOn)

                    isPending -> {
                        val comm = communications
                            .filter { it.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq }
                            .maxBy { it.timeStamp ?: Instant.DISTANT_PAST }
                        TaskStateErpModel.Pending(
                            sentOn = comm.timeStamp ?: Instant.DISTANT_PAST,
                            toTelematikId = comm.recipient
                        )
                    }

                    status == TaskStatusEnum.Ready -> TaskStateErpModel.Ready(
                        expiresOn = requireNotNull(expiresOn) { "expiresOn is null for Ready task $taskId" },
                        acceptUntil = requireNotNull(acceptUntil) { "acceptUntil is null for Ready task $taskId" }
                    )

                    status == TaskStatusEnum.Canceled ->
                        TaskStateErpModel.Deleted(lastModified = lastModified)

                    status != TaskStatusEnum.Completed && lastMedicationDispense != null ->
                        TaskStateErpModel.Provided(lastMedicationDispense = lastMedicationDispense)

                    status == TaskStatusEnum.InProgress ->
                        TaskStateErpModel.InProgress(lastModified = lastModified)

                    else -> TaskStateErpModel.Other(
                        state = status.toFhirTaskStatusErpModel(),
                        lastModified = lastModified
                    )
                }
            }

            override fun redeemedOn(): Instant? =
                if (status == TaskStatusEnum.Completed) lastModified else null

            @Suppress("CyclomaticComplexMethod")
            override fun redeemState(now: Instant, delta: Duration): RedeemStateErpModel {
                val expired = expiresOn != null && expiresOn <= now.toStartOfDayInUTC()
                val redeemableLater = medicationRequest?.multiplePrescriptionInfo?.indicator == true &&
                    medicationRequest.multiplePrescriptionInfo.start?.let { it > now } == true
                val ready = status == TaskStatusEnum.Ready
                val inProgress = status == TaskStatusEnum.InProgress
                val latestDispenseReqCommunication = communications
                    .filter { it.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq }
                    .maxOfOrNull { it.timeStamp ?: Instant.DISTANT_PAST }

                val isDeltaLocked = latestDispenseReqCommunication?.let { lastModified < it && (it + delta) > now } ?: false
                val valid = accessCode.isNotEmpty()
                return when {
                    redeemableLater || expired || inProgress -> RedeemStateErpModel.NotRedeemable
                    ready && valid && isDeltaLocked -> RedeemStateErpModel.RedeemableAfterDelta
                    ready && valid -> RedeemStateErpModel.RedeemableAndValid
                    else -> RedeemStateErpModel.NotRedeemable
                }
            }

            override fun isDirectAssignment(): Boolean =
                taskId.startsWith(DIRECT_ASSIGNMENT_INDICATOR) ||
                    taskId.startsWith(DIRECT_ASSIGNMENT_INDICATOR_PKV)

            override fun isDeletable(): Boolean =
                if (isDirectAssignment()) status == TaskStatusEnum.Completed else true

            override fun organizationName(): String =
                organization?.name ?: practitioner?.name ?: ""

            override fun medicationName(): String? =
                medicationRequest?.medication?.name()?.takeIf { it.isNotEmpty() }

            override fun isActive(): Boolean {
                val expired = expiresOn != null && expiresOn <= currentTime.toStartOfDayInUTC()
                val wasActiveAndThenCanceled = !expired && medicationDispenses.isEmpty() &&
                    status == TaskStatusEnum.Canceled
                val allowedStatus = status in setOf(TaskStatusEnum.Ready, TaskStatusEnum.InProgress)
                return (!expired && allowedStatus) || wasActiveAndThenCanceled
            }

            override fun isReady(): Boolean {
                val expired = expiresOn != null && expiresOn <= currentTime.toStartOfDayInUTC()
                val wasReadyAndThenCanceled = !expired && medicationDispenses.isEmpty() &&
                    status == TaskStatusEnum.Canceled
                return (!expired && status == TaskStatusEnum.Ready) || wasReadyAndThenCanceled
            }
        }

        /**
         * A synchronized [Diga] (Digitale Gesundheitsanwendung) task.
         *
         * Represents a prescription for a digital health application.
         *
         * @property medicationDispenses List of dispense records (codes) for the digital application.
         * @property deviceRequest The underlying FHIR DeviceRequest, if available.
         */
        @Serializable
        data class Diga(
            override val profileId: ProfileIdentifier,
            override val name: String?,
            override val taskId: String,
            override val accessCode: String,
            override val isEuRedeemable: Boolean,
            override val communications: List<CommunicationErpModel> = emptyList(),
            override val lastModified: Instant,
            override val isEuRedeemableByPatientAuthorization: Boolean,
            override val organization: OrganizationErpModel?,
            override val practitioner: PractitionerErpModel?,
            override val patient: PatientErpModel?,
            override val insuranceInformation: InsuranceErpModel?,
            override val expiresOn: Instant?,
            override val acceptUntil: Instant?,
            override val authoredOn: Instant,
            override val status: TaskStatusEnum,
            override var isIncomplete: Boolean,
            override var pvsIdentifier: String,
            override var failureToReport: String,
            override val currentTime: Instant = Clock.System.now(),
            val medicationDispenses: List<MedicationDispenseErpModel> = emptyList(),
            val deviceRequest: FhirTaskKbvDeviceRequestErpModel? = null
        ) : Synced {
            override val redeemedOn: Instant? get() = redeemedOn()
            override val startedOn: Instant get() = authoredOn
            override val endedOn: Instant? get() = redeemedOn() ?: expiresOn
            val deviceRequestState: DigaStatus
                get() {
                    val dispenseDeviceRequest = medicationDispenses.firstOrNull()?.deviceRequest
                    val sentOn = deviceRequest?.sentOn?.toInstant()
                    return status.mapToDigaStatus(
                        userActionState = deviceRequest?.userActionState,
                        sentOn = sentOn,
                        isDeclined = dispenseDeviceRequest?.isDeclined ?: false,
                        isRedeemed = dispenseDeviceRequest?.isRedeemed ?: false
                    )
                }

            override fun state(now: Instant, delta: Duration): TaskStateErpModel {
                return when {
                    expiresOn != null && expiresOn <= now.toStartOfDayInUTC() && status != TaskStatusEnum.Completed ->
                        TaskStateErpModel.Expired(expiredOn = expiresOn)

                    status == TaskStatusEnum.Ready -> TaskStateErpModel.Ready(
                        expiresOn = requireNotNull(expiresOn) { "expiresOn is null for Ready diga $taskId" },
                        acceptUntil = requireNotNull(acceptUntil) { "acceptUntil is null for Ready diga $taskId" }
                    )

                    status == TaskStatusEnum.Canceled ->
                        TaskStateErpModel.Deleted(lastModified = lastModified)

                    status != TaskStatusEnum.Completed && medicationDispenses.isNotEmpty() ->
                        TaskStateErpModel.Provided(
                            lastMedicationDispense = medicationDispenses.last().whenHandedOver?.toInstant()
                                ?: lastModified
                        )

                    status == TaskStatusEnum.InProgress ->
                        TaskStateErpModel.InProgress(lastModified = lastModified)

                    else -> TaskStateErpModel.Other(
                        state = status.toFhirTaskStatusErpModel(),
                        lastModified = lastModified
                    )
                }
            }

            override fun redeemedOn(): Instant? =
                if (status == TaskStatusEnum.Completed) lastModified else null

            override fun redeemState(now: Instant, delta: Duration): RedeemStateErpModel {
                val expired = expiresOn != null && expiresOn <= now.toStartOfDayInUTC()
                val ready = status == TaskStatusEnum.Ready
                val inProgress = status == TaskStatusEnum.InProgress
                val valid = accessCode.isNotEmpty()
                return when {
                    expired || inProgress -> RedeemStateErpModel.NotRedeemable
                    ready && valid -> RedeemStateErpModel.RedeemableAndValid
                    else -> RedeemStateErpModel.NotRedeemable
                }
            }

            override fun isDirectAssignment(): Boolean =
                taskId.startsWith(DIRECT_ASSIGNMENT_INDICATOR) ||
                    taskId.startsWith(DIRECT_ASSIGNMENT_INDICATOR_PKV)

            override fun isDeletable(): Boolean =
                if (isDirectAssignment()) status == TaskStatusEnum.Completed else true

            override fun organizationName(): String =
                organization?.name ?: practitioner?.name ?: ""

            override fun medicationName(): String? =
                deviceRequest?.appName

            override fun isActive(): Boolean {
                val expired = expiresOn != null && expiresOn <= currentTime.toStartOfDayInUTC()
                val wasActiveAndThenCanceled = !expired && medicationDispenses.isEmpty() &&
                    status == TaskStatusEnum.Canceled
                val allowedStatus = status in setOf(TaskStatusEnum.Ready, TaskStatusEnum.InProgress)
                return (!expired && allowedStatus) || wasActiveAndThenCanceled
            }

            override fun isReady(): Boolean {
                val expired = expiresOn != null && expiresOn <= currentTime.toStartOfDayInUTC()
                val wasReadyAndThenCanceled = !expired && medicationDispenses.isEmpty() &&
                    status == TaskStatusEnum.Canceled
                return (!expired && status == TaskStatusEnum.Ready) || wasReadyAndThenCanceled
            }
        }
    }
}
