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

package de.gematik.ti.erp.app.database.datastore.appauthentication

import androidx.datastore.core.DataStore
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationPasswordErpModel
import de.gematik.ti.erp.app.database.api.AppAuthenticationLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal const val APP_AUTHENTICATION_DATA_SOURCE = "AppAuthentication"

class AppAuthenticationLocalDataSourceV2(
    private val dataStore: DataStore<ErpAppAuthenticationEntitySchema>
) : AppAuthenticationLocalDataSource {
    override fun getAppAuthenticationErpModel(): Flow<AppAuthenticationErpModel> =
        dataStore.data.map { entitySchema -> entitySchema.entity.toAppAuthenticationErpModel() }

    override suspend fun initialiseAppAuthenticationEntity(model: AppAuthenticationErpModel) {
        dataStore.updateData { currentSchema ->
            currentSchema.copy(
                entity = model.toErpAppAuthenticationEntity()
            )
        }
    }

    override suspend fun enableDeviceSecurity() {
        dataStore.updateData { currentSchema ->
            val updatedEntity = currentSchema.entity.copy(
                deviceSecurity = true
            )
            currentSchema.copy(
                entity = updatedEntity
            )
        }
    }

    override suspend fun disableDeviceSecurity() {
        dataStore.updateData { currentSchema ->
            val updatedEntity = currentSchema.entity.copy(
                deviceSecurity = false
            )
            currentSchema.copy(
                entity = updatedEntity
            )
        }
    }

    override suspend fun setPassword(password: AppAuthenticationPasswordErpModel) {
        dataStore.updateData { currentSchema ->
            val updatedEntity = currentSchema.entity.copy(
                password = password.toErpAppAuthenticationPasswordEntity()
            )
            currentSchema.copy(
                entity = updatedEntity
            )
        }
    }

    override suspend fun resetPassword() {
        dataStore.updateData { currentSchema ->
            val updatedEntity = currentSchema.entity.copy(
                password = null
            )
            currentSchema.copy(
                entity = updatedEntity
            )
        }
    }

    @Requirement(
        "O.Pass_4#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Increments the number of authentication failures when the user fails to authenticate."
    )
    override suspend fun incrementNumberOfAuthenticationFailures() {
        dataStore.updateData { currentSchema ->
            val updatedEntity = currentSchema.entity.copy(
                failedAuthenticationAttempts = currentSchema.entity.failedAuthenticationAttempts + 1
            )
            currentSchema.copy(
                entity = updatedEntity
            )
        }
    }

    override suspend fun resetNumberOfAuthenticationFailures() {
        dataStore.updateData { currentSchema ->
            val updatedEntity = currentSchema.entity.copy(
                failedAuthenticationAttempts = 0
            )
            currentSchema.copy(
                entity = updatedEntity
            )
        }
    }

    override suspend fun setAuthenticationTimeOutSystemUptime(systemUptime: Long) {
        dataStore.updateData { currentSchema ->
            val updatedEntity = currentSchema.entity.copy(
                authenticationTimeOutSystemUptime = systemUptime
            )
            currentSchema.copy(
                entity = updatedEntity
            )
        }
    }

    override suspend fun resetAuthenticationTimeOutSystemUptime() {
        dataStore.updateData { currentSchema ->
            val updatedEntity = currentSchema.entity.copy(
                authenticationTimeOutSystemUptime = null
            )
            currentSchema.copy(
                entity = updatedEntity
            )
        }
    }
}
