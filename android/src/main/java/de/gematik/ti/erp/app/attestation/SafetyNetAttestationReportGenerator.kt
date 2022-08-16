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

package de.gematik.ti.erp.app.attestation

import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import java.security.cert.X509Certificate

private const val HOSTNAME = "attest.android.com"

class SafetyNetAttestationReportGenerator : AttestationReportGenerator {

    override suspend fun convertToReport(jwt: String, nonce: ByteArray): Attestation.Report {
        val jws = JwtConsumerBuilder()
            .setSkipAllValidators()
            .setDisableRequireSignature()
            .setSkipSignatureVerification()
            .build()

        val jwtContext = jws.process(jwt)
        val header = jwtContext.joseObjects[0]
        val cert = header.certificateChainHeaderValue[0]
        val secondPassJwtConsumer = JwtConsumerBuilder()
            .setVerificationKey(cert.publicKey)
            .build()

        secondPassJwtConsumer.processContext(jwtContext)

        val commonName = getCNFromCert(cert)
        if (commonName != HOSTNAME) {
            throw AttestationException(AttestationException.AttestationExceptionType.HOSTNAME_NOT_VERIFIED)
        }
        return SafetynetReport(jwtContext, nonce)
    }

    private fun getCNFromCert(cert: X509Certificate): String? {
        val x500name: X500Name = JcaX509CertificateHolder(cert).subject
        val cn: RDN = x500name.getRDNs(BCStyle.CN)[0]
        return IETFUtils.valueToString(cn.first.value)
    }
}
