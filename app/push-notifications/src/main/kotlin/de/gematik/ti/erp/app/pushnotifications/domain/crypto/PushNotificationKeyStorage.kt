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

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

interface PushNotificationKeyStorage {
    fun save(keyIdentifier: String, generations: List<PushNotificationKeyGeneration>): Result<Unit>
    fun load(keyIdentifier: String): Result<List<PushNotificationKeyGeneration>>
    fun clear(keyIdentifier: String): Result<Unit>
}

@Serializable
private data class StoredPushKeyGenerations(
    val version: Int,
    val generations: List<PushNotificationKeyGeneration>
)

class EncryptedSharedPreferencesKeyStorage(
    context: Context,
    masterKeyAlias: String = MASTER_KEY_ALIAS
) : PushNotificationKeyStorage {

    private val prefs: SharedPreferences? by lazy {
        runCatching {
            EncryptedSharedPreferences.create(
                context.applicationContext,
                PREFS_FILE_NAME,
                MasterKey.Builder(context.applicationContext, masterKeyAlias)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }.getOrElse { e ->
            Napier.e("EncryptedSharedPreferences unavailable — push key storage disabled", e)
            null
        }
    }

    @Requirement(
        "A_27177",
        sourceSpecification = "gemF_PushNotification",
        rationale = "Persists the derived secret and AES/GCM key encrypted for reuse.",
        codeLines = 9
    )
    override fun save(
        keyIdentifier: String,
        generations: List<PushNotificationKeyGeneration>
    ): Result<Unit> = withPrefs("save", keyIdentifier) { safePrefs ->
        val committed = safePrefs.edit()
            .putString(prefsKey(keyIdentifier), encodeStoredGenerations(generations))
            .commit()
        if (!committed) throw PushNotificationCryptoError.KeyStorageCommitFailed("save", keyIdentifier)
    }.onFailure { e -> Napier.e("save: failed for $keyIdentifier", e) }

    override fun load(keyIdentifier: String): Result<List<PushNotificationKeyGeneration>> {
        return withPrefs("load", keyIdentifier) { safePrefs ->
            val raw = safePrefs.getString(prefsKey(keyIdentifier), null)
                ?: return@withPrefs emptyList()
            decodeStoredGenerations(raw)
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { e ->
                val failure = PushNotificationCryptoError.ReEnrollmentRequired(keyIdentifier, e)
                Napier.e("load: stored push key material unreadable for $keyIdentifier; re-enrollment required", failure)
                Result.failure(failure)
            }
        )
    }

    override fun clear(keyIdentifier: String): Result<Unit> = withPrefs("clear", keyIdentifier) { safePrefs ->
        val committed = safePrefs.edit()
            .remove(prefsKey(keyIdentifier))
            .commit()
        if (!committed) throw PushNotificationCryptoError.KeyStorageCommitFailed("clear", keyIdentifier)
    }.onFailure { e -> Napier.e("clear: failed for $keyIdentifier", e) }

    private fun <T> withPrefs(op: String, keyId: String, block: (SharedPreferences) -> T): Result<T> {
        val safePrefs = prefs ?: run {
            Napier.e("$op: prefs unavailable for $keyId")
            return Result.failure(PushNotificationCryptoError.KeyStorageUnavailable(op, keyId))
        }
        return runCatching { block(safePrefs) }
    }

    private fun prefsKey(keyIdentifier: String) = "push_generations_$keyIdentifier"

    companion object {
        private const val PREFS_FILE_NAME = "push_notification_key_storage"
        private const val MASTER_KEY_ALIAS = "PUSH_KEY_STORAGE_MASTER_KEY"
        private const val STORAGE_FORMAT_VERSION = 1
        private val storageJson = Json { ignoreUnknownKeys = true }

        @VisibleForTesting
        internal fun encodeStoredGenerations(generations: List<PushNotificationKeyGeneration>): String =
            storageJson.encodeToString(
                StoredPushKeyGenerations(
                    version = STORAGE_FORMAT_VERSION,
                    generations = generations
                )
            )

        @VisibleForTesting
        internal fun decodeStoredGenerations(raw: String): List<PushNotificationKeyGeneration> {
            val stored = storageJson.decodeFromString<StoredPushKeyGenerations>(raw)
            check(stored.version == STORAGE_FORMAT_VERSION) {
                "Unsupported push key storage version ${stored.version}"
            }
            return stored.generations
        }
    }
}

@VisibleForTesting
class InMemoryKeyStorage : PushNotificationKeyStorage {
    private val store = ConcurrentHashMap<String, List<PushNotificationKeyGeneration>>()

    override fun save(
        keyIdentifier: String,
        generations: List<PushNotificationKeyGeneration>
    ): Result<Unit> {
        store[keyIdentifier] = generations.toList()
        return Result.success(Unit)
    }

    override fun load(keyIdentifier: String): Result<List<PushNotificationKeyGeneration>> =
        Result.success(store[keyIdentifier] ?: emptyList())

    override fun clear(keyIdentifier: String): Result<Unit> {
        store.remove(keyIdentifier)
        return Result.success(Unit)
    }
}
