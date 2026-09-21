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

package de.gematik.ti.erp.app.prescription.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.datetime.Instant

data class FullDetailDigaPreviewData(
    val name: String,
    val now: Instant,
    val prescription: TaskErpModel.Synced.Diga
)

data class FullDetailDigaPreviewDates(
    val now: Instant = Instant.parse("2022-12-06T14:49:46Z"),
    val authoredOn: Instant = Instant.parse("2022-12-06T14:49:46Z"),
    val expiresOn: Instant = Instant.parse("2022-12-12T14:49:46Z"),
    val acceptUntil: Instant = Instant.parse("2022-12-12T14:49:46Z")
)

@Suppress("MagicNumber")
fun digaSyncedPrescriptionPreviewData(
    name: String,
    previewDates: FullDetailDigaPreviewDates = FullDetailDigaPreviewDates(),
    status: DigaStatus = DigaStatus.Ready,
    taskStatus: TaskStatusEnum = TaskStatusEnum.Ready
) = FullDetailDigaPreviewData(
    name = name,
    now = previewDates.now,
    prescription = TaskErpModel.Synced.Diga(
        profileId = "profile-id-1",
        taskId = "1",
        name = "Power Puff",
        accessCode = "access-code-1",
        isEuRedeemable = false,
        isEuRedeemableByPatientAuthorization = false,
        lastModified = Instant.fromEpochSeconds(123456),
        organization = null,
        practitioner = null,
        patient = null,
        insuranceInformation = null,
        expiresOn = previewDates.expiresOn,
        acceptUntil = previewDates.acceptUntil,
        authoredOn = previewDates.authoredOn,
        status = taskStatus,
        isIncomplete = false,
        pvsIdentifier = "",
        failureToReport = "",
        currentTime = previewDates.now,
        deviceRequest = null
    )
)

class FullDetailDigaPreviewProvider : PreviewParameterProvider<FullDetailDigaPreviewData> {
    override val values: Sequence<FullDetailDigaPreviewData>
        get() = sequenceOf(
            digaSyncedPrescriptionPreviewData(
                name = "Ready in 6 days"
            ),
            digaSyncedPrescriptionPreviewData(
                name = "InProgress",
                status = DigaStatus.InProgress(Instant.parse("2024-07-01T10:00:00Z")),
                previewDates = FullDetailDigaPreviewDates(
                    now = Instant.parse("2022-12-06T14:49:46Z")
                )
            ),
            digaSyncedPrescriptionPreviewData(
                name = "Completed",
                status = DigaStatus.CompletedSuccessfully,
                taskStatus = TaskStatusEnum.Completed,
                previewDates = FullDetailDigaPreviewDates(
                    now = Instant.parse("2025-12-06T14:49:46Z")
                )
            ),
            digaSyncedPrescriptionPreviewData(
                name = "Completed Wrong",
                status = DigaStatus.CompletedWithRejection(Instant.parse("2024-08-01T10:00:00Z")),
                taskStatus = TaskStatusEnum.Completed,
                previewDates = FullDetailDigaPreviewDates(
                    now = Instant.parse("2025-12-06T14:49:46Z")
                )
            ),
            digaSyncedPrescriptionPreviewData(
                name = "Ready for Archive",
                status = DigaStatus.ReadyForSelfArchiveDiga,
                taskStatus = TaskStatusEnum.Completed,
                previewDates = FullDetailDigaPreviewDates(
                    now = Instant.parse("2022-12-06T14:49:46Z"),
                    acceptUntil = Instant.parse("2022-12-07T14:49:46Z"),
                    expiresOn = Instant.parse("2022-12-07T14:49:46Z")
                )
            )
        )
}
