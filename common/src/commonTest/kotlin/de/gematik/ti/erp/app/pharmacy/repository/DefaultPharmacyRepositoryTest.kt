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

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.fhir.common.model.erp.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.parser.PharmacyParsers
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService.FHIRVZD
import de.gematik.ti.erp.app.messages.repository.PharmacyCacheLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.PharmacyRemoteSelectorLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.ApoVzdRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.FhirVzdRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.PharmacyRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.usecase.model.LocationFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyFilter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import kotlin.test.Test

class DefaultPharmacyRepositoryTest {

    private lateinit var repository: DefaultPharmacyRepository
    private val remoteSelector = mockk<PharmacyRemoteSelectorLocalDataSource>()
    private val apoVzdRemoteDataSource = mockk<ApoVzdRemoteDataSource>()
    private val fhirVzdRemoteDataSource = mockk<FhirVzdRemoteDataSource>()
    private val searchAccessTokenLocalDataSource = mockk<PharmacySearchAccessTokenLocalDataSource>(relaxed = true)

    private val cachedPharmacyLocalDataSource = mockk<PharmacyCacheLocalDataSource>(relaxed = true)
    private val parser = mockk<PharmacyParsers>()
    private val filter = PharmacyFilter()

    private lateinit var remoteDataSource: PharmacyRemoteDataSource

    @Before
    fun setUp() {
        every { remoteSelector.getPharmacyVzdService() } returns FHIRVZD
        every { parser.bundleParser.extract(any()) } returns expectedCollection
        coEvery { cachedPharmacyLocalDataSource.savePharmacy(any(), any()) } returns Unit
        every { cachedPharmacyLocalDataSource.loadPharmacies() } returns flowOf(emptyList())
        remoteDataSource = fhirVzdRemoteDataSource

        repository = DefaultPharmacyRepository(
            remoteDataSourceSelector = remoteSelector,
            apoVzdRemoteDataSource = apoVzdRemoteDataSource,
            fhirVzdRemoteDataSource = fhirVzdRemoteDataSource,
            searchAccessTokenLocalDataSource = searchAccessTokenLocalDataSource,
            cachedPharmacyLocalDataSource = cachedPharmacyLocalDataSource,
            parsers = parser,
            redeemLocalDataSource = mockk(),
            favouriteLocalDataSource = mockk(),
            oftenUsedLocalDataSource = mockk()
        )
    }

    @Test
    fun `searchPharmacies for fhirvzd returns expected data`() = runTest {
        coEvery { remoteDataSource.searchPharmacies(filter, any()) } returns Result.success(mockJsonElement)

        val result = repository.searchPharmacies(filter)

        assertTrue(result.isSuccess)
        assertEquals(expectedCollection, result.getOrNull())
        coVerify { remoteDataSource.searchPharmacies(filter, any()) }
    }

