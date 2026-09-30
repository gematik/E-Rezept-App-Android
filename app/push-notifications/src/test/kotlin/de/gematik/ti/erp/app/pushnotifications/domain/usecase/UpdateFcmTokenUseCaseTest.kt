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

package de.gematik.ti.erp.app.pushnotifications.domain.usecase

import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.registration.PushRegistrationManager
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.model.PushChannelStatus
import de.gematik.ti.erp.app.pushnotifications.provider.FcmTokenProvider
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationData
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class UpdateFcmTokenUseCaseTest {
    private val registrationManager = mockk<PushRegistrationManager>()
    private val registrationStorage = mockk<PushRegistrationStorage>()
    private val idpUseCase = mockk<IdpUseCase>()
    private val pusherRepository = mockk<PusherRepository>()
    private val fcmTokenProvider = mockk<FcmTokenProvider>()
    private val channels = listOf(PushChannel("erp.task.activate", PushChannelStatus.ENABLED))

    private fun registration(fcmToken: String) = PushRegistrationData(
        keyIdentifier = "key-id",
        timeIssCreated = "2026-06",
        fcmToken = fcmToken
    )

    @Test
    fun `no registered profiles is a no-op success`() = runTest {
        coEvery { registrationStorage.loadAll() } returns Result.success(emptyMap())

        val result = useCase()("new-token")

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { registrationManager.register(any(), any(), any()) }
        coVerify(exactly = 0) { registrationStorage.save(any(), any()) }
    }

    @Test
    fun `app start without consented registrations does not request an FCM token`() = runTest {
        coEvery { registrationStorage.loadAll() } returns Result.success(emptyMap())

        val result = useCase().syncAtAppStart()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { fcmTokenProvider.getToken() }
        coVerify(exactly = 0) { registrationManager.register(any(), any(), any()) }
    }

    @Test
    fun `app start synchronizes stored registrations against the current FCM token`() = runTest {
        coEvery { registrationStorage.loadAll() } returns
            Result.success(mapOf("profile-id" to registration("old-token")))
        coEvery { fcmTokenProvider.getToken() } returns "current-token"
        coEvery { idpUseCase.loadAccessToken("profile-id", false, any()) } returns "bearer"
        coEvery { pusherRepository.getChannels("old-token", "profile-id") } returns Result.success(channels)
        coEvery {
            registrationManager.register(
                profileId = "profile-id",
                pushKeyOverride = "current-token",
                channels = channels
            )
        } returns Result.success(Unit)

        val result = useCase().syncAtAppStart()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fcmTokenProvider.getToken() }
        coVerify(exactly = 1) {
            registrationManager.register(
                profileId = "profile-id",
                pushKeyOverride = "current-token",
                channels = channels
            )
        }
    }

    @Test
    fun `unchanged token at app start skips authentication and pending token persistence`() = runTest {
        coEvery { registrationStorage.loadAll() } returns
            Result.success(mapOf("profile-id" to registration("same-token")))
        coEvery { fcmTokenProvider.getToken() } returns "same-token"

        val result = useCase().syncAtAppStart()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { fcmTokenProvider.getToken() }
        coVerify(exactly = 0) { idpUseCase.loadAccessToken(any(), any(), any()) }
        coVerify(exactly = 0) { registrationStorage.save(any(), any()) }
        coVerify(exactly = 0) { registrationManager.register(any(), any(), any()) }
    }

    @Test
    fun `authenticated rotation re-registers every registered profile with its channel state`() = runTest {
        coEvery { registrationStorage.loadAll() } returns
            Result.success(mapOf("profile-id" to registration("old-token")))
        coEvery { idpUseCase.loadAccessToken("profile-id", false, any()) } returns "bearer"
        coEvery { pusherRepository.getChannels("old-token", "profile-id") } returns Result.success(channels)
        coEvery {
            registrationManager.register(
                profileId = "profile-id",
                pushKeyOverride = "new-token",
                channels = channels
            )
        } returns Result.success(Unit)

        val result = useCase()("new-token")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            registrationManager.register(
                profileId = "profile-id",
                pushKeyOverride = "new-token",
                channels = channels
            )
        }
        coVerify(exactly = 0) { registrationStorage.save(any(), any()) }
    }

    @Test
    fun `missing authentication persists pending token without losing old pushkey`() = runTest {
        val saved = slot<PushRegistrationData>()
        coEvery { registrationStorage.loadAll() } returns
            Result.success(mapOf("profile-id" to registration("old-token")))
        coEvery { idpUseCase.loadAccessToken("profile-id", false, any()) } throws
            IllegalStateException("not authenticated")
        coEvery { registrationStorage.save("profile-id", capture(saved)) } returns Result.success(Unit)

        val result = useCase()("new-token")

        assertTrue(result.isSuccess)
        assertEquals("old-token", saved.captured.fcmToken)
        assertEquals("new-token", saved.captured.pendingFcmToken)
        coVerify(exactly = 0) { registrationManager.register(any(), any(), any()) }
    }

    @Test
    fun `authentication cancellation is rethrown without persisting a pending token`() = runTest {
        val cancellation = CancellationException("cancelled")
        coEvery { registrationStorage.loadAll() } returns
            Result.success(mapOf("profile-id" to registration("old-token")))
        coEvery { idpUseCase.loadAccessToken("profile-id", false, any()) } throws cancellation

        val thrown = assertFailsWith<CancellationException> {
            useCase()("new-token")
        }

        assertEquals(cancellation.message, thrown.message)
        coVerify(exactly = 0) { registrationStorage.save(any(), any()) }
        coVerify(exactly = 0) { registrationManager.register(any(), any(), any()) }
    }

    @Test
    fun `pending token storage failure is propagated`() = runTest {
        val failure = IllegalStateException("storage failed")
        coEvery { registrationStorage.loadAll() } returns
            Result.success(mapOf("profile-id" to registration("old-token")))
        coEvery { idpUseCase.loadAccessToken("profile-id", false, any()) } throws
            IllegalStateException("not authenticated")
        coEvery { registrationStorage.save("profile-id", any()) } returns Result.failure(failure)

        val result = useCase()("new-token")

        assertSame(failure, result.exceptionOrNull())
    }

    @Test
    fun `registration failure is returned to Firebase service boundary`() = runTest {
        val failure = IllegalStateException("registration failed")
        coEvery { registrationStorage.loadAll() } returns
            Result.success(mapOf("profile-id" to registration("old-token")))
        coEvery { idpUseCase.loadAccessToken("profile-id", false, any()) } returns "bearer"
        coEvery { pusherRepository.getChannels("old-token", "profile-id") } returns Result.success(channels)
        coEvery {
            registrationManager.register(any(), any(), any())
        } returns Result.failure(failure)

        val result = useCase()("new-token")

        assertSame(failure, result.exceptionOrNull())
    }

    @Test
    fun `one profile failure does not prevent synchronization of another profile`() = runTest {
        val firstFailure = IllegalStateException("first profile failed")
        coEvery { registrationStorage.loadAll() } returns Result.success(
            linkedMapOf(
                "profile-1" to registration("old-token-1"),
                "profile-2" to registration("old-token-2")
            )
        )
        coEvery { idpUseCase.loadAccessToken(any(), false, any()) } returns "bearer"
        coEvery { pusherRepository.getChannels("old-token-1", "profile-1") } returns
            Result.failure(firstFailure)
        coEvery { pusherRepository.getChannels("old-token-2", "profile-2") } returns
            Result.success(channels)
        coEvery {
            registrationManager.register("profile-2", "new-token", channels)
        } returns Result.success(Unit)

        val result = useCase()("new-token")

        assertSame(firstFailure, result.exceptionOrNull())
        coVerify(exactly = 1) {
            registrationManager.register("profile-2", "new-token", channels)
        }
    }

    private fun useCase() = UpdateFcmTokenUseCase(
        registrationManager = registrationManager,
        registrationStorage = registrationStorage,
        idpUseCase = idpUseCase,
        pusherRepository = pusherRepository,
        fcmTokenProvider = fcmTokenProvider
    )
}
