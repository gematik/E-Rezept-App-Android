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

package de.gematik.ti.erp.app.vau.usecase

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.vau.TestCertificates
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.repository.VauRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.security.interfaces.ECPublicKey
import java.util.Date
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class TruststoreUseCaseTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK
    lateinit var config: TruststoreConfig

    @MockK
    lateinit var repository: VauRepository

    @MockK
    lateinit var timeSource: TruststoreTimeSourceProvider

    @MockK
    lateinit var trustedTruststoreProvider: TrustedTruststoreProvider

    @MockK
    lateinit var trustedTruststore: TrustedTruststore

    private val vauPublicKey =
        KeyFactorySpi.EC().generatePublic(TestCertificates.Vau.X509Certificate.subjectPublicKeyInfo)!! as ECPublicKey
    private val ocspProducedAt = TestCertificates.OCSP1.ProducedAt

    lateinit var truststore: TruststoreUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { config.getOcspMaxAge() } returns 12.hours
        every { config.trustAnchor } returns TestCertificates.RCA3.X509Certificate
        every { timeSource() } returns ocspProducedAt + 2.hours
        every { trustedTruststore.vauPublicKey } returns vauPublicKey
        every { trustedTruststore.idpCertificates } returns
            listOf(TestCertificates.Idp1.X509Certificate, TestCertificates.Idp2.X509Certificate)
        every { trustedTruststore.caCertificates } returns listOf(TestCertificates.CA10.X509Certificate)
        every { trustedTruststore.ocspResponses } returns
            TestCertificates.OCSP.OCSPList.responses.map { it.responseObject as BasicOCSPResp }
        every { trustedTruststore.checkValidity(12.hours, any()) } returns Unit
        coEvery { repository.invalidate() } returns Unit

        truststore = TruststoreUseCase(
            config,
            repository,
            timeSource,
            trustedTruststoreProvider
        )
    }

    @Test
    fun `new instance of truststore contains no cached store - fetches and creates store from repository`() =
        runTest {
            coEvery { repository.withUntrusted<Boolean>(any()) } coAnswers {
                firstArg<suspend (UntrustedCertList, UntrustedOCSPList) -> Boolean>().invoke(
                    TestCertificates.Vau.CertList,
                    TestCertificates.OCSP.OCSPList
                )
            }
            every {
                trustedTruststoreProvider(
                    TestCertificates.OCSP.OCSPList,
                    TestCertificates.Vau.CertList,
                    TestCertificates.RCA3.X509Certificate,
                    12.hours,
                    any()
                )
            } returns trustedTruststore

            truststore.withValidVauPublicKey {
                assertEquals(vauPublicKey, it)
            }

            coVerify(exactly = 1) { repository.withUntrusted(any()) }
            coVerify(exactly = 0) { repository.invalidate() }
            verify(exactly = 1) { trustedTruststore.vauPublicKey }
            verify(exactly = 0) { trustedTruststore.checkValidity(any(), any()) }
        }

    @Test
    fun `validateIdpCertificate returns null when certificate is found`() {
        // Given
        val idpCertificate = TestCertificates.Idp1.X509Certificate

        // When
        val result = truststore.validateIdpCertificate(trustedTruststore, idpCertificate, false)

        // Then
        assertEquals(null, result)
    }

    @Test
    fun `validateIdpCertificate returns exception when certificate is not found`() {
        // Given
        val idpCertificate = TestCertificates.Idp3.X509Certificate

        // When
        val result = truststore.validateIdpCertificate(trustedTruststore, idpCertificate, false)

        // Then
        assertTrue(result is IllegalArgumentException)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `validateIdpCertificate throws exception when certificate is not found and invalidateStoreOnFailure is true`() {
        // Given
        val idpCertificate = TestCertificates.Idp3.X509Certificate

        // When/Then
        truststore.validateIdpCertificate(trustedTruststore, idpCertificate, true)
    }

    @Test
    fun `getValidVauPublicKey returns vauPublicKey from truststore`() = runTest {
        // Given
        coEvery { repository.withUntrusted<Boolean>(any()) } coAnswers {
            firstArg<suspend (UntrustedCertList, UntrustedOCSPList) -> Boolean>().invoke(
                TestCertificates.Vau.CertList,
                TestCertificates.OCSP.OCSPList
            )
        }
        every {
            trustedTruststoreProvider(
                TestCertificates.OCSP.OCSPList,
                TestCertificates.Vau.CertList,
                TestCertificates.RCA3.X509Certificate,
                12.hours,
                any()
            )
        } returns trustedTruststore

        // When
        val result = truststore.getValidVauPublicKey(timeSource())

        // Then
        assertEquals(vauPublicKey, result)
    }

    @Test
    fun `getValidTruststore returns cached truststore when it is valid`() = runTest {
        // Given
        truststore.cachedTruststore = trustedTruststore

        // When
        val result = truststore.getValidTruststore(timeSource())

        // Then
        assertEquals(trustedTruststore, result)
        verify(exactly = 1) { trustedTruststore.checkValidity(12.hours, any()) }
    }

    @Test
    fun `getValidTruststore creates new truststore when cached truststore is invalid`() = runTest {
        // Given
        truststore.cachedTruststore = trustedTruststore
        every { trustedTruststore.checkValidity(12.hours, any()) } throws IllegalStateException("Invalid store")

        coEvery { repository.withUntrusted<Boolean>(any()) } coAnswers {
            firstArg<suspend (UntrustedCertList, UntrustedOCSPList) -> Boolean>().invoke(
                TestCertificates.Vau.CertList,
                TestCertificates.OCSP.OCSPList
            )
        }
        every {
            trustedTruststoreProvider(
                TestCertificates.OCSP.OCSPList,
                TestCertificates.Vau.CertList,
                TestCertificates.RCA3.X509Certificate,
                12.hours,
                any()
            )
        } returns trustedTruststore

        // When
        val result = truststore.getValidTruststore(timeSource())

        // Then
        assertEquals(trustedTruststore, result)
        verify(exactly = 1) { trustedTruststore.checkValidity(12.hours, any()) }
        coVerify(exactly = 1) { repository.invalidate() }
    }

    @Test
    fun `createTrustedTruststore creates truststore from repository`() = runTest {
        // Given
        coEvery { repository.withUntrusted<Boolean>(any()) } coAnswers {
            firstArg<suspend (UntrustedCertList, UntrustedOCSPList) -> Boolean>().invoke(
                TestCertificates.Vau.CertList,
                TestCertificates.OCSP.OCSPList
            )
        }
        every {
            trustedTruststoreProvider(
                TestCertificates.OCSP.OCSPList,
                TestCertificates.Vau.CertList,
                TestCertificates.RCA3.X509Certificate,
                12.hours,
                any()
            )
        } returns trustedTruststore

        // When
        val result = truststore.createTrustedTruststore(timeSource())

        // Then
        assertEquals(trustedTruststore, result)
        coVerify(exactly = 1) { repository.withUntrusted(any()) }
    }

    /**
     * Helper function for tests that wraps getValidatedCrossSignedRoots and returns a boolean
     * indicating whether the validation was successful.
     */
    private fun isValidCertificateChain(
        trustAnchor: X509CertificateHolder,
        intermediateCerts: List<X509CertificateHolder>
    ): Boolean {
        val result = getValidatedChainSignedByTrustAnchor(
            trustAnchor,
            intermediateCerts,
            Date(timeSource().toEpochMilliseconds())
        )
        // If the list contains more than just the trust anchor, validation succeeded
        return result.size > 1 || intermediateCerts.isEmpty()
    }

    @Test
    fun `getValidatedChainSignedByTrustAnchor tests multiple certificate chain scenarios`() {
        // Test with valid certificate chain
        val trustAnchor = TestCertificates.TrustAnchorRCA3.X509Certificate
        val validIntermediateCerts = listOf(TestCertificates.RCA4CrossSignedByRCA3.X509Certificate)
        assertTrue(isValidCertificateChain(trustAnchor, validIntermediateCerts))

        // Test with invalid certificate chain
        val invalidIntermediateCerts = listOf(TestCertificates.RCA5CrossSignedByRCA4.X509Certificate)
        assertFalse(isValidCertificateChain(trustAnchor, invalidIntermediateCerts))

        // Test with empty intermediate certificates
        val emptyIntermediateCerts = emptyList<X509CertificateHolder>()
        val result = getValidatedChainSignedByTrustAnchor(
            trustAnchor,
            emptyIntermediateCerts,
            Date(timeSource().toEpochMilliseconds())
        )
        assertEquals(listOf(trustAnchor), result)
    }

    @Test
    fun `getValidatedChainSignedByTrustAnchor returns true for valid certificate chain`() {
        // Given
        val trustAnchor = TestCertificates.TrustAnchorRCA3.X509Certificate
        val intermediateCerts = listOf(
            TestCertificates.RCA4CrossSignedByRCA3.X509Certificate,
            TestCertificates.RCA5CrossSignedByRCA4.X509Certificate
        )

        // When
        val result = getValidatedChainSignedByTrustAnchor(
            trustAnchor,
            intermediateCerts,
            Date(TestCertificates.TestTimestamp.toEpochMilliseconds())
        )

        // Then
        assertEquals(listOf(trustAnchor) + intermediateCerts, result)
    }

    @Test
    fun `getValidatedChainSignedByTrustAnchor returns false for invalid certificate chain`() {
        // Given
        val trustAnchor = TestCertificates.RCA3.X509Certificate
        val intermediateCerts = listOf(TestCertificates.Idp1.X509Certificate)

        // When
        val result = isValidCertificateChain(trustAnchor, intermediateCerts)

        // Then
        assertFalse(result)
    }

    @Test
    fun `getValidatedCertificates validates CA certificate with cross-signed root and no intermediates`() {
        // When
        val result = getValidatedCertificates(
            listOf(TestCertificates.CA51.X509Certificate),
            listOf(TestCertificates.RCA5CrossSignedByRCA4.X509Certificate),
            Date(TestCertificates.TestTimestamp.toEpochMilliseconds())
        )

        // Then
        assertEquals(listOf(TestCertificates.CA51.X509Certificate), result)
    }

    @Test
    fun `getValidatedCertificates returns correct`() {
        // When
        val result = getValidatedCertificates(
            listOf(TestCertificates.CA51.X509Certificate),
            listOf(TestCertificates.RCA5CrossSignedByRCA4.X509Certificate),
            Date(TestCertificates.TestTimestamp.toEpochMilliseconds())
        )

        // Then
        assertEquals(listOf(TestCertificates.CA51.X509Certificate), result)
    }

    @Test
    fun `getValidatedCertificates returns correct certificates for cross to CA with intermediate certificates`() {
        // When
        val result = getValidatedCertificates(
            listOf(TestCertificates.CA51.X509Certificate, TestCertificates.CA10.X509Certificate, TestCertificates.CA11.X509Certificate),
            listOf(TestCertificates.RCA5CrossSignedByRCA4.X509Certificate),
            Date(TestCertificates.TestTimestamp.toEpochMilliseconds())
        )

        // Then
        assertEquals(listOf(TestCertificates.CA51.X509Certificate), result)
    }

    @Test
    fun `getValidatedCertificates returns empty list when date is invalid`() {
        // Given
        val invalidDate = Date(1893456000000) // January 1, 2030 - after certificate expiration

        // When
        val result = getValidatedCertificates(
            listOf(TestCertificates.CA51.X509Certificate),
            listOf(TestCertificates.RCA5CrossSignedByRCA4.X509Certificate),
            invalidDate
        )

        // Then
        assertEquals(emptyList<X509CertificateHolder>(), result)
    }

    @Test
    fun `truststore creation succeeds - idp certificate found`() =
        runTest {
            coEvery { repository.withUntrusted<Boolean>(any()) } coAnswers {
                firstArg<suspend (UntrustedCertList, UntrustedOCSPList) -> Boolean>().invoke(
                    TestCertificates.Vau.CertList,
                    TestCertificates.OCSP.OCSPList
                )
            }

            every {
                trustedTruststoreProvider(
                    TestCertificates.OCSP.OCSPList,
                    TestCertificates.Vau.CertList,
                    TestCertificates.RCA3.X509Certificate,
                    12.hours,
                    any()
                )
            } returns trustedTruststore

            truststore.checkIdpCertificate(TestCertificates.Idp1.X509Certificate)

            coVerifyOrder {
                repository.withUntrusted<Boolean>(any())
                trustedTruststore.idpCertificates
            }
            coVerify(exactly = 1) { repository.withUntrusted(any()) }
            coVerify(exactly = 0) { repository.invalidate() }
            verify(exactly = 0) { trustedTruststore.caCertificates }
            verify(exactly = 0) { trustedTruststore.vauPublicKey }
            verify(exactly = 0) { trustedTruststore.checkValidity(any(), any()) }
        }

    @Test(expected = IllegalArgumentException::class)
    fun `truststore creation succeeds - idp certificate not found`() =
        runTest {
            coEvery { repository.withUntrusted<Boolean>(any()) } coAnswers {
                firstArg<suspend (UntrustedCertList, UntrustedOCSPList) -> Boolean>().invoke(
                    TestCertificates.Vau.CertList,
                    TestCertificates.OCSP.OCSPList
                )
            }

            every {
                trustedTruststoreProvider(
                    TestCertificates.OCSP.OCSPList,
                    TestCertificates.Vau.CertList,
                    TestCertificates.RCA3.X509Certificate,
                    12.hours,
                    any()
                )
            } returns trustedTruststore

            try {
                truststore.checkIdpCertificate(TestCertificates.Idp3.X509Certificate)
            } finally {
                coVerifyOrder {
                    repository.withUntrusted<Boolean>(any())
                    trustedTruststore.idpCertificates
                }
                coVerify(exactly = 1) { repository.withUntrusted(any()) }
                coVerify(exactly = 0) { repository.invalidate() }
                verify(exactly = 0) { trustedTruststore.caCertificates }
                verify(exactly = 0) { trustedTruststore.vauPublicKey }
                verify(exactly = 0) { trustedTruststore.checkValidity(any(), any()) }
            }
        }

    @Test(expected = Exception::class)
    fun `truststore creation succeeds - idp certificate not found - invalidate`() =
        runTest {
            coEvery { repository.withUntrusted<Boolean>(any()) } coAnswers {
                firstArg<suspend (UntrustedCertList, UntrustedOCSPList) -> Boolean>().invoke(
                    TestCertificates.Vau.CertList,
                    TestCertificates.OCSP.OCSPList
                )
            }

            every {
                trustedTruststoreProvider(
                    TestCertificates.OCSP.OCSPList,
                    TestCertificates.Vau.CertList,
                    TestCertificates.RCA3.X509Certificate,
                    12.hours,
                    any()
                )
            } returns trustedTruststore

            try {
                truststore.checkIdpCertificate(TestCertificates.Idp3.X509Certificate, true)
            } finally {
                coVerifyOrder {
                    repository.withUntrusted<Boolean>(any())
                    trustedTruststore.idpCertificates
                    repository.invalidate()
                }
                coVerify(exactly = 1) { repository.withUntrusted(any()) }
                coVerify(exactly = 1) { repository.invalidate() }
                verify(exactly = 0) { trustedTruststore.caCertificates }
                verify(exactly = 0) { trustedTruststore.vauPublicKey }
                verify(exactly = 0) { trustedTruststore.checkValidity(any(), any()) }
            }
        }

    @Test
    fun `cached truststore is valid - reuses cached store`() =
        runTest {
            // First call to create the cached store
            coEvery { repository.withUntrusted<Boolean>(any()) } coAnswers {
                firstArg<suspend (UntrustedCertList, UntrustedOCSPList) -> Boolean>().invoke(
                    TestCertificates.Vau.CertList,
                    TestCertificates.OCSP.OCSPList
                )
            }
            every {
                trustedTruststoreProvider(
                    TestCertificates.OCSP.OCSPList,
                    TestCertificates.Vau.CertList,
                    TestCertificates.RCA3.X509Certificate,
                    12.hours,
                    any()
                )
            } returns trustedTruststore

            // First call to create the cached store
            truststore.withValidVauPublicKey {
                assertEquals(vauPublicKey, it)
            }

            // Second call should use the cached store
            truststore.withValidVauPublicKey {
                assertEquals(vauPublicKey, it)
            }

            coVerify(exactly = 1) { repository.withUntrusted(any()) }
            verify(exactly = 2) { trustedTruststore.vauPublicKey }
            verify(exactly = 1) { trustedTruststore.checkValidity(any(), any()) }
        }

    @Test
    fun `cached truststore is invalid - creates new store`() =
        runTest {
            // First call to create the cached store
            coEvery { repository.withUntrusted<Boolean>(any()) } coAnswers {
                firstArg<suspend (UntrustedCertList, UntrustedOCSPList) -> Boolean>().invoke(
                    TestCertificates.Vau.CertList,
                    TestCertificates.OCSP.OCSPList
                )
            }
            every {
                trustedTruststoreProvider(
                    TestCertificates.OCSP.OCSPList,
                    TestCertificates.Vau.CertList,
                    TestCertificates.RCA3.X509Certificate,
                    12.hours,
                    any()
                )
            } returns trustedTruststore

            // First call to create the cached store
            truststore.withValidVauPublicKey {
                assertEquals(vauPublicKey, it)
            }

            // Make the cached store invalid
            every { trustedTruststore.checkValidity(12.hours, any()) } throws IllegalStateException("Invalid store")

            // Second call should create a new store
            truststore.withValidVauPublicKey {
                assertEquals(vauPublicKey, it)
            }

            coVerify(exactly = 2) { repository.withUntrusted(any()) }
            coVerify(exactly = 1) { repository.invalidate() }
            verify(exactly = 2) { trustedTruststore.vauPublicKey }
            verify(exactly = 1) { trustedTruststore.checkValidity(any(), any()) }
        }
}
