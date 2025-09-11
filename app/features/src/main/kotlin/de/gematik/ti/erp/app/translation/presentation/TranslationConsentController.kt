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

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.fastAny
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.base.ContextExtensions.getCurrentLocale
import de.gematik.ti.erp.app.translation.domain.model.DownloadedLanguage
import de.gematik.ti.erp.app.translation.domain.model.LanguageDownloadState
import de.gematik.ti.erp.app.translation.domain.model.LanguageDownloadState.NotStarted
import de.gematik.ti.erp.app.translation.domain.usecase.GetDownloadedLanguagesUseCase
import de.gematik.ti.erp.app.translation.usecase.DownloadLanguageModelUseCase
import de.gematik.ti.erp.app.translation.usecase.ToggleTranslationConsentUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class TranslationConsentController(
    application: Application,
    private val getDownloadedLanguagesUseCase: GetDownloadedLanguagesUseCase,
    private val downloadLanguageModelUseCase: DownloadLanguageModelUseCase,
    private val toggleTranslationConsentUseCase: ToggleTranslationConsentUseCase
) : AndroidViewModel(application) {

    private val _languageDownloadState = MutableStateFlow<LanguageDownloadState>(NotStarted)
    private val downloadLanguageModels = MutableStateFlow<List<DownloadedLanguage>>(emptyList())

    private val currentLocale = application.getCurrentLocale()
    private val isCurrentLanguageDownloaded = downloadLanguageModels.map { models -> models.fastAny { it.code == currentLocale } }

    val onConsentEvent = ComposableEvent<Unit>()
    val onDownloadFailedEvent = ComposableEvent<String>()
    val languageDownloadState = _languageDownloadState.asStateFlow()

    fun toggleTranslationConsent(consentGiven: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            toggleTranslationConsentUseCase(consentGiven)
            if (consentGiven && isCurrentLanguageDownloaded.firstOrNull() == false) {
                downloadLanguageModel(currentLocale)
            } else {
                onConsentEvent.trigger()
            }
        }
    }

    private suspend fun fetchDownloadedLanguages() {
        getDownloadedLanguagesUseCase()
            .collectLatest { languages ->
                downloadLanguageModels.value = languages
            }
    }

    private suspend fun downloadLanguageModel(language: String) {
        downloadLanguageModelUseCase(language).collectLatest { state ->
            _languageDownloadState.value = state

            when (state) {
                is LanguageDownloadState.Completed -> {
                    onConsentEvent.trigger()
                    fetchDownloadedLanguages()
                }

                is LanguageDownloadState.Error -> {
                    onDownloadFailedEvent.trigger(state.exception.message ?: "Unknown error")
                }

                else -> Unit // No-op for NotStarted, etc.
            }
        }
    }
}

@Composable
fun rememberTranslationConsentController(): TranslationConsentController {
    val application = LocalContext.current.applicationContext as Application
    val getDownloadedLanguagesUseCase by rememberInstance<GetDownloadedLanguagesUseCase>()
    val downloadLanguageModelUseCase by rememberInstance<DownloadLanguageModelUseCase>()
    val toggleTranslationConsentUseCase by rememberInstance<ToggleTranslationConsentUseCase>()

    return remember {
        TranslationConsentController(
            application = application,
            getDownloadedLanguagesUseCase = getDownloadedLanguagesUseCase,
            downloadLanguageModelUseCase = downloadLanguageModelUseCase,
            toggleTranslationConsentUseCase = toggleTranslationConsentUseCase
        )
    }
}
