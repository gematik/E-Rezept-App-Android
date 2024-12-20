/*
 * Copyright 2024, gematik GmbH
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

import android.content.SharedPreferences
import androidx.core.content.edit
import de.gematik.ti.erp.app.featuretoggle.model.NewFeature
import io.github.aakira.napier.Napier

class NewFeaturesLocalDataSource(
    private val sharedPreferences: SharedPreferences
) {
    fun setDefaults() {
        try {
            sharedPreferences.edit {
                NewFeature.entries.forEach { feature ->
                    putBoolean(feature.name, feature.default)
                    apply()
                }
            }
        } catch (e: Throwable) {
            Napier.e { "error on NewFeaturesLocalDataSource defaults ${e.message}" }
        }
    }

    fun isNewFeatureSeen(feature: NewFeature): Boolean {
        return sharedPreferences.getBoolean(feature.name, false)
    }

    fun markFeatureSeen(feature: NewFeature) {
        sharedPreferences.edit {
            putBoolean(feature.name, true)
            apply()
        }
    }
}
