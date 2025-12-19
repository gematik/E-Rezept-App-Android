/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.settings.repository

import com.russhwolf.settings.MapSettings
import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.v1.AddressEntityV1
import de.gematik.ti.erp.app.database.realm.v1.AuthenticationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.AuthenticationPasswordEntityV1
import de.gematik.ti.erp.app.database.realm.v1.PasswordEntityV1
import de.gematik.ti.erp.app.database.realm.v1.PharmacySearchEntityV1
import de.gematik.ti.erp.app.database.realm.v1.SettingsEntityV1
import de.gematik.ti.erp.app.database.realm.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.database.realm.v1.migrations.SchemaVersion
import de.gematik.ti.erp.app.database.settings.SettingsLocalDataSource
import de.gematik.ti.erp.app.db.TestDB
import de.gematik.ti.erp.app.settings.model.SettingsData
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSettingsRepositoryTest : TestDB() {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    lateinit var realm: Realm
    private lateinit var repo: DefaultSettingsRepository
    private lateinit var settingsLocalDataSource: SettingsLocalDataSource

    @Before
    fun setUp() {
        realm = Realm.open(
            RealmConfiguration.Builder(
                schema = setOf(
                    SettingsEntityV1::class,
                    PharmacySearchEntityV1::class,
                    PasswordEntityV1::class,
                    ShippingContactEntityV1::class,
                    PharmacySearchEntityV1::class,
                    AddressEntityV1::class,
                    AuthenticationPasswordEntityV1::class,
                    AuthenticationEntityV1::class
                )
            )
                .schemaVersion(SchemaVersion.ACTUAL)
                .directory(tempDBPath)
                .build()
        ).also {
            it.writeBlocking {
                copyToRealm(SettingsEntityV1())
            }
        }

        settingsLocalDataSource = SettingsLocalDataSource(MapSettings())

        repo = DefaultSettingsRepository(
            dispatchers = StandardTestDispatcher(),
            realm = realm,
            settingsLocalDataSource = settingsLocalDataSource
        )
    }

    @Test
    fun `general settings`() = runTest {
        repo.general.first().also {
            assertEquals(false, it.zoomEnabled)
            assertEquals(false, it.welcomeDrawerShown)
            assertEquals(false, it.userHasAcceptedInsecureDevice)
            assertEquals(false, it.userHasAcceptedIntegrityNotOk)
            assertEquals(false, it.mlKitAccepted)
            assertEquals(false, it.screenShotsAllowed)
            assertEquals(false, it.trackingAllowed)
        }

        repo.saveZoomPreference(true)
        repo.saveWelcomeDrawerShown()
        repo.acceptInsecureDevice()
        repo.acceptIntegrityNotOk()
        repo.acceptMlKit()
        repo.saveAllowScreenshots(true)
        repo.saveAllowTracking(true)

        val testTimestamp = Instant.fromEpochSeconds(123456)
        repo.acceptUpdatedDataTerms(testTimestamp)

        repo.general.first().also {
            assertEquals(true, it.zoomEnabled)
            assertEquals(true, it.welcomeDrawerShown)
            assertEquals(true, it.userHasAcceptedInsecureDevice)
            assertEquals(true, it.userHasAcceptedIntegrityNotOk)
            assertEquals(true, it.mlKitAccepted)
            assertEquals(true, it.screenShotsAllowed)
            assertEquals(true, it.trackingAllowed)
        }

        settingsLocalDataSource.dataProtectionVersionAccepted.first().also {
            assertEquals(testTimestamp, it)
        }

        repo.saveZoomPreference(false)
        repo.saveAllowScreenshots(false)
        repo.saveAllowTracking(false)
        repo.general.first().also {
            assertEquals(false, it.zoomEnabled)
            assertEquals(true, it.welcomeDrawerShown)
            assertEquals(false, it.screenShotsAllowed)
            assertEquals(false, it.trackingAllowed)
            assertEquals(true, it.mlKitAccepted)
        }
    }

    @Test
    fun `pharmacy search`() = runTest {
        repo.pharmacySearch.first().also {
            assertEquals("", it.name)
            assertEquals(false, it.locationEnabled)
            assertEquals(false, it.deliveryService)
            assertEquals(false, it.onlineService)
            assertEquals(false, it.openNow)
        }

        repo.savePharmacySearch(
            SettingsData.PharmacySearch(
                name = "Some Pharmacy",
                locationEnabled = true,
                deliveryService = true,
                onlineService = false,
                openNow = true
            )
        )

        repo.pharmacySearch.first().also {
            assertEquals("Some Pharmacy", it.name)
            assertEquals(true, it.locationEnabled)
            assertEquals(true, it.deliveryService)
            assertEquals(false, it.onlineService)
            assertEquals(true, it.openNow)
        }
    }

    @Test
    fun `authentication mode`() = runTest {
        repo.authentication.first().also {
            assertTrue {
                it.methodIsUnspecified
            }
        }

        repo.enableDeviceSecurity()
        repo.authentication.first().also {
            assertTrue {
                it.methodIsDeviceSecurity
            }
        }

        repo.disableDeviceSecurity()
        repo.setPassword(SettingsData.Authentication.Password("password"))
        repo.authentication.first().also {
            assertTrue {
                it.methodIsPassword
            }

            it.password?.let { password ->
                assertEquals(false, password.isValid("Test123456"))
                assertEquals(true, password.isValid("password"))
            }
        }
    }

    @Test
    fun `authentication mode set to both`() = runTest {
        repo.setPassword(SettingsData.Authentication.Password("password"))
        realm.queryFirst<AuthenticationPasswordEntityV1>()!!.also {
            assertEquals(true, it.hash.isNotEmpty())
            assertEquals(true, it.salt.isNotEmpty())
        }

        repo.enableDeviceSecurity()
        repo.authentication.first().also {
            assertTrue {
                it.bothMethodsAvailable
            }
        }

        realm.queryFirst<AuthenticationPasswordEntityV1>()!!.also {
            assertEquals(true, it.hash.isNotEmpty())
            assertEquals(true, it.salt.isNotEmpty())
        }
    }

    @Test
    fun `number of authentication failures`() = runTest {
        repo.authentication.first().also {
            assertEquals(0, it.failedAuthenticationAttempts)
        }

        repo.incrementNumberOfAuthenticationFailures()
        repo.incrementNumberOfAuthenticationFailures()
        repo.authentication.first().also {
            assertEquals(2, it.failedAuthenticationAttempts)
        }

        repo.resetNumberOfAuthenticationFailures()
        repo.authentication.first().also {
            assertEquals(0, it.failedAuthenticationAttempts)
        }
    }
}
