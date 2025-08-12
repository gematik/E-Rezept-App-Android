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

import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.gematik.ti.erp.app.fhir.temporal.toStartOfDayInUTC
import de.gematik.ti.erp.app.theme.AppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object GemDatePickerDefaults {
    private val disabledOpacity = 0.40f

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun gemDatePickerColors(
        containerColor: Color = AppTheme.colors.neutral000,
        titleContentColor: Color = AppTheme.colors.neutral600,
        headlineContentColor: Color = AppTheme.colors.neutral900,
        weekdayContentColor: Color = AppTheme.colors.neutral900,
        subheadContentColor: Color = AppTheme.colors.neutral600,
        navigationContentColor: Color = AppTheme.colors.neutral900,
        yearContentColor: Color = AppTheme.colors.neutral900,
        disabledYearContentColor: Color = yearContentColor.copy(alpha = disabledOpacity),
        currentYearContentColor: Color = AppTheme.colors.primary400,
        selectedYearContentColor: Color = AppTheme.colors.neutral000,
        disabledSelectedYearContentColor: Color = selectedYearContentColor.copy(alpha = disabledOpacity),
        selectedYearContainerColor: Color = AppTheme.colors.primary700,
        disabledSelectedYearContainerColor: Color = selectedYearContentColor.copy(alpha = disabledOpacity),
        dayContentColor: Color = AppTheme.colors.neutral900,
        disabledDayContentColor: Color = dayContentColor.copy(alpha = disabledOpacity),
        selectedDayContentColor: Color = AppTheme.colors.neutral000,
        disabledSelectedDayContentColor: Color = selectedDayContentColor.copy(alpha = disabledOpacity),
        selectedDayContainerColor: Color = AppTheme.colors.primary700,
        disabledSelectedDayContainerColor: Color = selectedDayContainerColor.copy(alpha = disabledOpacity),
        todayContentColor: Color = AppTheme.colors.primary400,
        todayDateBorderColor: Color = AppTheme.colors.primary400,
        dayInSelectionRangeContainerColor: Color = AppTheme.colors.primary100,
        dayInSelectionRangeContentColor: Color = AppTheme.colors.neutral900,
        dividerColor: Color = AppTheme.colors.neutral600,
        dateTextFieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors()
    ): DatePickerColors =
        DatePickerColors(
            containerColor = containerColor,
            titleContentColor = titleContentColor,
            headlineContentColor = headlineContentColor,
            weekdayContentColor = weekdayContentColor,
            subheadContentColor = subheadContentColor,
            navigationContentColor = navigationContentColor,
            yearContentColor = yearContentColor,
            disabledYearContentColor = disabledYearContentColor,
            currentYearContentColor = currentYearContentColor,
            selectedYearContentColor = selectedYearContentColor,
            disabledSelectedYearContentColor = disabledSelectedYearContentColor,
            selectedYearContainerColor = selectedYearContainerColor,
            disabledSelectedYearContainerColor = disabledSelectedYearContainerColor,
            dayContentColor = dayContentColor,
            disabledDayContentColor = disabledDayContentColor,
            selectedDayContentColor = selectedDayContentColor,
            disabledSelectedDayContentColor = disabledSelectedDayContentColor,
            selectedDayContainerColor = selectedDayContainerColor,
            disabledSelectedDayContainerColor = disabledSelectedDayContainerColor,
            todayContentColor = todayContentColor,
            todayDateBorderColor = todayDateBorderColor,
            dayInSelectionRangeContainerColor = dayInSelectionRangeContainerColor,
            dayInSelectionRangeContentColor = dayInSelectionRangeContentColor,
            dividerColor = dividerColor,
            dateTextFieldColors = dateTextFieldColors
        )
}

@OptIn(ExperimentalMaterial3Api::class)
object GemSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis >= Clock.System.now().toStartOfDayInUTC().toEpochMilliseconds()
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year >= Clock.System.now().toLocalDateTime(TimeZone.UTC).year
    }
}
