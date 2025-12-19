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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.idp.usecase

import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.generateRandomAES256Key
import de.gematik.ti.erp.app.idp.EllipticCurvesExtending
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.api.REDIRECT_URI
import de.gematik.ti.erp.app.idp.api.models.IdpAuthFlowResult
import de.gematik.ti.erp.app.idp.api.models.IdpChallengeFlowResult
import de.gematik.ti.erp.app.idp.api.models.IdpInitialData
import de.gematik.ti.erp.app.idp.api.models.IdpNonce
import de.gematik.ti.erp.app.idp.api.models.IdpRefreshFlowResult
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.api.models.IdpState
import de.gematik.ti.erp.app.idp.api.models.IdpTokenResult
import de.gematik.ti.erp.app.idp.api.models.IdpUnsignedChallenge
import de.gematik.ti.erp.app.idp.api.models.JWSPublicKey
import de.gematik.ti.erp.app.idp.api.models.TokenResponse
import de.gematik.ti.erp.app.idp.buildJsonWebSignatureWithHealthCard
import de.gematik.ti.erp.app.idp.buildKeyVerifier
import de.gematik.ti.erp.app.idp.extension.issuerCommonName
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.secureRandomInstance
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import de.gematik.ti.erp.app.vau.repository.VauRemoteDataSource
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import de.gematik.ti.erp.app.vau.usecase.checkIdpCertificate
import io.github.aakira.napier.Napier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.jose4j.base64url.Base64
import org.jose4j.base64url.Base64Url
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.jose4j.jwt.consumer.JwtContext
import org.jose4j.jwt.consumer.NumericDateValidator
import org.jose4j.jwx.JsonWebStructure
import java.net.URI
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Security
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import javax.crypto.SecretKey
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Maximum validity period for IDP discovery documents in minutes (24 hours).
 *
 * The IDP configuration (discovery document) must be refreshed at least once every 24 hours
 * for security reasons. This ensures we're always using the latest IDP endpoints and certificates.
 */

private val discoveryDocumentMaxValidityMinutes: Int = 24.hours.inWholeMinutes.toInt()
private val discoveryDocumentMaxValiditySeconds: Int = 24.hours.inWholeSeconds.toInt()

