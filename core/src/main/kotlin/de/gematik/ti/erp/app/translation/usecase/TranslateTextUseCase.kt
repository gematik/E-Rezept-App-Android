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

package de.gematik.ti.erp.app.translation.usecase

import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import de.gematik.ti.erp.app.translation.domain.model.TRANSLATION_TAG
import de.gematik.ti.erp.app.translation.domain.model.TranslationFailure
import de.gematik.ti.erp.app.translation.domain.model.TranslationState
import de.gematik.ti.erp.app.translation.repository.TranslationRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class TranslateTextUseCase(
    private val remoteModelManager: RemoteModelManager,
    private val repository: TranslationRepository
) {

    private fun mapToMlKitLangCode(): String? {
        return TranslateLanguage.fromLanguageTag("de") // e.g., "de", "fr", etc.
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(text: String): Flow<TranslationState> {
        try {
            val sourceLang = mapToMlKitLangCode() ?: throw TranslationFailure.SourceLanguageNotSupported
            Napier.i(tag = TRANSLATION_TAG) { "Language translating from $sourceLang" }

            return combine(
                repository.getTargetLanguageTag(),
                repository.isTranslationAllowed()
            ) { targetLang, consent ->
                targetLang to consent
            }.flatMapLatest { (targetLang, consent) ->
                flow {
                    emit(TranslationState.Loading)

                    try {
                        if (targetLang == null || !consent) {
                            emit(TranslationState.Error(TranslationFailure.TargetLanguageNotSet))
                            return@flow
                        }
                        val model = TranslateRemoteModel.Builder(targetLang).build()
                        val isDownloaded = remoteModelManager.isModelDownloaded(model).await()

                        val options = TranslatorOptions.Builder()
                            .setSourceLanguage(sourceLang)
                            .setTargetLanguage(targetLang)
                            .build()

                        val translator = Translation.getClient(options)

                        if (isDownloaded) {
                            translator.downloadModelIfNeeded().await() // ensures model is usable
                            val translatedText = translator.translate(text).await()
                            emit(TranslationState.Success(translatedText ?: ""))
                        } else {
                            emit(TranslationState.Error(TranslationFailure.LanguageNotDownloaded))
                        }
                    } catch (e: Exception) {
                        Napier.e(tag = TRANSLATION_TAG) { "Error during translation: ${e.stackTraceToString()}" }
                        emit(TranslationState.Error(TranslationFailure.WrappedException(e)))
                    }
                }
            }
        } catch (e: Exception) {
            Napier.e(tag = TRANSLATION_TAG) { "Translation failed: ${e.stackTraceToString()}" }
            return flowOf(TranslationState.Error(TranslationFailure.WrappedException(e)))
        }
    }
}
