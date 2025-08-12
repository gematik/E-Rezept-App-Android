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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

// Material 3 Lib needs to migrate to materialTheme
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GemDateRangePickerDialog(
    initialStartDate: LocalDate?,
    initialEndDate: LocalDate?,
    modifier: Modifier = Modifier,
    cancelText: String = stringResource(R.string.cancel),
    okText: String = stringResource(R.string.ok),
    onConfirm: (startDate: LocalDate?, endDate: LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
        initialSelectedEndDateMillis = initialEndDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
        selectableDates = GemSelectableDates
    )
    DatePickerDialog(
        modifier = modifier,
        confirmButton = {
            TextButton(
                enabled = dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null,
                onClick = {
                    onConfirm(
                        dateRangePickerState.selectedStartDateMillis?.let { Instant.fromEpochMilliseconds(it).toLocalDate() },
                        dateRangePickerState.selectedEndDateMillis?.let { Instant.fromEpochMilliseconds(it).toLocalDate() }
                    )
                }
            ) {
                Text(okText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(cancelText)
            }
        },
        content = {
            Box(Modifier.weight(1f)) {
                GemDateRangePicker(
                    dateRangePickerState
                )
            }
        },
        onDismissRequest = onDismiss,
        colors = GemDatePickerDefaults.gemDatePickerColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GemDateRangePicker(
    datePickerState: DateRangePickerState
) {
    DateRangePicker(
        state = datePickerState,
        title = {
            Text(
                text = if (datePickerState.displayMode == DisplayMode.Picker) {
                    stringResource(R.string.date_picker_dialog_calender_header)
                } else {
                    stringResource(R.string.date_picker_dialog_textfield_header)
                },
                style = AppTheme.typography.caption1,
                modifier = Modifier
                    .fillMaxWidth()
                    .semanticsHeading()
                    .padding(start = PaddingDefaults.Large, end = PaddingDefaults.MediumSmall, top = PaddingDefaults.Medium),
                textAlign = TextAlign.Start
            )
        },
        colors = GemDatePickerDefaults.gemDatePickerColors()
    )
}
