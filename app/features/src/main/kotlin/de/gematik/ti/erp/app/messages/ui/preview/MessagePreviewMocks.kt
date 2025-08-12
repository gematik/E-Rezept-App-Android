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

package de.gematik.ti.erp.app.messages.ui.preview

import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import kotlinx.datetime.Instant

object MessagePreviewMocks {

    internal const val MOCK_TASK_ID_01 = "123-001"
    internal const val MOCK_TASK_ID_02 = "456-002"
    private const val MOCK_PRACTITIONER_NAME = "Dr. John Doe"

    private val MOCK_CHIP_INFO = Prescription.PrescriptionChipInformation(
        isPartOfMultiplePrescription = false,
        numerator = null,
        denominator = null,
        start = null
    )

    val MOCK_PRESCRIPTION_01 = Prescription.SyncedPrescription(
        taskId = MOCK_TASK_ID_01,
        name = "Rezept_01",
        redeemedOn = null,
        expiresOn = Instant.fromEpochSeconds(123456),
        state = SyncedTaskData.SyncedTask.Expired(
            expiredOn = Instant.fromEpochSeconds(123456)
        ),
        isIncomplete = false,
        organization = MOCK_PRACTITIONER_NAME,
        authoredOn = Instant.fromEpochSeconds(123456),
        acceptUntil = Instant.fromEpochSeconds(123456),
        isDirectAssignment = false,
        prescriptionChipInformation = MOCK_CHIP_INFO,
        deviceRequestState = DigaStatus.Ready,
        lastModified = Instant.fromEpochSeconds(123456)
    )

    val MOCK_PRESCRIPTION_02 = Prescription.SyncedPrescription(
        taskId = MOCK_TASK_ID_02,
        name = "Rezept_02",
        redeemedOn = null,
        expiresOn = Instant.fromEpochSeconds(123456),
        state = SyncedTaskData.SyncedTask.Expired(
            expiredOn = Instant.fromEpochSeconds(123456)
        ),
        isIncomplete = false,
        organization = MOCK_PRACTITIONER_NAME,
        authoredOn = Instant.fromEpochSeconds(123456),
        acceptUntil = Instant.fromEpochSeconds(123456),
        isDirectAssignment = false,
        prescriptionChipInformation = MOCK_CHIP_INFO,
        deviceRequestState = DigaStatus.Ready,
        lastModified = Instant.fromEpochSeconds(123456)
    )

    val MOCK_PRESCRIPTION_03 = Prescription.SyncedPrescription(
        taskId = MOCK_TASK_ID_02,
        name = "Rezept_03",
        redeemedOn = null,
        expiresOn = Instant.fromEpochSeconds(123456),
        state = SyncedTaskData.SyncedTask.Expired(
            expiredOn = Instant.fromEpochSeconds(123456)
        ),
        isIncomplete = false,
        organization = MOCK_PRACTITIONER_NAME,
        authoredOn = Instant.fromEpochSeconds(123456),
        acceptUntil = Instant.fromEpochSeconds(123456),
        isDirectAssignment = false,
        prescriptionChipInformation = MOCK_CHIP_INFO,
        deviceRequestState = DigaStatus.Ready,
        lastModified = Instant.fromEpochSeconds(123456)
    )
}
