/*
 * Copyright (c) 2021 gematik GmbH
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

import org.apache.commons.codec.binary.Base64
import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.cert.ocsp.OCSPResp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class OCSPUtilsTest {
    @Test
    fun `valid ocsp response cert is validated against its ca cert`() {
        val ocspResp = OCSPResp(Base64.decodeBase64(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp
        ocspResp.checkSignatureWith(TestCertificates.OCSP1.SignerCert.X509Certificate)
    }

    @Test(expected = Exception::class)
    fun `valid ocsp response cert is validated against wrong ca cert - should throw exception`() {
        val ocspResp = OCSPResp(Base64.decodeBase64(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp
        ocspResp.checkSignatureWith(TestCertificates.CA10.X509Certificate)
    }

    @Test
    fun `valid ocsp response cert is valid within 12 hours`() {
        val ocspResp = OCSPResp(Base64.decodeBase64(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp

        ocspResp.checkValidity(Duration.ofHours(12), TestCertificates.OCSP1.ProducedAt.plus(Duration.ofHours(0)))
        ocspResp.checkValidity(Duration.ofHours(12), TestCertificates.OCSP1.ProducedAt.plus(Duration.ofHours(5)))
        ocspResp.checkValidity(Duration.ofHours(12), TestCertificates.OCSP1.ProducedAt.plus(Duration.ofHours(12)))
    }

    @Test(expected = Exception::class)
    fun `valid ocsp response cert is invalid over 12 hours - throws exception`() {
        val ocspResp = OCSPResp(Base64.decodeBase64(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp

        ocspResp.checkValidity(Duration.ofHours(12), TestCertificates.OCSP1.ProducedAt.plus(Duration.ofHours(13)))
    }

    @Test(expected = Exception::class)
    fun `valid ocsp response cert is invalid if current time is in the past - throws exception`() {
        val ocspResp = OCSPResp(Base64.decodeBase64(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp

        ocspResp.checkValidity(Duration.ofHours(12), TestCertificates.OCSP1.ProducedAt.minus(Duration.ofHours(1)))
    }

    @Test
    fun `valid single response matches with its issuer certificate - returns true`() {
        val ocspResp = OCSPResp(Base64.decodeBase64(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp

        assertTrue(ocspResp.responses.first().matchesIssuer(TestCertificates.CA10.X509Certificate))
    }

    @Test
    fun `valid single response doesn't match with wrong issuer certificate - returns false`() {
        val ocspResp = OCSPResp(Base64.decodeBase64(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp

        assertFalse(ocspResp.responses.first().matchesIssuer(TestCertificates.CA11.X509Certificate))
    }
}
