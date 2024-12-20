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

package de.gematik.ti.erp.app.medicationplan.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.medicationplan.presentation.PrescriptionSchedule
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.ACTIVE_SYNCED_PRESCRIPTION_SCHEDULE
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.ACTIVE_SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.ACTIVE_SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ENDLESS
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SCANNED_PRESCRIPTION_SCHEDULE_INACTIVE
import de.gematik.ti.erp.app.utils.toLocalDate
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

val medicationPlanPreviewCurrentTime = Instant.parse("2023-01-01T16:20:00Z")

data class MedicationScheduleScreenPreview(
    val name: String,
    val state: UiState<PrescriptionSchedule>,
    val currentDate: LocalDate = medicationPlanPreviewCurrentTime.toLocalDate(),
    val isIgnoringBatteryOptimizations: Boolean = true
)

class MedicationPlanScheduleScreenPreviewParameter : PreviewParameterProvider<MedicationScheduleScreenPreview> {

    override val values: Sequence<MedicationScheduleScreenPreview>
        get() = sequenceOf(
            MedicationScheduleScreenPreview(
                name = "error state",
                state = UiState.Error(Throwable("test error")),
                isIgnoringBatteryOptimizations = false
            ),
            MedicationScheduleScreenPreview(
                name = "scanned prescription schedule inactive",
                state = UiState.Data(SCANNED_PRESCRIPTION_SCHEDULE_INACTIVE)
            ),
            MedicationScheduleScreenPreview(
                name = "scanned prescription schedule active",
                state = UiState.Data(SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE)
            ),
            MedicationScheduleScreenPreview(
                name = "synced prescription schedule active",
                state = UiState.Data(ACTIVE_SYNCED_PRESCRIPTION_SCHEDULE),
                isIgnoringBatteryOptimizations = false
            ),
            MedicationScheduleScreenPreview(
                name = "synced prescription structured schedule active",
                state = UiState.Data(ACTIVE_SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE)
            ),
            MedicationScheduleScreenPreview(
                name = "synced prescription structured schedule active endless",
                state = UiState.Data(ACTIVE_SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ENDLESS)
            )
        )
}
