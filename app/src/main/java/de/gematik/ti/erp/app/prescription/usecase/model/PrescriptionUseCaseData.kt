/*
 * Copyright (c) 2021 gematik GmbH
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

import java.time.LocalDate
import java.time.OffsetDateTime

object PrescriptionUseCaseData {
    /**
     * Individual prescription backed by its original task id.
     */
    sealed class Prescription {
        /**
         *  Represents a single [Task] synchronized with the backend.
         */
        data class Synced(
            val taskId: String,
            val name: String,
            val expiresOn: LocalDate?,
        ) : Prescription()

        /**
         *  Represents a single [Task] scanned by the user.
         */
        data class Scanned(
            val taskId: String,
            val nr: Int,
        ) : Prescription()
    }

    /**
     * One recipe contains several prescriptions.
     */
    sealed class Recipe {
        /**
         * Represents a group of [Task]s synchronized with the backend.
         */
        data class Synced(
            val organization: String,
            val authoredOn: OffsetDateTime,
            val prescriptions: List<Prescription.Synced>,
            val redeemedOn: OffsetDateTime?
        ) : Recipe()

        /**
         * Represents a group of [Task]s scanned by the user (e.g. of the paper based prescriptions).
         */
        data class Scanned(
            val title: String?,
            val scanSessionEnd: OffsetDateTime,
            val prescriptions: List<Prescription.Scanned>,
            val redeemedOn: OffsetDateTime?
        ) : Recipe()
    }
}
