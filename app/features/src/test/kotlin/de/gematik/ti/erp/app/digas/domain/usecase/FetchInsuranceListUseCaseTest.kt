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

package de.gematik.ti.erp.app.digas.domain.usecase

import app.cash.turbine.test
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FetchInsuranceListUseCaseTest {

    private val repository: PharmacyRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fetchInsuranceListUseCase: FetchInsuranceListUseCase

    private val mockPharmacyErpModels = listOf(
        FhirPharmacyErpModel(
            id = "1",
            name = "AOK Nordost - Die Gesundheitskasse",
            telematikId = "107519005",
            position = null,
            address = null,
            contact = mockk(relaxed = true),
            specialities = emptyList(),
            hoursOfOperation = null,
            availableTime = mockk(relaxed = true)
        ),
        FhirPharmacyErpModel(
            id = "2",
            name = "BARMER",
            telematikId = "104204002",
            position = null,
            address = null,
            contact = mockk(relaxed = true),
            specialities = emptyList(),
            hoursOfOperation = null,
            availableTime = mockk(relaxed = true)
        )
    )

    private val mockCollection = FhirPharmacyErpModelCollection(
        type = PharmacyVzdService.FHIRVZD,
        total = 2,
        id = null,
        entries = mockPharmacyErpModels
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        fetchInsuranceListUseCase = FetchInsuranceListUseCase(
            repository = repository,
            dispatchers = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should return list of insurances when repository returns success`() = testScope.runTest {
        // Given
        coEvery { repository.searchInsurances(any()) } returns Result.success(mockCollection)

        // When
        fetchInsuranceListUseCase().test {
            // Then
            val result = awaitItem()
            assertEquals(2, result.data?.size)
            result.data?.get(0)?.let { assertEquals("AOK Nordost - Die Gesundheitskasse", it.name) }
            result.data?.get(1)?.let { assertEquals("BARMER", it.name) }
            awaitComplete()
        }
    }

    @Test
    fun `should sort results by name`() = testScope.runTest {
        // Given
        val unsortedCollection = FhirPharmacyErpModelCollection(
            type = PharmacyVzdService.FHIRVZD,
            total = 2,
            id = null,
            entries = listOf(
                FhirPharmacyErpModel(
                    id = "2",
                    name = "BARMER",
                    telematikId = "104204002",
                    position = null,
                    address = null,
                    contact = mockk(relaxed = true),
                    specialities = emptyList(),
                    hoursOfOperation = null,
                    availableTime = mockk(relaxed = true)
                ),
                FhirPharmacyErpModel(
                    id = "1",
                    name = "AOK Nordost - Die Gesundheitskasse",
                    telematikId = "107519005",
                    position = null,
                    address = null,
                    contact = mockk(relaxed = true),
                    specialities = emptyList(),
                    hoursOfOperation = null,
                    availableTime = mockk(relaxed = true)
                )
            )
        )

        coEvery { repository.searchInsurances(any()) } returns Result.success(unsortedCollection)

        // When
        fetchInsuranceListUseCase().test {
            // Then
            val result = awaitItem()
            assertEquals(2, result.data?.size)
            result.data?.get(0)?.let { assertEquals("AOK Nordost - Die Gesundheitskasse", it.name) }
            result.data?.get(1)?.let { assertEquals("BARMER", it.name) }
            awaitComplete()
        }
    }
}
