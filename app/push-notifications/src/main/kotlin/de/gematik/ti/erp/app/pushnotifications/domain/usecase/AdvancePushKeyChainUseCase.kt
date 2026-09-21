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

package de.gematik.ti.erp.app.pushnotifications.domain.usecase

import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainAdvancer
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Advances HKDF push-notification key chain to the current calendar month.
 * No-op if the chain is already current.
 */
class AdvancePushKeyChainUseCase(
    private val keyChainAdvancer: PushKeyChainAdvancer,
    private val currentMonthProvider: () -> String = {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        "${now.year}-${now.monthNumber.toString().padStart(2, '0')}"
    }
) {
    suspend operator fun invoke() {
        val currentMonth = currentMonthProvider()
        val latest = keyChainAdvancer.getLatestGeneration()

        if (latest == null || latest.month < currentMonth) {
            Napier.d { "Advancing push key chain to $currentMonth (was: ${latest?.month ?: "empty"})" }
            keyChainAdvancer.advanceToMonth(currentMonth)
        } else {
            Napier.d { "Push key chain already current at $currentMonth — no advance needed." }
        }
    }
}
