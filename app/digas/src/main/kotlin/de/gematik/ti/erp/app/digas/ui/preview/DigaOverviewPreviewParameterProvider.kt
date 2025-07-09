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

package de.gematik.ti.erp.app.digas.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.digas.ui.model.DigaMainScreenUiModel
import de.gematik.ti.erp.app.digas.ui.model.DigaSegmentedControllerTap
import de.gematik.ti.erp.app.digas.ui.model.DigaTimestamps
import de.gematik.ti.erp.app.digas.ui.preview.DigaPreviewStates.DIGAS_ACTIVATE_PREVIEW
import de.gematik.ti.erp.app.digas.ui.preview.DigaPreviewStates.DIGAS_DOWNLOAD_PREVIEW
import de.gematik.ti.erp.app.digas.ui.preview.DigaPreviewStates.DIGAS_INSURANT_REJECTED_PREVIEW
import de.gematik.ti.erp.app.digas.ui.preview.DigaPreviewStates.DIGAS_INSURANT_WAIT_PREVIEW
import de.gematik.ti.erp.app.digas.ui.preview.DigaPreviewStates.DIGAS_READY_ARCHIVE_PREVIEW
import de.gematik.ti.erp.app.digas.ui.preview.DigaPreviewStates.DIGAS_REQUESTS_PREVIEW
import de.gematik.ti.erp.app.digas.ui.preview.DigaPreviewStates.DIGAS_SELF_ARCHIVE_PREVIEW
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

class DigaOverviewPreviewParameterProvider : PreviewParameterProvider<DigaPreviewData> {

    override val values = sequenceOf(
        DigaPreviewData(
            name = "Loading state overview",
            uiData = UiState.Loading(),
            selectedTap = DigaSegmentedControllerTap.OVERVIEW
        ),
        DigaPreviewData(
            name = "Empty state overview",
            uiData = UiState.Empty(),
            selectedTap = DigaSegmentedControllerTap.OVERVIEW
        ),
        DigaPreviewData(
            name = "Error state overview",
            uiData = UiState.Error(Exception("missing")),
            selectedTap = DigaSegmentedControllerTap.OVERVIEW
        ),
        DigaPreviewData(
            name = "Ready state overview",
            uiData = UiState.Data(
                digaMainScreenUiModel.copy(status = DIGAS_REQUESTS_PREVIEW)
            ),
            selectedTap = DigaSegmentedControllerTap.OVERVIEW
        ),
        DigaPreviewData(
            name = "In progress state overview",
            uiData = UiState.Data(
                digaMainScreenUiModel.copy(status = DIGAS_INSURANT_WAIT_PREVIEW)
            ),
            selectedTap = DigaSegmentedControllerTap.OVERVIEW
        ),
        DigaPreviewData(
            name = "Completed successfully state overview",
            uiData = UiState.Data(
                digaMainScreenUiModel.copy(status = DIGAS_DOWNLOAD_PREVIEW)
            ),
            selectedTap = DigaSegmentedControllerTap.OVERVIEW
        ),
        DigaPreviewData(
            name = "Open app with code overview",
            uiData = UiState.Data(
                digaMainScreenUiModel.copy(status = DIGAS_ACTIVATE_PREVIEW)
            ),
            selectedTap = DigaSegmentedControllerTap.OVERVIEW
        ),
        DigaPreviewData(
            name = "Ready for archive overview",
            uiData = UiState.Data(
                digaMainScreenUiModel.copy(status = DIGAS_READY_ARCHIVE_PREVIEW)
            ),
            selectedTap = DigaSegmentedControllerTap.OVERVIEW
        ),
        DigaPreviewData(
            name = "Archived overview",
            uiData = UiState.Data(
                digaMainScreenUiModel.copy(status = DIGAS_SELF_ARCHIVE_PREVIEW)
            ),
            selectedTap = DigaSegmentedControllerTap.OVERVIEW
        ),
        DigaPreviewData(
            name = "Completed with rejection",
            uiData = UiState.Data(
                digaMainScreenUiModel.copy(status = DIGAS_INSURANT_REJECTED_PREVIEW)
            ),
            selectedTap = DigaSegmentedControllerTap.OVERVIEW
        )
    )
}

