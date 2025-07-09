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

package de.gematik.ti.erp.app.prescription.detail.presentation

import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SCANNED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isEmptyState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isLoadingState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.rules.TestWatcher

class GetPrescriptionByTaskDataIdControllerTest : TestWatcher() {

    private val prescriptionRepository: PrescriptionRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var controllerUnderTest: GetPrescriptionByTaskIdController
    private lateinit var getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)

        getPrescriptionByTaskIdUseCase = GetPrescriptionByTaskIdUseCase(
            repository = prescriptionRepository,
            dispatcher = dispatcher
        )

        controllerUnderTest = GetPrescriptionByTaskIdController(
            taskId = "taskId",
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `prescription is empty and screen in error state`() {
        every { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        every { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()

        testScope.runTest {
            advanceUntilIdle()
            val prescription = controllerUnderTest.prescription.first()
            assert(prescription.isErrorState)
        }
    }

    @Test
    fun `prescription is loading and screen in loading state`() {
        every { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        every { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()

        testScope.runTest {
            val prescription = controllerUnderTest.prescription.first()
            assert(prescription.isLoadingState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `prescription is empty and screen in  state`() {
        every { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf()
        every { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf()

        testScope.runTest {
            advanceUntilIdle()
            val prescription = controllerUnderTest.prescription.first()
            assert(prescription.isEmptyState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a synced prescription is loaded and screen is in data state with a synced prescription`() {
        every { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SYNCED_TASK)
        every { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf()

        testScope.runTest {
            advanceUntilIdle()
            val prescription = controllerUnderTest.prescription.first()
            assert(prescription.isDataState)
            assert(prescription.data is PrescriptionData.Synced)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a scanned prescription is loaded and screen is in data state with a scanned prescription`() {
        every { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf()
        every { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)

        testScope.runTest {
            advanceUntilIdle()
            val prescription = controllerUnderTest.prescription.first()
            assert(prescription.isDataState)
            assert(prescription.data is PrescriptionData.Scanned)
        }
    }
}
