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

package de.gematik.ti.erp.app.database.realm.v1.settings

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.api.SettingsLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.writeToRealm
import de.gematik.ti.erp.app.settings.model.AppVersionErpModel
import de.gematik.ti.erp.app.settings.model.SettingsErpModel
import de.gematik.ti.erp.app.settings.model.ThemeMode
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class SettingsLocalDataSourceV1(
    private val realm: Realm
) : SettingsLocalDataSource {

    override fun loadSettings(): Flow<SettingsErpModel> =
        realm.query<SettingsEntityV1>().first().asFlow().mapNotNull { it.obj?.toSettingsErpModel() }

    override fun isAnalyticsAllowed(): Flow<Boolean> =
        realm.query<SettingsEntityV1>().first().asFlow().mapNotNull { it.obj?.trackingAllowed }

    override suspend fun saveLatestAppVersion(appVersion: AppVersionErpModel) {
        realm.writeToRealm<SettingsEntityV1, Unit> {
            it.latestAppVersionName = appVersion.name
            it.latestAppVersionCode = appVersion.code
        }
    }

    override suspend fun saveOnboardingShownIn(appVersion: AppVersionErpModel) {
        realm.writeToRealm<SettingsEntityV1, Unit> {
            it.onboardingLatestAppVersionName = appVersion.name
            it.onboardingLatestAppVersionCode = appVersion.code
        }
    }

    override suspend fun saveTheme(theme: ThemeMode) {
        realm.writeToRealm<SettingsEntityV1, Unit> {
            it.theme = theme.name
        }
    }

    override suspend fun saveZoomEnabled(enabled: Boolean) {
        realm.writeToRealm<SettingsEntityV1, Unit> {
            it.zoomEnabled = enabled
        }
    }

    override suspend fun acceptInsecureDevice() {
        realm.writeToRealm<SettingsEntityV1, Unit> {
            it.userHasAcceptedInsecureDevice = true
        }
    }

    override suspend fun saveWelcomeDrawerShown() {
        realm.writeToRealm<SettingsEntityV1, Unit> {
            it.welcomeDrawerShown = true
        }
    }

    override suspend fun saveAllowScreenshots(allow: Boolean) {
        realm.writeToRealm<SettingsEntityV1, Unit> {
            it.screenshotsAllowed = allow
        }
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
        realm.writeToRealm<SettingsEntityV1, Unit> {
            it.trackingAllowed = allow
        }
    }

    override suspend fun acceptIntegrityNotOk() {
        realm.writeToRealm<SettingsEntityV1, Unit> {
            it.userHasAcceptedIntegrityNotOk = true
        }
    }

    override suspend fun resetOnboardingShownIn() {
        realm.writeToRealm<SettingsEntityV1, Unit> {
            it.onboardingLatestAppVersionCode = -1
            it.onboardingLatestAppVersionName = ""
        }
    }

    override fun isDataPortedToRoom(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)

    override suspend fun markDataAsPortedToRoom() {
        // No-op for V1
    }
}
