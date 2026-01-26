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
import de.gematik.ti.erp.app.settings.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * DataStore for theme preferences using SharedPreferences.
 *
 * Provides a simple key-value storage for the user's theme mode preference.
 * Uses SharedPreferences instead of Realm because theme mode is a simple
 * preference setting that doesn't need complex querying or relationships.
 *
 * ## Usage:
 * ```kotlin
 * val dataStore = ThemePreferencesDataStore(sharedPrefs)
 *
 * // Observe theme mode changes
 * dataStore.themeMode.collect { mode ->
 *     when (mode) {
 *         ThemeMode.SYSTEM -> // Follow system
 *         ThemeMode.LIGHT -> // Force light
 *         ThemeMode.DARK -> // Force dark
 *     }
 * }
 *
 * // Save theme mode
 * dataStore.saveThemeMode(ThemeMode.DARK)
 * ```
 *
 * @param settings Platform-specific Settings implementation (SharedPreferences on Android)
 */
class ThemePreferencesDataStore(
    private val settings: Settings
) {
    private val _themeMode = MutableStateFlow(getThemeMode())

    /**
     * Observes the current theme mode preference.
     *
     * Emits [ThemeMode.SYSTEM] by default if no preference is stored.
     * The flow is hot and will emit the current value immediately upon collection,
     * and will emit new values whenever [saveThemeMode] is called.
     *
     * @return StateFlow of [ThemeMode] that emits whenever the theme preference changes
     */
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    /**
     * Saves the user's theme mode preference.
     *
     * Persists the selection to SharedPreferences for retrieval across app sessions.
     * Also immediately updates the [themeMode] flow with the new value.
     *
     * @param themeMode The theme mode to save (SYSTEM, LIGHT, or DARK)
     */
    fun saveThemeMode(themeMode: ThemeMode) {
        settings.putString(KEY_THEME_MODE, themeMode.name)
        _themeMode.value = themeMode
    }

    /**
     * Gets the current theme mode synchronously.
     *
     * @return The stored [ThemeMode], or [ThemeMode.SYSTEM] if not set
     */
    private fun getThemeMode(): ThemeMode {
        val value = settings.getStringOrNull(KEY_THEME_MODE)
        return value?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
