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

package de.gematik.ti.erp.app.mocks.prescription.model

import de.gematik.ti.erp.app.mocks.DATE_2024_01_01
import de.gematik.ti.erp.app.mocks.DATE_3024_01_01
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription

val MODEL_SYNCED_PRESCRIPTION_ACTIVE = Prescription.SyncedPrescription(
    taskId = "active-synced-task-id-1",
    name = "Medication",
    redeemedOn = null,
    expiresOn = DATE_3024_01_01,
    acceptUntil = DATE_3024_01_01,
    authoredOn = DATE_2024_01_01,
    state = SyncedTaskData.SyncedTask.Ready(
        expiresOn = DATE_3024_01_01,
        acceptUntil = DATE_3024_01_01
    ),
    isIncomplete = false,
    organization = "Dr. Max Mustermann",
    isDirectAssignment = false,
    prescriptionChipInformation = Prescription.PrescriptionChipInformation()
)

val MODEL_SYNCED_PRESCRIPTION_ARCHIVE = Prescription.SyncedPrescription(
    taskId = "archive-synced-task-id-1",
    name = "Medication",
    redeemedOn = DATE_2024_01_01,
    expiresOn = DATE_3024_01_01,
    acceptUntil = DATE_2024_01_01,
    authoredOn = DATE_2024_01_01,
    state = SyncedTaskData.SyncedTask.Other(
        state = SyncedTaskData.TaskStatus.Completed,
        lastModified = DATE_2024_01_01
    ),
    isIncomplete = false,
    organization = "Dr. Max Mustermann",
    isDirectAssignment = false,
    prescriptionChipInformation = Prescription.PrescriptionChipInformation()
)
