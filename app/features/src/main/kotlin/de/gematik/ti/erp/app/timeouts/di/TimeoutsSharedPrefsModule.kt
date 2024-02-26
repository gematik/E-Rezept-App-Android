/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.timeouts.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutsLocalDataSource
import de.gematik.ti.erp.app.timeouts.presentation.TimeoutsScreenViewModel
import de.gematik.ti.erp.app.timeouts.presentation.DefaultTimeoutsScreenViewModel
import de.gematik.ti.erp.app.timeouts.repository.TimeoutRepository
import de.gematik.ti.erp.app.timeouts.repository.DefaultTimeoutRepository
import de.gematik.ti.erp.app.timeouts.usecase.GetInactivityMetricUseCase
import de.gematik.ti.erp.app.timeouts.usecase.GetPauseMetricUseCase
import de.gematik.ti.erp.app.timeouts.usecase.SetInactivityMetricUseCase
import de.gematik.ti.erp.app.timeouts.usecase.SetPauseMetricUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

private const val ENCRYPTED_PREFS_FILE_NAME = "Encrypted_shared_prefs"
private const val ENCRYPTED_PREFS_MASTER_KEY_ALIAS = "ENCRYPTED_PREFS_MASTER_KEY_ALIAS"

private const val ENCRYPTED_SHARED_PREFS_TAG = "EncryptedSharedPrefsTag"

val timeoutsSharedPrefsModule = DI.Module("sharedPrefsModule") {
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
        TimeoutsLocalDataSource(sharedPreferences)
    }
    bindProvider<TimeoutRepository> {
        DefaultTimeoutRepository(instance()).also {
            // set the initial timeouts for the app
            if (!it.areTimeoutsExisting()) {
                it.setDefaultTimeouts()
            }
        }
    }
    bindProvider { GetInactivityMetricUseCase(instance()) }
    bindProvider { GetPauseMetricUseCase(instance()) }
    bindProvider { SetInactivityMetricUseCase(instance()) }
    bindProvider { SetPauseMetricUseCase(instance()) }
    bindProvider<TimeoutsScreenViewModel> {
        DefaultTimeoutsScreenViewModel(instance(), instance(), instance(), instance())
    }
}
