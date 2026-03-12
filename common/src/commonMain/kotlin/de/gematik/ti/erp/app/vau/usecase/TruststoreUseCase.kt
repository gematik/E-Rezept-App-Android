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

@file:Suppress("MagicNumber", "UnusedPrivateProperty", "UnusedPrivateMember")

package de.gematik.ti.erp.app.vau.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.canBeValidatedBy
import de.gematik.ti.erp.app.vau.checkSignatureWith
import de.gematik.ti.erp.app.vau.checkValidity
import de.gematik.ti.erp.app.vau.filterByOIDAndOCSPResponse
import de.gematik.ti.erp.app.vau.filterBySignature
import de.gematik.ti.erp.app.vau.getOscpResponsStatus
import de.gematik.ti.erp.app.vau.repository.VauRepository
import de.gematik.ti.erp.app.vau.validateSubjectDN
import io.github.aakira.napier.Napier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.ocsp.BasicOCSPResp
import org.bouncycastle.cert.ocsp.CertificateID
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import java.security.Security
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.Instant as KInstant
import kotlin.time.Duration as KDuration

/**
 * Prefix for Root Certificate Authority certificates.
 * Used for validating subject Distinguished Names (DN).
 */
private const val RCA_PREFIX = "GEM.RCA"

/**
 * Prefix for Component Certificate Authority certificates.
 * Used for validating subject Distinguished Names (DN).
 */
private const val CA_PREFIX = "GEM.KOMP-CA"

/**
 * Object Identifier (OID) for VAU (Verschlüsselung von Authentifizierung) certificates.
 * See gemSpec_OID `oid_erp-vau`.
 */
private val vauOid = byteArrayOf(6, 8, 42, -126, 20, 0, 76, 4, -126, 2) // oid = 1.2.276.0.76.4.258

/**
 * Object Identifier (OID) for IDP (Identity Provider) certificates.
 * See gemSpec_OID `oid_idpd`.
 */
private val idpOid = byteArrayOf(6, 8, 42, -126, 20, 0, 76, 4, -126, 4) // oid = 1.2.276.0.76.4.260

/**
 * Function type that provides the current timestamp for truststore validation.
 * Returns an [Instant] representing the current time.
 */
typealias TruststoreTimeSourceProvider = () -> Instant

/**
 * Function type that creates a trusted truststore from untrusted certificate and OCSP lists.
 *
 * @param untrustedOCSPList List of untrusted OCSP responses
 * @param untrustedCertList List of untrusted certificates
 * @param trustAnchor The trust anchor certificate (root certificate)
 * @param ocspResponseMaxAge Maximum allowed age for OCSP responses
 * @param timestamp Current timestamp for validation
 * @return A validated [TrustedTruststore]
 */
typealias TrustedTruststoreProvider = (
    untrustedOCSPList: UntrustedOCSPList,
    untrustedCertList: UntrustedCertList,
    trustAnchor: X509CertificateHolder,
    ocspResponseMaxAge: Duration,
    timestamp: Instant
) -> TrustedTruststore

/**
 * The TruststoreUseCase manages certificate validation and truststore operations.
 *
 * This class is responsible for:
 * - Creating and caching a trusted truststore
 * - Validating IDP (Identity Provider) certificates against the truststore
 * - Providing valid VAU (Verschlüsselung von Authentifizierung) public keys
 * - Managing the lifecycle of the truststore
 *
 * The truststore contains validated certificates and OCSP responses that are used
 * to verify the authenticity and validity of certificates used in secure communications.
 *
 * @param config Configuration for the truststore, including trust anchor and OCSP response max age
 * @param repository Repository for storing and retrieving certificate and OCSP data
 * @param timeSourceProvider Provider for current time used in validation
 * @param trustedTruststoreProvider Provider for creating trusted truststores
 */
