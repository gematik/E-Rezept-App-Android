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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Label(
    modifier: Modifier = Modifier,
    text: String?,
    label: String? = null,
    onClick: (() -> Unit)? = null,
    setHorizontalPadding: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    val verticalPadding = if (label != null) PaddingDefaults.ShortMedium else PaddingDefaults.Medium

    val horizontalPadding = if (setHorizontalPadding) PaddingDefaults.Medium else SizeDefaults.zero
    val noValueText = stringResource(R.string.pres_details_no_value)

    val rowModifier = modifier.then(
        if (onClick != null) {
            Modifier.combinedClickable(
                onClick = {
                    onClick.invoke()
                },
                onLongClick = {
                    if (text.isNotNullOrEmpty() && text != null) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        clipboardManager.setText(AnnotatedString(text))
                    }
                },
                role = Role.Button
            )
        } else {
            Modifier.semantics(mergeDescendants = true) {}
        }
    )

    Row(
        modifier = rowModifier
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = text ?: noValueText,
                style = AppTheme.typography.body1
            )
            if (label != null) {
                Text(
                    text = label,
                    style = AppTheme.typography.body2l
                )
            }
        }
        if (onClick != null) {
            SpacerMedium()
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
        }
    }
}

private fun String?.isNotNullOrEmpty() = !isNullOrEmpty() && this != ""
