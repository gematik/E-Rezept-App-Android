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

package de.gematik.ti.erp.app.idp.usecase

import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.api.unwrap
import de.gematik.ti.erp.app.db.entities.IdpConfiguration
import de.gematik.ti.erp.app.generateRandomAES256Key
import de.gematik.ti.erp.app.idp.EllipticCurvesExtending
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.api.REDIRECT_URI
import de.gematik.ti.erp.app.idp.api.models.JWSPublicKey
import de.gematik.ti.erp.app.idp.api.models.TokenResponse
import de.gematik.ti.erp.app.idp.buildJsonWebSignatureWithHealthCard
import de.gematik.ti.erp.app.idp.buildKeyVerifier
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.secureRandomInstance
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import de.gematik.ti.erp.app.vau.usecase.checkIdpCertificate
import java.net.URI
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Security
import java.security.interfaces.ECPublicKey
import java.time.Duration
import java.time.Instant
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import org.json.JSONObject
import timber.log.Timber

private val discoveryDocumentMaxValidityMinutes: Int = Duration.ofHours(24).toMinutes().toInt()
private val discoveryDocumentMaxValiditySeconds: Int = Duration.ofHours(24).seconds.toInt()

enum class IdpScope {
    Default,
    BiometricPairing
}

data class IdpChallengeFlowResult(
    val scope: IdpScope,
    val challenge: IdpUnsignedChallenge,
)

data class IdpAuthFlowResult(
    val accessToken: String,
    val ssoToken: String,
    val idTokenInsurantName: String,
    val idTokenInsuranceIdentifier: String,
    val idTokenInsuranceName: String
)

data class IdpRefreshFlowResult(
    val scope: IdpScope,
    val accessToken: String
)

data class IdpInitialData(
    val config: IdpConfiguration,
    val pukSigKey: JWSPublicKey,
    val pukEncKey: JWSPublicKey,
    val state: IdpState,
    val nonce: IdpNonce,
    val codeVerifier: String,
    val codeChallenge: String,
)

data class IdpUnsignedChallenge(
    val signedChallenge: String, // raw jws
    val challenge: String, // payload extracted from the jws
    val expires: Long // expiry timestamp parsed from challenge
)

data class IdpTokenResult(
    val decryptedAccessToken: String,
    val idTokenPayload: String,
)

@JvmInline
value class IdpState(val state: String) {
    operator fun component1(): String = state

    companion object {
        fun create(outLength: Int = 32) = IdpState(generateRandomUrlSafeStringSecure(outLength))
    }
}

@JvmInline
value class IdpNonce(val nonce: String) {
    operator fun component1(): String = nonce

    companion object {
        fun create() = IdpNonce(
            generateRandomUrlSafeStringSecure(32)
        )
    }
}

internal fun generateRandomUrlSafeStringSecure(outLength: Int = 32): String {
    require(outLength >= 1)
    val chars = Base64Url.encode(
        ByteArray((outLength / 4 + 1) * 3).apply {
            secureRandomInstance().nextBytes(this)
        }
    )
    return chars.substring(0 until outLength)
}

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

