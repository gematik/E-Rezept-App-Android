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

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class PageTestContainer(dispatchers: DispatchProvider) : ResourcePaging<Unit>(dispatchers, 50) {

    suspend fun downloadAll(profileId: ProfileIdentifier) = downloadPaged(profileId)

    override suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourceResult<Unit>> {
        assertEquals("gt2022-03-22T12:30:00Z", timestamp)
        assertEquals(50, count)
        return Result.success(ResourceResult(10, Unit))
    }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant =
        Instant.parse("2022-03-22T12:30:00.00Z")
}

private class PageTestContainerWithError(dispatchers: DispatchProvider) :
    ResourcePaging<Unit>(dispatchers, 50) {

    suspend fun downloadAll(profileId: ProfileIdentifier) = downloadPaged(profileId)

    override suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourceResult<Unit>> {
        assertEquals("gt2022-03-22T12:30:00Z", timestamp)
        assertEquals(50, count)
        return Result.failure(IllegalArgumentException())
    }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant =
        Instant.parse("2022-03-22T12:30:00.00Z")
}

private class PageTestContainerWithMultiplePages(dispatchers: DispatchProvider) :
    ResourcePaging<Unit>(dispatchers, 50) {

    private var page = 0

    suspend fun downloadAll(profileId: ProfileIdentifier) = downloadPaged(profileId)

    override suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourceResult<Unit>> {
        assertEquals("gt2022-03-22T12:30:00Z", timestamp)
        assertEquals(50, count)
        return when (page) {
            0 -> Result.success(ResourceResult(50, Unit))
            1 -> Result.success(ResourceResult(30, Unit))
            else -> error("")
        }.also {
            page++
        }
    }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        Instant.parse("2022-03-22T12:30:00.00Z")
}

private class PageTestContainerWithCustomReturn(dispatchers: DispatchProvider) :
    ResourcePaging<Int>(dispatchers, 50) {

    private var page = 0

    suspend fun downloadAll(profileId: ProfileIdentifier) =
        downloadPaged(profileId) { prev: Int?, next: Int -> (prev ?: 0) + next }

    override suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourceResult<Int>> {
        assertEquals("gt2022-03-22T12:30:00Z", timestamp)
        assertEquals(50, count)
        return when (page) {
            0 -> Result.success(ResourceResult(50, 1))
            1 -> Result.success(ResourceResult(30, 1))
            else -> error("")
        }.also {
            page++
        }
    }

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? =
        Instant.parse("2022-03-22T12:30:00.00Z")
}

@OptIn(ExperimentalCoroutinesApi::class)
class ResourcePagingTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Test
    fun `paging with less results than max page size`() = runTest {
        val paging = PageTestContainer(coroutineRule.dispatchers)
        val result = paging.downloadAll("")
        assertTrue { result.isSuccess }
    }

    @Test
    fun `paging with error`() = runTest {
        val paging = PageTestContainerWithError(coroutineRule.dispatchers)
        val result = paging.downloadAll("")
        assertTrue { result.isFailure }
    }

    @Test
    fun `paging with multiple pages`() = runTest {
        val paging = PageTestContainerWithMultiplePages(coroutineRule.dispatchers)
        val result = paging.downloadAll("")

        assertTrue { result.isSuccess }
    }

    @Test
    fun `paging with multiple pages and return value`() = runTest {
        val paging = PageTestContainerWithCustomReturn(coroutineRule.dispatchers)
        val result = paging.downloadAll("")
        assertTrue { result.isSuccess }
        assertEquals(2, result.getOrNull())
    }
}
