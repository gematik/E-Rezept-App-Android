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

package de.gematik.ti.erp.app.database.realm.v1.appauthentication

import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationPasswordErpModel
import de.gematik.ti.erp.app.database.api.AppAuthenticationLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.writeToRealm
import de.gematik.ti.erp.app.database.realm.v1.settings.SettingsEntityV1
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class AppAuthenticationLocalDataSourceV1(private val realm: Realm) : AppAuthenticationLocalDataSource {
    override fun getAppAuthenticationErpModel(): Flow<AppAuthenticationErpModel> =
        realm.query<SettingsEntityV1>().asFlow().mapNotNull { query ->
            query.list.first().let {
                it.authentication?.toAppAuthenticationErpModel()
            }
        }

    override suspend fun initialiseAppAuthenticationEntity(model: AppAuthenticationErpModel) {
        realm.writeToRealm<SettingsEntityV1, Unit> { settings ->
            settings.authentication = model.toAuthenticationEntityV1()
        }
    }

    override suspend fun enableDeviceSecurity() {
        realm.writeToRealm<SettingsEntityV1, Unit> { settings ->
            settings.authentication?.let { it.deviceSecurity = true }
        }
    }

    override suspend fun disableDeviceSecurity() {
        realm.writeToRealm<SettingsEntityV1, Unit> { settings ->
            settings.authentication?.let { it.deviceSecurity = false }
        }
    }

    override suspend fun setPassword(password: AppAuthenticationPasswordErpModel) {
        realm.writeToRealm<SettingsEntityV1, Unit> { settings ->
            settings.authentication?.let { auth ->
                auth.password = password.let {
                    AuthenticationPasswordEntityV1().apply {
                        this.hash = it.hash
                        this.salt = it.salt
                    }
                }
            }
        }
    }

    override suspend fun resetPassword() {
        realm.writeToRealm<SettingsEntityV1, Unit> { settings ->
            settings.authentication?.let { it.password = null }
        }
    }

    override suspend fun incrementNumberOfAuthenticationFailures() {
        realm.writeToRealm<SettingsEntityV1, Unit> { settings ->
            settings.authentication?.let { it.failedAuthenticationAttempts += 1 }
        }
    }

    override suspend fun resetNumberOfAuthenticationFailures() {
        realm.writeToRealm<SettingsEntityV1, Unit> { settings ->
            settings.authentication?.let { it.failedAuthenticationAttempts = 0 }
        }
    }

    override suspend fun setAuthenticationTimeOutSystemUptime(systemUptime: Long) {
        realm.writeToRealm<SettingsEntityV1, Unit> { settings ->
            settings.authentication?.let { it.authenticationTimeOutSystemUptime = systemUptime }
        }
    }

    override suspend fun resetAuthenticationTimeOutSystemUptime() {
        realm.writeToRealm<SettingsEntityV1, Unit> { settings ->
            settings.authentication?.let { it.authenticationTimeOutSystemUptime = null }
        }
    }
}
