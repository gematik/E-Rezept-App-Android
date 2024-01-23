/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.protocol.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.protocol.model.AuditEventData
import de.gematik.ti.erp.app.protocol.repository.AuditEventsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlin.math.max

const val AuditEventsInitialResultsPerPage = 50
const val AuditEventsNextResultsPerPage = 25

class AuditEventsUseCase(
    private val auditRepository: AuditEventsRepository,
    private val dispatchers: DispatchProvider
) {

    fun loadAuditEventsPaged(
        profileId: ProfileIdentifier
    ): Flow<PagingData<AuditEventData.AuditEvent>> {
        return Pager(
            PagingConfig(
                pageSize = AuditEventsNextResultsPerPage,
                initialLoadSize = AuditEventsInitialResultsPerPage,
                maxSize = AuditEventsInitialResultsPerPage * 2
            ),
            pagingSourceFactory = { AuditEventPagingSource(profileId) }
        ).flow.flowOn(dispatchers.io)
    }
    suspend fun loadAuditEvents(profileId: ProfileIdentifier): List<AuditEventData.AuditEvent> =
        withContext(dispatchers.io) {
            val initialResult = auditRepository.downloadAuditEvents(
                profileId,
                null,
                null
            ).getOrThrow()

            if (initialResult.bundleResultCount == AuditEventsInitialResultsPerPage) {
                val auditEvents = initialResult.auditEvents.toMutableList()

                var offset = initialResult.bundleResultCount
                var shouldContinue = true

                while (shouldContinue) {
                    val result = auditRepository.downloadAuditEvents(
                        profileId,
                        offset = offset,
                        count = AuditEventsNextResultsPerPage
                    ).getOrThrow()

                    if (result.bundleResultCount < AuditEventsNextResultsPerPage) {
                        shouldContinue = false
                    }

                    auditEvents += result.auditEvents
                    offset += result.bundleResultCount
                }

                auditEvents
            } else {
                initialResult.auditEvents
            }
        }

    data class AuditEventPagingKey(val offset: Int)

    inner class AuditEventPagingSource(private val profileId: ProfileIdentifier) :
        PagingSource<AuditEventPagingKey, AuditEventData.AuditEvent>() {

        override fun getRefreshKey(
            state: PagingState<AuditEventPagingKey, AuditEventData.AuditEvent>
        ): AuditEventPagingKey? = null

        override suspend fun load(
            params: LoadParams<AuditEventPagingKey>
        ): LoadResult<AuditEventPagingKey, AuditEventData.AuditEvent> {
            val count = params.loadSize

            when (params) {
                is LoadParams.Refresh -> {
                    return auditRepository.downloadAuditEvents(profileId, count, 0)
                        .map {
                            LoadResult.Page(
                                data = it.auditEvents,
                                nextKey = if (it.bundleResultCount == AuditEventsInitialResultsPerPage) {
                                    AuditEventPagingKey(
                                        it.bundleResultCount
                                    )
                                } else {
                                    null
                                },
                                prevKey = null
                            )
                        }.getOrElse { LoadResult.Error(it) }
                }

                is LoadParams.Append, is LoadParams.Prepend -> {
                    val key = params.key!!

                    return auditRepository.downloadAuditEvents(profileId, offset = key.offset, count = count).map {
                        val nextKey = if (it.bundleResultCount == count) {
                            AuditEventPagingKey(
                                key.offset + it.bundleResultCount
                            )
                        } else {
                            null
                        }
                        val prevKey = if (key.offset == 0) null else key.copy(offset = max(0, key.offset - count))

                        LoadResult.Page(
                            data = it.auditEvents,
                            nextKey = nextKey,
                            prevKey = prevKey,
                            itemsBefore = if (prevKey != null) count else 0,
                            itemsAfter = if (nextKey != null) count else 0
                        )
                    }.getOrElse { LoadResult.Error(it) }
                }
            }
        }
    }
}