@Requirement(
    "O.Auth_12#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "TruststoreUseCase implementation"
)
class TruststoreUseCase(
    private val config: TruststoreConfig,
    private val repository: VauRepository,
    private val timeSourceProvider: TruststoreTimeSourceProvider,
    private val trustedTruststoreProvider: TrustedTruststoreProvider
) {
    /**
     * Mutex to ensure thread-safe access to the cached truststore.
     */
    private val lock = Mutex()

    /**
     * Cached instance of the validated truststore to avoid repeated validation.
     */
    internal var cachedTruststore: TrustedTruststore? = null

    /**
     * Checks the provided [idpCertificate] against the truststore.
     *
     * This method validates that the provided IDP certificate is present in the truststore's
     * list of validated IDP certificates. If the certificate is not found, an exception is thrown.
     *
     * @param idpCertificate The IDP certificate to validate
     * @param invalidateStoreOnFailure If true, the truststore will be invalidated if validation fails
     * @throws IllegalArgumentException If the certificate is not found in the truststore
     */
    suspend fun checkIdpCertificate(
        idpCertificate: List<X509CertificateHolder>,
        ocspList: UntrustedOCSPList,
        invalidateStoreOnFailure: Boolean = false
    ) {
        lock.withLock {
            val timestamp = timeSourceProvider()

            Napier.d("Check IDP certificate with truststore")
            val exception = withLoadedStore(timestamp, idpCertificate, ocspList) { store ->
                validateIdpCertificate(store, idpCertificate.first(), invalidateStoreOnFailure)
            }

            // If an exception was returned, log it and throw it
            if (exception != null) {
                Napier.e { "checkIdpCertificate exception $exception" }
                throw exception
            }
        }
    }

    /**
     * Validates that the provided IDP certificate is present in the truststore's
     * list of validated IDP certificates.
     *
     * @param store The truststore to check against
     * @param idpCertificate The IDP certificate to validate
     * @param invalidateStoreOnFailure If true, throw the exception immediately if validation fails
     * @return null if validation succeeds, or the exception if validation fails
     */
    internal fun validateIdpCertificate(
        store: TrustedTruststore,
        idpCertificate: X509CertificateHolder,
        invalidateStoreOnFailure: Boolean
    ): Exception? {
        return try {
            // Find the certificate in the truststore's validated IDP certificates
            requireNotNull(store.idpCertificates.find { it == idpCertificate }) {
                "IDP certificate could not be validated"
            }
            null
        } catch (e: Exception) {
            // If invalidateStoreOnFailure is true, throw the exception immediately
            // Otherwise, return the exception to be thrown later
            if (invalidateStoreOnFailure) {
                throw e
            } else {
                e
            }
        }
    }

    /**
     * Executes the provided [block] with a valid VAU public key.
     *
     * This method ensures that a valid truststore is loaded and provides the VAU public key
     * from that truststore to the [block] function. The truststore is loaded and validated
     * before the block is executed.
     *
     * @param block Function to execute with the valid VAU public key
     * @return The result of the [block] function
     */
    suspend fun <R> withValidVauPublicKey(block: (vauPubKey: ECPublicKey) -> R): R = lock.withLock {
        val timestamp = timeSourceProvider()
        val vauPublicKey = getValidVauPublicKey(timestamp)
        return block(vauPublicKey)
    }

    /**
     * Gets a valid VAU public key from a valid truststore.
     *
     * @param timestamp Current time for validation
     * @return A valid VAU public key
     * @throws Exception If a valid VAU public key cannot be obtained
     */
    internal suspend fun getValidVauPublicKey(timestamp: Instant): ECPublicKey {
        return withLoadedStore(timestamp = timestamp) {
            it.vauPublicKey
        }
    }

    /**
     * Caches a validated truststore and returns it.
     *
     * This method manages the lifecycle of the truststore:
     * 1. If a cached truststore exists, it checks its validity
     * 2. If the cached truststore is valid, it uses it
     * 3. If the cached truststore is invalid or doesn't exist, it creates a new one
     * 4. If creating a new truststore fails, it invalidates the repository and tries again
     *
     * The method ensures that the truststore used is always valid at the provided [timestamp].
     * If the temporal validation fails (OCSP responses or certificates are outdated),
     * a new store is loaded from the repository.
     *
     * @param timestamp Current time for validation
     * @param block Function to execute with the loaded truststore
     * @return The result of the [block] function
     * @throws Exception If the truststore cannot be loaded or validated
     */
    internal suspend fun <R> withLoadedStore(
        timestamp: Instant,
        idpCertificate: List<X509CertificateHolder>? = null,
        ocspList: UntrustedOCSPList? = null,
        block: (TrustedTruststore) -> R
    ): R {
        try {
            val store = getValidTruststore(timestamp, idpCertificate, ocspList)

            // Cache the validated store for future use
            cachedTruststore = store

            // Execute the provided block with the validated store
            return block(store)
        } catch (e: Exception) {
            // If any exception occurs, invalidate the cache and repository
            cachedTruststore = null
            repository.invalidate()

            throw e
        }
    }

    @Requirement(
        "A_25059#2",
        sourceSpecification = "gemSpec_Krypt",
        rationale = """
            - Check validity of OCSP responses by checking that its not null and the max age. 
            - If this fails it throws an exception which recreates the trust store and new OCSP responses are retrieved
        """,
        codeLines = 40
    )
    /**
     * Gets a valid truststore, either from the cache or by creating a new one.
     *
     * @param timestamp Current time for validation
     * @return A valid [TrustedTruststore]
     * @throws Exception If a valid truststore cannot be obtained
     */
    internal suspend fun getValidTruststore(
        timestamp: Instant,
        idpCertificate: List<X509CertificateHolder>?,
        ocspList: UntrustedOCSPList?
    ): TrustedTruststore {
        return cachedTruststore?.let {
            Napier.d("Use cached truststore...")

            try {
                // Check if the cached truststore is still valid
                it.checkValidity(config.getOcspMaxAge(), timestamp)

                it
            } catch (e: Exception) {
                // OCSP responses or certificates are outdated

                // Delete all certs & responses and recreate the store
                repository.invalidate()

                createTrustedTruststore(timestamp, idpCertificate, ocspList)
            }
        } ?: run {
            Napier.d("Create truststore from repository...")

            try {
                createTrustedTruststore(timestamp, idpCertificate, ocspList)
            } catch (e: Exception) {
                // Retry one more time; the failure might originate from an outdated OCSP response

                // Delete all locally cached certs & responses
                repository.invalidate()

                createTrustedTruststore(timestamp, idpCertificate, ocspList)
            }
        }
    }

    // A_20617-01,
    @Requirement(
        "A_20161-01#2",
        "A_21218#2",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Create Truststore."
    )
    @Requirement(
        "A_20623#1",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Create truststore"
    )
    /**
     * Creates a new trusted truststore using the repository and truststore provider.
     *
     * This method loads untrusted certificates and OCSP responses from the repository,
     * then uses the trusted truststore provider to create a validated truststore.
     *
     * The created truststore contains validated certificates and OCSP responses that
     * can be used for secure communications.
     *
     * @param timestamp Current time for validation
     * @return A validated [TrustedTruststore]
     */
    internal suspend fun createTrustedTruststore(
        timestamp: Instant,
        idpCertificate: List<X509CertificateHolder>?,
        ocspList: UntrustedOCSPList?
    ): TrustedTruststore {
        Napier.d("Load truststore from repository...")

        return repository.withUntrusted { untrustedCertList, untrustedOCSPList ->
            val mergedCertList = idpCertificate
                ?.let { idpCert ->
                    untrustedCertList.copy(
                        eeCerts = untrustedCertList.eeCerts.orEmpty() + idpCert
                    )
                } ?: untrustedCertList

            val mergedOcspList = ocspList
                ?.takeIf { it.responses.isNotEmpty() }
                ?.let { ocsp ->
                    untrustedOCSPList.copy(
                        responses = untrustedOCSPList.responses + ocsp.responses
                    )
                } ?: untrustedOCSPList
            // Persist in new format so future calls can use the dedicated fields
            repository.saveLists(mergedCertList, mergedOcspList)
            trustedTruststoreProvider(
                mergedOcspList,
                mergedCertList,
                config.trustAnchor,
                config.getOcspMaxAge(),
                timestamp
            )
        }
    }
}

