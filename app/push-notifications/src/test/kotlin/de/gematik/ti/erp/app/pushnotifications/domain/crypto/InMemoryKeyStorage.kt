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
 */

package de.gematik.ti.erp.app.pushnotifications.domain.crypto

import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import java.util.concurrent.ConcurrentHashMap

class InMemoryKeyStorage : PushNotificationKeyStorage {
    private val store = ConcurrentHashMap<String, List<PushNotificationKeyGeneration>>()

    override suspend fun save(
        keyIdentifier: String,
        generations: List<PushNotificationKeyGeneration>
    ): Result<Unit> {
        store[keyIdentifier] = generations.toList()
        return Result.success(Unit)
    }

    override suspend fun load(keyIdentifier: String): Result<List<PushNotificationKeyGeneration>> =
        Result.success(store[keyIdentifier] ?: emptyList())

    override suspend fun clear(keyIdentifier: String): Result<Unit> {
        store.remove(keyIdentifier)
        return Result.success(Unit)
    }
}
