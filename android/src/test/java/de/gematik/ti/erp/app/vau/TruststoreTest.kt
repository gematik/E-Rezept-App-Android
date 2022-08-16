/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.vau

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.repository.VauRepository
import de.gematik.ti.erp.app.vau.usecase.TrustedTruststore
import de.gematik.ti.erp.app.vau.usecase.TrustedTruststoreProvider
import de.gematik.ti.erp.app.vau.usecase.TruststoreConfig
import de.gematik.ti.erp.app.vau.usecase.TruststoreTimeSourceProvider
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import de.gematik.ti.erp.app.vau.usecase.findValidIdpChains
import de.gematik.ti.erp.app.vau.usecase.findValidOcspResponses
import de.gematik.ti.erp.app.vau.usecase.findValidVauChain
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.apache.commons.codec.binary.Base64
import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.cert.ocsp.OCSPResp
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.security.interfaces.ECPublicKey
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class TruststoreTest {
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

        every { config.maxOCSPResponseAge } returns Duration.ofHours(12)
        every { config.trustAnchor } returns TestCertificates.RCA3.X509Certificate
        every { timeSource() } returns ocspProducedAt + Duration.ofHours(2)
        every { trustedTruststore.vauPublicKey } returns vauPublicKey
        every { trustedTruststore.idpCertificates } returns
            listOf(TestCertificates.Idp1.X509Certificate, TestCertificates.Idp2.X509Certificate)
        every { trustedTruststore.caCertificates } returns listOf(TestCertificates.CA10.X509Certificate)
        every { trustedTruststore.ocspResponses } returns
            TestCertificates.OCSP.OCSPList.responses.map { it.responseObject as BasicOCSPResp }
        every { trustedTruststore.checkValidity(Duration.ofHours(12), ocspProducedAt) } coAnswers { }
        coEvery { repository.invalidate() } coAnswers { }

        truststore = TruststoreUseCase(
            config,
            repository,
            timeSource,
            trustedTruststoreProvider
        )
    }

    @Test
    fun `find valid cert chain for vau cert - returns one cert chain`() {
        val ocspResp = OCSPResp(Base64.decodeBase64(TestCertificates.OCSP3.Base64)).responseObject as BasicOCSPResp
        val certChain = listOf(
            TestCertificates.Vau.X509Certificate,
            TestCertificates.CA10.X509Certificate,
            TestCertificates.RCA3.X509Certificate
        )

        assertArrayEquals(
            certChain.toTypedArray(),
            findValidVauChain(listOf(certChain), listOf(ocspResp), TestCertificates.OCSP3.ProducedAt).toTypedArray()
        )
    }

    @Test
    fun `find valid cert chain for idp cert - returns one cert chain`() {
        val ocspResp = listOf(
            OCSPResp(Base64.decodeBase64(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp,
            OCSPResp(Base64.decodeBase64(TestCertificates.OCSP2.Base64)).responseObject as BasicOCSPResp
        )
        val certChains = listOf(
            listOf(
                TestCertificates.Idp1.X509Certificate,
                TestCertificates.CA10.X509Certificate,
                TestCertificates.RCA3.X509Certificate
            ),
            listOf(
                TestCertificates.Idp2.X509Certificate,
                TestCertificates.CA10.X509Certificate,
                TestCertificates.RCA3.X509Certificate
            )
        )

        val chains = findValidIdpChains(certChains, ocspResp, TestCertificates.OCSP1.ProducedAt)

        assertArrayEquals(
            certChains[0].toTypedArray(),
            chains[0].toTypedArray()
        )
    }

    @Test
    fun `find valid ocsp responses - returns two ocsp responses`() {
        val ocspResps = listOf(
            OCSPResp(Base64.decodeBase64(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp,
            OCSPResp(Base64.decodeBase64(TestCertificates.OCSP2.Base64)).responseObject as BasicOCSPResp
        )
        val certChain = listOf(TestCertificates.CA10.X509Certificate, TestCertificates.RCA3.X509Certificate)

        assertArrayEquals(
            ocspResps.toTypedArray(),
            findValidOcspResponses(
                ocspResps,
                listOf(certChain),
                Duration.ofHours(12),
                TestCertificates.OCSP2.ProducedAt
            ).toTypedArray()
        )
    }

    @Test
    fun `find valid ocsp responses with wrong ca chain - returns no responses`() {
        val ocspResps = listOf(
            OCSPResp(Base64.decodeBase64(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp,
            OCSPResp(Base64.decodeBase64(TestCertificates.OCSP2.Base64)).responseObject as BasicOCSPResp
        )
        val certChain = listOf(TestCertificates.CA11.X509Certificate, TestCertificates.RCA3.X509Certificate)

        assertArrayEquals(
            emptyArray(),
            findValidOcspResponses(
                ocspResps,
                listOf(certChain),
                Duration.ofHours(12),
                TestCertificates.OCSP2.ProducedAt
            ).toTypedArray()
        )
    }

    @Test
    fun `create trusted truststore`() {
        val truststore = TrustedTruststore.create(
            TestCertificates.OCSP.OCSPList,
            TestCertificates.Vau.CertList,
            TestCertificates.RCA3.X509Certificate,
            Duration.ofHours(12),
            TestCertificates.OCSP2.ProducedAt
        )

        assertEquals(vauPublicKey, truststore.vauPublicKey)
        truststore.checkValidity(Duration.ofHours(12), TestCertificates.OCSP2.ProducedAt)
    }

    @Test
    fun `create trusted truststore with outdated ocsp responses`() {
        assertTrue(
            try {
                TrustedTruststore.create(
                    TestCertificates.OCSP.OCSPList,
                    TestCertificates.Vau.CertList,
                    TestCertificates.RCA3.X509Certificate,
                    Duration.ofHours(12),
                    TestCertificates.OCSP2.ProducedAt + Duration.ofDays(1)
                )

                false
            } catch (_: Exception) {
                true
            }
        )
    }

    // use case tests

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
                    Duration.ofHours(12),
                    any()
                )
            } returns trustedTruststore

            truststore.withValidVauPublicKey {
                assertEquals(vauPublicKey, it)
            }

            coVerify(exactly = 1) { repository.withUntrusted(any()) }
            coVerify(exactly = 0) { repository.invalidate() }
            verify(exactly = 1) { trustedTruststore.vauPublicKey }
            coVerify(exactly = 0) { trustedTruststore.checkValidity(any(), any()) }
        }

    @Test
    fun `new instance of truststore contains no cached store - fetches and creates store with invalid ocsp from repository`() =
        runTest {
            coEvery {
                repository.withUntrusted<Boolean>(any())
            } coAnswers {
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
                    Duration.ofHours(12),
                    any()
                )
            } answers {
                error("invalid ocsp")
            } andThenAnswer {
                trustedTruststore
            }

            truststore.withValidVauPublicKey {
                assertEquals(vauPublicKey, it)
            }

            coVerifyOrder {
                repository.withUntrusted<Boolean>(any())
                repository.invalidate()
                repository.withUntrusted<Boolean>(any())
                trustedTruststore.vauPublicKey
            }
            coVerify(exactly = 2) { repository.withUntrusted(any()) }
            coVerify(exactly = 1) { repository.invalidate() }
            verify(exactly = 1) { trustedTruststore.vauPublicKey }
            coVerify(exactly = 0) { trustedTruststore.checkValidity(any(), any()) }
        }

    @Test
    fun `truststore contains cached store - cached store is invalid - fetches and creates store with invalid ocsp from repository`() =
        runTest {
            coEvery {
                repository.withUntrusted<Boolean>(any())
            } coAnswers {
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
                    Duration.ofHours(12),
                    any()
                )
            } answers {
                trustedTruststore
            }

            every {
                trustedTruststore.checkValidity(
                    Duration.ofHours(12),
                    ocspProducedAt
                )
            } throws IllegalStateException()

            truststore.withValidVauPublicKey {
                assertEquals(vauPublicKey, it)
            }

            // cached store should now exist

            truststore.withValidVauPublicKey {
                assertEquals(vauPublicKey, it)
            }

            coVerifyOrder {
                repository.withUntrusted<Boolean>(any())
                trustedTruststore.vauPublicKey

                trustedTruststore.checkValidity(any(), any())
                repository.invalidate()
                repository.withUntrusted<Boolean>(any())
                trustedTruststore.vauPublicKey
            }
            coVerify(exactly = 2) { repository.withUntrusted(any()) }
            coVerify(exactly = 1) { repository.invalidate() }
            verify(exactly = 2) { trustedTruststore.vauPublicKey }
            coVerify(exactly = 1) { trustedTruststore.checkValidity(any(), any()) }
        }

    @Test(expected = Exception::class)
    fun `truststore creation finally fails`() =
        runTest {
            coEvery {
                repository.withUntrusted<Boolean>(any())
            } coAnswers {
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
                    Duration.ofHours(12),
                    ocspProducedAt
                )
            } throws IllegalStateException()

            try {
                truststore.withValidVauPublicKey {
                    assertFalse(true) // should never be called
                }
            } finally {
                coVerifyOrder {
                    repository.withUntrusted<Boolean>(any())
                    repository.invalidate()
                    repository.withUntrusted<Boolean>(any())
                    repository.invalidate()
                }
                coVerify(exactly = 2) { repository.withUntrusted(any()) }
                coVerify(exactly = 2) { repository.invalidate() }
                verify(exactly = 0) { trustedTruststore.vauPublicKey }
                coVerify(exactly = 0) { trustedTruststore.checkValidity(any(), any()) }
            }
        }

    @Test(expected = Exception::class)
    fun `truststore creation succeeds - block throws exception`() =
        runTest {
            coEvery {
                repository.withUntrusted<Boolean>(any())
            } coAnswers {
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
                    Duration.ofHours(12),
                    any()
                )
            } answers {
                trustedTruststore
            }

            try {
                truststore.withValidVauPublicKey {
                    assertEquals(vauPublicKey, it)
                    error("")
                }
            } finally {
                coVerifyOrder {
                    repository.withUntrusted<Boolean>(any())
                    trustedTruststore.vauPublicKey
                    repository.invalidate()
                }
                coVerify(exactly = 1) { repository.withUntrusted(any()) }
                coVerify(exactly = 1) { repository.invalidate() }
                verify(exactly = 1) { trustedTruststore.vauPublicKey }
                coVerify(exactly = 0) { trustedTruststore.checkValidity(any(), any()) }
            }
        }

    @Test
    fun `truststore creation succeeds - idp certificate found`() =
        runTest {
            coEvery {
                repository.withUntrusted<Boolean>(any())
            } coAnswers {
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
                    Duration.ofHours(12),
                    any()
                )
            } answers {
                trustedTruststore
            }

            truststore.checkIdpCertificate(TestCertificates.Idp1.X509Certificate)

            coVerifyOrder {
                repository.withUntrusted<Boolean>(any())
                trustedTruststore.idpCertificates
            }
            coVerify(exactly = 1) { repository.withUntrusted(any()) }
            coVerify(exactly = 0) { repository.invalidate() }
            verify(exactly = 0) { trustedTruststore.caCertificates }
            verify(exactly = 0) { trustedTruststore.vauPublicKey }
            coVerify(exactly = 0) { trustedTruststore.checkValidity(any(), any()) }
        }

    @Test(expected = IllegalArgumentException::class)
    fun `truststore creation succeeds - idp certificate not found`() =
        runTest {
            coEvery {
                repository.withUntrusted<Boolean>(any())
            } coAnswers {
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
                    Duration.ofHours(12),
                    any()
                )
            } answers {
                trustedTruststore
            }

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
                coVerify(exactly = 0) { trustedTruststore.checkValidity(any(), any()) }
            }
        }

    @Test(expected = Exception::class)
    fun `truststore creation succeeds - idp certificate not found - invalidate`() =
        runTest {
            coEvery {
                repository.withUntrusted<Boolean>(any())
            } coAnswers {
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
                    Duration.ofHours(12),
                    any()
                )
            } answers {
                trustedTruststore
            }

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
                coVerify(exactly = 0) { trustedTruststore.checkValidity(any(), any()) }
            }
        }
}
