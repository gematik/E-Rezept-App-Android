/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.detail.ui.model

import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier

import kotlinx.datetime.Instant

object PrescriptionData {
    sealed interface Prescription {
        val profileId: ProfileIdentifier
        val taskId: String
        val redeemedOn: Instant?
        val accessCode: String?
    }

    @Stable
    class Scanned(
        val task: ScannedTaskData.ScannedTask
    ) : Prescription {
        override val profileId: ProfileIdentifier = task.profileId
        override val taskId: String = task.taskId
        override val redeemedOn: Instant? = task.redeemedOn
        override val accessCode: String = task.accessCode
        val scannedOn: Instant = task.scannedOn
        val isRedeemed = redeemedOn != null
    }

    @Stable
    class Synced(
        val task: SyncedTaskData.SyncedTask
    ) : Prescription {
        override val profileId: ProfileIdentifier = task.profileId
        override val taskId: String = task.taskId
        override val redeemedOn: Instant? = task.redeemedOn()
        override val accessCode: String? = task.accessCode

        val name = task.medicationName()
        val state: SyncedTaskData.SyncedTask.TaskState = task.state()
        val authoredOn: Instant = task.authoredOn
        val expiresOn: Instant? = task.expiresOn
        val acceptUntil: Instant? = task.acceptUntil
        val patient: SyncedTaskData.Patient = task.patient
        val practitioner: SyncedTaskData.Practitioner = task.practitioner
        val insurance: SyncedTaskData.InsuranceInformation = task.insuranceInformation
        val organization: SyncedTaskData.Organization = task.organization
        val medicationRequest: SyncedTaskData.MedicationRequest = task.medicationRequest
        val medicationDispenses: List<SyncedTaskData.MedicationDispense> = task.medicationDispenses

        val isDirectAssignment = task.isDirectAssignment()
        val isSubstitutionAllowed = task.medicationRequest.substitutionAllowed
        val isDeletable = task.isDeletable()
        val isDispensed = task.medicationDispenses.isNotEmpty()
        val isIncomplete = task.isIncomplete

        val failureToReport = task.failureToReport
    }

    sealed interface Medication {
        class Request(val medicationRequest: SyncedTaskData.MedicationRequest) : Medication
        class Dispense(val medicationDispense: SyncedTaskData.MedicationDispense) : Medication
    }
}
