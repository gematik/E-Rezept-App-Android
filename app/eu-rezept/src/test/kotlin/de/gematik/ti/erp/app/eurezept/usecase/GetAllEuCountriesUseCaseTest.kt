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

package de.gematik.ti.erp.app.eurezept.usecase

import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetAllEuCountriesUseCase
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.fhir.FhirCountryErpModel
import de.gematik.ti.erp.app.fhir.FhirCountryErpModelCollection
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
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
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GetAllEuCountriesUseCaseTest {

    private val repository: EuRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private lateinit var originalLocale: Locale

    private lateinit var useCaseUnderTest: GetAllEuCountriesUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)
        useCaseUnderTest = GetAllEuCountriesUseCase(repository, dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        Locale.setDefault(originalLocale)
        clearMocks(repository)
    }

    @Test
    fun `invoke returns correctly mapped countries on success`() {
        testScope.runTest {
            val mockFhirCountries = listOf(
                createMockFhirCountryModel("Germany", "DE"),
                createMockFhirCountryModel("France", "FR"),
                createMockFhirCountryModel("Italy", "IT")
            )
            val mockFhirCollection = FhirCountryErpModelCollection(mockFhirCountries)
            coEvery { repository.fetchAvailableCountries() } returns Result.success(mockFhirCollection)

            val result = useCaseUnderTest.invoke()

            assertEquals(3, result.size)

            assertEquals("Germany", result[0].name)
            assertEquals("DE", result[0].code)
            assertEquals("🇩🇪", result[0].flagEmoji)

            assertEquals("France", result[1].name)
            assertEquals("FR", result[1].code)
            assertEquals("🇫🇷", result[1].flagEmoji)

            assertEquals("Italy", result[2].name)
            assertEquals("IT", result[2].code)
            assertEquals("🇮🇹", result[2].flagEmoji)
        }
    }

    @Test
    fun `invoke throws exception on repository failure`() {
        testScope.runTest {
            val testException = RuntimeException("Network error")
            coEvery { repository.fetchAvailableCountries() } returns Result.failure(testException)

            assertFailsWith<RuntimeException> {
                useCaseUnderTest.invoke()
            }
        }
    }

    @Test
    fun `filterCountries returns filtered countries by name and is case insensitive`() {
        val countries = createMockCountriesList()

        val resultUpperCase = useCaseUnderTest.filterCountries(countries, "ITALY")
        val resultLowerCase = useCaseUnderTest.filterCountries(countries, "italy")
        val resultMixedCase = useCaseUnderTest.filterCountries(countries, "ItAlY")

        assertEquals(1, resultUpperCase.size)
        assertEquals(1, resultLowerCase.size)
        assertEquals(1, resultMixedCase.size)
        assertEquals("Italy", resultUpperCase[0].name)
        assertEquals("Italy", resultLowerCase[0].name)
        assertEquals("Italy", resultMixedCase[0].name)
    }

    @Test
    fun `filterCountries returns empty list when no matches found`() {
        val countries = createMockCountriesList()

        val result = useCaseUnderTest.filterCountries(countries, "xyz")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns German localized names when app locale is German`() {
        Locale.setDefault(Locale.GERMAN)
        testScope.runTest {
            coEvery { repository.fetchAvailableCountries() } returns Result.success(
                FhirCountryErpModelCollection(
                    listOf(
                        createMockFhirCountryModel("Germany", "DE"),
                        createMockFhirCountryModel("France", "FR")
                    )
                )
            )

            val result = useCaseUnderTest.invoke()

            assertEquals("Deutschland", result[0].name)
            assertEquals("Frankreich", result[1].name)
        }
    }

    @Test
    fun `falls back to API name when country code cannot be resolved`() {
        testScope.runTest {
            coEvery { repository.fetchAvailableCountries() } returns Result.success(
                FhirCountryErpModelCollection(
                    listOf(createMockFhirCountryModel("SomeName", "XX"))
                )
            )

            val result = useCaseUnderTest.invoke()

            assertEquals("SomeName", result[0].name)
        }
    }

    private fun createMockFhirCountryModel(name: String?, code: String?): FhirCountryErpModel {
        return mockk {
            every { this@mockk.name } returns name
            every { this@mockk.code } returns code
        }
    }

    private fun createMockCountriesList(): List<Country> {
        return listOf(
            Country("Germany", "DE", "🇩🇪"),
            Country("France", "FR", "🇫🇷"),
            Country("Italy", "IT", "🇮🇹"),
            Country("Spain", "ES", "🇪🇸")
        )
    }
}
