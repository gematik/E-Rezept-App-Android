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

import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AuditEventRemoteDataSource(
    private val service: ErpService
) {
    suspend fun getAuditEvents(
        profileId: ProfileIdentifier,
        lastKnownUpdate: Instant?,
        count: Int? = null,
        offset: Int? = null
    ) = safeApiCall(
        errorMessage = "Error getting all audit events"
    ) {
        val dateTimeString: String? =
            lastKnownUpdate?.let {
                "gt${
                it.atOffset(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                }"
            }
        service.getAuditEvents(
            profileId = profileId,
            lastKnownDate = dateTimeString,
            count = count,
            offset = offset
        )
    }
}
