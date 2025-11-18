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

package de.gematik.ti.erp.app.navigation.triggers

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.navigationTriggerStore by preferencesDataStore("navigationTriggers")

/**
 * Interface for managing navigation triggers and tracking navigation completion.
 *
 * This interface serves two purposes:
 * 1. **Auto-navigation trigger**: Determines if automatic navigation should occur based on trigger state
 * 2. **Navigation flag**: Tracks whether a navigation event has been completed/consumed
 *
 * The mechanism uses two keys per trigger ID:
 * - A trigger key to mark that navigation should happen
 * - A consumed key to mark that navigation has already occurred
 *
 * This prevents duplicate navigations while allowing the trigger to be re-enabled in the future.
 */
interface NavigationTriggerDataStore {

    /**
     * Observes whether navigation should occur for the given trigger ID.
     *
     * Returns `true` when:
     * - Navigation has been triggered ([triggerNavigation] was called)
     * - AND navigation has not yet been consumed ([markAutoNavigationConsumed] was not called)
     *
     * @param id Unique identifier for the navigation trigger
     * @return Flow emitting `true` if navigation should occur, `false` otherwise
     */
    fun shouldNavigate(id: String): Flow<Boolean>

    /**
     * Triggers navigation for the given ID.
     *
     * Sets the trigger flag to indicate that navigation should occur.
     * If the trigger was already consumed, this will not re-trigger navigation
     * to prevent duplicate navigation events.
     *
     * @param id Unique identifier for the navigation trigger
     */
    suspend fun triggerNavigation(id: String)

    /**
     * Marks that the auto-navigation has been completed/consumed.
     *
     * Call this method after navigation has occurred to prevent the same
     * navigation from happening again. The trigger remains set but the
     * consumed flag prevents [shouldNavigate] from returning `true`.
     *
     * @param id Unique identifier for the navigation trigger (typically a profile ID)
     */
    suspend fun markAutoNavigationConsumed(id: String)
}

/**
 * Default implementation of [NavigationTriggerDataStore] using DataStore preferences.
 *
 * Stores navigation trigger state using two boolean keys per trigger ID:
 * - `trigger_event_{id}`: Indicates if navigation should happen
 * - `trigger_consumed_{id}`: Indicates if navigation has already been consumed
 */
class DefaultNavigationTriggerDataStore(context: Context) : NavigationTriggerDataStore {

    private val dataStore = context.navigationTriggerStore

    private fun triggerKey(id: String) = booleanPreferencesKey("trigger_event_$id")
    private fun consumedKey(id: String) = booleanPreferencesKey("trigger_consumed_$id")

    override fun shouldNavigate(id: String): Flow<Boolean> =
        dataStore.data.map { prefs ->
            val triggered = prefs[triggerKey(id)] ?: false
            val consumed = prefs[consumedKey(id)] ?: false
            triggered && !consumed
        }

    override suspend fun triggerNavigation(id: String) {
        dataStore.edit { prefs ->
            // Don't trigger again if already consumed
            if (prefs[consumedKey(id)] != true) {
                prefs[triggerKey(id)] = true
            }
        }
    }

    override suspend fun markAutoNavigationConsumed(id: String) {
        dataStore.edit { prefs ->
            prefs[consumedKey(id)] = true
        }
    }
}
