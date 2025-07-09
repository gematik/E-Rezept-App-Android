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

package de.gematik.ti.erp.app.translation.domain.usecase

import de.gematik.ti.erp.app.translation.domain.model.DownloadedLanguage
import de.gematik.ti.erp.app.translation.domain.model.getDisplayLanguage
import de.gematik.ti.erp.app.translation.repository.TranslationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GetDownloadedLanguagesUseCase(
    private val repository: TranslationRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val debounceTimeout: Long = 50L
) {
    // English ("en") is preinstalled on almost all Android devices and cannot be deleted.
    private val defaultPreinstalledModels = setOf("en")

    @OptIn(FlowPreview::class)
    suspend operator fun invoke(): Flow<List<DownloadedLanguage>> =
        withContext(dispatcher) {
            repository.getTargetLanguageTag()
                .debounce(debounceTimeout)
                .map { targetLanguage ->
                    repository.getDownloadedTranslationModels()
                        .map {
                            DownloadedLanguage(
                                code = it,
                                displayName = it.getDisplayLanguage(),
                                isTarget = it == targetLanguage,
                                deletable = !defaultPreinstalledModels.contains(it)
                            )
                        }
                }
        }
}
