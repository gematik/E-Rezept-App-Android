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
 */

package de.gematik.ti.erp.app.pushnotifications.repository

import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.model.PushChannelRequest
import de.gematik.ti.erp.app.pushnotifications.model.Pusher
import de.gematik.ti.erp.app.pushnotifications.model.PusherDeregistrationRequest
import de.gematik.ti.erp.app.pushnotifications.model.PusherRegistrationRequest

/** Calls the Fachdienst push API through VAU-enabled [ErpService]. */
class PusherRemoteDataSource(
    private val service: ErpService
) {
    suspend fun setPusher(
        profileId: ProfileIdentifier,
        request: PusherRegistrationRequest
    ): Result<Unit> =
        safeApiCall("Error setting Fachdienst pusher.") {
            service.setPusher(profileId, request)
        }.map { Unit }

    suspend fun getChannels(
        profileId: ProfileIdentifier,
        pushKey: String
    ): Result<List<PushChannel>> =
        safeApiCall("Error getting Fachdienst push channels.") {
            service.getPusherChannels(profileId, pushKey)
        }.map { it.channels }

    suspend fun getPushers(profileId: ProfileIdentifier): Result<List<Pusher>> =
        safeApiCall("Error getting Fachdienst pushers.") {
            service.getPushers(profileId)
        }.map { it.pushers }

    suspend fun deletePusher(
        profileId: ProfileIdentifier,
        appId: String,
        pushKey: String
    ): Result<Unit> {
        val request = PusherDeregistrationRequest(pushKey = pushKey, appId = appId)
        return safeApiCall("Error deleting Fachdienst pusher.") {
            service.deletePusher(profileId, request)
        }.map { Unit }
    }

    suspend fun setChannels(
        profileId: ProfileIdentifier,
        pushKey: String,
        channels: List<PushChannel>
    ): Result<Unit> =
        safeApiCall("Error setting Fachdienst push channels.") {
            service.setPusherChannels(profileId, pushKey, PushChannelRequest(channels))
        }.map { Unit }
}
