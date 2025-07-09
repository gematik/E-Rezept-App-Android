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

package de.gematik.ti.erp.app.translation.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.loading.LoadingIndicator
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.translation.domain.model.LanguageDownloadState
import de.gematik.ti.erp.app.translation.presentation.rememberTranslationSettingsController
import de.gematik.ti.erp.app.translation.ui.components.ConfirmLanguageDownloadDialog
import de.gematik.ti.erp.app.translation.ui.components.LanguagePickerList
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.showWithDismissButton

class TranslationPickLanguageScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val dialog = LocalDialog.current
        val snackbarScaffold = LocalSnackbarScaffold.current
        val uiScope = uiScope
        val controller = rememberTranslationSettingsController()

        var selectedLang by remember { mutableStateOf<String?>(null) }
        val confirmDownloadEvent = ComposableEvent<String>()

        val translatableLanguages = controller.translatableLanguages
        val languageDownloadState by controller.languageDownloadState.collectAsStateWithLifecycle()

        LaunchedEffect(languageDownloadState) {
            if (languageDownloadState is LanguageDownloadState.Completed) {
                snackbarScaffold.showWithDismissButton(
                    message = "Language model downloaded successfully",
                    actionLabel = "OK",
                    scope = uiScope,
                    action = navController::navigateUp
                )
            }
        }

        ConfirmLanguageDownloadDialog(
            event = confirmDownloadEvent,
            dialogScaffold = dialog,
            onConfirmRequest = { confirmedLanguage ->
                controller.downloadLanguageModel(confirmedLanguage)
            },
            onDismissRequest = {
                selectedLang = null
            }
        )

        AnimatedElevationScaffold(
            listState = listState,
            topBarTitle = "Select Language",
            actions = {
            },
            onBack = navController::navigateUp
        ) {
            if (languageDownloadState is LanguageDownloadState.Downloading) {
                LoadingIndicator()
            }
            LanguagePickerList(
                languages = translatableLanguages,
                listState = listState,
                selectedLanguageCode = selectedLang,
                onLanguageSelected = { language ->
                    selectedLang = language
                    confirmDownloadEvent.trigger(language)
                }
            )
        }
    }
}
