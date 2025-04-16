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

package de.gematik.ti.erp.app.pharmacy.repository.datasource.remote

import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.HTTP_UNAUTHORIZED
import de.gematik.ti.erp.app.api.UnauthorizedException
import de.gematik.ti.erp.app.pharmacy.api.ApoVzdPharmacySearchService
import de.gematik.ti.erp.app.pharmacy.api.FhirVzdPharmacySearchService
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyFilter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PharmacyRemoteDataSourceTest {

    private lateinit var apoVzdRemoteDataSource: ApoVzdRemoteDataSource
    private lateinit var fhirVzdRemoteDataSource: FhirVzdRemoteDataSource

    private lateinit var apoVzdSearchService: ApoVzdPharmacySearchService
    private lateinit var fhirVzdSearchService: FhirVzdPharmacySearchService

    @Before
    fun setUp() {
        apoVzdSearchService = mockk()
        fhirVzdSearchService = mockk()

        apoVzdRemoteDataSource = ApoVzdRemoteDataSource(
            searchService = apoVzdSearchService,
            redeemService = mockk()
        )

        fhirVzdRemoteDataSource = FhirVzdRemoteDataSource(
            searchService = fhirVzdSearchService
        )
    }

    @Test
    fun `ApoVzdRemoteDataSource - searchPharmacyByTelematikId + search returns success`() = runTest {
        // Given
        val telematikId = "123456789"
        val mockJsonElement = mockk<JsonElement>()
        coEvery { apoVzdSearchService.searchByTelematikId(telematikId) } returns Response.success(mockJsonElement)
        coEvery { apoVzdSearchService.search(emptyList(), emptyMap()) } returns Response.success(mockJsonElement)

        // When
        val itemResult = apoVzdRemoteDataSource.searchPharmacyByTelematikId(telematikId) {}
        val listResult = apoVzdRemoteDataSource.searchPharmacies(PharmacyFilter()) {}

        // Then
        coVerify { apoVzdSearchService.searchByTelematikId(telematikId) }
        coVerify { apoVzdSearchService.search(emptyList(), emptyMap()) }
        assertTrue(itemResult.isSuccess)
        assertTrue(listResult.isSuccess)
        assertEquals(mockJsonElement, itemResult.getOrNull())
        assertEquals(mockJsonElement, listResult.getOrNull())
    }

    @Test
    fun `ApoVzdRemoteDataSource - searchPharmacyByTelematikId + search returns failure`() = runTest {
        // Given
        val telematikId = "123456789"
        val exception = RuntimeException("API Error")
        coEvery { apoVzdSearchService.searchByTelematikId(telematikId) } throws exception
        coEvery { apoVzdSearchService.search(emptyList(), emptyMap()) } throws exception

        // When
        val itemResult = apoVzdRemoteDataSource.searchPharmacyByTelematikId(telematikId) {}
        val listResult = apoVzdRemoteDataSource.searchPharmacies(PharmacyFilter()) {}

        // Then
        coVerify { apoVzdSearchService.searchByTelematikId(telematikId) }
        coVerify { apoVzdSearchService.search(emptyList(), emptyMap()) }

        assertTrue(itemResult.isFailure)
        assertTrue(listResult.isFailure)

        val resultException = itemResult.exceptionOrNull()
        val listResultException = listResult.exceptionOrNull()
        assertTrue(resultException is IOException)
        assertTrue(listResultException is IOException)
    }

    @Test
    fun `FhirVzdRemoteDataSource - searchPharmacyByTelematikId + search returns success`() = runTest {
        // Given
        val telematikId = "123456789"
        val mockJsonElement = mockk<JsonElement>()
        val mockResponse = mockk<Response<JsonElement>> {
            every { isSuccessful } returns true
            every { body() } returns mockJsonElement
        }
        coEvery { fhirVzdSearchService.searchByTelematikId(telematikId = telematikId, status = null) } returns mockResponse
        coEvery {
            fhirVzdSearchService.search(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns mockResponse

        // When
        val itemResult = fhirVzdRemoteDataSource.searchPharmacyByTelematikId(telematikId) {}
        val listResult = fhirVzdRemoteDataSource.searchPharmacies(PharmacyFilter()) {}

        // Then
        coVerify { fhirVzdSearchService.searchByTelematikId(telematikId = telematikId, status = null) }
        coVerify { fhirVzdSearchService.search(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }

        assertTrue(itemResult.isSuccess)
        assertTrue(listResult.isSuccess)
        assertEquals(mockJsonElement, itemResult.getOrNull())
        assertEquals(mockJsonElement, listResult.getOrNull())
    }

    @Test
    fun `FhirVzdRemoteDataSource - searchPharmacyByTelematikId + search returns unauthorized exception`() = runTest {
        // Given
        val telematikId = "123456789"
        val unauthorizedException = UnauthorizedException("Unauthorized", null)

        coEvery { fhirVzdSearchService.searchByTelematikId(telematikId = telematikId, status = null) } throws unauthorizedException
        coEvery {
            fhirVzdSearchService.search(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws unauthorizedException

        // When
        val itemResult = fhirVzdRemoteDataSource.searchPharmacyByTelematikId(telematikId) {}
        val listResult = fhirVzdRemoteDataSource.searchPharmacies(PharmacyFilter()) {}

        // Then
        coVerify { fhirVzdSearchService.searchByTelematikId(telematikId = telematikId, status = null) }
        coVerify { fhirVzdSearchService.search(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
        assertTrue(itemResult.isFailure)
        assertTrue(listResult.isFailure)

        assertTrue(itemResult.exceptionOrNull() is IOException)
        assertTrue(listResult.exceptionOrNull() is IOException)
    }

    @Test
    fun `FhirVzdRemoteDataSource - searchPharmacyByTelematikId + search handles unauthorized exception by clearing token`() = runTest {
        // Given
        val telematikId = "123456789"
        val responseBody = "".toResponseBody("application/json".toMediaType())

        var itemTokenCleared = false
        var listTokenCleared = false
        coEvery { fhirVzdSearchService.searchByTelematikId(telematikId = telematikId, status = null) } returns Response.error(HTTP_UNAUTHORIZED, responseBody)
        coEvery {
            fhirVzdSearchService.search(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Response.error(HTTP_UNAUTHORIZED, responseBody)

        // When
        val itemResult = fhirVzdRemoteDataSource.searchPharmacyByTelematikId(telematikId) {
            itemTokenCleared = true // Simulating token clearance
        }

        val listResult = fhirVzdRemoteDataSource.searchPharmacyByTelematikId(telematikId) {
            listTokenCleared = true // Simulating token clearance
        }

        // Then
        coVerify { fhirVzdSearchService.searchByTelematikId(telematikId = telematikId, status = null) }
        assertTrue(itemResult.isFailure)
        assertTrue(listResult.isFailure)
        assertTrue(itemResult.exceptionOrNull() is ApiCallException)
        assertTrue(listResult.exceptionOrNull() is ApiCallException)
        assertTrue(itemTokenCleared) // Ensure token clearance was triggered
        assertTrue(listTokenCleared) // Ensure token clearance was triggered
    }
}
