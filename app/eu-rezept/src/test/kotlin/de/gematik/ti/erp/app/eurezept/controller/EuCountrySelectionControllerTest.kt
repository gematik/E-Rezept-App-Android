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

package de.gematik.ti.erp.app.eurezept.controller

import android.location.Location
import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetAllEuCountriesUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.LocationBasedCountryDetectionUseCase
import de.gematik.ti.erp.app.eurezept.presentation.EuCountrySelectionController
import de.gematik.ti.erp.app.shared.usecase.GetLocationUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EuCountrySelectionControllerTest {

    private val getAllEuCountriesUseCase: GetAllEuCountriesUseCase = mockk()
    private val getLocationUseCase: GetLocationUseCase = mockk()
    private val locationBasedCountryDetectionUseCase: LocationBasedCountryDetectionUseCase = mockk()

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private val mockCountries = listOf(
        Country("Germany", "DE", "🇩🇪"),
        Country("France", "FR", "🇫🇷"),
        Country("Italy", "IT", "🇮🇹")
    )

    private val mockLocation = mockk<Location> {
        every { latitude } returns 10.1234
        every { longitude } returns 10.3210
    }

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        every { getAllEuCountriesUseCase.filterCountries(any(), any()) } answers {
            val countries = firstArg<List<Country>>()
            val query = secondArg<String>()
            if (query.isEmpty()) {
                countries
            } else {
                countries.filter {
                    it.name.contains(query, ignoreCase = true) || it.code.contains(query, ignoreCase = true)
                }
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearMocks(getAllEuCountriesUseCase, getLocationUseCase, locationBasedCountryDetectionUseCase)
    }

    @Test
    fun `load countries successfully`() {
        coEvery { getAllEuCountriesUseCase.invoke() } returns mockCountries

        testScope.runTest {
            val controller = EuCountrySelectionController(
                getAllEuCountriesUseCase = getAllEuCountriesUseCase,
                getLocationUseCase = getLocationUseCase,
                locationBasedCountryDetectionUseCase = locationBasedCountryDetectionUseCase
            )

            advanceUntilIdle()

            val finalState = controller.uiState.value
            assertTrue(finalState.isDataState)
            assertEquals(3, finalState.data?.size)
            assertEquals("Germany", finalState.data?.get(0)?.name)
        }
    }

    @Test
    fun `show error state on exception`() {
        val testException = RuntimeException("Network error")
        coEvery { getAllEuCountriesUseCase.invoke() } throws testException

        testScope.runTest {
            val controller = EuCountrySelectionController(
                getAllEuCountriesUseCase = getAllEuCountriesUseCase,
                getLocationUseCase = getLocationUseCase,
                locationBasedCountryDetectionUseCase = locationBasedCountryDetectionUseCase
            )

            advanceUntilIdle()

            val finalState = controller.uiState.value
            assertTrue(finalState.isErrorState)
        }
    }

    @Test
    fun `search query filters countries correctly`() {
        coEvery { getAllEuCountriesUseCase.invoke() } returns mockCountries

        testScope.runTest {
            val controller = EuCountrySelectionController(
                getAllEuCountriesUseCase = getAllEuCountriesUseCase,
                getLocationUseCase = getLocationUseCase,
                locationBasedCountryDetectionUseCase = locationBasedCountryDetectionUseCase
            )

            advanceUntilIdle()

            controller.updateSearchQuery("germ")

            val filteredState = controller.uiState.value
            assertTrue(filteredState.isDataState)
            assertEquals(1, filteredState.data?.size)
            assertEquals("Germany", filteredState.data?.get(0)?.name)

            controller.updateSearchQuery("")

            val clearedState = controller.uiState.value
            assertTrue(clearedState.isDataState)
            assertEquals(3, clearedState.data?.size)
        }
    }

    @Test
    fun `location permission granted triggers location detection successfully`() {
        coEvery { getAllEuCountriesUseCase.invoke() } returns mockCountries
        coEvery { getLocationUseCase.invoke() } returns flowOf(
            GetLocationUseCase.LocationResult.Success(mockLocation)
        )
        coEvery {
            locationBasedCountryDetectionUseCase.detectCountryFromLocation(mockLocation, mockCountries)
        } returns flowOf(
            LocationBasedCountryDetectionUseCase.CountryDetectionResult.Success(mockCountries[0])
        )

        testScope.runTest {
            val controller = EuCountrySelectionController(
                getAllEuCountriesUseCase = getAllEuCountriesUseCase,
                getLocationUseCase = getLocationUseCase,
                locationBasedCountryDetectionUseCase = locationBasedCountryDetectionUseCase
            )

            advanceUntilIdle()

            var resultCountry: Country? = null

            controller.onLocationPermissionResult(true) { country ->
                resultCountry = country
            }
            advanceUntilIdle()

            assertEquals(mockCountries[0], resultCountry)
        }
    }
}
