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

package de.gematik.ti.erp.app.translation.repository.datasource.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import de.gematik.ti.erp.app.translation.domain.model.TRANSLATION_TAG
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private val Context.dataStore by preferencesDataStore("translationPrefs")

class TranslationLocalDataSource(
    context: Context,
    private val remoteModelManager: RemoteModelManager
) {

    private val dataStore = context.dataStore

    private val consentKey = booleanPreferencesKey("auto_translation_consent")
    private val targetLanguageKey = stringPreferencesKey("target_language_tag")

    fun sourceLanguageTag(): String {
        return TranslateLanguage.fromLanguageTag("de") ?: "de"
    }

    fun isConsentGiven(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[consentKey] ?: false }

    suspend fun setConsent(enabled: Boolean) {
        Napier.i(tag = TRANSLATION_TAG) { "setting consent for translation language as $enabled" }
        dataStore.edit { prefs ->
            prefs[consentKey] = enabled
        }
    }

    suspend fun clearConsent() {
        dataStore.edit { prefs ->
            prefs.remove(consentKey)
        }
    }

    fun getLanguageList(): List<String> = TranslateLanguage.getAllLanguages().toList()

    private suspend fun getLocalDownloadedTranslationModels(): List<String> {
        val downloadedModels = remoteModelManager
            .getDownloadedModels(TranslateRemoteModel::class.java)
            .await()

        return downloadedModels
            .filter { it.language.isNotNullOrEmpty() }
            .mapNotNull { it.language }
    }

    suspend fun setTargetLanguageTag(tag: String) {
        Napier.i(tag = TRANSLATION_TAG) { "setting target translation language as $tag" }
        dataStore.edit { prefs ->
            prefs[targetLanguageKey] = tag
        }
    }

    fun getTargetLanguage(): Flow<String?> {
        return dataStore.data.map { it[targetLanguageKey] }
    }

    suspend fun clearTargetLanguageTag() {
        dataStore.edit { prefs ->
            prefs.remove(targetLanguageKey)
        }
    }

    suspend fun deleteTranslationModel(languageTag: String): Boolean {
        val langCode = TranslateLanguage.fromLanguageTag(languageTag) ?: return false
        val model = TranslateRemoteModel.Builder(langCode).build()
        return try {
            remoteModelManager.deleteDownloadedModel(model).await()
            val models = getLocalDownloadedTranslationModels()
            if (models.size <= 1) {
                clearTargetLanguageTag()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteAllTranslationModels(): Boolean {
        val downloadedLangTags = getLocalDownloadedTranslationModels()

        var allDeleted = true
        for (langTag in downloadedLangTags) {
            val success = deleteTranslationModel(langTag)
            if (!success) {
                allDeleted = false
            }
        }

        if (allDeleted) {
            clearTargetLanguageTag()
        }

        return allDeleted
    }
}
