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
    MULTI_PROFILE("multiProfile"),
    FAST_TRACK("FastTrack")
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
