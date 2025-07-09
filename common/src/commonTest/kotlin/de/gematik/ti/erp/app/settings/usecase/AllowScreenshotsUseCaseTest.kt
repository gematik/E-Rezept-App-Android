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

package de.gematik.ti.erp.app.settings.usecase

import de.gematik.ti.erp.app.settings.repository.DefaultSettingsRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AllowScreenshotsUseCaseTest {

    private lateinit var allowScreenshotsUseCase: AllowScreenshotsUseCase

    @MockK(relaxed = true)
    private lateinit var settingsRepository: DefaultSettingsRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        allowScreenshotsUseCase = AllowScreenshotsUseCase(settingsRepository)
    }

    @Test
    fun `allow analytic`() = runTest {
        allowScreenshotsUseCase(true)
        coVerify(exactly = 1) {
            settingsRepository.saveAllowScreenshots(true)
        }
        coVerify(exactly = 0) {
            settingsRepository.saveAllowScreenshots(false)
        }
    }

    @Test
    fun `disallow analytic`() = runTest {
        allowScreenshotsUseCase(false)
        coVerify(exactly = 1) {
            settingsRepository.saveAllowScreenshots(false)
        }
        coVerify(exactly = 0) {
            settingsRepository.saveAllowScreenshots(true)
        }
    }
}