@Requirement(
    "A_21222#1",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Using the wrapper the certificate is always checked.",
    codeLines = 11
)
/**
 * Extension function that provides a wrapper for X.509 certificates of type [X509Certificate].
 *
 * This function converts the Java X509Certificate to a BouncyCastle X509CertificateHolder
 * and then checks it against the truststore.
 *
 * @param idpCertificate The IDP certificate to validate (Java X509Certificate format)
 * @param invalidateStoreOnFailure If true, the truststore will be invalidated if validation fails
 * @throws IllegalArgumentException If the certificate is not found in the truststore
 */
suspend fun TruststoreUseCase.checkIdpCertificate(
    idpCertificate: List<X509Certificate>,
    ocspList: UntrustedOCSPList,
    invalidateStoreOnFailure: Boolean = false
) {
    checkIdpCertificate(idpCertificate.map { X509CertificateHolder(it.encoded) }, ocspList, invalidateStoreOnFailure)
}

/**
 * A validated truststore containing trusted certificates and OCSP responses.
 *
 * This class represents a truststore that has been validated according to the
 * specifications in `gemSpec_Krypt A_21218`. It contains:
 * - A validated VAU certificate
 * - A list of validated IDP certificates
 * - A list of validated CA certificates
 * - A list of validated OCSP responses
 * - The extracted VAU public key for secure communications
 *
 * The truststore is created through the companion object's create method,
 * which validates all certificates and OCSP responses.
 */