    @Test
    fun `searchPharmacies handles failure correctly`() = runTest {
        val expectedException = Exception("Network Error")

        coEvery { remoteDataSource.searchPharmacies(filter, any()) } returns Result.failure(expectedException)

        val result = repository.searchPharmacies(filter)

        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `searchPharmacyByTelematikId returns expected data`() = runTest {
        val telematikId = "12345"

        coEvery { remoteDataSource.searchPharmacyByTelematikId(telematikId, any()) } returns Result.success(mockJsonElement)

        val result = repository.searchPharmacyByTelematikId(telematikId)

        assertTrue(result.isSuccess)
        assertEquals(expectedCollection, result.getOrNull())
        coVerify { remoteDataSource.searchPharmacyByTelematikId(telematikId, any()) }
    }

    @Test
    fun `searchPharmacyByTelematikId handles failure correctly`() = runTest {
        val telematikId = "12345"
        val expectedException = Exception("API Error")

        coEvery { remoteDataSource.searchPharmacyByTelematikId(telematikId, any()) } returns Result.failure(expectedException)

        val result = repository.searchPharmacyByTelematikId(telematikId)

        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `progressiveRadiusSearch stops at 2km and returns 51 Entries directly`() = runTest {
        val filter = PharmacyFilter(locationFilter = LocationFilter(0.0, 0.0))
        val twoKmEntries = (1..51).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val expectedCollection = FhirPharmacyErpModelCollection(FHIRVZD, 51, "", twoKmEntries)

        coEvery { remoteDataSource.searchPharmacies(any(), any()) } returns Result.success(mockJsonElement)
        coEvery { parser.bundleParser.extract(any()) } returns expectedCollection

        val result = repository.searchPharmacies(filter)

        assertTrue(result.isSuccess)
        assertEquals(51, result.getOrNull()?.entries?.size)
        coVerify(exactly = 1) { remoteDataSource.searchPharmacies(any(), any()) }
    }

    @Test
    fun `progressiveRadiusSearch stops at 2km and returns 100 Entries directly`() = runTest {
        val filter = PharmacyFilter(locationFilter = LocationFilter(0.0, 0.0))
        val twoKmEntries = (1..100).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val expectedCollection = FhirPharmacyErpModelCollection(FHIRVZD, 100, "", twoKmEntries)

        coEvery { remoteDataSource.searchPharmacies(any(), any()) } returns Result.success(mockJsonElement)
        coEvery { parser.bundleParser.extract(any()) } returns expectedCollection

        val result = repository.searchPharmacies(filter)

        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.entries?.size)
        coVerify(exactly = 1) { remoteDataSource.searchPharmacies(any(), any()) }
    }

    @Test
    fun `progressiveRadiusSearch expands to 3km when needed and returns 99 Entries`() = runTest {
        val filter = PharmacyFilter(locationFilter = mockk(relaxed = true))
        val twoKmEntries = (1..25).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val threeKmEntries = (1..99).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val smallResult = FhirPharmacyErpModelCollection(FHIRVZD, 25, "", twoKmEntries)
        val largeResult = FhirPharmacyErpModelCollection(FHIRVZD, 99, "", threeKmEntries)

        coEvery { remoteDataSource.searchPharmacies(any(), any()) } returnsMany listOf(
            Result.success(mockJsonElement),
            Result.success(mockJsonElement)
        )
        coEvery { parser.bundleParser.extract(mockJsonElement) } returnsMany listOf(smallResult, largeResult)

        val result = repository.searchPharmacies(filter)

        assertTrue(result.isSuccess)
        assertEquals(99, result.getOrNull()?.entries?.size)
        coVerify(exactly = 2) { remoteDataSource.searchPharmacies(any(), any()) }
    }

    @Test
    fun `progressiveRadiusSearch expands to 50km when needed but returns the value of the 20km search`() = runTest {
        val filter = PharmacyFilter(locationFilter = mockk(relaxed = true))
        val twoKmEntries = (1..2).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val threeKmEntries = (1..3).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val fiveKmEntries = (1..5).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val tenKmEntries = (1..10).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val twentyKmEntries = (1..50).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val fiftyKmEntries = (1..100).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val twoKmResult = FhirPharmacyErpModelCollection(FHIRVZD, 2, "", twoKmEntries)
        val threeKmResult = FhirPharmacyErpModelCollection(FHIRVZD, 3, "", threeKmEntries)
        val fiveKmResult = FhirPharmacyErpModelCollection(FHIRVZD, 4, "", fiveKmEntries)
        val tenKmResult = FhirPharmacyErpModelCollection(FHIRVZD, 10, "", tenKmEntries)
        val twentyKmResult = FhirPharmacyErpModelCollection(FHIRVZD, 50, "", twentyKmEntries)
        val fiftyKmResult = FhirPharmacyErpModelCollection(FHIRVZD, 100, "", fiftyKmEntries)

        coEvery { remoteDataSource.searchPharmacies(any(), any()) } returnsMany listOf(
            Result.success(mockJsonElement),
            Result.success(mockJsonElement),
            Result.success(mockJsonElement),
            Result.success(mockJsonElement),
            Result.success(mockJsonElement),
            Result.success(mockJsonElement)
        )
        coEvery { parser.bundleParser.extract(mockJsonElement) } returnsMany listOf(
            twoKmResult,
            threeKmResult,
            fiveKmResult,
            tenKmResult,
            twentyKmResult,
            fiftyKmResult
        )

        val result = repository.searchPharmacies(filter)

        assertTrue(result.isSuccess)
        assertEquals(50, result.getOrNull()?.entries?.size)
        coVerify(exactly = 6) { remoteDataSource.searchPharmacies(any(), any()) }
    }

    companion object {
        private val expectedCollection = FhirPharmacyErpModelCollection(FHIRVZD, 0, "", emptyList())
        private val mockJsonElement = mockk<JsonElement>()
    }
}
