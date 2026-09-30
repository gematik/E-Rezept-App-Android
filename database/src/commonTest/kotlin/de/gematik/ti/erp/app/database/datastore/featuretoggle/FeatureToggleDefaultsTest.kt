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

import de.gematik.ti.erp.app.database.datastore.DataStoreCryptography
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeatureToggleDefaultsTest {

    @Test
    fun `default value enables room db for new installations`() {
        val serializer = FeatureEntitySerializer(NoOpDataStoreCryptography)

        val featureStateByName = serializer.defaultValue.classes.associateBy(FeatureEntity::name)

        assertEquals(FEATURE_ENTITIES, serializer.defaultValue.classes)
        assertTrue(featureStateByName.getValue(ROOM_DB.name).isActive)
        assertFalse(featureStateByName.getValue(EU_REDEEM.name).isActive)
        assertTrue(featureStateByName.getValue(PUSH_NOTIFICATIONS.name).isActive)
    }

    @Test
    fun `migration keeps room db disabled when backfilling existing installations`() = runTest {
        val migration = FeatureToggleDataMigration()

        val migratedSchema = migration.migrate(
            FeatureEntitySchema(
                classes = emptySet()
            )
        )
        val featureStateByName = migratedSchema.classes.associateBy(FeatureEntity::name)

        assertFalse(featureStateByName.getValue(ROOM_DB.name).isActive)
        assertFalse(featureStateByName.getValue(EU_REDEEM.name).isActive)
        assertTrue(featureStateByName.getValue(PUSH_NOTIFICATIONS.name).isActive)
    }

    private object NoOpDataStoreCryptography : DataStoreCryptography {
        override fun encrypt(bytes: ByteArray): ByteArray = bytes

        override fun decrypt(bytes: ByteArray): ByteArray = bytes
    }
}
