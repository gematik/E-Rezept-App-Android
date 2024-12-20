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
import de.gematik.ti.erp.app.medicationplan.model.MedicationDosage
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotification
import de.gematik.ti.erp.app.medicationplan.model.ProfileWithSchedules
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.PROFILE1
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.PROFILE2
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE
import de.gematik.ti.erp.app.profiles.usecase.mapper.toModel
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime

data class MedicationSuccessScreenPreview(
    val name: String,
    val state: UiState<List<ProfileWithSchedules>>,
    val currentTime: Instant = medicationPlanPreviewCurrentTime
)

class MedicationSuccessScreenPreviewParameter : PreviewParameterProvider<MedicationSuccessScreenPreview> {

    override val values: Sequence<MedicationSuccessScreenPreview>
        get() = sequenceOf(
            MedicationSuccessScreenPreview(
                name = "error state",
                state = UiState.Error(Throwable("test error"))
            ),
            MedicationSuccessScreenPreview(
                name = "empty state",
                state = UiState.Empty()
            ),
            MedicationSuccessScreenPreview(
                name = "data state one profile with notifications",
                state = UiState.Data(
                    listOf(
                        ProfileWithSchedules(
                            PROFILE1.toModel(),
                            medicationSchedules = listOf(
                                SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE
                            )
                        )
                    )
                )
            ),
            MedicationSuccessScreenPreview(
                name = "data state two profiles with notifications",
                state = UiState.Data(
                    listOf(
                        ProfileWithSchedules(
                            PROFILE1.toModel(),
                            medicationSchedules = listOf(
                                SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE
                            )
                        ),
                        ProfileWithSchedules(
                            PROFILE2.toModel(),
                            medicationSchedules = listOf(
                                @Suppress("MagicNumber")
                                SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE.medicationSchedule.copy(
                                    notifications = listOf(
                                        MedicationNotification(
                                            dosage = MedicationDosage("TAB", "1"),
                                            time = LocalTime(12, 0)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
}
