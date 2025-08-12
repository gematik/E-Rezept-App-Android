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

package de.gematik.ti.erp.app.medicationplan.ui.components

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

internal fun LazyListScope.activateScheduleAndDosageInstructionCard(
    schedule: MedicationSchedule,
    dosageInstruction: MedicationPlanDosageInstruction,
    isIgnoringBatteryOptimizations: Boolean,
    onIgnoreBatteryOptimizations: () -> Unit,
    onActivateSchedule: () -> Unit,
    onDeactivateSchedule: () -> Unit,
    onClickDosageInfo: () -> Unit
) {
    item {
        Card(
            backgroundColor = AppTheme.colors.neutral000,
            border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral100),
            shape = RoundedCornerShape(SizeDefaults.doubleHalf),
            elevation = SizeDefaults.quarter
        ) {
            Column(modifier = Modifier.padding(vertical = PaddingDefaults.Small)) {
                ScheduleActivitySection(schedule, onActivateSchedule, onDeactivateSchedule)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    IgnoreBatteryOptimizationSection(
                        batteryOptimizationDisabled = isIgnoringBatteryOptimizations,
                        onIgnoreBatteryOptimizations = onIgnoreBatteryOptimizations
                    )
                }
                DosageInfoSection(dosageInstruction, onClickDosageInfo)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ScheduleActivitySection(
    schedule: MedicationSchedule,
    onActivateSchedule: () -> Unit,
    onDeactivateSchedule: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .toggleable(
                value = schedule.isActive,
                onValueChange = { checked ->
                    if (checked) {
                        onActivateSchedule()
                    } else {
                        onDeactivateSchedule()
                    }
                },
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            )
            .semantics(true) {},
        text = {
            Text(
                text = stringResource(R.string.activate_schedule_switch_text),
                style = AppTheme.typography.body1
            )
        },
        trailing = {
            Switch(
                checked = schedule.isActive,
                onCheckedChange = null
            )
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IgnoreBatteryOptimizationSection(
    batteryOptimizationDisabled: Boolean,
    onIgnoreBatteryOptimizations: () -> Unit
) {
    ListItem(
        modifier = Modifier.toggleable(
            value = batteryOptimizationDisabled,
            onValueChange = { checked ->
                onIgnoreBatteryOptimizations()
            },
            role = Role.Switch,
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current
        )
            .semantics(true) {},
        text = {
            Text(
                style = AppTheme.typography.body1,
                text = stringResource(R.string.exact_alarms_info)
            )
        },
        trailing = {
            Switch(
                checked = batteryOptimizationDisabled,
                onCheckedChange = null
            )
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DosageInfoSection(
    dosageInstruction: MedicationPlanDosageInstruction,
    onClickDosageInfo: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .clickable(
                role = Role.Button
            ) {
                onClickDosageInfo()
            },
        overlineText = {
            Text(
                stringResource(R.string.plan_schedule_dosage_instruction_label),
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral600
            )
        },
        text = {
            Text(
                style = AppTheme.typography.body1,
                text = when (dosageInstruction) {
                    is MedicationPlanDosageInstruction.FreeText -> dosageInstruction.text
                    is MedicationPlanDosageInstruction.Structured -> dosageInstruction.text
                    is MedicationPlanDosageInstruction.Empty -> stringResource(R.string.dosage_instruction_empty)
                    is MedicationPlanDosageInstruction.External -> stringResource(R.string.dosage_instruction_external)
                }
            )
        },
        trailing = {
            Icon(
                modifier = Modifier,
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = AppTheme.colors.primary700
            )
        }
    )
}
