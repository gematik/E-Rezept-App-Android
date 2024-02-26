/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.settings

import de.gematik.ti.erp.app.settings.repository.CardWallRepository
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import de.gematik.ti.erp.app.settings.usecase.AllowScreenshotsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetAuthenticationModeUseCase
import de.gematik.ti.erp.app.settings.usecase.GetCanStartToolTipsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetMLKitAcceptedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetOnboardingSucceededUseCase
import de.gematik.ti.erp.app.settings.usecase.GetScreenShotsAllowedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetShowWelcomeDrawerUseCase
import de.gematik.ti.erp.app.settings.usecase.GetZoomStateUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveDeviceSecurityUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveOnboardingDataUseCase
import de.gematik.ti.erp.app.settings.usecase.SavePasswordUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveToolTippsShownUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveWelcomeDrawerShownUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveZoomPreferenceUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

const val ApplicationPreferencesTag = "ApplicationPreferences"

val settingsModule = DI.Module("settingsModule") {
    bindProvider { CardWallRepository(prefs = instance(ApplicationPreferencesTag)) }
    bindProvider { SettingsRepository(instance(), instance()) }
    bindProvider { GetScreenShotsAllowedUseCase(instance()) }
    bindProvider { AllowScreenshotsUseCase(instance()) }
    bindProvider { GetOnboardingSucceededUseCase(instance()) }
    bindProvider { SaveOnboardingDataUseCase(instance()) }
    bindProvider { GetMLKitAcceptedUseCase(instance()) }
    bindProvider { GetCanStartToolTipsUseCase(instance()) }
    bindProvider { SaveToolTippsShownUseCase(instance()) }
    bindProvider { SavePasswordUseCase(instance()) }
    bindProvider { GetShowWelcomeDrawerUseCase(instance()) }
    bindProvider { SaveWelcomeDrawerShownUseCase(instance()) }
    bindProvider { GetAuthenticationModeUseCase(instance()) }
    bindProvider { GetZoomStateUseCase(instance()) }
    bindProvider { SaveDeviceSecurityUseCase(instance()) }
    bindProvider { SaveZoomPreferenceUseCase(instance()) }
}
