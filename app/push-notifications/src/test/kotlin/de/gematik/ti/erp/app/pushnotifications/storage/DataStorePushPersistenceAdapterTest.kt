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

package de.gematik.ti.erp.app.pushnotifications.storage

import de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial.PushKeyGenerationEntity
import de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial.PushKeyMaterialEntity
import de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial.PushKeyMaterialLocalDataSource
import de.gematik.ti.erp.app.database.datastore.pushnotification.registration.PushRegistrationEntity
import de.gematik.ti.erp.app.database.datastore.pushnotification.registration.PushRegistrationLocalDataSource
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoError
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DataStorePushPersistenceAdapterTest {
    @Test
    fun `registration adapter maps all fields in both directions`() = runTest {
        val localDataSource = mockk<PushRegistrationLocalDataSource>()
        val savedEntity = slot<PushRegistrationEntity>()
        coEvery { localDataSource.save("profile-1", capture(savedEntity)) } returns Unit
        coEvery { localDataSource.hasCompletedPermissionPrompt("profile-1") } returns true
        coEvery { localDataSource.markPermissionPromptCompleted("profile-1") } returns Unit
        coEvery { localDataSource.load("profile-1") } returns PushRegistrationEntity(
            profileId = "profile-1",
            keyIdentifier = "key-1",
            timeIssCreated = "2026-07",
            fcmToken = "fcm-token",
            pendingFcmToken = "pending-token"
        )
        val storage = DataStorePushRegistrationStorage(localDataSource)
        val registration = PushRegistrationData(
            keyIdentifier = "key-1",
            timeIssCreated = "2026-07",
            fcmToken = "fcm-token",
            pendingFcmToken = "pending-token"
        )

        storage.save("profile-1", registration).getOrThrow()
        storage.markPermissionPromptCompleted("profile-1").getOrThrow()

        assertEquals("profile-1", savedEntity.captured.profileId)
        assertEquals(registration, storage.load("profile-1").getOrThrow())
        assertTrue(storage.hasCompletedPermissionPrompt("profile-1").getOrThrow())
        coVerify(exactly = 1) { localDataSource.markPermissionPromptCompleted("profile-1") }
    }

    @Test
    fun `key material adapter maps retained generations in both directions`() = runTest {
        val localDataSource = mockk<PushKeyMaterialLocalDataSource>()
        val savedGenerations = slot<List<PushKeyGenerationEntity>>()
        coEvery { localDataSource.save("key-1", capture(savedGenerations)) } returns Unit
        coEvery { localDataSource.load("key-1") } returns PushKeyMaterialEntity(
            keyIdentifier = "key-1",
            generations = listOf(entityGeneration())
        )
        val storage = DataStorePushNotificationKeyStorage(localDataSource)
        val generation = domainGeneration()

        storage.save("key-1", listOf(generation)).getOrThrow()

        assertEquals("secret", savedGenerations.captured.single().secret)
        assertEquals(listOf(generation), storage.load("key-1").getOrThrow())
    }

    @Test
    fun `key material read failures require re-enrollment and retain the cause`() = runTest {
        val failure = IllegalStateException("key store failed")
        val localDataSource = mockk<PushKeyMaterialLocalDataSource>()
        coEvery { localDataSource.load("key-1") } throws failure

        val result = DataStorePushNotificationKeyStorage(localDataSource).load("key-1")

        val error = assertIs<PushNotificationCryptoError.ReEnrollmentRequired>(result.exceptionOrNull())
        assertSame(failure, error.cause)
        assertTrue(result.isFailure)
        coVerify(exactly = 1) { localDataSource.load("key-1") }
    }

    private fun domainGeneration() = PushNotificationKeyGeneration(
        encryptionKey = "encryption-key",
        secret = "secret",
        month = "2026-07",
        keyIdentifier = "key-1"
    )

    private fun entityGeneration() = PushKeyGenerationEntity(
        encryptionKey = "encryption-key",
        secret = "secret",
        month = "2026-07",
        keyIdentifier = "key-1"
    )
}
