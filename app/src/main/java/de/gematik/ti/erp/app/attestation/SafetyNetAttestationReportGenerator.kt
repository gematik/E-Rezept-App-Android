package de.gematik.ti.erp.app.attestation

import javax.inject.Inject
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import java.security.cert.X509Certificate

private const val HOSTNAME = "attest.android.com"

class SafetyNetAttestationReportGenerator @Inject constructor() : AttestationReportGenerator {

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
