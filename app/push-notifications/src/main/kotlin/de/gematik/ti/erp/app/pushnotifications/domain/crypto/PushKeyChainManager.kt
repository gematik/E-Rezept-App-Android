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

import de.gematik.ti.erp.app.pushnotifications.domain.formatPushNotificationYearMonth
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Owns one key-rotation service per `key_identifier`, allowing profiles to maintain independent chains.
 * The ISS remains in memory; restored chains use persisted derived key material.
 */
class PushKeyChainManager(
    private val hkdf: HkdfSha256,
    private val keyStorage: PushNotificationKeyStorage,
    private val registrationStorage: PushRegistrationStorage,
    private val currentMonthProvider: () -> String = {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        formatPushNotificationYearMonth(now.year, now.monthNumber)
    }
) : PushKeyChainAdvancer {

    private val services = ConcurrentHashMap<String, PushNotificationKeyRotationService>()
    private val restorationLock = Mutex()

    private var restorationComplete = false

    fun isInitialized(keyIdentifier: String): Boolean = services.containsKey(keyIdentifier)

    override suspend fun knownKeyIdentifiers(): Set<String> {
        restoreFromStorage()
        return services.keys.toSet()
    }

    private suspend fun initialize(iss: String, keyIdentifier: String, timeIssCreated: String) {
        restoreFromStorage()
        services[keyIdentifier] = PushNotificationKeyRotationService.create(
            initialSharedSecret = iss,
            timeIssCreated = timeIssCreated,
            keyIdentifier = keyIdentifier,
            hkdf = hkdf,
            storage = keyStorage
        )
        Napier.d { "PushKeyChainManager: initialized chain for keyIdentifier=$keyIdentifier, timeIssCreated=$timeIssCreated" }
    }

    suspend fun initializeAndAdvance(
        iss: String,
        keyIdentifier: String,
        timeIssCreated: String
    ) {
        initialize(
            iss = iss,
            keyIdentifier = keyIdentifier,
            timeIssCreated = timeIssCreated
        )
        advanceToCurrentMonth(keyIdentifier)
    }

    suspend fun advanceToCurrentMonth(keyIdentifier: String) {
        val currentMonth = currentMonthProvider()
        val latest = getLatestGeneration(keyIdentifier)

        if (latest == null || latest.month < currentMonth) {
            Napier.d {
                "Advancing push key chain $keyIdentifier to $currentMonth " +
                    "(was: ${latest?.month ?: "empty"})"
            }
            advanceToMonth(keyIdentifier, currentMonth)
        } else {
            Napier.d { "Push key chain $keyIdentifier already current at $currentMonth — no advance needed." }
        }
    }

    suspend fun advanceAllToCurrentMonth() {
        knownKeyIdentifiers().forEach { keyIdentifier ->
            try {
                advanceToCurrentMonth(keyIdentifier)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e(
                    "PushKeyChainManager: failed to advance chain for keyIdentifier=$keyIdentifier",
                    e
                )
            }
        }
    }

    /**
     * Restores persisted chains once and removes registration records that no longer have key material.
     */
    suspend fun restoreFromStorage() = restorationLock.withLock {
        if (restorationComplete) return@withLock

        val all = registrationStorage.loadAll().getOrThrow()
        if (all.isEmpty()) {
            restorationComplete = true
            Napier.d { "PushKeyChainManager: no stored registrations — no chains restored." }
            return@withLock
        }

        for ((profileId, registration) in all) {
            val storedGenerations = keyStorage.load(registration.keyIdentifier)
                .onFailure {
                    Napier.e(
                        "PushKeyChainManager: stored key generations unreadable for " +
                            "keyIdentifier=${registration.keyIdentifier}",
                        it
                    )
                }
                .getOrNull()
            if (storedGenerations.isNullOrEmpty()) {
                registrationStorage.clear(profileId)
                    .onFailure {
                        Napier.e(
                            "PushKeyChainManager: failed to clear stale registration metadata " +
                                "for profile=$profileId",
                            it
                        )
                    }
                Napier.w(
                    "PushKeyChainManager: registration metadata without key material for " +
                        "profile=$profileId — re-registration required."
                )
                continue
            }
            services[registration.keyIdentifier] = PushNotificationKeyRotationService.restore(
                timeIssCreated = registration.timeIssCreated,
                keyIdentifier = registration.keyIdentifier,
                hkdf = hkdf,
                storage = keyStorage,
                restoredGenerations = storedGenerations
            )
        }
        restorationComplete = true
        Napier.d { "PushKeyChainManager: restored ${services.size} chain(s) from storage." }
    }

    suspend fun clear(keyIdentifier: String): Result<Unit> =
        try {
            keyStorage.clear(keyIdentifier).getOrThrow()
            services.remove(keyIdentifier)
            Napier.d { "PushKeyChainManager: cleared chain for keyIdentifier=$keyIdentifier" }
            Result.success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }

    override suspend fun advanceToMonth(keyIdentifier: String, targetMonth: String) {
        restoreFromStorage()
        val service = services[keyIdentifier]
        if (service == null) {
            Napier.w("PushKeyChainManager.advanceToMonth: no chain for keyIdentifier=$keyIdentifier — skipping advance.")
            return
        }
        service.advanceToMonth(targetMonth)
    }

    override suspend fun getLatestGeneration(keyIdentifier: String): PushNotificationKeyGeneration? {
        restoreFromStorage()
        return services[keyIdentifier]?.getLatestGeneration()
    }

    override suspend fun getGenerationForMonth(
        keyIdentifier: String,
        month: String
    ): PushNotificationKeyGeneration? {
        restoreFromStorage()
        return services[keyIdentifier]?.getGenerationForMonth(month)
    }
}
