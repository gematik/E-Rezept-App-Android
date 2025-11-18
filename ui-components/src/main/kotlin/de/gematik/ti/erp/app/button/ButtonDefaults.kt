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

package de.gematik.ti.erp.app.button

import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.gematik.ti.erp.app.theme.AppTheme
// Todo: Use these in Button.kt instead of directly in screens!
object GemButtonDefaults {

    @Composable
    fun primaryButtonColors(
        backgroundColor: Color = AppTheme.colors.primary700,
        contentColor: Color = AppTheme.colors.neutral000,
        disabledBackgroundColor: Color = AppTheme.colors.neutral300,
        disabledContentColor: Color = AppTheme.colors.neutral600
    ): ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        disabledBackgroundColor = disabledBackgroundColor,
        disabledContentColor = disabledContentColor
    )

    @Composable
    fun secondaryButtonColors(
        backgroundColor: Color = AppTheme.colors.neutral100,
        contentColor: Color = AppTheme.colors.primary700,
        disabledBackgroundColor: Color = AppTheme.colors.neutral300,
        disabledContentColor: Color = AppTheme.colors.neutral600
    ): ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        disabledBackgroundColor = disabledBackgroundColor,
        disabledContentColor = disabledContentColor
    )

    @Composable
    fun tertiaryButtonColors(
        backgroundColor: Color = AppTheme.colors.neutral025,
        contentColor: Color = AppTheme.colors.primary700,
        disabledBackgroundColor: Color = AppTheme.colors.neutral300,
        disabledContentColor: Color = AppTheme.colors.neutral600
    ): ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        disabledBackgroundColor = disabledBackgroundColor,
        disabledContentColor = disabledContentColor
    )

    @Composable
    fun outlinedButtonColors(
        backgroundColor: Color = AppTheme.colors.neutral000,
        contentColor: Color = AppTheme.colors.primary700,
        disabledBackgroundColor: Color = AppTheme.colors.neutral300,
        disabledContentColor: Color = AppTheme.colors.neutral600
    ): ButtonColors = ButtonDefaults.outlinedButtonColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        disabledContentColor = disabledContentColor
    )
}
