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

package de.gematik.ti.erp.app.di

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Looper
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.base.BaseConstants.applicationScope
import de.gematik.ti.erp.app.base.usecase.IsFeatureToggleEnabledUseCase
import de.gematik.ti.erp.app.database.api.SettingsLocalDataSource
import de.gematik.ti.erp.app.database.api.debug.Android13DeprecationLocalDataSource
import de.gematik.ti.erp.app.database.api.debug.CommunicationDigaVersionLocalDataSource
import de.gematik.ti.erp.app.database.api.debug.CommunicationVersionLocalDataSource
import de.gematik.ti.erp.app.database.api.debug.ConsentVersionLocalDataSource
import de.gematik.ti.erp.app.database.api.debug.DbMigrationLogsLocalDataSource
import de.gematik.ti.erp.app.database.api.debug.EuVersionLocalDataSource
import de.gematik.ti.erp.app.database.api.debug.VirtualHealthCardLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.dbMigrationLogsLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.versions.communication.communicationVersionLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.versions.communicationdiga.communicationDigaVersionLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.versions.consent.consentVersionLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.versions.deprecation.android13DeprecationLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.versions.eu.euVersionLocalDataSource
import de.gematik.ti.erp.app.database.datastore.featuretoggle.IsRoomEnabled
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.database.datastore.featuretoggle.featureToggleLocalDataSource
import de.gematik.ti.erp.app.database.datastore.settings.SettingsLocalDataSourceV2
import de.gematik.ti.erp.app.database.datastore.settings.settingsLocalDataSource
import de.gematik.ti.erp.app.database.datastore.virtualhealthcard.virtualHealthCardLocalDataSource
import de.gematik.ti.erp.app.database.di.ModuleTags
import de.gematik.ti.erp.app.database.migration.DataMigrator
import de.gematik.ti.erp.app.datastore.featuretoggle.DefaultFeatureToggleRepository
import de.gematik.ti.erp.app.datastore.featuretoggle.FeatureToggleRepository
import de.gematik.ti.erp.app.info.di.buildConfigInformationModule
import de.gematik.ti.erp.app.migration.di.dataMigrationModule
import de.gematik.ti.erp.app.navigation.triggers.DefaultNavigationTriggerDataStore
import de.gematik.ti.erp.app.navigation.triggers.NavigationTriggerDataStore
import de.gematik.ti.erp.app.pkv.fileProviderAuthorityModule
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
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
    bindProvider<Resources> {
        val context = instance<Context>()
        context.resources
    }
    bindProvider<AssetManager> {
        val context = instance<Context>()
        context.assets
    }
    bindProvider<Looper> {
        val context = instance<Context>()
        context.mainLooper
    }
    // A scope for the whole application
    bindSingleton<CoroutineScope>(applicationScope) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
    bindSingleton(ApplicationPreferencesTag) {
        val context = instance<Context>()
        runBlocking(Dispatchers.IO) {
            context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        }
    }
    bindSingleton(NetworkPreferencesTag) {
        val context = instance<Context>()
        runBlocking(Dispatchers.IO) {
            context.getSharedPreferences(NETWORK_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        }
    }
    bindSingleton(NetworkSecurePreferencesTag) {
        val context = instance<Context>()

        @Requirement(
            "O.Arch_2#1",
            "O.Data_2#4",
            "O.Data_3#4",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Data storage using EncryptedSharedPreferences."
        )
        runBlocking(Dispatchers.IO) {
            EncryptedSharedPreferences.create(
                context,
                NETWORK_SECURE_PREFS_FILE_NAME,
                MasterKey.Builder(context, MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    bindSingleton { EndpointHelper(networkPrefs = instance(NetworkPreferencesTag)) }
    // data store
    // feature toggle
    bindSingleton<FeatureToggleRepository> { DefaultFeatureToggleRepository(instance()) }
    bindSingleton<RoomFeatureToggle>(tag = IsRoomEnabled) {
        RoomFeatureToggle {
            runBlocking {
                val settingsLocalDataSource = instance<SettingsLocalDataSource>(tag = ModuleTags.SETTINGS_V2)
                val isPorted = settingsLocalDataSource
                    .isDataPortedToRoom()
                    .firstOrNull() ?: false
                when {
                    isPorted -> true
                    else -> {
                        val migrationRequired = runCatching {
                            instance<DataMigrator>().isMigrationRequired()
                        }.getOrDefault(true)
                        !migrationRequired
                    }
                }
            }
        }
    }
    bindSingleton { featureToggleLocalDataSource(instance(), BuildConfigExtension.isInternalDebug) }
    bindProvider { IsFeatureToggleEnabledUseCase(instance()) }
    bindSingleton<SettingsLocalDataSourceV2> { settingsLocalDataSource(instance()) }

    bindSingleton<Android13DeprecationLocalDataSource> { android13DeprecationLocalDataSource(instance()) }
    // DEBUG ONLY
    bindSingleton<ConsentVersionLocalDataSource> { consentVersionLocalDataSource(instance()) }
    bindSingleton<CommunicationVersionLocalDataSource> { communicationVersionLocalDataSource(instance()) }
    bindSingleton<CommunicationDigaVersionLocalDataSource> { communicationDigaVersionLocalDataSource(instance()) }
    bindSingleton<EuVersionLocalDataSource> { euVersionLocalDataSource(instance()) }
    bindSingleton<DbMigrationLogsLocalDataSource> { dbMigrationLogsLocalDataSource(instance()) }
    bindSingleton<VirtualHealthCardLocalDataSource> { virtualHealthCardLocalDataSource(instance()) }

    bindSingleton<NavigationTriggerDataStore> { DefaultNavigationTriggerDataStore(instance()) }
    importAll(
        buildConfigInformationModule,
        fileProviderAuthorityModule,
        dataMigrationModule
    )
}
