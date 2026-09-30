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

import de.gematik.ti.erp.app.pushnotifications.BuildConfig.PUSH_GATEWAY_URL_DEV
import de.gematik.ti.erp.app.pushnotifications.BuildConfig.PUSH_GATEWAY_URL_RU
import de.gematik.ti.erp.app.pushnotifications.model.PusherRegistrationRequest
import de.gematik.ti.erp.app.pushnotifications.provider.PushApplicationIdProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultPusherRepositoryTest {

    private val profileId = "profile-id"
    private lateinit var dataSource: PusherRemoteDataSource
    private lateinit var repository: DefaultPusherRepository

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = DefaultPusherRepository(
            dataSource = dataSource,
            pushApplicationIdProvider = object : PushApplicationIdProvider {
                override fun getPushApplicationId(): String = "app-id"
            }
        )
    }

    @Test
    fun `registerDevice builds Fachdienst registration request`() = runTest {
        val request = slot<PusherRegistrationRequest>()
        coEvery { dataSource.setPusher(profileId, capture(request)) } returns Result.success(Unit)

        val result = repository.registerDevice(
            pushKey = "push-key",
            iss = "iss",
            keyIdentifier = "key-id",
            timeIssCreated = "2026-07",
            deviceName = "device",
            profileId = profileId
        )

        assertTrue(result.isSuccess)
        assertEquals("push-key", request.captured.pushKey)
        assertEquals("http", request.captured.kind)
        assertEquals("app-id", request.captured.appId)
        assertEquals("E-Rezept", request.captured.appDisplayName)
        assertEquals("device", request.captured.deviceDisplayName)
        assertEquals("de", request.captured.lang)
        assertEquals(PUSH_GATEWAY_URL_RU, request.captured.data.url)
        assertEquals("aes-hmac-sha256", request.captured.encryption.method)
        assertEquals("2026-07", request.captured.encryption.timeIssCreated)
        assertEquals("iss", request.captured.encryption.iss)
        assertEquals("key-id", request.captured.encryption.keyIdentifier)
        assertTrue(request.captured.append)
    }

    @Test
    fun `deregister uses configured application id`() = runTest {
        coEvery { dataSource.deletePusher(profileId, "app-id", "push-key") } returns Result.success(Unit)

        val result = repository.deregister(pushKey = "push-key", profileId = profileId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { dataSource.deletePusher(profileId, "app-id", "push-key") }
    }

    @Test
    fun `registerDevice uses provided push gateway URL`() = runTest {
        val customUrl = PUSH_GATEWAY_URL_DEV
        val customRepo = DefaultPusherRepository(
            dataSource = dataSource,
            pushApplicationIdProvider = { "app-id" },
            pushGatewayUrlProvider = { customUrl }
        )
        val request = slot<PusherRegistrationRequest>()
        coEvery { dataSource.setPusher(profileId, capture(request)) } returns Result.success(Unit)

        val result = customRepo.registerDevice(
            pushKey = "push-key",
            iss = "iss",
            keyIdentifier = "key-id",
            timeIssCreated = "2026-07",
            deviceName = "device",
            profileId = profileId
        )

        assertTrue(result.isSuccess)
        assertEquals(customUrl, request.captured.data.url)
    }
}
