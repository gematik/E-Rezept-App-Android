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

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.di.JWSConverterFactory
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.AccessTokenDataSource
import de.gematik.ti.erp.app.idp.repository.IdpLocalDataSource
import de.gematik.ti.erp.app.idp.repository.IdpPairingRepository
import de.gematik.ti.erp.app.idp.repository.IdpRemoteDataSource
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.DefaultProfilesRepository
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.util.encoders.Base64
import org.jose4j.base64url.Base64Url
import org.jose4j.jws.EcdsaUsingShaAlgorithm
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdpIntegrationTest {
    @MockK(relaxed = true)
    private lateinit var profilesRepository: DefaultProfilesRepository

    @MockK
    private lateinit var truststoreUseCase: TruststoreUseCase

    @MockK(relaxed = true)
    private lateinit var localDataSource: IdpLocalDataSource

    @MockK
    private lateinit var cryptoProvider: IdpCryptoProvider

    private val accessTokenDataSource: AccessTokenDataSource = mockk()

    private val lock: Mutex = mockk(relaxed = true)

    private lateinit var idpRepository: IdpRepository
    private lateinit var idpPairingRepository: IdpPairingRepository
    private lateinit var basicUseCase: IdpBasicUseCase
    private lateinit var useCase: IdpUseCase

    private val healthCardCert = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE
    private val healthCardCertPrivateKey = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY

    private val profileId = ""
    private val cardAccessNumber = ""

    @Suppress("JSON_FORMAT_REDUNDANT")
    private val jsonConverterFactory = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }.asConverterFactory("application/json".toMediaType())

    @Before
    fun setup() {
        Assume.assumeTrue(BuildKonfig.TEST_RUN_WITH_IDP_INTEGRATION)

        MockKAnnotations.init(this)

        coEvery { truststoreUseCase.checkIdpCertificate(any(), any()) } coAnswers {}
        every { cryptoProvider.signatureInstance() } returns Signature.getInstance("SHA256withECDSA")
        coEvery { localDataSource.loadIdpInfo() } returns null

        val client = OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().also {
                    if (BuildKonfig.INTERNAL) it.setLevel(HttpLoggingInterceptor.Level.BODY)
                }
            )
            .followRedirects(false)
            .build()

        val idpService = Retrofit.Builder()
            .client(client)
            .baseUrl(BuildKonfig.IDP_SERVICE_URI)
            .addConverterFactory(JWSConverterFactory())
            .addConverterFactory(jsonConverterFactory)
            .build()
            .create(IdpService::class.java)

        idpRepository = spyk(
            IdpRepository(
                remoteDataSource = IdpRemoteDataSource(idpService) { BuildKonfig.IDP_DEFAULT_SCOPE },
                localDataSource = localDataSource,
                accessTokenDataSource = accessTokenDataSource
            )
        )

        idpPairingRepository = spyk(
            IdpPairingRepository(
                localDataSource = localDataSource
            )
        )

        basicUseCase = IdpBasicUseCase(
            repository = idpRepository,
            truststoreUseCase = truststoreUseCase
        )

        useCase = DefaultIdpUseCase(
            repository = idpRepository,
            pairingRepository = idpPairingRepository,
            altAuthUseCase = IdpAlternateAuthenticationUseCase(
                basicUseCase = basicUseCase,
                repository = idpRepository,
                deviceInfo = mockk {
                    every { deviceName } returns "Test"
                    every { manufacturer } returns "Test"
                    every { productName } returns "Test"
                    every { model } returns "Test"
                    every { operatingSystem } returns "Android"
                    every { operatingSystemVersion } returns "XX"
                }
            ),
            profilesRepository = profilesRepository,
            basicUseCase = basicUseCase,
            cryptoProvider = cryptoProvider,
            lock = lock
        )
    }

    private fun sign(hash: ByteArray): ByteArray {
        val curveSpec = ECNamedCurveTable.getParameterSpec("brainpoolP256r1")
        val keySpec =
            org.bouncycastle.jce.spec.ECPrivateKeySpec(
                BigInteger(Base64.decode(healthCardCertPrivateKey)),
                curveSpec
            )
        val privateKey = KeyFactory.getInstance("EC", BCProvider).generatePrivate(keySpec)
        val signed = Signature.getInstance("NoneWithECDSA").apply {
            initSign(privateKey)
            update(hash)
        }.sign()
        return EcdsaUsingShaAlgorithm.convertDerToConcatenated(signed, 64)
    }

    @Test
    fun `authenticate with health card`() = runTest {
        useCase.authenticationFlowWithHealthCard(
            profileId = profileId,
            cardAccessNumber = cardAccessNumber,
            healthCardCertificate = {
                Base64.decode(healthCardCert)
            },
            sign = { sign(it) }
        )

        coVerify(exactly = 1) { idpRepository.saveSingleSignOnToken(profileId, any()) }
        coVerify(exactly = 1) { idpRepository.saveDecryptedAccessToken(profileId, any()) }

        assertEquals(true, idpRepository.decryptedAccessToken(profileId).first()?.accessToken?.isNotEmpty())
    }

    @Test
    fun `authenticate with health card and get paired devices`() = runTest {
        useCase.authenticationFlowWithHealthCard(
            profileId = profileId,
            scope = IdpScope.BiometricPairing,
            cardAccessNumber = cardAccessNumber,
            healthCardCertificate = {
                Base64.decode(healthCardCert)
            },
            sign = { sign(it) }
        )

        coEvery { localDataSource.authenticationData(profileId) } answers {
            flowOf(
                IdpData.AuthenticationData(
                    IdpData.DefaultToken(
                        token = mockk(relaxed = true),
                        cardAccessNumber = cardAccessNumber,
                        healthCardCertificate = Base64.decode(healthCardCert)
                    )
                )
            )
        }

        val pairedDevices = useCase.getPairedDevices(profileId = profileId)

        println(pairedDevices.getOrThrow())
    }

    @Test
    fun `authenticate with key store and get paired devices`() = runTest {
        val keyPair = KeyPairGenerator.getInstance("EC")
            .apply { initialize(ECGenParameterSpec("secp256r1")) }
            .generateKeyPair()

        val keyStore = mockk<KeyStore>(relaxed = true) {
            every { getEntry(any(), any()) } answers {
                mockk<KeyStore.PrivateKeyEntry> {
                    every { privateKey } returns keyPair.private
                }
            }
        }

        val alias = ByteArray(32).apply {
            Random.nextBytes(this)
        }

        every { cryptoProvider.keyStoreInstance() } returns keyStore

        useCase.alternatePairingFlowWithSecureElement(
            profileId = profileId,
            cardAccessNumber = cardAccessNumber,
            publicKeyOfSecureElementEntry = keyPair.public,
            aliasOfSecureElementEntry = alias,
            healthCardCertificate = {
                Base64.decode(healthCardCert)
            },
            signWithHealthCard = { sign(it) }
        )

        coEvery { idpRepository.authenticationData(profileId) } answers {
            flowOf(
                IdpData.AuthenticationData(
                    IdpData.AlternateAuthenticationWithoutToken(
                        cardAccessNumber = cardAccessNumber,
                        aliasOfSecureElementEntry = alias,
                        healthCardCertificate = Base64.decode(healthCardCert)
                    )
                )
            )
        }

        useCase.alternateAuthenticationFlowWithSecureElement(profileId = profileId, scope = IdpScope.Default)

        coVerify(exactly = 2) { idpRepository.saveSingleSignOnToken(profileId, any()) }
        coVerify(exactly = 1) { idpRepository.saveDecryptedAccessToken(profileId, any()) }

        assertEquals(true, idpRepository.decryptedAccessToken(profileId).first()?.accessToken?.isNotEmpty())

        //
        // paired devices
        //

        useCase.alternateAuthenticationFlowWithSecureElement(profileId = profileId, scope = IdpScope.BiometricPairing)

        coEvery { localDataSource.authenticationData(profileId) } answers {
            flowOf(
                IdpData.AuthenticationData(
                    IdpData.AlternateAuthenticationWithoutToken(
                        cardAccessNumber = cardAccessNumber,
                        aliasOfSecureElementEntry = alias,
                        healthCardCertificate = Base64.decode(healthCardCert)
                    )
                )
            )
        }

        val aliasBase64 = Base64Url.encode(alias)

        useCase.getPairedDevices(profileId = profileId).getOrThrow().let { pairedDevices ->
            println(pairedDevices)
            assertTrue {
                pairedDevices.any { (_, pairing) ->
                    pairing.keyAliasOfSecureElement == aliasBase64
                }
            }
        }

        useCase.deletePairedDevice(profileId = profileId, deviceAlias = aliasBase64)

        useCase.getPairedDevices(profileId = profileId).getOrThrow().let { pairedDevices ->
            println(pairedDevices)
            assertFalse {
                pairedDevices.any { (_, pairing) ->
                    pairing.keyAliasOfSecureElement == aliasBase64
                }
            }
        }
    }
}
