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

package de.gematik.ti.erp.app.translation.di

import com.google.mlkit.common.model.RemoteModelManager
import de.gematik.ti.erp.app.translation.domain.usecase.GetDownloadedLanguagesUseCase
import de.gematik.ti.erp.app.translation.repository.DefaultTranslationRepository
import de.gematik.ti.erp.app.translation.repository.TranslationRepository
import de.gematik.ti.erp.app.translation.repository.datasource.local.TranslationLocalDataSource
import de.gematik.ti.erp.app.translation.repository.datasource.remote.TranslationRemoteDataSource
import de.gematik.ti.erp.app.translation.usecase.DeleteDownloadedLanguageUseCase
import de.gematik.ti.erp.app.translation.usecase.DownloadLanguageModelUseCase
import de.gematik.ti.erp.app.translation.usecase.GetTranslatableLanguagesUseCase
import de.gematik.ti.erp.app.translation.usecase.GetTranslationConsentUseCase
import de.gematik.ti.erp.app.translation.usecase.IsTargetLanguageSetUseCase
import de.gematik.ti.erp.app.translation.usecase.ToggleTranslationConsentUseCase
import de.gematik.ti.erp.app.translation.usecase.TranslateTextUseCase
import de.gematik.ti.erp.app.translation.usecase.UpdateTargetLanguageUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val textTranslatorModule = DI.Module("textTranslatorModule", allowSilentOverride = true) {
    bindProvider { RemoteModelManager.getInstance() }
    bindProvider { TranslationRemoteDataSource(instance(), instance()) }
    bindProvider { TranslationLocalDataSource(instance(), instance()) }
    bindProvider<TranslationRepository> { DefaultTranslationRepository(instance(), instance(), instance()) }
    bindProvider { DeleteDownloadedLanguageUseCase(instance()) }
    bindProvider { DownloadLanguageModelUseCase(instance()) }
    bindProvider { GetDownloadedLanguagesUseCase(instance()) }
    bindProvider { GetTranslatableLanguagesUseCase(instance()) }
    bindProvider { GetTranslationConsentUseCase(instance()) }
    bindProvider { ToggleTranslationConsentUseCase(instance()) }
    bindProvider { TranslateTextUseCase(instance(), instance(), instance()) }
    bindProvider { UpdateTargetLanguageUseCase(instance()) }
    bindProvider { IsTargetLanguageSetUseCase(instance()) }
}
