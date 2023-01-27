/*
 * Copyright (c) 2023 gematik GmbH
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
import de.gematik.ti.erp.app.prescription.repository.model.SimpleAuditEvent
import de.gematik.ti.erp.app.protocol.repository.model.ProtocolRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.max

class ProtocolUseCase(
    private val repository: ProtocolRepository
) {
    data class AuditPagingKey(val offset: Int)

    inner class PharmacyPagingSource : PagingSource<AuditPagingKey, ProtocolUseCaseData.ProtocolEntry>() {

        override fun getRefreshKey(
            state: PagingState<AuditPagingKey, ProtocolUseCaseData.ProtocolEntry>
        ): AuditPagingKey? = null

        override suspend fun load(params: LoadParams<AuditPagingKey>):
            LoadResult<AuditPagingKey, ProtocolUseCaseData.ProtocolEntry> {
            val count = params.loadSize
            val key = params.key ?: AuditPagingKey(0)

            val resultSearchBundle =
                repository.downloadAuditEvents(offset = key.offset, count = count)

            return resultSearchBundle.fold(
                onSuccess = { events ->
                    val nextKey = if (events.size == count) {
                        AuditPagingKey(
                            key.offset + events.size
                        )
                    } else {
                        null
                    }
                    val prevKey = if (key.offset == 0) null else key.copy(offset = max(0, key.offset - count))

                    LoadResult.Page(
                        data = mapEvents(events),
                        nextKey = nextKey,
                        prevKey = prevKey,
                        itemsBefore = if (prevKey != null) count else 0,
                        itemsAfter = if (nextKey != null) count else 0
                    )
                },
                onFailure = {
                    LoadResult.Error(it)
                }
            )
        }
    }

    fun loadProtocol(): Flow<PagingData<ProtocolUseCaseData.ProtocolEntry>> =
        Pager(
            PagingConfig(
                pageSize = 10,
                initialLoadSize = 50,
                maxSize = 50 * 2
            ),
            pagingSourceFactory = { PharmacyPagingSource() }
        ).flow

    private fun mapEvents(events: List<SimpleAuditEvent>): List<ProtocolUseCaseData.ProtocolEntry> =
        events.map {
            ProtocolUseCaseData.ProtocolEntry(
                text = it.text,
                timestamp = it.timestamp,
                taskId = it.taskId
            )
        }
}
