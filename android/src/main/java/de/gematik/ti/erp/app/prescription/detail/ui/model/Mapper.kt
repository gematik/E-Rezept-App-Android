/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.detail.ui.model

import de.gematik.ti.erp.app.db.entities.MedicationDispenseSimple
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.pharmacy.usecase.model.UIPrescriptionOrder
import de.gematik.ti.erp.app.prescription.repository.InsuranceCompanyDetail
import de.gematik.ti.erp.app.prescription.repository.MedicationDetail
import de.gematik.ti.erp.app.prescription.repository.MedicationRequestDetail
import de.gematik.ti.erp.app.prescription.repository.OrganizationDetail
import de.gematik.ti.erp.app.prescription.repository.PatientDetail
import de.gematik.ti.erp.app.prescription.repository.PractitionerDetail
import de.gematik.ti.erp.app.redeem.ui.BitMatrixCode

fun mapToUIPrescriptionDetailScanned(
    task: Task,
    matrix: BitMatrixCode?,
    unRedeemMorePossible: Boolean
): UIPrescriptionDetailScanned {
    return UIPrescriptionDetailScanned(
        taskId = task.taskId,
        redeemedOn = task.redeemedOn,
        accessCode = task.accessCode,
        number = requireNotNull(task.nrInScanSession),
        scannedOn = requireNotNull(task.scannedOn),
        bitmapMatrix = matrix,
        unRedeemMorePossible = unRedeemMorePossible
    )
}

fun mapToUIPrescriptionDetailSynced(
    task: Task,
    medication: MedicationDetail,
    medicationRequest: MedicationRequestDetail,
    medicationDispense: MedicationDispenseSimple?,
    insurance: InsuranceCompanyDetail,
    organization: OrganizationDetail,
    patient: PatientDetail,
    practitioner: PractitionerDetail,
    matrix: BitMatrixCode?
): UIPrescriptionDetailSynced {
    return UIPrescriptionDetailSynced(
        taskId = task.taskId,
        taskStatus = task.status,
        redeemedOn = task.redeemedOn,
        accessCode = task.accessCode,
        redeemUntil = task.expiresOn,
        acceptUntil = task.acceptUntil,
        bitmapMatrix = matrix,
        practitioner = practitioner,
        organization = organization,
        patient = patient,
        insurance = insurance,
        medication = medication,
        medicationRequest = medicationRequest,
        medicationDispense = medicationDispense
    )
}

fun mapToUIPrescriptionOrder(
    task: Task,
    medication: MedicationDetail,
    medicationRequest: MedicationRequestDetail,
    patient: PatientDetail,
): UIPrescriptionOrder {
    val uiPrescriptionOrder = UIPrescriptionOrder(
        taskId = task.taskId,
        accessCode = task.accessCode!!,
        title = medication.text,
        substitutionsAllowed = medicationRequest.substitutionAllowed,
    )
    uiPrescriptionOrder.address = patient.address ?: ""
    uiPrescriptionOrder.patientName = patient.name ?: ""
    return uiPrescriptionOrder
}
