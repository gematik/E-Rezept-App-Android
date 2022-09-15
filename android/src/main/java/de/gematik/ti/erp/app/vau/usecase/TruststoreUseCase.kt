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

package de.gematik.ti.erp.app.vau.usecase

import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.checkSignatureWith
import de.gematik.ti.erp.app.vau.checkValidity
import de.gematik.ti.erp.app.vau.filterByOIDAndOCSPResponse
import de.gematik.ti.erp.app.vau.filterBySignature
import de.gematik.ti.erp.app.vau.repository.VauRepository
import de.gematik.ti.erp.app.vau.validateSubjectDN
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi
import timber.log.Timber
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private const val RCA_PREFIX = "GEM.RCA"
private const val CA_PREFIX = "GEM.KOMP-CA"

/**
 * See gemSpec_OID `oid_erp-vau`.
 */
private val vauOid = byteArrayOf(6, 8, 42, -126, 20, 0, 76, 4, -126, 2) // oid = 1.2.276.0.76.4.258

/**
 * See gemSpec_OID `oid_idpd`.
 */
private val idpOid = byteArrayOf(6, 8, 42, -126, 20, 0, 76, 4, -126, 4) // oid = 1.2.276.0.76.4.260

class TruststoreTimeSourceProvider @Inject constructor() {
    fun now(): Instant =
        Instant.now()
}

class TrustedTruststoreProvider @Inject constructor() {
    fun create(
        untrustedOCSPList: UntrustedOCSPList,
        untrustedCertList: UntrustedCertList,
        trustAnchor: X509CertificateHolder,
        ocspResponseMaxAge: Duration,
        timestamp: Instant
    ): TrustedTruststore =
        TrustedTruststore.create(
            untrustedOCSPList,
            untrustedCertList,
            trustAnchor,
            ocspResponseMaxAge,
            timestamp
        )
}

@Singleton
class TruststoreUseCase @Inject constructor(
    private val config: TruststoreConfig,
    private val repository: VauRepository,
    private val timeSourceProvider: TruststoreTimeSourceProvider,
    private val trustedTruststoreProvider: TrustedTruststoreProvider,
) {
    private val lock = Mutex()
    private var cachedTruststore: TrustedTruststore? = null

    /**
     * Checks this [idpCertificate] against the truststore.
     *
     * Throws an exception if the certificate couldn't be validated against this store
     * and if [invalidateStoreOnFailure] is true, this store will be invalidated.
     */
    suspend fun checkIdpCertificate(
        idpCertificate: X509CertificateHolder,
        invalidateStoreOnFailure: Boolean = false
    ) = lock.withLock {
        val timestamp = timeSourceProvider.now()

        val exception = withLoadedStore(timestamp) { store ->
            try {
                requireNotNull(store.idpCertificates.find { it == idpCertificate }) { "IDP certificate could not be validated" }

                null
            } catch (e: Exception) {
                if (invalidateStoreOnFailure) {
                    throw e
                } else {
                    e
                }
            }
        }

        if (exception != null) {
            throw exception
        }
    }

    suspend fun <R> withValidVauPublicKey(block: (vauPubKey: ECPublicKey) -> R): R = lock.withLock {
        val timestamp = timeSourceProvider.now()

        withLoadedStore(timestamp) {
            block(it.vauPublicKey)
        }
    }

    /**
     * Caches a validated store and returns it. If the temporal validation fails, a new store is loaded from the repository.
     * This takes care of loading the store from the backend or database.
     */
    private suspend fun <R> withLoadedStore(timestamp: Instant, block: (TrustedTruststore) -> R): R {
        try {
            val store = cachedTruststore?.let {
                Timber.d("Use cached truststore...")

                try {
                    it.checkValidity(config.maxOCSPResponseAge, timestamp)

                    it
                } catch (e: Exception) {
                    // ocsp responses or certificates are outdated

                    // delete all certs & responses and recreate the store
                    repository.invalidate()

                    createTrustedTruststore(timestamp)
                }
            } ?: run {
                Timber.d("Create truststore from repository...")

                try {
                    createTrustedTruststore(timestamp)
                } catch (e: Exception) {
                    // TODO differentiate kind of exception (ocsp vs cert)
                    // retry one more time; the failure might originate from an outdated ocsp response

                    // delete all locally cached certs & responses
                    repository.invalidate()

                    createTrustedTruststore(timestamp)
                }
            }

            cachedTruststore = store

            return block(store)
        } catch (e: Exception) {
            cachedTruststore = null
            repository.invalidate()

            throw e
        }
    }

    private suspend fun createTrustedTruststore(timestamp: Instant): TrustedTruststore {
        Timber.d("Load truststore from repository...")

        return repository.withUntrusted { untrustedCertList, untrustedOCSPList ->
            trustedTruststoreProvider.create(
                untrustedOCSPList,
                untrustedCertList,
                config.trustAnchor,
                config.maxOCSPResponseAge,
                timestamp
            )
        }
    }
}

/**
 * Wrapper for X.509 certificates of type [X509Certificate].
 */
suspend fun TruststoreUseCase.checkIdpCertificate(
    idpCertificate: X509Certificate,
    invalidateStoreOnFailure: Boolean = false
) {
    checkIdpCertificate(X509CertificateHolder(idpCertificate.encoded), invalidateStoreOnFailure)
}

