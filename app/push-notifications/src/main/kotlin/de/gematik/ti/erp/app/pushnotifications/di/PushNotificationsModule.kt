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

package de.gematik.ti.erp.app.pushnotifications.di

import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.base.BaseConstants.applicationScope
import de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial.PushKeyMaterialLocalDataSource
import de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial.pushKeyMaterialLocalDataSource
import de.gematik.ti.erp.app.database.datastore.pushnotification.registration.PushRegistrationLocalDataSource
import de.gematik.ti.erp.app.database.datastore.pushnotification.registration.pushRegistrationLocalDataSource
import de.gematik.ti.erp.app.pushnotifications.BuildConfig
import de.gematik.ti.erp.app.pushnotifications.BuildConfig.PUSH_GATEWAY_URL_PU
import de.gematik.ti.erp.app.pushnotifications.BuildConfig.PUSH_GATEWAY_URL_RU
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.DefaultHkdfSha256
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.DefaultPushNotificationCryptoService
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.HkdfSha256
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainAdvancer
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainManager
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoService
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationKeyStorage
import de.gematik.ti.erp.app.pushnotifications.domain.model.IncomingPushNotificationMapper
import de.gematik.ti.erp.app.pushnotifications.domain.registration.PushRegistrationManager
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.AcceptPushNotificationPermissionUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.AdvancePushKeyChainUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.DeclinePushNotificationPermissionUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.DecryptPushNotificationUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.DeletePusherUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.GetPusherChannelsUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.GetPushersUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.InitializeDebugPushKeyChainUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.RegisterPushNotificationsForProfileUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.ShouldShowPushPermissionPromptUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.SyncProfilePushNotificationStateUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.UpdateFcmTokenUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.UpdateProfilePushNotificationSettingUseCase
import de.gematik.ti.erp.app.pushnotifications.provider.FcmTokenProvider
import de.gematik.ti.erp.app.pushnotifications.provider.FirebaseAppProjectIdProvider
import de.gematik.ti.erp.app.pushnotifications.provider.FirebaseMessagingTokenProvider
import de.gematik.ti.erp.app.pushnotifications.provider.FirebaseProjectIdProvider
import de.gematik.ti.erp.app.pushnotifications.provider.FirebasePushApplicationIdProvider
import de.gematik.ti.erp.app.pushnotifications.provider.PushApplicationIdProvider
import android.app.Application
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.firstOrNull
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.gematik.ti.erp.app.pushnotifications.provider.PushGatewayUrlProvider
import de.gematik.ti.erp.app.pushnotifications.repository.DefaultPusherRepository
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRemoteDataSource
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import de.gematik.ti.erp.app.pushnotifications.storage.DataStorePushNotificationKeyStorage
import de.gematik.ti.erp.app.pushnotifications.storage.DataStorePushRegistrationStorage
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

private val android.content.Context.debugPushNotificationsDataStore by preferencesDataStore("debug_push_notifications_prefs")

