/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme

@Composable
fun BottomSheetAction(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector,
    title: String,
    info: String,
    onClick: () -> Unit
) =
    BottomSheetAction(
        modifier = modifier,
        enabled = enabled,
        icon = {
            Icon(
                icon,
                null,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
        },
        title = { Text(title) },
        info = { Text(info) },
        onClick = onClick
    )

@Composable
fun BottomSheetAction(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable RowScope.() -> Unit,
    title: @Composable ColumnScope.() -> Unit,
    info: @Composable ColumnScope.() -> Unit,
    onClick: () -> Unit
) {
    val titleColor = if (enabled) {
        Color.Unspecified
    } else {
        AppTheme.colors.neutral600
    }

    val textColor = if (enabled) {
        AppTheme.colors.neutral600
    } else {
        AppTheme.colors.neutral400
    }

    Row(
        modifier = modifier
            .clickable(onClick = onClick, enabled = enabled)
            .padding(16.dp)
    ) {
        CompositionLocalProvider(
            LocalContentColor provides textColor
        ) {
            icon()
        }
        SpacerMedium()
        Column {
            CompositionLocalProvider(
                LocalTextStyle provides AppTheme.typography.subtitle1,
                LocalContentColor provides if (titleColor == Color.Unspecified) {
                    LocalContentColor.current
                } else {
                    titleColor
                }
            ) {
                title()
            }
            CompositionLocalProvider(
                LocalTextStyle provides AppTheme.typography.body2,
                LocalContentColor provides textColor
            ) {
                info()
            }
        }
    }
}
