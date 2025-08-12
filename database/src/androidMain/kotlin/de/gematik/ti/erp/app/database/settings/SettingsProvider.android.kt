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

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

lateinit var appContext: Context

fun initSharedPrefsSettings(context: Context) {
    appContext = context.applicationContext
}

/**
 * Provides a secure [Settings] instance on Android backed by [EncryptedSharedPreferences].
 *
 * This implementation uses AndroidX Security's [MasterKey] and AES-based encryption schemes
 * to protect both keys and values at rest, leveraging Android's Keystore system.
 *
 * ### Encryption Details:
 * - **Key Encryption Scheme:** `AES256_SIV`
 *   - Ensures deterministic, tamper-resistant encryption for preference **keys**
 *   - Secure against key substitution and manipulation attacks
 *
 * - **Value Encryption Scheme:** `AES256_GCM`
 *   - Encrypts preference **values** with authenticated encryption
 *   - AES (Advanced Encryption Standard) with a 256-bit key in Galois/Counter Mode
 *   - Ensures **confidentiality**, **integrity**, and **authentication**
 *
 * - **Key Storage:** [MasterKey] generated using `AES256_GCM`, stored in the Android Keystore.
 *   - If supported, it uses **hardware-backed security** (e.g., TEE or StrongBox)
 *   - Prevents key extraction or misuse by unauthorized apps or processes
 *
 * ### Purpose:
 * This function returns a [SharedPreferencesSettings] instance that transparently encrypts
 * all keys and values. It is ideal for storing sensitive user data such as:
 * - Authentication tokens
 * - Consent flags
 * - Encrypted metadata
 *
 * ### Usage:
 * ```kotlin
 * val sharedPrefs = provideSettings()
 * sharedPrefs.putString("auth_token", "abc123")
 * val token = sharedPrefs.getStringOrNull("auth_token")
 * sharedPrefs.remove("auth_token")
 * sharedPrefs.clear()
 * ```
 *
 * @return A secure [Settings] implementation using encrypted Android SharedPreferences
 */
actual fun provideSettings(): Settings {
    val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPrefs = EncryptedSharedPreferences.create(
        appContext,
        "erp_app_db_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    return SharedPreferencesSettings(sharedPrefs)
}
