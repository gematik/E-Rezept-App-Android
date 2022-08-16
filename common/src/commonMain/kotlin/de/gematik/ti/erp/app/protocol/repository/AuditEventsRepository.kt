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
import de.gematik.ti.erp.app.prescription.repository.extractResources
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.AuditEvent

private const val AUDIT_EVENT_PAGE_SIZE = 50

class AuditEventsRepository(
    private val remoteDataSource: AuditEventRemoteDataSource,
    private val localDataSource: AuditEventLocalDataSource,
    private val dispatchers: DispatchProvider
) {
    suspend fun downloadAuditEvents(profileId: ProfileIdentifier): Result<Unit> = withContext(dispatchers.IO) {
        while (true) {
            val result = downloadAuditEvents(
                profileId = profileId,
                count = AUDIT_EVENT_PAGE_SIZE
            )
            if (result.isFailure || (result.getOrNull()!! != AUDIT_EVENT_PAGE_SIZE)) {
                break
            }
        }
        Result.success(Unit)
    }

    private suspend fun downloadAuditEvents(
        profileId: ProfileIdentifier,
        count: Int? = null
    ): Result<Int> {
        val syncedUpTo = localDataSource.latestAuditEventTimestamp(profileId).first()
        return remoteDataSource.getAuditEvents(
            profileId = profileId,
            lastKnownUpdate = syncedUpTo,
            count = count
        ).mapCatching { fhirBundle ->
            val events = fhirBundle.extractResources<AuditEvent>()
            localDataSource.saveAuditEvents(profileId, events)

            events.size
        }
    }

    fun auditEvents(profileId: ProfileIdentifier) = localDataSource.auditEvents(profileId).flowOn(dispatchers.IO)
}
