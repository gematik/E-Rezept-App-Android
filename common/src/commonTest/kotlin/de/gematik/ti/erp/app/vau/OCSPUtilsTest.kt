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

package de.gematik.ti.erp.app.vau

import de.gematik.ti.erp.app.Requirement
import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.cert.ocsp.OCSPResp
import org.bouncycastle.util.encoders.Base64
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.hours

class OCSPUtilsTest {
    @Test
    fun `valid ocsp response cert is validated against its ca cert`() {
        val ocspResp = OCSPResp(Base64.decode(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp
        ocspResp.checkSignatureWith(TestCertificates.OCSP1.SignerCert.X509Certificate)
    }

    @Test(expected = Exception::class)
    fun `valid ocsp response cert is validated against wrong ca cert - should throw exception`() {
        val ocspResp = OCSPResp(Base64.decode(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp
        ocspResp.checkSignatureWith(TestCertificates.CA10.X509Certificate)
    }

    @Requirement(
        "A_21218#7",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Fail when there is no matching certificate.",
        codeLines = 6
    )
    @Test
    fun `valid ocsp response cert is valid within 12 hours`() {
        val ocspResp = OCSPResp(Base64.decode(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp

        ocspResp.checkValidity(12.hours, TestCertificates.OCSP1.ProducedAt.plus(0.hours))
        ocspResp.checkValidity(12.hours, TestCertificates.OCSP1.ProducedAt.plus(5.hours))
        ocspResp.checkValidity(12.hours, TestCertificates.OCSP1.ProducedAt.plus(12.hours))
    }

    @Test(expected = Exception::class)
    fun `valid ocsp response cert is invalid over 12 hours - throws exception`() {
        val ocspResp = OCSPResp(Base64.decode(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp

        ocspResp.checkValidity(12.hours, TestCertificates.OCSP1.ProducedAt.plus(13.hours))
    }

    @Requirement(
        "A_21218#4",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Only if OCSP response greater than 12h available, we must request new ones."
    )
    @Test(expected = Exception::class)
    fun `valid ocsp response cert is invalid if current time is in the past - throws exception`() {
        val ocspResp = OCSPResp(Base64.decode(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp

        ocspResp.checkValidity(12.hours, TestCertificates.OCSP1.ProducedAt.minus(1.hours))
    }

    @Test
    fun `valid single response matches with its issuer certificate - returns true`() {
        val ocspResp = OCSPResp(Base64.decode(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp

        assertTrue(ocspResp.responses.first().matchesIssuer(TestCertificates.CA10.X509Certificate))
    }

    @Test
    fun `valid single response doesn't match with wrong issuer certificate - returns false`() {
        val ocspResp = OCSPResp(Base64.decode(TestCertificates.OCSP1.Base64)).responseObject as BasicOCSPResp

        assertFalse(ocspResp.responses.first().matchesIssuer(TestCertificates.CA11.X509Certificate))
    }
}
