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
package de.gematik.ti.erp.app.database.bridge.accesstoken

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.logger.DbMigrationFunctionalState.CheckFunctionalityForDifferentModels
import de.gematik.ti.erp.app.logger.DbMigrationFunctionalState.OperationNoCheck
import de.gematik.ti.erp.app.logger.DbMigrationLogEntry
import de.gematik.ti.erp.app.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.pharmacy.model.SearchAccessTokenErpModel
import de.gematik.ti.erp.app.pharmacy.model.SearchAccessTokenErpModel.Companion.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant

internal class PharmacySearchAccessTokenLocalDataSourceBridge(
    private val pharmacySearchAccessTokenLocalDataSourceV1: PharmacySearchAccessTokenLocalDataSource,
    private val pharmacySearchAccessTokenLocalDataSourceV2: PharmacySearchAccessTokenLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val useRoom: Boolean
) : PharmacySearchAccessTokenLocalDataSource {

    override val searchAccessToken: Flow<SearchAccessTokenErpModel?>
        get() {
            val operationName = getCurrentMethodName()
            return when {
                useRoom -> pharmacySearchAccessTokenLocalDataSourceV2.searchAccessToken
                else -> pharmacySearchAccessTokenLocalDataSourceV1.searchAccessToken
            }.also {
                runBlocking(Dispatchers.IO) {
                    val dataFromRoomDb = pharmacySearchAccessTokenLocalDataSourceV2.searchAccessToken.firstOrNull()
                    val dataFromRealmDb = pharmacySearchAccessTokenLocalDataSourceV1.searchAccessToken.firstOrNull()
                    logger.addLog(
                        DbMigrationLogEntry(
                            operation = operationName,
                            usesRoom = useRoom,
                            functionalState = CheckFunctionalityForDifferentModels,
                            roomData = dataFromRoomDb?.toJson(),
                            realmData = dataFromRealmDb?.toJson()
                        )
                    )
                }
            }
        }

    override suspend fun saveToken(token: String, currentTime: Instant) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> pharmacySearchAccessTokenLocalDataSourceV2.saveToken(token)
            else -> pharmacySearchAccessTokenLocalDataSourceV1.saveToken(token)
        }.also {
            run {
                val dataFromRoomDb = pharmacySearchAccessTokenLocalDataSourceV2.searchAccessToken.firstOrNull()
                val dataFromRealmDb = pharmacySearchAccessTokenLocalDataSourceV1.searchAccessToken.firstOrNull()
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = OperationNoCheck,
                        roomData = dataFromRoomDb?.toJson(),
                        realmData = dataFromRealmDb?.toJson()
                    )
                )
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
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = OperationNoCheck,
                        roomData = if (useRoom) "clearing token" else null,
                        realmData = if (!useRoom) "clearing token" else null
                    )
                )
            }
        }
    }
}
