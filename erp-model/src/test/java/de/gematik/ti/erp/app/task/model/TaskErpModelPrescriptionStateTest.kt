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

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Unit tests for [TaskErpModel.Synced.Prescription] state methods.
 * Logic ported from [SyncedTaskData.SyncedTask] which has no dedicated unit tests.
 *
 * Covers: state(), redeemState(), redeemedOn(), isActive(), isReady(),
 *         isDeletable(), isDirectAssignment()
 */
class TaskErpModelPrescriptionStateTest {

    private val now = Instant.parse("2024-06-01T12:00:00Z")

    // A base prescription in Ready state, not expired
    private fun basePrescription(
        taskId: String = "task-1",
        status: TaskStatusEnum = TaskStatusEnum.Ready,
        expiresOn: Instant? = now.plus(90.days),
        acceptUntil: Instant? = now.plus(28.days),
        lastModified: Instant = now.minus(1.days),
        lastMedicationDispense: Instant? = null,
        medicationDispenses: List<MedicationDispenseErpModel> = emptyList(),
        medicationRequest: MedicationRequestErpModel? = MedicationRequestErpModel(
            substitutionAllowed = true,
            multiplePrescriptionInfo = MultiplePrescriptionInfo(),
            note = null
        ),
        accessCode: String = "abc123"
    ) = TaskErpModel.Synced.Prescription(
        profileId = "profile-1",
        taskId = taskId,
        name = "TestMedication",
        accessCode = accessCode,
        isEuRedeemable = false,
        lastModified = lastModified,
        isEuRedeemableByPatientAuthorization = false,
        organization = null,
        practitioner = null,
        patient = null,
        insuranceInformation = null,
        expiresOn = expiresOn,
        acceptUntil = acceptUntil,
        authoredOn = now.minus(7.days),
        status = status,
        isIncomplete = false,
        pvsIdentifier = "pvs",
        failureToReport = "",
        currentTime = now,
        medicationRequest = medicationRequest,
        medicationDispenses = medicationDispenses,
        lastMedicationDispense = lastMedicationDispense
    )

    // ─── state() ────────────────────────────────────────────────────────────

    @Test
    fun `state - Ready when status is Ready and not expired`() {
        val task = basePrescription(status = TaskStatusEnum.Ready)
        val state = task.state(now)
        assertTrue(state is TaskStateErpModel.Ready)
        assertEquals(task.expiresOn, (state as TaskStateErpModel.Ready).expiresOn)
        assertEquals(task.acceptUntil, state.acceptUntil)
    }

    @Test
    fun `state - Expired when expiresOn is in the past and not Completed`() {
        val expired = now.minus(1.days)
        val task = basePrescription(
            status = TaskStatusEnum.Ready,
            expiresOn = expired,
            acceptUntil = expired
        )
        val state = task.state(now)
        assertTrue(state is TaskStateErpModel.Expired)
        assertEquals(expired, (state as TaskStateErpModel.Expired).expiredOn)
    }

    @Test
    fun `state - not Expired when status is Completed even if expiresOn is in the past`() {
        val task = basePrescription(
            status = TaskStatusEnum.Completed,
            expiresOn = now.minus(1.days)
        )
        // Completed tasks skip the expiry check → falls through to Other
        val state = task.state(now)
        assertFalse(state is TaskStateErpModel.Expired)
    }

    @Test
    fun `state - LaterRedeemable when multiplePrescriptionInfo start is in the future`() {
        val futureStart = now.plus(5.days)
        val request = MedicationRequestErpModel(
            substitutionAllowed = true,
            multiplePrescriptionInfo = MultiplePrescriptionInfo(
                indicator = true,
                start = futureStart
            ),
            note = null
        )
        val task = basePrescription(medicationRequest = request)
        val state = task.state(now)
        assertTrue(state is TaskStateErpModel.LaterRedeemable)
        assertEquals(futureStart, (state as TaskStateErpModel.LaterRedeemable).redeemableOn)
    }

    @Test
    fun `state - Ready (not LaterRedeemable) when multiplePrescriptionInfo start is in the past`() {
        val pastStart = now.minus(5.days)
        val request = MedicationRequestErpModel(
            substitutionAllowed = true,
            multiplePrescriptionInfo = MultiplePrescriptionInfo(
                indicator = true,
                start = pastStart
            ),
            note = null
        )
        val task = basePrescription(medicationRequest = request)
        val state = task.state(now)
        assertTrue(state is TaskStateErpModel.Ready)
    }

