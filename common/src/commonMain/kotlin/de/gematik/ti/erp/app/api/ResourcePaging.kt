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

package de.gematik.ti.erp.app.api

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

abstract class ResourcePaging(
    private val dispatchers: DispatchProvider,
    private val maxPageSize: Int
) {
    private val lock = Mutex()

    protected suspend fun downloadPaged(profileId: ProfileIdentifier): Result<Unit> = lock.withLock {
        withContext(dispatchers.IO) {
            downloadAll(profileId)
        }
    }

    private suspend fun downloadAll(profileId: ProfileIdentifier): Result<Unit> {
        while (true) {
            downloadResource(
                profileId = profileId,
                timestamp = toTimestampString(syncedUpTo(profileId)),
                count = maxPageSize
            ).onFailure {
                return@downloadAll Result.failure(it)
            }.onSuccess {
                Napier.d { "Received $it entries" }
                if (it != maxPageSize) {
                    return@downloadAll Result.success(Unit)
                }
            }
        }
    }

    private fun toTimestampString(timestamp: Instant?) =
        timestamp?.let {
            val tm = it.atOffset(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            "gt$tm"
        }

    /**
     * Downloads the specific resource and returns the size of the received list.
     */
    protected abstract suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<Int>

    protected abstract suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant?
}
