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
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier

import java.time.Instant

interface UIPrescriptionDetail {
    val profileId: ProfileIdentifier
    val taskId: String
    val redeemedOn: Instant?
    val accessCode: String?
    val matrixPayload: String?
}

@Immutable
data class UIPrescriptionDetailScanned(
    override val profileId: ProfileIdentifier,
    override val taskId: String,
    override val redeemedOn: Instant?,
    override val accessCode: String?,
    override val matrixPayload: String?,
    val number: Int,
    val scannedOn: Instant
) : UIPrescriptionDetail

@Immutable
data class UIPrescriptionDetailSynced(
    override val profileId: ProfileIdentifier,
    override val taskId: String,
    override val redeemedOn: Instant?,
    override val accessCode: String?,
    override val matrixPayload: String?,
    val state: SyncedTaskData.SyncedTask.TaskState,
    val isRedeemableAndValid: Boolean,
    val expiresOn: Instant?,
    val acceptUntil: Instant?,
    val patient: SyncedTaskData.Patient,
    val practitioner: SyncedTaskData.Practitioner,
    val insurance: SyncedTaskData.InsuranceInformation,
    val organization: SyncedTaskData.Organization,
    val medicationRequest: SyncedTaskData.MedicationRequest,
    val medicationDispenses: List<SyncedTaskData.MedicationDispense>,
    val taskStatus: SyncedTaskData.TaskStatus?
) : UIPrescriptionDetail
