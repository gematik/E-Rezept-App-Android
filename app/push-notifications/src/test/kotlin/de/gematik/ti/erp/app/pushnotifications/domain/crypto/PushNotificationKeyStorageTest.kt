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

package de.gematik.ti.erp.app.pushnotifications.domain.crypto

import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Persistence unit tests for [InMemoryKeyStorage].
 * Note: The storage holds **derived** key material (monthly HKDF outputs) — it never
 * contains the ISS, which lives only in RAM for the lifetime of [PushNotificationKeyRotationService].
 */
class PushNotificationKeyStorageTest {

    private val KEY_ID = "test-key-identifier"

    private fun makeGeneration(month: String, suffix: String = month) = PushNotificationKeyGeneration(
        month = month,
        secret = "secret_$suffix",
        encryptionKey = "encKey_$suffix",
        keyIdentifier = KEY_ID
    )

    private fun buildService(storage: PushNotificationKeyStorage) = PushNotificationKeyRotationService(
        initialSharedSecret = PushNotificationCryptoServiceTest.TEST_ISS,
        timeIssCreated = PushNotificationCryptoServiceTest.TEST_TIME_ISS_CREATED,
        keyIdentifier = KEY_ID,
        hkdf = DefaultHkdfSha256(),
        storage = storage
    )

    private class SaveFailingStorage : PushNotificationKeyStorage {
        override fun save(
            keyIdentifier: String,
            generations: List<PushNotificationKeyGeneration>
        ): Result<Unit> =
            Result.failure(PushNotificationCryptoError.KeyStorageCommitFailed("save", keyIdentifier))

        override fun load(keyIdentifier: String): Result<List<PushNotificationKeyGeneration>> =
            Result.success(emptyList())

        override fun clear(keyIdentifier: String): Result<Unit> =
            Result.success(Unit)
    }

    private class LoadFailingStorage : PushNotificationKeyStorage {
        override fun save(
            keyIdentifier: String,
            generations: List<PushNotificationKeyGeneration>
        ): Result<Unit> =
            Result.success(Unit)

        override fun load(keyIdentifier: String): Result<List<PushNotificationKeyGeneration>> =
            Result.failure(PushNotificationCryptoError.ReEnrollmentRequired(keyIdentifier))

        override fun clear(keyIdentifier: String): Result<Unit> =
            Result.success(Unit)
    }

    @Test
    fun `save and load returns identical generations in same order`() = runTest {
        val storage = InMemoryKeyStorage()
        val generations = listOf(
            makeGeneration("2024-06"),
            makeGeneration("2024-05"),
            makeGeneration("2024-04")
        )

        storage.save(KEY_ID, generations).getOrThrow()
        val restored = storage.load(KEY_ID).getOrThrow()

        assertEquals(generations.size, restored.size)
        generations.forEachIndexed { index, expected ->
            assertEquals("month[$index]", expected.month, restored[index].month)
            assertEquals("secret[$index]", expected.secret, restored[index].secret)
            assertEquals("encryptionKey[$index]", expected.encryptionKey, restored[index].encryptionKey)
            assertEquals("keyIdentifier[$index]", expected.keyIdentifier, restored[index].keyIdentifier)
        }
    }

