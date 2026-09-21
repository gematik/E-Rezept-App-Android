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

package de.gematik.ti.erp.app.settings.repository

import de.gematik.ti.erp.app.database.api.SettingsLocalDataSource
import de.gematik.ti.erp.app.settings.model.AppVersionErpModel
import de.gematik.ti.erp.app.settings.model.SettingsErpModel
import de.gematik.ti.erp.app.settings.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSettingsRepository(
    private val settingsLocalDataSource: SettingsLocalDataSource
) : SettingsRepository {
    override fun loadSettings(): Flow<SettingsErpModel> = settingsLocalDataSource.loadSettings()

    override fun isAnalyticsAllowed(): Flow<Boolean> = settingsLocalDataSource.isAnalyticsAllowed()

    override suspend fun saveLatestAppVersion(appVersion: AppVersionErpModel) =
        settingsLocalDataSource.saveLatestAppVersion(appVersion)

    override suspend fun saveOnboardingShownIn(appVersion: AppVersionErpModel) =
        settingsLocalDataSource.saveOnboardingShownIn(appVersion)

    override suspend fun saveTheme(theme: ThemeMode) =
        settingsLocalDataSource.saveTheme(theme)

    override suspend fun saveZoomEnabled(enabled: Boolean) =
        settingsLocalDataSource.saveZoomEnabled(enabled)

    override suspend fun acceptInsecureDevice() =
        settingsLocalDataSource.acceptInsecureDevice()

    override suspend fun saveWelcomeDrawerShown() =
        settingsLocalDataSource.saveWelcomeDrawerShown()

    override suspend fun saveAllowScreenshots(allow: Boolean) =
        settingsLocalDataSource.saveAllowScreenshots(allow)

    override suspend fun saveAllowTracking(allow: Boolean) =
        settingsLocalDataSource.saveAllowTracking(allow)

    override suspend fun acceptIntegrityNotOk() =
        settingsLocalDataSource.acceptIntegrityNotOk()

    override suspend fun resetOnboardingShownIn() =
        settingsLocalDataSource.resetOnboardingShownIn()
}
