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
import de.gematik.ti.erp.app.task.model.TeratogenicPrescriptionErpModel
import de.gematik.ti.erp.app.utils.uistate.UiState

data class PrescriptionDetailTeratogenicPrescriptionPreviewData(
    val name: String,
    val state: UiState<TeratogenicPrescriptionErpModel>
)

class PrescriptionDetailTeratogenicPrescriptionPreviewParameter :
    PreviewParameterProvider<PrescriptionDetailTeratogenicPrescriptionPreviewData> {

    override val values = sequenceOf(emptyState, errorState, loadedState, loadedStateAllFalse)

    companion object {
        val emptyState = PrescriptionDetailTeratogenicPrescriptionPreviewData(
            name = "emptyState",
            state = UiState.Empty()
        )

        val errorState = PrescriptionDetailTeratogenicPrescriptionPreviewData(
            name = "errorState",
            state = UiState.Error(Throwable("Error loading teratogenic prescription details"))
        )

        val loadedState = PrescriptionDetailTeratogenicPrescriptionPreviewData(
            name = "loadedState",
            state = UiState.Data(MOCK_TERATOGENIC_PRESCRIPTION_ALL_TRUE)
        )

        val loadedStateAllFalse = PrescriptionDetailTeratogenicPrescriptionPreviewData(
            name = "loadedStateAllFalse",
            state = UiState.Data(MOCK_TERATOGENIC_PRESCRIPTION_ALL_FALSE)
        )
    }
}

internal val MOCK_TERATOGENIC_PRESCRIPTION_ALL_TRUE = TeratogenicPrescriptionErpModel(
    offLabel = true,
    gebaerfaehigeFrau = true,
    einhaltungSicherheitsmassnahmen = true,
    aushaendigungInformationsmaterialien = true,
    erklaerungSachkenntnis = true
)

internal val MOCK_TERATOGENIC_PRESCRIPTION_ALL_FALSE = TeratogenicPrescriptionErpModel(
    offLabel = false,
    gebaerfaehigeFrau = false,
    einhaltungSicherheitsmassnahmen = false,
    aushaendigungInformationsmaterialien = false,
    erklaerungSachkenntnis = false
)
