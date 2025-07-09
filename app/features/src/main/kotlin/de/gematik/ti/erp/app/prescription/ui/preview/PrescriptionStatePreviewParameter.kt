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
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

data class PrescriptionStatePreview(
    val name: String, // a description for better understanding
    val prescriptionState: SyncedTaskData.SyncedTask.TaskState,
    val now: Instant
)

// see https://wiki.gematik.de/pages/viewpage.action?pageId=488575023&preview=/488575023/532687126/G%C3%BCltigkeitsangaben.xlsx
private val creationDateTime = Instant.parse("2023-01-01T16:20:00Z")

private val creationDate = creationDateTime.toLocalDateTime(
    TimeZone.of("Europe/Berlin")
).date.atStartOfDayIn(
    TimeZone.of("Europe/Berlin")
)
private val acceptUntil = creationDateTime.plus(27.days).toLocalDateTime(
    TimeZone.of("Europe/Berlin")
).date.atStartOfDayIn(
    TimeZone.of("Europe/Berlin")
)
private val expiryDate = Instant.parse("2023-04-01T16:20:00Z").toLocalDateTime(
    TimeZone.of("Europe/Berlin")
).date.atStartOfDayIn(
    TimeZone.of("Europe/Berlin")
)

val prescriptionStatePreviews: Sequence<PrescriptionStatePreview>
    get() = sequenceOf(
        PrescriptionStatePreview(
            name = "ready state with 0 days gone (valid for 27 days more than today)",
            prescriptionState = SyncedTaskData.SyncedTask.Ready(
                expiresOn = expiryDate,
                acceptUntil = acceptUntil
            ),
            now = creationDate
        ),
        PrescriptionStatePreview(
            name = "ready state with 24 days gone (valid for 3 days more than today)",
            prescriptionState = SyncedTaskData.SyncedTask.Ready(
                expiresOn = expiryDate,
                acceptUntil = acceptUntil
            ),
            now = creationDate.plus(24.days)
        ),
        PrescriptionStatePreview(
            name = "ready state with 25 days gone (valid for 2 days more than today)",
            prescriptionState = SyncedTaskData.SyncedTask.Ready(
                expiresOn = expiryDate,
                acceptUntil = acceptUntil
            ),
            now = creationDate.plus(25.days)
        ),
        PrescriptionStatePreview(
            name = "ready state with 26 days gone (valid for 1 day more than today)",
            prescriptionState = SyncedTaskData.SyncedTask.Ready(
                expiresOn = expiryDate,
                acceptUntil = acceptUntil
            ),
            now = creationDate.plus(26.days)
        ),
        PrescriptionStatePreview(
            name = "ready state with 27 days gone (only valid today)",
            prescriptionState = SyncedTaskData.SyncedTask.Ready(
                expiresOn = expiryDate,
                acceptUntil = acceptUntil
            ),
            now = creationDate.plus(27.days)
        ),
        PrescriptionStatePreview(
            name = "ready state with 28 days gone (61 days more than today as self payer)",
            prescriptionState = SyncedTaskData.SyncedTask.Ready(
                expiresOn = expiryDate,
                acceptUntil = acceptUntil
            ),
            now = creationDate.plus(28.days)
        ),
        PrescriptionStatePreview(
            name = "ready state with 85 days gone (valid for 3 days as self payer)",
            prescriptionState = SyncedTaskData.SyncedTask.Ready(
                expiresOn = expiryDate,
                acceptUntil = acceptUntil
            ),
            now = creationDate.plus(85.days)
        ),
        PrescriptionStatePreview(
            name = "ready state with 86 days gone (valid for 2 days as self payer)",
            prescriptionState = SyncedTaskData.SyncedTask.Ready(
                expiresOn = expiryDate,
                acceptUntil = acceptUntil
            ),
            now = creationDate.plus(86.days)
        ),
        PrescriptionStatePreview(
            name = "ready state with 88 days gone (valid until tomorrow as self payer)",
            prescriptionState = SyncedTaskData.SyncedTask.Ready(
                expiresOn = expiryDate,
                acceptUntil = acceptUntil
            ),
            now = creationDate.plus(87.days)
        ),
        PrescriptionStatePreview(
            name = "ready state with 88 days gone (only valid today as self payer)",
            prescriptionState = SyncedTaskData.SyncedTask.Ready(
                expiresOn = expiryDate,
                acceptUntil = acceptUntil
            ),
            now = creationDate.plus(88.days)
        ),
        PrescriptionStatePreview(
            name = "provided state after 5 minutes",
            prescriptionState = SyncedTaskData.SyncedTask.Provided(
                lastMedicationDispense = creationDate.plus(5.minutes)
            ),
            now = creationDate.plus(10.minutes)
        ),
        PrescriptionStatePreview(
            name = "provided state after 10 days",
            prescriptionState = SyncedTaskData.SyncedTask.Provided(
                lastMedicationDispense = creationDate.plus(10.days)
            ),
            now = creationDate.plus(20.days)
        ),
        PrescriptionStatePreview(
            name = "provided state after 2 hours",
            prescriptionState = SyncedTaskData.SyncedTask.Provided(
                lastMedicationDispense = creationDate.plus(1.hours)
            ),
            now = creationDate.plus(3.hours)
        )

        // toDo: add more states such as later redeemable for multiple prescriptions and so on
    )

