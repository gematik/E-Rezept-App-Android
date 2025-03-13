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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
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
import de.gematik.ti.erp.app.settings.model.PersonalSettingsClickActions
import de.gematik.ti.erp.app.settings.presentation.SettingStatesData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.LabelButton
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.extensions.sectionPadding

@Suppress("LongParameterList", "FunctionNaming")
@Composable
fun PersonalSettingsSection(
    zoomState: State<SettingStatesData.ZoomState>,
    screenShotState: State<Boolean>,
    isMedicationPlanEnabled: Boolean,
    personalSettingsClickActions: PersonalSettingsClickActions
) {
    Column {
        Text(
            text = stringResource(R.string.settings_personal_settings_header),
            style = AppTheme.typography.h6,
            modifier = Modifier.sectionPadding()
        )
        if (isMedicationPlanEnabled) {
            LabelButton(
                Icons.Outlined.Medication,
                stringResource(R.string.medication_plan_settings_title)
            ) {
                personalSettingsClickActions.onClickMedicationPlan()
            }
        }
        LabelButton(icon = Icons.Rounded.Language, text = stringResource(R.string.settings_language_label)) {
            personalSettingsClickActions.onClickLanguageSettings()
        }
        LabelButton(
            Icons.Outlined.Timeline,
            stringResource(R.string.settings_product_improvement_header)
        ) {
            personalSettingsClickActions.onClickProductImprovementSettings()
        }
        LabelButton(
            Icons.Outlined.Security,
            stringResource(R.string.settings_app_security_header)
        ) {
            personalSettingsClickActions.onClickDeviceSecuritySettings()
        }
        ZoomSwitch(zoomChecked = zoomState.value.zoomEnabled) { zoomEnabled ->
            personalSettingsClickActions.onToggleEnableZoom(zoomEnabled)
        }
        AllowScreenShotsSwitch(
            screenShotsAllowed = screenShotState.value,
            onToggleScreenshots = personalSettingsClickActions.onToggleScreenshots
        )
    }
}

@Composable
private fun ZoomSwitch(
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
private fun AllowScreenShotsSwitch(
    screenShotsAllowed: Boolean,
    onToggleScreenshots: (Boolean) -> Unit
) {
    LabeledSwitch(
        checked = screenShotsAllowed,
        onCheckedChange = { checked ->
            onToggleScreenshots(checked)
        },
        icon = Icons.Rounded.Camera,
        header = stringResource(R.string.settings_screenshots_toggle_text)
    )
}
