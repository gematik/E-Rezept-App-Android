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

package de.gematik.ti.erp.app.profiles.repository

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.db.SchemaVersion
import de.gematik.ti.erp.app.db.TestDB
import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AuthenticationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AuthenticationPasswordEntityV1
import de.gematik.ti.erp.app.db.entities.v1.IdpAuthenticationDataEntityV1
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
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@OptIn(ExperimentalCoroutinesApi::class)
class ProfilesRepositoryTest : TestDB() {
    @get:Rule
    val coroutineRule = CoroutineTestRule()
    private val defaultProfileName = "Sven Muster"
    private val defaultInsurantName = "Sven Muster"
    private val defaultInsuranceIdentifier = "123456789"
    private val defaultInsuranceIdentifier1 = "987654321"

    private val defaultInsuranceName = "MusterKasse"

    private val defaultProfileName1 = "Gabi Muster"

    lateinit var realm: Realm

    lateinit var repo: DefaultProfilesRepository

    @Before
    fun setUp() {
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
                    AuthenticationEntityV1::class,
                    AuthenticationPasswordEntityV1::class,
                    IdentifierEntityV1::class,
                    DeviceRequestEntityV1::class
                )
            )
                .schemaVersion(SchemaVersion.ACTUAL)
                .directory(tempDBPath)
                .build()
        )

        repo = DefaultProfilesRepository(
            realm = realm
        )
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun `profiles should return empty list `() = runTest {
        repo.profiles().first().also {
            assertEquals(0, it.size)
        }
    }

    @Test
    fun `activate profile should activate profile and deactivate other profiles`() = runTest {
        repo.createNewProfile(defaultProfileName1)
        repo.createNewProfile(defaultProfileName)
        repo.profiles().first().also {
            assertEquals(2, it.size)
            it.find { profile ->
                profile.name == defaultProfileName1
            }.apply {
                this?.let { defaultProfile1 ->
                    repo.activateProfile(defaultProfile1.id)
                    repo.profiles().first().also { profileList ->
                        profileList.find { profile ->
                            profile.name == defaultProfileName
                        }.apply {
                            this?.let { profile -> assertEquals(profile.active, false) }
                        }
                        profileList.find { profile ->
                            profile.name == defaultProfileName1
                        }.apply {
                            this?.let { profile -> assertEquals(profile.active, true) }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `remove active profile - should remove profile and activate an other profile`() = runTest {
        repo.createNewProfile(defaultProfileName1)
        repo.createNewProfile(defaultProfileName)
        repo.profiles().first().also { profileList ->
            assertEquals(2, profileList.size)
            profileList.find { profile ->
                profile.name == defaultProfileName
            }.apply {
                this?.let {
                    repo.removeProfile(it.id, defaultProfileName)
                }
            }
        }
        repo.profiles().first().also { newProfileList ->
            assertEquals(1, newProfileList.size)
            assertEquals(defaultProfileName1, newProfileList[0].name)
            assertEquals(true, newProfileList[0].active)
        }
    }

    @Test
    fun `remove last profile - should create a default profile`() = runTest {
        repo.createNewProfile(defaultProfileName)
        repo.profiles().first().also { profileList ->
            assertEquals(1, profileList.size)
            profileList.find { profile ->
                profile.name == defaultProfileName
            }.apply {
                this?.let {
                    repo.removeProfile(it.id, defaultProfileName1)
                }
            }
        }
        repo.profiles().first().also { profileList ->
            assertEquals(1, profileList.size)
            assertEquals(actual = profileList.first().name, expected = defaultProfileName1)
            assertEquals(actual = profileList.first().active, expected = true)
        }
    }

    @Test
    fun `saveInsuranceInformation - should save InsuranceInformation to profile`() = runTest {
        repo.createNewProfile(defaultProfileName)
        repo.profiles().first().also { profileList ->
            profileList.find { profile ->
                profile.name == defaultProfileName
            }.apply {
                this?.let {
                    repo.saveInsuranceInformation(
                        it.id,
                        defaultInsurantName,
                        defaultInsuranceIdentifier,
                        defaultInsuranceName
                    )
                }
            }
        }
        repo.profiles().first().also { profileList ->
            assertEquals(defaultInsurantName, profileList[0].insurantName)
            assertEquals(defaultInsuranceIdentifier, profileList[0].insuranceIdentifier)
            assertEquals(defaultInsuranceName, profileList[0].insuranceName)
        }
    }

    @Test
    fun `saveInsuranceInformation on profile with other insuranceId  - should fail`() = runTest {
        repo.createNewProfile(defaultProfileName)
        repo.profiles().first().also { profileList ->
            profileList.find { profile ->
                profile.name == defaultProfileName
            }.apply {
                this?.let {
                    repo.saveInsuranceInformation(
                        it.id,
                        defaultInsurantName,
                        defaultInsuranceIdentifier,
                        defaultInsuranceName
                    )
                }
            }
        }
        repo.profiles().first().also { profileList ->
            assertFails {
                repo.saveInsuranceInformation(
                    profileList[0].id,
                    defaultInsurantName,
                    defaultInsuranceIdentifier1,
                    defaultInsuranceName
                )
            }
        }
    }

    @Test
    fun `saveInsuranceInformation save the same insuranceId on 2 profiles - should fail`() = runTest {
        repo.createNewProfile(defaultProfileName)
        repo.createNewProfile(defaultProfileName1)

        repo.profiles().first().also { profileList ->
            assertFails {
                profileList.forEach {
                    repo.saveInsuranceInformation(
                        it.id,
                        defaultInsurantName,
                        defaultInsuranceIdentifier,
                        defaultInsuranceName
                    )
                }
            }
        }
    }

    @Test
    fun `saveInsuranceInformation when isNewlyCreated is true should set insurantName as profile name`() = runTest {
        realm.write {
            ProfileEntityV1().apply {
                this.name = defaultProfileName
                this.active = true
                this.isNewlyCreated = true
            }
        }

        val profileId = repo.profiles().first().find { it.name == defaultProfileName }?.id
        if (profileId != null) {
            repo.saveInsuranceInformation(
                profileId = profileId,
                insurantName = defaultInsurantName,
                insuranceIdentifier = defaultInsuranceIdentifier,
                insuranceName = defaultInsuranceName
            )
        }

        realm.query(ProfileEntityV1::class, "insuranceIdentifier == $0", defaultInsuranceIdentifier).first().find()
            ?.let { updatedProfile ->
                assertEquals(defaultInsurantName, updatedProfile.name)
                assertTrue(updatedProfile.isNewlyCreated)
            }
    }

    @Test
    fun `saveInsuranceInformation when isNewlyCreated is false should retain existing profile name`() = runTest {
        repo.createNewProfile(defaultProfileName)

        realm.write {
            val profileEntity = realm.query(ProfileEntityV1::class, "name == $0", defaultProfileName).first().find()
            profileEntity?.isNewlyCreated = false
        }

        val profileId = repo.profiles().first().find { it.name == defaultProfileName }?.id
        if (profileId != null) {
            repo.saveInsuranceInformation(
                profileId = profileId,
                insurantName = defaultInsurantName,
                insuranceIdentifier = defaultInsuranceIdentifier,
                insuranceName = defaultInsuranceName
            )
        }

        realm.query(ProfileEntityV1::class, "insuranceIdentifier == $0", defaultInsuranceIdentifier).first().find()
            ?.let { updatedProfile ->
                assertEquals(defaultProfileName, updatedProfile.name)
                assertFalse(updatedProfile.isNewlyCreated)
            }
    }

    @Test
    fun `update profile name with id`() = runTest {
        repo.createNewProfile(defaultProfileName)
        repo.profiles().first().also {
            repo.updateProfileName(it[0].id, defaultProfileName1)
        }
        repo.profiles().first().also {
            assertEquals(defaultProfileName1, it[0].name)
        }
    }

    @Test
    fun `update profile color`() = runTest {
        repo.createNewProfile(defaultProfileName)
        ProfilesData.ProfileColorNames.entries.forEach { colorName ->
            repo.profiles().first().also {
                repo.updateProfileColor(it[0].id, colorName)
            }
            repo.profiles().first().also {
                assertEquals(colorName, it[0].color)
            }
        }
    }

    @Test
    fun `update last authenticated`() = runTest {
        val now = Clock.System.now()
        repo.createNewProfile(defaultProfileName)
        repo.profiles().first().also {
            assertEquals(null, it[0].lastAuthenticated)
            repo.updateLastAuthenticated(it[0].id, now)
        }
        repo.profiles().first().also {
            assertEquals(now, it[0].lastAuthenticated)
        }
    }

    @Test
    fun `save avatar figure`() = runTest {
        repo.createNewProfile(defaultProfileName)
        ProfilesData.Avatar.entries.forEach { figure ->
            repo.profiles().first().also {
                repo.saveAvatarFigure(it[0].id, figure)
            }
            repo.profiles().first().also {
                assertEquals(figure, it[0].avatar)
            }
        }
    }

    @Test
    fun `save personalized profile image`() = runTest {
        val profileImage = byteArrayOf(0x01.toByte(), 0x02.toByte())
        repo.createNewProfile(defaultProfileName)
        repo.profiles().first().also {
            assertEquals(null, it[0].image)
            repo.savePersonalizedProfileImage(it[0].id, profileImage)
        }
        repo.profiles().first().also {
            it[0].image?.let { bytes ->
                assertEquals(0x01.toByte(), bytes[0])
                assertEquals(0x02.toByte(), bytes[1])
            }
        }
    }

    @Test
    fun `clear personalized profile image`() = runTest {
        val profileImage = byteArrayOf(0x01.toByte(), 0x02.toByte())
        repo.createNewProfile(defaultProfileName)
        repo.profiles().first().also {
            repo.savePersonalizedProfileImage(it[0].id, profileImage)
        }
        repo.profiles().first().also {
            repo.clearPersonalizedProfileImage(it[0].id)
        }
        repo.profiles().first().also {
            assertEquals(null, it[0].image)
        }
    }
}
