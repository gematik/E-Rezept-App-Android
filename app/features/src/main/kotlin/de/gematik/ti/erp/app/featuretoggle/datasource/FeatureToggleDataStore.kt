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

package de.gematik.ti.erp.app.featuretoggle.datasource

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("featureToggles")

enum class Features(val featureName: String) {
    // REDEEM_WITHOUT_TI("RedeemWithoutTI"),
}

class FeatureToggleDataStore(val context: Context) {

    private val dataStore = context.dataStore
    val features = Features.entries.toTypedArray()

    fun isFeatureEnabled(featureName: Features): Flow<Boolean> =
        if (BuildConfigExtension.isInternalDebug) {
            dataStore.data.map { prefs ->
                prefs[booleanPreferencesKey(featureName.featureName)] ?: false
            }
        } else {
            flowOf(false) // hide features in release build
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
