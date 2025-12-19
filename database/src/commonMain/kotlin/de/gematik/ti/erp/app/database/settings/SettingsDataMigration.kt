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

package de.gematik.ti.erp.app.database.settings

import com.russhwolf.settings.Settings
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.v1.SettingsEntityV1
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query

class SettingsDataMigration(
    private val realm: Realm,
    private val settingsLocalDataSource: SettingsLocalDataSource,
    private val settings: Settings
) {
    companion object {
        private const val MIGRATION_FLAG_KEY = "settings_datastore_migration"
    }

    fun performMigrationIfNeeded() {
        val migrationCompleted = settings.getBoolean(MIGRATION_FLAG_KEY, false)

        if (!migrationCompleted) {
            realm.query<SettingsEntityV1>().first().find()?.let { realmSettings ->
                migrateFromRealmToSharedPreferences(realmSettings)
            }

            settings.putBoolean(MIGRATION_FLAG_KEY, true)
        }

        settingsLocalDataSource.refreshAllValues()
    }

    private fun migrateFromRealmToSharedPreferences(realmSettings: SettingsEntityV1) {
        settingsLocalDataSource.saveZoomEnabled(realmSettings.zoomEnabled)
        settingsLocalDataSource.saveWelcomeDrawerShown()

        if (realmSettings.userHasAcceptedInsecureDevice) {
            settingsLocalDataSource.acceptInsecureDevice()
        }
        if (realmSettings.userHasAcceptedIntegrityNotOk) {
            settingsLocalDataSource.acceptIntegrityNotOk()
        }
        if (realmSettings.mlKitAccepted) {
            settingsLocalDataSource.acceptMlKit()
        }

        settingsLocalDataSource.saveAllowScreenshots(realmSettings.screenshotsAllowed)
        settingsLocalDataSource.saveAllowTracking(realmSettings.trackingAllowed)
        settingsLocalDataSource.acceptUpdatedDataTerms(realmSettings.dataProtectionVersionAccepted.toInstant())
    }
}
