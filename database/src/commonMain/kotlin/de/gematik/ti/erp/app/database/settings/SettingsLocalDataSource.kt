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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant

/**
 * LocalDataSource for settings using russhwolf multiplatform-settings.
 * Migrated from Realm SettingsEntityV1 in schema version 63.
 */
class SettingsLocalDataSource(private val settings: Settings) {

    private object Keys {
        const val ZOOM_ENABLED = "zoom_enabled"
        const val WELCOME_DRAWER_SHOWN = "welcome_drawer_shown"
        const val USER_ACCEPTED_INSECURE_DEVICE = "user_accepted_insecure_device"
        const val USER_ACCEPTED_INTEGRITY_NOT_OK = "user_accepted_integrity_not_ok"
        const val MLKIT_ACCEPTED = "mlkit_accepted"
        const val SCREENSHOTS_ALLOWED = "screenshots_allowed"
        const val TRACKING_ALLOWED = "tracking_allowed"
        const val DATA_PROTECTION_VERSION_ACCEPTED = "data_protection_version_accepted"
    }

    private val _zoomEnabled = MutableStateFlow(false)
    val zoomEnabled: Flow<Boolean> = _zoomEnabled.asStateFlow()

    private val _welcomeDrawerShown = MutableStateFlow(false)
    val welcomeDrawerShown: Flow<Boolean> = _welcomeDrawerShown.asStateFlow()

    private val _userHasAcceptedInsecureDevice = MutableStateFlow(false)
    val userHasAcceptedInsecureDevice: Flow<Boolean> = _userHasAcceptedInsecureDevice.asStateFlow()

    private val _userHasAcceptedIntegrityNotOk = MutableStateFlow(false)
    val userHasAcceptedIntegrityNotOk: Flow<Boolean> = _userHasAcceptedIntegrityNotOk.asStateFlow()

    private val _mlKitAccepted = MutableStateFlow(false)
    val mlKitAccepted: Flow<Boolean> = _mlKitAccepted.asStateFlow()

    private val _screenshotsAllowed = MutableStateFlow(false)
    val screenshotsAllowed: Flow<Boolean> = _screenshotsAllowed.asStateFlow()

    private val _trackingAllowed = MutableStateFlow(false)
    val trackingAllowed: Flow<Boolean> = _trackingAllowed.asStateFlow()

    private val _dataProtectionVersionAccepted = MutableStateFlow(
        Instant.fromEpochSeconds(1634256000L) // 2021-10-15T00:00:00Z
    )
    val dataProtectionVersionAccepted: Flow<Instant> = _dataProtectionVersionAccepted.asStateFlow()

    init {
        refreshAllValues()
    }

    fun refreshAllValues() {
        _zoomEnabled.value = settings.getBoolean(Keys.ZOOM_ENABLED, false)
        _welcomeDrawerShown.value = settings.getBoolean(Keys.WELCOME_DRAWER_SHOWN, false)
        _userHasAcceptedInsecureDevice.value = settings.getBoolean(Keys.USER_ACCEPTED_INSECURE_DEVICE, false)
        _userHasAcceptedIntegrityNotOk.value = settings.getBoolean(Keys.USER_ACCEPTED_INTEGRITY_NOT_OK, false)
        _mlKitAccepted.value = settings.getBoolean(Keys.MLKIT_ACCEPTED, false)
        _screenshotsAllowed.value = settings.getBoolean(Keys.SCREENSHOTS_ALLOWED, false)
        _trackingAllowed.value = settings.getBoolean(Keys.TRACKING_ALLOWED, false)

        val epochSeconds = settings.getLong(Keys.DATA_PROTECTION_VERSION_ACCEPTED, 1634256000L)
        _dataProtectionVersionAccepted.value = Instant.fromEpochSeconds(epochSeconds)
    }

    // Write operations
    fun saveZoomEnabled(enabled: Boolean) {
        settings[Keys.ZOOM_ENABLED] = enabled
        _zoomEnabled.value = enabled
    }

    fun saveWelcomeDrawerShown() {
        settings[Keys.WELCOME_DRAWER_SHOWN] = true
        _welcomeDrawerShown.value = true
    }

    fun acceptInsecureDevice() {
        settings[Keys.USER_ACCEPTED_INSECURE_DEVICE] = true
        _userHasAcceptedInsecureDevice.value = true
    }

    fun acceptIntegrityNotOk() {
        settings[Keys.USER_ACCEPTED_INTEGRITY_NOT_OK] = true
        _userHasAcceptedIntegrityNotOk.value = true
    }

    fun acceptMlKit() {
        settings[Keys.MLKIT_ACCEPTED] = true
        _mlKitAccepted.value = true
    }

    fun saveAllowScreenshots(allow: Boolean) {
        settings[Keys.SCREENSHOTS_ALLOWED] = allow
        _screenshotsAllowed.value = allow
    }

    fun saveAllowTracking(allow: Boolean) {
        settings[Keys.TRACKING_ALLOWED] = allow
        _trackingAllowed.value = allow
    }

    fun acceptUpdatedDataTerms(instant: Instant) {
        settings[Keys.DATA_PROTECTION_VERSION_ACCEPTED] = instant.epochSeconds
        _dataProtectionVersionAccepted.value = instant
    }
}
