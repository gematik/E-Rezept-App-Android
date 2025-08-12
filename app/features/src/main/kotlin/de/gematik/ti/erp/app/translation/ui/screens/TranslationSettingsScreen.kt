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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.digas.ui.model.ErrorScreenDataWithRetry
import de.gematik.ti.erp.app.error.ErrorScreenComponent
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.translation.domain.model.DownloadedLanguage
import de.gematik.ti.erp.app.translation.navigation.TranslationRoutes
import de.gematik.ti.erp.app.translation.presentation.rememberTranslationSettingsController
import de.gematik.ti.erp.app.translation.ui.components.ConfirmLanguageDownloadDialog
import de.gematik.ti.erp.app.translation.ui.components.DeleteLanguageModelDialog
import de.gematik.ti.erp.app.translation.ui.components.DownloadedLanguageItem
import de.gematik.ti.erp.app.translation.ui.components.TranslationConsentSection
import de.gematik.ti.erp.app.translation.ui.components.TranslationSettingsDropDownMenu
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isEmptyState

class TranslationSettingsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val dialog = LocalDialog.current
        val controller = rememberTranslationSettingsController()

        val deleteLanguageModelEvent = ComposableEvent<DownloadedLanguage>()
        val confirmDownloadEvent = ComposableEvent<String>()
        val errorData = ErrorScreenDataWithRetry()

        var selectedLang by remember { mutableStateOf<String?>(null) }

        val downloadedLanguages by controller.downloadedLanguages.collectAsStateWithLifecycle()

        val isConsentGiven by controller.isConsentGiven.collectAsStateWithLifecycle(false)

        DeleteLanguageModelDialog(
            event = deleteLanguageModelEvent,
            dialogScaffold = dialog,
            onConfirmRequest = { language ->
                controller.deleteDownloadedLanguage(language)
            }
        )

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
            onBack = navController::navigateUp,
            backLabel = stringResource(R.string.back),
            closeLabel = stringResource(R.string.cancel),
            actions = {
                TranslationSettingsDropDownMenu {
                    consentBasedNavigation(isConsentGiven, navController)
                }
            },
            topBarTitle = when {
                downloadedLanguages.isEmptyState -> "Select Language"
                else -> "Downloaded Languages"
            },
            bottomBar = {
                TranslationConsentSection(isConsentGiven) {
                    navController.navigate(TranslationRoutes.TranslationConsentBottomSheetScreen.path())
                }
            }
        ) {
            UiStateMachine(
                state = downloadedLanguages,
                onEmpty = {
                    ErrorScreenComponent(
                        titleText = "Available Offline Languages",
                        bodyText = "No languages have been downloaded for offline use.",
                        tryAgainText = "Add Language",
                        refreshIcon = Icons.Rounded.Download,
                        onClickRetry = { consentBasedNavigation(isConsentGiven, navController) }
                    )
                },
                onError = {
                    ErrorScreenComponent(
                        titleText = stringResource(errorData.title),
                        bodyText = stringResource(errorData.body)
                    )
                }
            ) { state ->
                LazyColumn(
                    state = listState
                ) {
                    items(state) { downloadedLanguage ->
                        DownloadedLanguageItem(
                            language = downloadedLanguage,
                            onTargetClick = {
                                controller.updateTargetLanguage(it)
                            },
                            onDeleteClick = {
                                deleteLanguageModelEvent.trigger(downloadedLanguage)
                            }
                        )
                    }
                    if (state.all { !it.deletable }) {
                        item {
                            SpacerXXLarge()
                        }
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .systemBarsPadding(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(
                                    onClick = { consentBasedNavigation(isConsentGiven, navController) }
                                ) {
                                    Icon(Icons.Rounded.Download, null)
                                    SpacerSmall()
                                    Text("Add Language")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun consentBasedNavigation(
        isConsentGiven: Boolean,
        navController: NavController
    ) {
        if (isConsentGiven) {
            navController.navigate(TranslationRoutes.TranslationPickLanguageScreen.path())
        } else {
            navController.navigate(TranslationRoutes.TranslationConsentBottomSheetScreen.path())
        }
    }
}
