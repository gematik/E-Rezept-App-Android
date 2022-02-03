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
import java.time.LocalDate
import java.time.OffsetDateTime

object PrescriptionUseCaseData {
    /**
     * Individual prescription backed by its original task id.
     */
    sealed class Prescription {
        abstract val taskId: String
        abstract val redeemedOn: OffsetDateTime?
        /**
         *  Represents a single [Task] synchronized with the backend.
         */
        @Immutable
        data class Synced(
            override val taskId: String,
            val name: String,
            val organization: String,
            val authoredOn: OffsetDateTime,
            override val redeemedOn: OffsetDateTime?,
            val expiresOn: LocalDate?,
            val acceptUntil: LocalDate?,
            val status: Status,
            val isDirectAssignment: Boolean
        ) : Prescription() {
            enum class Status {
                Ready, InProgress, Completed, Unknown
            }
        }

        /**
         *  Represents a single [Task] scanned by the user.
         */
        @Immutable
        data class Scanned(
            override val taskId: String,
            val scannedOn: OffsetDateTime,
            override val redeemedOn: OffsetDateTime?
        ) : Prescription()
    }
}
