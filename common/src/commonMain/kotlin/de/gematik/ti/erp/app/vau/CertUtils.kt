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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.vau

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder
import org.bouncycastle.operator.bc.BcECContentVerifierProviderBuilder
import java.util.Date

/**
 * Refer to gemSpec_OID.
 */
fun X509CertificateHolder.containsIdentifierOid(oid: ByteArray) =
    this.getExtension(ASN1ObjectIdentifier("1.3.36.8.3.3")).encoded.contains(oid)

fun List<List<X509CertificateHolder>>.filterByOIDAndOCSPResponse(
    oid: ByteArray,
    validOcspResponses: List<BasicOCSPResp>,
    timestamp: Instant
): List<List<X509CertificateHolder>> =
    filter { it.first().containsIdentifierOid(oid) }
        .filter { chain ->
            validOcspResponses.find { validOcspResponse ->
                val producedAt = validOcspResponse.producedAt.toInstant().toKotlinInstant()

                validOcspResponse.findValidCert(chain.first().serialNumber)?.let {
                    val thisUpdate = it.thisUpdate.toInstant().toKotlinInstant()

                    (producedAt <= timestamp) && (thisUpdate <= timestamp) &&
                        it.matchesIssuer(chain[1])
                    // TODO not present in test responses
                    // && it.matchesHashOfCertificate(chain[0])
                } ?: false
            } != null
        }

fun List<List<X509CertificateHolder>>.filterBySignature(timestamp: Instant) =
    filter { it.size >= 3 }
        .mapNotNull { chain ->
            try {
                chain.reduceRight { it, prev ->
                    it.checkSignatureWith(prev)
                    it.checkValidity(timestamp)
                    it
                }

                chain
            } catch (e: Exception) {
                null
            }
        }

fun List<X509CertificateHolder>.validateSubjectDN(cnPrefix: String): List<X509CertificateHolder> =
    this.filter {
        it.subjectDNContainsCNPrefixWithNumber(cnPrefix)
    }

fun X509CertificateHolder.checkSignatureWith(signatureCertificate: X509CertificateHolder) {
    val verifier =
        BcECContentVerifierProviderBuilder(DefaultDigestAlgorithmIdentifierFinder())
            .build(signatureCertificate)

    require(this.isSignatureValid(verifier))
}

/**
 * Validates the common name form the distinguished name.
 * Throws an exception if the common name is not present or the pattern `CN=GEM.KOMP-CA + number` doesn't match.
 */
internal fun X509CertificateHolder.subjectDNContainsCNPrefixWithNumber(cnPrefix: String): Boolean =
    this.subject.toString().split(",").find { it.startsWith("CN") }?.let {
        """CN=$cnPrefix\d+.*""".toRegex().matches(it)
    } ?: false

/**
 * Checks if the [this] certificate is valid at the provided time.
 * Throws an exception if the check fails.
 */
fun X509CertificateHolder.checkValidity(timestamp: Instant) {
    require(isValidOn(Date.from(timestamp.toJavaInstant())))
}

fun X509CertificateHolder.extractECPublicKey(): ECPublicKey {
    return KeyFactorySpi.EC().generatePublic(subjectPublicKeyInfo)!! as ECPublicKey
}
