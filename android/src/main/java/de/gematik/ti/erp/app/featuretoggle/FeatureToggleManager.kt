/*
 * Copyright (c) 2022 gematik GmbH
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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("featureToggles")

enum class Features(val featureName: String) {
    FAST_TRACK("FastTrack"),
    ADD_PROFILE("AddProfiles"),
    BIO_LOGIN("BioLogin")
}

class FeatureToggleManager @Inject constructor(@ApplicationContext val context: Context) {

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
