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

object SettingsKeys {
    const val ZOOM_ENABLED = "zoom_enabled"
    const val WELCOME_DRAWER_SHOWN = "welcome_drawer_shown"
    const val USER_ACCEPTED_INSECURE_DEVICE = "user_accepted_insecure_device"
    const val USER_ACCEPTED_INTEGRITY_NOT_OK = "user_accepted_integrity_not_ok"
    const val MLKIT_ACCEPTED = "mlkit_accepted"
    const val SCREENSHOTS_ALLOWED = "screenshots_allowed"
    const val TRACKING_ALLOWED = "tracking_allowed"
    const val DATA_PROTECTION_VERSION_ACCEPTED = "data_protection_version_accepted"
    const val MIGRATION_FLAG_KEY = "settings_datastore_migration"
}
