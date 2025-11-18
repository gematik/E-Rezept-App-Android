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

package de.gematik.ti.erp.app.demomode.navigation.triggers

import de.gematik.ti.erp.app.navigation.triggers.NavigationTriggerDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Demo mode implementation of [NavigationTriggerDataStore].
 *
 * This implementation uses in-memory storage to track navigation triggers and consumed states
 * during demo mode. The state is maintained in memory and will be reset when the app is restarted,
 * which is appropriate for demo/testing purposes.
 *
 * The implementation stores trigger IDs in two separate maps:
 * - [triggeredIds]: IDs that have been triggered for navigation
 * - [consumedIds]: IDs that have already been consumed (navigation completed)
 */
class DemoNavigationTriggerDataStore : NavigationTriggerDataStore {

    private val triggeredIds = MutableStateFlow<Set<String>>(setOf())
    private val consumedIds = MutableStateFlow<Set<String>>(setOf())

    /**
     * Observes whether navigation should occur for the given trigger ID.
     *
     * Returns `true` when the ID has been triggered but not yet consumed.
     *
     * @param id Unique identifier for the navigation trigger
     * @return Flow emitting `true` if navigation should occur, `false` otherwise
     */
    override fun shouldNavigate(id: String): Flow<Boolean> =
        combine(triggeredIds, consumedIds) { triggered, consumed ->
            id in triggered && id !in consumed
        }

    /**
     * Triggers navigation for the given ID in demo mode.
     *
     * Adds the ID to the triggered set
     *
     * @param id Unique identifier for the navigation trigger
     */
    override suspend fun triggerNavigation(id: String) {
        if (id !in consumedIds.value) {
            triggeredIds.value += id
        }
    }

    /**
     * Marks that the auto-navigation has been completed/consumed in demo mode.
     *
     * Adds the ID to the consumed set to prevent duplicate navigation.
     *
     * @param id Unique identifier for the navigation trigger
     */
    override suspend fun markAutoNavigationConsumed(id: String) {
        consumedIds.value += id
    }
}
