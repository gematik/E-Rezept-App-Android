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

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Divider
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.listitem.GemListItemDefaults
import de.gematik.ti.erp.app.localization.LanguageCode
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.settings.presentation.rememberSettingsLanguageScreenController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.settings.ui.preview.SettingsLanguageScreenPreviewData
import de.gematik.ti.erp.app.settings.ui.preview.SettingsLanguageScreenPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.preview.TestScaffold
import java.util.Locale

class SettingsLanguageScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val controller = rememberSettingsLanguageScreenController()
        val languages = controller.languageList
        val lazyListState = rememberLazyListState()
        val selectedAppLocale = AppCompatDelegate.getApplicationLocales()

        val selectedLanguage = remember() {
            selectedAppLocale.toLanguageTags().ifEmpty {
                Locale.getDefault().language
            }
        }

        AnimatedElevationScaffold(
            modifier = Modifier.navigationBarsPadding(),
            backLabel = stringResource(R.string.back),
            closeLabel = stringResource(R.string.cancel),
            navigationMode = NavigationBarMode.Back,
            topBarTitle = stringResource(R.string.language_selection_title),
            onBack = navController::popBackStack,
            listState = lazyListState
        ) {
            SettingsLanguageScreenContent(
                lazyListState = lazyListState,
                languages = languages,
                selectedLanguage = selectedLanguage
            )
        }
    }
}

@Composable
private fun SettingsLanguageScreenContent(
    lazyListState: LazyListState,
    languages: List<LanguageCode>,
    selectedLanguage: String?
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .wrapContentSize()
            .testTag(TestTag.Settings.LanguageColumnList)
    ) {
        item {
            SpacerMedium()
        }

        itemsIndexed(languages) { index, languageCode ->
            LanguageSelectionItem(
                language = languageCode.mapToName(),
                showDivider = index == 0,
                checked = languageCode.code == selectedLanguage ||
                    (languageCode.code == "iw" && selectedLanguage == "he"),
                onCheckedChange = { checked ->
                    if (checked) {
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(
                                languageCode.code
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun LanguageSelectionItem(
    language: String,
    enabled: Boolean = true,
    showDivider: Boolean = false,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column {
        ListItem(
            colors = GemListItemDefaults.gemListItemColors(),
            modifier = Modifier
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    role = Role.RadioButton,
                    enabled = enabled
                ),
            headlineContent = {
                Text(
                    text = language,
                    style = AppTheme.typography.body1
                )
            },
            leadingContent = {
                RadioButton(
                    selected = checked,
                    enabled = enabled,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AppTheme.colors.primary700,
                        unselectedColor = AppTheme.colors.primary700
                    ),
                    onClick = null
                )
            }
        )
        if (showDivider) {
            Divider(
                color = AppTheme.colors.neutral300,
                modifier = Modifier
                    .padding(PaddingDefaults.Medium)
            )
        }
    }
}

@LightDarkPreview
@Composable
fun SettingsLanguageScreenScaffoldPreview(
    @PreviewParameter(SettingsLanguageScreenPreviewParameterProvider::class) previewData: SettingsLanguageScreenPreviewData
) {
    PreviewAppTheme {
        val listState = rememberLazyListState()

        TestScaffold(
            topBarTitle = stringResource(R.string.language_selection_title),
            navigationMode = NavigationBarMode.Back
        ) {
            SettingsLanguageScreenContent(
                lazyListState = listState,
                languages = LanguageCode.entries,
                selectedLanguage = previewData.selectedLanguage
            )
        }
    }
}
