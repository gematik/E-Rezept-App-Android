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

package de.gematik.ti.erp.app.listitem

import androidx.compose.material3.ListItemColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.gematik.ti.erp.app.theme.AppTheme

object GemListItemDefaults {
    private const val DISABLED_OPACITY = 0.30f

    @Composable
    fun gemListItemColors(
        containerColor: Color = AppTheme.colors.neutral000,
        headlineColor: Color = AppTheme.colors.neutral900,
        leadingIconColor: Color = AppTheme.colors.primary700,
        overlineColor: Color = AppTheme.colors.neutral600,
        supportingColor: Color = AppTheme.colors.neutral600,
        trailingIconColor: Color = AppTheme.colors.primary700,
        disabledHeadlineColor: Color = headlineColor
            .copy(alpha = DISABLED_OPACITY),
        disabledLeadingIconColor: Color = leadingIconColor
            .copy(alpha = DISABLED_OPACITY),
        disabledTrailingIconColor: Color = trailingIconColor
            .copy(alpha = DISABLED_OPACITY)
    ): ListItemColors =
        ListItemColors(
            containerColor = containerColor,
            headlineColor = headlineColor,
            leadingIconColor = leadingIconColor,
            overlineColor = overlineColor,
            supportingTextColor = supportingColor,
            trailingIconColor = trailingIconColor,
            disabledHeadlineColor = disabledHeadlineColor,
            disabledLeadingIconColor = disabledLeadingIconColor,
            disabledTrailingIconColor = disabledTrailingIconColor
        )
}
