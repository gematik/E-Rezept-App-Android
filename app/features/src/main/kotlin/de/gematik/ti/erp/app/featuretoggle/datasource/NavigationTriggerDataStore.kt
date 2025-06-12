/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.featuretoggle.datasource

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.navigationTriggerStore by preferencesDataStore("navigationTriggers")

class NavigationTriggerDataStore(context: Context) {

    private val dataStore = context.navigationTriggerStore

    private fun triggerKey(profileId: String) = booleanPreferencesKey("trigger_event_$profileId")
    private fun consumedKey(profileId: String) = booleanPreferencesKey("trigger_consumed_$profileId")

    fun shouldNavigate(profileId: String): Flow<Boolean> =
        dataStore.data.map { prefs ->
            val triggered = prefs[triggerKey(profileId)] ?: false
            val consumed = prefs[consumedKey(profileId)] ?: false
            triggered && !consumed
        }

    suspend fun triggerNavigation(profileId: String) {
        dataStore.edit { prefs ->
            // Don't trigger again if already consumed
            if (prefs[consumedKey(profileId)] != true) {
                prefs[triggerKey(profileId)] = true
            }
        }
    }

    suspend fun markNavigationConsumed(profileId: String) {
        dataStore.edit { prefs ->
            prefs[consumedKey(profileId)] = true
        }
    }
}
