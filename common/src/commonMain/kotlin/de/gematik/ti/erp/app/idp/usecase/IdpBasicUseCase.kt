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
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
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
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

private val discoveryDocumentMaxValidityMinutes: Int = Duration.ofHours(24).toMinutes().toInt()
private val discoveryDocumentMaxValiditySeconds: Int = Duration.ofHours(24).seconds.toInt()

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

class IdpBasicUseCase(
    private val repository: IdpRepository,
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
            Napier.e("IDP config couldn't be validated", e)
            repository.invalidateConfig()
            // retry
            try {
                repository.loadUncheckedIdpConfiguration().also {
                    checkIdpConfigurationValidity(it, Instant.now())
                }
            } catch (e: Exception) {
                Napier.e("IDP config couldn't be validated again; finally aborting", e)
                repository.invalidateConfig()
                throw e
            }
        }

        // fetch both keys
        val pukSigKey = repository.fetchIdpPukSig(config.pukIdpSigEndpoint).getOrThrow()
        val pukEncKey = repository.fetchIdpPukEnc(config.pukIdpEncEndpoint).getOrThrow()

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

        // final [redirectSsoToken] & [accessToken]
        return IdpAuthFlowResult(
            accessToken = idpTokenResult.decryptedAccessToken,
            ssoToken = redirectSsoToken,
            idTokenInsuranceIdentifier = idTokenJson.jsonObject["idNummer"]?.jsonPrimitive?.content ?: "",
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

        val idpTokenResult = postCodeAndDecryptAccessToken(
            config.tokenEndpoint,
            nonce = nonce,
            codeVerifier = codeVerifier,
            code = codeFromRedirect,
            pukEncKey = pukEncKey,
            pukSigKey = pukSigKey,
            redirectUri = redirectUri
        )

        return IdpRefreshFlowResult(scope, idpTokenResult.decryptedAccessToken)
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

    suspend fun checkIdpConfigurationValidity(config: IdpData.IdpConfiguration, timestamp: Instant) {
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
