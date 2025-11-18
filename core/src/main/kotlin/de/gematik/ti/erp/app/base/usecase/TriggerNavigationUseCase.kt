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
 * Use case for triggering navigation events.
 *
 * This sets the trigger flag in the [NavigationTriggerDataStore] to indicate that a navigation
 * should occur. The trigger will only take effect if it hasn't already been consumed.
 *
 * Typically used in combination with [MarkAutoNavigationTriggerConsumedUseCase] to implement
 * one-time navigation behavior.
 */
class TriggerNavigationUseCase(
    private val dataStore: NavigationTriggerDataStore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Triggers a navigation event for the given identifier.
     *
     * @param id Unique identifier for the navigation trigger (e.g., feature flag name or event ID)
     */
    suspend operator fun invoke(id: String) {
        withContext(dispatcher) {
            dataStore.triggerNavigation(id)
        }
    }
}
