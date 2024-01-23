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

package de.gematik.ti.erp.app.idp.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.api.REDIRECT_URI
import de.gematik.ti.erp.app.idp.api.models.AuthenticationData
import de.gematik.ti.erp.app.idp.api.models.DeviceInformation
import de.gematik.ti.erp.app.idp.api.models.DeviceType
import de.gematik.ti.erp.app.idp.api.models.IdpAuthFlowResult
import de.gematik.ti.erp.app.idp.api.models.IdpInitialData
import de.gematik.ti.erp.app.idp.api.models.IdpState
import de.gematik.ti.erp.app.idp.api.models.IdpUnsignedChallenge
import de.gematik.ti.erp.app.idp.api.models.PairingData
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.idp.api.models.RegistrationData
import de.gematik.ti.erp.app.idp.buildJsonWebSignatureWithHealthCard
import de.gematik.ti.erp.app.idp.buildJsonWebSignatureWithSecureElement
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import io.github.aakira.napier.Napier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.jose4j.base64url.Base64Url
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.JsonWebStructure
import java.net.URI
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

class IdpAlternateAuthenticationUseCase(
    private val basicUseCase: IdpBasicUseCase,
    private val repository: IdpRepository,
    private val deviceInfo: IdpDeviceInfoProvider
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Requirement(
        "A_21591",
        "A_21600",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Gather device info and register with health card."
    )
    suspend fun registerDeviceWithHealthCard(
        initialData: IdpInitialData,
        accessToken: String,
        healthCardCertificate: ByteArray,

        publicKeyOfSecureElementEntry: PublicKey,
        aliasOfSecureElementEntry: ByteArray,

        signWithHealthCard: suspend (hash: ByteArray) -> ByteArray
    ): PairingResponseEntry {
        val (config, pukSigKey, pukEncKey) = initialData

        // TODO phone name? shall we support a real user chosen name?
        val deviceInformation = buildDeviceInformation(deviceInfo.deviceName)
        Napier.d("Device information: $deviceInformation")

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
            accessToken,
            idpPukSigKey = pukSigKey.jws.publicKey,
            idpPukEncKey = pukEncKey.jws.publicKey
        )

        return repository.postPairing(
            config.pairingEndpoint,
            encryptedRegistrationData.compactSerialization,
            encryptedAccessToken.compactSerialization
        ).getOrThrow()
    }

    suspend fun getPairedDevices(
        initialData: IdpInitialData,
        accessToken: String
    ): List<Pair<PairingResponseEntry, PairingData>> {
        val (config, pukSigKey, pukEncKey) = initialData

        val encryptedAccessToken = buildEncryptedAccessToken(
            accessToken,
            idpPukSigKey = pukSigKey.jws.publicKey,
            idpPukEncKey = pukEncKey.jws.publicKey
        )

        val pairedDevices = repository.getPairing(
            config.pairingEndpoint,
            encryptedAccessToken.compactSerialization
        ).getOrThrow()

        return pairedDevices.entries.map {
            val pairingData = requireNotNull(
                json.decodeFromString<PairingData>(
                    (JsonWebStructure.fromCompactSerialization(it.signedPairingData) as JsonWebSignature)
                        .unverifiedPayload
                )
            ) { "Couldn't parse pairing data" }

            it to pairingData
        }
    }

    // tag::DeletePairedDevicesUseCase[]
    @Requirement(
        "A_21443",
        sourceSpecification = "gemF_Biometrie",
        rationale = "Delete pairing for device."
    )
    suspend fun deletePairedDevice(
        initialData: IdpInitialData,
        accessToken: String,
        deviceAlias: String
    ) {
        val (config, pukSigKey, pukEncKey) = initialData

        val encryptedAccessToken = buildEncryptedAccessToken(
            accessToken,
            idpPukSigKey = pukSigKey.jws.publicKey,
            idpPukEncKey = pukEncKey.jws.publicKey
        )

        repository.deletePairing(
            url = config.pairingEndpoint,
            token = encryptedAccessToken.compactSerialization,
            alias = deviceAlias
        ).getOrThrow()
    }
    // end::DeletePairedDevicesUseCase[]

    private fun buildEncryptedAccessToken(
        accessToken: String,
        idpPukSigKey: PublicKey,
        idpPukEncKey: PublicKey
    ): JsonWebEncryption {
        val payload = """{ "njwt": "$accessToken" }"""

        val accessTokenExpiry = (JsonWebStructure.fromCompactSerialization(accessToken) as JsonWebSignature).let {
            it.key = idpPukSigKey
            json.parseToJsonElement(it.payload).jsonObject["exp"]?.jsonPrimitive?.int
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
        signatureObjectOfSecureElementEntry: Signature
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

        @Requirement(
            "O.Cryp_1#3",
            "O.Cryp_4#3",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Signature via ecdh ephemeral-static (one time usage)"
        )
        val signedAuthData =
            buildSignedAuthenticationData(authData, privateKeyOfSecureElementEntry, signatureObjectOfSecureElementEntry)
        val encryptedAuthData =
            buildEncryptedSignedAuthenticationData(
                signedAuthData,
                challenge.expires,
                initialData.pukEncKey.jws.publicKey
            )

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
            pukSigKey = pukSigKey,
            redirectUri = REDIRECT_URI
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

    private suspend fun postAlternateSignedChallengeAndGetRedirect(
        url: String,
        codeChallenge: JsonWebEncryption,
        state: IdpState
    ): URI {
        val redirect =
            URI(repository.postBiometricAuthenticationData(url, codeChallenge.compactSerialization).getOrThrow())

        val redirectState = IdpService.extractQueryParameter(redirect, "state")
        require(state.state == redirectState) { "Invalid state" }

        return redirect
    }

    private fun buildDeviceType(): DeviceType =
        DeviceType(
            manufacturer = deviceInfo.manufacturer,
            productName = deviceInfo.productName,
            model = deviceInfo.model,
            operatingSystem = deviceInfo.operatingSystem,
            operatingSystemVersion = deviceInfo.operatingSystemVersion
        )

    private fun buildDeviceInformation(userChosenName: String): DeviceInformation {
        return DeviceInformation(
            name = userChosenName, // Settings.System.getString(context.contentResolver, Settings.Global.DEVICE_NAME),
            deviceType = buildDeviceType()
        )
    }

    private fun buildPairingData(
        keyAliasOfSecureElement: ByteArray,
        subjectPublicKeyInfoOfSecureElement: SubjectPublicKeyInfo,
        healthCardCertificate: X509CertificateHolder
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

    private suspend fun buildSignedPairingData(
        pairingData: PairingData,
        sign: suspend (hash: ByteArray) -> ByteArray
    ): String =
        buildJsonWebSignatureWithHealthCard(
            {
                algorithmHeaderValue = "BP256R1"
                setHeader("typ", "JWT")
                payload = json.encodeToString(pairingData)
            },
            sign
        )

    private fun buildRegistrationData(
        signedPairingData: String,
        healthCardCertificate: ByteArray,
        deviceInformation: DeviceInformation
    ): RegistrationData =
        RegistrationData(
            signedPairingData = signedPairingData,
            healthCardCertificate = Base64Url.encode(healthCardCertificate),
            deviceInformation = deviceInformation
        )

    @Requirement(
        "A_21416",
        sourceSpecification = "gemF_Biometrie",
        rationale = "Generate registration data and encrypt it with PuK_IDP_ENC."
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
            payload = json.encodeToString(registrationData)
        }

    enum class AuthenticationMethod(val methods: List<String>) {
        Strong(listOf("mfa", "hwk", "generic-biometric")),
        DeviceCredentials(listOf("mfa", "hwk", "kba"))
    }

    private fun buildAuthenticationData(
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
            authenticationMethod = authenticationMethod.methods
        )
    }

    @Requirement(
        "O.Cryp_1#4",
        "O.Cryp_4#4",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Signature via ecdh ephemeral-static (one time usage)"
    )
    fun buildSignedAuthenticationData(
        authenticationData: AuthenticationData,
        privateKey: PrivateKey,
        signature: Signature
    ): String =
        buildJsonWebSignatureWithSecureElement(
            {
                algorithmHeaderValue = "ES256"
                setHeader("typ", "JWT")
                payload = json.encodeToString(authenticationData)
            },
            privateKey,
            signature
        )

    @Requirement(
        "A_21431",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Create and encrypt authentication data."
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
