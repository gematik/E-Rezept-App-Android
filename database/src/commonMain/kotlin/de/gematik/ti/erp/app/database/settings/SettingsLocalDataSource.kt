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
import com.russhwolf.settings.set
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.logger.DbMigrationLogEntry
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant

/**
 * LocalDataSource for settings using russhwolf multiplatform-settings.
 * Migrated from Realm SettingsEntityV1 in schema version 63.
 */
class SettingsLocalDataSource(private val settings: Settings) {
    private val _zoomEnabled = MutableStateFlow(
        settings.getBoolean(SettingsKeys.ZOOM_ENABLED, false)
    )
    val zoomEnabled: Flow<Boolean> = _zoomEnabled.asStateFlow()

    private val _welcomeDrawerShown = MutableStateFlow(
        settings.getBoolean(SettingsKeys.WELCOME_DRAWER_SHOWN, false)
    )
    val welcomeDrawerShown: Flow<Boolean> = _welcomeDrawerShown.asStateFlow()

    private val _userHasAcceptedInsecureDevice = MutableStateFlow(
        settings.getBoolean(SettingsKeys.USER_ACCEPTED_INSECURE_DEVICE, false)
    )
    val userHasAcceptedInsecureDevice: Flow<Boolean> = _userHasAcceptedInsecureDevice.asStateFlow()

    private val _userHasAcceptedIntegrityNotOk = MutableStateFlow(
        settings.getBoolean(SettingsKeys.USER_ACCEPTED_INTEGRITY_NOT_OK, false)
    )
    val userHasAcceptedIntegrityNotOk: Flow<Boolean> = _userHasAcceptedIntegrityNotOk.asStateFlow()

    private val _mlKitAccepted = MutableStateFlow(
        settings.getBoolean(SettingsKeys.MLKIT_ACCEPTED, false)
    )
    val mlKitAccepted: Flow<Boolean> = _mlKitAccepted.asStateFlow()

    private val _screenshotsAllowed = MutableStateFlow(
        settings.getBoolean(SettingsKeys.SCREENSHOTS_ALLOWED, false)
    )
    val screenshotsAllowed: Flow<Boolean> = _screenshotsAllowed.asStateFlow()

    private val _trackingAllowed = MutableStateFlow(
        settings.getBoolean(SettingsKeys.TRACKING_ALLOWED, false)
    )
    val trackingAllowed: Flow<Boolean> = _trackingAllowed.asStateFlow()

    private val _dataProtectionVersionAccepted = MutableStateFlow(
        Instant.fromEpochSeconds(
            settings.getLong(SettingsKeys.DATA_PROTECTION_VERSION_ACCEPTED, 1634256000L)
        )
    )
    val dataProtectionVersionAccepted: Flow<Instant> = _dataProtectionVersionAccepted.asStateFlow()

    private val _dbMigrationLogs = MutableStateFlow(loadDbMigrationLogs())
    val dbMigrationLogs: StateFlow<List<DbMigrationLogEntry>> = _dbMigrationLogs.asStateFlow()

    private fun loadDbMigrationLogs(): List<DbMigrationLogEntry> {
        val logJson = settings.getString(SettingsKeys.DB_MIGRATION_LOGS, "")
        return if (logJson.isNotEmpty()) {
            try {
                SafeJson.value.decodeFromString<List<DbMigrationLogEntry>>(logJson)
            } catch (e: Exception) {
                Napier.e { "error loading db logs ${e.stackTraceToString()}" }
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Write operations
    fun saveZoomEnabled(enabled: Boolean) {
        settings[SettingsKeys.ZOOM_ENABLED] = enabled
        _zoomEnabled.value = enabled
    }

    fun saveWelcomeDrawerShown() {
        settings[SettingsKeys.WELCOME_DRAWER_SHOWN] = true
        _welcomeDrawerShown.value = true
    }

    fun acceptInsecureDevice() {
        settings[SettingsKeys.USER_ACCEPTED_INSECURE_DEVICE] = true
        _userHasAcceptedInsecureDevice.value = true
    }

    fun acceptIntegrityNotOk() {
        settings[SettingsKeys.USER_ACCEPTED_INTEGRITY_NOT_OK] = true
        _userHasAcceptedIntegrityNotOk.value = true
    }

    fun acceptMlKit() {
        settings[SettingsKeys.MLKIT_ACCEPTED] = true
        _mlKitAccepted.value = true
    }

    fun saveAllowScreenshots(allow: Boolean) {
        settings[SettingsKeys.SCREENSHOTS_ALLOWED] = allow
        _screenshotsAllowed.value = allow
    }

    fun saveAllowTracking(allow: Boolean) {
        settings[SettingsKeys.TRACKING_ALLOWED] = allow
        _trackingAllowed.value = allow
    }

    fun acceptUpdatedDataTerms(instant: Instant) {
        settings[SettingsKeys.DATA_PROTECTION_VERSION_ACCEPTED] = instant.epochSeconds
        _dataProtectionVersionAccepted.value = instant
    }

    fun saveDbMigrationLogs(logs: List<DbMigrationLogEntry>) {
        val logJson = SafeJson.value.encodeToString(logs)
        settings[SettingsKeys.DB_MIGRATION_LOGS] = logJson
        _dbMigrationLogs.value = logs
    }
}
