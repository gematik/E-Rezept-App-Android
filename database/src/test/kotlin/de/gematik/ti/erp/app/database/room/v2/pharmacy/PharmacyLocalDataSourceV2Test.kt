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

package de.gematik.ti.erp.app.database.room.v2.pharmacy

import de.gematik.ti.erp.app.database.api.PharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.model.ContactInformationErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyAddressErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyErpModel
import de.gematik.ti.erp.app.pharmacy.model.TelematikId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class PharmacyLocalDataSourceV2Test {

    private class FakePharmacyDao : PharmacyDao {
        private val data = LinkedHashMap<String, PharmacyEntity>()
        private val flow = MutableStateFlow<List<PharmacyEntity>>(emptyList())

        private fun emit() {
            flow.value = data.values.toList()
        }

        override suspend fun upsert(entity: PharmacyEntity) {
            data[entity.id] = entity
            emit()
        }

        override suspend fun getPharmacyById(id: String): PharmacyEntity? = data[id]

        override fun observeIsFavourite(id: String): Flow<Boolean> =
            flow.map { list -> list.any { it.id == id && it.isFavourite } }

        override fun observeIsOftenUsed(id: String): Flow<Boolean> =
            flow.map { list -> list.any { it.id == id && it.isOftenUsed } }

        override fun observePharmacy(): Flow<List<PharmacyEntity>> = flow

        override suspend fun markUsed(telematikId: String, now: Instant): Int {
            val e = data[telematikId] ?: return 0
            val updated = e.copy(
                countUsage = e.countUsage + 1,
                lastUsed = now
            )
            data[telematikId] = updated
            emit()
            return 1
        }

        override suspend fun deleteById(id: String) {
            data.remove(id)
            emit()
        }
    }

    private fun model(
        id: String = "123",
        name: String = "Pharma",
        lastUsed: Instant = Instant.DISTANT_PAST,
        isFav: Boolean = false,
        isOften: Boolean = false,
        usage: Int = 0
    ) = PharmacyErpModel(
        lastUsed = lastUsed,
        isFavorite = isFav,
        isOftenUsed = isOften,
        usageCount = usage,
        telematikId = id,
        name = name,
        address = PharmacyAddressErpModel(lineAddress = "Addr 1", zip = "10115", city = "Berlin"),
        contact = ContactInformationErpModel(phone = "", mail = "", url = "")
    )

    private fun entity(
        id: String,
        name: String = "Pharma",
        isFav: Boolean = false,
        isOften: Boolean = false,
        usage: Int = 0,
        lastUsed: Instant? = null,
        created: Instant = Clock.System.now()
    ) = PharmacyEntity(
        id = id,
        name = name,
        lineAddress = "Addr 1",
        city = "Berlin",
        zip = "10115",
        latitude = 0.0,
        longitude = 0.0,
        phone = "",
        fax = "",
        email = "",
        web = "",
        imagePath = null,
        countUsage = usage,
        isFavourite = isFav,
        isOftenUsed = isOften,
        created = created,
        lastUsed = lastUsed
    )

    @Test
    fun markFavourite_setsFlag_preservesOftenUsed_and_updatesLastUsed() = runTest {
        val dao = FakePharmacyDao()
        val sut: PharmacyLocalDataSource = PharmacyLocalDataSourceV2(dao)

        // Seed existing often-used entry
        val created = Clock.System.now()
        dao.upsert(entity(id = "A", isFav = false, isOften = true, usage = 3, created = created, lastUsed = Instant.DISTANT_PAST))

        val before = dao.getPharmacyById("A")
        assertNotNull(before)
        val t0 = Clock.System.now()

        sut.markPharmacyAsFavourite(model(id = "A"))

        val after = dao.getPharmacyById("A")!!
        assertTrue(after.isFavourite)
        assertTrue(after.isOftenUsed) // preserved
        assertEquals(3, after.countUsage) // unchanged
        assertTrue(requireNotNull(after.lastUsed) >= t0)
        // created currently gets reset by mapper on upsert; verify current behavior
        // Future improvement could preserve created from existing
    }

    @Test
    fun markOftenUsed_preservesFavourite_incrementsUsage_and_updatesLastUsed() = runTest {
        val dao = FakePharmacyDao()
        val sut: PharmacyLocalDataSource = PharmacyLocalDataSourceV2(dao)

        // Seed existing favorite entry
        dao.upsert(entity(id = "B", isFav = true, isOften = false, usage = 0, lastUsed = null))

        val t0 = Clock.System.now()
        sut.markPharmacyAsOftenUsed(model(id = "B"))

        val after = dao.getPharmacyById("B")!!
        assertTrue(after.isFavourite) // preserved
        assertTrue(after.isOftenUsed)
        assertEquals(1, after.countUsage) // markUsed increments once
        assertTrue(requireNotNull(after.lastUsed) >= t0)
    }

    @Test
    fun deleteFavoritePharmacy_demark_ifOftenUsed_else_delete() = runTest {
        val dao = FakePharmacyDao()
        val sut: PharmacyLocalDataSource = PharmacyLocalDataSourceV2(dao)

        // Case 1: both true -> demark favorite only
        dao.upsert(entity(id = "C", isFav = true, isOften = true))
        sut.deleteFavoritePharmacy(TelematikId("C"))
        val c = dao.getPharmacyById("C")
        assertNotNull(c)
        assertFalse(c!!.isFavourite)
        assertTrue(c.isOftenUsed)

        // Case 2: only favorite -> delete
        dao.upsert(entity(id = "D", isFav = true, isOften = false))
        sut.deleteFavoritePharmacy(TelematikId("D"))
        val d = dao.getPharmacyById("D")
        assertNull(d)
    }

    @Test
    fun deleteOftenUsedPharmacy_demark_ifFavorite_else_delete() = runTest {
        val dao = FakePharmacyDao()
        val sut: PharmacyLocalDataSource = PharmacyLocalDataSourceV2(dao)

        // Case 1: both true -> demark often used only
        dao.upsert(entity(id = "E", isFav = true, isOften = true))
        sut.deleteOftenUsedPharmacy(TelematikId("E"))
        val e = dao.getPharmacyById("E")
        assertNotNull(e)
        assertTrue(e!!.isFavourite)
        assertFalse(e.isOftenUsed)

        // Case 2: only often used -> delete
        dao.upsert(entity(id = "F", isFav = false, isOften = true))
        sut.deleteOftenUsedPharmacy(TelematikId("F"))
        val f = dao.getPharmacyById("F")
        assertNull(f)
    }

    @Test
    fun loadPharmacies_sortsByFavoriteThenLastUsed_desc() = runTest {
        val dao = FakePharmacyDao()
        val sut: PharmacyLocalDataSource = PharmacyLocalDataSourceV2(dao)

        val base = Clock.System.now()
        val t1 = base - 10.seconds
        val t2 = base - 5.seconds
        val t3 = base

        dao.upsert(entity(id = "1", lastUsed = t1, isFav = true))
        dao.upsert(entity(id = "2", lastUsed = t3, isOften = true))
        dao.upsert(entity(id = "3", lastUsed = t2, isFav = true))
        dao.upsert(entity(id = "4", lastUsed = base + 10.seconds, isOften = true))

        val list = sut.loadPharmacies().first()
        val ids = list.map { it.telematikId }
        // Favorites: "3" (t2), "1" (t1) -> sorted by lastUsed DESC: "3", "1"
        // Often used: "4" (base+10s), "2" (t3) -> sorted by lastUsed DESC: "4", "2"
        // Expected: "3", "1", "4", "2"
        assertEquals(listOf("3", "1", "4", "2"), ids)
    }

    @Test
    fun loadPharmacies_favPrecedence_and_distinct() = runTest {
        val dao = FakePharmacyDao()
        val sut: PharmacyLocalDataSource = PharmacyLocalDataSourceV2(dao)

        dao.upsert(entity(id = "1", isFav = true, isOften = true, lastUsed = Clock.System.now()))
        dao.upsert(entity(id = "2", isFav = false, isOften = true, lastUsed = Clock.System.now()))

        val list = sut.loadPharmacies().first()
        assertEquals(2, list.size)
        assertTrue(list.first { it.telematikId == "1" }.isFavorite)
        assertTrue(list.first { it.telematikId == "1" }.isOftenUsed)
    }

    @Test
    fun flag_observation_flows_reflect_changes() = runTest {
        val dao = FakePharmacyDao()
        val sut: PharmacyLocalDataSource = PharmacyLocalDataSourceV2(dao)

        dao.upsert(entity(id = "X", isFav = false, isOften = false))

        assertFalse(sut.isPharmacyInFavorites(model(id = "X")).first())
        assertFalse(sut.isPharmacyOftenUsed(model(id = "X")).first())

        sut.markPharmacyAsFavourite(model(id = "X"))
        assertTrue(sut.isPharmacyInFavorites(model(id = "X")).first())
        assertFalse(sut.isPharmacyOftenUsed(model(id = "X")).first())

        sut.markPharmacyAsOftenUsed(model(id = "X"))
        assertTrue(sut.isPharmacyInFavorites(model(id = "X")).first())
        assertTrue(sut.isPharmacyOftenUsed(model(id = "X")).first())

        sut.deleteFavoritePharmacy(TelematikId("X"))
        assertFalse(sut.isPharmacyInFavorites(model(id = "X")).first())
        assertTrue(sut.isPharmacyOftenUsed(model(id = "X")).first())

        sut.deleteOftenUsedPharmacy(TelematikId("X"))
        // should be deleted now (both flags false)
        assertNull(dao.getPharmacyById("X"))
    }
}
