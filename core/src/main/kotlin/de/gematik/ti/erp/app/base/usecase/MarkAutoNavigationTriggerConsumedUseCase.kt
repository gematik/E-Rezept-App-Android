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

package de.gematik.ti.erp.app.base.usecase

import de.gematik.ti.erp.app.navigation.triggers.NavigationTriggerDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for marking a navigation trigger as consumed.
 *
 * This marks the navigation event as completed in the [NavigationTriggerDataStore],
 * preventing the same auto-navigation from occurring again. Once consumed, the trigger
 * remains set but will not cause navigation until it is reset.
 *
 * Typically used in combination with [TriggerNavigationUseCase] to implement one-time
 * navigation behavior, where both are called together to set the flag while immediately
 * preventing duplicate navigation.
 */
class MarkAutoNavigationTriggerConsumedUseCase(
    private val dataStore: NavigationTriggerDataStore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Marks the navigation trigger as consumed for the given identifier.
     *
     * @param id Unique identifier for the navigation trigger (e.g., feature flag name or event ID)
     */
    suspend operator fun invoke(id: String) {
        withContext(dispatcher) {
            dataStore.markAutoNavigationConsumed(id)
        }
    }
}