    @Test
    fun `state - Deleted when status is Canceled`() {
        val task = basePrescription(status = TaskStatusEnum.Canceled)
        val state = task.state(now)
        assertTrue(state is TaskStateErpModel.Deleted)
        assertEquals(task.lastModified, (state as TaskStateErpModel.Deleted).lastModified)
    }

    @Test
    fun `state - Provided when not Completed and lastMedicationDispense is set`() {
        val dispenseTime = now.minus(2.hours)
        val task = basePrescription(
            status = TaskStatusEnum.InProgress,
            lastMedicationDispense = dispenseTime
        )
        val state = task.state(now)
        assertTrue(state is TaskStateErpModel.Provided)
        assertEquals(dispenseTime, (state as TaskStateErpModel.Provided).lastMedicationDispense)
    }

    @Test
    fun `state - InProgress when status is InProgress and no dispense`() {
        val task = basePrescription(status = TaskStatusEnum.InProgress)
        val state = task.state(now)
        assertTrue(state is TaskStateErpModel.InProgress)
        assertEquals(task.lastModified, (state as TaskStateErpModel.InProgress).lastModified)
    }

    @Test
    fun `state - Other for unrecognised status`() {
        val task = basePrescription(status = TaskStatusEnum.Draft)
        val state = task.state(now)
        assertTrue(state is TaskStateErpModel.Other)
    }

    // ─── redeemState() ──────────────────────────────────────────────────────

    @Test
    fun `redeemState - RedeemableAndValid when Ready with valid accessCode`() {
        val task = basePrescription(status = TaskStatusEnum.Ready, accessCode = "valid")
        assertEquals(RedeemStateErpModel.RedeemableAndValid, task.redeemState(now))
    }

    @Test
    fun `redeemState - NotRedeemable when expired`() {
        val task = basePrescription(
            status = TaskStatusEnum.Ready,
            expiresOn = now.minus(1.days),
            acceptUntil = now.minus(1.days)
        )
        assertEquals(RedeemStateErpModel.NotRedeemable, task.redeemState(now))
    }

    @Test
    fun `redeemState - NotRedeemable when InProgress`() {
        val task = basePrescription(status = TaskStatusEnum.InProgress)
        assertEquals(RedeemStateErpModel.NotRedeemable, task.redeemState(now))
    }

    @Test
    fun `redeemState - NotRedeemable when redeemableLater (multiplePrescriptionInfo future start)`() {
        val request = MedicationRequestErpModel(
            substitutionAllowed = true,
            multiplePrescriptionInfo = MultiplePrescriptionInfo(
                indicator = true,
                start = now.plus(3.days)
            ),
            note = null
        )
        val task = basePrescription(medicationRequest = request)
        assertEquals(RedeemStateErpModel.NotRedeemable, task.redeemState(now))
    }

    @Test
    fun `redeemState - NotRedeemable when accessCode is empty`() {
        val task = basePrescription(status = TaskStatusEnum.Ready, accessCode = "")
        assertEquals(RedeemStateErpModel.NotRedeemable, task.redeemState(now))
    }

    // ─── redeemedOn() / property ────────────────────────────────────────────

    @Test
    fun `redeemedOn - returns lastModified when Completed`() {
        val task = basePrescription(status = TaskStatusEnum.Completed, lastModified = now)
        assertEquals(now, task.redeemedOn())
        assertEquals(now, task.redeemedOn) // property delegates to method
    }

    @Test
    fun `redeemedOn - returns null when not Completed`() {
        val task = basePrescription(status = TaskStatusEnum.Ready)
        assertNull(task.redeemedOn())
        assertNull(task.redeemedOn)
    }

    // ─── isActive() ─────────────────────────────────────────────────────────

    @Test
    fun `isActive - true when Ready and not expired`() {
        val task = basePrescription(status = TaskStatusEnum.Ready)
        assertTrue(task.isActive())
    }

    @Test
    fun `isActive - true when InProgress and not expired`() {
        val task = basePrescription(status = TaskStatusEnum.InProgress)
        assertTrue(task.isActive())
    }

