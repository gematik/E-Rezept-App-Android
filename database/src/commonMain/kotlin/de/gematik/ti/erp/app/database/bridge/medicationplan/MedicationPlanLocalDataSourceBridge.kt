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

package de.gematik.ti.erp.app.database.bridge.medicationplan

import de.gematik.ti.erp.app.base.utils.getCurrentMethodName
import de.gematik.ti.erp.app.database.api.MedicationPlanLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.database.datastore.featuretoggle.RoomFeatureToggle
import de.gematik.ti.erp.app.debug.model.DbMigrationFunctionalState
import de.gematik.ti.erp.app.debug.model.DbMigrationLogEntry
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDurationErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel.Companion.toJson
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleIntervalErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosageErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalTime

class MedicationPlanLocalDataSourceBridge(
    private val localDataSourceV1: MedicationPlanLocalDataSource,
    private val localDataSourceV2: MedicationPlanLocalDataSource,
    private val logger: DbMigrationLogHolder,
    private val roomFeatureToggle: RoomFeatureToggle
) : MedicationPlanLocalDataSource {

    private val useRoom: Boolean get() = roomFeatureToggle.isEnabled()

    suspend fun compareAndLogAllMedicationSchedules() { // TODO add to debug logging buttons when merged
        val dataFromRealmDb = localDataSourceV1.getAllMedicationSchedules().firstOrNull() ?: emptyList()
        val dataFromRoomDb = localDataSourceV2.getAllMedicationSchedules().firstOrNull() ?: emptyList()
        logger.addLog(
            DbMigrationLogEntry(
                operation = "compareAndLogAllMedicationSchedules",
                usesRoom = useRoom,
                functionalState = DbMigrationFunctionalState.CheckFunctionalityForDifferentModels,
                roomData = dataFromRoomDb.map { it.toJson() }.toString(),
                realmData = dataFromRealmDb.map { it.toJson() }.toString()
            )
        )
    }

    override fun getMedicationSchedule(taskId: String): Flow<MedicationScheduleErpModel?> {
        return when {
            useRoom -> localDataSourceV2.getMedicationSchedule(taskId)
            else -> localDataSourceV1.getMedicationSchedule(taskId)
        }
    }

    override fun getAllMedicationSchedules(): Flow<List<MedicationScheduleErpModel>> {
        return when {
            useRoom -> localDataSourceV2.getAllMedicationSchedules()
            else -> localDataSourceV1.getAllMedicationSchedules()
        }
    }

    override fun getMedicationScheduleNotification(notificationId: String): Flow<MedicationScheduleNotificationErpModel?> {
        return when {
            useRoom -> localDataSourceV2.getMedicationScheduleNotification(notificationId)
            else -> localDataSourceV1.getMedicationScheduleNotification(notificationId)
        }
    }

    override suspend fun deleteMedicationSchedule(taskId: String) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> localDataSourceV2.deleteMedicationSchedule(taskId)
            else -> localDataSourceV1.deleteMedicationSchedule(taskId)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = DbMigrationFunctionalState.OperationNoCheck,
                        roomData = if (useRoom) "deleting with taskId = $taskId" else null,
                        realmData = if (!useRoom) "deleting with taskId = $taskId" else null
                    )
                )
            }
        }
    }

    override suspend fun setOrCreateActiveMedicationSchedule(medicationScheduleErpModel: MedicationScheduleErpModel) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> localDataSourceV2.setOrCreateActiveMedicationSchedule(medicationScheduleErpModel)
            else -> localDataSourceV1.setOrCreateActiveMedicationSchedule(medicationScheduleErpModel)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = DbMigrationFunctionalState.OperationNoCheck,
                        roomData = if (useRoom) "setOrCreateActiveMedicationSchedule for taskId = ${medicationScheduleErpModel.taskId}" else null,
                        realmData = if (!useRoom) "setOrCreateActiveMedicationSchedule for taskId = ${medicationScheduleErpModel.taskId}" else null
                    )
                )
            }
        }
    }

    override suspend fun deactivateMedicationSchedule(taskId: String) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> localDataSourceV2.deactivateMedicationSchedule(taskId)
            else -> localDataSourceV1.deactivateMedicationSchedule(taskId)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = DbMigrationFunctionalState.OperationNoCheck,
                        roomData = if (useRoom) "deactivate for taskId = $taskId" else null,
                        realmData = if (!useRoom) "deactivate for taskId = $taskId" else null
                    )
                )
            }
        }
    }

    override suspend fun setMedicationScheduleDuration(
        taskId: String,
        medicationScheduleDurationErpModel: MedicationScheduleDurationErpModel
    ) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> localDataSourceV2.setMedicationScheduleDuration(taskId, medicationScheduleDurationErpModel)
            else -> localDataSourceV1.setMedicationScheduleDuration(taskId, medicationScheduleDurationErpModel)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = DbMigrationFunctionalState.OperationNoCheck,
                        roomData = if (useRoom) "set duration for taskId = $taskId" else null,
                        realmData = if (!useRoom) "set duration for taskId = = $taskId" else null
                    )
                )
            }
        }
    }

    override suspend fun setMedicationScheduleInterval(
        taskId: String,
        medicationScheduleIntervalErpModel: MedicationScheduleIntervalErpModel
    ) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> localDataSourceV2.setMedicationScheduleInterval(taskId, medicationScheduleIntervalErpModel)
            else -> localDataSourceV1.setMedicationScheduleInterval(taskId, medicationScheduleIntervalErpModel)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = DbMigrationFunctionalState.OperationNoCheck,
                        roomData = if (useRoom) "set interval for taskId = $taskId" else null,
                        realmData = if (!useRoom) "set interval for taskId = = $taskId" else null
                    )
                )
            }
        }
    }

    override suspend fun setOrCreateMedicationScheduleNotification(
        taskId: String,
        medicationScheduleNotificationErpModel: MedicationScheduleNotificationErpModel
    ) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> localDataSourceV2.setOrCreateMedicationScheduleNotification(taskId, medicationScheduleNotificationErpModel)
            else -> localDataSourceV1.setOrCreateMedicationScheduleNotification(taskId, medicationScheduleNotificationErpModel)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = DbMigrationFunctionalState.OperationNoCheck,
                        roomData = if (useRoom) "set notification for taskId = $taskId" else null,
                        realmData = if (!useRoom) "set notification for taskId = = $taskId" else null
                    )
                )
            }
        }
    }

    override suspend fun deleteMedicationScheduleNotification(medicationScheduleNotificationId: String) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> localDataSourceV2.deleteMedicationScheduleNotification(medicationScheduleNotificationId)
            else -> localDataSourceV1.deleteMedicationScheduleNotification(medicationScheduleNotificationId)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = DbMigrationFunctionalState.OperationNoCheck,
                        roomData = if (useRoom) "delete for notificationId = $medicationScheduleNotificationId" else null,
                        realmData = if (!useRoom) "delete for notificationId = = $medicationScheduleNotificationId" else null
                    )
                )
            }
        }
    }

    override suspend fun setMedicationScheduleNotificationDosage(
        medicationScheduleNotificationId: String,
        dosage: MedicationScheduleNotificationDosageErpModel
    ) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> localDataSourceV2.setMedicationScheduleNotificationDosage(medicationScheduleNotificationId, dosage)
            else -> localDataSourceV1.setMedicationScheduleNotificationDosage(medicationScheduleNotificationId, dosage)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = DbMigrationFunctionalState.OperationNoCheck,
                        roomData = if (useRoom) "set dosage for notificationId = $medicationScheduleNotificationId" else null,
                        realmData = if (!useRoom) "set dosage for notificationId = = $medicationScheduleNotificationId" else null
                    )
                )
            }
        }
    }

    override suspend fun setMedicationScheduleNotificationTime(medicationScheduleNotificationId: String, time: LocalTime) {
        val operationName = getCurrentMethodName()
        when {
            useRoom -> localDataSourceV2.setMedicationScheduleNotificationTime(medicationScheduleNotificationId, time)
            else -> localDataSourceV1.setMedicationScheduleNotificationTime(medicationScheduleNotificationId, time)
        }.also {
            run {
                logger.addLog(
                    DbMigrationLogEntry(
                        operation = operationName,
                        usesRoom = useRoom,
                        functionalState = DbMigrationFunctionalState.OperationNoCheck,
                        roomData = if (useRoom) "set duration for notificationId = $medicationScheduleNotificationId" else null,
                        realmData = if (!useRoom) "set duration for notificationId = = $medicationScheduleNotificationId" else null
                    )
                )
            }
        }
    }
}
