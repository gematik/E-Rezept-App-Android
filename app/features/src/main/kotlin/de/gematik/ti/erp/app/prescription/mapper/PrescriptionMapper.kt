/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.prescription.mapper

import de.gematik.ti.erp.app.prescription.model.ScannedTaskData.ScannedTask
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.SyncedTask
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription

internal fun ScannedTask.toPrescription() = Prescription.ScannedPrescription(
    taskId = taskId,
    scannedOn = scannedOn,
    name = name,
    index = index,
    redeemedOn = redeemedOn,
    communications = communications
)

internal fun SyncedTask.toPrescription() = Prescription.SyncedPrescription(
    taskId = taskId,
    isIncomplete = isIncomplete,
    name = medicationName(),
    organization = practitioner.name ?: organization.name ?: "",
    authoredOn = authoredOn,
    redeemedOn = redeemedOn(),
    expiresOn = expiresOn,
    acceptUntil = acceptUntil,
    state = state(),
    isDirectAssignment = isDirectAssignment(),
    prescriptionChipInformation = Prescription.PrescriptionChipInformation(
        isSelfPayPrescription = insuranceInformation
            .coverageType == SyncedTaskData.CoverageType.SEL,
        isPartOfMultiplePrescription = medicationRequest
            .multiplePrescriptionInfo.indicator,
        numerator = medicationRequest.multiplePrescriptionInfo
            .numbering?.numerator?.value,
        denominator = medicationRequest.multiplePrescriptionInfo
            .numbering?.denominator?.value,
        start = medicationRequest.multiplePrescriptionInfo.start
    )
)

@JvmName("filterScannedNonActiveTasks")
internal fun List<ScannedTask>.filterNonActiveTasks() =
    filter { it.redeemedOn != null }

@JvmName("filterScannedActiveTasks")
internal fun List<ScannedTask>.filterActiveTasks() =
    filter { it.redeemedOn == null }

@JvmName("filterSyncedNonActiveTasks")
internal fun List<SyncedTask>.filterNonActiveTasks() = filter { !it.isActive() }

@JvmName("filterSyncedActiveTasks")
internal fun List<SyncedTask>.filterActiveTasks() = filter { it.isActive() }

internal fun List<SyncedTask>.sortByExpiredDateAndAuthoredDate() =
    sortedWith(compareBy<SyncedTask> { it.expiresOn }.thenBy { it.authoredOn })

internal fun List<SyncedTask>.groupByHospitalsOrDoctors() =
    groupBy { it.practitioner.name ?: it.organization.name }

internal fun Map<String?, List<SyncedTask>>.flatMapToPrescriptions() =
    flatMap { (_, tasks) -> tasks.map(SyncedTask::toPrescription) }
