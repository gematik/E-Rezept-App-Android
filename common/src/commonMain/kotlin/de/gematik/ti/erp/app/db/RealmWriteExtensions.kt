/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.db

import io.github.aakira.napier.Napier
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.exceptions.RealmException
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import java.io.IOException

/**
 * Performs a **single-attempt Realm write operation** with structured error handling.
 *
 * This method:
 * - Executes the provided [block] within a Realm write transaction.
 * - Logs any errors that occur during the write.
 * - Does **not** retry on failure.
 *
 * **When to use:**
 * Use this method when you need a straightforward Realm write with error handling,
 * but without retry attempts. Ideal for operations where immediate failure is acceptable.
 *
 * @param block The write operation to perform inside the Realm transaction.
 * @return The result of the [block] execution.
 * @throws Throwable If the write operation fails.
 */
suspend fun <R> Realm.safeWrite(block: MutableRealm.() -> R): R {
    return safeWriteWithRetry(maxRetries = 1, block = block)
}

/**
 * Performs a **Realm write operation with retry support** using exponential backoff.
 *
 * This method:
 * - Attempts the provided [block] within a Realm write transaction.
 * - Retries on failure up to [maxRetries] times with exponential backoff.
 * - Logs the success, failure, and retry attempts using [Napier].
 * - Handles specific exceptions like [IllegalStateException], [RealmException], and [IOException].
 *
 * **When to use:**
 * Use this method when your write operations are critical and need retry support in case of failures.
 * Ideal for operations like syncing or persisting critical data.
 *
 * @param maxRetries The maximum number of retry attempts (default is 1).
 * @param initialDelay The initial delay in milliseconds before retrying (default is 100ms).
 * @param block The write operation to perform inside the Realm transaction.
 * @return The result of the [block] execution.
 * @throws Throwable If all retry attempts are exhausted or an unexpected error occurs.
 */
suspend fun <R> Realm.safeWriteWithRetry(
    maxRetries: Int = 1,
    initialDelay: Long = 100, // in ms
    block: MutableRealm.() -> R
): R {
    var attempt = 0
    var delayTime = initialDelay
    val startTime = Clock.System.now()

    while (attempt < maxRetries) {
        attempt++
        try {
            return write {
                block()
            }.also {
                Napier.i { "Realm write successful on attempt $attempt after ${Clock.System.now() - startTime}." }
            }
        } catch (throwable: Throwable) {
            Napier.e(throwable) { "Realm write failed on attempt $attempt after ${Clock.System.now() - startTime}." }

            when (throwable) {
                is IllegalStateException -> Napier.e(throwable) { "Illegal state during Realm write." }
                is RealmException -> Napier.e(throwable) { "Realm-specific error during write." }
                is IOException -> Napier.e(throwable) { "I/O error during Realm write." }
                else -> Napier.e(throwable) { "Unexpected error during Realm write." }
            }

            if (attempt == maxRetries) {
                Napier.e { "All $maxRetries retries exhausted for Realm write." }
                throw throwable
            }

            Napier.w { "Retrying Realm write in ${delayTime}ms (attempt $attempt of $maxRetries)." }
            delay(delayTime)
            delayTime *= 2 // Exponential backoff
        }
    }

    throw IllegalStateException("Realm safe-write error: This line should never be reached.")
}
