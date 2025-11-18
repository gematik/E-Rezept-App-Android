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

package de.gematik.ti.erp.app.redeem.model

import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.redeem.model.PrescriptionReadinessState.Companion.readinessState

/**
 * Represents the state of redeemable prescriptions during the redemption process.
 */
sealed class RedeemReadyPrescriptionsState {

    /**
     * Indicates that all prescriptions in the order are ready for redemption.
     */
    data object AllReady : RedeemReadyPrescriptionsState()

    /**
     * Indicates that none of the prescriptions in the order are ready for redemption.
     * @param allPrescriptions List of all prescriptions that are **not redeemable**.
     */
    data class NoneReady(val allPrescriptions: List<RedeemablePrescriptionInfo>) : RedeemReadyPrescriptionsState()

    /**
     * Indicates that some prescriptions in the order are missing or not ready.
     * @param missingPrescriptions List of prescriptions that are **not ready for redemption**.
     */
    data class SomeMissing(val missingPrescriptions: List<RedeemablePrescriptionInfo>) : RedeemReadyPrescriptionsState()
}

/**
 * Represents the readiness state of a each prescription for redemption.
 * This could be considered as a watered down version of [SyncedTaskData.SyncedTask.TaskState]
 */
sealed class PrescriptionReadinessState {
    data object Deleted : PrescriptionReadinessState()
    data object Ready : PrescriptionReadinessState()

    /**
     * The prescription exists but is **not redeemable** at this time.
     * This includes prescriptions that are:
     * - Pending approval
     * - In progress
     * - Redeemable later
     */
    data object NotRedeemable : PrescriptionReadinessState()

    /**
     * The prescription is **not ready** for redemption, but it is also **not in an invalid state**.
     */
    data object NotReady : PrescriptionReadinessState()

    companion object {

        /**
         * Converts a `SyncedTaskData.SyncedTask.TaskState` into a `PrescriptionReadinessState`.
         * @return A `PrescriptionReadinessState` representing whether the prescription is ready, deleted, or not redeemable.
         */
        fun SyncedTaskData.SyncedTask.TaskState.readinessState(): PrescriptionReadinessState {
            return when (this) {
                is SyncedTaskData.SyncedTask.Deleted -> Deleted

                is SyncedTaskData.SyncedTask.Ready -> Ready

                is SyncedTaskData.SyncedTask.LaterRedeemable,
                is SyncedTaskData.SyncedTask.Pending,
                is SyncedTaskData.SyncedTask.InProgress
                -> NotRedeemable

                else -> NotReady
            }
        }
    }
}

/**
 * Represents a **simplified version of a prescription** used in the redemption process.
 * This contains only essential details required for tracking readiness.
 *
 * @param taskId Unique identifier for the prescription task.
 * @param name The name of the prescribed medication (optional).
 * @param state The readiness state of the prescription for redemption.
 */
data class RedeemablePrescriptionInfo(
    val taskId: String,
    val name: String?,
    val state: PrescriptionReadinessState
) {
    companion object {
        /**
         * Converts a `PrescriptionData.Synced` object into a `RedeemablePrescriptionInfo`.
         * This transformation is used to provide a **lightweight summary** of the prescription for redemption processing.
         *
         * @return A `RedeemablePrescriptionInfo` object containing **minimal** required information.
         */
        fun PrescriptionData.Prescription.toPrescriptionInfo(): RedeemablePrescriptionInfo {
            return when (this) {
                is PrescriptionData.Scanned -> RedeemablePrescriptionInfo(
                    taskId = taskId,
                    name = name,
                    state = PrescriptionReadinessState.Ready
                )

                is PrescriptionData.Synced -> RedeemablePrescriptionInfo(
                    taskId = taskId,
                    name = name,
                    state = state.readinessState()
                )
            }
        }

        fun List<RedeemablePrescriptionInfo>.getPrescriptionErrorStateForDialog(): PrescriptionErrorState {
            return when {
                size > 1 -> PrescriptionErrorState.MoreThanOnePrescriptionHasIssues
                size == 1 && first().state == PrescriptionReadinessState.Deleted -> PrescriptionErrorState.Deleted
                size == 1 && first().state == PrescriptionReadinessState.NotRedeemable -> PrescriptionErrorState.NotRedeemable
                else -> PrescriptionErrorState.Generic
            }
        }
    }
}
