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

package de.gematik.ti.erp.app.appauthentication

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationMethodErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationPasswordErpModel
import de.gematik.ti.erp.app.appauthentication.repository.DefaultAppAuthenticationRepository
import de.gematik.ti.erp.app.database.api.AppAuthenticationLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.v1.AddressEntityV1
import de.gematik.ti.erp.app.database.realm.v1.appauthentication.AuthenticationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.appauthentication.AuthenticationPasswordEntityV1
import de.gematik.ti.erp.app.database.realm.v1.ShippingContactEntityV1
import de.gematik.ti.erp.app.database.realm.v1.appauthentication.AppAuthenticationLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.migrations.SchemaVersion
import de.gematik.ti.erp.app.database.realm.v1.settings.PasswordEntityV1
import de.gematik.ti.erp.app.database.realm.v1.settings.PharmacySearchEntityV1
import de.gematik.ti.erp.app.database.realm.v1.settings.SettingsEntityV1
import de.gematik.ti.erp.app.db.TestDB
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAppAuthenticationRepositoryTest : TestDB() {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    lateinit var realm: Realm
    private lateinit var repo: DefaultAppAuthenticationRepository
    private lateinit var localDataSource: AppAuthenticationLocalDataSource

    @Before
    fun setUp() {
        realm = Realm.open(
            RealmConfiguration.Builder(
                schema = setOf(
                    SettingsEntityV1::class,
                    PharmacySearchEntityV1::class,
                    PasswordEntityV1::class,
                    ShippingContactEntityV1::class,
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

        localDataSource = AppAuthenticationLocalDataSourceV1(realm)

        repo = DefaultAppAuthenticationRepository(
            dispatchers = StandardTestDispatcher(),
            appAuthenticationLocalDataSource = localDataSource
        )
    }

    @Test
    fun `authentication mode`() = runTest {
        repo.getAppAuthenticationErpModel().first().also {
            assertTrue {
                it.authenticationMethod is AppAuthenticationMethodErpModel.NotInitialised
            }
        }

        repo.enableDeviceSecurity()
        repo.getAppAuthenticationErpModel().first().also {
            assertTrue {
                it.authenticationMethod is AppAuthenticationMethodErpModel.DeviceSecurity
            }
        }

        repo.disableDeviceSecurity()
        repo.setPassword(AppAuthenticationPasswordErpModel.fromPassword("password"))
        repo.getAppAuthenticationErpModel().first().also {
            assertTrue {
                it.authenticationMethod is AppAuthenticationMethodErpModel.Password
            }

            val method = it.authenticationMethod as? AppAuthenticationMethodErpModel.Password
            method.let { method ->
                assertEquals(false, method?.password?.isValid("Test123456"))
                assertEquals(true, method?.password?.isValid("password"))
            }
        }
    }

    @Test
    fun `authentication mode set to both`() = runTest {
        repo.setPassword(AppAuthenticationPasswordErpModel.fromPassword("password"))
        realm.queryFirst<AuthenticationPasswordEntityV1>()!!.also {
            assertEquals(true, it.hash.isNotEmpty())
            assertEquals(true, it.salt.isNotEmpty())
        }

        repo.enableDeviceSecurity()
        repo.getAppAuthenticationErpModel().first().also {
            assertTrue {
                it.authenticationMethod is AppAuthenticationMethodErpModel.Both
            }
        }

        realm.queryFirst<AuthenticationPasswordEntityV1>()!!.also {
            assertEquals(true, it.hash.isNotEmpty())
            assertEquals(true, it.salt.isNotEmpty())
        }
    }

    @Test
    fun `number of authentication failures`() = runTest {
        repo.getAppAuthenticationErpModel().first().also {
            assertEquals(0, it.authenticationFailure.failedAttempts)
        }

        repo.incrementNumberOfAuthenticationFailures()
        repo.incrementNumberOfAuthenticationFailures()
        repo.getAppAuthenticationErpModel().first().also {
            assertEquals(2, it.authenticationFailure.failedAttempts)
        }

        repo.resetNumberOfAuthenticationFailures()
        repo.getAppAuthenticationErpModel().first().also {
            assertEquals(0, it.authenticationFailure.failedAttempts)
        }
    }
}
