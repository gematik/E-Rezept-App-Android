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

package de.gematik.ti.erp.app.appupdate.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.appupdate.datasource.local.AppUpdateManagerSelectionLocalDataSource
import de.gematik.ti.erp.app.appupdate.repository.AppUpdateFlagRepository
import de.gematik.ti.erp.app.appupdate.repository.AppUpdateManagerSelectionRepository
import de.gematik.ti.erp.app.appupdate.repository.DefaultAppUpdateManagerSelectionRepository
import de.gematik.ti.erp.app.appupdate.usecase.AppUpdateInfoUseCase
import de.gematik.ti.erp.app.appupdate.usecase.ChangeAppUpdateFlagUseCase
import de.gematik.ti.erp.app.appupdate.usecase.ChangeAppUpdateManagerFlagUseCase
import de.gematik.ti.erp.app.appupdate.usecase.GetAppUpdateFlagUseCase
import de.gematik.ti.erp.app.appupdate.usecase.GetAppUpdateManagerFlagUseCase
import de.gematik.ti.erp.app.appupdate.usecase.GetAppUpdateManagerUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

private const val ENCRYPTED_PREFS_FILE_NAME = "Encrypted_app_update_shared_prefs"
private const val ENCRYPTED_PREFS_MASTER_KEY_ALIAS = "ENCRYPTED_APP_UPDATE_PREFS_MASTER_KEY_ALIAS"

private const val ENCRYPTED_SHARED_PREFS_TAG = "EncryptedAppUpdateSharedPrefsTag"

val appUpdateModule = DI.Module("appUpdateModule") {
    bindSingleton(ENCRYPTED_PREFS_MASTER_KEY_ALIAS) {
        val context = instance<Context>()
        val masterKey = MasterKey.Builder(context, ENCRYPTED_PREFS_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        masterKey
    }
    bindSingleton(ENCRYPTED_SHARED_PREFS_TAG) {
        val context = instance<Context>()
        val masterKey = instance<MasterKey>(ENCRYPTED_PREFS_MASTER_KEY_ALIAS)

        @Requirement(
            "O.Data_2#2",
            "O.Data_3#2",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Data storage using EncryptedSharedPreferences."
        )
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    bindProvider {
        val sharedPreferences = instance<SharedPreferences>(ENCRYPTED_SHARED_PREFS_TAG)
        AppUpdateManagerSelectionLocalDataSource(sharedPreferences)
    }
    bindProvider<AppUpdateManagerSelectionRepository> {
        DefaultAppUpdateManagerSelectionRepository(instance())
    }
    bindSingleton { AppUpdateFlagRepository() }
    bindProvider { AppUpdateInfoUseCase() }
    bindProvider { ChangeAppUpdateFlagUseCase(instance()) }
    bindProvider { ChangeAppUpdateManagerFlagUseCase(instance()) }
    bindProvider { GetAppUpdateFlagUseCase(instance()) }
    bindProvider { GetAppUpdateManagerFlagUseCase(instance()) }
    bindProvider { GetAppUpdateManagerUseCase(instance()) }
}
