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

package de.gematik.ti.erp.app.database.room.v2.task.prescription

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import de.gematik.ti.erp.app.database.room.v2.task.util.TaskStatusConverter
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.datetime.Instant

/** Discriminator values stored in [ErpTaskEntity.taskType]. */
object TaskTypeValues {
    const val SCANNED = "Scanned"
    const val PRESCRIPTION = "Prescription"
    const val DIGA = "Diga"
}

@Entity(
    tableName = "tasks",
    indices = [
        Index("organizationId"),
        Index("practitionerId"),
        Index("patientId"),
        Index("insuranceInformationId"),
        Index("deviceRequestId"),
        Index("medicationRequestId"),
        Index("parentProfileId"),
        Index(value = ["accessCode"]),
        Index(value = ["status"]),
        Index(value = ["lastModified"])
    ]
)
@TypeConverters(InstantConverter::class, TaskStatusConverter::class)
data class ErpTaskEntity(
    @PrimaryKey
    val taskId: String,

    /** Discriminates between "Scanned", "Prescription", or "Diga". See [TaskTypeValues]. */
    val taskType: String,

    /** Back-reference to the owning profile. */
    val parentProfileId: String?,

    val accessCode: String,

    /** Display name – stored directly for Scanned; derived from ingredients for Synced. */
    val name: String?,

    val redeemedOn: Instant?,

    val isEuRedeemable: Boolean = false,

    // ── Synced-only fields ──────────────────────────────────────────────────────

    val isEuRedeemableByPatientAuthorization: Boolean = false,

    val lastModified: Instant? = null,
    val lastMedicationDispense: Instant? = null,

    val expiresOn: Instant? = null,
    val acceptUntil: Instant? = null,
    val authoredOn: Instant? = null,

    val status: TaskStatusEnum = TaskStatusEnum.Other,

    val isIncomplete: Boolean = false,
    val pvsIdentifier: String = "",
    val failureToReport: String = "",

    // ── Legacy FK id columns kept during migration (child tables now also store taskId) ──
    val organizationId: String? = null,
    val practitionerId: String? = null,
    val patientId: String? = null,
    val insuranceInformationId: String? = null,
    val medicationId: String? = null,
    val medicationRequestId: String? = null,
    val accidentInfoId: String? = null,
    val deviceRequestId: String? = null,
    val multiplePrescriptionId: String? = null,

    // ── Scanned-only fields ─────────────────────────────────────────────────────

    val scannedOn: Instant? = null,
    val index: Int = 0
)
