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

package de.gematik.ti.erp.app.db.entities

import de.gematik.ti.erp.app.db.TestDB
import de.gematik.ti.erp.app.db.queryFirst
import io.realm.kotlin.Deleteable
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestEntryRealm : RealmObject {
    var prop: String = "TestEntryRealm"
}

class TestEntryWithListRealmB : RealmObject, Cascading {
    var prop: String = "TestEntryWithListRealmB"
    var list: RealmList<TestEntryRealm> = realmListOf()

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            yield(list)
        }
}

class TestEntryWithListRealmA : RealmObject, Cascading {
    var prop: String = "TestEntryWithListRealmA"
    var list: RealmList<TestEntryWithListRealmB> = realmListOf()

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            yield(list)
        }
}

class TestRealm : RealmObject, Cascading {
    var prop: String = "TestRealm"
    var singleEntry: TestEntryWithListRealmA? = null
    var list: RealmList<TestEntryWithListRealmA> = realmListOf()

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            singleEntry?.let { yield(it) }
            yield(list)
        }
}

class EntityUtilsTest : TestDB() {
    lateinit var realm: Realm

    @BeforeTest
    fun setUp() {
        realm = Realm.open(
            RealmConfiguration.Builder(
                schema = setOf(
                    TestRealm::class,
                    TestEntryWithListRealmA::class,
                    TestEntryWithListRealmB::class,
                    TestEntryRealm::class
                )
            )
                .schemaVersion(0)
                .directory(tempDBPath)
                .build()
        ).also { realm ->
            realm.writeBlocking {
                copyToRealm(
                    TestRealm().apply {
                        this.singleEntry = TestEntryWithListRealmA()
                        this.list = (1..5).map { a ->
                            TestEntryWithListRealmA().apply {
                                this.prop = "a: $a"
                                this.list = (1..4).map { b ->
                                    TestEntryWithListRealmB().apply {
                                        this.prop = "a: $a b: $b"
                                        this.list = (1..3).map { c ->
                                            TestEntryRealm().apply {
                                                this.prop = "a: $a b: $b c: $c"
                                            }
                                        }.toRealmList()
                                    }
                                }.toRealmList()
                            }
                        }.toRealmList()
                    }
                )
            }
        }
    }

    @AfterTest
    fun cleanUp() {
        realm.close()
    }

    @Test
    fun `cascading delete - max depth`() {
        val result = realm.queryFirst<TestRealm>()?.flatten()!!.objectIterator()

        assertEquals("a: 1 b: 1 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 1 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 1 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 2 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 2 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 2 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 3 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 3 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 3 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 4 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 4 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 4 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 1 b: 1     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 1 b: 2     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 1 b: 3     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 1 b: 4     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 2 b: 1 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 1 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 1 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 2 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 2 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 2 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 3 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 3 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 3 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 4 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 4 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 4 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 2 b: 1     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 2 b: 2     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 2 b: 3     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 2 b: 4     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 3 b: 1 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 1 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 1 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 2 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 2 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 2 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 3 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 3 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 3 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 4 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 4 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 4 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 3 b: 1     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 3 b: 2     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 3 b: 3     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 3 b: 4     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 4 b: 1 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 1 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 1 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 2 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 2 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 2 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 3 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 3 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 3 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 4 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 4 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 4 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 4 b: 1     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 4 b: 2     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 4 b: 3     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 4 b: 4     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 5 b: 1 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 1 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 1 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 2 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 2 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 2 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 3 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 3 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 3 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 4 c: 1".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 4 c: 2".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 4 c: 3".trim(), (result.next() as TestEntryRealm).prop)
        assertEquals("a: 5 b: 1     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 5 b: 2     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 5 b: 3     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 5 b: 4     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("TestEntryWithListRealmA".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 1          ".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 2          ".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 3          ".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 4          ".trim(), (result.next() as TestEntryWithListRealmA).prop)

        realm.writeBlocking {
            val resultsToDelete = queryFirst<TestRealm>()!!
            deleteAll(resultsToDelete)
        }

        assertEquals(0, realm.query<TestRealm>().count().find())
        assertEquals(0, realm.query<TestEntryWithListRealmA>().count().find())
        assertEquals(0, realm.query<TestEntryWithListRealmB>().count().find())
        assertEquals(0, realm.query<TestEntryRealm>().count().find())
    }

    @Test
    fun `cascading delete - depth 1`() {
        val result = realm.queryFirst<TestRealm>()?.flatten(maxDepth = 1)!!.objectIterator()

        assertEquals("a: 1 b: 1     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 1 b: 2     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 1 b: 3     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 1 b: 4     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 2 b: 1     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 2 b: 2     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 2 b: 3     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 2 b: 4     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 3 b: 1     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 3 b: 2     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 3 b: 3     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 3 b: 4     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 4 b: 1     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 4 b: 2     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 4 b: 3     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 4 b: 4     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 5 b: 1     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 5 b: 2     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 5 b: 3     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("a: 5 b: 4     ".trim(), (result.next() as TestEntryWithListRealmB).prop)
        assertEquals("TestEntryWithListRealmA".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 1          ".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 2          ".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 3          ".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 4          ".trim(), (result.next() as TestEntryWithListRealmA).prop)

        realm.writeBlocking {
            val resultsToDelete = queryFirst<TestRealm>()!!
            deleteAll(resultsToDelete, maxDepth = 1)
        }

        assertEquals(0, realm.query<TestRealm>().count().find())
        assertEquals(0, realm.query<TestEntryWithListRealmA>().count().find())
        assertEquals(0, realm.query<TestEntryWithListRealmB>().count().find())
        assertEquals(60, realm.query<TestEntryRealm>().count().find())
    }

    @Test
    fun `cascading delete - depth 0`() {
        val result = realm.queryFirst<TestRealm>()?.flatten(maxDepth = 0)!!.objectIterator()

        assertEquals("TestEntryWithListRealmA".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 1          ".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 2          ".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 3          ".trim(), (result.next() as TestEntryWithListRealmA).prop)
        assertEquals("a: 4          ".trim(), (result.next() as TestEntryWithListRealmA).prop)

        realm.writeBlocking {
            val resultsToDelete = queryFirst<TestRealm>()!!
            deleteAll(resultsToDelete, maxDepth = 0)
        }

        assertEquals(0, realm.query<TestRealm>().count().find())
        assertEquals(0, realm.query<TestEntryWithListRealmA>().count().find())
        assertEquals(20, realm.query<TestEntryWithListRealmB>().count().find())
        assertEquals(60, realm.query<TestEntryRealm>().count().find())
    }
}
