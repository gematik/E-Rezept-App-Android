/*
 * Copyright (c) 2022 gematik GmbH
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
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import java.time.Instant

object PrescriptionUseCaseData {
    /**
     * Individual prescription backed by its original task id.
     */
    @Immutable
    sealed class Prescription {
        abstract val taskId: String
        abstract val redeemedOn: Instant?

        /**
         *  Represents a single [Task] synchronized with the backend.
         */
        @Immutable
        data class Synced(
            override val taskId: String,
            val state: SyncedTaskData.SyncedTask.TaskState,
            val name: String,
            val organization: String,
            val authoredOn: Instant,
            override val redeemedOn: Instant?,
            val expiresOn: Instant?,
            val acceptUntil: Instant?,
            val isDirectAssignment: Boolean
        ) : Prescription()

        /**
         *  Represents a single [Task] scanned by the user.
         */
        @Immutable
        data class Scanned(
            override val taskId: String,
            val scannedOn: Instant,
            override val redeemedOn: Instant?
        ) : Prescription()
    }
}
