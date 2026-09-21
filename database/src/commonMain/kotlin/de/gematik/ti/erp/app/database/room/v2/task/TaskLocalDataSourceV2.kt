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

package de.gematik.ti.erp.app.database.room.v2.task

import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSource
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceCommon
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceDiga
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceScanned
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceSynced
import de.gematik.ti.erp.app.database.room.v2.invoice.InvoiceDao
import de.gematik.ti.erp.app.database.room.v2.task.communication.CommunicationDao
import de.gematik.ti.erp.app.database.room.v2.task.insuranceinformation.InsuranceInformationDao
import de.gematik.ti.erp.app.database.room.v2.task.mappers.accidentInfoIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.deviceRequestIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.dispensesMedicationIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.ingredientIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.medicationIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.medicationRequestIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.multiplePrescriptionIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toRoomEntity
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toRoomEntityBundle
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.IngredientDao
import de.gematik.ti.erp.app.database.room.v2.task.medication.MedicationDispenseDao
import de.gematik.ti.erp.app.database.room.v2.task.organization.OrganizationDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefsDao
import de.gematik.ti.erp.app.task.model.MedicationCategory
import de.gematik.ti.erp.app.task.model.TaskErpModel

internal class TaskLocalDataSourceV2(
    private val taskDao: ErpTaskDao,
    taskWithRefsDao: ErpTaskWithRefsDao,
    private val organizationDao: OrganizationDao,
    private val insuranceInformationDao: InsuranceInformationDao,
    private val ingredientDao: IngredientDao,
    communicationDao: CommunicationDao,
    private val medicationDispenseDao: MedicationDispenseDao,
    invoiceDao: InvoiceDao
) : TaskLocalDataSource,
    TaskLocalDataSourceCommon by TaskLocalDataSourceV2Common(taskDao, taskWithRefsDao, communicationDao, medicationDispenseDao, invoiceDao),
    TaskLocalDataSourceScanned by TaskLocalDataSourceV2Scanned(taskDao, taskWithRefsDao, communicationDao),
    TaskLocalDataSourceSynced by TaskLocalDataSourceV2Synced(
        taskDao,
        taskWithRefsDao,
        organizationDao,
        insuranceInformationDao,
        ingredientDao,
        medicationDispenseDao
    ),
    TaskLocalDataSourceDiga by TaskLocalDataSourceV2Diga(taskDao, taskWithRefsDao) {

    override suspend fun saveTask(task: TaskErpModel) {
        val bundle = task.toRoomEntityBundle()
        taskDao.upsertAll(listOf(bundle.task))

        bundle.patient?.let { taskDao.upsertPatients(listOf(it)) }
        bundle.practitioner?.let { taskDao.upsertPractitioners(listOf(it)) }
        bundle.organization?.let { organizationDao.upsertAll(listOf(it)) }
        bundle.insuranceInformation?.let { insuranceInformationDao.upsertAll(listOf(it)) }

        if (task is TaskErpModel.Synced.Prescription) {
            task.medicationRequest?.let { medicationRequest ->
                val medId = medicationIdFor(task.taskId)
                medicationRequest.medication?.let { med ->
                    taskDao.upsertMedications(listOf(med.toRoomEntity(medId, task.taskId, null)))
                    val ingredientEntities = med.ingredients.mapIndexed { index, ingredient ->
                        ingredient.toRoomEntity(ingredientIdFor(medId, index), medId, index.toString())
                    }
                    if (ingredientEntities.isNotEmpty()) {
                        ingredientDao.upsertAll(ingredientEntities)
                    }
                }
                val medicationRequestId = medicationRequestIdFor(task.taskId)
                taskDao.upsertMedicationRequest(listOf(medicationRequest.toRoomEntity(medicationRequestId, task.taskId, medId)))

                val mpInfo = medicationRequest.multiplePrescriptionInfo
                if (mpInfo.indicator) {
                    val mpId = multiplePrescriptionIdFor(task.taskId)
                    taskDao.upsertMultiplePrescriptions(listOf(mpInfo.toRoomEntity(mpId, task.taskId)))
                }
            }
            task.medicationDispenses.forEach { dispense ->
                val dispenseIdForMed = dispense.dispenseId ?: task.taskId
                val medId = dispensesMedicationIdFor(task.taskId, dispenseIdForMed)

                val medEntity = dispense.medication?.toRoomEntity(medId, task.taskId, dispense.dispenseId)
                    ?: ErpMedicationEntity(
                        medicationId = medId,
                        taskId = task.taskId,
                        ratioId = null,
                        parentDispenseId = dispense.dispenseId,
                        text = dispense.deviceRequest?.display ?: "",
                        medicationCategory = MedicationCategory.UNKNOWN.name,
                        form = "",
                        vaccine = false,
                        medicationProfile = null,
                        manufacturingInstructions = "",
                        packaging = "",
                        normSizeCode = "",
                        amount = null,
                        identifier = null,
                        pzn = dispense.deviceRequest?.referencePzn ?: ""
                    )
                taskDao.upsertMedications(listOf(medEntity))

                medicationDispenseDao.upsertAll(listOf(dispense.toRoomEntity(task.taskId, medId)))
            }
        }

        if (task is TaskErpModel.Synced.Diga) {
            task.deviceRequest?.let { deviceRequest ->
                val accidentId = deviceRequest.accident?.let { accident ->
                    val id = accidentInfoIdFor(task.taskId)
                    taskDao.upsertAccidentInfos(listOf(accident.toRoomEntity(id, task.taskId)))
                    id
                }
                val deviceRequestId = deviceRequestIdFor(task.taskId)
                taskDao.upsertDeviceRequests(listOf(deviceRequest.toRoomEntity(deviceRequestId, task.taskId, accidentId)))
            }
            task.medicationDispenses.forEach { dispense ->
                val dispenseIdForMed = dispense.dispenseId ?: task.taskId
                val medId = dispensesMedicationIdFor(task.taskId, dispenseIdForMed)

                val medEntity = dispense.medication?.toRoomEntity(medId, task.taskId, dispense.dispenseId)
                    ?: ErpMedicationEntity(
                        medicationId = medId,
                        taskId = task.taskId,
                        ratioId = null,
                        parentDispenseId = dispense.dispenseId,
                        text = dispense.deviceRequest?.display ?: "",
                        medicationCategory = MedicationCategory.UNKNOWN.name,
                        form = "",
                        vaccine = false,
                        medicationProfile = null,
                        manufacturingInstructions = "",
                        packaging = "",
                        normSizeCode = "",
                        amount = null,
                        identifier = null,
                        pzn = dispense.deviceRequest?.referencePzn ?: ""
                    )
                taskDao.upsertMedications(listOf(medEntity))

                medicationDispenseDao.upsertAll(listOf(dispense.toRoomEntity(task.taskId, medId)))
            }
        }
    }
}
