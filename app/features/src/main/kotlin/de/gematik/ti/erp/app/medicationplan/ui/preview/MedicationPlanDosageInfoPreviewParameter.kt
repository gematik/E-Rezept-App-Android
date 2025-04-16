/*
 * Copyright 2025, gematik GmbH
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

@file:Suppress("UnusedPrivateProperty")

package de.gematik.ti.erp.app.medicationplan.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction

data class MedicationPlanDosageInfoPreview(
    val name: String,
    val dosageInstruction: MedicationPlanDosageInstruction
)

class MedicationPlanDosageInfoPreviewParameter : PreviewParameterProvider<MedicationPlanDosageInfoPreview> {

    override val values: Sequence<MedicationPlanDosageInfoPreview>
        get() = sequenceOf(
            MedicationPlanDosageInfoPreview(
                name = "free text dosage instruction",
                dosageInstruction = PREVIEW_FREETEXT_DOSAGE_INSTRUCTION
            ),
            MedicationPlanDosageInfoPreview(
                name = "empty dosage instruction",
                dosageInstruction = PREVIEW_EMPTY_DOSAGE_INSTRUCTION
            ),
            MedicationPlanDosageInfoPreview(
                name = "external dosage instruction",
                dosageInstruction = PREVIEW_EXTERNAL_DOSAGE_INSTRUCTION
            ),
            MedicationPlanDosageInfoPreview(
                name = "structured dosage instruction one in morning",
                dosageInstruction = PREVIEW_STRUCTURED_DOSAGE_INSTRUCTION_ONE_IN_MORNING
            ),
            MedicationPlanDosageInfoPreview(
                name = "structured dosage instruction two in all day times",
                dosageInstruction = PREVIEW_STRUCTURED_DOSAGE_INSTRUCTION_TWO_IN_ALL_DAY_TIMES
            )
        )
}

private val PREVIEW_FREETEXT_DOSAGE_INSTRUCTION = MedicationPlanDosageInstruction.FreeText(
    text = "Take 1 tablet every 8 hours"
)

private val PREVIEW_EMPTY_DOSAGE_INSTRUCTION = MedicationPlanDosageInstruction.Empty

private val PREVIEW_EXTERNAL_DOSAGE_INSTRUCTION = MedicationPlanDosageInstruction.External

private val PREVIEW_STRUCTURED_DOSAGE_INSTRUCTION_ONE_IN_MORNING = MedicationPlanDosageInstruction.Structured(
    text = "1-0-0",
    interpretation = mapOf(
        MedicationPlanDosageInstruction.DayTime.MORNING to "1"
    )
)

private val PREVIEW_STRUCTURED_DOSAGE_INSTRUCTION_TWO_IN_ALL_DAY_TIMES = MedicationPlanDosageInstruction.Structured(
    text = "2-2-2-2",
    interpretation = mapOf(
        MedicationPlanDosageInstruction.DayTime.MORNING to "2",
        MedicationPlanDosageInstruction.DayTime.NOON to "2",
        MedicationPlanDosageInstruction.DayTime.EVENING to "2",
        MedicationPlanDosageInstruction.DayTime.NIGHT to "2"
    )
)
