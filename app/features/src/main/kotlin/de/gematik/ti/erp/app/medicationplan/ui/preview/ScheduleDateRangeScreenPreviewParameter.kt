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
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SCANNED_TASK_SCHEDULE
import de.gematik.ti.erp.app.utils.maxLocalDate
import de.gematik.ti.erp.app.utils.toLocalDate
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.days

data class ScheduleDateRangeScreenPreview(
    val name: String,
    val state: UiState<PrescriptionSchedule>,
    val currentDate: LocalDate = medicationPlanPreviewCurrentTime.toLocalDate()
)

class ScheduleDateRangeScreenPreviewParameter : PreviewParameterProvider<ScheduleDateRangeScreenPreview> {

    override val values: Sequence<ScheduleDateRangeScreenPreview>
        get() = sequenceOf(
            ScheduleDateRangeScreenPreview(
                name = "error state",
                state = UiState.Error(Throwable("test error"))
            ),
            ScheduleDateRangeScreenPreview(
                name = "scheduled endless",
                state = UiState.Data(
                    SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE.copy(
                        medicationSchedule = SCANNED_TASK_SCHEDULE.copy(
                            start = medicationPlanPreviewCurrentTime.toLocalDate(),
                            end = maxLocalDate()
                        )
                    )
                )
            ),
            ScheduleDateRangeScreenPreview(
                name = "scheduled individual",
                state = UiState.Data(
                    SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE.copy(
                        medicationSchedule = SCANNED_TASK_SCHEDULE.copy(
                            start = medicationPlanPreviewCurrentTime.toLocalDate(),
                            end = medicationPlanPreviewCurrentTime.plus(10.days).toLocalDate()
                        )
                    )
                )
            )
        )
}
