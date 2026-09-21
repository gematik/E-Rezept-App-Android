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

package de.gematik.ti.erp.app.debugsettings.pushnotifications.datasource

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.debugPushNotificationsDataStore by preferencesDataStore("debug_push_notifications_prefs")

class DebugPushNotificationsLocalDataSource(context: Context) {

    private val dataStore = context.debugPushNotificationsDataStore

    private val oauthTokenKey = stringPreferencesKey("fcm_oauth_token")

    fun oauthToken(): Flow<String> =
        dataStore.data.map { prefs -> prefs[oauthTokenKey] ?: "" }

    suspend fun saveOauthToken(token: String) {
        dataStore.edit { prefs -> prefs[oauthTokenKey] = token }
    }
}