class TrustedTruststore private constructor(
    /**
     * The validated VAU (Verschlüsselung von Authentifizierung) certificate.
     */
    val vauCertificate: X509CertificateHolder,

    /**
     * List of validated IDP (Identity Provider) certificates.
     */
    val idpCertificates: List<X509CertificateHolder>,

    /**
     * List of validated CA (Certificate Authority) certificates.
     */
    val caCertificates: List<X509CertificateHolder>,

    /**
     * List of validated OCSP (Online Certificate Status Protocol) responses.
     */
    val ocspResponses: List<BasicOCSPResp>,

    /**
     * The extracted public key from the VAU certificate, used for secure communications.
     */
    val vauPublicKey: ECPublicKey
) {
    // A_20617-01,
    /**
     * Checks the validity of all certificates and OCSP responses in the truststore.
     *
     * This method verifies that:
     * 1. All OCSP responses are valid and not older than the specified maximum age
     * 2. The VAU certificate is valid at the specified timestamp
     * 3. All CA certificates are valid at the specified timestamp
     *
     * If any validation fails, an exception is thrown.
     *
     * @param ocspResponseMaxAge Maximum allowed age for OCSP responses
     * @param timestamp Current time for validation
     * @throws IllegalArgumentException If any certificate or OCSP response is invalid
     */
    @Requirement(
        "A_20161-01#3",
        "A_21218#1",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "OCSP validity is verified by the truststore."
    )
    @Requirement(
        "A_20623#2",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "OCSP validity is verified by truststore"
    )
    fun checkValidity(ocspResponseMaxAge: Duration, timestamp: Instant) {
        // Verify that there are OCSP responses and check their validity
        require(ocspResponses.isNotEmpty()) { "No OCSP responses. This should never happen" }
        ocspResponses.forEach { resp ->
            resp.checkValidity(ocspResponseMaxAge, timestamp)
        }

        // Check the validity of the VAU certificate
        vauCertificate.checkValidity(timestamp)

        // Verify that there are CA certificates and check their validity
        require(caCertificates.isNotEmpty()) { "No CA certificates. This should never happen" }
        caCertificates.forEach {
            it.checkValidity(timestamp)
        }
    }
    /**
     * Companion object containing factory method to create a validated truststore.
     */

    /**
     * Creates a validated truststore from untrusted certificate and OCSP lists.
     *
     * This method performs the following steps:
     * 1. Validates the subject DNs of certificates
     * 2. Validates additional root certificates against the trust anchor
     * 3. Finds valid OCSP responses
     * 4. Creates certificate chains
     * 5. Finds valid VAU and IDP certificate chains
     * 6. Creates a TrustedTruststore with the validated certificates and OCSP responses
     *
     * @param untrustedOCSPList List of untrusted OCSP responses
     * @param untrustedCertList List of untrusted certificates
     * @param trustAnchor The trust anchor certificate (root certificate)
     * @param ocspResponseMaxAge Maximum allowed age for OCSP responses
     * @param timestamp Current time for validation
     * @return A validated [TrustedTruststore]
     * @throws IllegalArgumentException If validation fails
     */
    @Requirement(
        "A_20623#3",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Create a TrustedTruststore."
    )
    @Requirement(
        "A_20161-01#1",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Create a TrustedTruststore."
    )
    @Requirement(
        "A_25063",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Create/Update TrustedTruststore."
    )
    @Requirement(
        "A_21216",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Create/Update TrustedTruststore."
    )
    @Requirement(
        "A_24469",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Create/Update TrustedTruststore with categories from the specification."
    )
    @Requirement(
        "A_25062, ",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Create/Update TrustedTruststore."
    )
    companion object {
        fun create(
            untrustedOCSPList: UntrustedOCSPList,
            untrustedCertList: UntrustedCertList,
            trustAnchor: X509CertificateHolder,
            ocspResponseMaxAge: Duration,
            timestamp: Instant
        ): TrustedTruststore {
            val addRoots = untrustedCertList.addRoots.distinct()

            // validate the common name according to the specified pattern in `gemSpec_Krypt Tab_KRYPT_ERP_FdV_Truststore_aktualisieren`
            val filteredAddRoots = addRoots.validateSubjectDN(RCA_PREFIX)
            val filteredCaCerts = untrustedCertList.caCerts.validateSubjectDN(CA_PREFIX).distinct()

            @Requirement(
                "A_24470",
                sourceSpecification = "gemSpec_Krypt",
                rationale = "All the categories from A-D are present below",
                codeLines = 48
            )
            val currentDate = Date(timestamp.toEpochMilliseconds())
            // Category A:
            // Before adding an addRoot we check if it can be validated by the currently potential trust store.
            // We expect the incoming addRoots to be chronically ordered (i.e. ["RCA3->RCA4", "RCA4->RCA5", ...])
            //  so a simple forEach loop is already sufficient here. See also gemSpec_Krypt A_21216.
            val validatedAddRoots = getValidatedChainSignedByTrustAnchor(trustAnchor, filteredAddRoots, currentDate)

            // Category B:
            val validatedCaCertificate = getValidatedCertificates(filteredCaCerts, validatedAddRoots, currentDate)
            var validatedEeCertificate = emptyList<X509CertificateHolder>()
            untrustedCertList.eeCerts?.let {
                validatedEeCertificate = getValidatedCertificates(untrustedCertList.eeCerts, validatedCaCertificate, currentDate)
            }

            @Requirement(
                "A_25060#1",
                sourceSpecification = "gemSpec_Krypt",
                rationale = "validatedCaCertificate are used to check the ocsp responses",
                codeLines = 6
            )
            val validOcspResponses = findValidOcspResponses(
                ocspResponses = untrustedOCSPList.responses.map { it.responseObject as BasicOCSPResp },
                caCertList = validatedCaCertificate,
                maxAge = ocspResponseMaxAge,
                timestamp = timestamp
            )

            require(
                checkEeCertificatesStatus(
                    eeCerts = validatedEeCertificate,
                    caCerts = validatedCaCertificate,
                    withResponses = validOcspResponses,
                    ocspResponseMaxAge = ocspResponseMaxAge,
                    validationTime = timestamp
                )
            )
            // Category C and D:
            @Requirement(
                "A_25058#1",
                sourceSpecification = "gemSpec_Krypt",
                rationale = "findValidVauChain and findValidIdpChains verify the certificate validity",
                codeLines = 39
            )
            val validVauCertChain =
                findNewValidVauChain(
                    validatedEeCertificate,
                    validatedCaCertificate,
                    validOcspResponses,
                    timestamp
                )

            val validIdpCertChains =
                findValidIdpChains(
                    validatedEeCertificate,
                    validatedCaCertificate,
                    validOcspResponses,
                    timestamp
                )

            val validVauCert = validVauCertChain[0]
            val validIdpCerts = validIdpCertChains

            return TrustedTruststore(
                vauCertificate = validVauCert,
                idpCertificates = validIdpCerts,
                caCertificates = validatedCaCertificate,
                vauPublicKey = KeyFactorySpi.EC().generatePublic(validVauCert.subjectPublicKeyInfo)!! as ECPublicKey,
                ocspResponses = untrustedOCSPList.responses.map { it.responseObject as BasicOCSPResp }
            )
        }
    }
}

