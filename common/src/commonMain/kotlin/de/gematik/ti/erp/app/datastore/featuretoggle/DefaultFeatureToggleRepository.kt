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

package de.gematik.ti.erp.app.datastore.featuretoggle

import de.gematik.ti.erp.app.database.datastore.featuretoggle.FeatureEntity
import de.gematik.ti.erp.app.database.datastore.featuretoggle.FeatureToggleLocalDataSource
import kotlinx.coroutines.flow.Flow

class DefaultFeatureToggleRepository(
    private val localDataSource: FeatureToggleLocalDataSource
) : FeatureToggleRepository {
    // combine currentActiveFeatureFlags with persistedFeatureFlags
    // to only show currently available feature flags in the right state
    override fun getFeatures(): Flow<Set<FeatureEntity>> {
        return localDataSource.persistedFeatures
        /*
        return combine(
            localDataSource.currentFeatures,
            localDataSource.persistedFeatures
        ) { current, persisted ->
            current.map { feature ->
                persisted.find { feature.name == it.name } ?: feature
            }.sortedBy { it.name }.toSet()
        }

         */
    }

    override fun isFeatureEnabled(feature: FeatureEntity): Flow<Boolean> =
        localDataSource.isFeatureEnabled(feature)

    override suspend fun toggleFeature(feature: FeatureEntity) =
        localDataSource.toggleFeature(feature)
}
