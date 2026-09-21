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

import de.gematik.ti.erp.app.settings.model.ThemeMode
import kotlinx.serialization.Serializable

const val SETTINGS_ENTITY_SCHEMA_VERSION = 2

@Serializable
data class SettingsEntitySchema(
    val version: Int = SETTINGS_ENTITY_SCHEMA_VERSION,
    val entity: SettingsEntity = SettingsEntity()
)

@Serializable
data class SettingsEntity(
    val latestAppVersion: AppVersionEntity = AppVersionEntity(),
    val onboardingShownIn: AppVersionEntity? = null,
    val welcomeDrawerShown: Boolean = false,
    val theme: String = ThemeMode.SYSTEM.name,
    val zoomEnabled: Boolean = false,
    val userHasAcceptedInsecureDevice: Boolean = false,
    val userHasAcceptedIntegrityNotOk: Boolean = false,
    val trackingAllowed: Boolean = false,
    val screenShotsAllowed: Boolean = false,
    val isDataPortedToRoom: Boolean = false
)

@Serializable
data class AppVersionEntity(
    val name: String = "",
    val code: Int = 0
)
