/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

@file:Suppress("ktlint:max-line-length", "ktlint:argument-list-wrapping")

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.utils.testRedeemedTaskIdsOrdered
import de.gematik.ti.erp.app.utils.testScannedTasks
import de.gematik.ti.erp.app.utils.testScannedTasksOrdered
import de.gematik.ti.erp.app.utils.testSyncedTasks
import de.gematik.ti.erp.app.utils.testSyncedTasksOrdered
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class PrescriptionUseCaseTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK(relaxed = true)
    lateinit var useCase: PrescriptionUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { useCase.syncedTasks("") } answers { flowOf(testSyncedTasks) }
        every { useCase.scannedTasks("") } answers { flowOf(testScannedTasks) }

        every { useCase.syncedActiveRecipes("", any()) } answers { callOriginal() }
        every { useCase.scannedActiveRecipes("") } answers { callOriginal() }
        every { useCase.redeemedPrescriptions("", any()) } answers { callOriginal() }
    }

    @Test
    fun `syncedRecipes - should return synchronized tasks in form of recipes sorted by authoredOn and grouped by organization`() =
        runTest {
            assertEquals(
                testSyncedTasksOrdered.map { it.taskId },
                useCase.syncedActiveRecipes(
                    profileId = "",
                    now = LocalDate.parse("2021-02-01").atStartOfDay().toInstant(ZoneOffset.UTC)
                ).first().map { it.taskId }
            )
        }

    @Test
    fun `scannedRecipes - should return scanned tasks in form of recipes sorted by scanSessionEnd`() =
        runTest {
            assertEquals(
                testScannedTasksOrdered.map { it.taskId },
                useCase.scannedActiveRecipes("").first().map { it.taskId }
            )
        }

    @Test
    fun `redeemed recipes - should return redeemed tasks ordered by redeemedOn`() =
        runTest {
            assertEquals(
                testRedeemedTaskIdsOrdered,
                useCase.redeemedPrescriptions(
                    profileId = "",
                    now = LocalDate.parse("2021-02-01").atStartOfDay().toInstant(ZoneOffset.UTC)
                ).first().map { it.taskId }
            )
        }
}
