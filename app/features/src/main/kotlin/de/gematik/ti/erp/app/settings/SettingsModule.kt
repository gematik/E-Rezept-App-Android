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

package de.gematik.ti.erp.app.settings

import android.annotation.SuppressLint
import de.gematik.ti.erp.app.database.api.debug.Android13DeprecationLocalDataSource
import de.gematik.ti.erp.app.localization.DefaultXmlResourceParserWrapper
import de.gematik.ti.erp.app.localization.GetSupportedLanguagesFromXmlUseCase
import de.gematik.ti.erp.app.localization.XmlResourceParserWrapper
import de.gematik.ti.erp.app.debug.repository.Android13DeprecationRepository
import de.gematik.ti.erp.app.settings.repository.CardWallRepository
import de.gematik.ti.erp.app.debug.repository.DefaultAndroid13DeprecationRepository
import de.gematik.ti.erp.app.settings.repository.DefaultSettingsRepository
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import de.gematik.ti.erp.app.settings.usecase.AllowScreenshotsUseCase
import de.gematik.ti.erp.app.appsecurity.usecase.GetShouldShowAndroid13DeprecationWarningUseCase
import de.gematik.ti.erp.app.settings.usecase.GetOrganDonationRegisterHostsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetScreenShotsAllowedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetShowWelcomeDrawerUseCase
import de.gematik.ti.erp.app.settings.usecase.GetThemeModeUseCase
import de.gematik.ti.erp.app.settings.usecase.GetZoomStateUseCase
import de.gematik.ti.erp.app.settings.usecase.ResetOnboardingUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveThemeModeUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveWelcomeDrawerShownUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveZoomEnabledUseCase
import de.gematik.ti.erp.app.appsecurity.usecase.SetShouldShowAndroid13DeprecationWarningUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

const val ApplicationPreferencesTag = "ApplicationPreferences"

@SuppressLint("ObsoleteSdkInt")
val settingsModule = DI.Module("settingsModule") {
    bindProvider { GetScreenShotsAllowedUseCase(instance()) }
    bindProvider<Android13DeprecationRepository> {
        DefaultAndroid13DeprecationRepository(
            dataStore = instance<Android13DeprecationLocalDataSource>()
        )
    }
    bindProvider { GetShouldShowAndroid13DeprecationWarningUseCase(instance()) }
    bindProvider { SetShouldShowAndroid13DeprecationWarningUseCase(instance()) }
    bindProvider { AllowScreenshotsUseCase(instance()) }
    bindProvider { GetShowWelcomeDrawerUseCase(instance()) }
    bindProvider { SaveWelcomeDrawerShownUseCase(instance()) }
    bindProvider { GetZoomStateUseCase(instance()) }
    bindProvider { ResetOnboardingUseCase(instance()) }
    bindProvider { SaveZoomEnabledUseCase(instance()) }
    bindProvider { GetThemeModeUseCase(instance()) }
    bindProvider { SaveThemeModeUseCase(instance()) }
    bindProvider { GetOrganDonationRegisterHostsUseCase(instance()) }

    bindProvider {
        val context = instance<android.content.Context>()
        val resId = context.resources.getIdentifier(
            "locale_config",
            "xml",
            context.packageName
        )
        context.resources.getXml(resId)
    }
    bindProvider<XmlResourceParserWrapper> { DefaultXmlResourceParserWrapper(instance()) }
    bindProvider { GetSupportedLanguagesFromXmlUseCase(instance(), instance()) }
}

val settingsRepositoryModule = DI.Module("settingsRepositoryModule") {
    bindProvider { CardWallRepository(prefs = instance(ApplicationPreferencesTag)) }
    bindProvider<SettingsRepository> {
        DefaultSettingsRepository(settingsLocalDataSource = instance())
    }
}
