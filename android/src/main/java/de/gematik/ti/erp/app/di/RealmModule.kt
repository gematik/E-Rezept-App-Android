/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.di

import android.content.Context
import android.content.SharedPreferences

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.MessageConversionException
import de.gematik.ti.erp.app.db.appSchemas
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.openRealmWith
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.secureRandomInstance
import org.jose4j.base64url.Base64
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance

private const val ENCRYPTED_REALM_PREFS_FILE_NAME = "ENCRYPTED_REALM_PREFS_FILE_NAME"
private const val ENCRYPTED_REALM_PASSWORD_KEY = "ENCRYPTED_REALM_PASSWORD_KEY"
private const val REALM_MASTER_KEY_ALIAS = "REALM_DB_MASTER_KEY"

private const val PassphraseSizeInBytes = 64

const val RealmDatabaseSecurePreferencesTag = "RealmDatabaseSecurePreferences"

val realmModule = DI.Module("realmModule") {
    bindSingleton(RealmDatabaseSecurePreferencesTag) {
        val context = instance<Context>()

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_REALM_PREFS_FILE_NAME,
            MasterKey.Builder(context, REALM_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    bindEagerSingleton {
        val securePrefs = instance<SharedPreferences>(RealmDatabaseSecurePreferencesTag)

        try {
            openRealmWith(
                schemas = appSchemas,
                configuration = {
                    it.encryptionKey(Base64.decode(getPassphrase(securePrefs)))
                }
            ).also { realm ->
                realm.writeBlocking {
                    queryFirst<SettingsEntityV1>()?.let {
                        it.latestAppVersionName = BuildConfig.VERSION_NAME
                        it.latestAppVersionCode = BuildConfig.VERSION_CODE
                    }
                }
            }
        } catch (expected: Throwable) {
            throw MessageConversionException(expected)
        }
    }
}

private fun getPassphrase(securePrefs: SharedPreferences): String {
    if (getPassword(securePrefs).isNullOrEmpty()) {
        val passPhrase = generatePassPhrase()
        storePassPhrase(securePrefs, passPhrase)
    }
    return getPassword(securePrefs)
        ?: throw IllegalStateException("passphrase should not be empty")
}

private fun generatePassPhrase(): String {
    val passPhrase = ByteArray(PassphraseSizeInBytes).apply {
        secureRandomInstance().nextBytes(this)
    }
    return Base64.encode(passPhrase)
}

private fun storePassPhrase(
    securePrefs: SharedPreferences,
    passPhrase: String
) {
    securePrefs.edit().putString(
        ENCRYPTED_REALM_PASSWORD_KEY,
        passPhrase
    )
        .apply()
}

private fun getPassword(securePrefs: SharedPreferences): String? {
    return securePrefs.getString(
        ENCRYPTED_REALM_PASSWORD_KEY,
        null
    )
}
