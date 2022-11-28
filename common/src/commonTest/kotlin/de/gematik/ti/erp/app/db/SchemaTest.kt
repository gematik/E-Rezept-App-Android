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

package de.gematik.ti.erp.app.db

import io.mockk.spyk
import io.mockk.verify
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.ext.query
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertTrue

class RealmA_V1 : RealmObject {
    var propA: Long = 0L
    var propB: String = "b"
}

class RealmB_V1 : RealmObject {
    var propA: Int = 1
    var propB: Int = 2
}

class RealmA_V2 : RealmObject {
    var propA: String = "a"
    var propB: String = "b"
    var propC: String = "c"
}

class RealmA_V3 : RealmObject {
    var propA: String = "a"
    var propB: String = "b"
    var propC: Int = 3
}

class SchemaTest : TestDB() {
    @Test
    fun `migrate from a new db`() {
        val schemas = setOf(
            AppRealmSchema(
                version = 0,
                classes = setOf(RealmA_V1::class),
                migrateOrInitialize = { migrationStartedFrom ->
                    assertEquals(-1, migrationStartedFrom)
                }
            ),
            AppRealmSchema(
                version = 1,
                classes = setOf(RealmA_V1::class),
                migrateOrInitialize = { migrationStartedFrom ->
                    assertEquals(-1, migrationStartedFrom)
                }
            ),
            AppRealmSchema(
                version = 2,
                classes = setOf(RealmA_V1::class),
                migrateOrInitialize = { migrationStartedFrom ->
                    assertEquals(-1, migrationStartedFrom)
                }
            ),
            AppRealmSchema(
                version = 3,
                classes = setOf(RealmA_V1::class, RealmB_V1::class),
                migrateOrInitialize = { migrationStartedFrom ->
                    assertEquals(-1, migrationStartedFrom)
                }
            ),
            AppRealmSchema(
                version = 4,
                classes = setOf(RealmA_V1::class, RealmB_V1::class, RealmA_V2::class),
                migrateOrInitialize = { migrationStartedFrom ->
                    assertEquals(-1, migrationStartedFrom)
                }
            )
        )

        var realm: Realm? = null
        try {
            realm = openRealmWith(schemas, configuration = { it.directory(tempDBPath) })

            realm.schema().classes.let { classes ->
                assertTrue { classes.any { it.name == "RealmA_V1" } }
                assertTrue { classes.any { it.name == "RealmB_V1" } }
                assertTrue { classes.any { it.name == "RealmA_V2" } }
            }
        } finally {
            realm?.close()
        }
    }

    @Test
    fun `migrate from existing db`() {
        Realm.open(
            RealmConfiguration.Builder(
                schema = setOf(RealmA_V1::class, LatestManualMigration::class)
            )
                .schemaVersion(0)
                .directory(tempDBPath)
                .build()
        ).also { realm ->
            realm.writeBlocking {
                copyToRealm(
                    LatestManualMigration().apply {
                        version = 0
                    }
                )
                copyToRealm(
                    RealmA_V1().apply {
                        propA = 123L
                        propB = "Test"
                    }
                )
            }
        }.close()

        val noCallVerifier = spyk({})
        val callVerifier = spyk({})

        val schemas = setOf(

            AppRealmSchema(
                version = 0,
                classes = setOf(RealmA_V1::class),
                migrateOrInitialize = {
                    noCallVerifier()
                }
            ),
            AppRealmSchema(
                version = 1,
                classes = setOf(RealmA_V1::class),
                migrateOrInitialize = { migrationStartedFrom ->
                    assertEquals(0, migrationStartedFrom)
                    callVerifier()
                }
            ),
            AppRealmSchema(
                version = 2,
                classes = setOf(RealmA_V1::class, RealmA_V2::class),
                migrateOrInitialize = { migrationStartedFrom ->
                    assertEquals(0, migrationStartedFrom)

                    val v1 = query<RealmA_V1>().first().find()

                    assertEquals(123L, v1?.propA)
                    assertEquals("Test", v1?.propB)

                    v1?.let {
                        copyToRealm(
                            RealmA_V2().apply {
                                propA = v1.propA.toString()
                                propB = v1.propB
                                propC = "65"
                            }
                        )
                        delete(v1)
                    }

                    callVerifier()
                }
            ),
            AppRealmSchema(
                version = 3,
                classes = setOf(RealmA_V1::class, RealmA_V2::class),
                migrateOrInitialize = { migrationStartedFrom ->
                    assertEquals(0, migrationStartedFrom)
                    assertEquals(null, query<RealmA_V1>().first().find())
                    callVerifier()
                }
            ),
            AppRealmSchema(
                version = 4,
                classes = setOf(RealmA_V1::class, RealmA_V2::class, RealmA_V3::class),
                migrateOrInitialize = { migrationStartedFrom ->
                    assertEquals(0, migrationStartedFrom)

                    val v2 = query<RealmA_V2>().first().find()

                    assertEquals("123", v2?.propA)
                    assertEquals("Test", v2?.propB)
                    assertEquals("65", v2?.propC)

                    v2?.let {
                        copyToRealm(
                            RealmA_V3().apply {
                                propA = v2.propA
                                propB = v2.propB
                                propC = v2.propC.toInt()
                            }
                        )
                        delete(v2)
                    }
                    callVerifier()
                }
            )
        )

        var realm: Realm? = null
        try {
            realm = openRealmWith(schemas, configuration = { it.directory(tempDBPath) })

            val v3 = realm.query<RealmA_V3>().first().find()
            assertEquals("123", v3?.propA)
            assertEquals("Test", v3?.propB)
            assertEquals(65, v3?.propC)

            verify(exactly = 0) { noCallVerifier() }
            verify(exactly = 4) { callVerifier() }
        } finally {
            realm?.close()
        }
    }
}