//
// Flow with health card:
// (1) [initializeConfigurationAndKeys] -> [challengeFlow] -> [basicAuthFlow] -> result: [accessToken] & [singleSignOnToken]
//
// Flow with health card & alternative authentication:
// (1)                                                              [secureElementKeyMaterial] -------- +
//                                                                                                       \
//     [initializeConfigurationAndKeys] -> [challengeFlow] -> [basicAuthFlow] -> result: [accessToken] -> + [registerDeviceWithHealthCard] -> result: list of paired devices
// (2) [initializeConfigurationAndKeys] -> [challengeFlow] -> [authenticateWithSecureElement] -> result: [accessToken] & [singleSignOnToken]
//
//
//
// [initializeConfigurationAndKeys]:
// (1) load idp config (local or remote) -> check if it's still valid & check cert against truststore
// (2) fetch public signature key as JWK -> check cert against truststore & check x & y coordinates against included cert
// (3) fetch public encryption key as JWK
// (*) result: [idpConfig], [state] & [nonce], [codeVerifier], [codeChallenge], [pukSigKey as JWK], [pukEncKey as JWK]
//
// [challengeFlow]:
// (1) [codeChallenge] -> fetch challenge with [codeChallenge] -> validate with [state] & [nonce] & [pukSigKey]
// (*) result: [unsignedChallenge (payload of JWS; signature verified)], [signedChallenge (raw challenge from backend)]
//
// [basicAuthFlow]:
// (1) [signatureHandleOfHealthCard] - +
//          [certificateOfHealthCard] - +
//                                       \
//     [signedChallenge] ---------------> + sign & encrypt -> [challenge as JWE(JWS(JWS))] -> post -> result: [redirect]
//
// (2) [code] ------------------- +
//     [ephemeralSymmetricalKey] - +
//     [codeVerifier] ------------- + -> [keyVerifier as JWE]
//                                               |
//                                               v
// (3) [redirect] -> extract [code] & [ssoToken] + -> post [code] with [keyVerifier] -> result: [encryptedAccessToken]
// (4) [encryptedAccessToken as JWE(JWS)] -> decrypt with [ephemeralSymmetricalKey] -> [accessToken as JWS] -> validate with [state] & [nonce] & [pukSigKey]
// (*) result: [accessToken as JWS] & [ssoToken]
//
/**
 * Core use case for Identity Provider (IDP) authentication flows.
 *
 * This class handles the complete authentication process with the German health insurance IDP,
 * including health card authentication and secure element (biometric) authentication.
 *
 * ## Key Concepts:
 *
 * ### What is an IDP (Identity Provider)?
 * An IDP is a service that authenticates users and provides access tokens. Think of it like
 * "Login with Google" - the IDP verifies who you are and gives you a token to prove it.
 *
 * ### What are Certificates?
 * Digital certificates are like digital passports - they prove the identity of a server or device.
 * They contain:
 * - A public key (used for encryption/verification)
 * - Information about who owns it
 * - A digital signature from a trusted authority
 *
 * ### Authentication Flow Overview:
 * 1. **Initialize**: Get configuration and encryption keys from the IDP
 * 2. **Challenge**: IDP sends a challenge (like a test question) to verify identity
 * 3. **Sign**: Sign the challenge with health card or biometric data to prove identity
 * 4. **Token**: Receive access token and SSO token for accessing protected resources
 *
 * ### Key Security Concepts:
 * - **Public/Private Keys**: Public key encrypts data, private key decrypts it (like a lock and key)
 * - **Signing**: Using private key to prove you created/approved something
 * - **Encryption**: Scrambling data so only the intended recipient can read it
 * - **OCSP**: Online Certificate Status Protocol - checks if certificates are still valid (not revoked)
 * - **Truststore**: A collection of trusted certificate authorities (like a list of trusted IDs)
 * - **JWS (JSON Web Signature)**: A signed JSON message that can't be tampered with
 * - **JWE (JSON Web Encryption)**: An encrypted JSON message only the recipient can read
 * - **SSO Token**: Single Sign-On token - lets you stay logged in without re-authenticating
 *
 * @property repository Repository for IDP operations (fetching config, keys, tokens)
 * @property remoteDataSource Data source for VAU (Vertrauenswürdige Ausführungsumgebung) operations
 * @property truststoreUseCase Use case for validating certificate trust chains
 */
