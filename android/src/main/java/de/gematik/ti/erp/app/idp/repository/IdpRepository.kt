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

package de.gematik.ti.erp.app.idp.repository

import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.db.entities.IdpAuthenticationDataEntity
import de.gematik.ti.erp.app.db.entities.IdpConfiguration
import de.gematik.ti.erp.app.idp.api.REDIRECT_URI
import de.gematik.ti.erp.app.idp.api.models.AuthenticationID
import de.gematik.ti.erp.app.idp.api.models.AuthenticationIDList
import de.gematik.ti.erp.app.idp.api.models.AuthorizationRedirectInfo
import de.gematik.ti.erp.app.idp.api.models.Challenge
import de.gematik.ti.erp.app.idp.api.models.IdpDiscoveryInfo
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntries
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.idp.usecase.IdpNonce
import de.gematik.ti.erp.app.idp.usecase.IdpState
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.vau.extractECPublicKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.bouncycastle.cert.X509CertificateHolder
import org.jose4j.base64url.Base64
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.JsonWebStructure
import java.security.KeyStore
import java.security.PublicKey
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private const val ssoTokenPrefKey = "ssoToken" // TODO remove within migration
private const val cardAccessNumberPrefKey = "cardAccessNumber"

@JvmInline
value class JWSDiscoveryDocument(val jws: JsonWebSignature)

sealed class SingleSignOnToken {
    abstract val expiresOn: Instant
    abstract val validOn: Instant

    fun isValid(instant: Instant = Instant.now()) =
        instant < expiresOn && instant >= validOn

    fun tokenOrNull(): String? =
        when (this) {
            is AlternateAuthenticationToken -> this.token
            is AlternateAuthenticationWithoutToken -> null
            is DefaultToken -> this.token
        }

    data class DefaultToken(
        val token: String,
        override val expiresOn: Instant = extractExpirationTimestamp(token),
        override val validOn: Instant = extractValidOnTimestamp(token),
    ) : SingleSignOnToken()

    data class AlternateAuthenticationToken(
        val token: String,
        override val expiresOn: Instant = extractExpirationTimestamp(token),
        override val validOn: Instant = extractValidOnTimestamp(token),
    ) : SingleSignOnToken()

    data class AlternateAuthenticationWithoutToken(
        override val expiresOn: Instant = Instant.MIN,
        override val validOn: Instant = Instant.MIN,
    ) : SingleSignOnToken()
}

fun extractExpirationTimestamp(ssoToken: String): Instant =
    Instant.ofEpochSecond(
        JsonWebStructure
            .fromCompactSerialization(ssoToken)
            .headers
            .getLongHeaderValue("exp")
    )

fun extractValidOnTimestamp(ssoToken: String): Instant =
    extractExpirationTimestamp(ssoToken) - Duration.ofHours(24)

