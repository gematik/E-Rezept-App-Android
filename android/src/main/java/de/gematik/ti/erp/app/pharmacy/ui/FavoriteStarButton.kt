/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import de.gematik.ti.erp.app.utils.compose.shortToast

@Composable
internal fun FavoriteStarButton(
    isMarked: Boolean,
    modifier: Modifier = Modifier,
    onChange: (Boolean) -> Unit
) {
    val color = if (isMarked) {
        AppTheme.colors.yellow500
    } else {
        AppTheme.colors.primary600
    }

    val icon = if (isMarked) {
        Icons.Rounded.Star
    } else {
        Icons.Rounded.StarBorder
    }

    val addedText = stringResource(R.string.pharmacy_detals_added_to_favorites)
    val removedText = stringResource(R.string.pharmacy_detalls_removed_from_favorites)

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    TertiaryButton(
        modifier = modifier.size(56.dp),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onChange(!isMarked)
            context.shortToast(
                when {
                    !isMarked -> addedText
                    else -> removedText
                }
            )
        },
        contentPadding = PaddingValues(PaddingDefaults.Medium)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color
        )
    }
}
