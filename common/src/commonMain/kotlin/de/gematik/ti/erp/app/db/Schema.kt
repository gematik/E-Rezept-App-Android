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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.db

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmObject
import kotlin.reflect.KClass

class AppRealmSchema(
    val version: Long,
    val classes: Set<KClass<out RealmObject>>,
    val migrateOrInitialize: (MutableRealm.(migrationStartedFrom: Long) -> Unit)? = null
) {
    override fun equals(other: Any?): Boolean {
        return (other as? AppRealmSchema)?.version == version
    }

    override fun hashCode(): Int {
        return version.hashCode()
    }
}

class LatestManualMigration : RealmObject {
    var version: Long = -1
}

typealias RealmSharedConfigBuilder = RealmConfiguration.Builder

fun openRealmWith(
    schemas: Set<AppRealmSchema>,
    configuration: ((RealmSharedConfigBuilder) -> RealmSharedConfigBuilder)? = null
): Realm {
    val latestSchema = requireNotNull(schemas.maxByOrNull { it.version }) { "At least one schema is required!" }

    return Realm.open(
        RealmConfiguration.Builder(latestSchema.classes + LatestManualMigration::class)
            .schemaVersion(latestSchema.version)
            .let {
                configuration?.invoke(it) ?: it
            }
            .build()
    ).also { realm ->
        val latestManualMigration = realm.query<LatestManualMigration>().first().find() ?: run {
            realm.writeBlocking {
                copyToRealm(
                    LatestManualMigration().apply {
                        version = -1
                    }
                )
            }
        }

        val migrationStartedFrom = latestManualMigration.version

        schemas.sortedBy { it.version }.forEach {
            if (it.version > latestManualMigration.version) {
                realm.writeBlocking {
                    it.migrateOrInitialize?.invoke(this, migrationStartedFrom)

                    findLatest(latestManualMigration)?.version = it.version
                }
            }
        }
    }
}
