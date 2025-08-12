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

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import kotlinx.datetime.LocalTime

// Material 3 Lib needs to migrate to materialTheme
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GemTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    var showPicker by remember { mutableStateOf(true) }
    val showPickerDescription = if (showPicker) {
        stringResource(R.string.time_picker_toggle_textfield)
    } else {
        stringResource(R.string.time_picker_toggle_dial)
    }
    ErezeptAlertDialog(
        title = {
            Text(
                text = if (showPicker) {
                    stringResource(R.string.time_picker_dialog_dial_header)
                } else {
                    stringResource(R.string.time_picker_dialog_textfield_header)
                },
                style = AppTheme.typography.caption1,
                modifier = Modifier.fillMaxWidth().semanticsHeading(),
                textAlign = TextAlign.Start
            )
        },
        body = {
            if (showPicker) {
                GemTimePicker(
                    timePickerState = timePickerState
                )
            } else {
                GemTimeInput(
                    timePickerState = timePickerState
                )
            }
        },
        actions = {
            IconButton(
                onClick = { showPicker = !showPicker }
            ) {
                Icon(
                    imageVector = if (showPicker) {
                        Icons.Rounded.Keyboard
                    } else {
                        Icons.Rounded.AccessTime
                    },
                    contentDescription = showPickerDescription,
                    tint = AppTheme.colors.neutral600
                )
            }
            Spacer(Modifier.weight(1f))
        },
        onConfirmRequest = {
            onConfirm(LocalTime(timePickerState.hour, timePickerState.minute))
        },
        onDismissRequest = {
            onDismiss()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GemTimePicker(
    timePickerState: TimePickerState
) {
    TimePicker(
        state = timePickerState,
        colors = GemTimePickerDefaults.gemTimePickerColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GemTimeInput(
    timePickerState: TimePickerState
) {
    TimeInput(
        state = timePickerState,
        colors = GemTimePickerDefaults.gemTimePickerColors()
    )
}

object GemTimePickerDefaults {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun gemTimePickerColors(
        clockDialColor: Color = AppTheme.colors.primary100,
        clockDialSelectedContentColor: Color = AppTheme.colors.neutral000,
        clockDialUnselectedContentColor: Color = AppTheme.colors.neutral900,
        selectorColor: Color = AppTheme.colors.primary700,
        containerColor: Color = AppTheme.colors.neutral000,
        periodSelectorBorderColor: Color = AppTheme.colors.primary700,
        periodSelectorSelectedContainerColor: Color = AppTheme.colors.primary100,
        periodSelectorUnselectedContainerColor: Color = AppTheme.colors.neutral025,
        periodSelectorSelectedContentColor: Color = AppTheme.colors.primary700,
        periodSelectorUnselectedContentColor: Color = AppTheme.colors.neutral900,
        timeSelectorSelectedContainerColor: Color = AppTheme.colors.primary100,
        timeSelectorUnselectedContainerColor: Color = AppTheme.colors.neutral025,
        timeSelectorSelectedContentColor: Color = AppTheme.colors.primary700,
        timeSelectorUnselectedContentColor: Color = AppTheme.colors.neutral900
    ): TimePickerColors =
        TimePickerColors(
            clockDialColor = clockDialColor,
            clockDialSelectedContentColor = clockDialSelectedContentColor,
            clockDialUnselectedContentColor = clockDialUnselectedContentColor,
            selectorColor = selectorColor,
            containerColor = containerColor,
            periodSelectorBorderColor = periodSelectorBorderColor,
            periodSelectorSelectedContainerColor = periodSelectorSelectedContainerColor,
            periodSelectorUnselectedContainerColor = periodSelectorUnselectedContainerColor,
            periodSelectorSelectedContentColor = periodSelectorSelectedContentColor,
            periodSelectorUnselectedContentColor = periodSelectorUnselectedContentColor,
            timeSelectorSelectedContainerColor = timeSelectorSelectedContainerColor,
            timeSelectorUnselectedContainerColor = timeSelectorUnselectedContainerColor,
            timeSelectorSelectedContentColor = timeSelectorSelectedContentColor,
            timeSelectorUnselectedContentColor = timeSelectorUnselectedContentColor
        )
}
