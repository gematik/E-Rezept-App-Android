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

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

object GemIconButtonDefaults {
    private const val DISABLED_OPACITY = 0.30f

    @Composable
    fun gemPrimaryIconButtonColors(
        containerColor: Color = AppTheme.colors.primary700,
        contentColor: Color = AppTheme.colors.neutral000,
        disabledContainerColor: Color = containerColor.copy(alpha = DISABLED_OPACITY),
        disabledContentColor: Color = contentColor.copy(alpha = DISABLED_OPACITY)
    ): IconButtonColors =
        IconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        )

    @Composable
    fun gemIconButtonColors(
        containerColor: Color = AppTheme.colors.neutral000,
        contentColor: Color = AppTheme.colors.primary700,
        disabledContainerColor: Color = containerColor.copy(alpha = DISABLED_OPACITY),
        disabledContentColor: Color = contentColor.copy(alpha = DISABLED_OPACITY)
    ): IconButtonColors =
        IconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        )

    @Composable
    fun gemOutlinedIconButtonBorder(
        enabled: Boolean = true,
        color: Color = AppTheme.colors.primary700
    ): BorderStroke {
        val borderColor: Color = if (enabled) {
            color
        } else {
            color
                .copy(alpha = DISABLED_OPACITY)
        }
        return remember(color) {
            BorderStroke(SizeDefaults.eighth, borderColor)
        }
    }
}
