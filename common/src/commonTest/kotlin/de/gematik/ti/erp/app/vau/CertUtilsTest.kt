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

package de.gematik.ti.erp.app.vau

import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.cert.ocsp.OCSPResp
import org.bouncycastle.util.encoders.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.test.Test

class CertUtilsTest {
    @Test
    fun `certificate contains oid`() {
        assertTrue(TestCertificates.Vau.X509Certificate.containsIdentifierOid(TestCertificates.Vau.OID))
        assertFalse(TestCertificates.Vau.X509Certificate.containsIdentifierOid(TestCertificates.Idp3.OID))

        assertTrue(TestCertificates.Idp1.X509Certificate.containsIdentifierOid(TestCertificates.Idp1.OID))
        assertTrue(TestCertificates.Idp2.X509Certificate.containsIdentifierOid(TestCertificates.Idp2.OID))

        assertTrue(TestCertificates.Idp3.X509Certificate.containsIdentifierOid(TestCertificates.Idp3.OID))
        assertFalse(TestCertificates.Idp3.X509Certificate.containsIdentifierOid(TestCertificates.Vau.OID))
    }

    @Test
    fun `validate cert chain by signature and valid timestamp - should return one chain`() {
        val certChain = listOf(
            TestCertificates.Vau.X509Certificate,
            TestCertificates.CA10.X509Certificate,
            TestCertificates.RCA3.X509Certificate
        )

        assertArrayEquals(
            certChain.toTypedArray(),
            listOf(certChain).filterBySignature(TestCertificates.Vau.ValidTimestamp).first()
                .toTypedArray()
        )
    }

    @Test
    fun `validate cert chain by signature and expired timestamp - should return no chain`() {
        val certChain = listOf(
            TestCertificates.Vau.X509Certificate,
            TestCertificates.CA10.X509Certificate,
            TestCertificates.RCA3.X509Certificate
        )

        assertTrue(
            listOf(certChain).filterBySignature(TestCertificates.Vau.ExpiredTimestamp).isEmpty()
        )
    }

    @Test
    fun `validate invalid cert chain by signature and timestamp - should return no chain`() {
        val certChain = listOf(
            TestCertificates.Vau.X509Certificate,
            TestCertificates.CA11.X509Certificate,
            TestCertificates.RCA3.X509Certificate
        )

        assertTrue(
            listOf(certChain).filterBySignature(TestCertificates.Vau.ExpiredTimestamp).isEmpty()
        )
    }

    @Test
    fun `validate too short but valid cert chain by signature and timestamp - should return no chain`() {
        val certChain =
            listOf(TestCertificates.Vau.X509Certificate, TestCertificates.CA10.X509Certificate)

        assertTrue(
            listOf(certChain).filterBySignature(TestCertificates.Vau.ExpiredTimestamp).isEmpty()
        )
    }

    @Test
    fun `filter chains by oid and ocsp response - return one chain`() {
        val certChain = listOf(
            listOf(
                TestCertificates.Vau.X509Certificate,
                TestCertificates.CA10.X509Certificate,
                TestCertificates.RCA3.X509Certificate
            )
        )
        val ocspResp =
            OCSPResp(Base64.decode(TestCertificates.OCSP3.Base64)).responseObject as BasicOCSPResp

        assertArrayEquals(
            certChain.toTypedArray(),
            certChain.filterByOIDAndOCSPResponse(
                TestCertificates.Vau.OID,
                listOf(ocspResp),
                TestCertificates.OCSP3.ProducedAt
            ).toTypedArray()
        )
    }

    @Test
    fun `filter chains by oid and no ocsp response - return no chain`() {
        val certChain = listOf(
            listOf(
                TestCertificates.Vau.X509Certificate,
                TestCertificates.CA10.X509Certificate,
                TestCertificates.RCA3.X509Certificate
            )
        )

        assertArrayEquals(
            emptyArray(),
            certChain.filterByOIDAndOCSPResponse(
                TestCertificates.Vau.OID,
                listOf(),
                TestCertificates.OCSP3.ProducedAt
            ).toTypedArray()
        )
    }

    @Test
    fun `filter chains by oid and multiple ocsp responses - return one chain`() {
        val ocspRespVau =
            OCSPResp(Base64.decode(TestCertificates.OCSP3.Base64)).responseObject as BasicOCSPResp
        val ocspRespIdp =
            OCSPResp(Base64.decode(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp

        val certChain = listOf(
            listOf(
                TestCertificates.Vau.X509Certificate,
                TestCertificates.CA10.X509Certificate,
                TestCertificates.RCA3.X509Certificate
            ),
            listOf(
                TestCertificates.Vau.X509Certificate,
                TestCertificates.CA11.X509Certificate,
                TestCertificates.RCA3.X509Certificate
            ),
            listOf(
                TestCertificates.Idp1.X509Certificate,
                TestCertificates.CA10.X509Certificate,
                TestCertificates.RCA3.X509Certificate
            )
        )

        // vau
        assertArrayEquals(
            arrayOf(
                TestCertificates.Vau.X509Certificate,
                TestCertificates.CA10.X509Certificate,
                TestCertificates.RCA3.X509Certificate
            ),
            certChain.filterByOIDAndOCSPResponse(
                TestCertificates.Vau.OID,
                listOf(ocspRespVau, ocspRespIdp),
                TestCertificates.OCSP3.ProducedAt
            ).first().toTypedArray()
        )
    }
}
