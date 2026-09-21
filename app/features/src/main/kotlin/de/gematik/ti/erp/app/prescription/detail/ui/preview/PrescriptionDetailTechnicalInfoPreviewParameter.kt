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

@file:Suppress("UnusedPrivateProperty")

package de.gematik.ti.erp.app.prescription.detail.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.prescription.ui.preview.MOCK_MODEL_PROFILE
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

private val mockTime = Instant.parse("2020-12-02T14:48:36Z")

data class PrescriptionDetailTechnicalInfoPreviewData(
    val name: String,
    val state: UiState<Pair<ProfileErpModel, TaskErpModel>>
)

class PrescriptionDetailTechnicalInfoPreviewParameter :
    PreviewParameterProvider<PrescriptionDetailTechnicalInfoPreviewData> {

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

private val MOCK_SCANNED_PRESCRIPTION = TaskErpModel.Scanned(
    profileId = "mockProfileId",
    taskId = "160.000.006.727.215.38",
    redeemedOn = mockTime,
    accessCode = "4e72654d6105f73fb3346df5728d5460a610bac60649cc8ebef28224a2eccbc6",
    scannedOn = mockTime,
    index = 1,
    name = "Mock Medication",
    isEuRedeemable = false
)
