/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.settings.model.SettingsActions
import de.gematik.ti.erp.app.settings.presentation.SettingStatesData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.LabelButton
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch

@Suppress("LongParameterList", "FunctionNaming")
@Composable
fun GlobalSettingsSection(
    isDemoMode: Boolean,
    zoomState: State<SettingStatesData.ZoomState>,
    screenShotState: State<Boolean>,
    isMedicationPlanEnabled: Boolean,
    onEnableZoom: () -> Unit,
    onDisableZoom: () -> Unit,
    onAllowScreenshots: () -> Unit,
    onDisallowScreenshots: () -> Unit,
    settingsActions: SettingsActions,
    onClickMedicationPlan: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.settings_personal_settings_header),
            style = AppTheme.typography.h6,
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Small,
                top = PaddingDefaults.Medium
            )
        )

        if (isMedicationPlanEnabled) {
            LabelButton(
                Icons.Outlined.Medication,
                stringResource(R.string.medication_plan_settings_title)
            ) {
                onClickMedicationPlan()
            }
        }
        LabelButton(icon = Icons.Rounded.Language, text = stringResource(R.string.settings_language_label)) {
            settingsActions.onClickLanguageSettings()
        }
        LabelButton(
            Icons.Outlined.Timeline,
            stringResource(R.string.settings_product_improvement_header)
        ) {
            settingsActions.onClickProductImprovementSettings()
        }
        LabelButton(
            Icons.Outlined.Security,
            stringResource(R.string.settings_app_security_header)
        ) {
            settingsActions.onClickDeviceSecuritySettings()
        }

        ZoomSection(zoomChecked = zoomState.value.zoomEnabled) { zoomEnabled ->
            if (zoomEnabled) onEnableZoom() else onDisableZoom()
        }

        AllowScreenShotsSection(
            screenShotsAllowed = screenShotState.value,
            onAllowScreenshots = onAllowScreenshots,
            onDisallowScreenshots = onDisallowScreenshots
        )

        if (isDemoMode) {
            LabelButton(
                icon = Icons.Outlined.AutoFixHigh,
                stringResource(R.string.demo_mode_settings_end_title)
            ) {
                settingsActions.onClickDemoModeEnd()
            }
        } else {
            LabelButton(
                icon = Icons.Outlined.AutoFixHigh,
                stringResource(R.string.demo_mode_settings_title)
            ) {
                settingsActions.onClickDemoMode()
            }
        }
    }
}

@Composable
private fun ZoomSection(
    modifier: Modifier = Modifier,
    zoomChecked: Boolean,
    onZoomChange: (Boolean) -> Unit
) {
    LabeledSwitch(
        modifier = modifier,
        checked = zoomChecked,
        onCheckedChange = onZoomChange,
        icon = Icons.Rounded.ZoomIn,
        header = stringResource(R.string.settings_accessibility_zoom_toggle)
    )
}

@Composable
private fun AllowScreenShotsSection(
    screenShotsAllowed: Boolean,
    onAllowScreenshots: () -> Unit,
    onDisallowScreenshots: () -> Unit
) {
    LabeledSwitch(
        checked = screenShotsAllowed,
        onCheckedChange = { checked ->
            if (checked) onAllowScreenshots() else onDisallowScreenshots()
        },
        icon = Icons.Rounded.Camera,
        header = stringResource(R.string.settings_screenshots_toggle_text)
    )
}
