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

package de.gematik.ti.erp.app.database.di

import com.russhwolf.settings.Settings
import de.gematik.ti.erp.app.database.api.PharmacyLocalDataSource
import de.gematik.ti.erp.app.database.api.ShippingInfoLocalDataSource
import de.gematik.ti.erp.app.database.api.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.database.api.TaskLocalDataSource
import de.gematik.ti.erp.app.database.api.TrustStoreLocalDataSource
import de.gematik.ti.erp.app.database.bridge.accesstoken.PharmacySearchAccessTokenLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.pharmacy.PharmacyLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.shipping.ShippingInfoLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.task.TaskLocalDataSourceBridge
import de.gematik.ti.erp.app.database.bridge.truststore.TrustStoreLocalDataSourceBridge
import de.gematik.ti.erp.app.database.datastore.featuretoggle.IsRoomEnabled
import de.gematik.ti.erp.app.database.realm.v1.pharmacy.PharmacyLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.shipping.ShippingInfoLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.pharmacy.PharmacySearchAccessTokenLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.task.datasource.TaskLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.truststore.TrustStoreLocalDataSourceV1
import de.gematik.ti.erp.app.database.room.roomModule
import de.gematik.ti.erp.app.database.room.v2.accesstoken.PharmacySearchAccessTokenLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.datasource.TaskLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.pharmacy.PharmacyLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.shippinginfo.ShippingInfoLocalDataSourceV2
import de.gematik.ti.erp.app.database.room.v2.truststore.TrustStoreLocalDataSourceV2
import de.gematik.ti.erp.app.database.settings.CommunicationDigaVersionDataStore
import de.gematik.ti.erp.app.database.settings.CommunicationVersionDataStore
import de.gematik.ti.erp.app.database.settings.ConsentVersionDataStore
import de.gematik.ti.erp.app.database.settings.DefaultCommunicationDigaVersionPreferencesDataStore
import de.gematik.ti.erp.app.database.settings.DefaultCommunicationVersionPreferencesDataStore
import de.gematik.ti.erp.app.database.settings.DefaultConsentVersionPreferencesDataStore
import de.gematik.ti.erp.app.database.settings.DefaultEuVersionPreferencesDataStore
import de.gematik.ti.erp.app.database.settings.EuVersionDataStore
import de.gematik.ti.erp.app.database.settings.SettingsDataMigration
import de.gematik.ti.erp.app.database.settings.SettingsLocalDataSource
import de.gematik.ti.erp.app.database.settings.ThemePreferencesDataStore
import de.gematik.ti.erp.app.database.settings.sharedPrefs
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

/**
 * Provides Kodein bindings for task-related local data sources, supporting both legacy (V1)
 * and new (V2) implementations. In debug mode, it wires a bridge implementation that compares
 * data between V1 and V2 for validation and migration purposes.
 *
 * @param IsRoomEnabled Indicates whether the app is running in debug mode. If true, a bridge
 * implementation (`LocalDataSourceBridge`) is bound to the default `LocalDataSource`
 * interface to enable runtime comparison between V1 and V2. In release mode, only V1 is expected
 * to be used.
 *
 * @return A Kodein `DI.Module` containing bindings for V1, V2, and bridge data sources.
 */
