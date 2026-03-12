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

package de.gematik.ti.erp.app.vau.repository

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.database.api.TrustStoreLocalDataSource
import de.gematik.ti.erp.app.vau.TestCertificates
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.model.TrustStoreErpModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class VauRepositoryTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK
    lateinit var trustStoreLocalDataSource: TrustStoreLocalDataSource

    @MockK
    lateinit var remoteDataSource: VauRemoteDataSource

    lateinit var repo: VauRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val config = de.gematik.ti.erp.app.vau.usecase.TruststoreConfig { TestCertificates.RCA3.Base64 }
        repo = VauRepository(trustStoreLocalDataSource, remoteDataSource, coroutineRule.dispatchers.io, config)
    }

    @Test
    fun `local database is empty - load from remote`() = runTest {
        coEvery { trustStoreLocalDataSource.loadUntrusted() } returns flowOf(null)
        coEvery { trustStoreLocalDataSource.saveCertificateAndOcspLists(any(), any()) } coAnswers { }
        coEvery { trustStoreLocalDataSource.deleteAll() } coAnswers { }
        coEvery { remoteDataSource.loadPkiCertificates(any()) } returns Result.success(TestCertificates.Vau.PkiList)
        coEvery { remoteDataSource.loadVauCertificates() } returns Result.success(TestCertificates.Vau.VauCert)
        coEvery { remoteDataSource.loadOcspResponse(any(), any()) } returns Result.success(TestCertificates.OCSP.OCSPList)

        repo.withUntrusted { certs, ocsp ->
            assertEquals(TestCertificates.Vau.PkiWithVauCertList, certs)
            assertEquals(TestCertificates.OCSP.OCSPList, ocsp)
        }

        coVerify(exactly = 1) { remoteDataSource.loadPkiCertificates(any()) }
        coVerify(exactly = 1) { remoteDataSource.loadVauCertificates() }
        coVerify(exactly = 1) { remoteDataSource.loadOcspResponse(any(), any()) }
        coVerify(exactly = 1) { trustStoreLocalDataSource.loadUntrusted() }
        coVerify(exactly = 0) { trustStoreLocalDataSource.saveCertificateAndOcspLists(any(), any()) }
    }

    @Test
    fun `local database is empty - load from remote fails`() = runTest {
        coEvery { trustStoreLocalDataSource.loadUntrusted() } returns flowOf(null)
        coEvery { trustStoreLocalDataSource.deleteAll() } coAnswers { }
        coEvery { remoteDataSource.loadPkiCertificates(any()) } returns Result.failure(IOException())
        // still stub others to avoid unexpected nulls if they get called before failure bubbles
        coEvery { remoteDataSource.loadVauCertificates() } returns Result.success(TestCertificates.Vau.VauCert)
        coEvery { remoteDataSource.loadOcspResponse(any(), any()) } returns Result.success(TestCertificates.OCSP.OCSPList)

        val r = try {
            repo.withUntrusted { certs, ocsp ->
                false
            }
        } catch (_: Exception) {
            true
        }

        assertTrue(r)

        coVerify(exactly = 1) { remoteDataSource.loadPkiCertificates(any()) }
        coVerify(exactly = 0) { remoteDataSource.loadVauCertificates() }
        coVerify(exactly = 0) { remoteDataSource.loadOcspResponse(any(), any()) }
        coVerify(exactly = 1) { trustStoreLocalDataSource.loadUntrusted() }
        coVerify(exactly = 0) { trustStoreLocalDataSource.saveCertificateAndOcspLists(any(), any()) }
    }

    @Test
    fun `local database is not empty`() = runTest {
        val certListJson = Json.encodeToString(UntrustedCertList.serializer(), TestCertificates.Vau.PkiList)
        val ocspListJson = Json.encodeToString(UntrustedOCSPList.serializer(), TestCertificates.OCSP.OCSPList)

        coEvery { trustStoreLocalDataSource.loadUntrusted() } returns flowOf(
            TrustStoreErpModel(
                certListJson = certListJson,
                ocspListJson = ocspListJson
            )
        )
        coEvery { trustStoreLocalDataSource.saveCertificateAndOcspLists(any(), any()) } coAnswers { }
        coEvery { trustStoreLocalDataSource.deleteAll() } coAnswers { }
        coEvery { remoteDataSource.loadVauCertificates() } returns Result.success(TestCertificates.Vau.VauCert)
        coEvery { remoteDataSource.loadOcspResponse(any(), any()) } returns Result.success(TestCertificates.OCSP.OCSPList)

        repo.withUntrusted { certs, ocsp ->
            assertEquals(TestCertificates.Vau.PkiList, certs)
            assertEquals(TestCertificates.OCSP.OCSPList, ocsp)
        }

        coVerify(exactly = 0) { remoteDataSource.loadPkiCertificates() }
        coVerify(exactly = 0) { remoteDataSource.loadOcspResponse(any(), any()) }
        coVerify(exactly = 1) { trustStoreLocalDataSource.loadUntrusted() }
        coVerify(exactly = 0) { trustStoreLocalDataSource.saveCertificateAndOcspLists(any(), any()) }
    }

    @Test
    fun `local database is not empty - exception thrown in block of withUntrusted`() =
        runTest {
            val certListJson = Json.encodeToString(UntrustedCertList.serializer(), TestCertificates.Vau.CertList)
            val ocspListJson = Json.encodeToString(UntrustedOCSPList.serializer(), TestCertificates.OCSP.OCSPList)

            coEvery { trustStoreLocalDataSource.loadUntrusted() } returns flowOf(
                TrustStoreErpModel(
                    certListJson = certListJson,
                    ocspListJson = ocspListJson
                )
            )

            coEvery { trustStoreLocalDataSource.saveCertificateAndOcspLists(any(), any()) } coAnswers { }
            coEvery { trustStoreLocalDataSource.deleteAll() } coAnswers { }
            coEvery { remoteDataSource.loadVauCertificates() } returns Result.success(TestCertificates.Vau.VauCert)

            val r = try {
                repo.withUntrusted { certs, ocsp ->
                    assertEquals(TestCertificates.Vau.CertList, certs)
                    assertEquals(TestCertificates.OCSP.OCSPList, ocsp)
                    error("fail")
                }
            } catch (_: Exception) {
                true
            }

            assertTrue(r)

            coVerify(exactly = 0) { remoteDataSource.loadPkiCertificates() }
            coVerify(exactly = 0) { remoteDataSource.loadOcspResponse(any(), any()) }
            coVerify(exactly = 1) { trustStoreLocalDataSource.loadUntrusted() }
            coVerify(exactly = 0) { trustStoreLocalDataSource.saveCertificateAndOcspLists(any(), any()) }
        }
}
