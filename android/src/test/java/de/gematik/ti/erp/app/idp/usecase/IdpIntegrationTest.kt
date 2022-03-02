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
import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.db.entities.ActiveProfile
import de.gematik.ti.erp.app.di.JWSConverterFactory
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.api.models.JWSAdapter
import de.gematik.ti.erp.app.idp.api.models.PairingData
import de.gematik.ti.erp.app.idp.repository.IdpLocalDataSource
import de.gematik.ti.erp.app.idp.repository.IdpRemoteDataSource
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.vau.api.model.X509ArrayAdapter
import de.gematik.ti.erp.app.vau.usecase.TruststoreUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.util.encoders.Base64
import org.jose4j.jws.EcdsaUsingShaAlgorithm
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.JsonWebStructure
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class IdpIntegrationTest {
    @MockK(relaxed = true)
    private lateinit var profilesRepository: ProfilesRepository

    @MockK
    private lateinit var truststoreUseCase: TruststoreUseCase

    @MockK(relaxed = true)
    private lateinit var localDataSource: IdpLocalDataSource

    @MockK
    private lateinit var cryptoProvider: IdpCryptoProvider

    private lateinit var idpRepository: IdpRepository
    private lateinit var basicUseCase: IdpBasicUseCase
    private lateinit var useCase: IdpUseCase

    private val moshi = Moshi.Builder().build()

    private val healthCardCert = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE
    private val healthCardCertPrivateKey = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY

    @Before
    fun setup() {
        Assume.assumeTrue(BuildKonfig.TEST_RUN_WITH_IDP_INTEGRATION)

        MockKAnnotations.init(this)

        every { profilesRepository.activeProfile() } returns flowOf(ActiveProfile(id = 0, profileName = ""))
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
            .addConverterFactory(
                MoshiConverterFactory.create(
                    moshi.newBuilder().add(JWSAdapter()).add(X509ArrayAdapter()).build()
                )
            )
            .build()
            .create(IdpService::class.java)

        idpRepository = spyk(
            IdpRepository(
                moshi = moshi,
                remoteDataSource = IdpRemoteDataSource(idpService),
                localDataSource = localDataSource
            )
        )

        basicUseCase = IdpBasicUseCase(
            repository = idpRepository,
            truststoreUseCase = truststoreUseCase,
            profilesRepository = profilesRepository
        )

        useCase = IdpUseCase(
            repository = idpRepository,
            altAuthUseCase = IdpAlternateAuthenticationUseCase(
                moshi = moshi,
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
            sharedPreferences = mockk(relaxed = true),
            cryptoProvider = cryptoProvider
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
            healthCardCertificate = {
                Base64.decode(healthCardCert)
            },
            sign = { sign(it) }
        )

        coVerify(exactly = 1) { idpRepository.setSingleSignOnToken("", any()) }
        coVerify(exactly = 1) { idpRepository.decryptedAccessTokenMap }

        assertEquals(true, idpRepository.decryptedAccessTokenMap.value[""]?.isNotEmpty())
    }

    @Test
    fun `authenticate with health card and get paired devices`() = runTest {
        val pairedDevices = useCase.getPairedDevices(
            healthCardCertificate = {
                Base64.decode(healthCardCert)
            },
            sign = { sign(it) }
        )

        println(pairedDevices.entries)

        val moshiAdapter = moshi.adapter(PairingData::class.java)
        pairedDevices.entries.forEach {
            println(moshiAdapter.fromJson((JsonWebStructure.fromCompactSerialization(it.signedPairingData) as JsonWebSignature).unverifiedPayload))
        }
    }

    @Test
    fun `authenticate with key store`() = runTest {
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
            publicKeyOfSecureElementEntry = keyPair.public,
            aliasOfSecureElementEntry = alias,
            healthCardCertificate = {
                Base64.decode(healthCardCert)
            },
            signWithHealthCard = { sign(it) }
        )

        coEvery { idpRepository.getHealthCardCertificate("") } answers { flowOf(Base64.decode(healthCardCert)) }
        coEvery { idpRepository.getAliasOfSecureElementEntry("") } answers { flowOf(alias) }

        useCase.alternateAuthenticationFlowWithSecureElement("")

        coVerify(exactly = 2) { idpRepository.setSingleSignOnToken("", any()) }
        coVerify(exactly = 1) { idpRepository.decryptedAccessTokenMap }

        assertEquals(true, idpRepository.decryptedAccessTokenMap.value[""]?.isNotEmpty())
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
            publicKeyOfSecureElementEntry = keyPair.public,
            aliasOfSecureElementEntry = alias,
            healthCardCertificate = {
                Base64.decode(healthCardCert)
            },
            signWithHealthCard = { sign(it) }
        )

        coEvery { idpRepository.getHealthCardCertificate("") } answers { flowOf(Base64.decode(healthCardCert)) }
        coEvery { idpRepository.getAliasOfSecureElementEntry("") } answers { flowOf(alias) }

        val pairedDevices = useCase.getPairedDevicesWithSecureElement("")

        println(pairedDevices.entries)

        val moshiAdapter = moshi.adapter(PairingData::class.java)
        pairedDevices.entries.forEach {
            println(moshiAdapter.fromJson((JsonWebStructure.fromCompactSerialization(it.signedPairingData) as JsonWebSignature).unverifiedPayload))
        }
    }
}
