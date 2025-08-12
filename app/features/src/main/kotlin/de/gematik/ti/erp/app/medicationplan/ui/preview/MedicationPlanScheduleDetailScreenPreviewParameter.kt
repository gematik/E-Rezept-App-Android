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

package de.gematik.ti.erp.app.medicationplan.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE_ENDLESS
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SCANNED_PRESCRIPTION_SCHEDULE_INACTIVE
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_SCHEDULE_ACTIVE_EVERY_TWO_DAYS
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_SCHEDULE_INACTIVE
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_ENDLESS
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_PERSONALIZED
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_INACTIVE
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.medicationPlanPreviewCurrentTime
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.LocalDate

data class MedicationPlanScheduleDetailScreenPreview(
    val name: String,
    val dosageInstruction: MedicationPlanDosageInstruction,
    val state: UiState<MedicationSchedule>,
    val currentDate: LocalDate = medicationPlanPreviewCurrentTime.toLocalDate(),
    val isIgnoringBatteryOptimizations: Boolean = true
)

class MedicationPlanScheduleDetailScreenPreviewParameter : PreviewParameterProvider<MedicationPlanScheduleDetailScreenPreview> {

    override val values: Sequence<MedicationPlanScheduleDetailScreenPreview>
        get() = sequenceOf(
            MedicationPlanScheduleDetailScreenPreview(
                name = "error state",
                state = UiState.Error(Throwable("test error")),
                dosageInstruction = MedicationPlanDosageInstruction.Empty,
                isIgnoringBatteryOptimizations = false
            ),
            MedicationPlanScheduleDetailScreenPreview(
                name = "scanned prescription schedule inactive",
                dosageInstruction = MedicationPlanDosageInstruction.Empty,
                state = UiState.Data(SCANNED_PRESCRIPTION_SCHEDULE_INACTIVE)
            ),
            MedicationPlanScheduleDetailScreenPreview(
                name = "scanned prescription schedule active",
                dosageInstruction = MedicationPlanDosageInstruction.Empty,
                state = UiState.Data(SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE_ENDLESS)
            ),
            MedicationPlanScheduleDetailScreenPreview(
                name = "synced prescription schedule inactive",
                state = UiState.Data(SYNCED_PRESCRIPTION_SCHEDULE_INACTIVE),
                dosageInstruction = MedicationPlanDosageInstruction.External,
                isIgnoringBatteryOptimizations = false
            ),
            MedicationPlanScheduleDetailScreenPreview(
                name = "synced prescription schedule active",
                state = UiState.Data(SYNCED_PRESCRIPTION_SCHEDULE_ACTIVE_EVERY_TWO_DAYS),
                dosageInstruction = MedicationPlanDosageInstruction.External,
                isIgnoringBatteryOptimizations = false
            ),
            MedicationPlanScheduleDetailScreenPreview(
                name = "synced prescription structured schedule active",
                dosageInstruction = MedicationPlanDosageInstruction.FreeText("dosage"),
                state = UiState.Data(SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_INACTIVE)
            ),
            MedicationPlanScheduleDetailScreenPreview(
                name = "synced prescription structured schedule active and personalized",
                dosageInstruction = MedicationPlanDosageInstruction.FreeText("dosage"),
                state = UiState.Data(SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_PERSONALIZED)
            ),
            MedicationPlanScheduleDetailScreenPreview(
                name = "synced prescription structured schedule active endless",
                dosageInstruction = MedicationPlanDosageInstruction.Empty,
                state = UiState.Data(SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_ENDLESS)
            )
        )
}
