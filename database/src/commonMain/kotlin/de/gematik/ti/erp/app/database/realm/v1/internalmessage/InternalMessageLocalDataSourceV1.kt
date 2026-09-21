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

package de.gematik.ti.erp.app.database.realm.v1.internalmessage

import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.communication.model.InternalMessageErpModel
import de.gematik.ti.erp.app.communication.model.toCommunicationProfile
import de.gematik.ti.erp.app.database.api.InternalMessagesLocalDataSource
import de.gematik.ti.erp.app.database.realm.v1.InternalMessageEntityV1
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class InternalMessagesLocalDataSourceV1(
    private val realm: Realm
) : InternalMessagesLocalDataSource {
    override fun getInternalMessages(): Flow<List<InternalMessageErpModel>> =
        realm.query<InternalMessageEntityV1>()
            .sort(
                "time",
                Sort.DESCENDING
            )
            .asFlow()
            .map { internalMessages ->
                internalMessages.list.map { it.toModel() }
            }

    override fun getUnreadInternalMessagesCount(): Flow<Long> =
        realm.query<InternalMessageEntityV1>()
            .asFlow()
            .map { internalMessages ->
                internalMessages.list.filter { it.isUnread }.size.toLong()
            }

    override fun getLastUpdatedVersion(): Flow<String?> =
        realm.query<InternalMessageEntityV1>()
            .sort(
                "version",
                Sort.DESCENDING
            )
            .asFlow()
            .map { internalMessages ->
                internalMessages.list.firstOrNull()?.version
            }

    override suspend fun setInternalMessagesAsRead() {
        realm.write {
            query<InternalMessageEntityV1>().find().forEach {
                it.isUnread = false
            }
        }
    }

    override suspend fun updateInternalMessage(updatedModel: InternalMessageErpModel) {
        realm.write {
            query<InternalMessageEntityV1>("id == $0", updatedModel.id).find().forEach {
                it.languageCode = updatedModel.languageCode
                it.text = updatedModel.text
                it.tag = updatedModel.tag
                it.sender = updatedModel.sender
            }
        }
    }

    override suspend fun saveInternalMessage(internalMessageErpModel: InternalMessageErpModel) {
        realm.write {
            copyToRealm(
                internalMessageErpModel.toEntity()
            )
        }
    }
}

fun InternalMessageEntityV1.toModel(): InternalMessageErpModel =
    InternalMessageErpModel(
        id = id,
        sender = sender,
        text = text,
        time = Instant.fromEpochSeconds(time.epochSeconds, time.nanosecondsOfSecond),
        tag = tag,
        isUnread = isUnread,
        messageProfile = messageProfile.toCommunicationProfile(),
        version = version,
        languageCode = languageCode
    )

fun InternalMessageErpModel.toEntity(): InternalMessageEntityV1 =
    InternalMessageEntityV1().apply {
        id = this@toEntity.id
        sender = this@toEntity.sender
        text = this@toEntity.text
        time = RealmInstant.from(this@toEntity.time.epochSeconds, this@toEntity.time.nanosecondsOfSecond)
        tag = this@toEntity.tag
        isUnread = this@toEntity.isUnread
        messageProfile = this@toEntity.messageProfile.toEntityValue() ?: CommunicationProfileV1.InApp
        version = this@toEntity.version
        languageCode = this@toEntity.languageCode
    }
