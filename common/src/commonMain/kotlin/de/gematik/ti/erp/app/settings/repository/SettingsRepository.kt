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

import de.gematik.ti.erp.app.database.realm.utils.writeToRealm
import de.gematik.ti.erp.app.database.realm.v1.SettingsEntityV1
import de.gematik.ti.erp.app.settings.AnalyticsSettings
import de.gematik.ti.erp.app.settings.AuthenticationSettings
import de.gematik.ti.erp.app.settings.GeneralSettings
import de.gematik.ti.erp.app.settings.PharmacySettings
import io.realm.kotlin.Realm
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class SettingsRepository(
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO,
    private val realm: Realm
) : GeneralSettings,
    PharmacySettings,
    AnalyticsSettings,
    AuthenticationSettings {
    suspend fun writeToRealm(block: SettingsEntityV1.() -> Unit) {
        withContext(dispatchers) {
            realm.writeToRealm<SettingsEntityV1, Unit> {
                it.block()
            }
        }
    }
}
