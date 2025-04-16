/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData.OverviewPharmacy
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

class GetOverviewPharmaciesUseCaseTest {

    private val repository: PharmacyRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var useCase: GetOverviewPharmaciesUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
        coEvery { repository.loadOftenUsedPharmacies() } returns flowOf(
            listOf(
                OverviewPharmacy(Instant.parse("2024-08-01T10:00:00Z"), false, 10, "ID001", "Pharmacy B Often", "Address 2"),
                OverviewPharmacy(Instant.parse("2024-08-06T09:00:00Z"), false, 5, "ID003", "Pharmacy C Often", "Address 3")

            )
        )
        coEvery { repository.loadFavoritePharmacies() } returns flowOf(
            listOf(
                OverviewPharmacy(Instant.parse("2024-08-02T11:00:00Z"), true, 20, "ID002", "Pharmacy A Fav", "Address 1"),
                OverviewPharmacy(Instant.parse("2024-08-05T12:00:00Z"), true, 15, "ID004", "Pharmacy D Fav", "Address 4")
            )
        )

        useCase = GetOverviewPharmaciesUseCase(repository, dispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `sort the pharmacies by favorites first and then often used with last used in descending order`() {
        testScope.runTest {
            advanceUntilIdle()
            val results = useCase.invoke().first().map { it.pharmacyName }
            assert(results.size == 4)
            assertEquals(
                listOf(
                    "Pharmacy D Fav",
                    "Pharmacy A Fav",
                    "Pharmacy C Often",
                    "Pharmacy B Often"
                ),
                results
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when no favourites show the often used ones in descending order`() {
        coEvery { repository.loadFavoritePharmacies() } returns flowOf(emptyList())
        testScope.runTest {
            advanceUntilIdle()
            val results = useCase.invoke().first().map { it.pharmacyName }
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
    fun `when no often used one show the favourites in descending order`() {
        coEvery { repository.loadOftenUsedPharmacies() } returns flowOf(emptyList())
        testScope.runTest {
            advanceUntilIdle()
            val results = useCase.invoke().first().map { it.pharmacyName }
            assert(results.size == 2)
            assertEquals(
                listOf(
                    "Pharmacy D Fav",
                    "Pharmacy A Fav"
                ),
                results
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when no often used ones or no favourites, show nothing`() {
        coEvery { repository.loadOftenUsedPharmacies() } returns flowOf(emptyList())
        coEvery { repository.loadFavoritePharmacies() } returns flowOf(emptyList())
        testScope.runTest {
            advanceUntilIdle()
            val results = useCase.invoke().first().map { it.pharmacyName }
            assert(results.isEmpty())
            assertEquals(
                emptyList<String>(),
                results
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when a often used one has the same telematik id as the favourite one, only consider the favourite one`() {
        coEvery { repository.loadOftenUsedPharmacies() } returns flowOf(
            listOf(
                OverviewPharmacy(Instant.parse("2024-08-02T11:00:00Z"), false, 20, "ID002", "Pharmacy A Fav", "Address 1"),
                OverviewPharmacy(Instant.parse("2024-08-06T09:00:00Z"), false, 5, "ID003", "Pharmacy C Often", "Address 3")

            )
        )
        testScope.runTest {
            advanceUntilIdle()
            val results = useCase.invoke().first().map { it.pharmacyName }
            assert(results.size == 3)
            assertEquals(
                listOf(
                    "Pharmacy D Fav",
                    "Pharmacy A Fav",
                    "Pharmacy C Often"
                ),
                results
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when there are more than 5 favourites, only the first 5 favourites are considered`() {
        coEvery { repository.loadFavoritePharmacies() } returns flowOf(
            listOf(
                OverviewPharmacy(Instant.parse("2024-08-02T11:00:00Z"), true, 20, "ID002", "Pharmacy A Fav", "Address 1"),
                OverviewPharmacy(Instant.parse("2024-08-05T12:00:00Z"), true, 15, "ID004", "Pharmacy D Fav", "Address 4"),
                OverviewPharmacy(Instant.parse("2024-08-06T12:00:00Z"), true, 15, "ID0041", "Pharmacy E Fav", "Address 4"),
                OverviewPharmacy(Instant.parse("2024-08-07T12:00:00Z"), true, 15, "ID0042", "Pharmacy F Fav", "Address 4"),
                OverviewPharmacy(Instant.parse("2024-08-08T12:00:00Z"), true, 15, "ID0043", "Pharmacy G Fav", "Address 4"),
                OverviewPharmacy(Instant.parse("2024-08-09T12:00:00Z"), true, 15, "ID0044", "Pharmacy H Fav", "Address 4")
            )
        )
        testScope.runTest {
            advanceUntilIdle()
            val results = useCase.invoke().first().map { it.pharmacyName }
            assert(results.size == 5)
            assertEquals(
                listOf(
                    "Pharmacy H Fav",
                    "Pharmacy G Fav",
                    "Pharmacy F Fav",
                    "Pharmacy E Fav",
                    "Pharmacy D Fav"
                ),
                results
            )
        }
    }
}
