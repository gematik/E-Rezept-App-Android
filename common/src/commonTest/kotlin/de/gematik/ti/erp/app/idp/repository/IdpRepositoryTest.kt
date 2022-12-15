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

import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.db.TestDB
import de.gematik.ti.erp.app.db.ACTUAL_SCHEMA_VERSION
import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AuditEventEntityV1
import de.gematik.ti.erp.app.db.entities.v1.IdpAuthenticationDataEntityV1
import de.gematik.ti.erp.app.db.entities.v1.IdpConfigurationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PasswordEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PharmacySearchEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
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
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.bouncycastle.cert.X509CertificateHolder
import org.jose4j.base64url.Base64
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.JsonWebStructure
import org.junit.Rule
import java.io.File
import java.security.Security
import java.time.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

const val EXPECTED_EXPIRATION_TIME = 1616143876L
const val EXPECTED_ISSUE_TIME = 1616057476L

@OptIn(ExperimentalCoroutinesApi::class)
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

    lateinit var repo: IdpRepository
    private val testDiscoveryDocument by lazy { File("$ResourceBasePath/idp/discovery-doc.jwt").readText() }
    private val testCertificateDocument by lazy { File("$ResourceBasePath/idp/idpCertificate.txt").readText() }
    private val ssoToken by lazy { File("$ResourceBasePath/idp/sso-token.txt").readText() }
    private val healthCardCert = X509CertificateHolder(
        Base64.decode(BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE)
    )
    // private val healthCardCertPrivateKey = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY

    private val x509Certificate = X509CertificateHolder(Base64.decode(testCertificateDocument))

    private val testIdpConfig = IdpData.IdpConfiguration(
        authorizationEndpoint = "http://localhost:8888/sign_response",
        ssoEndpoint = "http://localhost:8888/sso_response",
        tokenEndpoint = "http://localhost:8888/token",
        pairingEndpoint = "http://localhost:8888/pairings",
        authenticationEndpoint = "http://localhost:8888/alt_response",
        pukIdpEncEndpoint = "http://localhost:8888/idpEnc/jwk.json",
        pukIdpSigEndpoint = "http://localhost:8888/ipdSig/jwk.json",
        certificate = x509Certificate,
        expirationTimestamp = Instant.ofEpochSecond(EXPECTED_EXPIRATION_TIME),
        issueTimestamp = Instant.ofEpochSecond(EXPECTED_ISSUE_TIME),
        externalAuthorizationIDsEndpoint = "http://localhost:8888/appList",
        thirdPartyAuthorizationEndpoint = "http://localhost:8888/thirdPartyAuth"
    )

    @MockK
    lateinit var remoteDataSource: IdpRemoteDataSource

    lateinit var idpLocalDataSource: IdpLocalDataSource
    lateinit var profileRepository: ProfilesRepository

    @BeforeTest
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
                    AuditEventEntityV1::class,
                    SettingsEntityV1::class,
                    PharmacySearchEntityV1::class,
                    PasswordEntityV1::class,
                    ShippingContactEntityV1::class,
                    PharmacySearchEntityV1::class,
                    MultiplePrescriptionInfoEntityV1::class
                )
            )
                .schemaVersion(ACTUAL_SCHEMA_VERSION)
                .directory(tempDBPath)
                .build()
        )

        idpLocalDataSource = IdpLocalDataSource(realm)

        repo = IdpRepository(
            remoteDataSource = remoteDataSource,
            localDataSource = idpLocalDataSource
        )

        profileRepository = ProfilesRepository(
            dispatchers = coroutineRule.dispatchers,
            realm = realm
        )
    }

    @Test
    fun `save and get access token`() = runTest {
        repo.saveDecryptedAccessToken(profileId, accessToken)
        assertEquals(accessToken, repo.decryptedAccessToken(profileId).first())
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

        profileRepository.saveProfile(defaultProfileName1, true)
        val testprofile =
            profileRepository.profiles().first()[0]
        repo.saveSingleSignOnToken(testprofile.id, ssoToken)

        val savedSsoToken = profileRepository.profiles().first()[0].singleSignOnTokenScope
        assertEquals(ssoToken, savedSsoToken)
    }

    @Test
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
    }
}
