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

import de.gematik.ti.erp.app.database.api.PharmacyLocalDataSource
import de.gematik.ti.erp.app.database.api.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.fhir.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.pharmacy.model.FhirPharmacyErpModel
import de.gematik.ti.erp.app.fhir.pharmacy.parser.PharmacyParsers
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService.FHIRVZD
import de.gematik.ti.erp.app.messages.repository.PharmacyCacheLocalDataSource
import de.gematik.ti.erp.app.pharmacy.model.PharmacyErpModel
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.PharmacyRemoteSelectorLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.DefaultPharmacyRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.PharmacyRemoteDataSource
import de.gematik.ti.erp.app.pharmacy.usecase.model.LocationFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyFilter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import kotlin.test.Test

class DefaultPharmacyRepositoryTest {

    private lateinit var repository: DefaultPharmacyRepository
    private val remoteSelector = mockk<PharmacyRemoteSelectorLocalDataSource>()
    private val defaultPharmacyRemoteDataSource = mockk<DefaultPharmacyRemoteDataSource>()
    private val searchAccessTokenLocalDataSource = mockk<PharmacySearchAccessTokenLocalDataSource>(relaxed = true)
    private val cachedPharmacyLocalDataSource = mockk<PharmacyCacheLocalDataSource>(relaxed = true)
    private val parser = mockk<PharmacyParsers>()
    private val filter = PharmacyFilter()
    private val pharmacyLocalDataSource = mockk<PharmacyLocalDataSource>()

    private lateinit var remoteDataSource: PharmacyRemoteDataSource

    @Before
    fun setUp() {
        every { remoteSelector.getPharmacyVzdService() } returns FHIRVZD
        every { parser.bundleParser.extract(any()) } returns expectedCollection
        coEvery { cachedPharmacyLocalDataSource.savePharmacy(any(), any()) } returns Unit
        every { cachedPharmacyLocalDataSource.loadPharmacies() } returns flowOf(emptyList())
        remoteDataSource = defaultPharmacyRemoteDataSource

        repository = DefaultPharmacyRepository(
            pharmacyRemoteDataSource = defaultPharmacyRemoteDataSource,
            pharmacySearchAccessTokenLocalDataSource = searchAccessTokenLocalDataSource,
            cachedPharmacyLocalDataSource = cachedPharmacyLocalDataSource,
            parsers = parser,
            redeemLocalDataSource = mockk(),
            pharmacyLocalDataSource = pharmacyLocalDataSource
        )
    }

    @Test
    fun `loadPharmacies delegates to local data source and emits expected list`() = runTest {
        val expected = listOf(
            PharmacyErpModel(
                telematikId = "id1",
                name = "Pharmacy 1",
                address = null,
                contact = null,
                isFavorite = true,
                isOftenUsed = false,
                lastUsed = Instant.parse("2024-08-01T10:00:00Z")
            ),
            PharmacyErpModel(
                telematikId = "id2",
                name = "Pharmacy 2",
                address = null,
                contact = null,
                isFavorite = false,
                isOftenUsed = true,
                lastUsed = Instant.parse("2024-08-02T10:00:00Z")
            )
        )
        every { pharmacyLocalDataSource.loadPharmacies() } returns flowOf(expected)

        val result = repository.loadPharmacies().first()

        assertEquals(expected, result)
        assertEquals(true, result[0].isFavorite)
        assertEquals(false, result[0].isOftenUsed)
        assertEquals(Instant.parse("2024-08-01T10:00:00Z"), result[0].lastUsed)

        assertEquals(false, result[1].isFavorite)
        assertEquals(true, result[1].isOftenUsed)
        assertEquals(Instant.parse("2024-08-02T10:00:00Z"), result[1].lastUsed)

        verify(exactly = 1) { pharmacyLocalDataSource.loadPharmacies() }
    }

    @Test
    fun `loadPharmacies emits empty list when none available`() = runTest {
        every { pharmacyLocalDataSource.loadPharmacies() } returns flowOf(emptyList())

        val result = repository.loadPharmacies().first()

        assertTrue(result.isEmpty())
        verify(exactly = 1) { pharmacyLocalDataSource.loadPharmacies() }
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

    companion object {
        private val expectedCollection = FhirPharmacyErpModelCollection(FHIRVZD, 0, "", emptyList())
        private val mockJsonElement = mockk<JsonElement>()
    }
}
