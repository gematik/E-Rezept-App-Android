/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.changelogs

import de.gematik.ti.erp.app.db.entities.v1.InAppMessageEntity
import de.gematik.ti.erp.app.db.entities.v1.InternalMessageEntity
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.db.writeOrCopyToRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class InAppDataSource(
    private val realm: Realm,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) {
    val changeLogs: Flow<List<InAppMessageEntity>>
        get() = realm.query<InternalMessageEntity>().asFlow().mapNotNull {
            it.list.firstOrNull()?.inAppMessageEntity
        }.flowOn(dispatchers)

    val counter: Flow<Long>
        get() = realm.query<InternalMessageEntity>().asFlow().mapNotNull {
            it.list.firstOrNull()?.counter
        }

    val lastUpdatedVersion: Flow<String?>
        get() = realm.query<InternalMessageEntity>()
            .asFlow()
            .map { it.list.firstOrNull()?.lastUpdatedVersion }

    val lastVersion: Flow<String?>
        get() = realm.query<InternalMessageEntity>()
            .asFlow()
            .map { it.list.firstOrNull()?.lastVersion }

    val showWelcomeMessage: Flow<Boolean>
        get() = realm.query<InternalMessageEntity>()
            .asFlow()
            .map {
                it.list.firstOrNull()?.showWelcomeMessage ?: false
            }

    val welcomeMessageTimeStamp: Flow<Instant?>
        get() = realm.query<InternalMessageEntity>()
            .asFlow()
            .map {
                it.list.firstOrNull()?.welcomeMessageTimeStamp?.toInstant()
            }

    suspend fun setInternalMessageAsRead() {
        withContext(dispatchers) {
            realm.writeOrCopyToRealm(::InternalMessageEntity) { entity ->
                entity.inAppMessageEntity.forEach {
                    it.isUnRead = false
                }
                entity.counter = 0
            }
        }
    }

    suspend fun setShowWelcomeMessage() {
        withContext(dispatchers) {
            realm.writeOrCopyToRealm(::InternalMessageEntity) { entity ->
                entity.showWelcomeMessage = true
                entity.welcomeMessageTimeStamp = Clock.System.now().toRealmInstant()
            }
        }
    }

    suspend fun updateChangeLogs(newChangeLogs: RealmList<InAppMessageEntity>, lastVersion: String, inAppLastVersion: String) {
        withContext(dispatchers) {
            realm.writeBlocking {
                queryFirst<InternalMessageEntity>()?.apply {
                    newChangeLogs.forEach { message ->
                        this.inAppMessageEntity = this.inAppMessageEntity.plus(message).toRealmList()
                        this.counter = this.inAppMessageEntity.filter { it.isUnRead }.size.toLong()
                        if (this.lastVersion.isNullOrBlank()) {
                            this.lastVersion = lastVersion
                        }
                        this.lastUpdatedVersion = inAppLastVersion
                    }
                } ?: run {
                    // if No InternalMessageEntity exists, create a new one with lastversion as null as its a first install
                    copyToRealm(
                        InternalMessageEntity().apply {
                            this.inAppMessageEntity = newChangeLogs
                            this.lastVersion = null
                            this.counter = 1
                            this.lastUpdatedVersion = inAppLastVersion
                        }
                    )
                }
            }
        }
    }
}
