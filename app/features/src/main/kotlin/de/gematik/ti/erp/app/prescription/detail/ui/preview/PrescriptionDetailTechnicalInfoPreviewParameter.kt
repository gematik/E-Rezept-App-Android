/*
 * Copyright 2024, gematik GmbH
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

@file:Suppress("UnusedPrivateProperty")

package de.gematik.ti.erp.app.prescription.detail.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.ui.preview.MOCK_MODEL_PROFILE
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

data class PrescriptionDetailTechnicalInfoPreviewData(
    val name: String,
    val state: UiState<Pair<ProfilesUseCaseData.Profile, PrescriptionData.Prescription>>
)

class PrescriptionDetailTechnicalInfoPreviewParameter :
    PreviewParameterProvider<PrescriptionDetailTechnicalInfoPreviewData> {
    val time = Instant.parse("2020-12-02T14:48:36Z")

    override val values = sequenceOf(emptyState, errorState, loadedState)

    companion object {
        val emptyState = PrescriptionDetailTechnicalInfoPreviewData(
            name = "emptyState",
            state = UiState.Empty()
        )

        val errorState = PrescriptionDetailTechnicalInfoPreviewData(
            name = "errorState",
            state = UiState.Error(Throwable("Error loading prescription Technical details"))
        )

        val loadedState = PrescriptionDetailTechnicalInfoPreviewData(
            name = "loadedState",
            state = UiState.Data(
                data = Pair(MOCK_MODEL_PROFILE, MOCK_SCANNED_PRESCRIPTION)
            )
        )
    }
}

private val MOCK_SCANNED_PRESCRIPTION = PrescriptionData.Scanned(
    task = ScannedTaskData.ScannedTask(
        profileId = "mockProfileId",
        taskId = "160.000.006.727.215.38",
        redeemedOn = time,
        accessCode = "4e72654d6105f73fb3346df5728d5460a610bac60649cc8ebef28224a2eccbc6",
        scannedOn = time,
        index = 1,
        name = "Mock Medication"
    )
)
