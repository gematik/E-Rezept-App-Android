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
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultEuVersionPreferencesDataStore(
    private val settings: Settings
) : EuVersionDataStore {
    private val _euVersion = MutableStateFlow(
        getEuVersion().also {
            Napier.d(tag = "eu-version-preferences-datastore") { "Loaded EuVersion: $it" }
        }
    )

    override val euVersion: StateFlow<EuVersion> = _euVersion.asStateFlow()

    override fun saveEuVersion(euVersion: EuVersion) {
        settings.putString(KEY_EU_VERSION, euVersion.name)
        _euVersion.value = euVersion
    }

    private fun getEuVersion(): EuVersion {
        val value = settings.getStringOrNull(KEY_EU_VERSION)
        return try {
            value?.let { EuVersion.valueOf(it) } ?: EuVersion.V_1_1
        } catch (e: Exception) {
            Napier.w(tag = "eu-version-preferences-datastore") { "Invalid EuVersion: $value, using default: EuVersion.V_1_1 due to ${e.message}" }
            EuVersion.V_1_1
        }
    }

    companion object {
        private const val KEY_EU_VERSION = "debug_eu_version"
    }
}
