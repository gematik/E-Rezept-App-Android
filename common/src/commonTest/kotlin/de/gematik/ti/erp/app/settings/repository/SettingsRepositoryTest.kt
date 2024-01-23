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

package de.gematik.ti.erp.app.settings.repository

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.db.TestDB
import de.gematik.ti.erp.app.db.ACTUAL_SCHEMA_VERSION
import de.gematik.ti.erp.app.db.entities.v1.AddressEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PasswordEntityV1
import de.gematik.ti.erp.app.db.entities.v1.PharmacySearchEntityV1
import de.gematik.ti.erp.app.db.entities.v1.SettingsEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.settings.model.SettingsData
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest : TestDB() {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    lateinit var realm: Realm

    lateinit var repo: SettingsRepository

    @BeforeTest
    fun setUp() {
        realm = Realm.open(
            RealmConfiguration.Builder(
                schema = setOf(
                    SettingsEntityV1::class,
                    PharmacySearchEntityV1::class,
                    PasswordEntityV1::class,
                    ShippingContactEntityV1::class,
                    PharmacySearchEntityV1::class,
                    AddressEntityV1::class
                )
            )
                .schemaVersion(ACTUAL_SCHEMA_VERSION)
                .directory(tempDBPath)
                .build()
        ).also {
            it.writeBlocking {
                copyToRealm(SettingsEntityV1())
            }
        }

        repo = SettingsRepository(
            dispatchers = coroutineRule.dispatchers,
            realm = realm
        )
    }

    @Test
    fun `general settings`() = runTest {
        repo.general.first().also {
            assertEquals(false, it.zoomEnabled)
            assertEquals(false, it.mlKitAccepted)
            assertEquals(false, it.userHasAcceptedInsecureDevice)
            assertEquals(0, it.authenticationFails)
        }

        repo.acceptInsecureDevice()

        repo.acceptUpdatedDataTerms(Instant.fromEpochSeconds(123456))

        repo.incrementNumberOfAuthenticationFailures()
        repo.incrementNumberOfAuthenticationFailures()

        repo.saveZoomPreference(true)
        repo.acceptMlKit()

        repo.general.first().also {
            assertEquals(true, it.zoomEnabled)
            assertEquals(true, it.mlKitAccepted)
            assertEquals(true, it.userHasAcceptedInsecureDevice)
            assertEquals(2, it.authenticationFails)
        }

        repo.resetNumberOfAuthenticationFailures()
        repo.general.first().also {
            assertEquals(true, it.zoomEnabled)
            assertEquals(true, it.userHasAcceptedInsecureDevice)
            assertEquals(0, it.authenticationFails)
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
        repo.authenticationMode.first().also {
            assertTrue {
                it is SettingsData.AuthenticationMode.Unspecified
            }
        }

        repo.saveAuthenticationMode(
            SettingsData.AuthenticationMode.DeviceSecurity
        )

        repo.authenticationMode.first().also {
            assertTrue {
                it is SettingsData.AuthenticationMode.DeviceSecurity
            }
        }

        repo.saveAuthenticationMode(
            SettingsData.AuthenticationMode.Password("Test123")
        )

        repo.authenticationMode.first().also {
            assertTrue {
                it is SettingsData.AuthenticationMode.Password
            }
            val password = it as SettingsData.AuthenticationMode.Password

            assertEquals(false, password.isValid("Test123456"))
            assertEquals(true, password.isValid("Test123"))
        }
    }

    @Test
    fun `authentication mode set to password - set other mode will reset stored credentials`() = runTest {
        repo.saveAuthenticationMode(
            SettingsData.AuthenticationMode.Password("Test123")
        )

        realm.queryFirst<PasswordEntityV1>()!!.also {
            assertEquals(true, it.hash.isNotEmpty())
            assertEquals(true, it.salt.isNotEmpty())
        }

        repo.saveAuthenticationMode(
            SettingsData.AuthenticationMode.DeviceSecurity
        )

        realm.queryFirst<PasswordEntityV1>()!!.also {
            assertEquals(true, it.hash.isEmpty())
            assertEquals(true, it.salt.isEmpty())
        }
    }
}
