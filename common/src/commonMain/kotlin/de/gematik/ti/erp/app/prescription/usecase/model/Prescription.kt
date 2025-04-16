/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.prescription.usecase.model

import androidx.compose.runtime.Immutable
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface Prescription {
    val name: String?
    val taskId: String
    val redeemedOn: Instant?
    val startedOn: Instant?
    val expiresOn: Instant?

    /**
     * Represents a single [Task] synchronized with the backend.
     */
    @Immutable
    @Serializable
    data class SyncedPrescription(
        override val taskId: String,
        override val name: String?,
        override val redeemedOn: Instant?,
        override val expiresOn: Instant?,
        val state: SyncedTaskData.SyncedTask.TaskState,
        val isIncomplete: Boolean,
        val organization: String,
        val authoredOn: Instant,
        val acceptUntil: Instant?,
        val isDirectAssignment: Boolean,
        val prescriptionChipInformation: PrescriptionChipInformation
    ) : Prescription {
        override val startedOn = authoredOn
    }

    @Serializable
    data class PrescriptionChipInformation(
        val isSelfPayPrescription: Boolean = false,
        val isPartOfMultiplePrescription: Boolean = false,
        val numerator: String? = null,
        val denominator: String? = null,
        val start: Instant? = null
    )

    /**
     *  Represents a single [Task] scanned by the user.
     */
    @Immutable
    @Serializable
    data class ScannedPrescription(
        override val taskId: String,
        override val name: String,
        override val redeemedOn: Instant?,
        val scannedOn: Instant,
        val index: Int,
        val communications: List<Communication>
    ) : Prescription {
        override val startedOn = scannedOn
        override val expiresOn = null
    }

    fun redeemedOrExpiredOn(): Instant =
        when (this) {
            is ScannedPrescription -> requireNotNull(redeemedOn) { "Scanned prescription needs a redeemed timestamp" }

            is SyncedPrescription -> redeemedOn ?: expiresOn ?: authoredOn
        }
}