@Requirement(
    "A_21222#2",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "X509Certificates are checked before using them."
)
/**
 * Returns a list of validated OCSP responses by signature and the provided [timestamp] & [maxAge].
 *
 * This function validates OCSP responses by:
 * 1. Extracting the signer certificate from the OCSP response
 * 2. Validating the signer certificate against the provided certificate chains
 * 3. Checking the signature of the OCSP response
 * 4. Verifying that the OCSP response is valid at the provided timestamp and not older than maxAge
 *
 * An OCSP response is considered valid if:
 * - It contains a signer certificate
 * - The signer certificate can be validated with at least one of the certificate chains
 * - The signature of the OCSP response is valid
 * - The OCSP response is within the provided period of time
 *
 * @param ocspResponses List of OCSP responses to validate
 * @param caCertChains List of certificate chains to validate the OCSP signer certificates
 * @param maxAge Maximum allowed age for OCSP responses
 * @param timestamp Current time for validation
 * @return List of validated OCSP responses
 */
@Requirement(
    "A_25060#2",
    sourceSpecification = "gemSpec_Krypt",
    rationale = """
       -  Validate the OCSP response by verifying its signature using BouncyCastle,
          based on the responder certificate included in the OCSP response (Category B).
        - Ensure temporal validity of the OCSP response by checking thisUpdate/nextUpdate,
          enforcing maximum OCSP age and allowed clock skew.
        - Identify the issuing CA certificate and construct the expected CertificateID (SHA-1),
          following RFC 6960.
        - Match the correct SingleResponse by comparing issuerNameHash, issuerKeyHash,
          and certificate serial number.
    """,
    codeLines = 81
)
fun findValidOcspResponses(
    ocspResponses: List<BasicOCSPResp>,
    caCertList: List<X509CertificateHolder>,
    maxAge: Duration,
    timestamp: Instant
): List<BasicOCSPResp> = ocspResponses.mapNotNull { ocspResponse ->
    try {
        // extract signer cert and any included intermediates from OCSP response
        val ocspCerts = ocspResponse.certs?.toList().orEmpty()
        val signerCert = requireNotNull(ocspCerts.firstOrNull()) { "No signer certificates within the ocsp response" }

        val ocspIntermediates = if (ocspCerts.size > 1) ocspCerts.drop(1) else emptyList()
        val ocspCertLis = ocspIntermediates + signerCert

        // find at least one valid cert chain
        require(ocspCertLis.filterBySignature(caCertList)) { "Couldn't validate signer cert of ocsp response" }
        // check signature
        ocspResponse.checkSignatureWith(signerCert)
        // check validity
        ocspResponse.checkValidity(maxAge, timestamp)

        // return valid response
        ocspResponse
    } catch (e: Exception) {
        Napier.d("OCSP response not valid", e)
        null
    }
}

