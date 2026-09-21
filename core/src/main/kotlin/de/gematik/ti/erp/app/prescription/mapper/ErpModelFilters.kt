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
package de.gematik.ti.erp.app.prescription.mapper

import de.gematik.ti.erp.app.task.model.TaskErpModel

@JvmName("filterScannedNonActiveTasks")
fun List<TaskErpModel.Scanned>.filterNonActiveTasks() = filter { it.redeemedOn != null }

@JvmName("filterNonActiveDigaTasks")
fun List<TaskErpModel.Synced.Diga>.filterNonActiveDigaTasks() =
    filter { it.deviceRequest != null && it.deviceRequest?.isArchived == true }

@JvmName("filterScannedActiveTasks")
fun List<TaskErpModel.Scanned>.filterActivePrescriptionTasks() = filter { it.redeemedOn == null }

@JvmName("filterActiveDigaTasks")
fun List<TaskErpModel.Synced.Diga>.filterActiveDigaTasks() =
    filter { it.deviceRequest != null && it.deviceRequest?.isArchived == false }

@JvmName("filterErpModelSyncedNonActiveTasks")
fun List<TaskErpModel.Synced.Prescription>.filterNonActiveTasks() = filter { !it.isActive() }

@JvmName("filterSyncedActiveTasks")
fun List<TaskErpModel.Synced.Prescription>.filterActivePrescriptionTasks() = filter { it.isActive() }

@JvmName("sortSyncedByExpiredDateAndAuthoredDate")
fun List<TaskErpModel.Synced>.sortByExpiredDateAndAuthoredDate(): List<TaskErpModel.Synced> =
    sortedWith(compareBy<TaskErpModel.Synced> { it.expiresOn }.thenBy { it.authoredOn })

@JvmName("groupSyncedByHospitalsOrDoctors")
fun List<TaskErpModel.Synced>.groupByHospitalsOrDoctors(): Map<String?, List<TaskErpModel.Synced>> =
    groupBy { it.practitioner?.name ?: it.organization?.name }

@JvmName("flatMapToBaseErpModel")
fun Map<String?, List<TaskErpModel.Synced>>.flatMapToBaseErpModel(): List<TaskErpModel> =
    flatMap { (_, tasks) -> tasks }
