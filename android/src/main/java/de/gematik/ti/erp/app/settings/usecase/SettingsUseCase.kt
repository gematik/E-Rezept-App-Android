/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.settings.usecase

import android.app.KeyguardManager
import android.content.Context
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.profiles.usecase.sanitizedProfileName
import de.gematik.ti.erp.app.settings.GeneralSettings
import de.gematik.ti.erp.app.settings.PharmacySettings
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.model.SettingsData.General
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val DEFAULT_PROFILE_NAME = ""
val DATA_PROTECTION_LAST_UPDATED: Instant =
    LocalDate.parse(BuildKonfig.DATA_PROTECTION_LAST_UPDATED).atStartOfDay().toInstant(ZoneOffset.UTC)

class SettingsUseCase(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : GeneralSettings by settingsRepository,
    PharmacySettings by settingsRepository {

    // tag::ShowInsecureDevicePrompt[]
    val showInsecureDevicePrompt =
        settingsRepository.general.map {
            val deviceSecured =
                (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceSecure

            if (!deviceSecured) {
                !it.userHasAcceptedInsecureDevice
            } else {
                false
            }
        }
    // end::ShowInsecureDevicePrompt[]

    val showOnboarding = settingsRepository.general.map { it.onboardingShownIn == null }
    val showWelcomeDrawer = settingsRepository.general.map { !it.welcomeDrawerShown }

    var showDataTermsUpdate: Flow<Boolean> =
        settingsRepository.general.map { it.dataProtectionVersionAcceptedOn < DATA_PROTECTION_LAST_UPDATED }

    suspend fun welcomeDrawerShown() {
        settingsRepository.saveWelcomeDrawerShown()
    }
    override val general: Flow<General>
        get() = settingsRepository.general

    suspend fun onboardingSucceeded(
        authenticationMode: SettingsData.AuthenticationMode,
        defaultProfileName: String,
        now: Instant = Instant.now()
    ) {
        sanitizedProfileName(defaultProfileName)?.also { name ->
            settingsRepository.saveOnboardingSucceededData(authenticationMode, name, now)
        }
    }
}