@Singleton
class IdpRepository @Inject constructor(
    moshi: Moshi,
    private val remoteDataSource: IdpRemoteDataSource,
    private val localDataSource: IdpLocalDataSource
) {
    private val discoveryDocumentBodyAdapter = moshi.adapter(IdpDiscoveryInfo::class.java)
    private val authenticationIDAdapter = moshi.adapter(AuthenticationIDList::class.java)
    private val authorizationRedirectInfoAdapter =
        moshi.adapter(AuthorizationRedirectInfo::class.java)

    val decryptedAccessTokenMap: MutableStateFlow<Map<String, String?>> = MutableStateFlow(mutableMapOf())

    fun decryptedAccessToken(profileName: String) =
        decryptedAccessTokenMap.map { it[profileName] }.distinctUntilChanged()

    suspend fun setCardAccessNumber(profileName: String, can: String?) {
        require(can?.isNotEmpty() ?: true)
        localDataSource.setCardAccessNumber(profileName, can)
    }

    fun updateDecryptedAccessTokenMap(currentName: String, updatedName: String) {
        decryptedAccessTokenMap.update {
            val token = it[currentName]
            it - currentName + (updatedName to token)
        }
    }

    fun cardAccessNumber(profileName: String) =
        localDataSource.cardAccessNumber(profileName)

    suspend fun getSingleSignOnToken(profileName: String) = localDataSource.loadIdpAuthData(profileName).map { entity ->
        when (entity.singleSignOnTokenScope) {
            IdpAuthenticationDataEntity.SingleSignOnTokenScope.Default ->
                entity.singleSignOnToken?.let { token ->
                    SingleSignOnToken.DefaultToken(
                        token = token,
                        expiresOn = entity.singleSignOnTokenExpiresOn
                            ?: extractExpirationTimestamp(token), // scope & token present; this must be not null
                        validOn = entity.singleSignOnTokenValidOn
                            ?: extractValidOnTimestamp(token), // scope & token present; this must be not null
                    )
                }
            IdpAuthenticationDataEntity.SingleSignOnTokenScope.AlternateAuthentication ->
                entity.singleSignOnToken?.let { token ->
                    SingleSignOnToken.AlternateAuthenticationToken(
                        token = token,
                        expiresOn = entity.singleSignOnTokenExpiresOn
                            ?: extractExpirationTimestamp(token), // scope & token present; this must be not null
                        validOn = entity.singleSignOnTokenValidOn
                            ?: extractValidOnTimestamp(token), // scope & token present; this must be not null
                    )
                } ?: SingleSignOnToken.AlternateAuthenticationWithoutToken()
            else -> null
        }
    }

    suspend fun setSingleSignOnToken(profileName: String, token: SingleSignOnToken) {
        val actualToken = when (token) {
            is SingleSignOnToken.AlternateAuthenticationToken -> token.token
            is SingleSignOnToken.DefaultToken -> token.token
            is SingleSignOnToken.AlternateAuthenticationWithoutToken -> null
        }

        val actualTokenScope = when (token) {
            is SingleSignOnToken.AlternateAuthenticationWithoutToken,
            is SingleSignOnToken.AlternateAuthenticationToken ->
                IdpAuthenticationDataEntity.SingleSignOnTokenScope.AlternateAuthentication
            is SingleSignOnToken.DefaultToken ->
                IdpAuthenticationDataEntity.SingleSignOnTokenScope.Default
        }

        localDataSource.saveSingleSignOnToken(
            profileName = profileName,
            token = actualToken,
            scope = actualTokenScope,
            validOn = token.validOn,
            expiresOn = token.expiresOn
        )
        if (token.isValid()) {
            localDataSource.updateLastAuthenticated(token.validOn, profileName)
        }
    }

    suspend fun getHealthCardCertificate(profileName: String) =
        localDataSource.loadIdpAuthData(profileName).map { it.healthCardCertificate }

    suspend fun setHealthCardCertificate(profileName: String, cert: ByteArray) =
        localDataSource.saveHealthCardCertificate(profileName, cert)

    suspend fun getSingleSignOnTokenScope(profileName: String) =
        localDataSource.loadIdpAuthData(profileName).map { it.singleSignOnTokenScope }

    suspend fun getAliasOfSecureElementEntry(profileName: String) =
        localDataSource.loadIdpAuthData(profileName).map { it.aliasOfSecureElementEntry }

    suspend fun setAliasOfSecureElementEntry(profileName: String, alias: ByteArray) {
        require(alias.size == 32)
        localDataSource.saveSecureElementAlias(profileName, alias)
    }

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
        code: String,
        redirectUri: String = REDIRECT_URI
    ) =
        remoteDataSource.postToken(
            url,
            keyVerifier = keyVerifier,
            code = code,
            redirectUri = redirectUri
        )

    suspend fun fetchExternalAuthorizationIDList(
        url: String,
        idpPukSigKey: PublicKey,
    ): List<AuthenticationID> {
        val jwtResult = remoteDataSource.fetchExternalAuthorizationIDList(url).getOrThrow()

        return extractAuthenticationIDList(jwtResult.apply { key = idpPukSigKey }.payload)
    }

    suspend fun fetchIdpPukSig(url: String) =
        remoteDataSource.fetchIdpPukSig(url)

    suspend fun fetchIdpPukEnc(url: String) =
        remoteDataSource.fetchIdpPukEnc(url)

    private fun parseDiscoveryDocumentBody(body: String): IdpDiscoveryInfo =
        requireNotNull(discoveryDocumentBodyAdapter.fromJson(body)) { "Couldn't parse discovery document" }

    fun extractAuthenticationIDList(payload: String): List<AuthenticationID> {
        // TODO: check certificate
        return requireNotNull(authenticationIDAdapter.fromJson(payload)) { "Couldn't parse Authentication List" }.authenticationIDList
    }

    fun extractAuthorizationRedirectInfo(payload: String): AuthorizationRedirectInfo {
        // TODO: check certificate
        return requireNotNull(authorizationRedirectInfoAdapter.fromJson(payload)) { "Couldn't parse AuthorizationRedirectInfo" }
    }

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
            certificate = certificateHolder,
            externalAuthorizationIDsEndpoint = overwriteEndpoint(discoveryDocumentBody.krankenkassenAppURL),
            thirdPartyAuthorizationEndpoint = overwriteEndpoint(discoveryDocumentBody.thirdPartyAuthorizationURL)
        )
    }

    private fun convertTimeStampTo(timeStamp: Long) =
        Instant.ofEpochSecond(timeStamp)

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
            token = token,
        )

    suspend fun postBiometricAuthenticationData(
        url: String,
        encryptedSignedAuthenticationData: String
    ): Result<String> =
        remoteDataSource.authorizeBiometric(url, encryptedSignedAuthenticationData)

    suspend fun postExternAppAuthorizationData(
        url: String,
        externalAuthorizationData: IdpUseCase.ExternalAuthorizationData
    ): Result<String> =
        remoteDataSource.authorizeExtern(
            url = url,
            externalAuthorizationData = externalAuthorizationData
        )

    suspend fun invalidate(profileName: String) {
        try {
            getAliasOfSecureElementEntry(profileName).first()?.also {
                KeyStore.getInstance("AndroidKeyStore")
                    .apply { load(null) }
                    .deleteEntry(it.decodeToString())
            }
        } catch (e: Exception) {
            // silent fail; expected
        }
        invalidateConfig()
        invalidateDecryptedAccessToken(profileName)
        localDataSource.clearIdpAuthData(profileName)
    }

    suspend fun invalidateConfig() {
        localDataSource.clearIdpInfo()
    }

    suspend fun invalidateWithUserCredentials(profileName: String) {
        invalidate(profileName)
        setCardAccessNumber(profileName, null)
    }

    suspend fun invalidateSingleSignOnTokenRetainingScope(profileName: String) =
        localDataSource.saveSingleSignOnToken(profileName = profileName, token = null, validOn = null, expiresOn = null)

    fun invalidateDecryptedAccessToken(profileName: String) {
        decryptedAccessTokenMap.update {
            it - profileName
        }
    }

    suspend fun getAuthorizationRedirect(
        url: String,
        state: IdpState,
        codeChallenge: String,
        nonce: IdpNonce,
        kkAppId: String
    ): String {
        return remoteDataSource.requestAuthorizationRedirect(
            url = url, externalAppId = kkAppId,
            codeChallenge = codeChallenge,
            nonce = nonce.nonce,
            state = state.state
        ).getOrThrow()
    }
}
