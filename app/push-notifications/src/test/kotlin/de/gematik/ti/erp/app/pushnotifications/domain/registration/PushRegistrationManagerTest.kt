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

package de.gematik.ti.erp.app.pushnotifications.domain.registration

import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainManager
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.provider.FcmTokenProvider
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationData
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PushRegistrationManagerTest {
    private val repository = mockk<PusherRepository>()
    private val fcmTokenProvider = mockk<FcmTokenProvider>()
    private val keyChainManager = mockk<PushKeyChainManager>(relaxUnitFun = true)
    private val registrationStorage = mockk<PushRegistrationStorage>()
    private val channels = listOf(PushChannel(id = "erp.task.activate", status = "enabled"))
    private val profileId = "profile-id"

    init {
        coEvery { keyChainManager.restoreFromStorage() } returns Unit
        coEvery { keyChainManager.clear(any()) } returns Result.success(Unit)
    }

    @Test
    fun `changed stored token is deregistered before registration key initialization and channels`() = runTest {
        val events = mutableListOf<String>()
        coEvery { keyChainManager.restoreFromStorage() } coAnswers {
            events += "restore"
            Unit
        }
        coEvery { registrationStorage.load(profileId) } coAnswers {
            events += "load"
            Result.success(
                PushRegistrationData(
                    keyIdentifier = "old-key-id",
                    timeIssCreated = "2026-06",
                    fcmToken = "old-token",
                    pendingFcmToken = "pending-token"
                )
            )
        }
        coEvery { registrationStorage.save(profileId, any()) } coAnswers {
            events += "save"
            Result.success(Unit)
        }
        coEvery { fcmTokenProvider.getToken() } returns "provider-token"
        coEvery { repository.deregister("old-token", profileId) } coAnswers {
            events += "deregister"
            Result.success(Unit)
        }
        coEvery {
            repository.registerDevice(
                pushKey = "pending-token",
                iss = any(),
                keyIdentifier = "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                timeIssCreated = "2026-07",
                deviceName = "device",
                profileId = profileId
            )
        } coAnswers {
            events += "register"
            Result.success(Unit)
        }
        coEvery {
            keyChainManager.initializeAndAdvance(
                iss = any(),
                keyIdentifier = "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                timeIssCreated = "2026-07"
            )
        } coAnswers { events += "key-chain" }
        coEvery { repository.setChannels("pending-token", channels, profileId) } coAnswers {
            events += "channels"
            Result.success(Unit)
        }

        val result = manager(
            initialSharedSecretGenerator = { ByteArray(32) { it.toByte() } },
            keyIdentifierProvider = { "6ba7b810-9dad-11d1-80b4-00c04fd430c8" }
        ).register(profileId = profileId, channels = channels)

        assertTrue(result.isSuccess)
        assertEquals(
            listOf("restore", "load", "deregister", "register", "save", "key-chain", "channels"),
            events
        )
        coVerify(exactly = 0) { fcmTokenProvider.getToken() }
        coVerify(exactly = 1) { keyChainManager.clear("old-key-id") }
    }

    @Test
    fun `registration generates 32 byte ISS UUID key identifier and supplied current month`() = runTest {
        val iss = slot<String>()
        val keyIdentifier = slot<String>()
        val rawIss = ByteArray(32) { it.toByte() }
        coEvery { registrationStorage.load(profileId) } returns Result.success(null)
        coEvery { registrationStorage.save(profileId, any()) } returns Result.success(Unit)
        coEvery { fcmTokenProvider.getToken() } returns "provider-token"
        coEvery {
            repository.registerDevice(
                pushKey = "provider-token",
                iss = capture(iss),
                keyIdentifier = capture(keyIdentifier),
                timeIssCreated = "2026-07",
                deviceName = "device",
                profileId = profileId
            )
        } returns Result.success(Unit)
        coEvery { keyChainManager.initializeAndAdvance(any(), any(), any()) } returns Unit
        coEvery { repository.setChannels("provider-token", channels, profileId) } returns Result.success(Unit)

        val result = manager(
            initialSharedSecretGenerator = { rawIss }
        ).register(profileId = profileId, channels = channels)

        assertTrue(result.isSuccess)
        assertEquals(64, iss.captured.length)
        assertTrue(iss.captured.matches(Regex("[0-9a-f]{64}")))
        assertTrue(rawIss.all { it == 0.toByte() })
        UUID.fromString(keyIdentifier.captured)
        coVerify(exactly = 1) {
            keyChainManager.initializeAndAdvance(
                iss = iss.captured,
                keyIdentifier = keyIdentifier.captured,
                timeIssCreated = "2026-07"
            )
        }
        coVerify(exactly = 1) { registrationStorage.save(profileId, any()) }
    }

    @Test
    fun `registration failure is propagated and stops local key initialization`() = runTest {
        val failure = IllegalStateException("registration failed")
        coEvery { registrationStorage.load(profileId) } returns Result.success(null)
        coEvery { fcmTokenProvider.getToken() } returns "provider-token"
        coEvery { repository.registerDevice(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(failure)

        val result = manager().register(profileId = profileId, channels = channels)

        assertSame(failure, result.exceptionOrNull())
        coVerify(exactly = 0) { keyChainManager.initializeAndAdvance(any(), any(), any()) }
        coVerify(exactly = 0) { repository.setChannels(any(), any(), any()) }
        coVerify(exactly = 0) { registrationStorage.save(any(), any()) }
    }

    @Test
    fun `channel failure is propagated after successful key initialization`() = runTest {
        val failure = IllegalStateException("channel sync failed")
        coEvery { registrationStorage.load(profileId) } returns Result.success(null)
        coEvery { registrationStorage.save(profileId, any()) } returns Result.success(Unit)
        coEvery { fcmTokenProvider.getToken() } returns "provider-token"
        coEvery { repository.registerDevice(any(), any(), any(), any(), any(), any()) } returns
            Result.success(Unit)
        coEvery { keyChainManager.initializeAndAdvance(any(), any(), any()) } returns Unit
        coEvery { repository.setChannels("provider-token", channels, profileId) } returns
            Result.failure(failure)

        val result = manager().register(profileId = profileId, channels = channels)

        assertSame(failure, result.exceptionOrNull())
        coVerify(exactly = 1) { keyChainManager.initializeAndAdvance(any(), any(), any()) }
    }

    @Test
    fun `clear keeps local cleanup best effort when remote deregistration fails`() = runTest {
        coEvery { registrationStorage.load(profileId) } returns Result.success(registration())
        coEvery { repository.deregister("stored-fcm-token", profileId) } returns
            Result.failure(IllegalStateException("remote failure"))
        coEvery { keyChainManager.clear("key-id") } returns Result.success(Unit)
        coEvery { registrationStorage.clear(profileId) } returns Result.success(Unit)

        val result = manager().clear(profileId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deregister("stored-fcm-token", profileId) }
        coVerify(exactly = 1) { keyChainManager.clear("key-id") }
        coVerify(exactly = 1) { registrationStorage.clear(profileId) }
    }

    @Test
    fun `clear uses the stored FCM token instead of fetching a fresh token`() = runTest {
        coEvery { registrationStorage.load(profileId) } returns Result.success(
            PushRegistrationData(
                keyIdentifier = "key-id",
                timeIssCreated = "2026-07",
                fcmToken = "old-registered-token"
            )
        )
        coEvery { fcmTokenProvider.getToken() } returns "new-rotated-token"
        coEvery { repository.deregister("old-registered-token", profileId) } returns Result.success(Unit)
        coEvery { keyChainManager.clear("key-id") } returns Result.success(Unit)
        coEvery { registrationStorage.clear(profileId) } returns Result.success(Unit)

        val result = manager().clear(profileId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deregister("old-registered-token", profileId) }
        coVerify(exactly = 0) { repository.deregister("new-rotated-token", any()) }
    }

    @Test
    fun `clear falls back to a fresh FCM token when no registration exists`() = runTest {
        coEvery { registrationStorage.load(profileId) } returns Result.success(null)
        coEvery { fcmTokenProvider.getToken() } returns "fcm-token"
        coEvery { repository.deregister("fcm-token", profileId) } returns Result.success(Unit)
        coEvery { registrationStorage.clear(profileId) } returns Result.success(Unit)

        val result = manager().clear(profileId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deregister("fcm-token", profileId) }
        coVerify(exactly = 1) { registrationStorage.clear(profileId) }
    }

    @Test
    fun `clear falls back to a fresh FCM token when registration loading fails`() = runTest {
        coEvery { registrationStorage.load(profileId) } returns
            Result.failure(IllegalStateException("load failed"))
        coEvery { fcmTokenProvider.getToken() } returns "fresh-fcm-token"
        coEvery { repository.deregister("fresh-fcm-token", profileId) } returns Result.success(Unit)
        coEvery { registrationStorage.clear(profileId) } returns Result.success(Unit)

        val result = manager().clear(profileId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deregister("fresh-fcm-token", profileId) }
        coVerify(exactly = 1) { registrationStorage.clear(profileId) }
    }

    @Test
    fun `clear always clears registration storage when key storage clear fails`() = runTest {
        coEvery { registrationStorage.load(profileId) } returns Result.success(registration())
        coEvery { repository.deregister(any(), any()) } returns Result.success(Unit)
        coEvery { keyChainManager.clear("key-id") } returns Result.failure(RuntimeException("storage error"))
        coEvery { registrationStorage.clear(profileId) } returns Result.success(Unit)

        val result = manager().clear(profileId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { registrationStorage.clear(profileId) }
    }

    private fun registration() = PushRegistrationData(
        keyIdentifier = "key-id",
        timeIssCreated = "2026-07",
        fcmToken = "stored-fcm-token"
    )

    private fun manager(
        initialSharedSecretGenerator: () -> ByteArray = { ByteArray(32) },
        keyIdentifierProvider: () -> String = { UUID.randomUUID().toString() }
    ) = PushRegistrationManager(
        repository = repository,
        fcmTokenProvider = fcmTokenProvider,
        keyChainManager = keyChainManager,
        registrationStorage = registrationStorage,
        deviceNameProvider = { "device" },
        currentMonthProvider = { "2026-07" },
        initialSharedSecretGenerator = initialSharedSecretGenerator,
        keyIdentifierProvider = keyIdentifierProvider
    )
}
