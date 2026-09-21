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

package de.gematik.ti.erp.app.database.room.v2.internalmessage

import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.communication.model.InternalMessageErpModel
import de.gematik.ti.erp.app.communication.model.toCommunicationProfile
import de.gematik.ti.erp.app.database.api.InternalMessagesLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InternalMessagesLocalDataSourceV2(
    private val dao: InternalMessageDao
) : InternalMessagesLocalDataSource {

    override fun getInternalMessages(): Flow<List<InternalMessageErpModel>> =
        dao.observeAll().map { list ->
            list.map { it.toModel() }
        }

    override fun getUnreadInternalMessagesCount(): Flow<Long> =
        dao.observeUnreadCount()

    override fun getLastUpdatedVersion(): Flow<String?> =
        dao.observeLastUpdatedVersion()

    override suspend fun setInternalMessagesAsRead() {
        dao.markAllAsRead()
    }

    override suspend fun updateInternalMessage(updatedModel: InternalMessageErpModel) {
        val existing = dao.getById(updatedModel.id)
        if (existing != null) {
            dao.upsert(
                existing.copy(
                    languageCode = updatedModel.languageCode,
                    text = updatedModel.text,
                    tag = updatedModel.tag,
                    sender = updatedModel.sender
                )
            )
        }
    }

    override suspend fun saveInternalMessage(internalMessageErpModel: InternalMessageErpModel) {
        dao.insert(internalMessageErpModel.toRoomEntity())
    }

    private fun InternalMessageRoomEntity.toModel(): InternalMessageErpModel =
        InternalMessageErpModel(
            id = id,
            sender = sender,
            text = text,
            time = time,
            tag = tag,
            isUnread = isUnread,
            messageProfile = messageProfile.toCommunicationProfile(),
            version = version,
            languageCode = languageCode
        )

    private fun InternalMessageErpModel.toRoomEntity(): InternalMessageRoomEntity =
        InternalMessageRoomEntity(
            id = id,
            sender = sender,
            text = text,
            time = time,
            tag = tag,
            isUnread = isUnread,
            messageProfile = messageProfile.toEntityValue() ?: CommunicationProfileV1.InApp,
            version = version,
            languageCode = languageCode
        )
}
