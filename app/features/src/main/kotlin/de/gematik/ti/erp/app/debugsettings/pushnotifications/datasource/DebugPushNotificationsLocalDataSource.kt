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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.debugPushNotificationsDataStore by preferencesDataStore("debug_push_notifications_prefs")

class DebugPushNotificationsLocalDataSource(private val context: Context) {

    private val dataStore = context.debugPushNotificationsDataStore

    private val oauthTokenKey = stringPreferencesKey("fcm_oauth_token")
    private val pushGatewayUrlKey = stringPreferencesKey("debug_push_gateway_url")

    @Volatile
    private var cachedPushGatewayUrl: String? = null

    fun oauthToken(): Flow<String> =
        dataStore.data.map { prefs -> prefs[oauthTokenKey] ?: "" }

    suspend fun saveOauthToken(token: String) {
        dataStore.edit { prefs -> prefs[oauthTokenKey] = token }
    }

    fun pushGatewayUrl(): Flow<String> =
        dataStore.data.map { prefs ->
            val url = prefs[pushGatewayUrlKey] ?: ""
            cachedPushGatewayUrl = url.ifBlank { null }
            url
        }

    fun getPushGatewayUrlSync(): String? {
        if (cachedPushGatewayUrl == null) {
            cachedPushGatewayUrl = runBlocking(Dispatchers.IO) {
                dataStore.data.firstOrNull()?.get(pushGatewayUrlKey)?.ifBlank { null }
            }
        }
        return cachedPushGatewayUrl
    }

    suspend fun savePushGatewayUrl(url: String) {
        cachedPushGatewayUrl = url.ifBlank { null }
        dataStore.edit { prefs -> prefs[pushGatewayUrlKey] = url }
    }

    private val sharedPrefs = context.getSharedPreferences("debug_push_notifications_prefs_sp", Context.MODE_PRIVATE)

    fun isFailPushGatewayTest(): Flow<Boolean> = kotlinx.coroutines.flow.flow {
        emit(sharedPrefs.getBoolean("fail_push_gateway_test", false))
    }

    fun isFailPushGatewayTestSync(): Boolean {
        return sharedPrefs.getBoolean("fail_push_gateway_test", false)
    }

    suspend fun saveFailPushGatewayTest(fail: Boolean) {
        sharedPrefs.edit().putBoolean("fail_push_gateway_test", fail).apply()
    }

    fun isShowRawPushNotificationSync(): Boolean {
        return sharedPrefs.getBoolean("show_raw_push_notification", false)
    }

    fun isShowRawPushNotification(): Flow<Boolean> = kotlinx.coroutines.flow.flow {
        emit(sharedPrefs.getBoolean("show_raw_push_notification", false))
    }

    suspend fun saveShowRawPushNotification(show: Boolean) {
        sharedPrefs.edit().putBoolean("show_raw_push_notification", show).apply()
    }
}
