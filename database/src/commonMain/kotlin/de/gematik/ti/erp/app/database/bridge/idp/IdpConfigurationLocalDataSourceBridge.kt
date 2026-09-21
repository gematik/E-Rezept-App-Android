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

package de.gematik.ti.erp.app.database.bridge.idp

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.IdpConfigurationLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.idp.IdpConfigurationErpModel

class IdpConfigurationLocalDataSourceBridge(
    private val userAuthenticationLocalDataSourceV1: IdpConfigurationLocalDataSource,
    private val userAuthenticationLocalDataSourceV2: IdpConfigurationLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : IdpConfigurationLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    override suspend fun getIdpConfiguration(): IdpConfigurationErpModel? {
        return when {
            useRoom -> userAuthenticationLocalDataSourceV2.getIdpConfiguration()
            else -> userAuthenticationLocalDataSourceV1.getIdpConfiguration()
        }
    }

    override suspend fun saveIdpConfiguration(idpConfiguration: IdpConfigurationErpModel) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> userAuthenticationLocalDataSourceV2.saveIdpConfiguration(idpConfiguration)
            else -> userAuthenticationLocalDataSourceV1.saveIdpConfiguration(idpConfiguration)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun invalidateIdpConfiguration() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> userAuthenticationLocalDataSourceV2.invalidateIdpConfiguration()
            else -> userAuthenticationLocalDataSourceV1.invalidateIdpConfiguration()
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }
}
