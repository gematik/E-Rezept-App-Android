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

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.database.realm.v1.AddressEntityV1
import de.gematik.ti.erp.app.database.realm.v1.settings.PasswordEntityV1
import de.gematik.ti.erp.app.database.realm.v1.settings.PharmacySearchEntityV1
import de.gematik.ti.erp.app.database.realm.v1.settings.SettingsEntityV1
import de.gematik.ti.erp.app.database.realm.v1.appauthentication.AuthenticationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.appauthentication.AuthenticationPasswordEntityV1
import de.gematik.ti.erp.app.database.realm.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.database.realm.v1.migrations.SchemaVersion
import de.gematik.ti.erp.app.database.realm.v1.settings.SettingsLocalDataSourceV1
import de.gematik.ti.erp.app.db.TestDB
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSettingsRepositoryTest : TestDB() {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    lateinit var realm: Realm
    private lateinit var repo: DefaultSettingsRepository

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

        repo = DefaultSettingsRepository(
            settingsLocalDataSource = SettingsLocalDataSourceV1(realm)
        )
    }

    @Test
    fun `general settings`() = runTest {
        repo.loadSettings().first().also {
            assertEquals(false, it.zoomEnabled)
            assertEquals(false, it.welcomeDrawerShown)
            assertEquals(false, it.userHasAcceptedInsecureDevice)
            assertEquals(false, it.userHasAcceptedIntegrityNotOk)
            assertEquals(false, it.screenShotsAllowed)
            assertEquals(false, it.trackingAllowed)
        }

        repo.saveZoomEnabled(true)
        repo.saveWelcomeDrawerShown()
        repo.acceptInsecureDevice()
        repo.acceptIntegrityNotOk()
        repo.saveAllowScreenshots(true)
        repo.saveAllowTracking(true)

        repo.loadSettings().first().also {
            assertEquals(true, it.zoomEnabled)
            assertEquals(true, it.welcomeDrawerShown)
            assertEquals(true, it.userHasAcceptedInsecureDevice)
            assertEquals(true, it.userHasAcceptedIntegrityNotOk)
            assertEquals(true, it.screenShotsAllowed)
            assertEquals(true, it.trackingAllowed)
        }

        repo.saveZoomEnabled(false)
        repo.saveAllowScreenshots(false)
        repo.saveAllowTracking(false)
        repo.loadSettings().first().also {
            assertEquals(false, it.zoomEnabled)
            assertEquals(true, it.welcomeDrawerShown)
            assertEquals(false, it.screenShotsAllowed)
            assertEquals(false, it.trackingAllowed)
        }
    }
}