// TODO: Cleanup ERA-12507
class IdpBasicUseCase(
    private val repository: IdpRepository,
    private val remoteDataSource: VauRemoteDataSource,
    private val truststoreUseCase: TruststoreUseCase
) {

    // ////////////////////////////////////////////////////////////////////////////////////////
    @Requirement(
        "A_17207#3",
        "GS-A_4357-02#3",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Initialize required algorithms implemented using ECDSA."
    )
    /**
     * Initializes the IDP configuration and cryptographic keys needed for authentication.
     *
     * This is the first step in any authentication flow. It:
     * 1. Loads and validates the IDP configuration (server endpoints, settings)
     * 2. Fetches public keys for signature verification and encryption
     * 3. Validates that the certificates are trustworthy
     * 4. Generates random values (state, nonce, code verifier) for security
     *
     * ## Why do we need this?
     * Before we can authenticate, we need to:
     * - Know where to send requests (from configuration)
     * - Have the IDP's public keys to encrypt data and verify signatures
     * - Generate random values to prevent replay attacks (someone reusing old requests)
     *
     * ## Security Features:
     * - **State**: Random value to verify responses match our requests
     * - **Nonce**: Random value to prevent replay attacks
     * - **Code Verifier/Challenge**: Proves the same client that started auth is finishing it (PKCE)
     *
     * @return [IdpInitialData] containing configuration, keys, and security parameters
     * @throws Exception if configuration is invalid, expired, or certificates can't be validated
     */
    suspend fun initializeConfigurationAndKeys(): IdpInitialData {
        ensureCryptoInitialized()

        val config = loadAndValidateIdpConfiguration()

        // fetch both keys
        val pukSigKey: JWSPublicKey = repository.fetchIdpPukSig(config.pukIdpSigEndpoint).getOrThrow()
        val pukEncKey: JWSPublicKey = repository.fetchIdpPukEnc(config.pukIdpEncEndpoint).getOrThrow()

        val pukKeyLis = listOf(pukSigKey, pukEncKey)

        // verify PUK SIG key matches its certificate
        validatePukSigPublicKey(pukSigKey)

        // validate trust chain (IDP SIG EE cert + config cert)
        validateIdpTrustChain(
            config = config,
            pukSigKey = pukKeyLis,
            invalidateStoreOnFailure = false
        )

        // generate state & nonce used to verify the integrity of certain calls
        val state = IdpState.create()
        val nonce = IdpNonce.create()

        @Requirement(
            "A_20309#1",
            sourceSpecification = "gemSpec_IDP_Frontend",
            rationale = "generation and hashing for codeChallenge"
        )
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)

        return IdpInitialData(
            config,
            pukSigKey = pukSigKey,
            pukEncKey = pukEncKey,
            state = state,
            nonce = nonce,
            codeVerifier = codeVerifier,
            codeChallenge = codeChallenge
        )
    }

    /**
     * Ensures cryptographic libraries are properly initialized.
     *
     * ## What does this do?
     * Sets up BouncyCastle (BC) as the primary cryptographic provider. BouncyCastle is a
     * library that provides the cryptographic algorithms needed for secure communication.
     *
     * ## Why is this needed?
     * - The default Java crypto libraries don't support all the algorithms required
     * - BouncyCastle provides elliptic curve cryptography needed for health cards
     * - Must be initialized before any crypto operations
     *
     * ## Thread Safety:
     * Uses a mutex (lock) to ensure this only happens once, even if called from multiple threads.
     *
     * @see BCProvider The BouncyCastle security provider
     * @see EllipticCurvesExtending Custom elliptic curve initialization
     */
    private suspend fun ensureCryptoInitialized() {
        cryptoInitializedLock.withLock {
            if (!isCryptoInitialized) {
                Security.removeProvider("BC")
                Security.insertProviderAt(BCProvider, 1)
                EllipticCurvesExtending.init()
                isCryptoInitialized = true
            }
        }
    }

    /**
     * Loads and validates the IDP configuration with automatic retry logic.
     *
     * ## What is IDP Configuration?
     * The configuration contains essential information about the IDP server:
     * - Server endpoint URLs (where to send authentication requests)
     * - Certificate information (to verify we're talking to the real IDP)
     * - Validity timestamps (when the config expires)
     *
     * ## Retry Logic:
     * If validation fails on the first attempt:
     * 1. Invalidates the cached configuration (forces fresh download)
     * 2. Retries loading and validating once more
     * 3. If second attempt fails, throws exception
     *
     * ## Why retry?
     * Network issues or expired cached configs can cause temporary failures.
     * One retry with fresh data often resolves the issue.
     *
     * @return Valid [IdpData.IdpConfiguration] ready for use
     * @throws Exception if config cannot be loaded or validated after retry
     */
    private suspend fun loadAndValidateIdpConfiguration(): IdpData.IdpConfiguration =
        retryOnce(
            operation = {
                repository.loadUncheckedIdpConfiguration()
                    .also {
                        @Requirement(
                            "O.Auth_10#2",
                            sourceSpecification = "BSI-eRp-ePA",
                            rationale = "The application also checks for the expiration time-stamp of the IDP configuration.",
                            codeLines = 3
                        )
                        checkIdpConfigurationValidity(it, Clock.System.now())
                    }
            },
            onFailure = { e ->
                Napier.e("IDP config couldn't be validated", e)
                repository.invalidateConfig()
            }
        )

    private suspend fun <T> retryOnce(
        operation: suspend () -> T,
        onFailure: suspend (Throwable) -> Unit
    ): T {
        return try {
            operation()
        } catch (e: Throwable) {
            onFailure(e)
            try {
                operation()
            } catch (e2: Throwable) {
                Napier.e("Retry failed; finally aborting", e2)
                onFailure(e2)
                throw e2
            }
        }
    }

    // A_20617-01,
    @Requirement(
        "A_23082#3",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Get and validate discovery document."
    )
    @Requirement(
        "O.Ntwk_4#1",
        "O.Resi_6#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Get and validate discovery document."
    )
    @Requirement(
        "A_21218#5",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Check OCSP validity."
    )
    /**
     * Validates that the IDP configuration is trustworthy and still valid.
     *
     * ## What does this check?
     * 1. **Certificate Trust Chain**: Verifies the IDP's certificate was issued by a trusted authority
     * 2. **OCSP Status**: Checks if certificates have been revoked (are they still good to use?)
     * 3. **Timestamp Validity**: Ensures the configuration hasn't expired
     *
     * ## Why is this important?
     * - Prevents using compromised or revoked certificates
     * - Ensures we're talking to the real IDP, not an imposter
     * - Protects against using outdated configuration with security vulnerabilities
     *
     * ## Validation Rules:
     * - Configuration must have been issued in the past (not future-dated)
     * - Configuration must not be expired (max 24 hours validity)
     * - Allows 60 seconds clock skew to handle minor time differences between systems
     *
     * @param config The IDP configuration to validate
     * @param timestamp The current time to validate against (defaults to now)
     * @throws IllegalArgumentException if validation fails
     */
    @Requirement(
        "A_20512#1",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Get and validate discovery document.",
        codeLines = 6
    )
    suspend fun checkIdpConfigurationValidity(
        config: IdpData.IdpConfiguration,
        timestamp: Instant
    ) {
        // truststore / OCSP validation (config cert + PUK SIG cert)
        validateIdpTrustChainFromRepo(config)

        val claims = JwtClaims().apply {
            issuedAt = NumericDate.fromMilliseconds(config.issueTimestamp.toEpochMilliseconds())
            @Requirement(
                "O.Auth_10#1",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "The application also checks for the expiration time-stamp of the IDP configuration.",
                codeLines = 3
            )
            @Requirement(
                "A_20512#2,",
                sourceSpecification = "gemSpec_IDP_Frontend",
                rationale = "The application also checks for the expiration time-stamp of the IDP configuration. " +
                    "The discoveryDocumentMaxValiditySeconds and discoveryDocumentMaxValidityMinutes value is used to set " +
                    "the maximum validity time of 24 hours.",
                codeLines = 3
            )
            expirationTime = NumericDate.fromMilliseconds(config.expirationTimestamp.toEpochMilliseconds())
        }

        val validationError = NumericDateValidator().apply {
            setAllowedClockSkewSeconds(60)
            setEvaluationTime(NumericDate.fromMilliseconds(timestamp.toEpochMilliseconds()))
            setRequireExp(true)
            setRequireIat(true)
            setIatAllowedSecondsInThePast(discoveryDocumentMaxValiditySeconds)
            setMaxFutureValidityInMinutes(discoveryDocumentMaxValidityMinutes)
        }.validate(JwtContext(claims, emptyList()))

        require(validationError == null) {
            "IDP configuration couldn't be validated: ${validationError.errorMessage}"
        }
    }

    /**
     * Validates the IDP certificate trust chain using repository data.
     *
     * ## What is a Trust Chain?
     * A trust chain is like a chain of recommendations:
     * - The IDP has a certificate
     * - That certificate was signed by a trusted authority (CA)
     * - We verify both the IDP certificate and the signature
     *
     * This method fetches the public signature key and validates that the entire
     * certificate chain is trustworthy and hasn't been revoked.
     *
     * @param config The IDP configuration containing certificate information
     * @throws Exception if trust chain validation fails
     */
    private suspend fun validateIdpTrustChainFromRepo(config: IdpData.IdpConfiguration) {
        val pukSigKey: JWSPublicKey =
            repository.fetchIdpPukSig(config.pukIdpSigEndpoint).getOrThrow()

        validatePukSigPublicKey(pukSigKey)
        validateIdpTrustChain(
            config = config,
            pukSigKey = listOf(pukSigKey),
            invalidateStoreOnFailure = true
        )
    }

    /**
     * Validates that the public key in the JWS matches the public key in its certificate.
     *
     * ## Why is this important?
     * This ensures that the public key we're about to use for verification actually
     * belongs to the certificate holder. It's like checking that a photo ID matches
     * the person showing it to you.
     *
     * ## Technical Details:
     * - Extracts public key from certificate
     * - Extracts public key from JWS
     * - Compares the elliptic curve points (w parameter) to ensure they match
     *
     * @param pukSigKey The JWS public key to validate
     * @throws IllegalArgumentException if keys don't match
     */
    private fun validatePukSigPublicKey(pukSigKey: JWSPublicKey) {
        val certPubKey = pukSigKey.jws.leafCertificate.publicKey as ECPublicKey
        val pubKey = pukSigKey.jws.publicKey as ECPublicKey

        require(certPubKey.w == pubKey.w) {
            "Public key of idpPukSig doesn't match with its contained certificate"
        }
    }

    /**
     * Validates the complete IDP certificate trust chain including OCSP checks.
     *
     * ## What is OCSP?
     * OCSP (Online Certificate Status Protocol) is like checking if a driver's license
     * has been suspended. Even if a certificate looks valid, it might have been revoked
     * (cancelled) by the issuing authority.
     *
     * ## This method validates:
     * 1. **IDP Signature Certificate**: Checks the certificate used for signing
     * 2. **Configuration Certificate**: Checks the certificate in the IDP config
     * 3. **OCSP Status**: Verifies neither certificate has been revoked
     * 4. **Trust Chain**: Ensures both certificates were issued by trusted authorities
     *
     * ## Why check two certificates?
     * - One certificate is for signing data (signatures)
     * - Another is for the configuration itself
     * - Both must be valid and trustworthy
     *
     * @param config The IDP configuration containing certificate information
     * @param pukSigKey The public signature key with its certificate
     * @param invalidateStoreOnFailure Whether to invalidate local cache if validation fails
     * @throws Exception if any certificate is invalid, revoked, or untrusted
     */
    private suspend fun validateIdpTrustChain(
        config: IdpData.IdpConfiguration,
        pukSigKey: List<JWSPublicKey>,
        invalidateStoreOnFailure: Boolean
    ) {
        // Collect all available IDP EE certificates from the provided keys
        val idpSignatureCertificates: List<X509Certificate> = pukSigKey
            .mapNotNull { it.jws.leafCertificate }

        require(idpSignatureCertificates.isNotEmpty()) {
            "No IDP SIG EE certificate available"
        }

        // Configuration certificate and its OCSP response (shared for all validations)
        val configurationCertificateHolder = config.certificate
        val configCertOcspResponse = remoteDataSource
            .loadOcspResponse(
                configurationCertificateHolder.issuer.issuerCommonName(),
                configurationCertificateHolder.serialNumber.toString()
            )
            .getOrThrow()

        val x509ConfigurationCertificate = JcaX509CertificateConverter()
            .setProvider("BC")
            .getCertificate(configurationCertificateHolder)

        // Fetch OCSP for each IDP certificate and collect all responses
        val aggregatedIdpOcspResponses = mutableListOf<org.bouncycastle.cert.ocsp.OCSPResp>()
        idpSignatureCertificates.forEach { idpSigCert ->
            val signatureCertOcspResponse = remoteDataSource
                .loadOcspResponse(
                    idpSigCert.issuerCommonName(),
                    idpSigCert.serialNumber.toString()
                )
                .getOrThrow()
            aggregatedIdpOcspResponses += signatureCertOcspResponse.responses
        }

        // Build combined lists containing all IDP certs plus the configuration certificate,
        // and all OCSP responses (IDP + configuration)
        val idpCertificateList: List<X509Certificate> =
            idpSignatureCertificates + x509ConfigurationCertificate

        val idpOcspResponseList = UntrustedOCSPList(
            aggregatedIdpOcspResponses + configCertOcspResponse.responses
        )

        // Perform a single truststore check using the aggregated inputs
        truststoreUseCase.checkIdpCertificate(
            idpCertificate = idpCertificateList,
            ocspList = idpOcspResponseList,
            invalidateStoreOnFailure = invalidateStoreOnFailure
        )
    }

    /**
     * Executes the challenge flow to obtain an authentication challenge from the IDP.
     *
     * ## What is a Challenge?
     * A challenge is like a test question the IDP sends to verify you are who you claim to be.
     * You must sign this challenge with your health card or biometric data to prove your identity.
     *
     * ## How it works:
     * 1. Sends a request to the IDP with security parameters (code challenge, state, nonce)
     * 2. Receives an unsigned challenge from the IDP
     * 3. Validates the challenge matches our request (checks state and nonce)
     * 4. Returns the challenge ready to be signed
     *
     * ## Security Parameters:
     * - **Code Challenge**: Proves the same client completing auth started it (PKCE)
     * - **State**: Prevents request forgery attacks
     * - **Nonce**: Prevents replay attacks
     * - **Scope**: Defines what permissions we're requesting
     *
     * @param initialData Configuration and keys from initialization step
     * @param scope The authentication scope (e.g., basic auth or biometric pairing)
     * @param redirectUri Where the IDP should redirect after authentication
     * @return [IdpChallengeFlowResult] containing the challenge to be signed
     * @throws Exception if challenge cannot be fetched or validated
     */
    suspend fun challengeFlow(
        initialData: IdpInitialData,
        scope: IdpScope,
        redirectUri: String
    ): IdpChallengeFlowResult {
        val (config, pukSigKey, _, state, nonce) = initialData
        val codeChallenge = initialData.codeChallenge

        // fetch and check the challenge

        val challenge = fetchAndCheckUnsignedChallenge(
            url = config.authorizationEndpoint,
            codeChallenge = codeChallenge,
            state = state,
            nonce = nonce,
            scope = scope,
            pukSigKey = pukSigKey,
            redirectUri = redirectUri
        )

        return IdpChallengeFlowResult(
            scope = scope,
            challenge = challenge
        )
    }

    suspend fun basicAuthFlow(
        initialData: IdpInitialData,
        redirectUri: String = REDIRECT_URI,
        challengeData: IdpChallengeFlowResult,
        healthCardCertificate: ByteArray,
        sign: suspend (hash: ByteArray) -> ByteArray
    ): IdpAuthFlowResult {
        val (config, pukSigKey, pukEncKey, state, nonce) = initialData
        val codeVerifier = initialData.codeVerifier

        // sign [challengeBody] with the health card

        val signedChallenge =
            buildSignedChallenge(challengeData.challenge.signedChallenge, healthCardCertificate) {
                sign(it)
            }
        val encryptedSignedChallenge =
            buildEncryptedSignedChallenge(
                signedChallenge,
                challengeData.challenge.expires,
                pukEncKey.jws.publicKey
            )

        // post encrypted signed challenge & parse returned redirect url

        val redirect = postSignedChallengeAndGetRedirect(
            config.authorizationEndpoint,
            codeChallenge = encryptedSignedChallenge,
            state = state
        )

        val redirectCodeJwe = IdpService.extractQueryParameter(redirect, "code")
        val redirectSsoToken = IdpService.extractQueryParameter(redirect, "ssotoken")

        // post [redirectCodeJwe] &b get the access token

        val idpTokenResult = postCodeAndDecryptAccessToken(
            config.tokenEndpoint,
            nonce = nonce,
            codeVerifier = codeVerifier,
            code = redirectCodeJwe,
            pukEncKey = pukEncKey,
            pukSigKey = pukSigKey,
            redirectUri = redirectUri
        )

        val idTokenJson = Json.parseToJsonElement(
            idpTokenResult.idTokenPayload
        )

        @Requirement(
            "A_21327#4",
            sourceSpecification = "gemSpec_IDP_Frontend",
            rationale = "Usage of idToken Payload / idToken is not persisted / " +
                "since we have automatic memory management, we can't delete the token. " +
                "Due to the use of frameworks we have sensitive data as immutable objects and hence " +
                "cannot override it"
        )
        return IdpAuthFlowResult(
            accessToken = idpTokenResult.decryptedAccessToken,
            expiresOn = idpTokenResult.expiresOn,
            ssoToken = redirectSsoToken,
            idTokenInsuranceIdentifier = idTokenJson.jsonObject["idNummer"]?.jsonPrimitive?.content ?: "",
            organizationIdentifier = idTokenJson.jsonObject["organizationIK"]?.jsonPrimitive?.content ?: "",
            idTokenInsuranceName = idTokenJson.jsonObject["organizationName"]?.jsonPrimitive?.content ?: "",
            idTokenInsurantName = idTokenJson.jsonObject["given_name"]?.jsonPrimitive?.content?.let {
                it + " " + idTokenJson.jsonObject["family_name"]?.jsonPrimitive?.content
            } ?: ""
        )
    }

    suspend fun refreshAccessTokenWithSsoFlow(
        initialData: IdpInitialData,
        scope: IdpScope,
        ssoToken: String,
        redirectUri: String
    ): IdpRefreshFlowResult {
        val (config, pukSigKey, pukEncKey) = initialData
        val state = initialData.state
        val nonce = initialData.nonce
        val codeVerifier = initialData.codeVerifier
        val codeChallenge = initialData.codeChallenge

        val unsignedChallenge = fetchAndCheckUnsignedChallenge(
            url = config.authorizationEndpoint,
            codeChallenge = codeChallenge,
            state = state,
            nonce = nonce,
            scope = scope,
            pukSigKey = pukSigKey,
            redirectUri = redirectUri
        )

        val redirect = postUnsignedChallengeWithSsoTokenAndGetRedirect(
            config.ssoEndpoint,
            // we post an unsigned (i.e., we didn't sign the challenge, which is signed by the idp, again) to the sso response endpoint
            unsignedCodeChallenge = unsignedChallenge.signedChallenge,
            ssoToken = ssoToken,
            state = state
        )

        val codeFromRedirect = IdpService.extractQueryParameter(redirect, "code")

        @Requirement(
            "A_20283-01#2",
            sourceSpecification = "gemSpec_IDP_Frontend",
            rationale = "post redirect and decrypt access token."
        )
        val idpTokenResult = postCodeAndDecryptAccessToken(
            config.tokenEndpoint,
            nonce = nonce,
            codeVerifier = codeVerifier,
            code = codeFromRedirect,
            pukEncKey = pukEncKey,
            pukSigKey = pukSigKey,
            redirectUri = redirectUri
        )

        return IdpRefreshFlowResult(scope, idpTokenResult.decryptedAccessToken, expiresOn = idpTokenResult.expiresOn)
    }

    // ////////////////////////////////////////////////////////////////////////////////////////

    suspend fun postSignedChallengeAndGetRedirect(
        url: String,
        codeChallenge: JsonWebEncryption,
        state: IdpState
    ): URI {
        val redirect = URI(repository.postSignedChallenge(url, codeChallenge.compactSerialization).getOrThrow())

        val redirectState = IdpService.extractQueryParameter(redirect, "state")
        require(state.state == redirectState) { "Invalid state" }

        return redirect
    }

    suspend fun postUnsignedChallengeWithSsoTokenAndGetRedirect(
        url: String,
        unsignedCodeChallenge: String,
        ssoToken: String,
        state: IdpState
    ): URI {
        val redirect = URI(repository.postUnsignedChallengeWithSso(url, ssoToken, unsignedCodeChallenge).getOrThrow())

        val redirectState = IdpService.extractQueryParameter(redirect, "state")
        require(state.state == redirectState) { "Invalid state" }

        return redirect
    }

    // GS-A_4357-01
    suspend fun fetchAndCheckUnsignedChallenge(
        url: String,
        codeChallenge: String,
        state: IdpState,
        nonce: IdpNonce,
        scope: IdpScope,
        pukSigKey: JWSPublicKey,
        redirectUri: String
    ): IdpUnsignedChallenge {
        val signedChallenge = repository.fetchChallenge(
            url = url,
            codeChallenge = codeChallenge,
            state = state.state,
            nonce = nonce.nonce,
            isDeviceRegistration = scope == IdpScope.BiometricPairing,
            redirectUri = redirectUri
        ).map {
            it.challenge.jws.apply {
                key = pukSigKey.jws.publicKey
            }
            it.challenge
        }.getOrThrow()

        // check state & nonce
        val unsignedChallenge = signedChallenge.jws.payload
        val unsignedChallengeJson = Json.parseToJsonElement(unsignedChallenge)
        require(state.state == unsignedChallengeJson.jsonObject["state"]!!.jsonPrimitive.content) { "Invalid state" }
        require(nonce.nonce == unsignedChallengeJson.jsonObject["nonce"]!!.jsonPrimitive.content) { "Invalid nonce" }

        val unsignedChallengeExpires = unsignedChallengeJson.jsonObject["exp"]!!.jsonPrimitive.long

        return IdpUnsignedChallenge(
            signedChallenge.raw,
            unsignedChallenge,
            unsignedChallengeExpires
        )
    }

    suspend fun postCodeAndDecryptAccessToken(
        url: String,
        nonce: IdpNonce,
        codeVerifier: String,
        code: String,
        pukEncKey: JWSPublicKey,
        pukSigKey: JWSPublicKey,
        redirectUri: String
    ): IdpTokenResult {
        @Requirement(
            "O.Cryp_3#3",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "AES Key-Generation and one time usage"
        )
        @Requirement(
            "O.Cryp_4#5",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "One time usage for JWE ECDH-ES Encryption"
        )
        @Requirement(
            "GS-A_4389#1",
            sourceSpecification = "gemSpec_Krypt",
            rationale = "AES Key-Generation and one time usage"
        )
        val symmetricalKey = generateRandomAES256Key()

        val keyVerifier = buildKeyVerifier(
            symmetricalKey,
            codeVerifier,
            pukEncKey.jws.publicKey
        )
        return repository.postToken(
            url = url,
            keyVerifier = keyVerifier.compactSerialization,
            code = code,
            redirectUri = redirectUri
        ).map {
            @Requirement(
                "A_20283-01#3",
                "A_19938-01#3",
                sourceSpecification = "gemSpec_IDP_Frontend",
                rationale = "decrypt access token and token validation."
            )
            val decryptedIdToken = decryptIdToken(it, symmetricalKey)
            val idTokenPayload = decryptedIdToken.apply {
                key = pukSigKey.jws.publicKey
            }.payload
            checkNonce(
                idTokenPayload,
                nonce.nonce
            )

            val json = decryptAccessToken(it, symmetricalKey)
            IdpTokenResult(
                decryptedAccessToken = Json.parseToJsonElement(json).jsonObject["njwt"]!!.jsonPrimitive.content,
                expiresOn = Clock.System.now().plus(it.expiresIn.seconds),
                idTokenPayload = idTokenPayload
            )
        }.getOrThrow()
    }

    suspend fun buildSignedChallenge(
        challengeBody: String,
        healthCardCertificate: ByteArray,
        sign: suspend (hash: ByteArray) -> ByteArray
    ): String =
        buildJsonWebSignatureWithHealthCard(
            {
                contentTypeHeaderValue = "NJWT"
                algorithmHeaderValue = "BP256R1"
                setHeader("x5c", listOf(Base64.encode(healthCardCertificate)))
                payload = """{ "njwt": "$challengeBody" }"""
            },
            sign
        )

    fun buildEncryptedSignedChallenge(
        signedChallenge: String,
        challengeExpires: Long,
        idpPukEncKey: PublicKey
    ) =
        JsonWebEncryption().apply {
            contentTypeHeaderValue = "NJWT"
            algorithmHeaderValue = KeyManagementAlgorithmIdentifiers.ECDH_ES
            encryptionMethodHeaderParameter = ContentEncryptionAlgorithmIdentifiers.AES_256_GCM
            setHeader("exp", challengeExpires)
            key = idpPukEncKey
            payload = """{ "njwt": "$signedChallenge" }"""
        }

    @Requirement(
        "A_20309#2",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Generate code verifier using SecureRandome."
    )
    fun generateCodeVerifier(): String {
        // https://datatracker.ietf.org/doc/html/rfc7636#section-4.1
        // 60 bytes are about 80 characters of base64
        return Base64Url.encode(
            ByteArray(60).apply {
                secureRandomInstance().nextBytes(this)
            }
        )
    }

    @Requirement(
        "A_20309#3",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Hashing of code verifier to generate code challenge."
    )
    fun generateCodeChallenge(codeVerifier: String): String {
        // https://datatracker.ietf.org/doc/html/rfc7636#section-4.2
        return Base64Url.encode(
            MessageDigest.getInstance("SHA-256").apply {
                update(codeVerifier.toByteArray(Charsets.UTF_8))
            }.digest()
        )
    }

    /**
     * Returns the actual id_token.
     */
    private fun decryptIdToken(data: TokenResponse, key: SecretKey): JsonWebSignature {
        val json = decryptJWE(data.idToken, key)
        return JsonWebStructure.fromCompactSerialization(
            Json.parseToJsonElement(json)
                .jsonObject["njwt"]?.jsonPrimitive?.content
        ) as JsonWebSignature
    }

    /**
     * Compares the contained nonce with [nonce] after checking the signature of the JWS.
     */
    private fun checkNonce(idTokenPayload: String, nonce: String) {
        require(
            Json.parseToJsonElement(
                idTokenPayload
            ).jsonObject["nonce"]?.jsonPrimitive?.content == nonce
        )
    }

    private fun decryptAccessToken(data: TokenResponse, symmetricalKey: SecretKey): String =
        decryptJWE(data.accessToken, symmetricalKey)

    private fun decryptJWE(encryptedJWE: String, symmetricalKey: SecretKey): String =
        JsonWebEncryption().apply {
            key = symmetricalKey
            compactSerialization = encryptedJWE
        }.payload

    companion object {
        private var isCryptoInitialized = false
        private var cryptoInitializedLock = Mutex()
    }
}
