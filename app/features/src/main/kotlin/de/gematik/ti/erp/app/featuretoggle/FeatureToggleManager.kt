/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.featuretoggle

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("featureToggles")

enum class Features(val featureName: String) {
    // REDEEM_WITHOUT_TI("RedeemWithoutTI"),
}

// TODO: Poor naming, change it
class FeatureToggleManager(val context: Context) {

    private val dataStore = context.dataStore
    val features = Features.values()

    fun isFeatureEnabled(featureName: String) = dataStore.data.map { prefs ->
        prefs[booleanPreferencesKey(featureName)] ?: false
    }

    fun featuresState() = dataStore.data.map { prefs ->
        val map: MutableMap<String, Boolean> = mutableMapOf()
        for (i in features) {
            map[i.featureName] = prefs[booleanPreferencesKey(i.featureName)] ?: false
        }
        map
    }

    suspend fun toggleFeature(featureKey: Preferences.Key<Boolean>) {
        dataStore.edit { features ->
            val currentValue = features[featureKey] ?: false
            features[featureKey] = !currentValue
        }
    }
}
