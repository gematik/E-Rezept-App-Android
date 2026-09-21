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

package de.gematik.ti.erp.app.database.bridge.userauthentication

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.UserAuthenticationLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.coroutines.flow.Flow

class UserAuthenticationLocalDataSourceBridge(
    private val userAuthenticationLocalDataSourceV1: UserAuthenticationLocalDataSource,
    private val userAuthenticationLocalDataSourceV2: UserAuthenticationLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : UserAuthenticationLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    override fun getUserAuthenticationForProfile(profileIdentifier: ProfileIdentifier): Flow<UserAuthenticationErpModel> {
        return when {
            useRoom -> userAuthenticationLocalDataSourceV2.getUserAuthenticationForProfile(profileIdentifier)
            else -> userAuthenticationLocalDataSourceV1.getUserAuthenticationForProfile(profileIdentifier)
        }
    }

    override suspend fun saveUserAuthenticationForProfile(
        profileIdentifier: ProfileIdentifier,
        userAuthentication: UserAuthenticationErpModel
    ) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> userAuthenticationLocalDataSourceV2.saveUserAuthenticationForProfile(profileIdentifier, userAuthentication)
            else -> userAuthenticationLocalDataSourceV1.saveUserAuthenticationForProfile(profileIdentifier, userAuthentication)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun invalidateSingleSignOnTokenForProfile(profileIdentifier: ProfileIdentifier) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> userAuthenticationLocalDataSourceV2.invalidateSingleSignOnTokenForProfile(profileIdentifier)
            else -> userAuthenticationLocalDataSourceV1.invalidateSingleSignOnTokenForProfile(profileIdentifier)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun deleteUserAuthenticationDataForProfile(profileIdentifier: ProfileIdentifier) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> userAuthenticationLocalDataSourceV2.deleteUserAuthenticationDataForProfile(profileIdentifier)
            else -> userAuthenticationLocalDataSourceV1.deleteUserAuthenticationDataForProfile(profileIdentifier)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }
}
