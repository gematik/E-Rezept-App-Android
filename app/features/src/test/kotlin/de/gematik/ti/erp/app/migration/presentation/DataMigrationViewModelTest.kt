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

package de.gematik.ti.erp.app.migration.presentation

import de.gematik.ti.erp.app.database.migration.DataMigrator
import de.gematik.ti.erp.app.database.migration.MigrationProgress
import de.gematik.ti.erp.app.database.migration.MigrationStep
import de.gematik.ti.erp.app.migration.usecase.ClearMigratedPharmacyAndShippingInfoUseCase
import de.gematik.ti.erp.app.migration.usecase.CompleteMigrationUseCase
import de.gematik.ti.erp.app.migration.usecase.StartMigrationUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DataMigrationViewModelTest {

    private val progress = MutableStateFlow(MigrationProgress())
    private val dataMigrator: DataMigrator = mockk(relaxed = true)
    private val startMigrationUseCase: StartMigrationUseCase = mockk(relaxed = true)
    private val completeMigrationUseCase: CompleteMigrationUseCase = mockk(relaxed = true)
    private val clearMigratedPharmacyAndShippingInfoUseCase: ClearMigratedPharmacyAndShippingInfoUseCase = mockk(relaxed = true)
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { dataMigrator.progress } returns progress
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful migration triggers completion use case`() = runTest(dispatcher) {
        val viewModel = DataMigrationViewModel(
            dataMigrator = dataMigrator,
            startMigrationUseCase = startMigrationUseCase,
            completeMigrationUseCase = completeMigrationUseCase,
            clearMigratedPharmacyAndShippingInfoUseCase = clearMigratedPharmacyAndShippingInfoUseCase
        )
        val collectionJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        coVerify(exactly = 1) { startMigrationUseCase.invoke() }
        coVerify(exactly = 0) { completeMigrationUseCase.invoke() }

        progress.value = MigrationProgress(isFinished = true)

        advanceUntilIdle()
        coVerify(exactly = 1) { completeMigrationUseCase.invoke() }
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(true, viewModel.uiState.value.data?.isFinished)

        collectionJob.cancel()
    }

    @Test
    fun `recoverable shipping info failure can be completed manually`() = runTest(dispatcher) {
        val error = IllegalStateException("recoverable")
        val viewModel = DataMigrationViewModel(
            dataMigrator = dataMigrator,
            startMigrationUseCase = startMigrationUseCase,
            completeMigrationUseCase = completeMigrationUseCase,
            clearMigratedPharmacyAndShippingInfoUseCase = clearMigratedPharmacyAndShippingInfoUseCase
        )
        val collectionJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        progress.value = MigrationProgress(error = error to MigrationStep.SHIPPING_INFO)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isDataState)
        assertEquals(MigrationStep.SHIPPING_INFO, viewModel.uiState.value.data?.error?.second)

        viewModel.recoverMigrationFailure()

        advanceUntilIdle()
        coVerify(exactly = 1) { clearMigratedPharmacyAndShippingInfoUseCase.invoke() }
        coVerify(exactly = 1) { completeMigrationUseCase.invoke() }

        collectionJob.cancel()
    }

    @Test
    fun `failed migration recovery does not crash or complete migration`() = runTest(dispatcher) {
        val error = IllegalStateException("recoverable")
        val recoveryError = IllegalStateException("database unavailable")
        coEvery { clearMigratedPharmacyAndShippingInfoUseCase.invoke() } throws recoveryError
        val viewModel = DataMigrationViewModel(
            dataMigrator = dataMigrator,
            startMigrationUseCase = startMigrationUseCase,
            completeMigrationUseCase = completeMigrationUseCase,
            clearMigratedPharmacyAndShippingInfoUseCase = clearMigratedPharmacyAndShippingInfoUseCase
        )
        val collectionJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        progress.value = MigrationProgress(error = error to MigrationStep.SHIPPING_INFO)
        advanceUntilIdle()

        viewModel.recoverMigrationFailure()

        advanceUntilIdle()
        coVerify(exactly = 1) { clearMigratedPharmacyAndShippingInfoUseCase.invoke() }
        coVerify(exactly = 0) { completeMigrationUseCase.invoke() }

        collectionJob.cancel()
    }

    @Test
    fun `failed finished migration does not auto-complete`() = runTest(dispatcher) {
        val error = IllegalStateException("fatal")
        val viewModel = DataMigrationViewModel(
            dataMigrator = dataMigrator,
            startMigrationUseCase = startMigrationUseCase,
            completeMigrationUseCase = completeMigrationUseCase,
            clearMigratedPharmacyAndShippingInfoUseCase = clearMigratedPharmacyAndShippingInfoUseCase
        )
        val collectionJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        progress.value = MigrationProgress(isFinished = true, error = error to MigrationStep.PROFILE)

        advanceUntilIdle()
        coVerify(exactly = 0) { completeMigrationUseCase.invoke() }

        collectionJob.cancel()
    }
}
