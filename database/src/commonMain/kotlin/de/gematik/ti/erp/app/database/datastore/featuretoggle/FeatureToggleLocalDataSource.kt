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

package de.gematik.ti.erp.app.database.datastore.featuretoggle

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

const val FEATURE_TOGGLE_DATA_SOURCE = "FeatureToggles"

val REALM_V2 = FeatureEntity(name = "RealmVersion2", isActive = false)

val FEATURE_ENTITIES = setOf(
    REALM_V2
)

class FeatureToggleLocalDataSource(
    private val dataStore: DataStore<FeatureEntitySchema>,
    private val isDebug: Boolean
) {
    val persistedFeatures = dataStore.data.map { it.classes.sortedBy { it.name }.toSet() }

    fun isFeatureEnabled(feature: FeatureEntity): Flow<Boolean> =
        if (isDebug) {
            dataStore.data.map { currentSchema ->
                val currentFeatureSet = currentSchema.classes
                val match = currentFeatureSet.find { feature.name == it.name }
                match?.isActive ?: false
            }
        } else {
            emptyFlow() // hide features in release build
        }

    suspend fun toggleFeature(feature: FeatureEntity) {
        dataStore.updateData {
                currentSchema ->
            val currentFeatureSet = currentSchema.classes.toMutableSet()
            val match = currentFeatureSet.find { it.name == feature.name }
            if (match != null) {
                currentFeatureSet.apply {
                    remove(match)
                    add(match.copy(isActive = !match.isActive))
                }.toSet()
            } else {
                throw Throwable("Togglable Feature ${feature.name} not found")
            }
            currentSchema.copy(
                classes = currentFeatureSet
            )
        }
    }
}
