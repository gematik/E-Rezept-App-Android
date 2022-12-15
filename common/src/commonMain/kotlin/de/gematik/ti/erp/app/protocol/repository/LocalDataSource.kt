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

package de.gematik.ti.erp.app.protocol.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.gematik.ti.erp.app.db.entities.v1.AuditEventEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.SyncedTaskEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.db.tryWrite
import de.gematik.ti.erp.app.fhir.model.extractAuditEvents
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.protocol.model.AuditEventData
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.max
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlin.math.max
import kotlin.math.min

// max page size within ui
private const val AuditEventsMaxPageSize = 25

class AuditEventLocalDataSource(
    private val realm: Realm
) {
    suspend fun saveAuditEvents(profileId: ProfileIdentifier, events: JsonElement): Int =
        realm.tryWrite {
            val profile = requireNotNull(
                queryFirst<ProfileEntityV1>(
                    "id = $0",
                    profileId
                )
            ) { "No profile with id = $profileId found!" }

            val totalAuditEventsInBundle = extractAuditEvents(events) { id, taskId, description, timestamp ->
                val entity = copyToRealm(
                    AuditEventEntityV1().apply {
                        this.id = id
                        this.text = description
                        this.timestamp = timestamp.toRealmInstant()
                        this.taskId = taskId
                        this.profile = profile
                    }
                )

                profile.auditEvents += entity
            }

            totalAuditEventsInBundle
        }

    fun latestAuditEventTimestamp(profileId: ProfileIdentifier) =
        realm.query<AuditEventEntityV1>("profile.id = $0", profileId)
            .max<RealmInstant>("timestamp")
            .asFlow()
            .map {
                it?.toInstant()
            }

    data class AuditPagingKey(val offset: Int)

    inner class AuditPagingSource(val profileId: ProfileIdentifier) :
        PagingSource<AuditPagingKey, AuditEventData.AuditEvent>() {
        private val profile = realm.query<ProfileEntityV1>("id = $0", profileId).first()

        override fun getRefreshKey(state: PagingState<AuditPagingKey, AuditEventData.AuditEvent>): AuditPagingKey? =
            null

        override suspend fun load(params: LoadParams<AuditPagingKey>):
            LoadResult<AuditPagingKey, AuditEventData.AuditEvent> {
            val count = params.loadSize
            val key = params.key ?: AuditPagingKey(0)

            val events = requireNotNull(profile.find()).auditEvents
            val result = events.asReversed().subList(key.offset, min(key.offset + count, events.size))

            val nextKey = if (result.size == count) {
                AuditPagingKey(
                    key.offset + result.size
                )
            } else {
                null
            }
            val prevKey = if (key.offset == 0) null else key.copy(offset = max(0, key.offset - count))

            val taskIds = result.distinctBy { it.taskId }.map { it.taskId }

            val medicationTexts = taskIds.associateWith { taskId ->
                taskId?.let {
                    realm.queryFirst<SyncedTaskEntityV1>("taskId = $0", it)?.medicationRequest?.medication?.text
                }
            }

            return LoadResult.Page(
                data = result.map {
                    AuditEventData.AuditEvent(
                        auditId = it.id,
                        medicationText = it.taskId?.let { taskId ->
                            medicationTexts[taskId]
                        },
                        description = it.text,
                        timestamp = it.timestamp.toInstant()
                    )
                },
                nextKey = nextKey,
                prevKey = prevKey,
                itemsBefore = if (prevKey != null) count else 0,
                itemsAfter = if (nextKey != null) count else 0
            )
        }
    }

    fun auditEvents(profileId: ProfileIdentifier): Flow<PagingData<AuditEventData.AuditEvent>> =
        Pager(
            PagingConfig(
                pageSize = AuditEventsMaxPageSize,
                initialLoadSize = AuditEventsMaxPageSize * 2,
                maxSize = AuditEventsMaxPageSize * 3
            ),
            pagingSourceFactory = { AuditPagingSource(profileId) }
        ).flow
}
