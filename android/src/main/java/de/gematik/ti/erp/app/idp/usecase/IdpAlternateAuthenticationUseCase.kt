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

import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.api.models.AuthenticationData
import de.gematik.ti.erp.app.idp.api.models.DeviceInformation
import de.gematik.ti.erp.app.idp.api.models.DeviceType
import de.gematik.ti.erp.app.idp.api.models.PairingData
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntries
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.idp.api.models.RegistrationData
import de.gematik.ti.erp.app.idp.buildJsonWebSignatureWithHealthCard
import de.gematik.ti.erp.app.idp.buildJsonWebSignatureWithSecureElement
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import java.net.URI
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.jose4j.base64url.Base64Url
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.JsonWebStructure
import org.json.JSONObject
import timber.log.Timber
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdpAlternateAuthenticationUseCase @Inject constructor(
    private val moshi: Moshi,
    private val basicUseCase: IdpBasicUseCase,
    private val repository: IdpRepository,
    private val deviceInfo: IdpDeviceInfoProvider
) {

    suspend fun registerDeviceWithHealthCard(
        initialData: IdpInitialData,
        accessToken: String,
        healthCardCertificate: ByteArray,

        publicKeyOfSecureElementEntry: PublicKey,
        aliasOfSecureElementEntry: ByteArray,

        signWithHealthCard: suspend (hash: ByteArray) -> ByteArray,
    ): PairingResponseEntry {
        val (config, pukSigKey, pukEncKey) = initialData

        // TODO phone name? shall we support a real user chosen name?
        val deviceInformation = buildDeviceInformation(deviceInfo.deviceName)
        Timber.d("Device information: $deviceInformation")

        val healthCardCertificateHolder = X509CertificateHolder(healthCardCertificate)

        val encoded: ByteArray = publicKeyOfSecureElementEntry.encoded
        val subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(
            ASN1Sequence.getInstance(encoded)
        )

        val pairingData = buildPairingData(aliasOfSecureElementEntry, subjectPublicKeyInfo, healthCardCertificateHolder)
        val signedPairingData = buildSignedPairingData(pairingData) {
            signWithHealthCard(it)
        }
        val registrationData =
            buildRegistrationData(signedPairingData, healthCardCertificate, deviceInformation)
        val encryptedRegistrationData =
            buildEncryptedRegistrationData(registrationData, pukEncKey.jws.publicKey)

        val encryptedAccessToken = buildEncryptedAccessToken(
            accessToken, idpPukSigKey = pukSigKey.jws.publicKey, idpPukEncKey = pukEncKey.jws.publicKey,
        )

        return when (
            val r = repository.postPairing(
                config.pairingEndpoint,
                encryptedRegistrationData.compactSerialization,
                encryptedAccessToken.compactSerialization
            )
        ) {
            is Result.Success -> r.data
            is Result.Error -> throw r.exception
        }
    }

    suspend fun getPairedDevices(
        initialData: IdpInitialData,
        accessToken: String
    ): PairingResponseEntries {
        val (config, pukSigKey, pukEncKey) = initialData

        val encryptedAccessToken = buildEncryptedAccessToken(
            accessToken, idpPukSigKey = pukSigKey.jws.publicKey, idpPukEncKey = pukEncKey.jws.publicKey,
        )

        return when (
            val r = repository.getPairing(
                config.pairingEndpoint,
                encryptedAccessToken.compactSerialization
            )
        ) {
            is Result.Success -> r.data
            is Result.Error -> throw r.exception
        }
    }

    fun buildEncryptedAccessToken(
        accessToken: String,
        idpPukSigKey: PublicKey,
        idpPukEncKey: PublicKey
    ): JsonWebEncryption {
        val payload = """{ "njwt": "$accessToken" }"""

        val accessTokenExpiry = (JsonWebStructure.fromCompactSerialization(accessToken) as JsonWebSignature).let {
            it.key = idpPukSigKey
            JSONObject(it.payload)["exp"] as Int
        }

        return JsonWebEncryption().apply {
            contentTypeHeaderValue = "NJWT"
            algorithmHeaderValue = KeyManagementAlgorithmIdentifiers.ECDH_ES
            encryptionMethodHeaderParameter = ContentEncryptionAlgorithmIdentifiers.AES_256_GCM
            setHeader("exp", accessTokenExpiry)
            key = idpPukEncKey
            this.payload = payload
        }
    }

    suspend fun authenticateWithSecureElement(
        initialData: IdpInitialData,
        challenge: IdpUnsignedChallenge,
        healthCardCertificate: ByteArray,
        authenticationMethod: AuthenticationMethod = AuthenticationMethod.Strong,

        aliasOfSecureElementEntry: ByteArray,
        privateKeyOfSecureElementEntry: PrivateKey,
        signatureObjectOfSecureElementEntry: Signature,
    ): IdpAuthFlowResult {
        val (config, pukSigKey, pukEncKey, state, nonce) = initialData
        val codeVerifier = initialData.codeVerifier

        val deviceInformation = buildDeviceInformation(deviceInfo.deviceName)

        val authData = buildAuthenticationData(
            challenge.signedChallenge,
            healthCardCertificate,
            aliasOfSecureElementEntry,
            deviceInformation,
            authenticationMethod
        )
        val signedAuthData =
            buildSignedAuthenticationData(authData, privateKeyOfSecureElementEntry, signatureObjectOfSecureElementEntry)
        val encryptedAuthData =
            buildEncryptedSignedAuthenticationData(signedAuthData, challenge.expires, initialData.pukEncKey.jws.publicKey)

        val redirect = postAlternateSignedChallengeAndGetRedirect(
            config.authenticationEndpoint,
            codeChallenge = encryptedAuthData,
            state = state
        )

        val redirectCodeJwe = IdpService.extractQueryParameter(redirect, "code")
        val redirectSsoToken = IdpService.extractQueryParameter(redirect, "ssotoken")

        // post [redirectCodeJwe] &b get the access token

        val idpTokenResult = basicUseCase.postCodeAndDecryptAccessToken(
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

    suspend fun postAlternateSignedChallengeAndGetRedirect(
        url: String,
        codeChallenge: JsonWebEncryption,
        state: IdpState,
    ): URI {
        val redirect = when (
            val r =
                repository.postBiometricAuthenticationData(url, codeChallenge.compactSerialization)
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

    fun buildDeviceType(): DeviceType =
        DeviceType(
            manufacturer = deviceInfo.manufacturer,
            productName = deviceInfo.productName,
            model = deviceInfo.model,
            operatingSystem = deviceInfo.operatingSystem,
            operatingSystemVersion = deviceInfo.operatingSystemVersion
        )

    fun buildDeviceInformation(userChosenName: String): DeviceInformation {
        return DeviceInformation(
            name = userChosenName, // Settings.System.getString(context.contentResolver, Settings.Global.DEVICE_NAME),
            deviceType = buildDeviceType()
        )
    }

    fun buildPairingData(
        keyAliasOfSecureElement: ByteArray,
        subjectPublicKeyInfoOfSecureElement: SubjectPublicKeyInfo,
        healthCardCertificate: X509CertificateHolder,
    ): PairingData {
        require(keyAliasOfSecureElement.size == 32)

        return PairingData(
            subjectPublicKeyInfoOfSecureElement = Base64Url.encode(
                subjectPublicKeyInfoOfSecureElement.toASN1Primitive().encoded
            ),
            keyAliasOfSecureElement = Base64Url.encode(keyAliasOfSecureElement),
            productName = deviceInfo.productName,

            serialNumberOfHealthCard = healthCardCertificate.serialNumber.toString(),
            issuerOfHealthCard = Base64Url.encode(
                healthCardCertificate.issuer.toASN1Primitive().encoded
            ),
            validityUntilOfHealthCard = healthCardCertificate.notAfter.time / 1000,
            subjectPublicKeyInfoOfHealthCard = Base64Url.encode(
                healthCardCertificate.subjectPublicKeyInfo.encoded
            )
        )
    }

    suspend fun buildSignedPairingData(
        pairingData: PairingData,
        sign: suspend (hash: ByteArray) -> ByteArray
    ): String =
        buildJsonWebSignatureWithHealthCard(
            {
                algorithmHeaderValue = "BP256R1"
                setHeader("typ", "JWT")
                payload = moshi.adapter(PairingData::class.java).toJson(pairingData)
            },
            sign
        )

    fun buildRegistrationData(
        signedPairingData: String,
        healthCardCertificate: ByteArray,
        deviceInformation: DeviceInformation
    ): RegistrationData =
        RegistrationData(
            signedPairingData = signedPairingData,
            healthCardCertificate = Base64Url.encode(healthCardCertificate),
            deviceInformation = deviceInformation
        )

    fun buildEncryptedRegistrationData(
        registrationData: RegistrationData,
        idpPukEncKey: PublicKey
    ): JsonWebEncryption =
        JsonWebEncryption().apply {
            contentTypeHeaderValue = "JSON"
            algorithmHeaderValue = KeyManagementAlgorithmIdentifiers.ECDH_ES
            encryptionMethodHeaderParameter = ContentEncryptionAlgorithmIdentifiers.AES_256_GCM
            setHeader("typ", "JWT")
            key = idpPukEncKey // KeyFactorySpi.EC().generatePublic(healthCardCertificate.subjectPublicKeyInfo)
            payload = moshi.adapter(RegistrationData::class.java).toJson(registrationData)
        }

    enum class AuthenticationMethod(val methods: List<String>) {
        Strong(listOf("mfa", "hwk", "generic-biometric")),
        DeviceCredentials(listOf("mfa", "hwk", "kba"))
    }

    fun buildAuthenticationData(
        challengeToken: String,
        healthCardCertificate: ByteArray,
        keyAliasOfSecureElement: ByteArray,
        deviceInformation: DeviceInformation,
        authenticationMethod: AuthenticationMethod
    ): AuthenticationData {
        require(keyAliasOfSecureElement.size == 32)

        return AuthenticationData(
            challenge = challengeToken,
            healthCardCertificate = Base64Url.encode(
                healthCardCertificate
            ),
            keyAliasOfSecureElement = Base64Url.encode(keyAliasOfSecureElement),
            deviceInformation = deviceInformation,
            authenticationMethod = authenticationMethod.methods,
        )
    }

    fun buildSignedAuthenticationData(
        authenticationData: AuthenticationData,
        privateKey: PrivateKey,
        signature: Signature
    ): String =
        buildJsonWebSignatureWithSecureElement(
            {
                algorithmHeaderValue = "ES256"
                setHeader("typ", "JWT")
                payload = moshi.adapter(AuthenticationData::class.java).toJson(authenticationData)
            },
            privateKey, signature
        )

    fun buildEncryptedSignedAuthenticationData(
        signedAuthenticationData: String,
        challengeExpiry: Long,
        idpPukEncKey: PublicKey
    ): JsonWebEncryption {
        val payload = """{ "njwt": "$signedAuthenticationData" }"""

        return JsonWebEncryption().apply {
            contentTypeHeaderValue = "NJWT"
            algorithmHeaderValue = KeyManagementAlgorithmIdentifiers.ECDH_ES
            encryptionMethodHeaderParameter = ContentEncryptionAlgorithmIdentifiers.AES_256_GCM
            setHeader("typ", "JWT")
            setHeader("exp", challengeExpiry)
            key = idpPukEncKey
            this.payload = payload
        }
    }
}
