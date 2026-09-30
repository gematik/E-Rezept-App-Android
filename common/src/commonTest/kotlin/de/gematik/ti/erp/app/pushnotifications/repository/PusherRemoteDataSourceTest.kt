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

/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package de.gematik.ti.erp.app.pushnotifications.repository

import de.gematik.ti.erp.app.api.ErpService
import de.gematik.ti.erp.app.pushnotifications.model.EmptyJsonObjectResponse
import de.gematik.ti.erp.app.pushnotifications.model.Pusher
import de.gematik.ti.erp.app.pushnotifications.model.PushersResponse
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.model.PushChannelsResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PusherRemoteDataSourceTest {

    private val profileId = "profile-id"
    private lateinit var service: ErpService
    private lateinit var dataSource: PusherRemoteDataSource

    @Before
    fun setUp() {
        service = mockk()
        dataSource = PusherRemoteDataSource(service)
    }

    @Test
    fun `getPushers delegates to ErpService with profileId tag and maps response`() = runTest {
        val pushers = listOf(
            Pusher(
                pushKey = "push-key",
                kind = "http",
                appId = "app-id",
                deviceDisplayName = "device"
            )
        )
        coEvery { service.getPushers(profileId) } returns
            Response.success(PushersResponse(pushers))

        val result = dataSource.getPushers(profileId)

        assertEquals(pushers, result.getOrThrow())
        coVerify(exactly = 1) { service.getPushers(profileId) }
    }

    @Test
    fun `getChannels delegates to ErpService with profileId tag and maps response`() = runTest {
        val channels = listOf(PushChannel(id = "erp.task.activate", status = "enabled"))
        coEvery { service.getPusherChannels(profileId, "push-key") } returns
            Response.success(PushChannelsResponse(channels))

        val result = dataSource.getChannels(profileId, "push-key")

        assertEquals(channels, result.getOrThrow())
        coVerify(exactly = 1) { service.getPusherChannels(profileId, "push-key") }
    }

    @Test
    fun `getPushers preserves HTTP failure as Result failure`() = runTest {
        coEvery { service.getPushers(profileId) } returns
            Response.error(500, "server error".toResponseBody())

        val result = dataSource.getPushers(profileId)

        assertTrue(result.isFailure)
    }

    @Test
    fun `setPusher parses EmptyJsonObjectResponse and returns success`() = runTest {
        coEvery { service.setPusher(eq(profileId), any()) } returns
            Response.success(EmptyJsonObjectResponse())

        val result = dataSource.setPusher(profileId, mockk(relaxed = true))

        assertTrue(result.isSuccess)
    }

    @Test
    fun `deletePusher parses EmptyJsonObjectResponse and returns success`() = runTest {
        coEvery { service.deletePusher(eq(profileId), any()) } returns
            Response.success(EmptyJsonObjectResponse())

        val result = dataSource.deletePusher(profileId, "app-id", "push-key")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `setPusher propagates HTTP 4xx as Result failure`() = runTest {
        coEvery { service.setPusher(eq(profileId), any()) } returns
            Response.error(401, "unauthorized".toResponseBody())

        val result = dataSource.setPusher(profileId, mockk(relaxed = true))

        assertTrue(result.isFailure)
    }
}
