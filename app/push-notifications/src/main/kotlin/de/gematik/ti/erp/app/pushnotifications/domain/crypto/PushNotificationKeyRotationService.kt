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

import androidx.annotation.VisibleForTesting
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationKeyRotationService.Companion.RETAINED_GENERATION_COUNT
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import io.github.aakira.napier.Napier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * Manages the monthly HKDF key-rotation chain for push notification encryption.
 *
 * @param timeIssCreated  "YYYY-MM" — the first month a key is generated for; sent as `time_iss_created` during registration.
 * @param keyIdentifier   UUID identifying this key chain; sent during registration and echoed in every FCM push.
 */
class PushNotificationKeyRotationService(
    private val initialSharedSecret: String,
    private val timeIssCreated: String,
    val keyIdentifier: String,
    private val hkdf: HkdfSha256,
    private val storage: PushNotificationKeyStorage? = null,
    private val currentMonthProvider: () -> String = {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        "${today.year}-${today.monthNumber.toString().padStart(2, '0')}"
    }
) : PushKeyChainAdvancer {
    private val validatedTimeIssCreated: String = requireYearMonth(timeIssCreated)

    // One month behind timeIssCreated so the first addGenerationLocked() call derives
    // the key exactly for timeIssCreated (the HKDF step always produces nextMonth = currentMonth + 1).
    private val seedMonth: String = decrementMonth(validatedTimeIssCreated)

    private val lock = Mutex()
    private val generations = mutableListOf<PushNotificationKeyGeneration>()

    init {
        val saved = storage?.load(keyIdentifier)
            ?.onFailure { Napier.e("Failed to restore push key generations for keyIdentifier=$keyIdentifier; starting fresh", it) }
            ?.getOrNull()
        if (!saved.isNullOrEmpty()) {
            generations.addAll(saved)
            Napier.d { "Restored ${saved.size} push key generation(s) for keyIdentifier=$keyIdentifier from storage" }
        }
    }

    @VisibleForTesting
    internal suspend fun addGeneration() = lock.withLock {
        addGenerationLocked()
    }

    private fun addGenerationLocked() {
        val latest = generations.firstOrNull()
        addGenerationLocked(
            inputKeyMaterial = (latest?.secret ?: initialSharedSecret).hexToByteArray(),
            currentMonth = latest?.month ?: seedMonth
        )
    }

    @Requirement(
        "A_27170-01#2",
        "A_27176",
        sourceSpecification = "gemF_PushNotification",
        rationale = "Derives the monthly secret and AES/GCM key via HKDF; the first generation is the initial derivation for time_iss_created.",
        codeLines = 18
    )
    private fun addGenerationLocked(inputKeyMaterial: ByteArray, currentMonth: String) {
        val nextMonth = incrementMonth(requireYearMonth(currentMonth))
        val derived = hkdf.derive(
            ikm = inputKeyMaterial,
            info = nextMonth.toByteArray(Charsets.UTF_8),
            length = HKDF_OUTPUT_LENGTH
        )

        generations.add(
            0,
            PushNotificationKeyGeneration(
                encryptionKey = derived.copyOfRange(HKDF_KEY_OFFSET, HKDF_OUTPUT_LENGTH).toHexString(),
                secret = derived.copyOfRange(0, HKDF_SECRET_LENGTH).toHexString(),
                month = nextMonth,
                keyIdentifier = keyIdentifier
            )
        )
    }

    @Requirement(
        "A_27179#1",
        "A_27180#1",
        sourceSpecification = "gemF_PushNotification",
        rationale = "Derives key material forward to the requested month on push receipt and invokes old-key cleanup after derivation.",
        codeLines = 35
    )
    override suspend fun advanceToMonth(targetMonth: String) = lock.withLock {
        requireYearMonth(targetMonth)

        val currentMonth = requireYearMonth(currentMonthProvider())
        var maxAllowed = currentMonth
        repeat(MAX_FUTURE_MONTHS_ALLOWED) { maxAllowed = incrementMonth(maxAllowed) }
        if (targetMonth > maxAllowed) {
            throw PushNotificationCryptoError.FutureMonthRejected(targetMonth, maxAllowed)
        }

        val generatedInitialKey = generations.isEmpty()
        if (generatedInitialKey) addGenerationLocked()

        if (generations.first().month == targetMonth) {
            if (generatedInitialKey) {
                storage?.save(keyIdentifier, generations.toList())?.getOrThrow()
            }
            return@withLock
        }

        if (targetMonth <= generations.first().month) {
            throw PushNotificationCryptoError.StaleMonthRejected(
                month = targetMonth,
                latestMonth = generations.first().month
            )
        }

        while (generations.first().month != targetMonth) {
            addGenerationLocked()
        }

        removeOldGenerations()
        storage?.save(keyIdentifier, generations.toList())?.getOrThrow()
    }

    override suspend fun getLatestGeneration(): PushNotificationKeyGeneration? = lock.withLock {
        generations.firstOrNull()
    }

    /** Returns the generation for [month] ("YYYY-MM") if still within the [RETAINED_GENERATION_COUNT]-month retention window. */
    suspend fun getGenerationForMonth(month: String): PushNotificationKeyGeneration? = lock.withLock {
        val validMonth = requireYearMonth(month)
        generations.firstOrNull { it.month == validMonth }
    }

    suspend fun getGenerations(): List<PushNotificationKeyGeneration> = lock.withLock {
        generations.toList()
    }

    @Requirement(
        "A_27180#2",
        sourceSpecification = "gemF_PushNotification",
        rationale = "Implements the two-month retention rule by removing generations older than the calculated cutoff.",
        codeLines = 9
    )
    private fun removeOldGenerations() {
        if (generations.size <= 1) return

        val latestMonth = generations.first().month
        var cutoff = latestMonth
        repeat(RETAINED_GENERATION_COUNT) { cutoff = decrementMonth(cutoff) }

        generations.retainAll { gen -> gen.month > cutoff }
    }

    companion object {
        const val RETAINED_GENERATION_COUNT = 2
        const val MAX_FUTURE_MONTHS_ALLOWED = 2
        private const val HKDF_OUTPUT_LENGTH = 64 // total HKDF output bytes (spec A_27170-01)
        private const val HKDF_SECRET_LENGTH = 32 // first 32 bytes → next IKM
        private const val HKDF_KEY_OFFSET = 32 // last 32 bytes → AES-256 encryption key
        internal fun incrementMonth(month: String) = shiftMonth(month, 1)
        internal fun decrementMonth(month: String) = shiftMonth(month, -1)

        internal fun requireYearMonth(month: String): String {
            require(YYYY_MM_REGEX.matches(month)) { "Expected month in yyyy-MM format, but was '$month'" }
            return month
        }

        private fun shiftMonth(month: String, delta: Int): String {
            val shifted = LocalDate.parse("${requireYearMonth(month)}-01").plus(DatePeriod(months = delta))
            return "${shifted.year}-${shifted.monthNumber.toString().padStart(2, '0')}"
        }

        private val YYYY_MM_REGEX = Regex("""\d{4}-(0[1-9]|1[0-2])""")
    }
}
