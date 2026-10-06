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

package de.gematik.ti.erp.app.message

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageEventCard(
    timestamp: String,
    title: String,
    description: String?,
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    titleContentDescription: String? = null,
    medicationNames: List<String> = emptyList(),
    revealMedicationNamesOnLongPress: Boolean = false,
    medicationLabel: String? = null,
    onLongPressLabel: String? = null,
    actionContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    val names = medicationNames.filter(String::isNotBlank)
    val canRevealMedicationNames = revealMedicationNamesOnLongPress && names.isNotEmpty()
    var showMedicationNames by remember(timestamp, title) { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium)
            .then(
                if (canRevealMedicationNames) {
                    Modifier.combinedClickable(
                        onClick = {},
                        onClickLabel = null,
                        onLongClickLabel = onLongPressLabel,
                        role = Role.Button,
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMedicationNames = !showMedicationNames
                        }
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(SizeDefaults.double),
        border = BorderStroke(SizeDefaults.sixteenth, AppTheme.colors.neutral200),
        elevation = SizeDefaults.quarter,
        backgroundColor = AppTheme.colors.neutral000
    ) {
        Column(
            modifier = Modifier.padding(PaddingDefaults.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(SizeDefaults.fourfold)
                    .background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SizeDefaults.doubleHalf),
                    tint = iconTint
                )
            }
            SpacerMedium()
            Text(
                text = timestamp,
                style = AppTheme.typography.subtitle2,
                color = AppTheme.colors.neutral700
            )
            Text(
                modifier = if (titleContentDescription != null) {
                    Modifier.semantics { contentDescription = titleContentDescription }
                } else {
                    Modifier
                },
                text = title,
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.neutral900
            )
            description?.let {
                Text(
                    modifier = Modifier.padding(top = PaddingDefaults.Tiny),
                    text = it,
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.neutral700
                )
            }
            if (showMedicationNames) {
                Text(
                    modifier = Modifier
                        .padding(top = PaddingDefaults.Tiny)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    text = listOfNotNull(medicationLabel, names.joinToString()).joinToString(": "),
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.neutral900
                )
            }
            actionContent?.let {
                Divider(
                    modifier = Modifier.padding(vertical = PaddingDefaults.Medium),
                    color = AppTheme.colors.neutral200
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    content = it
                )
            }
        }
    }
}
