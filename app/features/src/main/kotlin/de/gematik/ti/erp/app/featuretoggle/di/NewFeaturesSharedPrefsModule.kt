/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.featuretoggle.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.gematik.ti.erp.app.featuretoggle.datasource.NewFeaturesLocalDataSource
import de.gematik.ti.erp.app.featuretoggle.repository.DefaultNewFeaturesRepository
import de.gematik.ti.erp.app.featuretoggle.repository.NewFeaturesRepository
import de.gematik.ti.erp.app.featuretoggle.usecase.IsNewFeatureSeenUseCase
import de.gematik.ti.erp.app.featuretoggle.usecase.MarkNewFeatureSeenUseCase
import de.gematik.ti.erp.app.featuretoggle.usecase.SetNewFeatureDefaultsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

private const val ENCRYPTED_PREFS_FILE_NAME = "new_features_encrypted_shared_prefs"
private const val ENCRYPTED_PREFS_MASTER_KEY_ALIAS = "NEW_FEATURES_ENCRYPTED_PREFS_MASTER_KEY_ALIAS"

private const val ENCRYPTED_SHARED_PREFS_TAG = "NewFeaturesEncryptedSharedPrefsTag"

val newFeaturesSharedPrefsModule = DI.Module("newFeaturesSharedPrefsModule") {
    // encrypted key for the shared preferences
    bindSingleton(ENCRYPTED_PREFS_MASTER_KEY_ALIAS) {
        val context = instance<Context>()
        val masterKey = MasterKey.Builder(context, ENCRYPTED_PREFS_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        masterKey
    }
    // shared preferences
    bindSingleton(ENCRYPTED_SHARED_PREFS_TAG) {
        val context = instance<Context>()
        val masterKey = instance<MasterKey>(ENCRYPTED_PREFS_MASTER_KEY_ALIAS)

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    // data source
    bindProvider {
        val sharedPreferences = instance<SharedPreferences>(ENCRYPTED_SHARED_PREFS_TAG)
        NewFeaturesLocalDataSource(sharedPreferences)
    }
    // repository
    bindProvider<NewFeaturesRepository> {
        DefaultNewFeaturesRepository(instance())
    }
    // use-cases
    bindProvider { IsNewFeatureSeenUseCase(instance()) }
    bindProvider { MarkNewFeatureSeenUseCase(instance()) }
    bindProvider {
        SetNewFeatureDefaultsUseCase(instance()).apply { CoroutineScope(Dispatchers.IO).launch { invoke() } }
    }
}
