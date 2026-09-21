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

package de.gematik.ti.erp.app.appauthentication.repository

import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationFailureErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationMethodErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationPasswordErpModel
import de.gematik.ti.erp.app.database.api.AppAuthenticationLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class DefaultAppAuthenticationRepository(
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO,
    private val appAuthenticationLocalDataSource: AppAuthenticationLocalDataSource
) : AppAuthenticationRepository {
    override fun getAppAuthenticationErpModel(): Flow<AppAuthenticationErpModel> =
        appAuthenticationLocalDataSource.getAppAuthenticationErpModel().flowOn(dispatchers)

    override suspend fun initialiseAppAuthenticationWithChosenMethod(method: AppAuthenticationMethodErpModel) =
        appAuthenticationLocalDataSource.initialiseAppAuthenticationEntity(
            AppAuthenticationErpModel(
                authenticationMethod = method,
                authenticationFailure = AppAuthenticationFailureErpModel()
            )
        )

    override suspend fun enableDeviceSecurity() =
        appAuthenticationLocalDataSource.enableDeviceSecurity()

    override suspend fun disableDeviceSecurity() =
        appAuthenticationLocalDataSource.disableDeviceSecurity()

    override suspend fun setPassword(password: AppAuthenticationPasswordErpModel) =
        appAuthenticationLocalDataSource.setPassword(password)

    override suspend fun resetPassword() =
        appAuthenticationLocalDataSource.resetPassword()

    override suspend fun incrementNumberOfAuthenticationFailures() =
        appAuthenticationLocalDataSource.incrementNumberOfAuthenticationFailures()

    override suspend fun resetNumberOfAuthenticationFailures() =
        appAuthenticationLocalDataSource.resetNumberOfAuthenticationFailures()

    override suspend fun setAuthenticationTimeOutSystemUptime(systemUptime: Long) =
        appAuthenticationLocalDataSource.setAuthenticationTimeOutSystemUptime(systemUptime)

    override suspend fun resetAuthenticationTimeOutSystemUptime() =
        appAuthenticationLocalDataSource.resetAuthenticationTimeOutSystemUptime()
}
