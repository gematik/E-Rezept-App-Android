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
import de.gematik.ti.erp.app.medicationplan.model.ProfileWithSchedules
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.PROFILE1
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.PROFILE2
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE_ENDLESS
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SCANNED_PRESCRIPTION_SCHEDULE_INACTIVE
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_SCHEDULE_INACTIVE
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_ENDLESS
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_PERSONALIZED
import de.gematik.ti.erp.app.profiles.usecase.mapper.toModel
import de.gematik.ti.erp.app.utils.uistate.UiState

data class MedicationPlanScheduleListScreenPreview(
    val name: String,
    val state: UiState<List<ProfileWithSchedules>>
)

class MedicationPlanScheduleListScreenPreviewParameter : PreviewParameterProvider<MedicationPlanScheduleListScreenPreview> {

    override val values: Sequence<MedicationPlanScheduleListScreenPreview>
        get() = sequenceOf(
            MedicationPlanScheduleListScreenPreview(
                name = "error state",
                state = UiState.Error(Throwable("test error"))
            ),
            MedicationPlanScheduleListScreenPreview(
                name = "empty state",
                state = UiState.Empty()
            ),
            MedicationPlanScheduleListScreenPreview(
                name = "data state one profile with inactive schedules",
                state = UiState.Data(
                    listOf(
                        ProfileWithSchedules(
                            PROFILE1.toModel(),
                            medicationSchedules = listOf(
                                SCANNED_PRESCRIPTION_SCHEDULE_INACTIVE,
                                SYNCED_PRESCRIPTION_SCHEDULE_INACTIVE
                            )
                        )
                    )
                )
            ),
            MedicationPlanScheduleListScreenPreview(
                name = "data state one profile with notifications",
                state = UiState.Data(
                    listOf(
                        ProfileWithSchedules(
                            PROFILE1.toModel(),
                            medicationSchedules = listOf(
                                SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_ENDLESS
                            )
                        )
                    )
                )
            ),
            MedicationPlanScheduleListScreenPreview(
                name = "data state two profiles with notifications",
                state = UiState.Data(
                    listOf(
                        ProfileWithSchedules(
                            PROFILE1.toModel(),
                            medicationSchedules = listOf(
                                SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE_ENDLESS
                            )
                        ),
                        ProfileWithSchedules(
                            PROFILE2.toModel(),
                            medicationSchedules = listOf(
                                @Suppress("MagicNumber")
                                SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_ENDLESS,
                                SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_PERSONALIZED
                            )
                        )
                    )
                )
            )
        )
}
