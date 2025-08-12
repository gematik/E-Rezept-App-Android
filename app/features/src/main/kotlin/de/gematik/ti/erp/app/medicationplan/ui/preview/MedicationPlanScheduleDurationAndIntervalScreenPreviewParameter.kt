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
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE_ENDLESS
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_SCHEDULE_ACTIVE_EVERY_TWO_DAYS
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_ENDLESS
import de.gematik.ti.erp.app.medicationplan.ui.preview.mocks.SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_PERSONALIZED
import de.gematik.ti.erp.app.utils.uistate.UiState

data class MedicationPlanScheduleDurationAndIntervalScreenPreview(
    val name: String,
    val state: UiState<MedicationSchedule>
)

class MedicationPlanScheduleDurationAndIntervalScreenPreviewParameter : PreviewParameterProvider<MedicationPlanScheduleDurationAndIntervalScreenPreview> {

    override val values: Sequence<MedicationPlanScheduleDurationAndIntervalScreenPreview>
        get() = sequenceOf(
            MedicationPlanScheduleDurationAndIntervalScreenPreview(
                name = "scanned prescription schedule active",
                state = UiState.Data(SCANNED_PRESCRIPTION_SCHEDULE_ACTIVE_ENDLESS)
            ),
            MedicationPlanScheduleDurationAndIntervalScreenPreview(
                name = "synced prescription schedule active",
                state = UiState.Data(SYNCED_PRESCRIPTION_SCHEDULE_ACTIVE_EVERY_TWO_DAYS)
            ),
            MedicationPlanScheduleDurationAndIntervalScreenPreview(
                name = "synced prescription structured schedule active and personalized",
                state = UiState.Data(SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_PERSONALIZED)
            ),
            MedicationPlanScheduleDurationAndIntervalScreenPreview(
                name = "synced prescription structured schedule active endless",
                state = UiState.Data(SYNCED_PRESCRIPTION_STRUCTURED_SCHEDULE_ACTIVE_ENDLESS)
            )
        )
}
