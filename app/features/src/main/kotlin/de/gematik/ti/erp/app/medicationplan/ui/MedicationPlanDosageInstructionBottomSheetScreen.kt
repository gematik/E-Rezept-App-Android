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

/*
 * Copyright 2024, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *j
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.medicationplan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationPlanDosageInfoPreview
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationPlanDosageInfoPreviewParameter
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction
import de.gematik.ti.erp.app.medicationplan.presentation.rememberMedicationPlanDosageInstructionBottomSheetController
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.Body2lText
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

class MedicationPlanDosageInstructionBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(forceToMaxHeight = true) {
    @Composable
    override fun Content() {
        val taskId = navBackStackEntry.arguments?.getString("taskId")
        val controller = rememberMedicationPlanDosageInstructionBottomSheetController(taskId ?: "")
        val dosageInstructionState by controller.dosageInstruction.collectAsStateWithLifecycle()

        UiStateMachine(
            state = dosageInstructionState,
            onError = { _ ->
                ErrorScreenComponent()
            },
            onLoading = {
                FullScreenLoadingIndicator()
            },
            onContent = { dosageInstruction ->
                MedicationPlanDosageInstructionBottomSheetContent(dosageInstruction = dosageInstruction)
            },
            onEmpty = {
                ErrorScreenComponent()
            }
        )
    }
}

@Composable
private fun MedicationPlanDosageInstructionBottomSheetContent(dosageInstruction: MedicationPlanDosageInstruction) {
    when (dosageInstruction) {
        is MedicationPlanDosageInstruction.External -> ExternalInfo()
        MedicationPlanDosageInstruction.Empty -> EmptyInfo()
        is MedicationPlanDosageInstruction.FreeText -> FreeTextInfo(dosageInstruction)
        is MedicationPlanDosageInstruction.Structured -> StructuredInfo(dosageInstruction)
    }
}

@Composable
private fun StructuredInfo(dosageInstruction: MedicationPlanDosageInstruction.Structured) {
    InfoContent(
        dosageText = dosageInstruction.text,
        body = stringResource(R.string.structured_dosage_info_body)
    ) {
        dosageInstruction.interpretation.forEach { (dayTime, times) ->
            when (dayTime) {
                MedicationPlanDosageInstruction.DayTime.MORNING -> Body2lText(stringResource(R.string.structured_dosage_morning, times))
                MedicationPlanDosageInstruction.DayTime.NOON -> Body2lText(stringResource(R.string.structured_dosage_noon, times))
                MedicationPlanDosageInstruction.DayTime.EVENING -> Body2lText(stringResource(R.string.structured_dosage_evening, times))
                MedicationPlanDosageInstruction.DayTime.NIGHT -> Body2lText(stringResource(R.string.structured_dosage_night, times))
            }
        }
    }
}

@Composable
private fun FreeTextInfo(dosageInstruction: MedicationPlanDosageInstruction.FreeText) {
    InfoContent(
        dosageText = dosageInstruction.text,
        body = stringResource(R.string.freetext_dosage_info_body)
    )
}

@Composable
private fun EmptyInfo() {
    InfoContent(
        dosageText = stringResource(R.string.empty_dosage_info_dosage_text),
        body = stringResource(R.string.empty_dosage_info_body)
    )
}

@Composable
private fun ExternalInfo() {
    InfoContent(
        dosageText = stringResource(R.string.external_dosage_info_dosage_text),
        body = stringResource(R.string.external_dosage_info_body)
    )
}

@Composable
private fun InfoContent(dosageText: String, body: String, footer: (@Composable () -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(top = PaddingDefaults.Medium, bottom = PaddingDefaults.XXLarge),
        verticalArrangement = Arrangement.spacedBy(SizeDefaults.one)
    ) {
        Text(
            stringResource(R.string.dosage_info_bottomsheet_header),
            style = AppTheme.typography.subtitle1
        )
        Text(
            dosageText,
            style = AppTheme.typography.body2,
            color = AppTheme.colors.neutral600
        )
        Text(
            body,
            style = AppTheme.typography.body2,
            color = AppTheme.colors.neutral600
        )
        footer?.invoke()
    }
}

@LightDarkPreview
@Composable
fun MedicationPlanDosageInfoContentPreview(
    @PreviewParameter(MedicationPlanDosageInfoPreviewParameter::class) previewData: MedicationPlanDosageInfoPreview
) {
    PreviewAppTheme {
        MedicationPlanDosageInstructionBottomSheetContent(previewData.dosageInstruction)
    }
}
