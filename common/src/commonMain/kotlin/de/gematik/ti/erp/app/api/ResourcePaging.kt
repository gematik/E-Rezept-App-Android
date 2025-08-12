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

package de.gematik.ti.erp.app.api

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

abstract class ResourcePaging<T>(
    private val dispatchers: DispatchProvider,
    private val maxPageSize: Int,
    private val maxPages: Int = Int.MAX_VALUE
) {
    private val lock = Mutex()

    protected open val tag: String = "ResourcePaging"

    protected suspend fun downloadPaged(profileId: ProfileIdentifier): Result<Unit> =
        lock.withLock {
            withContext(dispatchers.io) {
                downloadAll(profileId)
            }
        }

    protected suspend fun downloadPaged(profileId: ProfileIdentifier, fold: (prev: T?, next: T) -> T): Result<T?> =
        lock.withLock {
            withContext(dispatchers.io) {
                downloadAll(profileId, fold)
            }
        }

    private suspend fun downloadAll(
        profileId: ProfileIdentifier
    ): Result<Unit> {
        var pages = 0
        var condition = true
        while (condition && pages < maxPages) {
            val r = downloadResource(
                profileId = profileId,
                timestamp = syncedUpTo(profileId).toTimestampString(),
                count = maxPageSize
            ).fold(
                onSuccess = {
                    Napier.d {
                        "$tag - Received ${it.count} entries"
                    }
                    if (it.count != maxPageSize) {
                        condition = false
                    }
                    it
                },
                onFailure = {
                    it
                }
            )

            if (r is Throwable) {
                return Result.failure(r)
            }

            pages++
        }
        return Result.success(Unit)
    }

    private suspend fun downloadAll(
        profileId: ProfileIdentifier,
        fold: (prev: T?, next: T) -> T
    ): Result<T?> {
        var pages = 0
        var result: T? = null
        var condition = true
        while (condition && pages < maxPages) {
            val r = downloadResource(
                profileId = profileId,
                timestamp = syncedUpTo(profileId).toTimestampString(),
                count = maxPageSize
            ).fold(
                onSuccess = {
                    Napier.d { "Received ${it.count} entries" }
                    if (it.count != maxPageSize) {
                        Napier.d { "All downloaded: ${it.count} != $maxPageSize" }
                        condition = false
                    }
                    it
                },
                onFailure = {
                    Napier.e(it) { "Failed to download" }
                    it
                }
            )

            when (r) {
                is Throwable -> {
                    return Result.failure(r)
                }

                is ResourceResult<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    result = fold(result, r.data as T)
                }
            }

            pages++
        }
        return Result.success(result)
    }

    class ResourceResult<T>(val count: Int, val data: T)

    /**
     * Downloads the specific resource and returns the size of the received list.
     */
    protected abstract suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourceResult<T>>

    protected abstract suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant?
    companion object {
        fun Instant?.toTimestampString() =
            this?.let {
                // TODO: remove java date time stuff
                val tm = it.toJavaInstant().atOffset(ZoneOffset.UTC)
                    .truncatedTo(ChronoUnit.SECONDS)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                "gt$tm"
            }
    }
}
