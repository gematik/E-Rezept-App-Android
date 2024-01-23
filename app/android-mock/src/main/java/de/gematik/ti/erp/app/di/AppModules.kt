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

package de.gematik.ti.erp.app.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.analytics.Analytics
import de.gematik.ti.erp.app.analytics.usecase.AnalyticsUseCase
import de.gematik.ti.erp.app.featuretoggle.FeatureToggleManager
import de.gematik.ti.erp.app.info.mockBuildConfigurationModule
import de.gematik.ti.erp.app.pkv.mockFileProviderAuthorityModule
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

private const val PREFERENCES_FILE_NAME = "appPrefs"
private const val NETWORK_SECURE_PREFS_FILE_NAME = "networkingSecurePrefs"
private const val NETWORK_PREFS_FILE_NAME = "networkingPrefs"
private const val MASTER_KEY_ALIAS = "netWorkMasterKey"

const val ApplicationPreferencesTag = "ApplicationPreferences"
const val NetworkPreferencesTag = "NetworkPreferences"
const val NetworkSecurePreferencesTag = "NetworkSecurePreferences"

val appModules = DI.Module("appModules") {
    bindSingleton<DispatchProvider> { object : DispatchProvider {} }
    bindSingleton(ApplicationPreferencesTag) {
        val context = instance<Context>()
        context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    }
    bindSingleton(NetworkPreferencesTag) {
        val context = instance<Context>()
        context.getSharedPreferences(NETWORK_PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }
    bindSingleton(NetworkSecurePreferencesTag) {
        val context = instance<Context>()

        EncryptedSharedPreferences.create(
            context,
            NETWORK_SECURE_PREFS_FILE_NAME,
            MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    bindSingleton { EndpointHelper(networkPrefs = instance(NetworkPreferencesTag)) }

    bindSingleton { FeatureToggleManager(instance()) }

    bindProvider { AnalyticsUseCase(instance()) }

    bindSingleton { Analytics(instance(), instance(), instance(), instance()) }

    importAll(
        mockBuildConfigurationModule,
        mockFileProviderAuthorityModule
    )
}
