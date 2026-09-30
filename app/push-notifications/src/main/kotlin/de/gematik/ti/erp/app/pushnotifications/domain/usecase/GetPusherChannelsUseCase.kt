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
 */

package de.gematik.ti.erp.app.pushnotifications.domain.usecase

import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads the Fachdienst's actual channel state for the current device pusher.
 */
class GetPusherChannelsUseCase(
    private val registrationStorage: PushRegistrationStorage,
    private val repository: PusherRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(profileId: ProfileIdentifier): Result<List<PushChannel>> = withContext(dispatcher) {
        try {
            val pushKey = requireNotNull(registrationStorage.load(profileId).getOrThrow()?.fcmToken) {
                "Fachdienst channels cannot be read before the device pusher is registered."
            }
            Result.success(repository.getChannels(pushKey = pushKey, profileId = profileId).getOrThrow())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
