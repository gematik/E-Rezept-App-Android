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

package de.gematik.ti.erp.app.database.room.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.jose4j.base64url.Base64
import java.io.File
import java.security.SecureRandom
import de.gematik.ti.erp.app.database.BuildConfig as ModuleBuildConfig

object RoomEncryptionConfig {
    private const val ENCRYPTED_ROOM_PREFS_FILE_NAME = "ENCRYPTED_ROOM_PREFS_FILE_NAME"
    private const val ENCRYPTED_ROOM_PASSWORD_KEY = "ENCRYPTED_ROOM_PASSWORD_KEY"
    private const val ROOM_MASTER_KEY_ALIAS = "ROOM_DB_MASTER_KEY"
    private const val DEBUG_ENCRYPTION_PREFS_FILE_NAME = "DEBUG_ENCRYPTION_PREFS"
    private const val DEBUG_ENCRYPTION_FORCED_KEY = "DEBUG_ENCRYPTION_FORCED"
    private const val PassphraseSizeInBytes = 64

    fun getOpenHelperFactoryIfNeeded(context: Context): androidx.sqlite.db.SupportSQLiteOpenHelper.Factory? {
        // In debug builds, only encrypt if explicitly forced via the debug menu
        if (ModuleBuildConfig.DEBUG && !isDebugEncryptionForced(context)) return null

        val prefs = encryptedPrefs(context)
        val passphraseB64 = getOrCreatePassphrase(prefs)
        val passphraseBytes = Base64.decode(passphraseB64)

        // Ensure SQLCipher libs are loaded
        System.loadLibrary("sqlcipher")
        return SupportOpenHelperFactory(passphraseBytes)
    }

    /** Returns true if encryption has been manually forced on via the debug menu. */
    fun isDebugEncryptionForced(context: Context): Boolean =
        context.getSharedPreferences(DEBUG_ENCRYPTION_PREFS_FILE_NAME, Context.MODE_PRIVATE)
            .getBoolean(DEBUG_ENCRYPTION_FORCED_KEY, false)

    /** Enables or disables the debug encryption override flag. */
    fun setDebugEncryptionForced(context: Context, forced: Boolean) {
        context.getSharedPreferences(DEBUG_ENCRYPTION_PREFS_FILE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(DEBUG_ENCRYPTION_FORCED_KEY, forced)
            .commit()
    }

    /** Returns the stored passphrase as Base64, or null if none is stored yet. */
    fun getPassphraseBase64OrNull(context: Context): String? =
        runCatching { encryptedPrefs(context).getString(ENCRYPTED_ROOM_PASSWORD_KEY, null) }
            .getOrNull()
            .takeIf { !it.isNullOrEmpty() }

    /** Deletes the stored passphrase. The DB will be unreadable until it is re-created. */
    fun deletePassphrase(context: Context) {
        runCatching {
            encryptedPrefs(context).edit().remove(ENCRYPTED_ROOM_PASSWORD_KEY).commit()
        }
    }

    private fun encryptedPrefs(context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_ROOM_PREFS_FILE_NAME,
            MasterKey.Builder(context, ROOM_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun getOrCreatePassphrase(prefs: SharedPreferences): String {
        val existing = prefs.getString(ENCRYPTED_ROOM_PASSWORD_KEY, null)
        if (!existing.isNullOrEmpty()) return existing
        val phrase = ByteArray(PassphraseSizeInBytes).apply { SecureRandom().nextBytes(this) }
        val b64 = Base64.encode(phrase)
        prefs.edit().putString(ENCRYPTED_ROOM_PASSWORD_KEY, b64).apply()
        return b64
    }

    /**
     * Simple plaintext detection: If the DB file starts with "SQLite format 3", it is unencrypted.
     */
    fun isPlaintextSqlite(file: File): Boolean {
        return try {
            if (!file.exists() || file.length() < 16) return false
            file.inputStream().use { input ->
                val header = ByteArray(16)
                val read = input.read(header)
                if (read == 16) {
                    val text = String(header, Charsets.US_ASCII)
                    text.startsWith("SQLite format 3")
                } else false
            }
        } catch (_: Throwable) {
            false
        }
    }
}
