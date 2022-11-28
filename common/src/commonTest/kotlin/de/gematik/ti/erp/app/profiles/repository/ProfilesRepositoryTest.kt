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

package de.gematik.ti.erp.app.profiles.repository

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.db.TestDB
import de.gematik.ti.erp.app.db.ACTUAL_SCHEMA_VERSION
import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.AuditEventEntityV1
import de.gematik.ti.erp.app.db.entities.v1.IdpAuthenticationDataEntityV1
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
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import java.time.Instant
import kotlin.test.Test
import kotlin.test.BeforeTest
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

    lateinit var repo: ProfilesRepository

    @BeforeTest
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

        repo = ProfilesRepository(
            dispatchers = coroutineRule.dispatchers,
            realm = realm
        )
    }

    @Test
    fun `profiles should return empty list `() = runTest {
        repo.profiles().first().also {
            assertEquals(0, it.size)
        }
    }

    @Test
    fun `save profile - profiles should return activated profile`() = runTest {
        repo.saveProfile(defaultProfileName1, true)
        repo.profiles().first().also {
            assertEquals(1, it.size)
            assertEquals(defaultProfileName1, it[0].name)
            assertEquals(true, it[0].active)
        }
    }

    @Test
    fun `activate profile should activate profile and deactivate other profiles`() = runTest {
        repo.saveProfile(defaultProfileName, true)
        repo.saveProfile(defaultProfileName1, false)
        repo.profiles().first().also {
            assertEquals(2, it.size)
            it.find { profile ->
                profile.name == defaultProfileName1
            }.apply {
                this?.let { defaultProfile2 ->
                    repo.activateProfile(defaultProfile2.id)
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
        repo.saveProfile(defaultProfileName, true)
        repo.saveProfile(defaultProfileName1, false)
        repo.profiles().first().also { profileList ->
            assertEquals(2, profileList.size)
            profileList.find { profile ->
                profile.name == defaultProfileName
            }.apply {
                this?.let {
                    repo.removeProfile(it.id)
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
    fun `remove last profile - should fail`() = runTest {
        repo.saveProfile(defaultProfileName, true)
        repo.profiles().first().also { profileList ->
            assertEquals(1, profileList.size)
            profileList.find { profile ->
                profile.name == defaultProfileName
            }.apply {
                this?.let {
                    assertFails {
                        repo.removeProfile(it.id)
                    }
                }
            }
        }
    }

    @Test
    fun `saveInsuranceInformation - should save InsuranceInformation to profile`() = runTest {
        repo.saveProfile(defaultProfileName, true)
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
        repo.saveProfile(defaultProfileName, true)
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
        repo.saveProfile(defaultProfileName, true)
        repo.saveProfile(defaultProfileName1, true)

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
    fun `update profile name with id`() = runTest {
        repo.saveProfile(defaultProfileName, true)
        repo.profiles().first().also {
            repo.updateProfileName(it[0].id, defaultProfileName1)
        }
        repo.profiles().first().also {
            assertEquals(defaultProfileName1, it[0].name)
        }
    }

    @Test
    fun `update profile color`() = runTest {
        repo.saveProfile(defaultProfileName, true)
        ProfilesData.ProfileColorNames.values().forEach { colorName ->
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
        val now = Instant.now()
        repo.saveProfile(defaultProfileName, true)
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
        repo.saveProfile(defaultProfileName, true)
        ProfilesData.AvatarFigure.values().forEach { figure ->
            repo.profiles().first().also {
                repo.saveAvatarFigure(it[0].id, figure)
            }
            repo.profiles().first().also {
                assertEquals(figure, it[0].avatarFigure)
            }
        }
    }

    @Test
    fun `save personalized profile image`() = runTest {
        val profileImage = byteArrayOf(0x01.toByte(), 0x02.toByte())
        repo.saveProfile(defaultProfileName, true)
        repo.profiles().first().also {
            assertEquals(null, it[0].personalizedImage)
            repo.savePersonalizedProfileImage(it[0].id, profileImage)
        }
        repo.profiles().first().also {
            it[0].personalizedImage?.let { bytes ->
                assertEquals(0x01.toByte(), bytes[0])
                assertEquals(0x02.toByte(), bytes[1])
            }
        }
    }

    @Test
    fun `clear personalized profile image`() = runTest {
        val profileImage = byteArrayOf(0x01.toByte(), 0x02.toByte())
        repo.saveProfile(defaultProfileName, true)
        repo.profiles().first().also {
            repo.savePersonalizedProfileImage(it[0].id, profileImage)
        }
        repo.profiles().first().also {
            repo.clearPersonalizedProfileImage(it[0].id)
        }
        repo.profiles().first().also {
            assertEquals(null, it[0].personalizedImage)
        }
    }
}
