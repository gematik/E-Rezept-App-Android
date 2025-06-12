/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.settings

import de.gematik.ti.erp.app.settings.repository.CardWallRepository
import de.gematik.ti.erp.app.settings.repository.DefaultSettingsRepository
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import de.gematik.ti.erp.app.settings.usecase.AllowScreenshotsUseCase
import de.gematik.ti.erp.app.settings.usecase.DefaultXmlResourceParserWrapper
import de.gematik.ti.erp.app.settings.usecase.DisableDeviceSecurityUseCase
import de.gematik.ti.erp.app.settings.usecase.EnableDeviceSecurityUseCase
import de.gematik.ti.erp.app.settings.usecase.GetAuthenticationUseCase
import de.gematik.ti.erp.app.settings.usecase.GetCanStartToolTipsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetMLKitAcceptedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetOrganDonationRegisterHostsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetScreenShotsAllowedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetShowWelcomeDrawerUseCase
import de.gematik.ti.erp.app.settings.usecase.GetSupportedLanguagesFromXmlUseCase
import de.gematik.ti.erp.app.settings.usecase.GetZoomStateUseCase
import de.gematik.ti.erp.app.settings.usecase.HasValidDigasUseCase
import de.gematik.ti.erp.app.settings.usecase.ResetPasswordUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveToolTipsShownUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveWelcomeDrawerShownUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveZoomPreferenceUseCase
import de.gematik.ti.erp.app.settings.usecase.SetPasswordUseCase
import de.gematik.ti.erp.app.settings.usecase.XmlResourceParserWrapper
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

const val ApplicationPreferencesTag = "ApplicationPreferences"

val settingsModule = DI.Module("settingsModule") {
    bindProvider { GetScreenShotsAllowedUseCase(instance()) }
    bindProvider { AllowScreenshotsUseCase(instance()) }
    bindProvider { GetMLKitAcceptedUseCase(instance()) }
    bindProvider { GetCanStartToolTipsUseCase(instance()) }
    bindProvider { SaveToolTipsShownUseCase(instance()) }
    bindProvider { SetPasswordUseCase(instance()) }
    bindProvider { GetShowWelcomeDrawerUseCase(instance()) }
    bindProvider { SaveWelcomeDrawerShownUseCase(instance()) }
    bindProvider { GetAuthenticationUseCase(instance()) }
    bindProvider { GetZoomStateUseCase(instance()) }
    bindProvider { EnableDeviceSecurityUseCase(instance()) }
    bindProvider { DisableDeviceSecurityUseCase(instance()) }
    bindProvider { ResetPasswordUseCase(instance()) }
    bindProvider { SaveZoomPreferenceUseCase(instance()) }
    bindProvider { GetOrganDonationRegisterHostsUseCase(instance()) }
    bindProvider { HasValidDigasUseCase(instance()) }

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
    bindProvider<SettingsRepository> { DefaultSettingsRepository(instance(), instance()) }
}
