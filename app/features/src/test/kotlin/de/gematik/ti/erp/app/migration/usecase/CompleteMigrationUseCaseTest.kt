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

package de.gematik.ti.erp.app.migration.usecase

import de.gematik.ti.erp.app.database.api.SettingsLocalDataSource
import de.gematik.ti.erp.app.database.datastore.featuretoggle.EU_REDEEM
import de.gematik.ti.erp.app.database.datastore.featuretoggle.ROOM_DB
import de.gematik.ti.erp.app.datastore.featuretoggle.FeatureToggleRepository
import de.gematik.ti.erp.app.logger.SessionLogHolder
import de.gematik.ti.erp.app.logger.model.LogEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CompleteMigrationUseCaseTest {

    private val settingsLocalDataSource: SettingsLocalDataSource = mockk(relaxed = true)
    private val featureToggleRepository: FeatureToggleRepository = mockk(relaxed = true)
    private val sessionLogHolder: SessionLogHolder = mockk(relaxed = true)
    private val dispatcher = StandardTestDispatcher()

    private lateinit var useCase: CompleteMigrationUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        useCase = CompleteMigrationUseCase(
            settingsLocalDataSource = settingsLocalDataSource,
            featureToggleRepository = featureToggleRepository,
            sessionLogHolder = sessionLogHolder,
            dispatcher = dispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke enables room db when migration completes and feature is disabled`() = runTest(dispatcher) {
        val logEntry = slot<LogEntry>()
        every {
            featureToggleRepository.getFeatures()
        } returns flowOf(setOf(ROOM_DB.copy(isActive = false), EU_REDEEM))
        coEvery { featureToggleRepository.toggleFeature(ROOM_DB) } returns Unit

        useCase()

        coVerify(exactly = 1) { featureToggleRepository.toggleFeature(ROOM_DB) }
        coVerify(exactly = 1) { settingsLocalDataSource.markDataAsPortedToRoom() }
        coVerify(exactly = 1) { sessionLogHolder.addLog(capture(logEntry)) }
        assertEquals(
            "db_backend_selected=ROOM migration_completed=true is_porting_done=false",
            logEntry.captured.response.content.text
        )
    }

    @Test
    fun `invoke does not toggle room db when feature is already enabled`() = runTest(dispatcher) {
        val logEntry = slot<LogEntry>()
        every {
            featureToggleRepository.getFeatures()
        } returns flowOf(setOf(ROOM_DB, EU_REDEEM))

        useCase()

        coVerify(exactly = 0) { featureToggleRepository.toggleFeature(any()) }
        coVerify(exactly = 1) { settingsLocalDataSource.markDataAsPortedToRoom() }
        coVerify(exactly = 1) { sessionLogHolder.addLog(capture(logEntry)) }
        assertEquals(
            "db_backend_selected=ROOM migration_completed=true is_porting_done=false",
            logEntry.captured.response.content.text
        )
    }
}
