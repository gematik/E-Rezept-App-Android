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

package de.gematik.ti.erp.app.protocol.usecase

import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.gematik.ti.erp.app.fhir.audit.model.erp.FhirAuditEventErpModel
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.protocol.model.AuditEventPagingKey
import de.gematik.ti.erp.app.protocol.repository.AuditEventsRepository
import kotlin.math.max

class AuditEventPagingSource(
    private val profileId: ProfileIdentifier,
    private val auditRepository: AuditEventsRepository
) : PagingSource<AuditEventPagingKey, FhirAuditEventErpModel>() {

    override fun getRefreshKey(
        state: PagingState<AuditEventPagingKey, FhirAuditEventErpModel>
    ): AuditEventPagingKey? = null

    override suspend fun load(
        params: LoadParams<AuditEventPagingKey>
    ): LoadResult<AuditEventPagingKey, FhirAuditEventErpModel> {
        val count = params.loadSize

        when (params) {
            is LoadParams.Refresh -> {
                return auditRepository.downloadAuditEvents(
                    profileId = profileId,
                    count = count,
                    offset = 0
                )
                    .map {
                        LoadResult.Page(
                            data = it?.auditEvents ?: emptyList(),
                            nextKey = AuditEventPagingKey.from(
                                currentOffset = params.key?.offset,
                                auditEvents = it?.auditEvents
                            ),
                            prevKey = null
                        )
                    }.getOrElse { LoadResult.Error(it) }
            }

            is LoadParams.Append -> {
                return auditRepository.downloadAuditEvents(
                    profileId = profileId,
                    offset = params.key.offset,
                    count = count
                ).map {
                    val prevKey = if (params.key.offset == 0) null else params.key.copy(
                        offset = max(0, params.key.offset - count)
                    )

                    val nextKey = AuditEventPagingKey.from(
                        currentOffset = params.key.offset,
                        auditEvents = it?.auditEvents
                    )

                    LoadResult.Page(
                        data = it?.auditEvents ?: emptyList(),
                        nextKey = nextKey,
                        prevKey = prevKey,
                        itemsBefore = if (params.key.offset == 0) 0 else count,
                        itemsAfter = if (nextKey != null) count else 0
                    )
                }.getOrElse { LoadResult.Error(it) }
            }

            is LoadParams.Prepend -> {
                // prepend is not supported
                return LoadResult.Error(UnsupportedOperationException("Prepend is not supported"))
            }
        }
    }
}