fun checkEeCertificateStatus(
    eeCertificate: X509CertificateHolder,
    caCerts: List<X509CertificateHolder>,
    ocspResponse: BasicOCSPResp,
    validationTime: Instant,
    ocspResponseMaxAge: Duration,
    maxClockSkew: KDuration = 5.minutes
): Boolean {
    val basic = ocspResponse

    // Verify signature of OCSP response
    val responderCertHolder = basic.certs.firstOrNull() ?: return false
    val responderCert = toX509(responderCertHolder)
    val verifierProvider = JcaContentVerifierProviderBuilder()
        .setProvider("BC")
        .build(responderCert.publicKey)
    if (!basic.isSignatureValid(verifierProvider)) {
        return false
    }

    // Time checks
    basic.checkValidity(ocspResponseMaxAge, validationTime)

    // Find issuer of EE certificate
    val issuerHolder = findIssuerFor(eeCertificate, caCerts) ?: return false

    val digestCalcProv = JcaDigestCalculatorProviderBuilder()
        .setProvider("BC")
        .build()

    val expectedId = CertificateID(
        digestCalcProv.get(CertificateID.HASH_SHA1),
        issuerHolder,
        eeCertificate.serialNumber
    )

    // Match single response
    val single = basic.responses.firstOrNull { singleResp ->
        val id = singleResp.certID
        id.serialNumber == expectedId.serialNumber &&
            id.issuerKeyHash.contentEquals(expectedId.issuerKeyHash) &&
            id.issuerNameHash.contentEquals(expectedId.issuerNameHash)
    } ?: return false

    val thisUpdate: KInstant = single.thisUpdate.toInstant().toKotlinInstant()
    val nextUpdate: KInstant = single.nextUpdate?.toInstant()?.toKotlinInstant() ?: (thisUpdate + ocspResponseMaxAge)

    if (validationTime < thisUpdate - maxClockSkew) return false
    if (validationTime > nextUpdate + maxClockSkew) return false

    return single.getOscpResponsStatus()
}

