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
import de.gematik.ti.erp.app.database.settings.SettingsKeys.DATA_PROTECTION_VERSION_ACCEPTED
import de.gematik.ti.erp.app.database.settings.SettingsKeys.MIGRATION_FLAG_KEY
import de.gematik.ti.erp.app.database.settings.SettingsKeys.MLKIT_ACCEPTED
import de.gematik.ti.erp.app.database.settings.SettingsKeys.SCREENSHOTS_ALLOWED
import de.gematik.ti.erp.app.database.settings.SettingsKeys.TRACKING_ALLOWED
import de.gematik.ti.erp.app.database.settings.SettingsKeys.USER_ACCEPTED_INSECURE_DEVICE
import de.gematik.ti.erp.app.database.settings.SettingsKeys.USER_ACCEPTED_INTEGRITY_NOT_OK
import de.gematik.ti.erp.app.database.settings.SettingsKeys.WELCOME_DRAWER_SHOWN
import de.gematik.ti.erp.app.database.settings.SettingsKeys.ZOOM_ENABLED
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query

class SettingsDataMigration(
    private val realm: Realm,
    private val settings: Settings
) {
    fun performMigrationIfNeeded() {
        val migrationCompleted = settings.getBoolean(MIGRATION_FLAG_KEY, false)

        if (!migrationCompleted) {
            realm.query<SettingsEntityV1>().first().find()?.let { realmSettings ->
                migrateFromRealmToSharedPreferences(realmSettings)
            }

            settings.putBoolean(MIGRATION_FLAG_KEY, true)
        }
    }

    private fun migrateFromRealmToSharedPreferences(realmSettings: SettingsEntityV1) {
        settings.putBoolean(ZOOM_ENABLED, realmSettings.zoomEnabled)

        if (realmSettings.welcomeDrawerShown) {
            settings.putBoolean(WELCOME_DRAWER_SHOWN, true)
        }

        if (realmSettings.userHasAcceptedInsecureDevice) {
            settings.putBoolean(USER_ACCEPTED_INSECURE_DEVICE, true)
        }

        if (realmSettings.userHasAcceptedIntegrityNotOk) {
            settings.putBoolean(USER_ACCEPTED_INTEGRITY_NOT_OK, true)
        }

        if (realmSettings.mlKitAccepted) {
            settings.putBoolean(MLKIT_ACCEPTED, true)
        }

        settings.putBoolean(SCREENSHOTS_ALLOWED, realmSettings.screenshotsAllowed)
        settings.putBoolean(TRACKING_ALLOWED, realmSettings.trackingAllowed)
        settings.putLong(DATA_PROTECTION_VERSION_ACCEPTED, realmSettings.dataProtectionVersionAccepted.toInstant().epochSeconds)
    }
}