// This creates a truststore described as in `gemSpec_Krypt A_21218`.
class TrustedTruststore private constructor(
    val vauCertificate: X509CertificateHolder,
    val idpCertificates: List<X509CertificateHolder>,
    val caCertificates: List<X509CertificateHolder>,
    val ocspResponses: List<BasicOCSPResp>,

    val vauPublicKey: ECPublicKey
) {
    fun checkValidity(ocspResponseMaxAge: Duration, timestamp: Instant) {
        require(ocspResponses.isNotEmpty()) { "No OCSp responses. This should never happen" }
        ocspResponses.forEach { resp ->
            resp.checkValidity(ocspResponseMaxAge, timestamp)
        }

        vauCertificate.checkValidity(timestamp)

        require(caCertificates.isNotEmpty()) { "No CA certificates. This should never happen" }
        caCertificates.forEach {
            it.checkValidity(timestamp)
        }
    }

    companion object {
        fun create(
            untrustedOCSPList: UntrustedOCSPList,
            untrustedCertList: UntrustedCertList,
            trustAnchor: X509CertificateHolder,
            ocspResponseMaxAge: Duration,
            timestamp: Instant
        ): TrustedTruststore {
            // FIXME
            require(untrustedCertList.addRoots.isEmpty()) { "Additional roots currently unsupported!" }

            // validate the common name according to the specified pattern in `gemSpec_Krypt Tab_KRYPT_ERP_FdV_Truststore_aktualisieren`
            // val filteredAddRoots = untrustedCertList.addRoots.validateSubjectDN(RCA_PREFIX).distinct()
            val filteredCaCerts = untrustedCertList.caCerts.validateSubjectDN(CA_PREFIX).distinct()

            val validOcspResponses = findValidOcspResponses(
                untrustedOCSPList.responses.map { it.responseObject as BasicOCSPResp },
                filteredCaCerts.map { ca -> listOf(ca, trustAnchor) }, // FIXME add addRoots
                ocspResponseMaxAge,
                timestamp
            )

            // FIXME add addRoots
            val eeChains = untrustedCertList.eeCerts.distinct()
                .flatMap { ee -> filteredCaCerts.map { ca -> listOf(ee, ca, trustAnchor) } }

            require(eeChains.isNotEmpty())
            eeChains.forEach {
                require(it.size >= 3)
            }

            val validVauCertChain = findValidVauChain(eeChains, validOcspResponses, timestamp)
            val validIdpCertChains = findValidIdpChains(eeChains, validOcspResponses, timestamp)

            val validVauCert = validVauCertChain[0]
            val validIdpCerts = validIdpCertChains.map { it.first() }
            val validCaCerts = listOf(validVauCertChain[1]) + validIdpCertChains.map { it[1] }

            return TrustedTruststore(
                vauCertificate = validVauCert,
                idpCertificates = validIdpCerts,
                caCertificates = validCaCerts,
                vauPublicKey = KeyFactorySpi.EC()
                    .generatePublic(validVauCert.subjectPublicKeyInfo)!! as ECPublicKey,
                ocspResponses = validOcspResponses
            )
        }
    }
}

/**
 * Returns a list of validated OCSP responses by signature and the provided [timestamp] & [maxAge].
 *
 * A OCSP response is valid, if the contained certificate is verified with at least on of the
 * certificate chains [caCertChains] and is within the provided period of time.
 */
fun findValidOcspResponses(
    ocspResponses: List<BasicOCSPResp>,
    caCertChains: List<List<X509CertificateHolder>>,
    maxAge: Duration,
    timestamp: Instant
): List<BasicOCSPResp> =
    ocspResponses.mapNotNull { ocspResponse ->
        try {
            // extract signer cert
            val innerOcspCert =
                requireNotNull(ocspResponse.certs?.firstOrNull()) { "No signer certificates within the ocsp response" }

            // find at least one valid cert chain
            require(
                caCertChains
                    .map { listOf(innerOcspCert) + it }
                    .filterBySignature(timestamp)
                    .isNotEmpty()
            ) { "Couldn't validate signer cert of ocsp response" }

            // check signature
            ocspResponse.checkSignatureWith(innerOcspCert)
            // check validity
            ocspResponse.checkValidity(maxAge, timestamp)

            // return valid response
            ocspResponse
        } catch (e: Exception) {
            Timber.d(e)
            null
        }
    }

/**
 * Returns the first valid VAU certificate.
 * While only one VAU certificate can exist at any time, this won't fail if multiple chains are valid.
 * Before we throw an exception with this non-critical behavior, just pick the first valid as the true one.
 */
fun findValidVauChain(
    chains: List<List<X509CertificateHolder>>,
    validOcspResponses: List<BasicOCSPResp>,
    timestamp: Instant
): List<X509CertificateHolder> =
    chains
        .filterByOIDAndOCSPResponse(vauOid, validOcspResponses, timestamp)
        .filterBySignature(timestamp)
        .also {
            require(it.isNotEmpty()) { "No valid certificate chain with VAU end entity found" }
        }
        .first()

/**
 * Returns all valid IDP certificate chains.
 */
fun findValidIdpChains(
    chains: List<List<X509CertificateHolder>>,
    validOcspResponses: List<BasicOCSPResp>,
    timestamp: Instant
): List<List<X509CertificateHolder>> =
    chains
        .filterByOIDAndOCSPResponse(idpOid, validOcspResponses, timestamp)
        .filterBySignature(timestamp)
        .also {
            require(it.size >= 2) { "No valid certificate chains with IDP end entity found" }
        }
