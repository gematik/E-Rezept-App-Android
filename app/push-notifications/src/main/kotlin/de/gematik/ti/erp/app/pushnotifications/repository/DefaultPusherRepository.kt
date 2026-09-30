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

package de.gematik.ti.erp.app.pushnotifications.repository

import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.BuildConfig
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.model.Pusher
import de.gematik.ti.erp.app.pushnotifications.model.PusherData
import de.gematik.ti.erp.app.pushnotifications.model.PusherEncryption
import de.gematik.ti.erp.app.pushnotifications.model.PusherRegistrationRequest
import de.gematik.ti.erp.app.pushnotifications.provider.PushApplicationIdProvider
import de.gematik.ti.erp.app.pushnotifications.provider.PushGatewayUrlProvider
import io.github.aakira.napier.Napier

class DefaultPusherRepository(
    private val dataSource: PusherRemoteDataSource,
    private val pushApplicationIdProvider: PushApplicationIdProvider,
    private val pushGatewayUrlProvider: PushGatewayUrlProvider = PushGatewayUrlProvider {
        if (BuildConfig.DEBUG) BuildConfig.PUSH_GATEWAY_URL_RU
        else BuildConfig.PUSH_GATEWAY_URL_PU
    }
) : PusherRepository {

    override suspend fun getPushers(profileId: ProfileIdentifier): Result<List<Pusher>> =
        dataSource.getPushers(profileId)

    override suspend fun getChannels(
        pushKey: String,
        profileId: ProfileIdentifier
    ): Result<List<PushChannel>> = dataSource.getChannels(profileId, pushKey)

    override suspend fun registerDevice(
        pushKey: String,
        iss: String,
        keyIdentifier: String,
        timeIssCreated: String,
        deviceName: String,
        profileId: ProfileIdentifier
    ): Result<Unit> {
        val appId = pushApplicationIdProvider.getPushApplicationId()
        val gatewayUrl = pushGatewayUrlProvider.getPushGatewayUrl()
        val request = PusherRegistrationRequest(
            pushKey = pushKey,
            kind = "http",
            appId = appId,
            appDisplayName = BuildConfig.APP_DISPLAY_NAME,
            deviceDisplayName = deviceName,
            lang = "de",
            data = PusherData(url = gatewayUrl),
            encryption = PusherEncryption(
                method = BuildConfig.ENCRYPTION_METHOD,
                timeIssCreated = timeIssCreated,
                iss = iss,
                keyIdentifier = keyIdentifier
            ),
            append = true
        )
        Napier.d(tag = "PusherRegistration", message = "Registering pusher -> app_id: $appId, url: $gatewayUrl")
        return dataSource.setPusher(profileId, request)
    }

    override suspend fun deregister(pushKey: String, profileId: ProfileIdentifier): Result<Unit> =
        dataSource.deletePusher(
            profileId = profileId,
            appId = pushApplicationIdProvider.getPushApplicationId(),
            pushKey = pushKey
        )

    override suspend fun deletePusher(
        pushKey: String,
        appId: String,
        profileId: ProfileIdentifier
    ): Result<Unit> =
        dataSource.deletePusher(
            profileId = profileId,
            appId = appId,
            pushKey = pushKey
        )

    override suspend fun setChannels(
        pushKey: String,
        channels: List<PushChannel>,
        profileId: ProfileIdentifier
    ): Result<Unit> = dataSource.setChannels(profileId, pushKey, channels)
}
