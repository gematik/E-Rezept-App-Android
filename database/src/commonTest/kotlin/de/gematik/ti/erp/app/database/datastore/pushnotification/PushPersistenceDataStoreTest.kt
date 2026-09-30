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

package de.gematik.ti.erp.app.database.datastore.pushnotification

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import de.gematik.ti.erp.app.database.datastore.DataStoreCryptography
import de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial.PushKeyGenerationEntity
import de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial.PushKeyMaterialEntity
import de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial.PushKeyMaterialEntitySchema
import de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial.PushKeyMaterialEntitySerializer
import de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial.PushKeyMaterialLocalDataSource
import de.gematik.ti.erp.app.database.datastore.pushnotification.registration.PushRegistrationEntity
import de.gematik.ti.erp.app.database.datastore.pushnotification.registration.PushRegistrationEntitySchema
import de.gematik.ti.erp.app.database.datastore.pushnotification.registration.PushRegistrationEntitySerializer
import de.gematik.ti.erp.app.database.datastore.pushnotification.registration.PushRegistrationLocalDataSource
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class PushPersistenceDataStoreTest {
    private val cryptography = XorCryptography()

    @Test
    fun `registration serializer encrypts and round trips every field`() = runTest {
        val serializer = PushRegistrationEntitySerializer(cryptography)
        val expected = PushRegistrationEntitySchema(
            registrations = mapOf(
                "profile-1" to PushRegistrationEntity(
                    profileId = "profile-1",
                    keyIdentifier = "key-1",
                    timeIssCreated = "2026-07",
                    fcmToken = "fcm-token",
                    pendingFcmToken = "pending-token"
                )
            ),
            completedPermissionPromptProfiles = setOf("profile-1")
        )

        val encoded = serializer.write(expected)

        assertFalse(encoded.decodeToString().contains("fcm-token"))
        assertEquals(expected, serializer.read(encoded))
    }

    @Test
    fun `key material serializer encrypts and round trips retained generations`() = runTest {
        val serializer = PushKeyMaterialEntitySerializer(cryptography)
        val expected = PushKeyMaterialEntitySchema(
            keyMaterial = mapOf(
                "key-1" to PushKeyMaterialEntity(
                    keyIdentifier = "key-1",
                    generations = listOf(generation("key-1", "2026-07"), generation("key-1", "2026-06"))
                )
            )
        )

        val encoded = serializer.write(expected)

        assertFalse(encoded.decodeToString().contains("secret-2026-07"))
        assertEquals(expected, serializer.read(encoded))
    }

    @Test
    fun `serializers reject corrupt encrypted data`() = runTest {
        assertCorruption {
            PushRegistrationEntitySerializer(cryptography).read("not-encrypted-data".encodeToByteArray())
        }
        assertCorruption {
            PushKeyMaterialEntitySerializer(cryptography).read("not-encrypted-data".encodeToByteArray())
        }
    }

    @Test
    fun `serializers reject unsupported schema versions`() = runTest {
        val invalidRegistration = encryptedJson(PushRegistrationEntitySchema(version = 2))
        val invalidKeyMaterial = encryptedJson(PushKeyMaterialEntitySchema(version = 2))

        assertCorruption { PushRegistrationEntitySerializer(cryptography).read(invalidRegistration) }
        assertCorruption { PushKeyMaterialEntitySerializer(cryptography).read(invalidKeyMaterial) }
    }

    @Test
    fun `registration updates are atomic across profiles`() = runTest {
        val dataStore = InMemoryDataStore(PushRegistrationEntitySchema())
        val localDataSource = PushRegistrationLocalDataSource(dataStore)

        (1..20).map { index ->
            async {
                val profileId = "profile-$index"
                localDataSource.save(
                    profileId,
                    PushRegistrationEntity(
                        profileId = profileId,
                        keyIdentifier = "key-$index",
                        timeIssCreated = "2026-07"
                    )
                )
            }
        }.awaitAll()

        assertEquals(20, localDataSource.loadAll().size)
    }

    @Test
    fun `clearing registration preserves completed permission prompt`() = runTest {
        val dataStore = InMemoryDataStore(PushRegistrationEntitySchema())
        val localDataSource = PushRegistrationLocalDataSource(dataStore)
        localDataSource.save(
            "profile-1",
            PushRegistrationEntity(
                profileId = "profile-1",
                keyIdentifier = "key-1",
                timeIssCreated = "2026-07"
            )
        )
        localDataSource.markPermissionPromptCompleted("profile-1")

        localDataSource.clear("profile-1")

        assertEquals(null, localDataSource.load("profile-1"))
        assertTrue(localDataSource.hasCompletedPermissionPrompt("profile-1"))
    }

    @Test
    fun `key material updates and clear are scoped by key identifier`() = runTest {
        val dataStore = InMemoryDataStore(PushKeyMaterialEntitySchema())
        val localDataSource = PushKeyMaterialLocalDataSource(dataStore)

        listOf("key-1", "key-2").map { keyIdentifier ->
            async { localDataSource.save(keyIdentifier, listOf(generation(keyIdentifier, "2026-07"))) }
        }.awaitAll()
        localDataSource.clear("key-1")

        assertEquals(null, localDataSource.load("key-1"))
        assertEquals("key-2", localDataSource.load("key-2")?.keyIdentifier)
    }

    private fun generation(keyIdentifier: String, month: String) = PushKeyGenerationEntity(
        encryptionKey = "encryption-$month",
        secret = "secret-$month",
        month = month,
        keyIdentifier = keyIdentifier
    )

    private suspend inline fun assertCorruption(crossinline block: suspend () -> Unit) {
        try {
            block()
            fail("Expected CorruptionException")
        } catch (_: CorruptionException) {
            assertTrue(true)
        }
    }

    private fun encryptedJson(value: PushRegistrationEntitySchema): ByteArray =
        encryptedJson(SafeJson.value.encodeToString(value))

    private fun encryptedJson(value: PushKeyMaterialEntitySchema): ByteArray =
        encryptedJson(SafeJson.value.encodeToString(value))

    private fun encryptedJson(json: String): ByteArray =
        Base64.getEncoder().encode(cryptography.encrypt(json.encodeToByteArray()))

    private suspend fun PushRegistrationEntitySerializer.write(value: PushRegistrationEntitySchema): ByteArray =
        ByteArrayOutputStream().use { output ->
            writeTo(value, output)
            output.toByteArray()
        }

    private suspend fun PushRegistrationEntitySerializer.read(bytes: ByteArray): PushRegistrationEntitySchema =
        readFrom(ByteArrayInputStream(bytes))

    private suspend fun PushKeyMaterialEntitySerializer.write(value: PushKeyMaterialEntitySchema): ByteArray =
        ByteArrayOutputStream().use { output ->
            writeTo(value, output)
            output.toByteArray()
        }

    private suspend fun PushKeyMaterialEntitySerializer.read(bytes: ByteArray): PushKeyMaterialEntitySchema =
        readFrom(ByteArrayInputStream(bytes))

    private class XorCryptography : DataStoreCryptography {
        override fun encrypt(bytes: ByteArray): ByteArray = bytes.xor()
        override fun decrypt(bytes: ByteArray): ByteArray = bytes.xor()

        private fun ByteArray.xor() = map { byte -> (byte.toInt() xor MASK).toByte() }.toByteArray()

        private companion object {
            const val MASK = 0x5a
        }
    }

    private class InMemoryDataStore<T>(initialValue: T) : DataStore<T> {
        private val state = MutableStateFlow(initialValue)
        private val lock = Mutex()

        override val data: Flow<T> = state

        override suspend fun updateData(transform: suspend (t: T) -> T): T = lock.withLock {
            transform(state.value).also { state.value = it }
        }
    }
}
