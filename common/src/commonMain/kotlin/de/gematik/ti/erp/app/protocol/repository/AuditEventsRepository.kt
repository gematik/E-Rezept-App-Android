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

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.ResourcePaging
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.Instant

private const val AuditEventsMaxPageSize = 50

class AuditEventsRepository(
    private val remoteDataSource: AuditEventRemoteDataSource,
    private val localDataSource: AuditEventLocalDataSource,
    private val dispatchers: DispatchProvider
) : ResourcePaging(dispatchers, AuditEventsMaxPageSize) {

    suspend fun downloadAuditEvents(profileId: ProfileIdentifier) = downloadPaged(profileId)

    fun auditEvents(profileId: ProfileIdentifier) = localDataSource.auditEvents(profileId).flowOn(dispatchers.IO)

    override suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<Int> =
        remoteDataSource.getAuditEvents(
            profileId = profileId,
            lastKnownUpdate = timestamp,
            count = count
        ).mapCatching { fhirBundle ->
            withContext(dispatchers.IO) {
                localDataSource.saveAuditEvents(profileId, fhirBundle)
            }
        }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        localDataSource.latestAuditEventTimestamp(profileId).first()
}