fun databaseModule() = DI.Module("databaseModule", allowSilentOverride = true) {
    // Settings
    bindSingleton<Settings> { sharedPrefs }

    // DEBUG ONLY: Theme selector to switch between different UI themes
    bindSingleton { ThemePreferencesDataStore(sharedPrefs) }

    // DEBUG ONLY: Consent version selector for testing different versions
    bindSingleton<ConsentVersionDataStore> { DefaultConsentVersionPreferencesDataStore(sharedPrefs) }

    // DEBUG ONLY: Communication version selector for testing different versions
    bindSingleton<CommunicationVersionDataStore> { DefaultCommunicationVersionPreferencesDataStore(sharedPrefs) }

    // DEBUG ONLY: Communication DiGA version selector for testing different DiGA versions
    bindSingleton<CommunicationDigaVersionDataStore> { DefaultCommunicationDigaVersionPreferencesDataStore(sharedPrefs) }

    // DEBUG ONLY: Eu version selector for testing different versions
    bindSingleton<EuVersionDataStore> { DefaultEuVersionPreferencesDataStore(sharedPrefs) }

    // task module
    bindProvider<TaskLocalDataSource>(tag = ModuleTags.TASK_V1) { TaskLocalDataSourceV1(instance()) }
    bindProvider<TaskLocalDataSource>(tag = ModuleTags.TASK_V2) { TaskLocalDataSourceV2() }
    bindProvider<TaskLocalDataSource> {
        TaskLocalDataSourceBridge(
            instance(tag = ModuleTags.TASK_V1),
            instance(tag = ModuleTags.TASK_V2),
            instance(tag = IsRoomEnabled)
        )
    }

    // pharmacy module
    bindProvider<PharmacyLocalDataSource>(tag = ModuleTags.PHARMACY_V1) { PharmacyLocalDataSourceV1(instance()) }

    bindProvider<PharmacyLocalDataSource>(tag = ModuleTags.PHARMACY_V2) { PharmacyLocalDataSourceV2(instance()) }
    // Bridge will compare V1 vs V2 and may prefer V2 depending on flag
    bindProvider<PharmacyLocalDataSource> {
        PharmacyLocalDataSourceBridge(
            instance(tag = ModuleTags.PHARMACY_V1),
            instance(tag = ModuleTags.PHARMACY_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    // shipping info module
    bindProvider<ShippingInfoLocalDataSource>(tag = ModuleTags.SHIPPING_INFO_V1) { ShippingInfoLocalDataSourceV1(instance()) }
    bindProvider<ShippingInfoLocalDataSource>(tag = ModuleTags.SHIPPING_INFO_V2) { ShippingInfoLocalDataSourceV2(instance()) }
    bindProvider<ShippingInfoLocalDataSource> {
        ShippingInfoLocalDataSourceBridge(
            instance(tag = ModuleTags.SHIPPING_INFO_V1),
            instance(tag = ModuleTags.SHIPPING_INFO_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }
    // truststore module
    bindProvider<TrustStoreLocalDataSource>(tag = ModuleTags.TRUSTSTORE_V1) { TrustStoreLocalDataSourceV1(instance()) }
    bindProvider<TrustStoreLocalDataSource>(tag = ModuleTags.TRUSTSTORE_V2) { TrustStoreLocalDataSourceV2(instance()) }
    // Bridge will compare V1 vs V2 and may prefer V2 depending on flag
    bindProvider<TrustStoreLocalDataSource> {
        TrustStoreLocalDataSourceBridge(
            instance(tag = ModuleTags.TRUSTSTORE_V1),
            instance(tag = ModuleTags.TRUSTSTORE_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }
    // PharmacySearchAccessToken module
    bindProvider<PharmacySearchAccessTokenLocalDataSource>(tag = ModuleTags.SEARCH_ACCESS_TOKEN_V1) {
        PharmacySearchAccessTokenLocalDataSourceV1(instance())
    }
    bindProvider<PharmacySearchAccessTokenLocalDataSource>(tag = ModuleTags.SEARCH_ACCESS_TOKEN_V2) {
        PharmacySearchAccessTokenLocalDataSourceV2(instance())
    }
    // Bridge will compare V1 vs V2 and may prefer V2 depending on flag
    bindProvider<PharmacySearchAccessTokenLocalDataSource> {
        PharmacySearchAccessTokenLocalDataSourceBridge(
            instance(tag = ModuleTags.SEARCH_ACCESS_TOKEN_V1),
            instance(tag = ModuleTags.SEARCH_ACCESS_TOKEN_V2),
            instance(),
            instance(tag = IsRoomEnabled)
        )
    }

    import(roomModule)

    bindSingleton { SettingsDataMigration(realm = instance(), settings = sharedPrefs) }
    bindSingleton { SettingsLocalDataSource(sharedPrefs) }
}
