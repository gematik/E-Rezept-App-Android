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
import de.gematik.ti.erp.app.prescription.ui.model.ArchiveSegmentedControllerTap
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
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
    val archivedDigas: UiState<List<TaskErpModel>>,
    val archivedPrescriptions: UiState<List<TaskErpModel>>,
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

    private fun buildPrescription(
        taskId: String,
        name: String,
        status: TaskStatusEnum,
        expiresOn: Instant? = null,
        acceptUntil: Instant? = null,
        authoredOn: Instant = now - 10.days,
        isIncomplete: Boolean = false
    ) = TaskErpModel.Synced.Prescription(
        profileId = "preview",
        taskId = taskId,
        name = name,
        accessCode = "",
        isEuRedeemable = false,
        isEuRedeemableByPatientAuthorization = false,
        lastModified = Instant.fromEpochSeconds(123456),
        organization = null,
        practitioner = null,
        patient = null,
        insuranceInformation = null,
        expiresOn = expiresOn,
        acceptUntil = acceptUntil,
        authoredOn = authoredOn,
        status = status,
        isIncomplete = isIncomplete,
        pvsIdentifier = "",
        failureToReport = "",
        medicationRequest = null,
        medicationDispenses = emptyList()
    )

    private val mockPrescriptions: List<TaskErpModel> = listOf(
        buildPrescription("1", "Painkillers Medication 1", TaskStatusEnum.Canceled, expiresOn = now - 3.days, authoredOn = now - 10.days),
        buildPrescription("2", "Painkillers Medication 2", TaskStatusEnum.Canceled, authoredOn = now - 15.days),
        buildPrescription(
            "3",
            "Painkillers Medication 3",
            TaskStatusEnum.Canceled,
            expiresOn = now - 1.days,
            acceptUntil = now - 40.days,
            authoredOn = now - 40.days
        ),
        TaskErpModel.Scanned(
            profileId = "preview",
            taskId = "4",
            index = 1,
            name = "Painkillers Medication 4",
            accessCode = "",
            scannedOn = now - 10.days,
            redeemedOn = now - 5.days,
            isEuRedeemable = false
        )
    )

    private val mockDiga: List<TaskErpModel> = listOf(
        buildPrescription("5", "Diga App 1", TaskStatusEnum.Canceled, expiresOn = now - 1.days, acceptUntil = now - 40.days, authoredOn = now - 40.days)
    )

    val mockPrescriptionsUiState: UiState<List<TaskErpModel>> = UiState.Data(mockPrescriptions)
    val mockDigasUiState: UiState<List<TaskErpModel>> = UiState.Data(mockDiga)
}
