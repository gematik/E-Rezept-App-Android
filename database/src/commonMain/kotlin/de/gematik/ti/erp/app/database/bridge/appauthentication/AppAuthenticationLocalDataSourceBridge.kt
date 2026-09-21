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

package de.gematik.ti.erp.app.database.bridge.appauthentication

import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationPasswordErpModel
import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.AppAuthenticationLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import kotlinx.coroutines.flow.Flow

class AppAuthenticationLocalDataSourceBridge(
    private val appAuthenticationLocalDataSourceV1: AppAuthenticationLocalDataSource,
    private val appAuthenticationLocalDataSourceV2: AppAuthenticationLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : AppAuthenticationLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    override fun getAppAuthenticationErpModel(): Flow<AppAuthenticationErpModel> {
        return when {
            useRoom -> appAuthenticationLocalDataSourceV2.getAppAuthenticationErpModel()
            else -> appAuthenticationLocalDataSourceV1.getAppAuthenticationErpModel()
        }
    }

    override suspend fun initialiseAppAuthenticationEntity(model: AppAuthenticationErpModel) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> appAuthenticationLocalDataSourceV2.initialiseAppAuthenticationEntity(model)
            else -> appAuthenticationLocalDataSourceV1.initialiseAppAuthenticationEntity(model)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun enableDeviceSecurity() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> appAuthenticationLocalDataSourceV2.enableDeviceSecurity()
            else -> appAuthenticationLocalDataSourceV1.enableDeviceSecurity()
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun disableDeviceSecurity() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> appAuthenticationLocalDataSourceV2.disableDeviceSecurity()
            else -> appAuthenticationLocalDataSourceV1.disableDeviceSecurity()
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun setPassword(password: AppAuthenticationPasswordErpModel) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> appAuthenticationLocalDataSourceV2.setPassword(password)
            else -> appAuthenticationLocalDataSourceV1.setPassword(password)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun resetPassword() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> appAuthenticationLocalDataSourceV2.resetPassword()
            else -> appAuthenticationLocalDataSourceV1.resetPassword()
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun incrementNumberOfAuthenticationFailures() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> appAuthenticationLocalDataSourceV2.incrementNumberOfAuthenticationFailures()
            else -> appAuthenticationLocalDataSourceV1.incrementNumberOfAuthenticationFailures()
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun resetNumberOfAuthenticationFailures() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> appAuthenticationLocalDataSourceV2.resetNumberOfAuthenticationFailures()
            else -> appAuthenticationLocalDataSourceV1.resetNumberOfAuthenticationFailures()
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun setAuthenticationTimeOutSystemUptime(systemUptime: Long) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> appAuthenticationLocalDataSourceV2.setAuthenticationTimeOutSystemUptime(systemUptime)
            else -> appAuthenticationLocalDataSourceV1.setAuthenticationTimeOutSystemUptime(systemUptime)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun resetAuthenticationTimeOutSystemUptime() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> appAuthenticationLocalDataSourceV2.resetAuthenticationTimeOutSystemUptime()
            else -> appAuthenticationLocalDataSourceV1.resetAuthenticationTimeOutSystemUptime()
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }
}
