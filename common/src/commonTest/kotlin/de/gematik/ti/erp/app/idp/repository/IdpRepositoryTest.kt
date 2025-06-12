/*
 * Copyright 2025, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.idp.repository

import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.db.SchemaVersion
import de.gematik.ti.erp.app.db.TestDB
import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AuthenticationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AuthenticationPasswordEntityV1
import de.gematik.ti.erp.app.db.entities.v1.IdpAuthenticationDataEntityV1
import de.gematik.ti.erp.app.db.entities.v1.IdpConfigurationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PasswordEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PharmacySearchEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.ChargeableItemV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.InvoiceEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.PKVInvoiceEntityV1
import de.gematik.ti.erp.app.db.entities.v1.invoice.PriceComponentV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.DeviceRequestDispenseEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.DeviceRequestEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.IdentifierEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.IngredientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.InsuranceInformationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationDispenseEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationRequestEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MultiplePrescriptionInfoEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.OrganizationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PatientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PractitionerEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.QuantityEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.RatioEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.ScannedTaskEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.SyncedTaskEntityV1
import de.gematik.ti.erp.app.fhir.model.ResourceBasePath
import de.gematik.ti.erp.app.idp.EllipticCurvesExtending
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.repository.DefaultProfilesRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.bouncycastle.cert.X509CertificateHolder
import org.jose4j.base64url.Base64
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.security.Security
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

// const val EXPECTED_EXPIRATION_TIME = 1616143876L
// const val EXPECTED_ISSUE_TIME = 1616057476L

class CommonIdpRepositoryTest : TestDB() {

    init {
        EllipticCurvesExtending.init()
        Security.insertProviderAt(BCProvider, 1)
    }

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val defaultProfileName1 = "TestProfile"

    private val profileId = "12345"
    private val accessToken = "54321"

    lateinit var realm: Realm

    private lateinit var repo: IdpRepository

    // private val testDiscoveryDocument by lazy { File("$ResourceBasePath/idp/discovery-doc.jwt").readText() }
    // private val testCertificateDocument by lazy { File("$ResourceBasePath/idp/idpCertificate.txt").readText() }

    private val ssoToken by lazy { File("$ResourceBasePath/idp/sso-token.txt").readText() }
    private val healthCardCert = X509CertificateHolder(
        Base64.decode(BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE)
    )
    // private val healthCardCertPrivateKey = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY

    // private val x509Certificate = X509CertificateHolder(Base64.decode(testCertificateDocument))

    /*private val testIdpConfig = IdpData.IdpConfiguration(
        authorizationEndpoint = "http://localhost:8888/sign_response",
        ssoEndpoint = "http://localhost:8888/sso_response",
        tokenEndpoint = "http://localhost:8888/token",
        pairingEndpoint = "http://localhost:8888/pairings",
        authenticationEndpoint = "http://localhost:8888/alt_response",
        pukIdpEncEndpoint = "http://localhost:8888/idpEnc/jwk.json",
        pukIdpSigEndpoint = "http://localhost:8888/ipdSig/jwk.json",
        certificate = x509Certificate,
        expirationTimestamp = Instant.fromEpochSeconds(EXPECTED_EXPIRATION_TIME),
        issueTimestamp = Instant.fromEpochSeconds(EXPECTED_ISSUE_TIME),
        externalAuthorizationIDsEndpoint = "http://localhost:8888/appList",
        federationAuthorizationIDsEndpoint = "", // not found in test data
        thirdPartyAuthorizationEndpoint = "http://localhost:8888/thirdPartyAuth",
        federationAuthorizationEndpoint = "" // not found in test data
    )*/

    @MockK
    lateinit var remoteDataSource: IdpRemoteDataSource

    @MockK
    lateinit var idpLocalDataSource: IdpLocalDataSource

    private lateinit var profileRepository: ProfileRepository

    private val accessTokenDataSource: AccessTokenDataSource = mockk()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        realm = Realm.open(
            RealmConfiguration.Builder(
                schema = setOf(
                    ProfileEntityV1::class,
                    SyncedTaskEntityV1::class,
                    OrganizationEntityV1::class,
                    PractitionerEntityV1::class,
                    PatientEntityV1::class,
                    InsuranceInformationEntityV1::class,
                    MedicationRequestEntityV1::class,
                    MedicationDispenseEntityV1::class,
                    CommunicationEntityV1::class,
                    AddressEntityV1::class,
                    MedicationEntityV1::class,
                    IngredientEntityV1::class,
                    RatioEntityV1::class,
                    QuantityEntityV1::class,
                    ScannedTaskEntityV1::class,
                    IdpAuthenticationDataEntityV1::class,
                    IdpConfigurationEntityV1::class,
                    SettingsEntityV1::class,
                    PharmacySearchEntityV1::class,
                    PasswordEntityV1::class,
                    ShippingContactEntityV1::class,
                    PharmacySearchEntityV1::class,
                    MultiplePrescriptionInfoEntityV1::class,
                    PKVInvoiceEntityV1::class,
                    InvoiceEntityV1::class,
                    ChargeableItemV1::class,
                    PriceComponentV1::class,
                    IdentifierEntityV1::class,
                    AuthenticationEntityV1::class,
                    AuthenticationPasswordEntityV1::class,
                    DeviceRequestEntityV1::class,
                    DeviceRequestDispenseEntityV1::class
                )
            )
                .schemaVersion(SchemaVersion.ACTUAL)
                .directory(tempDBPath)
                .build()
        )

        idpLocalDataSource = IdpLocalDataSource(realm)

        repo = DefaultIdpRepository(
            remoteDataSource = remoteDataSource,
            localDataSource = idpLocalDataSource,
            accessTokenDataSource = accessTokenDataSource
        )

        profileRepository = DefaultProfilesRepository(
            realm = realm
        )
    }

    @Test
    fun `save and get access token`() = runTest {
        val accessTokenExpiresOn = Clock.System.now().plus(5.minutes)
        val expected = AccessToken(accessToken, accessTokenExpiresOn)
        every { accessTokenDataSource.save(profileId, expected) } returns Unit
        every { accessTokenDataSource.get(profileId) } returns flowOf(
            AccessToken(accessToken, accessTokenExpiresOn)
        )

        repo.saveDecryptedAccessToken(profileId, AccessToken(accessToken, accessTokenExpiresOn))
        assertEquals(expected, repo.decryptedAccessToken(profileId).first())
    }

    @Test
    fun `save and get single signOn token`() = runTest {
        val ssoToken = IdpData.DefaultToken(
            token = IdpData.SingleSignOnToken(
                token = ssoToken
            ),
            "123123",
            healthCardCert
        )

        profileRepository.createNewProfile(defaultProfileName1)

        val testProfile = profileRepository.profiles().first()[0]

        repo.saveSingleSignOnToken(testProfile.id, ssoToken)

        val savedSsoToken = profileRepository.profiles().first()[0].singleSignOnTokenScope
        assertEquals(ssoToken, savedSsoToken)
    }

    /*@Test
    fun `load unchecked idp configuration`() {
        val discoveryDocument = JWSDiscoveryDocument(
            JsonWebStructure.fromCompactSerialization(
                testDiscoveryDocument
            ) as JsonWebSignature
        )

        coEvery { remoteDataSource.fetchDiscoveryDocument() } coAnswers { Result.success(discoveryDocument) }
        runTest {
            val idpConfiguration = repo.loadUncheckedIdpConfiguration()

            assertEquals(testIdpConfig.authorizationEndpoint, idpConfiguration.authorizationEndpoint)
            assertEquals(testIdpConfig.ssoEndpoint, idpConfiguration.ssoEndpoint)
            assertEquals(testIdpConfig.tokenEndpoint, idpConfiguration.tokenEndpoint)
            assertEquals(testIdpConfig.pairingEndpoint, idpConfiguration.pairingEndpoint)
            assertEquals(testIdpConfig.authenticationEndpoint, idpConfiguration.authenticationEndpoint)
            assertEquals(testIdpConfig.pukIdpEncEndpoint, idpConfiguration.pukIdpEncEndpoint)
            assertEquals(testIdpConfig.pukIdpSigEndpoint, idpConfiguration.pukIdpSigEndpoint)
            assertEquals(testIdpConfig.certificate, idpConfiguration.certificate)
            assertEquals(testIdpConfig.expirationTimestamp, idpConfiguration.expirationTimestamp)
            assertEquals(testIdpConfig.issueTimestamp, idpConfiguration.issueTimestamp)
            assertEquals(
                testIdpConfig.externalAuthorizationIDsEndpoint,
                idpConfiguration.externalAuthorizationIDsEndpoint
            )
            assertEquals(
                testIdpConfig.thirdPartyAuthorizationEndpoint,
                idpConfiguration.thirdPartyAuthorizationEndpoint
            )

            val savedIdpConfig = idpLocalDataSource.loadIdpInfo()
            assertEquals(testIdpConfig, savedIdpConfig)
        }
    }*/
}
