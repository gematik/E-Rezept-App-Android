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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.pharmacy.model.PharmacyErpModel
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test

class GetPharmaciesUseCaseTest {

    private val repository: PharmacyRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var useCase: GetPharmaciesUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
        coEvery { repository.loadPharmacies() } returns flowOf(
            listOf(
                PharmacyErpModel(
                    Instant.parse("2024-08-06T09:00:00Z"),
                    isFavorite = false,
                    isOftenUsed = true,
                    usageCount = 5,
                    telematikId = "ID003",
                    name = "Pharmacy C Often",
                    address = null,
                    contact = null
                ),
                PharmacyErpModel(
                    Instant.parse("2024-08-01T10:00:00Z"),
                    isFavorite = false,
                    isOftenUsed = false,
                    usageCount = 10,
                    telematikId = "ID001",
                    name = "Pharmacy B Often",
                    address = null,
                    contact = null
                ),
                PharmacyErpModel(
                    Instant.parse("2024-08-02T11:00:00Z"),
                    isFavorite = true,
                    isOftenUsed = false,
                    usageCount = 20,
                    telematikId = "ID002",
                    name = "Pharmacy A Fav",
                    address = null,
                    contact = null
                ),
                PharmacyErpModel(
                    Instant.parse("2024-08-05T12:00:00Z"),
                    isFavorite = true,
                    isOftenUsed = false,
                    usageCount = 15,
                    telematikId = "ID004",
                    name = "Pharmacy D Fav",
                    address = null,
                    contact = null
                )
            )
        )

        useCase = GetPharmaciesUseCase(repository, dispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when no favourites show the often used ones in descending order`() {
        coEvery { repository.loadPharmacies() } returns flowOf(
            listOf(
                PharmacyErpModel(
                    Instant.parse("2024-08-06T09:00:00Z"),
                    isFavorite = false,
                    isOftenUsed = false,
                    usageCount = 5,
                    telematikId = "ID003",
                    name = "Pharmacy C Often",
                    address = null,
                    contact = null
                ),
                PharmacyErpModel(
                    Instant.parse("2024-08-01T10:00:00Z"),
                    isFavorite = false,
                    isOftenUsed = false,
                    usageCount = 10,
                    telematikId = "ID001",
                    name = "Pharmacy B Often",
                    address = null,
                    contact = null
                )
            )
        )

        testScope.runTest {
            advanceUntilIdle()
            val results = useCase.invoke().first().map { it.name }
            assert(results.size == 2)
            assertEquals(
                listOf(
                    "Pharmacy C Often",
                    "Pharmacy B Often"
                ),
                results
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when no often used ones or no favourites, show nothing`() {
        coEvery { repository.loadPharmacies() } returns flowOf(emptyList())
        testScope.runTest {
            advanceUntilIdle()
            val results = useCase.invoke().first().map { it.name }
            assert(results.isEmpty())
            assertEquals(
                emptyList<String>(),
                results
            )
        }
    }
}
