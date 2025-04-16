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

package de.gematik.ti.erp.app.pharmacy.repository

import de.gematik.ti.erp.app.fhir.common.model.erp.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.pharmacy.model.erp.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.parser.PharmacyBundleParser
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService.FHIRVZD
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
    private val parser = mockk<PharmacyBundleParser>()
    private val filter = PharmacyFilter()

    private lateinit var remoteDataSource: PharmacyRemoteDataSource

    @Before
    fun setUp() {
        every { remoteSelector.getPharmacyVzdService() } returns FHIRVZD
        every { parser.extract(any()) } returns expectedCollection
        remoteDataSource = fhirVzdRemoteDataSource

        repository = DefaultPharmacyRepository(
            remoteDataSourceSelector = remoteSelector,
            apoVzdRemoteDataSource = apoVzdRemoteDataSource,
            fhirVzdRemoteDataSource = fhirVzdRemoteDataSource,
            searchAccessTokenLocalDataSource = searchAccessTokenLocalDataSource,
            parser = parser,
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
    fun `progressiveRadiusSearch stops at 5km when sufficient results found`() = runTest {
        val filter = PharmacyFilter(locationFilter = LocationFilter(0.0, 0.0))
        val uniqueEntries = (1..101).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val expectedCollection = FhirPharmacyErpModelCollection(FHIRVZD, 101, "", uniqueEntries)

        coEvery { remoteDataSource.searchPharmacies(any(), any()) } returns Result.success(mockJsonElement)
        coEvery { parser.extract(any()) } returns expectedCollection

        val result = repository.searchPharmacies(filter)

        assertTrue(result.isSuccess)
        assertEquals(101, result.getOrNull()?.entries?.size)
        coVerify(exactly = 1) { remoteDataSource.searchPharmacies(any(), any()) }
    }

    @Test
    fun `progressiveRadiusSearch expands to 25km when needed`() = runTest {
        val filter = PharmacyFilter(locationFilter = mockk(relaxed = true))
        val smallEntries = (1..10).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val largeEntries = (1..110).map { index ->
            mockk<FhirPharmacyErpModel> {
                every { telematikId } returns "telematik_$index"
            }
        }
        val smallResult = FhirPharmacyErpModelCollection(FHIRVZD, 10, "", smallEntries)
        val largeResult = FhirPharmacyErpModelCollection(FHIRVZD, 110, "", largeEntries)

        coEvery { remoteDataSource.searchPharmacies(any(), any()) } returnsMany listOf(
            Result.success(mockJsonElement),
            Result.success(mockJsonElement),
            Result.success(mockJsonElement),
            Result.success(mockJsonElement),
            Result.success(mockJsonElement)
        )
        coEvery { parser.extract(mockJsonElement) } returnsMany listOf(smallResult, smallResult, smallResult, smallResult, largeResult)

        val result = repository.searchPharmacies(filter)

        assertTrue(result.isSuccess)
        assertEquals(110, result.getOrNull()?.entries?.size)
        coVerify(exactly = 5) { remoteDataSource.searchPharmacies(any(), any()) }
    }

    companion object {
        private val expectedCollection = FhirPharmacyErpModelCollection(FHIRVZD, 0, "", emptyList())
        private val mockJsonElement = mockk<JsonElement>()
    }
}
