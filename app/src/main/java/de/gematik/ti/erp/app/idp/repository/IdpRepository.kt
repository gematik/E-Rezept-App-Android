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

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.db.entities.IdpConfiguration
import de.gematik.ti.erp.app.di.NetworkSecureSharedPreferences
import de.gematik.ti.erp.app.idp.api.models.Challenge
import de.gematik.ti.erp.app.idp.api.models.IdpDiscoveryInfo
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.vau.extractECPublicKey
import org.bouncycastle.cert.X509CertificateHolder
import org.jose4j.base64url.Base64
import org.jose4j.jws.JsonWebSignature
import java.security.KeyStore
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private const val ssoTokenPrefKey = "ssoToken" // TODO remove within migration
private const val cardAccessNumberPrefKey = "cardAccessNumber"

@JvmInline
value class JWSDiscoveryDocument(val jws: JsonWebSignature)

data class SingleSignOnToken(val token: String, val scope: Scope = Scope.Default) {
    enum class Scope {
        Default,
        AlternateAuthentication
    }
}

@Singleton
class IdpRepository @Inject constructor(
    moshi: Moshi,
    private val remoteDataSource: IdpRemoteDataSource,
    private val localDataSource: IdpLocalDataSource,
    @NetworkSecureSharedPreferences private val securePrefs: SharedPreferences
) {
    private val discoveryDocumentBodyAdapter = moshi.adapter(IdpDiscoveryInfo::class.java)

    var decryptedAccessToken: String? = null
        set(v) {
            field = if (v?.isBlank() == true) null else v
        }

    var cardAccessNumber: String?
        set(can) {
            securePrefs.edit {
                putString(cardAccessNumberPrefKey, can)
            }
        }
        get() = securePrefs.getString(cardAccessNumberPrefKey, null)

    suspend fun getSingleSignOnToken(profileName: String) = localDataSource.loadIdpAuthData(profileName).let { entity ->
        entity.singleSignOnToken?.let { token ->
            entity.singleSignOnTokenScope?.let { scope ->
                SingleSignOnToken(token, scope)
            }
        }
    }

    suspend fun setSingleSignOnToken(token: SingleSignOnToken) =
        localDataSource.saveSingleSignOnToken(token.token, token.scope)

    suspend fun getHealthCardCertificate(profileName: String) = localDataSource.loadIdpAuthData(profileName).healthCardCertificate

    suspend fun setHealthCardCertificate(cert: ByteArray) = localDataSource.saveHealthCardCertificate(cert)

    suspend fun getSingleSignOnTokenScope(profileName: String) =
        localDataSource.loadIdpAuthData(profileName).singleSignOnTokenScope

    suspend fun isPairingScope(profileName: String) =
        localDataSource.loadIdpAuthData(profileName).singleSignOnTokenScope == SingleSignOnToken.Scope.AlternateAuthentication

    suspend fun setPairingScope() =
        localDataSource.saveSingleSignOnToken(null, SingleSignOnToken.Scope.AlternateAuthentication)

    suspend fun getAliasOfSecureElementEntry(profileName: String) = localDataSource.loadIdpAuthData(profileName).aliasOfSecureElementEntry
    suspend fun setAliasOfSecureElementEntry(alias: ByteArray) {
        require(alias.size == 32)
        localDataSource.saveSecureElementAlias(alias)
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
            when (val r = remoteDataSource.fetchDiscoveryDocument()) {
                is Result.Error -> throw r.exception
                is Result.Success -> extractUncheckedIdpConfiguration(r.data).also { localDataSource.saveIdpInfo(it) }
            }
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

    private fun parseDiscoveryDocumentBody(body: String): IdpDiscoveryInfo =
        requireNotNull(discoveryDocumentBodyAdapter.fromJson(body)) { "Couldn't parse discovery document" }

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

    suspend fun postPairing(
        url: String,
        encryptedRegistrationData: String,
        token: String
    ): Result<PairingResponseEntry> =
        remoteDataSource.postPairing(url, token = token, encryptedRegistrationData = encryptedRegistrationData)

    suspend fun postAlternateAuthenticationData(
        url: String,
        encryptedSignedAuthenticationData: String
    ): Result<String> =
        remoteDataSource.postAuthenticationData(url, encryptedSignedAuthenticationData)

    suspend fun invalidate(profileName: String) {
        try {
            getAliasOfSecureElementEntry(profileName)?.also {
                KeyStore.getInstance("AndroidKeyStore")
                    .apply { load(null) }
                    .deleteEntry(it.decodeToString())
            }
        } catch (e: Exception) {
            // silent fail; expected
        }
        invalidateConfig()
        invalidateDecryptedAccessToken()
        localDataSource.clearIdpAuthData()
    }

    suspend fun invalidateConfig() {
        localDataSource.clearIdpInfo()
    }

    suspend fun invalidateWithUserCredentials(profileName: String) {
        invalidate(profileName)

        cardAccessNumber = "" // TODO better handling
    }

    suspend fun invalidateSingleSignOnTokenRetainingScope() =
        localDataSource.saveSingleSignOnToken(null)

    fun invalidateDecryptedAccessToken() {
        decryptedAccessToken = null
    }
}
