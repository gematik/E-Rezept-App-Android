/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.idp.repository

import de.gematik.ti.erp.app.idp.api.models.Challenge
import de.gematik.ti.erp.app.idp.api.models.IdpDiscoveryInfo
import de.gematik.ti.erp.app.vau.extractECPublicKey
import java.time.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bouncycastle.cert.X509CertificateHolder
import org.jose4j.base64url.Base64
import org.jose4j.jws.JsonWebSignature

@JvmInline
value class JWSDiscoveryDocument(val jws: JsonWebSignature)

data class SingleSignOnToken(val token: String, val scope: Scope = Scope.Default) {
    enum class Scope {
        Default,
        AlternateAuthentication
    }
}

class IdpRepository(
    private val remoteDataSource: IdpRemoteDataSource,
    private val localDataSource: IdpLocalDataSource,
    private val json: Json
) {
    var decryptedAccessToken: String? = null
        set(v) {
            field = if (v?.isBlank() == true) null else v
        }

    var cardAccessNumber: String? = null

    suspend fun getSingleSignOnToken() = localDataSource.loadIdpAuthData().let { entity ->
        entity.singleSignOnToken?.let { token ->
            entity.singleSignOnTokenScope?.let { scope ->
                SingleSignOnToken(token, scope)
            }
        }
    }

    suspend fun setSingleSignOnToken(token: SingleSignOnToken) =
        localDataSource.saveSingleSignOnToken(token.token, token.scope)

    suspend fun getSingleSignOnTokenScope() =
        localDataSource.loadIdpAuthData().singleSignOnTokenScope

    suspend fun fetchChallenge(
        url: String,
        codeChallenge: String,
        state: String,
        nonce: String,
        isDeviceRegistration: Boolean = false
    ): Result<Challenge> =
        remoteDataSource.fetchChallenge(url, codeChallenge, state, nonce, isDeviceRegistration)

    /**
     * Returns an unchecked and possible invalid idp configuration parsed from the discovery document.
     */
    suspend fun loadUncheckedIdpConfiguration(): IdpConfiguration {
        return localDataSource.loadIdpInfo() ?: run {
            extractUncheckedIdpConfiguration(
                remoteDataSource.fetchDiscoveryDocument().getOrThrow()
            ).also { localDataSource.saveIdpInfo(it) }
        }
    }

    suspend fun postSignedChallenge(url: String, signedChallenge: String): Result<String> =
        remoteDataSource.postChallenge(url, signedChallenge)

    suspend fun postUnsignedChallengeWithSso(
        url: String,
        ssoToken: String,
        unsignedChallenge: String
    ): Result<String> =
        remoteDataSource.postChallenge(url, ssoToken, unsignedChallenge)

    suspend fun postToken(
        url: String,
        keyVerifier: String,
        code: String
    ) =
        remoteDataSource.postToken(
            url,
            keyVerifier = keyVerifier,
            code = code
        )

    suspend fun fetchIdpPukSig(url: String) =
        remoteDataSource.fetchIdpPukSig(url)

    suspend fun fetchIdpPukEnc(url: String) =
        remoteDataSource.fetchIdpPukEnc(url)

    @OptIn(ExperimentalSerializationApi::class)
    private fun parseDiscoveryDocumentBody(body: String): IdpDiscoveryInfo =
        requireNotNull(json.decodeFromString(body)) { "Couldn't parse discovery document" }

    fun extractUncheckedIdpConfiguration(discoveryDocument: JWSDiscoveryDocument): IdpConfiguration {
        val x5c = requireNotNull(
            (discoveryDocument.jws.headers?.getObjectHeaderValue("x5c") as? ArrayList<*>)?.firstOrNull() as? String
        ) { "Missing certificate" }
        val certificateHolder = X509CertificateHolder(Base64.decode(x5c))

        discoveryDocument.jws.key = certificateHolder.extractECPublicKey()

        val discoveryDocumentBody = parseDiscoveryDocumentBody(discoveryDocument.jws.payload)

        return IdpConfiguration(
            authorizationEndpoint = overwriteEndpoint(discoveryDocumentBody.authorizationURL),
            ssoEndpoint = overwriteEndpoint(discoveryDocumentBody.ssoURL),
            tokenEndpoint = overwriteEndpoint(discoveryDocumentBody.tokenURL),
            pairingEndpoint = discoveryDocumentBody.pairingURL,
            authenticationEndpoint = overwriteEndpoint(discoveryDocumentBody.authenticationURL),
            pukIdpEncEndpoint = overwriteEndpoint(discoveryDocumentBody.uriPukIdpEnc),
            pukIdpSigEndpoint = overwriteEndpoint(discoveryDocumentBody.uriPukIdpSig),
            expirationTimestamp = convertTimeStampTo(discoveryDocumentBody.expirationTime),
            issueTimestamp = convertTimeStampTo(discoveryDocumentBody.issuedAt),
            certificate = certificateHolder
        )
    }

    private fun convertTimeStampTo(timeStamp: Long) =
        Instant.ofEpochSecond(timeStamp)

    private fun overwriteEndpoint(oldEndpoint: String) =
        oldEndpoint.replace(".zentral.idp.splitdns.ti-dienste.de", ".app.ti-dienste.de")

    suspend fun invalidate() {
        invalidateConfig()
        invalidateDecryptedAccessToken()
        localDataSource.clearIdpAuthData()
    }

    suspend fun invalidateConfig() {
        localDataSource.clearIdpInfo()
    }

    suspend fun invalidateWithUserCredentials() {
        invalidate()

        cardAccessNumber = "" // TODO better handling
    }

    suspend fun invalidateSingleSignOnTokenRetainingScope() =
        localDataSource.saveSingleSignOnToken(null)

    fun invalidateDecryptedAccessToken() {
        decryptedAccessToken = null
    }
}
