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

import androidx.compose.runtime.Immutable
import de.gematik.ti.erp.app.db.entities.MedicationDispenseSimple
import de.gematik.ti.erp.app.db.entities.TaskStatus
import de.gematik.ti.erp.app.prescription.repository.InsuranceCompanyDetail
import de.gematik.ti.erp.app.prescription.repository.MedicationDetail
import de.gematik.ti.erp.app.prescription.repository.MedicationRequestDetail
import de.gematik.ti.erp.app.prescription.repository.OrganizationDetail
import de.gematik.ti.erp.app.prescription.repository.PatientDetail
import de.gematik.ti.erp.app.prescription.repository.PractitionerDetail
import de.gematik.ti.erp.app.redeem.ui.BitMatrixCode
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

interface UIPrescriptionDetail {
    val taskId: String
    val redeemedOn: OffsetDateTime?
    val accessCode: String?
    val bitmapMatrix: BitMatrixCode?
}

@Immutable
data class UIPrescriptionDetailScanned(
    override val taskId: String,
    override val redeemedOn: OffsetDateTime?,
    override val accessCode: String?,
    override val bitmapMatrix: BitMatrixCode?,
    val number: Int,
    val scannedOn: OffsetDateTime,
    val unRedeemMorePossible: Boolean
) : UIPrescriptionDetail {

    fun formattedScannedInfo(text: String): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")
        return scannedOn.format(formatter).replace("-", text)
    }
}

@Immutable
data class UIPrescriptionDetailSynced(
    override val taskId: String,
    override val redeemedOn: OffsetDateTime?,
    override val accessCode: String?,
    override val bitmapMatrix: BitMatrixCode?,
    val redeemUntil: LocalDate?,
    val acceptUntil: LocalDate?,
    val patient: PatientDetail,
    val practitioner: PractitionerDetail,
    val medication: MedicationDetail,
    val insurance: InsuranceCompanyDetail,
    val organization: OrganizationDetail,
    val medicationRequest: MedicationRequestDetail,
    val medicationDispense: MedicationDispenseSimple?,
    val taskStatus: TaskStatus?
) : UIPrescriptionDetail
