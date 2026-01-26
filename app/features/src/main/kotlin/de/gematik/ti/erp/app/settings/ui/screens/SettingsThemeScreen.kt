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

package de.gematik.ti.erp.app.settings.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Divider
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.settings.model.ThemeMode
import de.gematik.ti.erp.app.settings.presentation.rememberSettingsThemeScreenController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

/**
 * Settings screen for selecting the app theme (Light, Dark, or System default).
 */
class SettingsThemeScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(withCloseButton = true) {
    @Composable
    override fun Content() {
        val controller = rememberSettingsThemeScreenController()
        val selectedTheme by controller.selectedTheme.collectAsStateWithLifecycle()
        val lazyListState = rememberLazyListState()

        AnimatedElevationScaffold(
            modifier = Modifier.navigationBarsPadding(),
            backLabel = stringResource(R.string.back),
            closeLabel = stringResource(R.string.cancel),
            navigationMode = NavigationBarMode.Back,
            topBarTitle = stringResource(R.string.settings_theme_title),
            onBack = navController::popBackStack,
            listState = lazyListState
        ) {
            SettingsThemeScreenContent(
                lazyListState = lazyListState,
                selectedTheme = selectedTheme,
                onThemeSelected = controller::onThemeSelected
            )
        }
    }
}

@Composable
private fun SettingsThemeScreenContent(
    lazyListState: LazyListState,
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .wrapContentSize()
            .testTag(TestTag.Settings.ThemeColumnList)
    ) {
        item {
            SpacerMedium()
        }

        item {
            ThemeSelectionItem(
                theme = ThemeMode.SYSTEM,
                themeName = stringResource(R.string.settings_theme_system),
                checked = selectedTheme == ThemeMode.SYSTEM,
                onCheckedChange = {
                    onThemeSelected(ThemeMode.SYSTEM)
                }
            )
        }

        item {
            Divider()
        }

        item {
            ThemeSelectionItem(
                theme = ThemeMode.LIGHT,
                themeName = stringResource(R.string.settings_theme_light),
                checked = selectedTheme == ThemeMode.LIGHT,
                onCheckedChange = {
                    onThemeSelected(ThemeMode.LIGHT)
                }
            )
        }

        item {
            Divider()
        }

        item {
            ThemeSelectionItem(
                theme = ThemeMode.DARK,
                themeName = stringResource(R.string.settings_theme_dark),
                checked = selectedTheme == ThemeMode.DARK,
                onCheckedChange = {
                    onThemeSelected(ThemeMode.DARK)
                }
            )
        }

        item {
            Divider()
        }
    }
}

@Composable
private fun ThemeSelectionItem(
    theme: ThemeMode,
    themeName: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = checked,
                    onClick = onCheckedChange,
                    role = Role.RadioButton
                )
                .padding(
                    horizontal = PaddingDefaults.Medium,
                    vertical = PaddingDefaults.Medium
                )
                .testTag("${TestTag.Settings.ThemeItem}_${theme.name}")
        ) {
            RadioButton(
                selected = checked,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AppTheme.colors.primary600,
                    unselectedColor = AppTheme.colors.neutral400
                )
            )
            Text(
                text = themeName,
                style = AppTheme.typography.body1,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@LightDarkPreview
@Composable
internal fun SettingsThemeScreenPreview() {
    PreviewAppTheme {
        val lazyListState = rememberLazyListState()
        AnimatedElevationScaffold(
            modifier = Modifier.navigationBarsPadding(),
            listState = lazyListState,
            navigationMode = NavigationBarMode.Back,
            backLabel = "Back",
            closeLabel = "Cancel",
            topBarTitle = "Theme",
            onBack = {}
        ) {
            SettingsThemeScreenContent(
                lazyListState = lazyListState,
                selectedTheme = ThemeMode.LIGHT,
                onThemeSelected = {}
            )
        }
    }
}
