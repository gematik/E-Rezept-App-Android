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
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/**
 * Persistence behavior for [PushNotificationKeyRotationService].
 * Storage holds derived monthly key material, never the ISS.
 */
class PushNotificationKeyStorageTest {

    private val KEY_ID = "test-key-identifier"

    private fun buildService(
        storage: PushNotificationKeyStorage,
        restoredGenerations: List<PushNotificationKeyGeneration> = emptyList()
    ) = if (restoredGenerations.isEmpty()) {
        PushNotificationKeyRotationService.create(
            initialSharedSecret = PushNotificationCryptoServiceTest.TEST_ISS,
            timeIssCreated = PushNotificationCryptoServiceTest.TEST_TIME_ISS_CREATED,
            keyIdentifier = KEY_ID,
            hkdf = DefaultHkdfSha256(),
            storage = storage
        )
    } else {
        PushNotificationKeyRotationService.restore(
            timeIssCreated = PushNotificationCryptoServiceTest.TEST_TIME_ISS_CREATED,
            keyIdentifier = KEY_ID,
            hkdf = DefaultHkdfSha256(),
            storage = storage,
            restoredGenerations = restoredGenerations
        )
    }

    private class SaveFailingStorage : PushNotificationKeyStorage {
        override suspend fun save(
            keyIdentifier: String,
            generations: List<PushNotificationKeyGeneration>
        ): Result<Unit> =
            Result.failure(PushNotificationCryptoError.KeyStorageCommitFailed("save", keyIdentifier))

        override suspend fun load(keyIdentifier: String): Result<List<PushNotificationKeyGeneration>> =
            Result.success(emptyList())

        override suspend fun clear(keyIdentifier: String): Result<Unit> =
            Result.success(Unit)
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
    fun `service advancing after restore continues the chain correctly`() = runTest {
        val storage = InMemoryKeyStorage()

        buildService(storage).advanceToMonth("2023-10")

        // Simulate restart
        val serviceAfterRestart = buildService(storage, storage.load(KEY_ID).getOrThrow())

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