class DigaDetailPreviewParameterProvider : PreviewParameterProvider<DigaPreviewData> {
    override val values: Sequence<DigaPreviewData>
        get() = sequenceOf(
            DigaPreviewData(
                name = "Loading state detail",
                uiData = UiState.Loading(),
                selectedTap = DigaSegmentedControllerTap.DETAIL
            ),
            DigaPreviewData(
                name = "Empty state detail",
                uiData = UiState.Empty(),
                selectedTap = DigaSegmentedControllerTap.DETAIL
            ),
            DigaPreviewData(
                name = "Error state detail",
                uiData = UiState.Error(Exception("missing")),
                selectedTap = DigaSegmentedControllerTap.DETAIL
            ),
            DigaPreviewData(
                name = "Ready detail",
                uiData = UiState.Data(
                    digaMainScreenUiModel.copy(status = DIGAS_REQUESTS_PREVIEW)
                ),
                selectedTap = DigaSegmentedControllerTap.DETAIL
            )
        )
}

class DigaStatusPreviewParameterProvider : PreviewParameterProvider<DigaStatus> {
    override val values: Sequence<DigaStatus>
        get() = sequenceOf(
            DIGAS_REQUESTS_PREVIEW,
            DIGAS_INSURANT_WAIT_PREVIEW,
            DIGAS_INSURANT_REJECTED_PREVIEW,
            DIGAS_DOWNLOAD_PREVIEW,
            DIGAS_ACTIVATE_PREVIEW,
            DIGAS_READY_ARCHIVE_PREVIEW,
            DIGAS_SELF_ARCHIVE_PREVIEW
        )
}

object DigaPreviewStates {
    val DIGAS_REQUESTS_PREVIEW = DigaStatus.Ready
    val DIGAS_INSURANT_WAIT_PREVIEW = DigaStatus.InProgress(Instant.parse("2024-07-01T10:00:00Z"))
    val DIGAS_INSURANT_REJECTED_PREVIEW = DigaStatus.CompletedWithRejection(Instant.parse("2024-08-01T10:00:00Z"))
    val DIGAS_DOWNLOAD_PREVIEW = DigaStatus.CompletedSuccessfully
    val DIGAS_ACTIVATE_PREVIEW = DigaStatus.OpenAppWithRedeemCode
    val DIGAS_READY_ARCHIVE_PREVIEW = DigaStatus.ReadyForSelfArchiveDiga
    val DIGAS_SELF_ARCHIVE_PREVIEW = DigaStatus.SelfArchiveDiga
}

val digaMainScreenUiModel: DigaMainScreenUiModel = DigaMainScreenUiModel(
    name = "Kaia Rückenschmerzen - Rückentraining für Zuhause",
    canBeRedeemedAgain = true,
    languages = listOf("Deutsch", "Englisch"),
    supportedPlatforms = listOf("iOS", "Android"),
    medicalServicesRequired = false,
    additionalDevices = "keine Zusatzgeräte benötigt",
    fee = "0",
    cost = "500",
    insuredPerson = "Anette Wagner",
    prescribingPerson = "Dr. med. Robin Schneider",
    institution = "Praxis Dr. med. Robin Schneider",
    lifeCycleTimestamps = DigaTimestamps(
        issuedOn = Instant.parse("2025-11-02T14:49:46Z"),
        sentOn = Instant.parse("2025-11-02T14:49:46Z"),
        modifiedOn = Instant.parse("2025-11-02T14:49:46Z"),
        expiresOn = Instant.parse("2025-12-02T14:49:46Z"),
        now = Instant.parse("2026-08-01T10:00:00Z")
    ),
    code = "XX123456789909",
    deepLink = "",
    status = DIGAS_REQUESTS_PREVIEW
)

data class DigaPreviewData(
    val name: String,
    val uiData: UiState<DigaMainScreenUiModel>,
    val selectedTap: DigaSegmentedControllerTap
)
