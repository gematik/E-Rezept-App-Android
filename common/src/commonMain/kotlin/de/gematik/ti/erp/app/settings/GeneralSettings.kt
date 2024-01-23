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

import de.gematik.ti.erp.app.settings.model.SettingsData
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface GeneralSettings {
    val general: Flow<SettingsData.General>

    suspend fun acceptUpdatedDataTerms(now: Instant = Clock.System.now())
    suspend fun saveOnboardingSucceededData(
        authenticationMode: SettingsData.AuthenticationMode,
        profileName: String,
        now: Instant = Clock.System.now()
    )

    suspend fun saveAuthenticationMode(mode: SettingsData.AuthenticationMode)
    val authenticationMode: Flow<SettingsData.AuthenticationMode>

    suspend fun saveZoomPreference(enabled: Boolean)

    suspend fun acceptInsecureDevice()

    suspend fun incrementNumberOfAuthenticationFailures()
    suspend fun resetNumberOfAuthenticationFailures()
    suspend fun saveWelcomeDrawerShown()
    suspend fun saveMainScreenTooltipShown()
    suspend fun acceptMlKit()
    suspend fun saveAllowScreenshots(allow: Boolean)
}
