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

import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import kotlinx.coroutines.tasks.await

/**
 * ML Kit–backed implementation of [TranslationModelManager].
 * Lives in [core] alongside the other ML Kit data sources so that [core] is the single
 * module that carries the ML Kit dependency; no other module needs it.
 */
class DefaultTranslationModelManager(
    private val remoteModelManager: RemoteModelManager
) : TranslationModelManager {

    override suspend fun isModelDownloaded(languageTag: String): Boolean {
        val model = TranslateRemoteModel.Builder(languageTag).build()
        return remoteModelManager.isModelDownloaded(model).await()
    }
}
