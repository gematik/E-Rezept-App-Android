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

package de.gematik.ti.erp.app.material3.components.switchs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwitchColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.gematik.ti.erp.app.theme.AppTheme

object GemSwitchDefaults {
    private val disabledOpacity = 0.40f

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun gemSwitchColors(
        checkedThumbColor: Color = AppTheme.colors.neutral000,
        checkedTrackColor: Color = AppTheme.colors.primary700,
        checkedBorderColor: Color = Color.Transparent,
        checkedIconColor: Color = AppTheme.colors.primary700,
        uncheckedThumbColor: Color = AppTheme.colors.neutral700,
        uncheckedTrackColor: Color = AppTheme.colors.neutral025,
        uncheckedBorderColor: Color = AppTheme.colors.neutral700,
        uncheckedIconColor: Color = AppTheme.colors.neutral000,
        disabledCheckedThumbColor: Color = checkedThumbColor.copy(disabledOpacity),
        disabledCheckedTrackColor: Color = checkedTrackColor.copy(disabledOpacity),
        disabledCheckedBorderColor: Color = Color.Transparent,
        disabledCheckedIconColor: Color = checkedIconColor.copy(disabledOpacity),
        disabledUncheckedThumbColor: Color = uncheckedThumbColor.copy(disabledOpacity),
        disabledUncheckedTrackColor: Color = uncheckedTrackColor.copy(disabledOpacity),
        disabledUncheckedBorderColor: Color = uncheckedBorderColor.copy(disabledOpacity),
        disabledUncheckedIconColor: Color = uncheckedIconColor.copy(disabledOpacity)
    ): SwitchColors =
        SwitchColors(
            checkedThumbColor = checkedThumbColor,
            checkedTrackColor = checkedTrackColor,
            checkedBorderColor = checkedBorderColor,
            checkedIconColor = checkedIconColor,
            uncheckedThumbColor = uncheckedThumbColor,
            uncheckedTrackColor = uncheckedTrackColor,
            uncheckedBorderColor = uncheckedBorderColor,
            uncheckedIconColor = uncheckedIconColor,
            disabledCheckedThumbColor = disabledCheckedThumbColor,
            disabledCheckedTrackColor = disabledCheckedTrackColor,
            disabledCheckedBorderColor = disabledCheckedBorderColor,
            disabledCheckedIconColor = disabledCheckedIconColor,
            disabledUncheckedThumbColor = disabledUncheckedThumbColor,
            disabledUncheckedTrackColor = disabledUncheckedTrackColor,
            disabledUncheckedBorderColor = disabledUncheckedBorderColor,
            disabledUncheckedIconColor = disabledUncheckedIconColor
        )
}