val pushNotificationsModule = DI.Module("pushNotificationsModule") {

    bindSingleton<HkdfSha256> { DefaultHkdfSha256() }

    bindSingleton<PushKeyMaterialLocalDataSource> { pushKeyMaterialLocalDataSource(instance<Application>()) }
    bindSingleton<PushRegistrationLocalDataSource> { pushRegistrationLocalDataSource(instance<Application>()) }

    bindSingleton<PushNotificationKeyStorage> {
        DataStorePushNotificationKeyStorage(localDataSource = instance())
    }

    bindSingleton<PushRegistrationStorage> {
        DataStorePushRegistrationStorage(localDataSource = instance())
    }

    // The tag keeps this stateful manager in the parent application DI instead of copying it into Activity DI.
    bindSingleton<PushKeyChainManager>(applicationScope) {
        PushKeyChainManager(
            hkdf = instance(),
            keyStorage = instance(),
            registrationStorage = instance()
        )
    }

    // Preserve untagged injection sites while resolving the shared application instance above.
    bindSingleton<PushKeyChainManager> { instance(tag = applicationScope) }
    bindSingleton<PushKeyChainAdvancer> { instance<PushKeyChainManager>() }

    bindSingleton<PushNotificationCryptoService> {
        DefaultPushNotificationCryptoService(keyChain = instance<PushKeyChainAdvancer>())
    }

    bindSingleton<FcmTokenProvider> { FirebaseMessagingTokenProvider() }
    bindSingleton<FirebaseProjectIdProvider> { FirebaseAppProjectIdProvider() }
    bindSingleton<PushApplicationIdProvider> { FirebasePushApplicationIdProvider() }

    bindSingleton<PushGatewayUrlProvider> {
        val context = instance<Application>()
        PushGatewayUrlProvider {
            if (BuildConfig.DEBUG) {
                val url = runBlocking {
                    val key = stringPreferencesKey("debug_push_gateway_url")
                    context.debugPushNotificationsDataStore.data.firstOrNull()?.get(key)
                }
                if (!url.isNullOrBlank()) url else PUSH_GATEWAY_URL_RU
            } else {
                PUSH_GATEWAY_URL_PU
            }
        }
    }

    // Reuse the VAU-enabled ERP service for authenticated push endpoints.
    bindSingleton<PusherRemoteDataSource> { PusherRemoteDataSource(instance<ErpService>()) }

    bindSingleton<PusherRepository> {
        DefaultPusherRepository(
            dataSource = instance(),
            pushApplicationIdProvider = instance(),
            pushGatewayUrlProvider = instance()
        )
    }

    bindProvider<IncomingPushNotificationMapper> { IncomingPushNotificationMapper() }
    bindProvider<DecryptPushNotificationUseCase> { DecryptPushNotificationUseCase(instance()) }
    bindProvider<AdvancePushKeyChainUseCase> { AdvancePushKeyChainUseCase(instance()) }
    bindProvider<InitializeDebugPushKeyChainUseCase> { InitializeDebugPushKeyChainUseCase(keyChainManager = instance()) }

    bindProvider<PushRegistrationManager> {
        PushRegistrationManager(
            repository = instance(),
            fcmTokenProvider = instance(),
            keyChainManager = instance(),
            registrationStorage = instance()
        )
    }

    bindProvider<RegisterPushNotificationsForProfileUseCase> {
        RegisterPushNotificationsForProfileUseCase(registrationManager = instance())
    }

    bindProvider<AcceptPushNotificationPermissionUseCase> {
        AcceptPushNotificationPermissionUseCase(
            registrationManager = instance(),
            registrationStorage = instance(),
            pusherRepository = instance()
        )
    }

    bindProvider<DeclinePushNotificationPermissionUseCase> {
        DeclinePushNotificationPermissionUseCase(registrationStorage = instance())
    }

    bindProvider<ShouldShowPushPermissionPromptUseCase> {
        ShouldShowPushPermissionPromptUseCase(registrationStorage = instance())
    }

    bindProvider<GetPusherChannelsUseCase> {
        GetPusherChannelsUseCase(
            registrationStorage = instance(),
            repository = instance()
        )
    }

    bindProvider<GetPushersUseCase> {
        GetPushersUseCase(
            repository = instance()
        )
    }

    bindProvider<DeletePusherUseCase> {
        DeletePusherUseCase(
            repository = instance()
        )
    }

    bindProvider<UpdateProfilePushNotificationSettingUseCase> {
        UpdateProfilePushNotificationSettingUseCase(
            registrationStorage = instance(),
            pusherRepository = instance(),
            registrationManager = instance()
        )
    }

    bindProvider<SyncProfilePushNotificationStateUseCase> {
        SyncProfilePushNotificationStateUseCase(
            registrationStorage = instance(),
            pusherRepository = instance(),
            fcmTokenProvider = instance(),
            pushApplicationIdProvider = instance(),
            keyChainManager = instance(),
            registrationManager = instance()
        )
    }

    bindProvider<UpdateFcmTokenUseCase> {
        UpdateFcmTokenUseCase(
            registrationManager = instance(),
            registrationStorage = instance(),
            idpUseCase = instance(),
            pusherRepository = instance(),
            fcmTokenProvider = instance()
        )
    }
}
