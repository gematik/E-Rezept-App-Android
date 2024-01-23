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

package de.gematik.ti.erp.app.prescription.usecase.model

import androidx.compose.runtime.Immutable
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import kotlinx.datetime.Instant

@Immutable
sealed interface Prescription {
    val taskId: String
    val redeemedOn: Instant?
    val startedOn: Instant?

    /**
     * Represents a single [Task] synchronized with the backend.
     */
    @Immutable
    data class SyncedPrescription(
        override val taskId: String,
        override val redeemedOn: Instant?,
        val state: SyncedTaskData.SyncedTask.TaskState,
        val name: String?,
        val isIncomplete: Boolean,
        val organization: String,
        val authoredOn: Instant,
        val expiresOn: Instant?,
        val acceptUntil: Instant?,
        val isDirectAssignment: Boolean,
        val prescriptionChipInformation: PrescriptionChipInformation
    ) : Prescription {
        override val startedOn = authoredOn
    }

    data class PrescriptionChipInformation(
        val isPartOfMultiplePrescription: Boolean = false,
        val numerator: String? = null,
        val denominator: String? = null,
        val start: Instant? = null
    )

    /**
     *  Represents a single [Task] scanned by the user.
     */
    @Immutable
    data class ScannedPrescription(
        override val taskId: String,
        override val redeemedOn: Instant?,
        val scannedOn: Instant,
        val communications: List<CommunicationEntityV1>
    ) : Prescription {
        override val startedOn = scannedOn
    }

    fun redeemedOrExpiredOn(): Instant =
        when (this) {
            is ScannedPrescription -> requireNotNull(redeemedOn) {
                "Scanned prescriptions require " +
                    "a redeemed timestamp"
            }

            is SyncedPrescription -> redeemedOn ?: expiresOn ?: authoredOn
        }
}
