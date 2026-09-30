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

package de.gematik.ti.erp.app.pushnotifications.domain.usecase

import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InitializeDebugPushKeyChainUseCaseTest {
    private val keyChainManager = mockk<PushKeyChainManager>()

    init {
        coEvery { keyChainManager.restoreFromStorage() } returns Unit
    }

    @Test
    fun `non internal debug build never initializes debug key material`() = runTest {
        val initialized = useCase(isInternalDebugBuild = false)()

        assertFalse(initialized)
        coVerify(exactly = 0) { keyChainManager.initializeAndAdvance(any(), any(), any()) }
        coVerify(exactly = 0) { keyChainManager.advanceToCurrentMonth(any()) }
    }

    @Test
    fun `internal debug build initializes and advances through manager`() = runTest {
        every { keyChainManager.isInitialized("debug-key-id") } returns false
        coEvery { keyChainManager.initializeAndAdvance(any(), any(), any()) } returns Unit

        val initialized = useCase(isInternalDebugBuild = true)(requiredKeyIdentifier = "debug-key-id")

        assertTrue(initialized)
        coVerify(exactly = 1) {
            keyChainManager.initializeAndAdvance(
                iss = "debug-iss",
                keyIdentifier = "debug-key-id",
                timeIssCreated = "2026-07"
            )
        }
    }

    @Test
    fun `existing matching debug chain only advances to current month`() = runTest {
        every { keyChainManager.isInitialized("debug-key-id") } returns true
        coEvery { keyChainManager.advanceToCurrentMonth("debug-key-id") } returns Unit

        val initialized = useCase(isInternalDebugBuild = true)(requiredKeyIdentifier = "debug-key-id")

        assertTrue(initialized)
        coVerify(exactly = 0) { keyChainManager.initializeAndAdvance(any(), any(), any()) }
        coVerify(exactly = 1) { keyChainManager.advanceToCurrentMonth("debug-key-id") }
    }

    @Test
    fun `incoming non debug key identifier cannot initialize debug chain`() = runTest {
        val initialized = useCase(isInternalDebugBuild = true)(requiredKeyIdentifier = "other-key-id")

        assertFalse(initialized)
        coVerify(exactly = 0) { keyChainManager.initializeAndAdvance(any(), any(), any()) }
    }

    private fun useCase(isInternalDebugBuild: Boolean) = InitializeDebugPushKeyChainUseCase(
        keyChainManager = keyChainManager,
        isInternalDebugBuild = { isInternalDebugBuild },
        initialSharedSecret = "debug-iss",
        timeIssCreated = "2026-07",
        keyIdentifier = "debug-key-id"
    )
}
