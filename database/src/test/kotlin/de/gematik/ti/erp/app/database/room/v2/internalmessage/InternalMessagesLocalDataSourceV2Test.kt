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
 */

package de.gematik.ti.erp.app.database.room.v2.internalmessage

import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.communication.model.InternalMessageErpModel
import de.gematik.ti.erp.app.database.api.InternalMessagesLocalDataSource
import de.gematik.ti.erp.app.utils.plusDuration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class InternalMessagesLocalDataSourceV2Test {

    private class FakeInternalMessageDao : InternalMessageDao {
        val messages = LinkedHashMap<String, InternalMessageRoomEntity>()
        private val _flow = MutableStateFlow<List<InternalMessageRoomEntity>>(emptyList())

        private fun emit() {
            _flow.value = messages.values.toList()
        }

        override suspend fun upsert(item: InternalMessageRoomEntity) {
            messages[item.id] = item
            emit()
        }

        override suspend fun insert(item: InternalMessageRoomEntity) {
            messages[item.id] = item
            emit()
        }

        override suspend fun getById(id: String): InternalMessageRoomEntity? = messages[id]

        override fun observeAll(): Flow<List<InternalMessageRoomEntity>> =
            _flow.map { list -> list.sortedByDescending { it.time } }

        override fun observeUnreadCount(): Flow<Long> =
            _flow.map { list -> list.count { it.isUnread }.toLong() }

        override fun observeLastUpdatedVersion(): Flow<String?> =
            _flow.map { list -> list.sortedByDescending { it.version }.firstOrNull()?.version }

        override suspend fun markAllAsRead() {
            messages.values.forEach {
                messages[it.id] = it.copy(isUnread = false)
            }
            emit()
        }
    }

    private val fakeDao = FakeInternalMessageDao()
    private val sut: InternalMessagesLocalDataSource = InternalMessagesLocalDataSourceV2(fakeDao)
    private val now = Clock.System.now()

    @Test
    fun getInternalMessages_returnsMappedAndSorted() = runTest {
        val msg1 = InternalMessageRoomEntity("id1", "sender1", "text1", now, "tag1", true, CommunicationProfileV1.InApp, "1.0", "de")
        val msg2 = InternalMessageRoomEntity("id2", "sender2", "text2", now.plusDuration(7L.days), "tag2", false, CommunicationProfileV1.InApp, "1.1", "de")
        fakeDao.upsert(msg1)
        fakeDao.upsert(msg2)

        val result = sut.getInternalMessages().first()
        assertEquals(2, result.size)
        assertEquals("id2", result[0].id) // msg2 has later timestamp, so it is sorted first
    }

    @Test
    fun getUnreadInternalMessagesCount_returnsCount() = runTest {
        val msg1 = InternalMessageRoomEntity("id1", "sender1", "text1", now, "tag1", true, CommunicationProfileV1.InApp, "1.0", "de")
        val msg2 = InternalMessageRoomEntity("id2", "sender2", "text2", now, "tag2", false, CommunicationProfileV1.InApp, "1.1", "de")
        fakeDao.upsert(msg1)
        fakeDao.upsert(msg2)

        val count = sut.getUnreadInternalMessagesCount().first()
        assertEquals(1, count)
    }

    @Test
    fun setInternalMessagesAsRead_marksAllAsRead() = runTest {
        val msg = InternalMessageRoomEntity("id1", "sender1", "text1", now, "tag1", true, CommunicationProfileV1.InApp, "1.0", "de")
        fakeDao.upsert(msg)

        sut.setInternalMessagesAsRead()

        assertFalse(fakeDao.messages["id1"]!!.isUnread)
    }

    @Test
    fun saveInternalMessage_insertsToDao() = runTest {
        val model = InternalMessageErpModel(
            id = "id100",
            sender = "sender",
            text = "text",
            time = now,
            tag = "tag",
            isUnread = true,
            messageProfile = de.gematik.ti.erp.app.communication.model.CommunicationErpModel.CommunicationProfile.InApp,
            version = "1.0",
            languageCode = "de"
        )

        sut.saveInternalMessage(model)

        val saved = fakeDao.messages["id100"]
        assertNotNull(saved)
        assertEquals("text", saved.text)
        assertEquals("sender", saved.sender)
    }
}