private val serverDate = Instant.parse("2023-01-01T22:00:00Z")

val prescriptionStatePreviewsNearDayEnd: Sequence<PrescriptionStatePreview>
    get() = sequenceOf(
        PrescriptionStatePreview(
            name = "Server: $serverDate, " +
                "Actual: ${serverDate.toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.Pending(
                sentOn = serverDate,
                toTelematikId = ""
            ),
            now = serverDate.plus(1.hours)
        ),
        PrescriptionStatePreview(
            name = "Server: ${serverDate.plus(1.hours)}, " +
                "Actual: ${serverDate.plus(1.hours).toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.Pending(
                sentOn = serverDate.plus(1.hours),
                toTelematikId = ""
            ),
            now = serverDate.plus(2.hours)
        ),
        PrescriptionStatePreview(
            name = "Server: ${serverDate.plus(2.hours)}, " +
                "Actual: ${serverDate.plus(2.hours).toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.Pending(
                sentOn = serverDate.plus(2.hours),
                toTelematikId = ""
            ),
            now = serverDate.plus(3.hours)
        ),
        PrescriptionStatePreview(
            name = "Server: ${serverDate.plus(3.hours)}, " +
                "Actual: ${serverDate.plus(3.hours).toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.Pending(
                sentOn = serverDate.plus(3.hours),
                toTelematikId = ""
            ),
            now = serverDate.plus(4.hours)
        ),
        PrescriptionStatePreview(
            name = "Server: $serverDate, " +
                "Actual: ${serverDate.toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.InProgress(
                lastModified = serverDate
            ),
            now = serverDate.plus(1.hours)
        ),
        PrescriptionStatePreview(
            name = "Server: ${serverDate.plus(1.hours)}, " +
                "Actual: ${serverDate.plus(1.hours).toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.InProgress(
                lastModified = serverDate.plus(1.hours)
            ),
            now = serverDate.plus(2.hours)
        ),
        PrescriptionStatePreview(
            name = "Server: ${serverDate.plus(2.hours)}, " +
                "Actual: ${serverDate.plus(2.hours).toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.InProgress(
                lastModified = serverDate.plus(2.hours)
            ),
            now = serverDate.plus(3.hours)
        ),
        PrescriptionStatePreview(
            name = "Server: ${serverDate.plus(3.hours)}, " +
                "Actual: ${serverDate.plus(3.hours).toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.InProgress(
                lastModified = serverDate.plus(3.hours)
            ),
            now = serverDate.plus(4.hours)
        ),
        PrescriptionStatePreview(
            name = "Server: $serverDate, " +
                "Actual: ${serverDate.toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.Provided(
                lastMedicationDispense = serverDate
            ),
            now = serverDate.plus(1.hours)
        ),
        PrescriptionStatePreview(
            name = "Server: ${serverDate.plus(1.hours)}, " +
                "Actual: ${serverDate.plus(1.hours).toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.Provided(
                lastMedicationDispense = serverDate.plus(1.hours)
            ),
            now = serverDate.plus(2.hours)
        ),
        PrescriptionStatePreview(
            name = "Server: ${serverDate.plus(2.hours)}, " +
                "Actual: ${serverDate.plus(2.hours).toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.Provided(
                lastMedicationDispense = serverDate.plus(2.hours)
            ),
            now = serverDate.plus(3.hours)
        ),
        PrescriptionStatePreview(
            name = "Server: ${serverDate.plus(3.hours)}, " +
                "Actual: ${serverDate.plus(3.hours).toLocalDateTime(
                    TimeZone.of("Europe/Berlin")
                )}",
            prescriptionState = SyncedTaskData.SyncedTask.Provided(
                lastMedicationDispense = serverDate.plus(3.hours)
            ),
            now = serverDate.plus(4.hours)
        )
        // toDo: add more states such as later redeemable for multiple prescriptions and so on
    )
class PrescriptionStatePreviewParameterProvider : PreviewParameterProvider<PrescriptionStatePreview> {
    override val values = prescriptionStatePreviews
}
