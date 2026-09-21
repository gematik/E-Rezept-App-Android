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

package de.gematik.ti.erp.app.database.bridge.pharmacy

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.pharmacy.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.pharmacy.model.SearchAccessTokenErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

internal class PharmacySearchAccessTokenLocalDataSourceBridge(
    private val pharmacySearchAccessTokenLocalDataSourceV1: PharmacySearchAccessTokenLocalDataSource,
    private val pharmacySearchAccessTokenLocalDataSourceV2: PharmacySearchAccessTokenLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : PharmacySearchAccessTokenLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    override val searchAccessToken: Flow<SearchAccessTokenErpModel?>
        get() {
            val operationName = getCurrentMethodName()
            return when {
                useRoom -> pharmacySearchAccessTokenLocalDataSourceV2.searchAccessToken
                else -> pharmacySearchAccessTokenLocalDataSourceV1.searchAccessToken
            }.also {
                logger.logOperation(operationName, useRoom)
            }
        }

    override suspend fun saveToken(token: String, currentTime: Instant) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> pharmacySearchAccessTokenLocalDataSourceV2.saveToken(token)
            else -> pharmacySearchAccessTokenLocalDataSourceV1.saveToken(token)
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }

    override suspend fun clearToken() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> pharmacySearchAccessTokenLocalDataSourceV2.clearToken()
            else -> pharmacySearchAccessTokenLocalDataSourceV1.clearToken()
        }.also {
            run {
                logger.logOperation(operationName, useRoom)
            }
        }
    }
}
