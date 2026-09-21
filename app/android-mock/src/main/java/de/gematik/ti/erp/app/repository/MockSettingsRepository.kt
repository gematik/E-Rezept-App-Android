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

package de.gematik.ti.erp.app.repository

import de.gematik.ti.erp.app.settings.model.AppVersionErpModel
import de.gematik.ti.erp.app.settings.model.SettingsErpModel
import de.gematik.ti.erp.app.settings.model.ThemeMode
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class MockSettingsRepository : SettingsRepository {

    private val settingsFlow = MutableStateFlow(
        SettingsErpModel(
            latestAppVersion = AppVersionErpModel(name = "mock", code = 0),
            onboardingShownIn = AppVersionErpModel(name = "mock", code = 0),
            theme = ThemeMode.SYSTEM,
            welcomeDrawerShown = true,
            zoomEnabled = false,
            userHasAcceptedInsecureDevice = false,
            userHasAcceptedIntegrityNotOk = false,
            trackingAllowed = false,
            screenShotsAllowed = false
        )
    )

    override fun loadSettings(): Flow<SettingsErpModel> = settingsFlow

    override fun isAnalyticsAllowed(): Flow<Boolean> =
        settingsFlow.map { it.trackingAllowed }

    override suspend fun saveLatestAppVersion(appVersion: AppVersionErpModel) {
        settingsFlow.value = settingsFlow.value.copy(latestAppVersion = appVersion)
    }

    override suspend fun saveOnboardingShownIn(appVersion: AppVersionErpModel) {
        settingsFlow.value = settingsFlow.value.copy(onboardingShownIn = appVersion)
    }

    override suspend fun saveTheme(theme: ThemeMode) {
        settingsFlow.value = settingsFlow.value.copy(theme = theme)
    }

    override suspend fun saveZoomEnabled(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(zoomEnabled = enabled)
    }

    override suspend fun acceptInsecureDevice() {
        settingsFlow.value = settingsFlow.value.copy(userHasAcceptedInsecureDevice = true)
    }

    override suspend fun saveWelcomeDrawerShown() {
        settingsFlow.value = settingsFlow.value.copy(welcomeDrawerShown = true)
    }

    override suspend fun saveAllowScreenshots(allow: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(screenShotsAllowed = allow)
    }

    override suspend fun saveAllowTracking(allow: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(trackingAllowed = allow)
    }

    override suspend fun acceptIntegrityNotOk() {
        settingsFlow.value = settingsFlow.value.copy(userHasAcceptedIntegrityNotOk = true)
    }

    override suspend fun resetOnboardingShownIn() {
        settingsFlow.value = settingsFlow.value.copy(onboardingShownIn = null)
    }
}
