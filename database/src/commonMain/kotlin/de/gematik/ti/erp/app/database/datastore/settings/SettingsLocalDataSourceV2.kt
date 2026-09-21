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

package de.gematik.ti.erp.app.database.datastore.settings

import androidx.datastore.core.DataStore
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.api.SettingsLocalDataSource
import de.gematik.ti.erp.app.settings.model.AppVersionErpModel
import de.gematik.ti.erp.app.settings.model.SettingsErpModel
import de.gematik.ti.erp.app.settings.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val SETTINGS_DATA_STORE = "SettingsDataStore"

class SettingsLocalDataSourceV2(
    private val dataStore: DataStore<SettingsEntitySchema>
) : SettingsLocalDataSource {
    override fun loadSettings(): Flow<SettingsErpModel> = dataStore.data.map {
        it.entity.toSettingsErpModel()
    }

    override fun isAnalyticsAllowed(): Flow<Boolean> = dataStore.data.map {
        it.entity.trackingAllowed
    }

    override suspend fun saveLatestAppVersion(appVersion: AppVersionErpModel) {
        updateEntity { it.copy(latestAppVersion = appVersion.toAppVersionEntity()) }
    }

    override suspend fun saveOnboardingShownIn(appVersion: AppVersionErpModel) {
        updateEntity { it.copy(onboardingShownIn = appVersion.toAppVersionEntity()) }
    }

    override suspend fun resetOnboardingShownIn() {
        updateEntity { it.copy(onboardingShownIn = null) }
    }

    override suspend fun saveWelcomeDrawerShown() {
        updateEntity { it.copy(welcomeDrawerShown = true) }
    }

    override suspend fun saveTheme(theme: ThemeMode) {
        updateEntity { it.copy(theme = theme.name) }
    }

    override suspend fun saveZoomEnabled(enabled: Boolean) {
        updateEntity { it.copy(zoomEnabled = enabled) }
    }

    override suspend fun acceptInsecureDevice() {
        updateEntity { it.copy(userHasAcceptedInsecureDevice = true) }
    }

    override suspend fun acceptIntegrityNotOk() {
        updateEntity { it.copy(userHasAcceptedIntegrityNotOk = true) }
    }

    @Requirement(
        "O.Purp_5#6",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = " save allow/disallow analytics state to settings repository."
    )
    @Requirement(
        "A_24525#3",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Save the user's decision to allow or disallow tracking."
    )
    override suspend fun saveAllowTracking(allow: Boolean) {
        updateEntity { it.copy(trackingAllowed = allow) }
    }

    override suspend fun saveAllowScreenshots(allow: Boolean) {
        updateEntity { it.copy(screenShotsAllowed = allow) }
    }

    override fun isDataPortedToRoom(): Flow<Boolean> = dataStore.data.map {
        it.entity.isDataPortedToRoom
    }

    override suspend fun markDataAsPortedToRoom() {
        updateEntity { it.copy(isDataPortedToRoom = true) }
    }

    private suspend fun updateEntity(transform: (SettingsEntity) -> SettingsEntity) {
        dataStore.updateData { schema ->
            schema.copy(entity = transform(schema.entity))
        }
    }
}