@Requirement(
    "A_21222#3",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "X509Certificates are checked before using them."
)
/**
 * Returns the first valid VAU certificate chain.
 *
 * This function finds a valid VAU certificate chain by:
 * 1. Filtering chains by the VAU OID
 * 2. Checking that the certificates have valid OCSP responses
 * 3. Validating the signature chain
 *
 * While only one VAU certificate should exist at any time, this function won't fail if multiple
 * chains are valid. Instead, it returns the first valid chain as the true one.
 *
 * @param chains List of certificate chains to validate
 * @param validOcspResponses List of validated OCSP responses
 * @param timestamp Current time for validation
 * @return The first valid VAU certificate chain
 * @throws IllegalArgumentException If no valid VAU certificate chain is found
 */
@Requirement(
    "A_21222#3",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "X509Certificates are checked before using them."
)
@Requirement(
    "A_25058#2",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "the require method breaks the code if the certificate is not present",
    codeLines = 8
)
fun findNewValidVauChain(
    chains: List<X509CertificateHolder>,
    ca: List<X509CertificateHolder>,
    validOcspResponses: List<BasicOCSPResp>,
    timestamp: Instant
): List<X509CertificateHolder> = chains.filterByOIDAndOCSPResponse(vauOid, ca, validOcspResponses, timestamp)

/**
 * Returns all valid IDP certificate chains.
 *
 * This function finds valid IDP certificate chains by:
 * 1. Filtering chains by the IDP OID
 * 2. Checking that the certificates have valid OCSP responses
 * 3. Validating the signature chain
 *
 * Unlike the VAU certificate, multiple valid IDP certificates can exist simultaneously.
 * This function returns all valid IDP certificate chains.
 *
 * @param chains List of certificate chains to validate
 * @param validOcspResponses List of validated OCSP responses
 * @param timestamp Current time for validation
 * @return List of valid IDP certificate chains
 * @throws IllegalArgumentException If fewer than 2 valid IDP certificate chains are found
 */
@Requirement(
    "A_20625#1",
    "A_20623#5",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Validate signature of IDP chains. Check for OID, OCSP response and signature."
)
@Requirement(
    "A_21222#4",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "X509Certificates are checked before using them."
)
@Requirement(
    "A_25058#3",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "the require method breaks the code if the certificate is not present",
    codeLines = 8
)
fun findValidIdpChains(
    chains: List<X509CertificateHolder>,
    ca: List<X509CertificateHolder>,
    validOcspResponses: List<BasicOCSPResp>,
    timestamp: Instant
): List<X509CertificateHolder> = chains.filterByOIDAndOCSPResponse(idpOid, ca, validOcspResponses, timestamp)

/**
 * Validates a cross-signed certificate chain against a trust anchor.
 *
 * This function verifies that:
 * 1. The chain is properly signed (each certificate is signed by the next one in the chain)
 * 2. The chain leads up to the trust anchor certificate
 *
 * @param trustAnchorCert The trust anchor (root) certificate
 * @param intermediateCerts The list of intermediate certificates to validate
 * @return true if the chain is valid, false otherwise
 */

