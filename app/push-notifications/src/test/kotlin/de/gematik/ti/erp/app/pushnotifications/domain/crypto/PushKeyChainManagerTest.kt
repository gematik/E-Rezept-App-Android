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

package de.gematik.ti.erp.app.pushnotifications.domain.crypto

import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationData
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PushKeyChainManagerTest {
    @Test
    fun `concurrent restoration is idempotent and loads persistence once`() = runTest {
        val registrationStorage = InMemoryRegistrationStorage(
            "profile-1" to registration("key-1")
        )
        val keyStorage = InMemoryKeyStorage().apply {
            save("key-1", listOf(generation("key-1"))).getOrThrow()
        }
        val manager = manager(registrationStorage, keyStorage)

        (1..10).map { async { manager.restoreFromStorage() } }.awaitAll()

        assertEquals(1, registrationStorage.loadAllCalls)
        assertEquals(setOf("key-1"), manager.knownKeyIdentifiers())
        assertEquals("2026-07", manager.getLatestGeneration("key-1")?.month)
    }

    @Test
    fun `registration without usable key material is cleared during restoration`() = runTest {
        val registrationStorage = InMemoryRegistrationStorage(
            "profile-1" to registration("key-1")
        )
        val manager = manager(registrationStorage, InMemoryKeyStorage())

        manager.restoreFromStorage()

        assertTrue(manager.knownKeyIdentifiers().isEmpty())
        assertEquals(null, registrationStorage.load("profile-1").getOrThrow())
    }

    @Test
    fun `new registration saved after restoration is not removed before first generation`() = runTest {
        val registrationStorage = InMemoryRegistrationStorage()
        val keyStorage = InMemoryKeyStorage()
        val manager = manager(registrationStorage, keyStorage)
        manager.restoreFromStorage()
        registrationStorage.save("profile-1", registration("key-1")).getOrThrow()

        manager.initializeAndAdvance(
            iss = "00".repeat(32),
            keyIdentifier = "key-1",
            timeIssCreated = "2026-07"
        )

        assertNotNull(registrationStorage.load("profile-1").getOrThrow())
        assertTrue(keyStorage.load("key-1").getOrThrow().isNotEmpty())
    }

    @Test
    fun `initialization advances chain to current month`() = runTest {
        val manager = manager(
            registrationStorage = InMemoryRegistrationStorage(),
            keyStorage = InMemoryKeyStorage(),
            currentMonth = "2026-07"
        )

        manager.initializeAndAdvance(
            iss = "00".repeat(32),
            keyIdentifier = "key-1",
            timeIssCreated = "2026-05"
        )

        assertEquals("2026-07", manager.getLatestGeneration("key-1")?.month)
    }

    @Test
    fun `initialization discards initial shared secret after persisting derived keys`() = runTest {
        val service = PushNotificationKeyRotationService.create(
            initialSharedSecret = "00".repeat(32),
            timeIssCreated = "2026-07",
            keyIdentifier = "key-1",
            hkdf = DefaultHkdfSha256(),
            storage = InMemoryKeyStorage(),
            currentMonthProvider = { "2026-07" }
        )

        service.advanceToMonth("2026-07")

        assertTrue(!service.hasInitialSharedSecret())
    }

    @Test
    fun `restored chain has no initial shared secret`() = runTest {
        val service = PushNotificationKeyRotationService.restore(
            timeIssCreated = "2026-07",
            keyIdentifier = "key-1",
            hkdf = DefaultHkdfSha256(),
            restoredGenerations = listOf(generation("key-1"))
        )

        assertTrue(!service.hasInitialSharedSecret())
    }

    @Test
    fun `restored future chain is not moved backwards`() = runTest {
        val registrationStorage = InMemoryRegistrationStorage(
            "profile-1" to registration("key-1")
        )
        val keyStorage = InMemoryKeyStorage().apply {
            save("key-1", listOf(generation("key-1", month = "2026-07"))).getOrThrow()
        }
        val manager = manager(registrationStorage, keyStorage, currentMonth = "2026-05")

        manager.advanceToCurrentMonth("key-1")

        assertEquals("2026-07", manager.getLatestGeneration("key-1")?.month)
    }

    @Test
    fun `advance all moves every restored chain to current month`() = runTest {
        val registrationStorage = InMemoryRegistrationStorage(
            "profile-1" to registration("key-1"),
            "profile-2" to registration("key-2")
        )
        val keyStorage = InMemoryKeyStorage().apply {
            save("key-1", listOf(generation("key-1", month = "2026-05"))).getOrThrow()
            save("key-2", listOf(generation("key-2", month = "2026-06"))).getOrThrow()
        }
        val manager = manager(registrationStorage, keyStorage, currentMonth = "2026-07")

        manager.advanceAllToCurrentMonth()

        assertEquals("2026-07", manager.getLatestGeneration("key-1")?.month)
        assertEquals("2026-07", manager.getLatestGeneration("key-2")?.month)
    }

    @Test
    fun `clearing a chain removes persisted and in-memory key material`() = runTest {
        val keyStorage = InMemoryKeyStorage()
        val manager = manager(InMemoryRegistrationStorage(), keyStorage)
        manager.initializeAndAdvance(
            iss = "00".repeat(32),
            keyIdentifier = "key-1",
            timeIssCreated = "2026-07"
        )

        manager.clear("key-1").getOrThrow()

        assertTrue(keyStorage.load("key-1").getOrThrow().isEmpty())
        assertTrue("key-1" !in manager.knownKeyIdentifiers())
    }

    @Test
    fun `failed restoration can be retried`() = runTest {
        val registrationStorage = InMemoryRegistrationStorage().apply {
            loadAllFailure = IllegalStateException("read failed")
        }
        val manager = manager(registrationStorage, InMemoryKeyStorage())

        assertFailsWith<IllegalStateException> { manager.restoreFromStorage() }
        registrationStorage.loadAllFailure = null

        manager.restoreFromStorage()
        assertEquals(2, registrationStorage.loadAllCalls)
    }

    private fun manager(
        registrationStorage: PushRegistrationStorage,
        keyStorage: PushNotificationKeyStorage,
        currentMonth: String = "2026-07"
    ) = PushKeyChainManager(
        hkdf = DefaultHkdfSha256(),
        keyStorage = keyStorage,
        registrationStorage = registrationStorage,
        currentMonthProvider = { currentMonth }
    )

    private fun registration(keyIdentifier: String) = PushRegistrationData(
        keyIdentifier = keyIdentifier,
        timeIssCreated = "2026-07",
        fcmToken = "fcm-token"
    )

    private fun generation(
        keyIdentifier: String,
        month: String = "2026-07"
    ) = PushNotificationKeyGeneration(
        encryptionKey = "11".repeat(32),
        secret = "22".repeat(32),
        month = month,
        keyIdentifier = keyIdentifier
    )

    private class InMemoryRegistrationStorage(
        vararg registrations: Pair<ProfileIdentifier, PushRegistrationData>
    ) : PushRegistrationStorage {
        private val registrations = ConcurrentHashMap(registrations.toMap())
        var loadAllCalls = 0
        var loadAllFailure: Throwable? = null

        override suspend fun save(
            profileId: ProfileIdentifier,
            data: PushRegistrationData
        ): Result<Unit> = runCatching {
            registrations[profileId] = data
        }

        override suspend fun load(profileId: ProfileIdentifier): Result<PushRegistrationData?> =
            Result.success(registrations[profileId])

        override suspend fun loadAll(): Result<Map<ProfileIdentifier, PushRegistrationData>> {
            loadAllCalls++
            return loadAllFailure?.let(Result.Companion::failure)
                ?: Result.success(registrations.toMap())
        }

        override suspend fun hasCompletedPermissionPrompt(
            profileId: ProfileIdentifier
        ): Result<Boolean> = Result.success(false)

        override suspend fun markPermissionPromptCompleted(
            profileId: ProfileIdentifier
        ): Result<Unit> = Result.success(Unit)

        override suspend fun clear(profileId: ProfileIdentifier): Result<Unit> = runCatching {
            registrations.remove(profileId)
            Unit
        }
    }
}
