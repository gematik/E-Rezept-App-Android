/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.db.appSchemas
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.openRealmWith
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.secureRandomInstance
import io.realm.kotlin.exceptions.RealmException
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

// TODO: Does not get printed in technical requirements
val realmModule = DI.Module("realmModule") {
    @Requirement(
        "O.Arch_4#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Implementation of data storage"
    )
    @Requirement(
        "O.Data_14#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "All data is encrypted irrespective of the data type and lock state."
    )
    @Requirement(
        "O.Data_14#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "All data is encrypted irrespective of the data type and lock state."
    )
    bindSingleton(RealmDatabaseSecurePreferencesTag) {
        val context = instance<Context>()

        @Requirement(
            "O.Arch_2#1",
            "O.Data_2#1",
            "O.Data_3#1",
            "O.Purp_8#1",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Data storage using EncryptedSharedPreferences."
        )
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

        @Requirement(
            "O.Arch_2#2",
            "O.Arch_4#1",
            "O.Data_2#4",
            "O.Data_3#4",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Database configuration with encryption key. " +
                "The key is stored in the encrypted shared preferences."
        )
        try {
            val context = instance<Context>()
            val profileName = context.resources.getString(R.string.onboarding_default_profile_name)
            openRealmWith(
                schemas = appSchemas(profileName = profileName),
                configuration = {
                    it.encryptionKey(Base64.decode(getPassphrase(securePrefs)))
                }
            ).also { realm ->
                realm.writeBlocking {
                    queryFirst<SettingsEntityV1>()?.let {
                        it.latestAppVersionName = BuildKonfig.VERSION_NAME
                        it.latestAppVersionCode = BuildKonfig.VERSION_CODE
                    }
                }
            }
        } catch (expected: Throwable) {
            throw RealmException("exception on module start", expected)
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

@Requirement(
    "O.Data_4#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Database access is done via prepared statements featured by Realm Database."
)
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