    @Test
    fun `isActive - false when expired`() {
        val task = basePrescription(
            status = TaskStatusEnum.Ready,
            expiresOn = now.minus(1.days),
            acceptUntil = now.minus(1.days)
        )
        assertFalse(task.isActive())
    }

    @Test
    fun `isActive - false when Completed`() {
        val task = basePrescription(status = TaskStatusEnum.Completed)
        assertFalse(task.isActive())
    }

    @Test
    fun `isActive - true when Canceled before expiry with no dispenses (wasActiveAndThenCanceled)`() {
        val task = basePrescription(
            status = TaskStatusEnum.Canceled,
            expiresOn = now.plus(30.days),
            medicationDispenses = emptyList()
        )
        assertTrue(task.isActive())
    }

    @Test
    fun `isActive - false when Canceled after expiry`() {
        val task = basePrescription(
            status = TaskStatusEnum.Canceled,
            expiresOn = now.minus(1.days)
        )
        assertFalse(task.isActive())
    }

    // ─── isReady() ──────────────────────────────────────────────────────────

    @Test
    fun `isReady - true when Ready and not expired`() {
        val task = basePrescription(status = TaskStatusEnum.Ready)
        assertTrue(task.isReady())
    }

    @Test
    fun `isReady - false when InProgress`() {
        val task = basePrescription(status = TaskStatusEnum.InProgress)
        assertFalse(task.isReady())
    }

    @Test
    fun `isReady - false when expired`() {
        val task = basePrescription(
            status = TaskStatusEnum.Ready,
            expiresOn = now.minus(1.days),
            acceptUntil = now.minus(1.days)
        )
        assertFalse(task.isReady())
    }

    @Test
    fun `isReady - true when Canceled before expiry with no dispenses`() {
        val task = basePrescription(
            status = TaskStatusEnum.Canceled,
            expiresOn = now.plus(30.days),
            medicationDispenses = emptyList()
        )
        assertTrue(task.isReady())
    }

    // ─── isDirectAssignment() ───────────────────────────────────────────────

    @Test
    fun `isDirectAssignment - true when taskId starts with 169`() {
        val task = basePrescription(taskId = "169.000.001.234")
        assertTrue(task.isDirectAssignment())
    }

    @Test
    fun `isDirectAssignment - true when taskId starts with 209 (PKV)`() {
        val task = basePrescription(taskId = "209.000.001.234")
        assertTrue(task.isDirectAssignment())
    }

    @Test
    fun `isDirectAssignment - false for regular taskId`() {
        val task = basePrescription(taskId = "160.000.001.234")
        assertFalse(task.isDirectAssignment())
    }

    // ─── isDeletable() ──────────────────────────────────────────────────────

    @Test
    fun `isDeletable - true for non-direct-assignment regardless of status`() {
        assertTrue(basePrescription(taskId = "160.x", status = TaskStatusEnum.Ready).isDeletable())
        assertTrue(basePrescription(taskId = "160.x", status = TaskStatusEnum.InProgress).isDeletable())
        assertTrue(basePrescription(taskId = "160.x", status = TaskStatusEnum.Completed).isDeletable())
    }

    @Test
    fun `isDeletable - false for direct assignment when not Completed`() {
        assertFalse(basePrescription(taskId = "169.x", status = TaskStatusEnum.Ready).isDeletable())
        assertFalse(basePrescription(taskId = "169.x", status = TaskStatusEnum.InProgress).isDeletable())
    }

    @Test
    fun `isDeletable - true for direct assignment when Completed`() {
        assertTrue(basePrescription(taskId = "169.x", status = TaskStatusEnum.Completed).isDeletable())
    }

    // ─── acceptDaysLeft / expiryDaysLeft on Ready state ─────────────────────

    @Test
    fun `Ready state - acceptDaysLeft and expiryDaysLeft computed correctly`() {
        val task = basePrescription(
            status = TaskStatusEnum.Ready,
            acceptUntil = now.plus(28.days),
            expiresOn = now.plus(90.days)
        )
        val state = task.state(now) as TaskStateErpModel.Ready
        // -1 because on the day of acceptUntil the prescription is not paid by insurance
        assertEquals(27, state.acceptDaysLeft(now))
        assertEquals(89, state.expiryDaysLeft(now))
    }
}
