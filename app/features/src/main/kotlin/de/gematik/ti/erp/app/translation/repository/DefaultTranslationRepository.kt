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

package de.gematik.ti.erp.app.translation.repository

import android.content.Context
import de.gematik.ti.erp.app.base.ContextExtensions.getCurrentLocale
import de.gematik.ti.erp.app.translation.domain.model.LanguageDownloadState
import de.gematik.ti.erp.app.translation.repository.datasource.local.TranslationLocalDataSource
import de.gematik.ti.erp.app.translation.repository.datasource.remote.TranslationRemoteDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of [TranslationRepository] that manages automatic text translation consent
 * and language model lifecycle by delegating to local and remote data sources.
 *
 * This repository handles user consent specifically for offline ML Kit translations, ensures that
 * models are downloaded only when allowed, and provides access to supported languages and
 * downloaded models.
 *
 * @property localDataSource Responsible for storing user consent and managing translation models
 *                            locally (e.g., list, delete, check if downloaded).
 * @property remoteDataSource Handles downloading of ML Kit translation models, providing progress updates.
 */
class DefaultTranslationRepository(
    private val context: Context,
    private val localDataSource: TranslationLocalDataSource,
    private val remoteDataSource: TranslationRemoteDataSource
) : TranslationRepository {

    /**
     * Enables user consent for local translation and persists it via [TranslationLocalDataSource].
     */
    override suspend fun enableConsentForLocalTranslation() {
        localDataSource.setTargetLanguageTag(context.getCurrentLocale())
        localDataSource.setConsent(true)
    }

    /**
     * Disables user consent for local translation and removes all previously downloaded
     * translation models from the device to ensure no offline processing is retained.
     */
    override suspend fun disableConsentForLocalTranslation() {
        localDataSource.clearConsent()
        localDataSource.deleteAllTranslationModels()
    }

    /**
     * Checks whether the user has granted consent for performing local translation on the device.
     *
     * @return A [Flow] that emits `true` if consent is granted, otherwise `false`.
     */
    override fun isTranslationAllowed(): Flow<Boolean> =
        localDataSource.isConsentGiven()

    /**
     * Retrieves a list of all language codes supported by ML Kit for translation.
     *
     * @return A list of BCP-47 language tags (e.g., "en", "de", "fr").
     */
    override fun getPossibleLanguageList(): List<String> =
        localDataSource.getLanguageList()

    /**
     * Gets a list of downloaded translation models currently stored on the device.
     *
     * @return A list of language codes representing the installed models.
     */
    override suspend fun getDownloadedTranslationModels(): List<String> =
        remoteDataSource.getDownloadedTranslationModels()

    /**
     * Deletes a specific downloaded translation model from local storage.
     *
     * @param languageTag The BCP-47 language tag of the model to delete (e.g., "fr").
     * @return True if the model was successfully deleted, false otherwise.
     */
    override suspend fun deleteTranslationModel(languageTag: String): Boolean {
        return localDataSource.deleteTranslationModel(languageTag)
    }

    /**
     * Initiates downloading of the translation model for the specified target language.
     * Emits progress and completion status via a [Flow].
     *
     * @param targetLanguage The BCP-47 language tag of the desired translation target (e.g., "en").
     * @return A [Flow] emitting download progress or status.
     */
    override fun downloadLanguageModels(targetLanguage: String): Flow<LanguageDownloadState> =
        remoteDataSource.downloadLanguageModels(targetLanguage)

    override suspend fun setTargetLanguageTag(tag: String) {
        localDataSource.setTargetLanguageTag(tag)
    }

    override fun getTargetLanguageTag(): Flow<String?> =
        localDataSource.getTargetLanguage()

    override suspend fun clearTargetLanguageTag() {
        localDataSource.clearTargetLanguageTag()
    }
}
