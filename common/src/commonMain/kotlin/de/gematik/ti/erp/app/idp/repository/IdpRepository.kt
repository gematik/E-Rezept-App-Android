/*
 * Copyright (c) 2023 gematik GmbH
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

import de.gematik.ti.erp.app.idp.api.models.AuthenticationId
import de.gematik.ti.erp.app.idp.api.models.AuthenticationIdList
import de.gematik.ti.erp.app.idp.api.models.Challenge
import de.gematik.ti.erp.app.idp.api.models.ExternalAuthorizationData
import de.gematik.ti.erp.app.idp.api.models.IdpDiscoveryInfo
import de.gematik.ti.erp.app.idp.api.models.IdpNonce
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.api.models.IdpState
import de.gematik.ti.erp.app.idp.api.models.JWSPublicKey
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntries
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.idp.api.models.TokenResponse
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.vau.extractECPublicKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bouncycastle.cert.X509CertificateHolder
import org.jose4j.base64url.Base64
import org.jose4j.jws.JsonWebSignature
import java.security.PublicKey
import java.time.Instant

@JvmInline
value class JWSDiscoveryDocument(val jws: JsonWebSignature)

class IdpRepository constructor(
    private val remoteDataSource: IdpRemoteDataSource,
    private val localDataSource: IdpLocalDataSource
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val decryptedAccessTokenMap: MutableStateFlow<Map<String, String>> = MutableStateFlow(mutableMapOf())

    fun decryptedAccessToken(profileId: ProfileIdentifier) =
        decryptedAccessTokenMap.map { it[profileId] }.distinctUntilChanged()

    fun saveDecryptedAccessToken(profileId: ProfileIdentifier, accessToken: String) {
        decryptedAccessTokenMap.update {
            it + (profileId to accessToken)
        }
    }

    suspend fun saveSingleSignOnToken(profileId: ProfileIdentifier, token: IdpData.SingleSignOnTokenScope) {
        localDataSource.saveSingleSignOnToken(profileId, token)
    }

    fun authenticationData(profileId: ProfileIdentifier): Flow<IdpData.AuthenticationData> =
        localDataSource.authenticationData(profileId)

    suspend fun fetchChallenge(
        url: String,
        codeChallenge: String,
        state: String,
        nonce: String,
        isDeviceRegistration: Boolean,
        redirectUri: String
    ): Result<Challenge> =
        remoteDataSource.fetchChallenge(
            url = url,
            codeChallenge = codeChallenge,
            state = state,
            nonce = nonce,
            isDeviceRegistration = isDeviceRegistration,
            redirectUri = redirectUri
        )

    /**
     * Returns an unchecked and possible invalid idp configuration parsed from the discovery document.
     */
    suspend fun loadUncheckedIdpConfiguration(): IdpData.IdpConfiguration {
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
        code: String,
        redirectUri: String
    ): Result<TokenResponse> =
        remoteDataSource.postToken(
            url,
            keyVerifier = keyVerifier,
            code = code,
            redirectUri = redirectUri
        )

    suspend fun fetchExternalAuthorizationIDList(
        url: String,
        idpPukSigKey: PublicKey
    ): List<AuthenticationId> {
        val jwtResult = remoteDataSource.fetchExternalAuthorizationIDList(url).getOrThrow()

        return extractAuthenticationIDList(jwtResult.apply { key = idpPukSigKey }.payload)
    }

    suspend fun fetchIdpPukSig(url: String): Result<JWSPublicKey> =
        remoteDataSource.fetchIdpPukSig(url)

    suspend fun fetchIdpPukEnc(url: String): Result<JWSPublicKey> =
        remoteDataSource.fetchIdpPukEnc(url)

    private fun parseDiscoveryDocumentBody(body: String): IdpDiscoveryInfo =
        json.decodeFromString(body)

    private fun extractAuthenticationIDList(payload: String): List<AuthenticationId> {
        return json.decodeFromString<AuthenticationIdList>(payload).authenticationList
    }

    private fun extractUncheckedIdpConfiguration(discoveryDocument: JWSDiscoveryDocument): IdpData.IdpConfiguration {
        val x5c = requireNotNull(
            (discoveryDocument.jws.headers?.getObjectHeaderValue("x5c") as? ArrayList<*>)?.firstOrNull() as? String
        ) { "Missing certificate" }
        val certificateHolder = X509CertificateHolder(Base64.decode(x5c))

        discoveryDocument.jws.key = certificateHolder.extractECPublicKey()

        val discoveryDocumentBody = parseDiscoveryDocumentBody(discoveryDocument.jws.payload)

        return IdpData.IdpConfiguration(
            authorizationEndpoint = overwriteEndpoint(discoveryDocumentBody.authorizationURL),
            ssoEndpoint = overwriteEndpoint(discoveryDocumentBody.ssoURL),
            tokenEndpoint = overwriteEndpoint(discoveryDocumentBody.tokenURL),
            pairingEndpoint = discoveryDocumentBody.pairingURL,
            authenticationEndpoint = overwriteEndpoint(discoveryDocumentBody.authenticationURL),
            pukIdpEncEndpoint = overwriteEndpoint(discoveryDocumentBody.uriPukIdpEnc),
            pukIdpSigEndpoint = overwriteEndpoint(discoveryDocumentBody.uriPukIdpSig),
            expirationTimestamp = Instant.ofEpochSecond(discoveryDocumentBody.expirationTime),
            issueTimestamp = Instant.ofEpochSecond(discoveryDocumentBody.issuedAt),
            certificate = certificateHolder,
            externalAuthorizationIDsEndpoint = overwriteEndpoint(discoveryDocumentBody.krankenkassenAppURL),
            thirdPartyAuthorizationEndpoint = overwriteEndpoint(discoveryDocumentBody.thirdPartyAuthorizationURL)
        )
    }

    private fun overwriteEndpoint(oldEndpoint: String?) =
        oldEndpoint?.replace(".zentral.idp.splitdns.ti-dienste.de", ".app.ti-dienste.de") ?: ""

    suspend fun postPairing(
        url: String,
        encryptedRegistrationData: String,
        token: String
    ): Result<PairingResponseEntry> =
        remoteDataSource.postPairing(
            url,
            token = token,
            encryptedRegistrationData = encryptedRegistrationData
        )

    suspend fun getPairing(
        url: String,
        token: String
    ): Result<PairingResponseEntries> =
        remoteDataSource.getPairing(
            url,
            token = token
        )

    suspend fun deletePairing(
        url: String,
        token: String,
        alias: String
    ): Result<Unit> =
        remoteDataSource.deletePairing(
            url = url,
            token = token,
            alias = alias
        )

    suspend fun postBiometricAuthenticationData(
        url: String,
        encryptedSignedAuthenticationData: String
    ): Result<String> =
        remoteDataSource.authorizeBiometric(url, encryptedSignedAuthenticationData)

    suspend fun postExternAppAuthorizationData(
        url: String,
        externalAuthorizationData: ExternalAuthorizationData
    ): Result<String> =
        remoteDataSource.authorizeExtern(
            url = url,
            externalAuthorizationData = externalAuthorizationData
        )

    suspend fun invalidate(profileId: ProfileIdentifier) {
        invalidateConfig()
        invalidateDecryptedAccessToken(profileId)
        localDataSource.invalidateAuthenticationData(profileId)
    }

    suspend fun invalidateConfig() {
        localDataSource.invalidateConfiguration()
    }

    suspend fun invalidateSingleSignOnTokenRetainingScope(profileId: ProfileIdentifier) {
        localDataSource.invalidateSingleSignOnTokenRetainingScope(profileId)
        invalidateDecryptedAccessToken(profileId)
    }

    fun invalidateDecryptedAccessToken(profileId: ProfileIdentifier) {
        decryptedAccessTokenMap.update {
            it - profileId
        }
    }

    suspend fun getAuthorizationRedirect(
        url: String,
        state: IdpState,
        codeChallenge: String,
        nonce: IdpNonce,
        kkAppId: String,
        scope: IdpScope
    ): String {
        return remoteDataSource.requestAuthorizationRedirect(
            url = url,
            externalAppId = kkAppId,
            codeChallenge = codeChallenge,
            nonce = nonce.nonce,
            state = state.state,
            isPairingScope = scope == IdpScope.BiometricPairing
        ).getOrThrow()
    }
}