    @Test
    fun `load on empty storage returns empty list`() = runTest {
        val storage = InMemoryKeyStorage()
        val result = storage.load(KEY_ID).getOrThrow()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `load for unknown key returns empty list`() = runTest {
        val storage = InMemoryKeyStorage()
        storage.save(KEY_ID, listOf(makeGeneration("2024-06"))).getOrThrow()

        val result = storage.load("different-key-id").getOrThrow()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `second save overwrites first save for same key identifier`() = runTest {
        val storage = InMemoryKeyStorage()
        storage.save(KEY_ID, listOf(makeGeneration("2024-04"), makeGeneration("2024-03"))).getOrThrow()

        val updated = listOf(makeGeneration("2024-06"), makeGeneration("2024-05"))
        storage.save(KEY_ID, updated).getOrThrow()

        val restored = storage.load(KEY_ID).getOrThrow()
        assertEquals(2, restored.size)
        assertEquals("2024-06", restored[0].month)
        assertEquals("2024-05", restored[1].month)
    }

    @Test
    fun `clear removes all generations for key identifier`() = runTest {
        val storage = InMemoryKeyStorage()
        storage.save(KEY_ID, listOf(makeGeneration("2024-06"))).getOrThrow()

        storage.clear(KEY_ID).getOrThrow()

        assertTrue(storage.load(KEY_ID).getOrThrow().isEmpty())
    }

    @Test
    fun `clear does not affect generations stored under a different key identifier`() = runTest {
        val storage = InMemoryKeyStorage()
        val otherId = "other-key-id"
        storage.save(KEY_ID, listOf(makeGeneration("2024-06"))).getOrThrow()
        storage.save(otherId, listOf(makeGeneration("2024-05", "other"))).getOrThrow()

        storage.clear(KEY_ID).getOrThrow()

        assertTrue(storage.load(KEY_ID).getOrThrow().isEmpty())
        assertEquals(1, storage.load(otherId).getOrThrow().size)
    }

    @Test
    fun `stored generations encode with storage format version`() {
        val generations = listOf(makeGeneration("2024-06"))

        val encoded = EncryptedSharedPreferencesKeyStorage.encodeStoredGenerations(generations)

        assertTrue(encoded.contains(""""version":1"""))
        val decoded = EncryptedSharedPreferencesKeyStorage.decodeStoredGenerations(encoded)
        assertEquals(generations, decoded)
    }

    @Test
    fun `legacy bare generation list still decodes`() {
        val generations = listOf(makeGeneration("2024-06"))
        val legacyJson = Json.encodeToString(generations)

        val decoded = EncryptedSharedPreferencesKeyStorage.decodeStoredGenerations(legacyJson)

        assertEquals(generations, decoded)
    }

    @Test
    fun `initial key derivation is saved when target is seed month`() = runTest {
        val storage = InMemoryKeyStorage()

        buildService(storage).advanceToMonth("2023-10")

        val saved = storage.load(KEY_ID).getOrThrow()
        assertEquals("2023-10", saved.firstOrNull()?.month)
    }

    @Test
    fun `storage save failure is propagated to caller`() = runTest {
        val service = buildService(SaveFailingStorage())

        try {
            service.advanceToMonth("2023-10")
            fail("Expected key storage save failure")
        } catch (e: PushNotificationCryptoError.KeyStorageCommitFailed) {
            assertEquals(
                "Push key storage commit failed during 'save' for key_identifier '$KEY_ID'.",
                e.message
            )
        }
    }

    @Test
    fun `storage load failure signals re-enrollment instead of empty key chain`() {
        try {
            buildService(LoadFailingStorage())
            fail("Expected re-enrollment signal")
        } catch (e: PushNotificationCryptoError.ReEnrollmentRequired) {
            assertEquals(
                "Stored push key material for key_identifier '$KEY_ID' is unreadable. Re-enrollment is required.",
                e.message
            )
        }
    }

    @Test
    fun `key rotation service restores generations from storage after simulated app restart`() = runTest {
        val storage = InMemoryKeyStorage()

        val serviceFirstLaunch = buildService(storage)
        serviceFirstLaunch.advanceToMonth("2023-11")
        val generationsAfterFirstLaunch = serviceFirstLaunch.getGenerations()
        assertTrue(generationsAfterFirstLaunch.isNotEmpty())

        // App restart: the init block should restore the persisted generations.
        val serviceAfterRestart = buildService(storage)
        val restoredGenerations = serviceAfterRestart.getGenerations()

        assertEquals(
            "Restored generation count should match original",
            generationsAfterFirstLaunch.size,
            restoredGenerations.size
        )
        generationsAfterFirstLaunch.forEachIndexed { i, expected ->
            assertEquals("month[$i]", expected.month, restoredGenerations[i].month)
            assertEquals("secret[$i]", expected.secret, restoredGenerations[i].secret)
            assertEquals("encryptionKey[$i]", expected.encryptionKey, restoredGenerations[i].encryptionKey)
        }
    }

    @Test
    fun `service advancing after restore continues the chain correctly`() = runTest {
        val storage = InMemoryKeyStorage()

        buildService(storage).advanceToMonth("2023-10")

        // Simulate restart
        val serviceAfterRestart = buildService(storage)

        // Advance one more month: should derive Nov-2023 from the persisted Oct-2023 chain, not from ISS
        serviceAfterRestart.advanceToMonth("2023-11")
        val nov = serviceAfterRestart.getGenerationForMonth("2023-11")

        assertEquals("2023-11", nov?.month)
        // Nov-2023 key is a known spec vector from PushNotificationCryptoServiceTest
        assertEquals(
            PushNotificationCryptoServiceTest.EXPECTED_KEY_NOV_2023,
            nov?.encryptionKey
        )
    }
}