@Singleton
class IdpBasicUseCase @Inject constructor(
    private val repository: IdpRepository,
    private val profilesRepository: ProfilesRepository,
    private val truststoreUseCase: TruststoreUseCase
) {

    // ////////////////////////////////////////////////////////////////////////////////////////

    suspend fun initializeConfigurationAndKeys(): IdpInitialData {
        cryptoInitializedLock.withLock {
            if (!isCryptoInitialized) {
                Security.removeProvider("BC")
                Security.insertProviderAt(BCProvider, 1)
                EllipticCurvesExtending.init()
                isCryptoInitialized = true
            }
        }

        val config = try {
            repository.loadUncheckedIdpConfiguration().also {
                checkIdpConfigurationValidity(it, Instant.now())
            }
        } catch (e: Exception) {
            Timber.e(e, "IDP config couldn't be validated")
            repository.invalidateConfig()
            // retry
            try {
                repository.loadUncheckedIdpConfiguration().also {
                    checkIdpConfigurationValidity(it, Instant.now())
                }
            } catch (e: Exception) {
                Timber.e(e, "IDP config couldn't be validated again; finally aborting")
                repository.invalidateConfig()
                throw e
            }
        }

        // fetch both keys
        val pukSigKey = repository.fetchIdpPukSig(config.pukIdpSigEndpoint).unwrap()
        val pukEncKey = repository.fetchIdpPukEnc(config.pukIdpEncEndpoint).unwrap()

        // check signature key with truststore
        val certPubKey = pukSigKey.jws.leafCertificate.publicKey as ECPublicKey
        val pubKey = pukSigKey.jws.publicKey as ECPublicKey
        require(certPubKey.w == pubKey.w) { "Public key of idpPukSig doesn't match with its contained certificate" }
        truststoreUseCase.checkIdpCertificate(pukSigKey.jws.leafCertificate)

        // generate state & nonce used to verify the integrity of certain calls
        val state = IdpState.create()
        val nonce = IdpNonce.create()

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

    suspend fun challengeFlow(
        initialData: IdpInitialData,
        scope: IdpScope,
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
            pukSigKey = pukSigKey
        )

        return IdpChallengeFlowResult(
            scope = scope,
            challenge = challenge
        )
    }

    suspend fun basicAuthFlow(
        initialData: IdpInitialData,
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
            pukSigKey = pukSigKey
        )

        val idTokenJson = JSONObject(
            idpTokenResult.idTokenPayload
        )

        // final [redirectSsoToken] & [accessToken]
        return IdpAuthFlowResult(
            accessToken = idpTokenResult.decryptedAccessToken,
            ssoToken = redirectSsoToken,
            idTokenInsuranceIdentifier = idTokenJson.getStringOrNull("idNummer") ?: "",
            idTokenInsuranceName = idTokenJson.getStringOrNull("organizationName") ?: "",
            idTokenInsurantName = idTokenJson.getStringOrNull("given_name")?.let { it + " " + idTokenJson.getString("family_name") } ?: ""
        )
    }

    suspend fun refreshAccessTokenWithSsoFlow(
        initialData: IdpInitialData,
        scope: IdpScope,
        ssoToken: String
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
            pukSigKey = pukSigKey
        )

        val redirect = postUnsignedChallengeWithSsoTokenAndGetRedirect(
            config.ssoEndpoint,
            // we post an unsigned (i.e., we didn't sign the challenge, which is signed by the idp, again) to the sso response endpoint
            unsignedCodeChallenge = unsignedChallenge.signedChallenge,
            ssoToken = ssoToken,
            state = state
        )

        val codeFromRedirect = IdpService.extractQueryParameter(redirect, "code")

        val idpTokenResult = postCodeAndDecryptAccessToken(
            config.tokenEndpoint,
            nonce = nonce,
            codeVerifier = codeVerifier,
            code = codeFromRedirect,
            pukEncKey = pukEncKey,
            pukSigKey = pukSigKey
        )

        return IdpRefreshFlowResult(scope, idpTokenResult.decryptedAccessToken)
    }

    // ////////////////////////////////////////////////////////////////////////////////////////

    suspend fun postSignedChallengeAndGetRedirect(
        url: String,
        codeChallenge: JsonWebEncryption,
        state: IdpState,
    ): URI {
        val redirect = when (
            val r =
                repository.postSignedChallenge(url, codeChallenge.compactSerialization)
        ) {
            is Result.Success -> {
                URI(r.data)
            }
            is Result.Error -> throw r.exception
        }

        val redirectState = IdpService.extractQueryParameter(redirect, "state")
        require(state.state == redirectState) { "Invalid state" }

        return redirect
    }

    suspend fun postUnsignedChallengeWithSsoTokenAndGetRedirect(
        url: String,
        unsignedCodeChallenge: String,
        ssoToken: String,
        state: IdpState,
    ): URI {
        val redirect = when (
            val r =
                repository.postUnsignedChallengeWithSso(url, ssoToken, unsignedCodeChallenge)
        ) {
            is Result.Success -> {
                URI(r.data)
            }
            is Result.Error -> throw r.exception
        }

        val redirectState = IdpService.extractQueryParameter(redirect, "state")
        require(state.state == redirectState) { "Invalid state" }

        return redirect
    }

    suspend fun fetchAndCheckUnsignedChallenge(
        url: String,
        codeChallenge: String,
        state: IdpState,
        nonce: IdpNonce,
        scope: IdpScope,
        pukSigKey: JWSPublicKey
    ): IdpUnsignedChallenge {
        val signedChallenge = when (
            val r = repository.fetchChallenge(
                url = url,
                codeChallenge = codeChallenge,
                state = state.state,
                nonce = nonce.nonce,
                isDeviceRegistration = scope == IdpScope.BiometricPairing
            )
        ) {
            is Result.Success -> {
                r.data.challenge.jws.apply {
                    key = pukSigKey.jws.publicKey
                }
                r.data.challenge
            }
            is Result.Error -> throw r.exception
        }

        // check state & nonce
        val unsignedChallenge = signedChallenge.jws.payload
        val unsignedChallengeJson = JSONObject(unsignedChallenge)
        require(state.state == unsignedChallengeJson["state"] as String) { "Invalid state" }
        require(nonce.nonce == unsignedChallengeJson["nonce"] as String) { "Invalid nonce" }

        val unsignedChallengeExpires = (unsignedChallengeJson["exp"] as Int).toLong()

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
        redirectUri: String = REDIRECT_URI
    ): IdpTokenResult {
        val symmetricalKey = generateRandomAES256Key()

        val keyVerifier = buildKeyVerifier(
            symmetricalKey,
            codeVerifier,
            pukEncKey.jws.publicKey
        )

        return when (
            val r = repository.postToken(
                url = url,
                keyVerifier = keyVerifier.compactSerialization,
                code = code,
                redirectUri = redirectUri
            )
        ) {
            is Result.Success -> {
                val decryptedIdToken = decryptIdToken(r.data, symmetricalKey)
                val idTokenPayload = decryptedIdToken.apply {
                    key = pukSigKey.jws.publicKey
                }.payload
                checkNonce(
                    idTokenPayload,
                    nonce.nonce
                )

                val json = decryptAccessToken(r.data, symmetricalKey)
                IdpTokenResult(
                    decryptedAccessToken = JSONObject(json)["njwt"] as String,
                    idTokenPayload = idTokenPayload
                )
            }
            is Result.Error -> throw r.exception
        }
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

    fun generateCodeVerifier(): String {
        // https://datatracker.ietf.org/doc/html/rfc7636#section-4.1
        // 60 bytes are about 80 characters of base64
        return Base64Url.encode(
            ByteArray(60).apply {
                secureRandomInstance().nextBytes(this)
            }
        )
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        // https://datatracker.ietf.org/doc/html/rfc7636#section-4.2
        return Base64Url.encode(
            MessageDigest.getInstance("SHA-256").apply {
                update(codeVerifier.toByteArray(Charsets.UTF_8))
            }.digest()
        )
    }

    suspend fun checkIdpConfigurationValidity(config: IdpConfiguration, timestamp: Instant) {
        truststoreUseCase.checkIdpCertificate(config.certificate, true)

        val claims = JwtClaims().apply {
            issuedAt = NumericDate.fromMilliseconds(config.issueTimestamp.toEpochMilli())
            expirationTime = NumericDate.fromMilliseconds(config.expirationTimestamp.toEpochMilli())
        }

        val r = NumericDateValidator().apply {
            setAllowedClockSkewSeconds(60)
            setEvaluationTime(NumericDate.fromMilliseconds(timestamp.toEpochMilli()))
            setRequireExp(true)
            setRequireIat(true)
            setIatAllowedSecondsInThePast(discoveryDocumentMaxValiditySeconds)
            setMaxFutureValidityInMinutes(discoveryDocumentMaxValidityMinutes)
        }.validate(JwtContext(claims, emptyList()))

        require(r == null) { "IDP configuration couldn't be validated: ${r.errorMessage}" }
    }

    /**
     * Returns the actual id_token.
     */
    private fun decryptIdToken(data: TokenResponse, key: SecretKey): JsonWebSignature {
        val json = decryptJWE(data.idToken, key)
        return JsonWebStructure.fromCompactSerialization(JSONObject(json).getString("njwt")) as JsonWebSignature
    }

    /**
     * Compares the contained nonce with [nonce] after checking the signature of the JWS.
     */
    private fun checkNonce(idTokenPayload: String, nonce: String) {
        require(
            JSONObject(
                idTokenPayload
            ).getString("nonce") == nonce
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

fun JSONObject.getStringOrNull(name: String) =
    if (has(name)) getString(name) else null
