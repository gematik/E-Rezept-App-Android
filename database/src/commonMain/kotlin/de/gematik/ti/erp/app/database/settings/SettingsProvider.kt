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

expect fun provideSettings(): Settings

/**
 * Provides a [Settings] instance suitable for the current platform.
 *
 * This function is defined as an `expect` in common code and implemented
 * separately for each platform (e.g., Android, JVM/desktop, iOS).
 *
 * ## Platform-specific implementations:
 * - **Android**: Wraps `SharedPreferences` via [SharedPreferencesSettings]
 * - **Desktop (JVM)**: Uses Java's `Preferences` API via [PreferencesSettings]
 * - **iOS/macOS**: Uses `NSUserDefaults` via [AppleSettings] *(if implemented)*
 *
 * ## Usage:
 * Call this in shared or platform code to get access to key-value storage:
 *
 * ```kotlin
 * val sharedPrefs = provideSettings()
 *
 * // Store values
 * sharedPrefs.putString("auth_token", "abc123")
 *
 * // Retrieve values
 * val token = sharedPrefs.getStringOrNull("auth_token")
 *
 * // Remove a specific key
 * sharedPrefs.remove("auth_token")
 *
 * // Clear all stored keys
 * sharedPrefs.clear()
 * ```
 *
 * ## Purpose:
 * Used for storing user preferences, feature flags, auth tokens, and other
 * simple key-value pairs that must persist across sessions. Ideal for use in
 * Kotlin Multiplatform Projects (KMP) where consistent access to preferences
 * is needed across Android, desktop, and iOS targets.
 *
 * @return A platform-appropriate [Settings] implementation
 */
val sharedPrefs = provideSettings()
