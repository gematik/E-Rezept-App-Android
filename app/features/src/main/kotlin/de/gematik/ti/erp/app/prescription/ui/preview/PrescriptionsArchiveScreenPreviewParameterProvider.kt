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

package de.gematik.ti.erp.app.prescription.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.ui.model.ArchiveSegmentedControllerTap
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

class PrescriptionsArchiveScreenPreviewParameterProvider : PreviewParameterProvider<PrescriptionsDigasArchiveScreenPreviewData> {
    override val values: Sequence<PrescriptionsDigasArchiveScreenPreviewData>
        get() = sequenceOf(
            mockEmptyDigas,
            mockPrescriptionsWithDigas,
            mockDigasWithPrescriptions,
            mockEmptyPrescriptionsAndDigas,
            mockErrorPrescriptions,
            mockError

        )
}

data class PrescriptionsDigasArchiveScreenPreviewData(
    val archivedDigas: UiState<List<Prescription>>,
    val archivedPrescriptions: UiState<List<Prescription>>,
    val selectedTab: ArchiveSegmentedControllerTap = ArchiveSegmentedControllerTap.PRESCRIPTION
)

val mockPrescriptionsWithDigas = PrescriptionsDigasArchiveScreenPreviewData(
    archivedDigas = PrescriptionsArchiveScreenPreviewData.mockDigasUiState,
    archivedPrescriptions = PrescriptionsArchiveScreenPreviewData.mockPrescriptionsUiState
)

val mockEmptyDigas = PrescriptionsDigasArchiveScreenPreviewData(
    archivedDigas = UiState.Empty(),
    archivedPrescriptions = PrescriptionsArchiveScreenPreviewData.mockPrescriptionsUiState
)

val mockDigasWithPrescriptions = PrescriptionsDigasArchiveScreenPreviewData(
    archivedDigas = PrescriptionsArchiveScreenPreviewData.mockDigasUiState,
    archivedPrescriptions = PrescriptionsArchiveScreenPreviewData.mockPrescriptionsUiState,
    selectedTab = ArchiveSegmentedControllerTap.DIGAS
)

val mockEmptyPrescriptionsAndDigas = PrescriptionsDigasArchiveScreenPreviewData(
    archivedDigas = UiState.Empty(),
    archivedPrescriptions = UiState.Empty()
)

val mockErrorPrescriptions = PrescriptionsDigasArchiveScreenPreviewData(
    archivedDigas = PrescriptionsArchiveScreenPreviewData.mockDigasUiState,
    archivedPrescriptions = UiState.Error(Throwable("Error")),
    selectedTab = ArchiveSegmentedControllerTap.PRESCRIPTION
)

val mockError = PrescriptionsDigasArchiveScreenPreviewData(
    archivedDigas = UiState.Error(Throwable("Error")),
    archivedPrescriptions = UiState.Error(Throwable("Error")),
    selectedTab = ArchiveSegmentedControllerTap.PRESCRIPTION
)

object PrescriptionsArchiveScreenPreviewData {
    val now: Instant = Instant.parse("2023-11-20T15:20:00Z")

    private val mockPrescriptions = listOf(
        Prescription.SyncedPrescription(
            taskId = "1",
            name = "Painkillers Medication 1",
            redeemedOn = now - 2.days,
            expiresOn = now - 3.days,
            state = SyncedTaskData.SyncedTask.Expired(
                expiredOn = now - 1.days
            ),
            isIncomplete = true,
            organization = "Health Organization A",
            authoredOn = now - 10.days,
            acceptUntil = now + 20.days,
            isDirectAssignment = false,
            deviceRequestState = DigaStatus.Ready,
            lastModified = Instant.fromEpochSeconds(123456),
            prescriptionChipInformation = Prescription.PrescriptionChipInformation(
                isSelfPayPrescription = true,
                isPartOfMultiplePrescription = false,
                numerator = "1",
                denominator = "5",
                start = now - 5.days
            )
        ),
        Prescription.SyncedPrescription(
            taskId = "2",
            name = "Painkillers Medication 2",
            redeemedOn = now - 10.days,
            expiresOn = null,
            state = SyncedTaskData.SyncedTask.Deleted(
                lastModified = now - 5.days
            ),
            isIncomplete = true,
            organization = "Health Organization B",
            authoredOn = now - 15.days,
            acceptUntil = null,
            isDirectAssignment = false,
            deviceRequestState = DigaStatus.Ready,
            lastModified = Instant.fromEpochSeconds(123456),
            prescriptionChipInformation = Prescription.PrescriptionChipInformation(
                isSelfPayPrescription = true,
                isPartOfMultiplePrescription = false,
                numerator = null,
                denominator = null,
                start = null
            )
        ),
        Prescription.SyncedPrescription(
            taskId = "3",
            name = "Painkillers Medication 3",
            redeemedOn = null,
            expiresOn = now - 1.days,
            state = SyncedTaskData.SyncedTask.Expired(
                expiredOn = now - 1.days
            ),
            isIncomplete = false,
            organization = "Health Organization C",
            authoredOn = now - 40.days,
            acceptUntil = now - 40.days,
            isDirectAssignment = false,
            deviceRequestState = DigaStatus.Ready,
            lastModified = Instant.fromEpochSeconds(123456),
            prescriptionChipInformation = Prescription.PrescriptionChipInformation(
                isSelfPayPrescription = false,
                isPartOfMultiplePrescription = true,
                numerator = "2",
                denominator = "3",
                start = now - 35.days
            )
        ),
        Prescription.ScannedPrescription(
            taskId = "4",
            name = "Painkillers Medication 4",
            redeemedOn = now - 5.days,
            scannedOn = now - 10.days,
            index = 1,
            communications = emptyList()
        )
    )

    private val mockDiga = listOf(
        Prescription.SyncedPrescription(
            taskId = "3",
            name = "Painkillers Medication 3",
            redeemedOn = null,
            expiresOn = now - 1.days,
            state = SyncedTaskData.SyncedTask.Expired(
                expiredOn = now - 1.days
            ),
            isIncomplete = false,
            organization = "Health Organization C",
            authoredOn = now - 40.days,
            acceptUntil = now - 40.days,
            isDirectAssignment = false,
            deviceRequestState = DigaStatus.SelfArchiveDiga,
            isDiga = true,
            isNew = false,
            lastModified = Instant.fromEpochSeconds(123456),
            prescriptionChipInformation = Prescription.PrescriptionChipInformation(
                isSelfPayPrescription = false,
                isPartOfMultiplePrescription = true,
                numerator = "2",
                denominator = "3",
                start = now - 35.days
            )
        )
    )

    val mockPrescriptionsUiState: UiState<List<Prescription>> = UiState.Data(mockPrescriptions)
    val mockDigasUiState: UiState<List<Prescription>> = UiState.Data(mockDiga)
}
