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

/*
 * Copyright (Change Date see Readme), gematik GmbH
 */

package de.gematik.ti.erp.app.database.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple preferences-backed data store for the debug override flag controlling the Android 8 deprecation screen.
 */
class DefaultAndroid8DeprecationOverridePreferencesDataStore(
    private val settings: Settings
) : Android8DeprecationOverrideDataStore {
    private val preferencesKey = "debug_android_8_deprecation_override"

    private val _override = MutableStateFlow(get())
    override val override: StateFlow<Boolean> = _override.asStateFlow()

    override fun save(value: Boolean) {
        settings[preferencesKey] = value
        _override.value = value
    }

    override fun get(): Boolean = settings.getBoolean(preferencesKey, false)
}
