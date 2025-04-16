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

object PrescriptionUseCaseData {
    /**
     * Individual prescription backed by its original task id.
     */
    @Immutable
    sealed class Prescription {
        abstract val taskId: String
        abstract val redeemedOn: Instant?

        /**
         * Represents a single [Task] synchronized with the backend.
         */
        @Immutable
        data class Synced(
            override val taskId: String,
            val state: SyncedTaskData.SyncedTask.TaskState,
            val name: String?,
            val isIncomplete: Boolean,
            val organization: String,
            val authoredOn: Instant,
            override val redeemedOn: Instant?,
            val expiresOn: Instant?,
            val acceptUntil: Instant?,
            val isDirectAssignment: Boolean,
            val multiplePrescriptionState: MultiplePrescriptionState
        ) : Prescription()

        data class MultiplePrescriptionState(
            val isPartOfMultiplePrescription: Boolean = false,
            val numerator: String? = null,
            val denominator: String? = null,
            val start: Instant? = null
        )

        /**
         *  Represents a single [Task] scanned by the user.
         */
        @Immutable
        data class Scanned(
            override val taskId: String,
            val scannedOn: Instant,
            override val redeemedOn: Instant?,
            val communications: List<Communication>
        ) : Prescription()

        fun redeemedOrExpiredOn(): Instant =
            when (this) {
                is Scanned -> requireNotNull(redeemedOn) { "Scanned prescriptions require a redeemed timestamp" }
                is Synced -> redeemedOn ?: expiresOn ?: authoredOn
            }
    }
}
