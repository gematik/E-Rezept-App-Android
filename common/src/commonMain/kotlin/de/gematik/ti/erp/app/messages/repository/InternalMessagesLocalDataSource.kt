/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.messages.repository

import de.gematik.ti.erp.app.db.entities.v1.InternalMessageEntityV1
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class InternalMessagesLocalDataSource(
    private val realm: Realm,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) {
    fun getInternalMessages(): Flow<List<InternalMessageEntityV1>> =
        realm.query<InternalMessageEntityV1>()
            .sort(
                "time",
                Sort.DESCENDING
            )
            .asFlow()
            .map {
                    internalMessages ->
                internalMessages.list
            }
            .flowOn(dispatchers)

    fun getUnreadInternalMessagesCount(): Flow<Long> =
        realm.query<InternalMessageEntityV1>()
            .asFlow()
            .map { internalMessages ->
                internalMessages.list.filter { it.isUnread }.size.toLong()
            }.flowOn(dispatchers)

    fun getLastUpdatedVersion(): Flow<String?> =
        realm.query<InternalMessageEntityV1>()
            .sort(
                "version",
                Sort.DESCENDING
            )
            .asFlow()
            .map {
                    internalMessages ->
                internalMessages.list.firstOrNull()?.version
            }
            .flowOn(dispatchers)

    suspend fun setInternalMessagesAsRead() {
        withContext(dispatchers) {
            realm.write {
                query<InternalMessageEntityV1>().find().forEach {
                    it.isUnread = false
                }
            }
        }
    }

    suspend fun updateInternalMessage(updatedEntity: InternalMessageEntityV1) {
        withContext(dispatchers) {
            realm.write {
                query<InternalMessageEntityV1>("id == $0", updatedEntity.id).find().forEach {
                    it.languageCode = updatedEntity.languageCode
                    it.text = updatedEntity.text
                    it.tag = updatedEntity.tag
                    it.sender = updatedEntity.sender
                }
            }
        }
    }

    suspend fun saveInternalMessage(internalMessageEntityV1: InternalMessageEntityV1) {
        withContext(dispatchers) {
            realm.write {
                copyToRealm(
                    internalMessageEntityV1
                )
            }
        }
    }
}
