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

package de.gematik.ti.erp.app.prescription.detail.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.task.model.AccidentType
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.datetime.Instant

data class AccidentInfoPreviewParameter(
    val name: String,
    val syncedPrescription: TaskErpModel.Synced.Prescription
)

class AccidentInfoPreviewParameterProvider : PreviewParameterProvider<AccidentInfoPreviewParameter> {

    override val values: Sequence<AccidentInfoPreviewParameter>
        get() = sequenceOf(
            AccidentInfoPreviewParameter(
                "None",
                PREVIEW_SYNCED_PRESCRIPTION_ACCIDENT_TYPE_NONE
            ),
            AccidentInfoPreviewParameter(
                "Accident",
                PREVIEW_SYNCED_PRESCRIPTION_ACCIDENT_TYPE_ACCIDENT
            ),
            AccidentInfoPreviewParameter(
                "Work Accident",
                PREVIEW_SYNCED_PRESCRIPTION_ACCIDENT_TYPE_WORK_ACCIDENT
            ),
            AccidentInfoPreviewParameter(
                "Occupational Illness",
                PREVIEW_SYNCED_PRESCRIPTION_ACCIDENT_TYPE_OCCUPATIONAL_ILLNESS
            )
        )
}

private val PREVIEW_SYNCED_PRESCRIPTION_ACCIDENT_TYPE_NONE = SYNCED_TASK.copy(
    status = TaskStatusEnum.Ready,
    medicationRequest = SYNCED_TASK.medicationRequest?.copy(
        accidentType = AccidentType.None,
        dateOfAccident = null,
        location = null
    )
)

val time = Instant.parse("2024-07-03T14:20:00Z")

private val PREVIEW_SYNCED_PRESCRIPTION_ACCIDENT_TYPE_ACCIDENT = SYNCED_TASK.copy(
    status = TaskStatusEnum.Ready,
    medicationRequest = SYNCED_TASK.medicationRequest?.copy(
        accidentType = AccidentType.Unfall,
        dateOfAccident = time,
        location = "somewhere"
    )
)

private val PREVIEW_SYNCED_PRESCRIPTION_ACCIDENT_TYPE_WORK_ACCIDENT = SYNCED_TASK.copy(
    status = TaskStatusEnum.Ready,
    medicationRequest = SYNCED_TASK.medicationRequest?.copy(
        accidentType = AccidentType.Arbeitsunfall,
        dateOfAccident = time,
        location = "work"
    )
)

private val PREVIEW_SYNCED_PRESCRIPTION_ACCIDENT_TYPE_OCCUPATIONAL_ILLNESS = SYNCED_TASK.copy(
    status = TaskStatusEnum.Ready,
    medicationRequest = SYNCED_TASK.medicationRequest?.copy(
        accidentType = AccidentType.Berufskrankheit,
        dateOfAccident = time,
        location = "home"
    )
)
