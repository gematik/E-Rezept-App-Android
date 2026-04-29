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

/**
 * Implementation of [CommunicationDigaVersionDataStore] using SharedPreferences.
 *
 * @param settings Platform-specific Settings implementation (SharedPreferences on Android)
 */
class DefaultCommunicationDigaVersionPreferencesDataStore(
    private val settings: Settings
) : CommunicationDigaVersionDataStore {
    private val _communicationDigaVersion = MutableStateFlow(
        getCommunicationDigaVersion().also {
            Napier.d(tag = "communication-diga-version-preferences-datastore") { "Loaded CommunicationDigaVersion: $it" }
        }
    )

    override val communicationDigaVersion: StateFlow<CommunicationDigaVersion> =
        _communicationDigaVersion.asStateFlow()

    override fun saveCommunicationDigaVersion(communicationDigaVersion: CommunicationDigaVersion) {
        settings.putString(KEY_COMMUNICATION_DIGA_VERSION, communicationDigaVersion.name)
        _communicationDigaVersion.value = communicationDigaVersion
    }

    private fun getCommunicationDigaVersion(): CommunicationDigaVersion {
        val value = settings.getStringOrNull(KEY_COMMUNICATION_DIGA_VERSION)
        return value?.let { CommunicationDigaVersion.valueOf(it) } ?: CommunicationDigaVersion.V_1_5
    }

    companion object {
        private const val KEY_COMMUNICATION_DIGA_VERSION = "debug_communication_diga_version"
    }
}
