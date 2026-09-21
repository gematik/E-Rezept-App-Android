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

import android.app.Application
import de.gematik.ti.erp.app.pushnotifications.BuildConfig
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.DefaultHkdfSha256
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.DefaultPushNotificationCryptoService
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.EncryptedSharedPreferencesKeyStorage
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.HkdfSha256
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainAdvancer
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoService
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationKeyRotationService
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationKeyStorage
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.AdvancePushKeyChainUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.DecryptPushNotificationUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.GetFcmTokenUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.GetFirebaseProjectIdUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.GetProfilePushNotificationSettingsUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.SaveProfilePushNotificationSettingUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val pushNotificationsModule = DI.Module("pushNotificationsModule") {

    bindSingleton<HkdfSha256> { DefaultHkdfSha256() }

    bindSingleton<PushNotificationKeyStorage> {
        EncryptedSharedPreferencesKeyStorage(context = instance<Application>())
    }

    // TODO: Replace these debug test values with app-generated push registration values once the registration flow is wired up.
    bindSingleton {
        PushNotificationKeyRotationService(
            initialSharedSecret = BuildConfig.PUSH_NOTIFICATION_DEBUG_INITIAL_SHARED_SECRET,
            timeIssCreated = BuildConfig.PUSH_NOTIFICATION_DEBUG_TIME_ISS_CREATED,
            keyIdentifier = BuildConfig.PUSH_NOTIFICATION_DEBUG_KEY_IDENTIFIER,
            hkdf = instance(),
            storage = instance()
        )
    }

    bindSingleton<PushKeyChainAdvancer> { instance<PushNotificationKeyRotationService>() }

    bindSingleton<PushNotificationCryptoService> {
        DefaultPushNotificationCryptoService(keyRotationService = instance())
    }

    bindProvider { GetProfilePushNotificationSettingsUseCase(instance()) }
    bindProvider { SaveProfilePushNotificationSettingUseCase(instance()) }
    bindProvider { GetFcmTokenUseCase() }
    bindProvider { GetFirebaseProjectIdUseCase() }
    bindProvider { DecryptPushNotificationUseCase(instance()) }
    bindProvider { AdvancePushKeyChainUseCase(instance<PushKeyChainAdvancer>()) }
}
