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

package de.gematik.ti.erp.app.vau.repository

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.vau.TestCertificates
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
    lateinit var localDataSource: VauLocalDataSource

    @MockK
    lateinit var remoteDataSource: VauRemoteDataSource

    lateinit var repo: VauRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        repo = VauRepository(localDataSource, remoteDataSource, coroutineRule.dispatchers)
    }

    @Test
    fun `local database is empty - load from remote`() = runTest {
        coEvery { localDataSource.loadUntrusted() } coAnswers { null }
        coEvery { localDataSource.saveLists(any(), any()) } coAnswers { }
        coEvery { localDataSource.deleteAll() } coAnswers { }
        coEvery { remoteDataSource.loadCertificates() } coAnswers { Result.success(TestCertificates.Vau.CertList) }
        coEvery { remoteDataSource.loadOcspResponses() } coAnswers { Result.success(TestCertificates.OCSP.OCSPList) }

        repo.withUntrusted { certs, ocsp ->
            assertEquals(TestCertificates.Vau.CertList, certs)
            assertEquals(TestCertificates.OCSP.OCSPList, ocsp)
        }

        coVerify(exactly = 1) { remoteDataSource.loadCertificates() }
        coVerify(exactly = 1) { remoteDataSource.loadOcspResponses() }
        coVerify(exactly = 1) { localDataSource.loadUntrusted() }
        coVerify(exactly = 1) { localDataSource.saveLists(any(), any()) }
    }

    @Test
    fun `local database is empty - load from remote fails`() = runTest {
        coEvery { localDataSource.loadUntrusted() } coAnswers { null }
        coEvery { localDataSource.saveLists(any(), any()) } coAnswers { }
        coEvery { localDataSource.deleteAll() } coAnswers { }
        coEvery { remoteDataSource.loadCertificates() } coAnswers { Result.failure(IOException()) }
        coEvery { remoteDataSource.loadOcspResponses() } coAnswers { Result.success(TestCertificates.OCSP.OCSPList) }

        val r = try {
            repo.withUntrusted { certs, ocsp ->
                false
            }
        } catch (_: Exception) {
            true
        }

        assertTrue(r)

        coVerify(exactly = 1) { remoteDataSource.loadCertificates() }
        coVerify(exactly = 1) { remoteDataSource.loadOcspResponses() }
        coVerify(exactly = 1) { localDataSource.loadUntrusted() }
        coVerify(exactly = 0) { localDataSource.saveLists(any(), any()) }
    }

    @Test
    fun `local database is not empty`() = runTest {
        coEvery { localDataSource.loadUntrusted() } coAnswers {
            Pair(
                TestCertificates.Vau.CertList,
                TestCertificates.OCSP.OCSPList
            )
        }
        coEvery { localDataSource.saveLists(any(), any()) } coAnswers { }
        coEvery { localDataSource.deleteAll() } coAnswers { }

        repo.withUntrusted { certs, ocsp ->
            assertEquals(TestCertificates.Vau.CertList, certs)
            assertEquals(TestCertificates.OCSP.OCSPList, ocsp)
        }

        coVerify(exactly = 0) { remoteDataSource.loadCertificates() }
        coVerify(exactly = 0) { remoteDataSource.loadOcspResponses() }
        coVerify(exactly = 1) { localDataSource.loadUntrusted() }
        coVerify(exactly = 1) { localDataSource.saveLists(any(), any()) }
    }

    @Test
    fun `local database is not empty - exception thrown in block of withUntrusted`() =
        runTest {
            coEvery { localDataSource.loadUntrusted() } coAnswers {
                Pair(
                    TestCertificates.Vau.CertList,
                    TestCertificates.OCSP.OCSPList
                )
            }

            coEvery { localDataSource.saveLists(any(), any()) } coAnswers { }
            coEvery { localDataSource.deleteAll() } coAnswers { }

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

            coVerify(exactly = 0) { remoteDataSource.loadCertificates() }
            coVerify(exactly = 0) { remoteDataSource.loadOcspResponses() }
            coVerify(exactly = 1) { localDataSource.loadUntrusted() }
            coVerify(exactly = 0) { localDataSource.saveLists(any(), any()) }
        }
}
