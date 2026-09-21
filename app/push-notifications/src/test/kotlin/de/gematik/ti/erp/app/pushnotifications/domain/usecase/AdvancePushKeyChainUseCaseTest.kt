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
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdvancePushKeyChainUseCaseTest {

    private class FakePushKeyChainAdvancer(
        private var latestGeneration: PushNotificationKeyGeneration? = null
    ) : PushKeyChainAdvancer {
        var advanceCalledWith: String? = null
        var advanceCallCount: Int = 0

        override suspend fun getLatestGeneration(): PushNotificationKeyGeneration? = latestGeneration

        override suspend fun advanceToMonth(targetMonth: String) {
            advanceCallCount++
            advanceCalledWith = targetMonth
            latestGeneration = PushNotificationKeyGeneration(
                encryptionKey = "fake-key",
                secret = "fake-secret",
                month = targetMonth,
                keyIdentifier = "fake-id"
            )
        }
    }

    private fun buildUseCase(
        fake: FakePushKeyChainAdvancer,
        currentMonth: String = "2026-05"
    ) = AdvancePushKeyChainUseCase(
        keyChainAdvancer = fake,
        currentMonthProvider = { currentMonth }
    )

    @Test
    fun `when chain is empty, advances to current month`() = runTest {
        val fake = FakePushKeyChainAdvancer(latestGeneration = null)
        buildUseCase(fake, currentMonth = "2026-05").invoke()

        assertEquals(1, fake.advanceCallCount)
        assertEquals("2026-05", fake.advanceCalledWith)
    }

    @Test
    fun `when latest month is behind current month, advances`() = runTest {
        val fake = FakePushKeyChainAdvancer(
            latestGeneration = generation("2026-03")
        )
        buildUseCase(fake, currentMonth = "2026-05").invoke()

        assertEquals(1, fake.advanceCallCount)
        assertEquals("2026-05", fake.advanceCalledWith)
    }

    @Test
    fun `when latest month equals current month, is a no-op`() = runTest {
        val fake = FakePushKeyChainAdvancer(
            latestGeneration = generation("2026-05")
        )
        buildUseCase(fake, currentMonth = "2026-05").invoke()

        assertEquals(0, fake.advanceCallCount)
        assertNull(fake.advanceCalledWith)
    }

    @Test
    fun `when latest month is ahead of current month, is a no-op`() = runTest {
        val fake = FakePushKeyChainAdvancer(
            latestGeneration = generation("2026-07")
        )
        buildUseCase(fake, currentMonth = "2026-05").invoke()

        assertEquals(0, fake.advanceCallCount)
        assertNull(fake.advanceCalledWith)
    }

    @Test
    fun `correct month string format YYYY-MM is passed to advanceToMonth`() = runTest {
        val fake = FakePushKeyChainAdvancer(latestGeneration = null)
        buildUseCase(fake, currentMonth = "2026-01").invoke()

        assertEquals("2026-01", fake.advanceCalledWith)
    }

    @Test
    fun `advance is called exactly once even if month is many months behind`() = runTest {
        val fake = FakePushKeyChainAdvancer(
            latestGeneration = generation("2024-01")
        )
        buildUseCase(fake, currentMonth = "2026-05").invoke()

        assertEquals(1, fake.advanceCallCount)
        assertEquals("2026-05", fake.advanceCalledWith)
    }

    private fun generation(month: String) = PushNotificationKeyGeneration(
        encryptionKey = "fake-key",
        secret = "fake-secret",
        month = month,
        keyIdentifier = "fake-id"
    )
}
