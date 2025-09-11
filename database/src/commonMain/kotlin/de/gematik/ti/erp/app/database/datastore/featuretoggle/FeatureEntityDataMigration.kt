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

import androidx.datastore.core.DataMigration

class FeatureToggleDataMigration() : DataMigration<FeatureEntitySchema> {
    override suspend fun shouldMigrate(currentData: FeatureEntitySchema): Boolean {
        val isUpToDate = isDataStoreSynced(currentData.classes)
        val hasVersionMismatch = currentData.version < FEATURE_ENTITY_SCHEMA_VERSION
        return hasVersionMismatch || isUpToDate
    }

    override suspend fun migrate(currentData: FeatureEntitySchema): FeatureEntitySchema {
        var migrationData = currentData
        // Start Migration Example
        if (migrationData.version < 1) {
            val migratedEntities = migrationData.classes.map { entity ->
                entity.copy()
            }.toSet()
            migrationData = migrationData.copy(classes = migratedEntities)
        }
        // End Example
        // sync Data
        migrationData = migrationData.copy(classes = syncDataStore(migrationData.classes))
        return migrationData
    }

    override suspend fun cleanUp() {
    }

    private fun isDataStoreSynced(currentData: Set<FeatureEntity>): Boolean {
        return isAnyFeatureMissing(currentData) || isDeprecatedDataStored(currentData)
    }

    private fun isDeprecatedDataStored(currentData: Set<FeatureEntity>): Boolean {
        val featureNames = FEATURE_ENTITIES.map { it.name }
        return currentData.any {
            it.name !in featureNames
        }
    }

    private fun isAnyFeatureMissing(currentData: Set<FeatureEntity>): Boolean {
        val featureNames = currentData.map { it.name }
        return FEATURE_ENTITIES.any {
            it.name !in featureNames
        }
    }

    private fun syncDataStore(currentData: Set<FeatureEntity>): Set<FeatureEntity> {
        return FEATURE_ENTITIES.map { feature ->
            currentData.find { feature.name == it.name } ?: feature
        }.sortedBy { it.name }.toSet()
    }
}
