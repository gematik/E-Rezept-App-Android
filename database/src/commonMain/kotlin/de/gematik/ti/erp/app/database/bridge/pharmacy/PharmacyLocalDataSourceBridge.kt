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
import de.gematik.ti.erp.app.database.api.PharmacyLocalDataSource
import de.gematik.ti.erp.app.logger.DbMigrationFunctionalState.CheckFunctionalityForDifferentModels
import de.gematik.ti.erp.app.logger.DbMigrationFunctionalState.OperationNoCheck
import de.gematik.ti.erp.app.logger.DbMigrationLogEntry
import de.gematik.ti.erp.app.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.pharmacy.model.PharmacyErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyErpModel.Companion.toJson
import de.gematik.ti.erp.app.pharmacy.model.TelematikId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

internal class PharmacyLocalDataSourceBridge(
    private val pharmacyLocalDataSourceV1: PharmacyLocalDataSource,
    private val pharmacyLocalDataSourceV2: PharmacyLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val useRoom: Boolean
) : PharmacyLocalDataSource {
    override fun loadPharmacies(): Flow<List<PharmacyErpModel>> {
        val operationName = getCurrentMethodName()
        return when {
            useRoom -> pharmacyLocalDataSourceV2.loadPharmacies()
            else -> pharmacyLocalDataSourceV1.loadPharmacies()
        }.also {
            runBlocking(Dispatchers.IO) {
                val dataFromRealmDb = pharmacyLocalDataSourceV1.loadPharmacies().firstOrNull()
                val dataFromRoomDb = pharmacyLocalDataSourceV2.loadPharmacies().firstOrNull()
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = CheckFunctionalityForDifferentModels,
                        roomData = dataFromRoomDb?.map { it.toJson() }.toString(),
                        realmData = dataFromRealmDb?.map { it.toJson() }.toString()
                    )
                )
            }
        }
    }

    override fun getPharmacy(telematikId: TelematikId): Flow<PharmacyErpModel?> {
        return when {
            useRoom -> pharmacyLocalDataSourceV2.getPharmacy(telematikId)
            else -> pharmacyLocalDataSourceV1.getPharmacy(telematikId)
        }
    }

    override suspend fun deletePharmacy(telematikId: TelematikId) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> pharmacyLocalDataSourceV2.deletePharmacy(telematikId)
            else -> pharmacyLocalDataSourceV1.deletePharmacy(telematikId)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = OperationNoCheck,
                        roomData = if (useRoom) "deleting with telematikId = ${telematikId.value}" else null,
                        realmData = if (!useRoom) "deleting with telematikId = ${telematikId.value}" else null
                    )
                )
            }
        }
    }

    override suspend fun deleteFavoritePharmacy(telematikId: TelematikId) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> pharmacyLocalDataSourceV2.deleteFavoritePharmacy(telematikId)
            else -> pharmacyLocalDataSourceV1.deleteFavoritePharmacy(telematikId)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = OperationNoCheck,
                        roomData = if (useRoom) "demark favorite or delete with telematikId = ${telematikId.value}" else null,
                        realmData = if (!useRoom) "demark favorite or delete with telematikId = ${telematikId.value}" else null
                    )
                )
            }
        }
    }

    override suspend fun deleteOftenUsedPharmacy(telematikId: TelematikId) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> pharmacyLocalDataSourceV2.deleteOftenUsedPharmacy(telematikId)
            else -> pharmacyLocalDataSourceV1.deleteOftenUsedPharmacy(telematikId)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = OperationNoCheck,
                        roomData = if (useRoom) "demark oftenUsed or delete with telematikId = ${telematikId.value}" else null,
                        realmData = if (!useRoom) "demark oftenUsed or delete with telematikId = ${telematikId.value}" else null
                    )
                )
            }
        }
    }

    override suspend fun markPharmacyAsFavourite(pharmacy: PharmacyErpModel) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> pharmacyLocalDataSourceV2.markPharmacyAsFavourite(pharmacy)
            else -> pharmacyLocalDataSourceV1.markPharmacyAsFavourite(pharmacy)
        }.also {
            run {
                val dataFromRealmDb = pharmacyLocalDataSourceV1.getPharmacy(TelematikId(pharmacy.telematikId)).firstOrNull()
                val dataFromRoomDb = pharmacyLocalDataSourceV2.getPharmacy(TelematikId(pharmacy.telematikId)).firstOrNull()
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

    override suspend fun markPharmacyAsOftenUsed(pharmacy: PharmacyErpModel) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> pharmacyLocalDataSourceV2.markPharmacyAsOftenUsed(pharmacy)
            else -> pharmacyLocalDataSourceV1.markPharmacyAsOftenUsed(pharmacy)
        }.also {
            run {
                val dataFromRealmDb = pharmacyLocalDataSourceV1.getPharmacy(TelematikId(pharmacy.telematikId)).firstOrNull()
                val dataFromRoomDb = pharmacyLocalDataSourceV2.getPharmacy(TelematikId(pharmacy.telematikId)).firstOrNull()
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        functionalState = OperationNoCheck,
                        usesRoom = useRoom,
                        roomData = dataFromRoomDb?.toJson(),
                        realmData = dataFromRealmDb?.toJson()
                    )
                )
            }
        }
    }

    override fun isPharmacyInFavorites(pharmacy: PharmacyErpModel): Flow<Boolean> {
        val operationName = getCurrentMethodName()
        return when {
            useRoom -> pharmacyLocalDataSourceV2.isPharmacyInFavorites(pharmacy)
            else -> pharmacyLocalDataSourceV1.isPharmacyInFavorites(pharmacy)
        }.also {
            runBlocking(Dispatchers.IO) {
                val dataFromRealmDb = pharmacyLocalDataSourceV1.getPharmacy(TelematikId(pharmacy.telematikId)).firstOrNull()
                val dataFromRoomDb = pharmacyLocalDataSourceV2.getPharmacy(TelematikId(pharmacy.telematikId)).firstOrNull()
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        functionalState = OperationNoCheck,
                        usesRoom = useRoom,
                        roomData = dataFromRoomDb?.toJson(),
                        realmData = dataFromRealmDb?.toJson()
                    )
                )
            }
        }
    }

    override fun isPharmacyOftenUsed(pharmacy: PharmacyErpModel): Flow<Boolean> {
        val operationName = getCurrentMethodName()
        return when {
            useRoom -> pharmacyLocalDataSourceV2.isPharmacyOftenUsed(pharmacy)
            else -> pharmacyLocalDataSourceV1.isPharmacyOftenUsed(pharmacy)
        }.also {
            runBlocking(Dispatchers.IO) {
                val dataFromRealmDb = pharmacyLocalDataSourceV1.getPharmacy(TelematikId(pharmacy.telematikId)).firstOrNull()
                val dataFromRoomDb = pharmacyLocalDataSourceV2.getPharmacy(TelematikId(pharmacy.telematikId)).firstOrNull()
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        functionalState = OperationNoCheck,
                        usesRoom = useRoom,
                        roomData = dataFromRoomDb?.toJson(),
                        realmData = dataFromRealmDb?.toJson()
                    )
                )
            }
        }
    }
}
