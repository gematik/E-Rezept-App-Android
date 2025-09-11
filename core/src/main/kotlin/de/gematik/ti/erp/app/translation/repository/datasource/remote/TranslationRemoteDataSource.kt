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

package de.gematik.ti.erp.app.translation.repository.datasource.remote

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.translation.domain.model.LanguageDownloadState
import de.gematik.ti.erp.app.translation.domain.model.TRANSLATION_TAG
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class TranslationRemoteDataSource(
    private val remoteModelManager: RemoteModelManager,
    private val networkStatusTracker: NetworkStatusTracker
) {

    suspend fun getDownloadedTranslationModels(): List<String> {
        val downloadedModels = remoteModelManager
            .getDownloadedModels(TranslateRemoteModel::class.java)
            .await()

        val downloadedLanguages = downloadedModels.mapNotNull { it.language }

        Napier.d(tag = TRANSLATION_TAG) { "Downloaded languages $downloadedLanguages" }

        return downloadedLanguages
    }

    fun downloadLanguageModels(targetLangTag: String): Flow<LanguageDownloadState> {
        return flow {
            // 1) Quick pre-check
            val online = networkStatusTracker.isNetworkAvailable()
            Napier.d(tag = TRANSLATION_TAG) { "Network available: $online" }
            if (!online) {
                emit(LanguageDownloadState.Error(Exception("Kein Internet")))
                return@flow
            }

            emit(LanguageDownloadState.Downloading)

            try {
                val supportedLanguages = TranslateLanguage.getAllLanguages()

                val sourceLangTag = "de"
                val targetLangTagNormalized = targetLangTag.lowercase()
                Napier.i(tag = TRANSLATION_TAG) { "Downloading translation model for $sourceLangTag --> $targetLangTagNormalized" }

                if (sourceLangTag !in supportedLanguages || targetLangTagNormalized !in supportedLanguages) {
                    emit(LanguageDownloadState.Error(IllegalArgumentException("Unsupported language tag.")))
                    return@flow
                }

                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLangTag)
                    .setTargetLanguage(targetLangTagNormalized)
                    .build()

                val translator = Translation.getClient(options)

                val conditions = DownloadConditions.Builder().build()

                translator.downloadModelIfNeeded(conditions).await()

                val model = TranslateRemoteModel.Builder(targetLangTagNormalized).build()

                repeat(10) {
                    val downloaded = remoteModelManager.isModelDownloaded(model).await()
                    if (downloaded) {
                        Napier.d(tag = TRANSLATION_TAG) { "Model $targetLangTagNormalized confirmed downloaded." }
                        emit(LanguageDownloadState.Completed)
                        return@flow
                    }
                    delay(300) // wait and try again
                }

                emit(LanguageDownloadState.Error(Exception("Model download timed out or failed to register")))
            } catch (e: Exception) {
                emit(LanguageDownloadState.Error(e))
            }
        }
    }
}
