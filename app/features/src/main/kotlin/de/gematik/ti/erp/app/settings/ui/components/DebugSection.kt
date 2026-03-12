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

package de.gematik.ti.erp.app.settings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.TireRepair
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import de.gematik.ti.erp.app.listitem.GemListItemDefaults
import de.gematik.ti.erp.app.settings.model.DebugClickActions
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.LabelButton
import de.gematik.ti.erp.app.utils.extensions.sectionPadding

@Composable
fun DebugSection(
    debugClickActions: DebugClickActions
) {
    var hasSeenDeveloperMode by rememberSaveable { mutableStateOf(false) }

    Column {
        Text(
            text = "Nerd control room",
            style = AppTheme.typography.h6,
            modifier = Modifier.sectionPadding()
        )
        LabelButtonWithNewBadge(
            icon = Icons.Outlined.TireRepair,
            text = "Secret switches",
            showNewBadge = !hasSeenDeveloperMode,
            modifier = Modifier.testTag("debug-section")
        ) {
            hasSeenDeveloperMode = true
            debugClickActions.onClickDebug()
        }
        LabelButton(
            icon = Icons.Outlined.Translate,
            text = "Pocket translator (offline)"
        ) {
            debugClickActions.onClickTranslation()
        }
        LabelButton(
            Icons.Outlined.AllInclusive,
            "Bottom-sheet playground"
        ) {
            debugClickActions.onClickBottomSheetShowcase()
        }

        Text(
            text = "Local spyglass",
            style = AppTheme.typography.h6,
            modifier = Modifier.sectionPadding()
        )
        LabelButton(
            Icons.Outlined.TrackChanges,
            "Trace lab",
            modifier = Modifier.testTag("tracking-debug")
        ) {
            debugClickActions.onClickDemoTracking()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LabelButtonWithNewBadge(
    icon: ImageVector,
    text: String,
    showNewBadge: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String = text,
    onClick: () -> Unit
) {
    val iconColorTint = AppTheme.colors.primary700
    val textColor = AppTheme.colors.neutral900
    val descriptionColor = AppTheme.colors.neutral700

    ListItem(
        colors = GemListItemDefaults.gemListItemColors(
            leadingIconColor = iconColorTint,
            headlineColor = textColor,
            supportingColor = descriptionColor
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        headlineContent = {
            Text(
                text = text,
                style = AppTheme.typography.body1,
                color = textColor
            )
        },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SizeDefaults.one),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showNewBadge) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = AppTheme.colors.yellow500,
                                shape = RoundedCornerShape(SizeDefaults.oneHalf)
                            )
                            .padding(horizontal = SizeDefaults.one, vertical = SizeDefaults.quarter)
                    ) {
                        Text(
                            text = "for devs and testers",
                            style = AppTheme.typography.body2,
                            color = AppTheme.colors.neutral999
                        )
                    }
                }
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    null,
                    tint = AppTheme.colors.neutral600
                )
            }
        },
        leadingContent = {
            Icon(icon, null, tint = iconColorTint)
        }
    )
}
