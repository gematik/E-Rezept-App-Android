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

package de.gematik.ti.erp.app.digas.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.listitem.GemListItemDefaults
import de.gematik.ti.erp.app.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun Label(
    modifier: Modifier = Modifier,
    text: String?,
    label: String? = null,
    onClick: (() -> Unit)? = null,
    imageVector: ImageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
    iconTint: Color = AppTheme.colors.neutral600,
    iconContentDescription: String? = null
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val longClickLabel = stringResource(R.string.a11y_long_press_copy)
    val noValueText = stringResource(R.string.pres_details_no_value)

    val rowModifier = modifier.then(
        if (onClick != null) {
            Modifier.clickable(
                onClick = {
                    onClick.invoke()
                },
                role = Role.Button
            )
        } else {
            Modifier.combinedClickable(
                onClick = {},
                onLongClickLabel = longClickLabel,
                onLongClick = {
                    if (!text.isNullOrEmpty()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        clipboardManager.setText(AnnotatedString(text))
                    }
                },
                role = Role.Button
            )
        }
    )

    ListItem(
        modifier = rowModifier,
        colors = GemListItemDefaults.gemListItemColors(),
        overlineContent = {
            label?.let {
                Text(
                    text = label,
                    style = AppTheme.typography.body2
                )
            }
        },
        headlineContent = {
            Text(
                text = text ?: noValueText,
                style = AppTheme.typography.body1
            )
        },
        trailingContent = {
            onClick?.let {
                Icon(imageVector, iconContentDescription, tint = iconTint)
            }
        }
    )
}
