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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.TireRepair
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.settings.model.DebugClickActions
import de.gematik.ti.erp.app.settings.ui.preview.LocalIsPreviewMode
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.LabelButton
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.extensions.sectionPadding

@Composable
fun DebugSection(
    debugClickActions: DebugClickActions
) {
    Column {
        Text(
            text = "Debug settings",
            style = AppTheme.typography.h6,
            modifier = Modifier.sectionPadding()
        )
        LabelButton(
            Icons.Outlined.TireRepair,
            "Debug section",
            modifier = Modifier.testTag("debug-section")
        ) {
            debugClickActions.onClickDebug()
        }
        LabelButton(
            icon = Icons.Outlined.Translate,
            text = "Offline Translation"
        ) {
            debugClickActions.onClickTranslation()
        }
        LabelButton(
            Icons.Outlined.AllInclusive,
            "Bottom sheet showcase"
        ) {
            debugClickActions.onClickBottomSheetShowcase()
        }

        Text(
            text = "Local Tracking",
            style = AppTheme.typography.h6,
            modifier = Modifier.sectionPadding()
        )
        LabelButton(
            Icons.Outlined.TrackChanges,
            "Tracking Debug",
            modifier = Modifier.testTag("tracking-debug")
        ) {
            debugClickActions.onClickDemoTracking()
        }
    }
}

@Composable
fun DebugMenuSection(onClickDebug: () -> Unit) {
    if (!LocalIsPreviewMode.current) {
        OutlinedDebugButton(
            text = stringResource(id = R.string.debug_menu),
            onClick = { onClickDebug() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium,
                    bottom = PaddingDefaults.Small,
                    top = PaddingDefaults.Medium
                )
                .testTag(TestTag.Settings.DebugMenuButton)
        )
    }
}
