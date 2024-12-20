/*
 * Copyright 2024, gematik GmbH
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

import de.gematik.ti.erp.app.Requirement
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.isismtt.ocsp.CertHash
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.cert.ocsp.SingleResp
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider
import org.bouncycastle.operator.bc.BcECContentVerifierProviderBuilder
import java.math.BigInteger
import kotlin.time.Duration

private val certHashOid = ASN1ObjectIdentifier("1.3.36.8.3.13")

/**
 * Returns true if the required `CertHash` extension within the [SingleResp] matches the
 * calculated hash of [cert] (i.e. VAU or IDP certificate).
 */
fun SingleResp.matchesHashOfCertificate(cert: X509CertificateHolder) =
    try {
        val certHash = CertHash.getInstance(
            requireNotNull(this.getExtension(certHashOid)) { "CertHash extension required" }
        )

        val digest = BcDigestCalculatorProvider().get(certHash.hashAlgorithm).apply {
            outputStream.apply {
                write(cert.toASN1Structure().getEncoded("DER"))
                close()
            }
        }.digest

        certHash.certificateHash.contentEquals(digest)
    } catch (e: Exception) {
        false
    }

fun BasicOCSPResp.findValidCert(serialNumber: BigInteger): SingleResp? =
    this.responses
        .find { it.certID.serialNumber == serialNumber }
        ?.takeIf { it.certStatus == null }

/**
 * This checks whether the contained certificate id is the one of the issuer certificate.
 */
fun SingleResp.matchesIssuer(issuerCert: X509CertificateHolder) =
    this.certID?.matchesIssuer(issuerCert, BcDigestCalculatorProvider()) ?: false

/**
 * Checks the signature over the field 'tbsResponseData' with [signatureCertificate].
 * Throws an exception if the check fails.
 */
fun BasicOCSPResp.checkSignatureWith(signatureCertificate: X509CertificateHolder) {
    val verifier =
        BcECContentVerifierProviderBuilder(DefaultDigestAlgorithmIdentifierFinder())
            .build(signatureCertificate)

    require(this.isSignatureValid(verifier)) {
        "OCSP response signature couldn't be validated against its signer certificate"
    }
}

@Requirement(
    "A_21218#3",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Checks if the valid OCSP response is less than 12 hours old."
)
/**
 * Checks if the field 'producedAt' plus [maxAge] is after [timestamp] and 'producedAt' is before [timestamp].
 * Throws an exception if the check fails.
 */
@Requirement(
    "A_21218#3",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Checks if the valid OCSP response is less than 12 hours old."
)
fun BasicOCSPResp.checkValidity(maxAge: Duration, timestamp: Instant) {
    requireNotNull(
        this.producedAt?.toInstant()?.toKotlinInstant()?.takeIf { it + maxAge >= timestamp && timestamp >= it }
    ) { "OCSP response expired" }
}
