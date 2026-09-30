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

package de.gematik.ti.erp.app.pushnotifications.domain.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.model.Pusher
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetPushersUseCase(
    private val repository: PusherRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @Requirement(
        "A_27664",
        sourceSpecification = "gemF_PushNotification",
        rationale = """
            Retrieves the list of all pushers registered for the authenticated profile via
            GET /pushers/v1. Used in the 'Registered devices' screen to show which devices
            receive push notifications.
        """,
        codeLines = 4
    )
    suspend operator fun invoke(profileId: ProfileIdentifier): Result<List<Pusher>> =
        withContext(dispatcher) {
            repository.getPushers(profileId)
        }
}
