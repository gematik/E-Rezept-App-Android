/*
 * Copyright (c) 2024 gematik GmbH
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

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.idp.api.models.Challenge
import de.gematik.ti.erp.app.idp.api.models.IdpDiscoveryInfo
import de.gematik.ti.erp.app.idp.api.models.IdpNonce
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.api.models.IdpState
import de.gematik.ti.erp.app.idp.api.models.JWSPublicKey
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntries
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.idp.api.models.RemoteFederationIdp
import de.gematik.ti.erp.app.idp.api.models.RemoteFederationIdps
import de.gematik.ti.erp.app.idp.api.models.TokenResponse
import de.gematik.ti.erp.app.idp.api.models.UniversalLinkToken
import de.gematik.ti.erp.app.idp.extension.extractNullableQueryParameter
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError.Companion.parseToError
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError.Companion.parsedUriToError
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.vau.extractECPublicKey
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.bouncycastle.cert.X509CertificateHolder
import org.jose4j.base64url.Base64
import org.jose4j.jws.JsonWebSignature
import java.net.HttpURLConnection
import java.net.URI
import java.security.PublicKey

class IdpRepository(
    private val remoteDataSource: IdpRemoteDataSource,
    private val localDataSource: IdpLocalDataSource,
    private val accessTokenDataSource: AccessTokenDataSource
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun decryptedAccessToken(profileId: ProfileIdentifier) = accessTokenDataSource.get(profileId)

    @Requirement(
        "A_21328#1",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Store access token in data structure only."
    )
    fun saveDecryptedAccessToken(profileId: ProfileIdentifier, accessToken: AccessToken) {
        accessTokenDataSource.save(profileId, accessToken)
    }

    suspend fun saveSingleSignOnToken(profileId: ProfileIdentifier, token: IdpData.SingleSignOnTokenScope) {
        localDataSource.saveSingleSignOnToken(profileId, token)
    }

    fun authenticationData(profileId: ProfileIdentifier): Flow<IdpData.AuthenticationData> =
        localDataSource.authenticationData(profileId)

    @Requirement(
        "A_20483",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Fetch challenge from IDP."
    )
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

    suspend fun fetchFederationIDList(
        url: String,
        idpPukSigKey: PublicKey
    ): List<RemoteFederationIdp> {
        val jwtResult = remoteDataSource.fetchExternalAuthorizationIDList(url).getOrThrow()
        return extractFederationIdpList(jwtResult.apply { key = idpPukSigKey }.payload)
    }

    suspend fun fetchIdpPukSig(url: String): Result<JWSPublicKey> =
        remoteDataSource.fetchIdpPukSig(url)

    suspend fun fetchIdpPukEnc(url: String): Result<JWSPublicKey> =
        remoteDataSource.fetchIdpPukEnc(url)

    private fun parseDiscoveryDocumentBody(body: String): IdpDiscoveryInfo =
        json.decodeFromString(body)

    private fun extractFederationIdpList(payload: String): List<RemoteFederationIdp> {
        return json.decodeFromString<RemoteFederationIdps>(payload).items
    }

    private fun extractUncheckedIdpConfiguration(discoveryDocument: JWSDiscoveryDocument): IdpData.IdpConfiguration {
        val x5c = requireNotNull(
            (discoveryDocument.jws.headers?.getObjectHeaderValue("x5c") as? ArrayList<*>)?.firstOrNull() as? String
        ) { "Missing certificate" }
        val certificateHolder = X509CertificateHolder(Base64.decode(x5c))

        discoveryDocument.jws.key = certificateHolder.extractECPublicKey()

        val discoveryDocumentBody = parseDiscoveryDocumentBody(discoveryDocument.jws.payload)

        return IdpData.IdpConfiguration(
            authorizationEndpoint = overwriteEndpoint(discoveryDocumentBody.authorizationUrl),
            ssoEndpoint = overwriteEndpoint(discoveryDocumentBody.ssoUrl),
            tokenEndpoint = overwriteEndpoint(discoveryDocumentBody.tokenUrl),
            pairingEndpoint = discoveryDocumentBody.pairingUrl,
            authenticationEndpoint = overwriteEndpoint(discoveryDocumentBody.authenticationUrl),
            pukIdpEncEndpoint = overwriteEndpoint(discoveryDocumentBody.uriPukIdpEnc),
            pukIdpSigEndpoint = overwriteEndpoint(discoveryDocumentBody.uriPukIdpSig),
            expirationTimestamp = Instant.fromEpochSeconds(discoveryDocumentBody.expirationTime),
            issueTimestamp = Instant.fromEpochSeconds(discoveryDocumentBody.issuedAt),
            certificate = certificateHolder,
            externalAuthorizationIDsEndpoint = overwriteEndpoint(discoveryDocumentBody.healthInsuranceAppV1Url),
            thirdPartyAuthorizationEndpoint = overwriteEndpoint(discoveryDocumentBody.thirdPartyAuthorizationV1Url),
            federationAuthorizationIDsEndpoint = overwriteEndpoint(discoveryDocumentBody.healthInsuranceAppV2Url),
            federationAuthorizationEndpoint = overwriteEndpoint(discoveryDocumentBody.thirdPartyAuthorizationV2Url)
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

    suspend fun authorizeExternalHealthInsuranceAppDataAsGiD(
        url: String,
        token: UniversalLinkToken
    ): Result<String> =
        remoteDataSource.authorizeExternalAppDataWithGid(
            url = url,
            code = token.code,
            state = token.state
        )

    @Requirement(
        "A_20186",
        "A_21326",
        "A_21327",
        "A_20499-01",
        "A_21603",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Invalidate/delete session data upon logout. " +
            "since we have automatic memory management, we can't delete the token. " +
            "Due to the use of frameworks we have sensitive data as immutable objects and hence " +
            "cannot override it"
    )
    @Requirement(
        "O.Tokn_6#4",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "invalidate config and token "
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
        accessTokenDataSource.delete(profileId)
    }

    /**
     * @return URI as a result object
     * @throws GematikResponseError as a result object
     * @param url authentication url
     * @param state initial state
     * @param codeChallenge initial code challenge
     * @param nonce initial nonce
     * @param externalAppId universalLinkIdp authenticatorId
     * @param idpScope default scope
     */
    suspend fun getGidAuthorizationRedirect(
        url: String,
        state: IdpState,
        codeChallenge: String,
        nonce: IdpNonce,
        externalAppId: String,
        idpScope: IdpScope
    ): Result<URI> {
        try {
            val response = remoteDataSource.getGidAuthorizationRedirectUrl(
                url = url,
                externalAppId = externalAppId,
                codeChallenge = codeChallenge,
                nonce = nonce.nonce,
                state = state.state,
                isPairingScope = idpScope == IdpScope.BiometricPairing
            )
            if (response.code() == HttpURLConnection.HTTP_MOVED_TEMP) {
                val headers = response.headers()
                val redirectUri = requireNotNull(headers["Location"]) {
                    "Missing parameters to get redirect uri"
                }
                val parsedUri = URI(redirectUri)
                return if (parsedUri.extractNullableQueryParameter("error") != null) {
                    val error = parsedUri.parsedUriToError()
                    Napier.e { "error on gid response ${error.gematikErrorText}" }
                    Result.failure(error)
                } else {
                    Napier.d { "success on gid response $parsedUri" }
                    Result.success(parsedUri)
                }
            } else {
                Napier.e { "error on gid response, wrong response code ${response.code()}" }
                val error = response.body().toString()
                return Result.failure(error.parseToError())
            }
        } catch (e: Throwable) {
            Napier.e { "failure on gid response ${e.message}" }
            return Result.failure(GematikResponseError.emptyResponseError(e.message))
        }
    }
}

@JvmInline
value class JWSDiscoveryDocument(val jws: JsonWebSignature)
