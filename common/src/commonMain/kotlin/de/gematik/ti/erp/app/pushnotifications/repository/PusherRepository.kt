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
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.model.Pusher

interface PusherRepository {
    suspend fun getPushers(profileId: ProfileIdentifier): Result<List<Pusher>>

    suspend fun getChannels(
        pushKey: String,
        profileId: ProfileIdentifier
    ): Result<List<PushChannel>>

    suspend fun registerDevice(
        pushKey: String,
        iss: String,
        keyIdentifier: String,
        timeIssCreated: String,
        deviceName: String,
        profileId: ProfileIdentifier
    ): Result<Unit>

    suspend fun deregister(pushKey: String, profileId: ProfileIdentifier): Result<Unit>

    /**
     * Deletes a registered pusher using the given [appId], unlike [deregister] which always
     * uses the current device's app id. This allows removing pushers registered by other
     * devices/ platforms.
     */
    suspend fun deletePusher(
        pushKey: String,
        appId: String,
        profileId: ProfileIdentifier
    ): Result<Unit>

    suspend fun setChannels(
        pushKey: String,
        channels: List<PushChannel>,
        profileId: ProfileIdentifier
    ): Result<Unit>
}