// ---------------- checkEeCertificatesStatus() ----------------

/**
 * Verifies that for all EE certificates in the TrustStore
 * there is at least one matching, valid OCSP response in the list.
 *
 * (If you're only validating VAU, you can simplify this accordingly.)
 */
fun checkEeCertificatesStatus(
    eeCerts: List<X509CertificateHolder>,
    caCerts: List<X509CertificateHolder>,
    withResponses: List<BasicOCSPResp>,
    ocspResponseMaxAge: Duration,
    validationTime: Instant
): Boolean {
    return eeCerts.all { ee ->
        withResponses.any { resp ->
            checkEeCertificateStatus(
                eeCertificate = ee,
                caCerts = caCerts,
                ocspResponse = resp,
                ocspResponseMaxAge = ocspResponseMaxAge,
                validationTime = validationTime
            )
        }
    }
}

private fun findIssuerFor(cert: X509CertificateHolder, allCerts: List<X509CertificateHolder>): X509CertificateHolder? {
    val issuerName = cert.issuer
    return allCerts.firstOrNull { it.subject == issuerName }
}

private val certConverter: JcaX509CertificateConverter =
    JcaX509CertificateConverter().setProvider("BC")

private fun toX509(cert: X509CertificateHolder): X509Certificate =
    certConverter.getCertificate(cert)

internal fun getValidatedChainSignedByTrustAnchor(
    trustAnchorCert: X509CertificateHolder,
    crossCerts: List<X509CertificateHolder>,
    currentDate: Date
): List<X509CertificateHolder> {
    Security.addProvider(BouncyCastleProvider())

    val validatedRootsChain = mutableListOf<X509CertificateHolder>()
    validatedRootsChain += trustAnchorCert

    try {
        val fullChain = listOf(trustAnchorCert) + crossCerts

        for (i in 0 until fullChain.size - 1) {
            val first = fullChain[i]
            val second = fullChain[i + 1]

            val canFirstValidateSecond = try {
                second.canBeValidatedBy(first, currentDate)
            } catch (e: Exception) {
                Napier.e(e) { "❌ Forward verification failed between cert[$i] -> cert[${i + 1}]: ${e.message}" }
                false
            }

            val canSecondValidateFirst = if (!canFirstValidateSecond) {
                try {
                    first.canBeValidatedBy(second, currentDate)
                } catch (e: Exception) {
                    Napier.e(e) { "❌ Reverse verification failed between cert[${i + 1}] -> cert[$i]: ${e.message}" }
                    false
                }
            } else {
                false
            }

            if (canFirstValidateSecond || canSecondValidateFirst) {
                // We always append the "next" element in the chain, i.e. [trustAnchor, next, next, ...]
                validatedRootsChain += second
            } else {
                // As soon as there is no valid relationship in either direction, we stop extending the chain
                break
            }
        }

        Napier.d("✅ Chain of cross-signed certificates is valid (supports forward and reverse validation).")
    } catch (e: Exception) {
        Napier.d("❌ Verification failed: ${e.message}", e)
    }

    return validatedRootsChain
}

internal fun getValidatedCertificates(
    candidateCertificates: List<X509CertificateHolder>,
    trustedCertificates: List<X509CertificateHolder>,
    currentDate: Date
): List<X509CertificateHolder> {
    Security.addProvider(BouncyCastleProvider())

    return try {
        val validCertificates = candidateCertificates.filter { candidate ->
            trustedCertificates.any { trusted ->
                try {
                    // forward: candidate is validated by trusted
                    candidate.canBeValidatedBy(trusted, currentDate) ||
                        // reverse: trusted is validated by candidate
                        trusted.canBeValidatedBy(candidate, currentDate)
                } catch (e: Exception) {
                    Napier.e(e) { "❌ Verification failed between candidate and trusted: ${e.message}" }
                    false
                }
            }
        }

        Napier.d("✅ Found ${validCertificates.size} certificate(s) with a valid (forward or reverse) trust relationship.")
        validCertificates
    } catch (e: Exception) {
        Napier.d("❌ Verification failed: ${e.message}", e)
        emptyList()
    }
}
