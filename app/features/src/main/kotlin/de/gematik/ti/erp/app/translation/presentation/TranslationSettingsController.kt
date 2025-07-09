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

package de.gematik.ti.erp.app.translation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.translation.domain.model.DownloadedLanguage
import de.gematik.ti.erp.app.translation.domain.model.LanguageDownloadState
import de.gematik.ti.erp.app.translation.domain.model.LanguageDownloadState.NotStarted
import de.gematik.ti.erp.app.translation.domain.model.getDisplayLanguage
import de.gematik.ti.erp.app.translation.domain.usecase.GetDownloadedLanguagesUseCase
import de.gematik.ti.erp.app.translation.usecase.DeleteDownloadedLanguageUseCase
import de.gematik.ti.erp.app.translation.usecase.DownloadLanguageModelUseCase
import de.gematik.ti.erp.app.translation.usecase.GetTranslatableLanguagesUseCase
import de.gematik.ti.erp.app.translation.usecase.GetTranslationConsentUseCase
import de.gematik.ti.erp.app.translation.usecase.UpdateTargetLanguageUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class TranslationSettingsController(
    getTranslationConsentUseCase: GetTranslationConsentUseCase,
    private val getTranslatableLanguagesUseCase: GetTranslatableLanguagesUseCase,
    private val getDownloadedLanguagesUseCase: GetDownloadedLanguagesUseCase,
    private val downloadLanguageModelUseCase: DownloadLanguageModelUseCase,
    private val deleteDownloadedLanguageUseCase: DeleteDownloadedLanguageUseCase,
    private val updateTargetLanguageUseCase: UpdateTargetLanguageUseCase
) : Controller() {

    init {
        startPollingDownloadedLanguages()
    }

    private val _downloadLanguageModel = MutableStateFlow<UiState<List<DownloadedLanguage>>>(UiState.Empty())
    val downloadedLanguages = _downloadLanguageModel.asStateFlow()

    private val _languageDownloadState = MutableStateFlow<LanguageDownloadState>(NotStarted)
    val languageDownloadState = _languageDownloadState.asStateFlow()

    val isConsentGiven = getTranslationConsentUseCase()

    private fun fetchDownloadedLanguages() {
        controllerScope.launch {
            getDownloadedLanguagesUseCase.invoke()
                .collectLatest { languages ->
                    _downloadLanguageModel.value = UiState.Data(languages)
                }
        }
    }

    val translatableLanguages: List<Pair<String, String>> by lazy {
        getTranslatableLanguagesUseCase
            .invoke()
            .map { tag ->
                tag to tag.getDisplayLanguage()
            }.sortedBy { it.second }
    }

    fun downloadLanguageModel(language: String) {
        controllerScope.launch {
            downloadLanguageModelUseCase.invoke(language)
                .collectLatest { state ->
                    if (state is LanguageDownloadState.Completed) {
                        fetchDownloadedLanguages()
                    }

                    _languageDownloadState.value = state
                }
        }
    }

    fun deleteDownloadedLanguage(language: DownloadedLanguage) {
        controllerScope.launch {
            val result = deleteDownloadedLanguageUseCase.invoke(language.code)
            if (result) {
                fetchDownloadedLanguages()
            }
        }
    }

    fun updateTargetLanguage(language: DownloadedLanguage) {
        controllerScope.launch {
            updateTargetLanguageUseCase.invoke(language.code)
            fetchDownloadedLanguages()
        }
    }

    private fun startPollingDownloadedLanguages(intervalMs: Long = 10_000) {
        controllerScope.launch {
            while (true) {
                fetchDownloadedLanguages()
                delay(intervalMs)
            }
        }
    }
}

@Composable
fun rememberTranslationSettingsController(): TranslationSettingsController {
    val getTranslatableLanguagesUseCase by rememberInstance<GetTranslatableLanguagesUseCase>()
    val getDownloadedLanguagesUseCase by rememberInstance<GetDownloadedLanguagesUseCase>()
    val downloadLanguageModelUseCase by rememberInstance<DownloadLanguageModelUseCase>()
    val deleteDownloadedLanguageUseCase by rememberInstance<DeleteDownloadedLanguageUseCase>()
    val updateTargetLanguageUseCase by rememberInstance<UpdateTargetLanguageUseCase>()
    val getTranslationConsentUseCase by rememberInstance<GetTranslationConsentUseCase>()

    return remember {
        TranslationSettingsController(
            getTranslatableLanguagesUseCase = getTranslatableLanguagesUseCase,
            getDownloadedLanguagesUseCase = getDownloadedLanguagesUseCase,
            deleteDownloadedLanguageUseCase = deleteDownloadedLanguageUseCase,
            downloadLanguageModelUseCase = downloadLanguageModelUseCase,
            updateTargetLanguageUseCase = updateTargetLanguageUseCase,
            getTranslationConsentUseCase = getTranslationConsentUseCase
        )
    }
}
