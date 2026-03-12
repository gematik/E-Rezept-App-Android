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
package de.gematik.ti.erp.app.database.bridge.truststore

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.TrustStoreLocalDataSource
import de.gematik.ti.erp.app.logger.DbMigrationFunctionalState.CheckFunctionalityForDifferentModels
import de.gematik.ti.erp.app.logger.DbMigrationFunctionalState.OperationNoCheck
import de.gematik.ti.erp.app.logger.DbMigrationLogEntry
import de.gematik.ti.erp.app.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.vau.model.TrustStoreErpModel
import de.gematik.ti.erp.app.vau.model.TrustStoreErpModel.Companion.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

internal class TrustStoreLocalDataSourceBridge(
    private val trustStoreLocalDataSourceV1: TrustStoreLocalDataSource,
    private val trustStoreLocalDataSourceV2: TrustStoreLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val useRoom: Boolean
) : TrustStoreLocalDataSource {

    override suspend fun saveCertificateAndOcspLists(certListJson: String, ocspListJson: String) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> trustStoreLocalDataSourceV2.saveCertificateAndOcspLists(certListJson, ocspListJson)
            else -> trustStoreLocalDataSourceV1.saveCertificateAndOcspLists(certListJson, ocspListJson)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = OperationNoCheck,
                        roomData = if (useRoom) "saving certListJson and ocspListJson" else null,
                        realmData = if (!useRoom) "saving certListJson and ocspListJson" else null
                    )
                )
            }
        }
    }

    override fun loadUntrusted(): Flow<TrustStoreErpModel?> {
        val operationName = getCurrentMethodName()
        return when {
            useRoom -> trustStoreLocalDataSourceV2.loadUntrusted()
            else -> trustStoreLocalDataSourceV1.loadUntrusted()
        }.also {
            runBlocking(Dispatchers.IO) {
                val dataFromRealmDb = trustStoreLocalDataSourceV1.loadUntrusted().firstOrNull()
                val dataFromRoomDb = trustStoreLocalDataSourceV2.loadUntrusted().firstOrNull()
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

    override suspend fun deleteAll() {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> trustStoreLocalDataSourceV2.deleteAll()
            else -> trustStoreLocalDataSourceV1.deleteAll()
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = OperationNoCheck,
                        roomData = if (useRoom) "deleting all trust store data" else null,
                        realmData = if (!useRoom) "deleting all trust store data" else null
                    )
                )
            }
        }
    }
}
